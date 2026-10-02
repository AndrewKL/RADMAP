package org.jmol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3f;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.shape.Mesh;
import org.jmol.viewer.Viewer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Psi4EngineTest {

  private static final String MOLECULE = "C     0.66950    0.00000    0.00000\n"
      + "C    -0.66950    0.00000    0.00000\n";

  @TempDir
  Path dir;

  /** A Psi4 output: the echoed input, some program text, then RADMAP's lines. */
  private Path psi4Out(String name, String input, String... radmapLines)
      throws IOException {
    List<String> lines = new ArrayList<String>();
    lines.add("    Psi4: An Open-Source Ab Initio Electronic Structure Package");
    lines.add("  ==> Input File <==");
    lines.add("--------------------------------------------------------------------------");
    lines.addAll(Arrays.asList(input.split("\n")));
    lines.add("--------------------------------------------------------------------------");
    lines.add("    Total Energy =                        -79.1234567890");
    lines.addAll(Arrays.asList(radmapLines));
    return Files.write(dir.resolve(name), lines);
  }

  private static String input(boolean withForces) {
    return Psi4Engine.generateInput("1GB", "B3LYP-D3BJ", "def2-TZVP", "", 0,
        2, MOLECULE, "H", new Point3f(1.5f, -2.25f, 0.125f), withForces);
  }

  @Test
  void memoryLineSeparatesTheUnit() {
    assertEquals("memory 500 MB", Psi4Engine.memoryLine("500MB"));
    assertEquals("memory 15 GB", Psi4Engine.memoryLine("15GB"));
    assertEquals("", Psi4Engine.memoryLine("Default"));
    assertEquals("", Psi4Engine.memoryLine(null));
  }

  @Test
  void energyInputDescribesTheMoleculeWithTheProbeAppended() {
    List<String> lines = Arrays.asList(input(false).split("\n"));

    assertEquals("# RADMAP_PROBE H 1.5 -2.25 0.125", lines.get(0));
    assertEquals("memory 1 GB", lines.get(1));
    int start = lines.indexOf("molecule {");
    assertEquals(Arrays.asList("molecule {", "0 2",
        "C     0.66950    0.00000    0.00000",
        "C    -0.66950    0.00000    0.00000", "H 1.5 -2.25 0.125",
        "symmetry c1", "no_reorient", "no_com", "}"), lines.subList(start,
        start + 9));
    assertTrue(lines.contains("set reference uhf"));
    assertTrue(lines.contains("set basis def2-TZVP"));
    assertTrue(lines.contains("energy('b3lyp-d3bj')"));
    assertTrue(lines.contains("psi4.core.print_out('RADMAP_PROBE H 1.5 -2.25 0.125\\n')"));
    assertTrue(lines.contains("psi4.core.print_out('RADMAP_ENERGY %.10f\\n' % psi4.variable('CURRENT ENERGY'))"));
    assertFalse(input(false).contains("gradient"));
  }

  @Test
  void singletsUseARestrictedReference() {
    String text = Psi4Engine.generateInput("Default", "hf", "6-31G", null, 0,
        1, MOLECULE, "H", new Point3f(), false);
    assertTrue(text.contains("set reference rhf\n"));
    assertFalse(text.contains("memory"));
  }

  @Test
  void optionsBecomeSetLines() {
    String text = Psi4Engine.generateInput("Default", "hf", "6-31G",
        "soscf true; maxiter 200 ;", 0, 2, MOLECULE, "H", new Point3f(), false);
    assertTrue(text.contains("set soscf true\nset maxiter 200\n\n"), text);
  }

  @Test
  void forcesInputRunsAGradientAndPrintsEveryAtom() {
    String text = input(true);
    assertTrue(text.contains("radmap_gradient = gradient('b3lyp-d3bj').np\n"));
    assertTrue(text.contains("for radmap_i in range(radmap_molecule.natom()):\n"));
    assertTrue(text.contains("RADMAP_FORCE %d"));
    assertFalse(text.contains("\nenergy("));
  }

  @Test
  void filesAreWrittenForEachNonNullVertexWithARunScript() throws IOException {
    Mesh mesh = new Mesh("test", null, (short) 0, 0);
    mesh.vertices = new Point3f[] { new Point3f(1, 2, 3), null,
        new Point3f(4, 5, 6) };

    int count = Psi4Engine.generateFiles(mesh, dir.toString(), "1GB", 2,
        "b3lyp", "6-31G", "", 0, 2, MOLECULE, "H", false);

    assertEquals(2, count);
    assertTrue(Files.readAllLines(dir.resolve("RA_0.in")).contains(
        "H 1.0 2.0 3.0"));
    assertFalse(Files.exists(dir.resolve("RA_1.in")));
    assertTrue(Files.readAllLines(dir.resolve("RA_2.in")).contains(
        "H 4.0 5.0 6.0"));
    Path script = dir.resolve(Psi4Engine.RUN_SCRIPT_NAME);
    assertTrue(Files.isExecutable(script));
    String text = new String(Files.readAllBytes(script));
    assertTrue(text.startsWith("#!/bin/sh\n"));
    assertTrue(text.contains("THREADS=${THREADS:-2}"));
    assertTrue(text.contains("psi4 -n \"$THREADS\" -i \"$f\" -o \"${f%.in}.out\""));
    assertEquals(Arrays.asList("RA_0.in", "RA_2.in"), Psi4Engine
        .readJobList(dir.toString()));
  }

  @Test
  void generatingIntoAMissingFolderReportsFailure() {
    Mesh mesh = new Mesh("test", null, (short) 0, 0);
    mesh.vertices = new Point3f[] { new Point3f() };
    assertEquals(-1, Psi4Engine.generateFiles(mesh, dir.resolve("missing")
        .toString(), "1GB", 1, "b3lyp", "6-31G", "", 0, 2, MOLECULE, "H",
        false));
  }

  @Test
  void energyIsReadFromTheTaggedLinesNotTheEchoedInput() throws IOException {
    Path out = psi4Out("RA_0.out", input(false),
        "RADMAP_PROBE H 1.5 -2.25 0.125", "RADMAP_ENERGY -79.1234567890");

    assertTrue(Psi4Engine.isPsi4Output(out.toString()));
    DataVertex vertex = Psi4Engine.readEnergy(out.toString());
    assertEquals(new Point3f(1.5f, -2.25f, 0.125f), vertex.xyz);
    assertEquals(-79.12346f, vertex.energy, 1e-4f);
  }

  @Test
  void unfinishedJobIsRecognisedButGivesNoData() throws IOException {
    Path out = psi4Out("RA_0.out", input(true));

    assertTrue(Psi4Engine.isPsi4Output(out.toString()));
    assertNull(Psi4Engine.readEnergy(out.toString()));
    assertNull(Psi4Engine.readForces(out.toString()));
  }

  @Test
  void gaussianOutputIsNotMistakenForPsi4() throws IOException {
    Path out = GaussianFixtures.energyOut(dir, "RA  0.out", "1 2 3", "-76.5");
    assertFalse(Psi4Engine.isPsi4Output(out.toString()));
  }

  @Test
  void forcesAreReadPerAtom() throws IOException {
    Path out = psi4Out("RA_0.out", input(true), "RADMAP_PROBE H 0.0 2.0 0.0",
        "RADMAP_FORCE 0 1.00000000 0.00000000 0.00000000 0.20000000 0.30000000 0.00000000",
        "RADMAP_FORCE 1 0.00000000 0.00000000 0.00000000 0.00000000 0.00000000 0.00000000",
        "RADMAP_ENERGY -79.5");

    Psi4Engine.Forces forces = Psi4Engine.readForces(out.toString());

    assertEquals(new Point3f(0, 2, 0), forces.probe);
    assertEquals(2, forces.locations.length);
    assertEquals(new Point3f(1, 0, 0), forces.locations[0]);
    assertEquals(new Point3f(0.2f, 0.3f, 0), forces.forces[0]);
  }

  @Test
  void energyOnlyOutputHasNoForces() throws IOException {
    Path out = psi4Out("RA_0.out", input(false), "RADMAP_PROBE H 0 2 0",
        "RADMAP_ENERGY -79.5");
    assertNull(Psi4Engine.readForces(out.toString()));
  }

  @Test
  void surfaceLoadersAcceptPsi4Output() throws IOException {
    Path out = psi4Out("RA_0.out", input(true), "RADMAP_PROBE H 0.0 2.0 0.0",
        "RADMAP_FORCE 0 1.0 0.0 0.0 0.2 0.3 0.0",
        "RADMAP_FORCE 1 0.0 0.0 0.0 0.0 0.0 0.0", "RADMAP_ENERGY -79.5");

    assertEquals(-79.5f, isosurfacePES.loadGaussianOutFile(out.toString()).energy);
    // same geometry and forces as the Gaussian case in DEDXSurfaceUtilsTest
    assertEquals(0.3f, DEDXSurfaceUtils.readFlexDataFromGaussianOutFile(
        out.toString(), 2f, 0.5f).energy, 1e-5f);
    assertEquals((float) Math.sqrt(0.13), DEDXSurfaceUtils
        .readTotalDEDXDataFromGaussianOutFile(out.toString()).energy, 1e-5f);
  }

  @Test
  void folderLoadMixesGaussianAndPsi4Outputs() throws IOException {
    GaussianFixtures.energyOut(dir, "RA  0.out", "1 0 0", "-76.5");
    psi4Out("RA_1.out", input(false), "RADMAP_PROBE H 0 1 0",
        "RADMAP_ENERGY -76.25");
    psi4Out("RA_2.out", input(false)); // unfinished

    assertEquals(2, isosurfacePES.loadGaussianOutFiles(dir.toString()).length);
  }

  @Test
  void generateThroughTheViewerWritesOneInputPerSurfacePoint()
      throws IOException {
    Path xyz = Files.write(dir.resolve("ethylene.xyz"), Arrays.asList("6",
        "ethylene", "C 0.6695 0 0", "C -0.6695 0 0", "H 1.2321 0.9289 0",
        "H 1.2321 -0.9289 0", "H -1.2321 0.9289 0", "H -1.2321 -0.9289 0"));
    Viewer viewer = (Viewer) JmolViewer.allocateViewer(null,
        new SmarterJmolAdapter());
    viewer.runScriptImmediately("load \""
        + xyz.toString().replace('\\', '/') + "\"");
    Path jobs = Files.createDirectory(dir.resolve("jobs"));

    int count = isosurfacePES.generatePsi4files(viewer, 0, 2, 1.5f, 1.2f, "H",
        jobs.toString(), "ethylene", "1GB", 1, "b3lyp", "6-31G", "", true);

    assertTrue(count > 10, "expected many sample points, got " + count);
    List<String> first = Files.readAllLines(jobs.resolve("RA_0.in"));
    int start = first.indexOf("molecule {");
    assertTrue(first.get(start + 2).startsWith("C "), first.get(start + 2));
    assertEquals("# RADMAP_PROBE " + first.get(start + 8), first.get(0));
    assertTrue(Files.exists(jobs.resolve(Psi4Engine.RUN_SCRIPT_NAME)));
    assertTrue(Files.exists(jobs.resolve("ethylene.mol")));
  }
}
