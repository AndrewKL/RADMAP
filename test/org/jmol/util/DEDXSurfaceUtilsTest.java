package org.jmol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.vecmath.Point3f;

import org.jmol.util.DEDXSurfaceUtils.Point3fVecMath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DEDXSurfaceUtilsTest {

  @TempDir
  Path dir;

  @Test
  void vectorHelpers() {
    Point3f a = new Point3f(1, 2, 3);
    Point3f b = new Point3f(4, -5, 6);

    assertEquals(12f, Point3fVecMath.dot(a, b));
    assertEquals(new Point3f(5, -3, 9), Point3fVecMath.add(a, b));
    assertEquals(new Point3f(-3, 7, -3), Point3fVecMath.sub(a, b));
    assertEquals(new Point3f(2, 4, 6), Point3fVecMath.mult(a, 2));
    assertEquals(new Point3f(0.5f, 1, 1.5f), Point3fVecMath.div(a, 2));
    assertEquals(5f, Point3fVecMath.length(new Point3f(3, 0, -4)));
    assertEquals(5f, Point3fVecMath.abslength(new Point3f(3, 0, -4)));
    assertEquals(new Point3f(0.6f, 0, -0.8f),
        Point3fVecMath.normal(new Point3f(3, 0, -4)));
  }

  @Test
  void projectOntoUnitVectorKeepsTheParallelComponent() {
    assertEquals(new Point3f(0, 2, 0), Point3fVecMath.project(new Point3f(1,
        2, 3), new Point3f(0, 1, 0)));
  }

  @Test
  void totalDedxSumsForceMagnitudesExceptOnTheProbe() throws IOException {
    float[][] atoms = { { 0, 0, 0, 3, 0, 4 }, { 1, 0, 0, 0, 2, 0 },
        { 5, 5, 5, 9, 9, 9 } }; // last row is the probe
    Path out = GaussianFixtures.forcesOut(dir, "RA  0.out", "5.0 5.0 5.0",
        atoms);

    DataVertex vertex = DEDXSurfaceUtils
        .readTotalDEDXDataFromGaussianOutFile(out.toString());

    assertEquals(new Point3f(5, 5, 5), vertex.xyz);
    assertEquals(7f, vertex.energy, 1e-5f);
  }

  @Test
  void flexWeightIsTheForceDifferencePerpendicularToTheBond()
      throws IOException {
    // two atoms one unit apart along x; one is pushed 0.3 sideways and 0.2 along
    float[][] atoms = { { 1, 0, 0, 0.2f, 0.3f, 0 }, { 0, 0, 0, 0, 0, 0 } };
    Path out = GaussianFixtures.forcesOut(dir, "RA  0.out", "0.0 2.0 0.0",
        atoms);

    DataVertex vertex = DEDXSurfaceUtils.readFlexDataFromGaussianOutFile(
        out.toString(), 2f, 0.5f);

    assertEquals(new Point3f(0, 2, 0), vertex.xyz);
    assertEquals(0.3f, vertex.energy, 1e-5f);
  }

  @Test
  void flexWeightIsZeroWhenForcesActAlongTheBond() throws IOException {
    float[][] atoms = { { 1, 0, 0, 0.4f, 0, 0 }, { 0, 0, 0, -0.1f, 0, 0 } };
    Path out = GaussianFixtures.forcesOut(dir, "RA  0.out", "0.0 2.0 0.0",
        atoms);

    assertEquals(0f, DEDXSurfaceUtils.readFlexDataFromGaussianOutFile(
        out.toString(), 2f, 0.5f).energy, 1e-6f);
  }

  @Test
  void outputsWithoutAForceTableAreSkipped() throws IOException {
    Path out = GaussianFixtures.energyOut(dir, "RA  0.out", "1 2 3", "-76.5");

    assertNull(DEDXSurfaceUtils.readTotalDEDXDataFromGaussianOutFile(out
        .toString()));
    assertNull(DEDXSurfaceUtils.readFlexDataFromGaussianOutFile(
        out.toString(), 2f, 0.5f));
  }

  @Test
  void folderLoadSkipsUnusableFilesAndWritesDataFile() throws IOException {
    float[][] atoms = { { 0, 0, 0, 0, 0, 1 }, { 1, 0, 0, 0, 0, 0 } };
    GaussianFixtures.forcesOut(dir, "RA  0.out", "1.0 0.0 0.0", atoms);
    GaussianFixtures.energyOut(dir, "RA  1.out", "0 1 0", "-76.5");
    GaussianFixtures.forcesOut(dir, "other.out", "9.0 9.0 9.0", atoms);

    DataVertex[] data = DEDXSurfaceUtils.loadTotalDEDXGaussianOutFiles(dir
        .toString());

    assertEquals(1, data.length);
    assertEquals(1f, data[0].energy, 1e-6f);
    List<String> written = Files.readAllLines(dir
        .resolve("VertexDataFile flex.txt"));
    assertEquals(1, written.size());
    assertEquals(" 1.0 0.0 0.0 1.0", written.get(0));
  }
}
