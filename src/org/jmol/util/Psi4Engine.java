package org.jmol.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import org.jmol.shape.Mesh;

public class Psi4Engine {

  /*
   * Psi4 (https://psicode.org) as an open source alternative to Gaussian for
   * the isosurfacePES and FlexSurface tools.
   *
   * Each input file ends with a few lines of Python that write the values
   * RADMAP needs into the Psi4 output on lines starting with the tags below,
   * so loading does not depend on the layout of the rest of the output.
   */

  public static final String PROBE_TAG = "RADMAP_PROBE";
  public static final String ENERGY_TAG = "RADMAP_ENERGY";
  public static final String FORCE_TAG = "RADMAP_FORCE";

  public static final String RUN_SCRIPT_NAME = "run-psi4.sh";
  /** Lists the inputs of the current job set, one per line. */
  public static final String JOB_LIST_NAME = "radmap-jobs.txt";

  /** Atom positions (bohr) and forces (hartree/bohr) from one output file. */
  public static class Forces {
    public Point3f probe;
    public Point3f[] locations;
    public Point3f[] forces;
  }

  public static String jobName(int vertexIndex) {
    return "RA_" + vertexIndex;
  }

  /**
   * @param memory as shown in the dialog, e.g. "1GB", or "Default"
   * @return the Psi4 memory line, or "" to leave Psi4's default
   */
  public static String memoryLine(String memory) {
    if (memory == null || memory.length() == 0 || memory.equals("Default"))
      return "";
    int i = 0;
    while (i < memory.length() && Character.isDigit(memory.charAt(i)))
      i++;
    return "memory " + memory.substring(0, i) + " " + memory.substring(i).trim();
  }

  public static String generateInput(String memory, String method,
                                     String basis, String options, int charge,
                                     int multiplicity,
                                     String moleculeSpecification,
                                     String fragment, Point3f probe,
                                     boolean withForces) {
    String probeLine = fragment + " " + probe.x + " " + probe.y + " " + probe.z;
    StringBuffer sb = new StringBuffer();
    sb.append("# ").append(PROBE_TAG).append(' ').append(probeLine).append('\n');
    String memoryLine = memoryLine(memory);
    if (memoryLine.length() > 0)
      sb.append(memoryLine).append('\n');
    sb.append('\n');
    sb.append("molecule {\n");
    sb.append(charge).append(' ').append(multiplicity).append('\n');
    sb.append(moleculeSpecification);
    if (!moleculeSpecification.endsWith("\n"))
      sb.append('\n');
    sb.append(probeLine).append('\n');
    // keep the frame RADMAP generated the surface in
    sb.append("symmetry c1\nno_reorient\nno_com\n");
    sb.append("}\n\n");
    sb.append("set reference ").append(multiplicity == 1 ? "rhf" : "uhf").append('\n');
    if (basis != null && basis.trim().length() > 0)
      sb.append("set basis ").append(basis.trim()).append('\n');
    if (options != null) {
      String[] list = options.split(";");
      for (int i = 0; i < list.length; i++)
        if (list[i].trim().length() > 0)
          sb.append("set ").append(list[i].trim()).append('\n');
    }
    sb.append('\n');
    String call = "('" + method.trim().toLowerCase() + "')";
    if (withForces) {
      sb.append("radmap_gradient = gradient").append(call).append(".np\n");
      sb.append("radmap_molecule = psi4.core.get_active_molecule()\n");
      sb.append("radmap_geometry = radmap_molecule.geometry().np\n");
    } else {
      sb.append("energy").append(call).append('\n');
    }
    sb.append("psi4.core.print_out('").append(PROBE_TAG).append(' ').append(
        probeLine).append("\\n')\n");
    if (withForces) {
      sb.append("for radmap_i in range(radmap_molecule.natom()):\n");
      sb.append("    psi4.core.print_out('").append(FORCE_TAG).append(
          " %d %.8f %.8f %.8f %.8f %.8f %.8f\\n' % (radmap_i,\n");
      sb.append("        radmap_geometry[radmap_i][0], radmap_geometry[radmap_i][1], radmap_geometry[radmap_i][2],\n");
      sb.append("        -radmap_gradient[radmap_i][0], -radmap_gradient[radmap_i][1], -radmap_gradient[radmap_i][2]))\n");
    }
    sb.append("psi4.core.print_out('").append(ENERGY_TAG).append(
        " %.10f\\n' % psi4.variable('CURRENT ENERGY'))\n");
    return sb.toString();
  }

  /**
   * Writes one Psi4 input per mesh vertex, plus a script that runs them.
   *
   * @return the number of input files written, or -1 if the folder cannot be
   *         written to
   */
  public static int generateFiles(Mesh theMesh, String folderLocation,
                                  String memory, int threads, String method,
                                  String basis, String options, int charge,
                                  int multiplicity,
                                  String moleculeSpecification,
                                  String fragment, boolean withForces) {
    int count = 0;
    try {
      PrintWriter jobList = new PrintWriter(new File(folderLocation,
          JOB_LIST_NAME));
      for (int i = 0; i < theMesh.vertices.length; i++) {
        if (theMesh.vertices[i] == null)
          continue;
        File file = new File(folderLocation, jobName(i) + ".in");
        String text = generateInput(memory, method, basis, options, charge,
            multiplicity, moleculeSpecification, fragment,
            theMesh.vertices[i], withForces);
        // an unchanged input is left alone so that its finished output still
        // counts as up to date
        if (!text.equals(readFile(file))) {
          PrintWriter writer = new PrintWriter(file);
          writer.print(text);
          writer.close();
        }
        jobList.print(file.getName() + "\n");
        count++;
      }
      jobList.close();
      writeRunScript(folderLocation, threads);
    } catch (FileNotFoundException e) {
      System.out.println("error: cannot write Psi4 files to " + folderLocation);
      return -1;
    }
    return count;
  }

