package org.jmol.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Builds minimal Gaussian output files in the shape RADMAP parses. */
final class GaussianFixtures {

  private GaussianFixtures() {
  }

  /** An output holding the RADMAP title line and, optionally, an SCF energy. */
  static Path energyOut(Path dir, String name, String probe, String energy)
      throws IOException {
    List<String> lines = new ArrayList<String>();
    lines.add(" Entering Gaussian System");
    lines.add(" RA H 0 " + probe);
    lines.add(" -------------------");
    lines.add(" Charge =  0 Multiplicity = 2");
    if (energy != null) {
      // an earlier cycle, to check that the last SCF energy wins
      lines.add(" SCF Done:  E(UB3LYP) =  -1.00000000     A.U. after   10 cycles");
      lines.add(" SCF Done:  E(UB3LYP) =  " + energy + "     A.U. after   12 cycles");
    }
    return Files.write(dir.resolve(name), lines);
  }

  /**
   * An optimisation output with a -DE/DX table.
   * 
   * @param atoms one row per atom: x, y, z, then -dE/dx, -dE/dy, -dE/dz
   */
  static Path forcesOut(Path dir, String name, String probe, float[][] atoms)
      throws IOException {
    List<String> lines = new ArrayList<String>();
    lines.add(" Entering Gaussian System");
    lines.add(" RA H 0 " + probe);
    lines.add(" -------------------");
    lines.add(" Charge =  0 Multiplicity = 2 ");
    for (int i = 0; i < atoms.length; i++)
      lines.add(" H 0 " + atoms[i][0] + " " + atoms[i][1] + " " + atoms[i][2]);
    lines.add(" ");
    lines.add(" GradGradGradGradGradGradGradGradGradGradGradGradGradGradGrad");
    lines.add(" Variable       Old X    -DE/DX   Delta X   Delta X   Delta X     New X");
    lines.add("                                 (Linear)    (Quad)   (Total)");
    for (int i = 0; i < atoms.length; i++)
      for (int axis = 0; axis < 3; axis++)
        lines.add("    " + "XYZ".charAt(axis) + (i + 1) + "        " + atoms[i][axis]
            + "   " + atoms[i][axis + 3] + "   0.00000   0.00000   0.00000   "
            + atoms[i][axis]);
    return Files.write(dir.resolve(name), lines);
  }
}
