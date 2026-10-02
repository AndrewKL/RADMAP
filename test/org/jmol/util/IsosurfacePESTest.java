package org.jmol.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3f;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.shape.Mesh;
import org.jmol.shape.MeshCollection;
import org.jmol.shape.Shape;
import org.jmol.viewer.Viewer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IsosurfacePESTest {

  @TempDir
  Path dir;

  @Test
  void gaussianInputFileHasSectionsSeparatedByBlankLines() throws IOException {
    Path file = dir.resolve("job.gjf");
    int result = isosurfacePES.generateGaussianInputFile(file.toString(),
        "%mem=1GB", "# UB3LYP/6-31G", "RA H 0 1.0 2.0 3.0", -1, 2,
        "O 0.0 0.0 0.0");

    assertEquals(0, result);
    assertEquals(Arrays.asList("%mem=1GB", "", "# UB3LYP/6-31G", "",
        "RA H 0 1.0 2.0 3.0", "", "-1 2", "O 0.0 0.0 0.0"),
        Files.readAllLines(file));
  }

  @Test
  void gaussianInputFileReportsUnwritablePath() {
    String missing = dir.resolve("no-such-folder").resolve("job.gjf").toString();
    assertEquals(-1, isosurfacePES.generateGaussianInputFile(missing, "", "",
        "", 0, 1, ""));
  }

  @Test
  void batchFileListsOneJobPerNonNullVertex() throws IOException {
    Mesh mesh = new Mesh("test", null, (short) 0, 0);
    mesh.vertices = new Point3f[] { new Point3f(), null, new Point3f() };

    isosurfacePES.generateBatchFileForIsosurface(mesh, dir.toString());

    assertEquals(Arrays.asList("!", "! batchfile list", "!start=1", "!",
        "RA  0.gjf , RA  0.out", "RA  2.gjf , RA  2.out"),
        Files.readAllLines(dir.resolve("RA Batch File.bcf")));
  }

  @Test
  void outFileGivesProbePositionAndLastScfEnergy() throws IOException {
    Path out = GaussianFixtures.energyOut(dir, "RA  0.out", "1.5 -2.25 0.125",
        "-76.4124");

    DataVertex vertex = isosurfacePES.loadGaussianOutFile(out.toString());

    assertEquals(new Point3f(1.5f, -2.25f, 0.125f), vertex.xyz);
    assertEquals(-76.4124f, vertex.energy, 1e-4f);
  }

  @Test
  void outFileWithoutScfEnergyIsSkipped() throws IOException {
    Path out = GaussianFixtures.energyOut(dir, "RA  0.out", "1 2 3", null);
    assertNull(isosurfacePES.loadGaussianOutFile(out.toString()));
  }

  @Test
  void outFileWithoutTitleIsSkipped() throws IOException {
    Path out = Files.write(dir.resolve("RA  0.out"), Arrays.asList(
        " Entering Gaussian System",
        " SCF Done:  E(UB3LYP) =  -76.4124     A.U. after   12 cycles"));
    assertNull(isosurfacePES.loadGaussianOutFile(out.toString()));
  }

  @Test
  void folderLoadReadsOnlyRadmapOutputsAndWritesDataFile() throws IOException {
    GaussianFixtures.energyOut(dir, "RA  0.out", "1 0 0", "-76.5");
    GaussianFixtures.energyOut(dir, "RA  1.out", "0 1 0", "-76.25");
    GaussianFixtures.energyOut(dir, "RA  2.out", "0 0 1", null);
    GaussianFixtures.energyOut(dir, "other.out", "9 9 9", "-1.5");
    GaussianFixtures.energyOut(dir, "RA  3.log", "9 9 9", "-1.5");

    DataVertex[] data = isosurfacePES.loadGaussianOutFiles(dir.toString());

    float[] energies = new float[data.length];
    for (int i = 0; i < data.length; i++)
      energies[i] = data[i].energy;
    Arrays.sort(energies);
    assertArrayEquals(new float[] { -76.5f, -76.25f }, energies);

    List<String> written = Files.readAllLines(dir
        .resolve("VertexDataFile energy.txt"));
    assertEquals(2, written.size());
    assertTrue(written.contains(" 1.0 0.0 0.0 -76.5"), written.toString());
  }

  @Test
  void vertexDataFileHasOneRowPerVertex() throws IOException {
    DataVertex[] data = { new DataVertex(new Point3f(1, 2, 3), 4.5f),
        new DataVertex(new Point3f(-1, 0, 0.5f), -2f) };

    isosurfacePES.writeVertexDataFile(dir.toString(), data, "flex");

    assertEquals(Arrays.asList(" 1.0 2.0 3.0 4.5", " -1.0 0.0 0.5 -2.0"),
        Files.readAllLines(dir.resolve("VertexDataFile flex.txt")));
  }

  @Test
  void generateThenLoadRoundTripsThroughTheViewer() throws IOException {
    // not water: Jmol leaves solvent molecules out of a solvent surface
    Path xyz = Files.write(dir.resolve("ethylene.xyz"), Arrays.asList("6",
        "ethylene", "C 0.6695 0 0", "C -0.6695 0 0", "H 1.2321 0.9289 0",
        "H 1.2321 -0.9289 0", "H -1.2321 0.9289 0", "H -1.2321 -0.9289 0"));
    Viewer viewer = (Viewer) JmolViewer.allocateViewer(null,
        new SmarterJmolAdapter());
    viewer.runScriptImmediately("load \""
        + xyz.toString().replace('\\', '/') + "\"");
    Path jobs = Files.createDirectory(dir.resolve("jobs"));

    isosurfacePES.generateIsosurfacePESfiles(viewer, 0, 2, 1.5f, 1.2f, "H",
        jobs.toString(), "my ethylene", "# UB3LYP/6-31G", "%mem=1GB");

    List<String> input = Files.readAllLines(jobs.resolve("RA  0.gjf"));
    assertEquals("%mem=1GB", input.get(0));
    assertEquals("# UB3LYP/6-31G", input.get(2));
    String title = input.get(4);
    assertTrue(title.startsWith("RA H 0 "), title);
    assertEquals("0 2", input.get(6));
    assertTrue(input.get(7).startsWith("C "), input.get(7));
    // the probe is appended after the molecule, at the position in the title
    assertEquals(title.substring(3), input.get(13));
    assertTrue(Files.exists(jobs.resolve("myethylene.mol")));

    long inputs;
    try (java.util.stream.Stream<Path> files = Files.list(jobs)) {
      inputs = files.filter(p -> p.toString().endsWith(".gjf")).count();
    }
    assertTrue(inputs > 10, "expected many sample points, got " + inputs);
    assertEquals(inputs + 4, Files.readAllLines(
        jobs.resolve("RA Batch File.bcf")).size());

    // probes on the +x side are low in energy, those on the -x side high
    for (int i = 0; i < inputs; i++) {
      String probe = Files.readAllLines(jobs.resolve("RA  " + i + ".gjf"))
          .get(4).substring("RA H 0 ".length());
      float x = Float.parseFloat(probe.split(" ")[0]);
      GaussianFixtures.energyOut(jobs, "RA  " + i + ".out", probe,
          x > 0 ? "-2.0" : "-1.5");
    }

    isosurfacePES.loadIsosurfacePES(viewer, jobs.toString(), "5",
        ColorIndexUtil.ROYGB);

    Mesh mesh = findMesh(viewer);
    assertNotNull(mesh);
    assertEquals(mesh.vertexCount, mesh.vertexColixes.length);
    short lowSide = 0, highSide = 0;
    float maxX = Float.NEGATIVE_INFINITY, minX = Float.POSITIVE_INFINITY;
    for (int i = 0; i < mesh.vertexCount; i++) {
      if (mesh.vertices[i].x > maxX) {
        maxX = mesh.vertices[i].x;
        lowSide = mesh.vertexColixes[i];
      }
      if (mesh.vertices[i].x < minX) {
        minX = mesh.vertices[i].x;
        highSide = mesh.vertexColixes[i];
      }
    }
    assertNotEquals(lowSide, highSide);
  }

  private static Mesh findMesh(Viewer viewer) {
    Mesh mesh = null;
    for (Shape shape : viewer.getShapeManager().getShapes())
      if (shape instanceof MeshCollection)
        for (Mesh m : ((MeshCollection) shape).meshes)
          if (m != null)
            mesh = m;
    return mesh;
  }
}
