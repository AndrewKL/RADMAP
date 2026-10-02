package org.jmol.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Psi4Runner {

  /*
   * Runs the Psi4 jobs in a folder written by Psi4Engine.generateFiles, a
   * few at a time, skipping those that already finished.
   */

  public interface Listener {
    /**
     * Called from a worker thread each time a job ends.
     * 
     * @param finished jobs with a usable result, including earlier runs
     * @param failed jobs that ended without a result in this run
     * @param total all jobs in the folder
     * @param job the input file that just ended
     */
    void progress(int finished, int failed, int total, String job);
  }

  private final List<String> command;
  private final File folder;
  private final int threads;
  private final int parallelJobs;

  private volatile boolean cancelled;
  private final List<Process> running = Collections
      .synchronizedList(new ArrayList<Process>());
  private final List<String> failedJobs = Collections
      .synchronizedList(new ArrayList<String>());
  private int finished;

  /**
   * @param command the Psi4 executable, optionally with leading arguments,
   *        e.g. "psi4" or "conda run -n psi4 psi4"
   * @param folder
   * @param threads threads per job
   * @param parallelJobs jobs to run side by side
   */
  public Psi4Runner(String command, File folder, int threads, int parallelJobs) {
    this.command = splitCommand(command);
    this.folder = folder;
    this.threads = Math.max(1, threads);
    this.parallelJobs = Math.max(1, parallelJobs);
  }

  /**
   * @return "psi4" if that runs as it is, otherwise the psi4 of a conda
   *         environment named psi4 in one of the usual places, otherwise
   *         "psi4"
   */
  public static String findCommand() {
    if (checkCommand("psi4") != null)
      return "psi4";
    String home = System.getProperty("user.home");
    String[] roots = { "/opt/homebrew/Caskroom/miniforge/base",
        "/usr/local/Caskroom/miniforge/base", home + "/miniforge3",
        home + "/mambaforge", home + "/miniconda3", home + "/anaconda3",
        "/opt/miniconda3", "/opt/anaconda3" };
    for (int i = 0; i < roots.length; i++) {
      File candidate = new File(roots[i], "envs/psi4/bin/psi4");
      if (candidate.isFile())
        return candidate.getPath();
    }
    return "psi4";
  }

  static List<String> splitCommand(String command) {
    command = (command == null ? "" : command.trim());
    // a path to the executable may contain spaces
    if (command.length() == 0 || new File(command).isFile())
      return new ArrayList<String>(Arrays.asList(command.length() == 0 ? "psi4"
          : command));
    return new ArrayList<String>(Arrays.asList(command.split("\\s+")));
  }

  /**
   * @param command
   * @return what "command --version" prints, or null if it cannot be run
   */
  public static String checkCommand(String command) {
    try {
      List<String> args = splitCommand(command);
      args.add("--version");
      ProcessBuilder builder = new ProcessBuilder(args);
      builder.redirectErrorStream(true);
      Process process = builder.start();
      process.getOutputStream().close();
      String output = new String(process.getInputStream().readAllBytes()).trim();
      if (!process.waitFor(60, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        return null;
      }
      return (process.exitValue() == 0 ? output : null);
    } catch (IOException e) {
      return null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
  }

  public static File outputFor(File input) {
    String name = input.getName();
    return new File(input.getParentFile(), name.substring(0,
        name.length() - 3) + ".out");
  }

  /**
   * @param input
   * @return true if the job has a result that is newer than its input, so a
   *         regenerated input is run again
   */
  public static boolean isFinished(File input) {
    File output = outputFor(input);
    return output.isFile() && output.lastModified() >= input.lastModified()
        && Psi4Engine.readEnergy(output.getPath()) != null;
  }

  /**
   * @param folder
   * @return the inputs of the current job set, from the list written at
   *         generation time
   */
  public static List<File> jobInputs(File folder) {
    List<File> inputs = new ArrayList<File>();
    List<String> names = Psi4Engine.readJobList(folder.getPath());
    for (int i = 0; i < names.size(); i++)
      inputs.add(new File(folder, names.get(i)));
    return inputs;
  }

  /**
   * @param folder
   * @return RA_ input and output files that are not part of the current job
   *         set, left over from an earlier surface
   */
  public static List<File> staleFiles(File folder) {
    List<String> names = Psi4Engine.readJobList(folder.getPath());
    List<File> stale = new ArrayList<File>();
    File[] files = folder.listFiles();
    if (files == null)
      return stale;
    for (int i = 0; i < files.length; i++) {
      String name = files[i].getName();
      if (!name.matches("RA_\\d+\\.(in|out)"))
        continue;
      String input = name.substring(0, name.lastIndexOf('.')) + ".in";
      if (!names.contains(input))
        stale.add(files[i]);
    }
    return stale;
  }

  public void cancel() {
    cancelled = true;
    synchronized (running) {
      for (int i = 0; i < running.size(); i++)
        running.get(i).destroy();
    }
  }

  public boolean isCancelled() {
    return cancelled;
  }

  /** @return names of the inputs that ended without a result */
  public List<String> getFailedJobs() {
    synchronized (failedJobs) {
      return new ArrayList<String>(failedJobs);
    }
  }

  /**
   * Runs every unfinished job and returns when all have ended or the run was
   * cancelled.
   * 
   * @param listener may be null
   * @return the number of jobs with a usable result
   */
  public int run(final Listener listener) {
    final List<File> inputs = jobInputs(folder);
    final int total = inputs.size();
    List<File> pending = new ArrayList<File>();
    synchronized (this) {
      finished = 0;
      for (int i = 0; i < total; i++) {
        if (isFinished(inputs.get(i)))
          finished++;
        else
          pending.add(inputs.get(i));
      }
    }
    if (listener != null)
      listener.progress(finished, 0, total, null);

    ExecutorService pool = Executors.newFixedThreadPool(parallelJobs);
    for (int i = 0; i < pending.size(); i++) {
      final File input = pending.get(i);
      pool.execute(new Runnable() {
        public void run() {
          if (cancelled)
            return;
          boolean ok = runJob(input);
          if (cancelled)
            return;
          // reported under the lock so that counts never arrive out of order
          synchronized (Psi4Runner.this) {
            if (ok)
              finished++;
            else
              failedJobs.add(input.getName());
            if (listener != null)
              listener.progress(finished, failedJobs.size(), total, input
                  .getName());
          }
        }
      });
    }
    pool.shutdown();
    try {
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      cancel();
      Thread.currentThread().interrupt();
    }
    synchronized (this) {
      return finished;
    }
  }

  boolean runJob(File input) {
    List<String> args = new ArrayList<String>(command);
    args.add("-n");
    args.add(String.valueOf(threads));
    args.add("-i");
    args.add(input.getName());
    args.add("-o");
    args.add(outputFor(input).getName());
    ProcessBuilder builder = new ProcessBuilder(args);
    builder.directory(folder);
    builder.redirectErrorStream(true);
    builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
    Process process = null;
    try {
      process = builder.start();
      running.add(process);
      if (cancelled)
        process.destroy();
      process.getOutputStream().close();
      return process.waitFor() == 0 && isFinished(input);
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    } finally {
      if (process != null) {
        running.remove(process);
        process.destroy();
      }
    }
  }
}