  private static String readFile(File file) {
    if (!file.isFile())
      return null;
    try {
      return new String(java.nio.file.Files.readAllBytes(file.toPath()));
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * @param folderLocation
   * @return the input file names of the job set last generated in the
   *         folder; empty if there is none
   */
  public static List<String> readJobList(String folderLocation) {
    List<String> names = new ArrayList<String>();
    String text = readFile(new File(folderLocation, JOB_LIST_NAME));
    if (text == null)
      return names;
    String[] lines = text.split("\n");
    for (int i = 0; i < lines.length; i++)
      if (lines[i].trim().length() > 0)
        names.add(lines[i].trim());
    return names;
  }

  /**
   * The script runs every input that has no finished output, so it can be
   * stopped and restarted. JOBS sets how many run side by side.
   */
  public static void writeRunScript(String folderLocation, int threads)
      throws FileNotFoundException {
    File script = new File(folderLocation, RUN_SCRIPT_NAME);
    PrintWriter writer = new PrintWriter(script);
    writer.print("#!/bin/sh\n");
    writer.print("# Runs the RADMAP Psi4 jobs in this folder, skipping finished ones.\n");
    writer.print("# Usage: ./" + RUN_SCRIPT_NAME + "    or    JOBS=4 ./"
        + RUN_SCRIPT_NAME + "\n");
    writer.print("cd \"$(dirname \"$0\")\" || exit 1\n");
    writer.print("THREADS=${THREADS:-" + Math.max(1, threads) + "}\n");
    writer.print("JOBS=${JOBS:-1}\n");
    writer.print("export THREADS\n");
    writer.print("while read -r f; do\n");
    writer.print("  { [ ! \"$f\" -nt \"${f%.in}.out\" ] && grep -q '^" + ENERGY_TAG
        + "' \"${f%.in}.out\" 2>/dev/null; } || echo \"$f\"\n");
    writer.print("done < " + JOB_LIST_NAME + " | xargs -P \"$JOBS\" -I {} sh -c '\n");
    writer.print("  f=\"{}\"\n");
    writer.print("  echo \"running $f\"\n");
    writer.print("  psi4 -n \"$THREADS\" -i \"$f\" -o \"${f%.in}.out\" || echo \"FAILED $f\" >&2\n");
    writer.print("'\n");
    writer.close();
    script.setExecutable(true);
  }

  private static List<String> readTaggedLines(String path) {
    List<String> lines = new ArrayList<String>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(path));
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("RADMAP_"))
          lines.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (br != null)
          br.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return lines;
  }

  private static Point3f point(String[] tokens, int first) {
    return new Point3f(Float.parseFloat(tokens[first]), Float
        .parseFloat(tokens[first + 1]), Float.parseFloat(tokens[first + 2]));
  }

  /**
   * @param path
   * @return true if the file is the output of a RADMAP Psi4 job, finished or
   *         not
   */
  public static boolean isPsi4Output(String path) {
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(path));
      String line;
      while ((line = br.readLine()) != null)
        if (line.indexOf(PROBE_TAG) >= 0)
          return true;
    } catch (IOException e) {
      // not readable: not ours
    } finally {
      try {
        if (br != null)
          br.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return false;
  }

  /**
   * @param path
   * @return the probe position and energy, or null if the job did not finish
   */
  public static DataVertex readEnergy(String path) {
    Point3f probe = null;
    String energy = null;
    List<String> lines = readTaggedLines(path);
    try {
      for (int i = 0; i < lines.size(); i++) {
        String[] tokens = lines.get(i).split("\\s+");
        if (tokens[0].equals(PROBE_TAG))
          probe = point(tokens, 2);
        else if (tokens[0].equals(ENERGY_TAG))
          energy = tokens[1];
      }
      if (probe == null || energy == null)
        return null;
      return new DataVertex(probe, Float.parseFloat(energy));
    } catch (RuntimeException e) {
      System.out.println("error: unreadable RADMAP lines in " + path);
      return null;
    }
  }

  /**
   * @param path
   * @return atom positions and forces, or null if the job did not finish or
   *         was generated without forces
   */
  public static Forces readForces(String path) {
    Forces result = new Forces();
    List<Point3f> locations = new ArrayList<Point3f>();
    List<Point3f> forces = new ArrayList<Point3f>();
    boolean finished = false;
    List<String> lines = readTaggedLines(path);
    try {
      for (int i = 0; i < lines.size(); i++) {
        String[] tokens = lines.get(i).split("\\s+");
        if (tokens[0].equals(PROBE_TAG)) {
          result.probe = point(tokens, 2);
        } else if (tokens[0].equals(FORCE_TAG)) {
          locations.add(point(tokens, 2));
          forces.add(point(tokens, 5));
        } else if (tokens[0].equals(ENERGY_TAG)) {
          finished = true;
        }
      }
    } catch (RuntimeException e) {
      System.out.println("error: unreadable RADMAP lines in " + path);
      return null;
    }
    if (!finished || result.probe == null || locations.size() == 0)
      return null;
    result.locations = locations.toArray(new Point3f[locations.size()]);
    result.forces = forces.toArray(new Point3f[forces.size()]);
    return result;
  }
}
