package org.jmol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point3f;

import org.jmol.shape.Mesh;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Runs against a shell script standing in for the psi4 executable. */
class Psi4RunnerTest {

  @TempDir
  Path dir;

  Path jobs;
  String fakePsi4;

  @BeforeEach
  void setUp() throws IOException {
    assumeTrue(new File("/bin/sh").exists(), "needs a POSIX shell");
    jobs = Files.createDirectory(dir.resolve("jobs"));
    // fails any job whose probe is at x = 13, sleeps if SLOW exists
    Path script = Files.write(dir.resolve("fakepsi4"), Arrays.asList(
        "#!/bin/sh",
        "while [ $# -gt 0 ]; do case \"$1\" in",
        "  --version) echo 1.9.1; exit 0;;",
        "  -i) in=\"$2\"; shift;; -o) out=\"$2\"; shift;; -n) echo \"$2\" >> threads.log; shift;;",
        "esac; shift; done",
        "[ -f SLOW ] && sleep 30",
        "if grep -q '^# RADMAP_PROBE H 13' \"$in\"; then echo crashed > \"$out\"; exit 1; fi",
        "{ cat \"$in\"; sed -n 's/^# RADMAP_PROBE/RADMAP_PROBE/p' \"$in\"; echo 'RADMAP_ENERGY -1.5'; } > \"$out\""));
    script.toFile().setExecutable(true);
    fakePsi4 = script.toString();
  }

  private void generate(float... probeX) {
    Mesh mesh = new Mesh("test", null, (short) 0, 0);
    mesh.vertices = new Point3f[probeX.length];
    for (int i = 0; i < probeX.length; i++)
      mesh.vertices[i] = new Point3f(probeX[i], 0, 0);
    assertEquals(probeX.length, Psi4Engine.generateFiles(mesh,
        jobs.toString(), "1GB", 1, "b3lyp", "6-31G", "", 0, 2,
        "C 0.0 0.0 0.0\n", "H", false));
  }

  private Psi4Runner runner(int threads, int parallel) {
    return new Psi4Runner(fakePsi4, jobs.toFile(), threads, parallel);
  }

  @Test
  void checkCommandReportsTheVersionOrNull() {
    assertEquals("1.9.1", Psi4Runner.checkCommand(fakePsi4));
    assertNull(Psi4Runner.checkCommand(dir.resolve("no-such-program")
        .toString()));
  }

  @Test
  void commandMayCarryLeadingArguments() {
    assertEquals(Arrays.asList("conda", "run", "-n", "psi4", "psi4"),
        Psi4Runner.splitCommand(" conda run -n psi4  psi4 "));
    assertEquals(Arrays.asList("psi4"), Psi4Runner.splitCommand(""));
    assertEquals(Arrays.asList(fakePsi4), Psi4Runner.splitCommand(fakePsi4));
  }

  @Test
  void runsEveryJobAndReportsProgress() {
    generate(1, 2, 3, 4, 5);
    final List<String> events = Collections
        .synchronizedList(new ArrayList<String>());

    int finished = runner(2, 3).run(new Psi4Runner.Listener() {
      public void progress(int done, int failed, int total, String job) {
        events.add(done + "/" + failed + "/" + total);
      }
    });

    assertEquals(5, finished);
    assertEquals("0/0/5", events.get(0));
    assertEquals(6, events.size());
    assertTrue(events.contains("5/0/5"));
    for (int i = 0; i < 5; i++)
      assertNotNull(Psi4Engine.readEnergy(jobs.resolve("RA_" + i + ".out")
          .toString()));
    assertEquals(5, isosurfacePES.loadGaussianOutFiles(jobs.toString()).length);
  }

  @Test
  void passesTheThreadCountToPsi4() throws IOException {
    generate(1);
    runner(4, 1).run(null);
    assertEquals(Arrays.asList("4"), Files.readAllLines(jobs
        .resolve("threads.log")));
  }

  @Test
  void failedJobsAreCountedAndNamed() {
    generate(1, 13, 3);
    Psi4Runner runner = runner(1, 2);

    assertEquals(2, runner.run(null));
    assertEquals(Arrays.asList("RA_1.in"), runner.getFailedJobs());
  }

  @Test
  void finishedJobsAreNotRunAgain() throws IOException {
    generate(1, 2, 3);
    runner(1, 1).run(null);
    Files.delete(jobs.resolve("threads.log"));
    Files.delete(jobs.resolve("RA_2.out"));

    assertEquals(3, runner(1, 1).run(null));
    assertEquals(1, Files.readAllLines(jobs.resolve("threads.log")).size());
  }

  @Test
  void regeneratingKeepsUnchangedJobsAndRerunsChangedOnes() throws IOException {
    generate(1, 2, 3);
    runner(1, 1).run(null);
    // make the old files clearly older than anything written from here on
    FileTime past = FileTime.fromMillis(System.currentTimeMillis() - 60000);
    for (File f : jobs.toFile().listFiles())
      Files.setLastModifiedTime(f.toPath(), past);

    generate(1, 2, 7);

    assertTrue(Psi4Runner.isFinished(jobs.resolve("RA_0.in").toFile()));
    assertTrue(Psi4Runner.isFinished(jobs.resolve("RA_1.in").toFile()));
    assertFalse(Psi4Runner.isFinished(jobs.resolve("RA_2.in").toFile()));
  }

  @Test
  void filesFromALargerEarlierSurfaceAreStale() {
    generate(1, 2, 3, 4);
    runner(1, 2).run(null);

    generate(1, 2);

    List<String> stale = new ArrayList<String>();
    for (File f : Psi4Runner.staleFiles(jobs.toFile()))
      stale.add(f.getName());
    Collections.sort(stale);
    assertEquals(Arrays.asList("RA_2.in", "RA_2.out", "RA_3.in", "RA_3.out"),
        stale);
    assertEquals(2, Psi4Runner.jobInputs(jobs.toFile()).size());
  }

  @Test
  void cancelStopsRunningJobsAndSkipsTheRest() throws Exception {
    generate(1, 2, 3, 4, 5, 6);
    Files.createFile(jobs.resolve("SLOW"));
    final Psi4Runner runner = runner(1, 2);
    final int[] result = { -1 };
    Thread thread = new Thread(new Runnable() {
      public void run() {
        result[0] = runner.run(null);
      }
    });
    thread.start();
    Thread.sleep(1000);

    runner.cancel();
    thread.join(10000);

    assertFalse(thread.isAlive(), "run should return soon after cancel");
    assertTrue(runner.isCancelled());
    assertEquals(0, result[0]);
  }

  @Test
  void missingExecutableFailsEveryJobInsteadOfThrowing() {
    generate(1, 2);
    Psi4Runner runner = new Psi4Runner(dir.resolve("no-such-program")
        .toString(), jobs.toFile(), 1, 1);

    assertEquals(0, runner.run(null));
    assertEquals(2, runner.getFailedJobs().size());
  }
}
