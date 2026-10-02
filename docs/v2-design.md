# RADMAP v2 design

Status: draft for review, 2026-10-01

## Summary

RADMAP v1 is a 2012 fork of Jmol that maps the energy of a probe atom around a
molecule. It works, but it needs a Gaussian licence, the user has to run several
hundred jobs by hand, and the numbers it produces are not accurate enough to quote.

v2 has three aims:

1. **No Gaussian requirement.** Psi4, which is open source, becomes a first-class
   engine. Gaussian stays as an option for people who have it.
2. **Fix the bugs found in v1.** There are 19, listed below. Most are small; three
   change the numbers RADMAP reports and need a decision from Andrew first.
3. **Results that can be trusted.** Sensible default level of theory, energies kept
   in double precision, and surfaces reported as interaction energies in kcal/mol.

A first step is already in the working tree: a Psi4 mode in the isosurfacePES dialog
(see [What exists today](#what-exists-today)). The rest of this document is the plan
for getting from there to v2.

## Goals and non-goals

Goals:

- A user with only free software can go from a structure file to a coloured surface.
- RADMAP runs the jobs itself, shows progress, and can resume after a stop.
- Every bug in the [bug list](#bugs-to-fix) is fixed or explicitly declined.
- A surface made in v2 states what it shows: method, basis, units and energy range.

Non-goals:

- Rewriting or upgrading Jmol itself beyond what RADMAP needs.
- Periodic systems, solvent models, or excited states.
- A new GUI toolkit. The dialogs stay in Swing.
- Reproducing v1 numbers exactly. Where v1 was wrong, v2 will differ.

## What exists today

| Piece | State |
| --- | --- |
| Maven build on JDK 21, runnable jar, launcher script | Done |
| Unit tests over the RADMAP code | Done |
| Gaussian mode: write `.gjf` files and a batch file, parse `.out` | Works as in v1 |
| Psi4 mode (the default): write `.in` files and `run-psi4.sh`, parse tagged output lines | Done; checked against Psi4 1.11 on a few ethylene jobs |
| FlexSurface from Psi4 gradients | Same |
| Running jobs from inside RADMAP | First version done: "Run with Psi4" button with progress bar, stop and resume. No per-job failure view yet |
| Bug fixes | Not started; the tests avoid asserting the buggy behaviour |

The Psi4 inputs end with a few lines of Python that print `RADMAP_PROBE`,
`RADMAP_ENERGY` and `RADMAP_FORCE` lines into the output. Loading reads only those
lines, so it does not depend on Psi4's output layout.

## Design

### 1. Engines

v1 has Gaussian's file format written directly into the surface code. The Psi4 mode
added a second path beside it. v2 turns the two into implementations of one small
interface:

```java
interface Engine {
  String name();
  /** Write the input for one job into the job folder. */
  void writeInput(Job job, Path folder) throws IOException;
  /** The command that runs one job, or null if the engine is run by hand. */
  List<String> command(Job job, int threads);
  /** Read a finished job; empty if it has not finished or failed. */
  Optional<JobResult> readResult(Job job, Path folder) throws IOException;
}
```

`Job` holds the vertex index, probe position, molecule, charge, multiplicity, method,
basis, options and whether forces are wanted. `JobResult` holds the energy and,
optionally, atom positions and forces. Both use doubles.

| Engine | v2 role | Notes |
| --- | --- | --- |
| Psi4 | Default | Free; density-fitted DFT is fast at this system size |
| Gaussian | Optional | Kept for existing users and for reading v1 result folders |
| xtb | Optional, later | Semi-empirical; only for a cheap first pass over a surface |

Adding an engine is then one class and its tests. The surface, colouring and dialog
code never see a file format.

### 2. Job manifest

v1 recovers each probe position by parsing it back out of the Gaussian title line,
and identifies jobs by file names with two spaces in them. v2 writes a
`radmap-jobs.json` manifest into the job folder at generation time:

- the molecule, charge, multiplicity, method, basis and engine;
- surface settings (resolution, probe radius, fragment);
- one entry per job: index, probe position, input file, output file.

Loading reads the manifest and asks the engine for each job's result. This removes
the title-line round trip, lets RADMAP report which jobs are missing or failed, and
records what level of theory a surface was computed at. Folders with no manifest are
treated as v1 folders and read the old way.

### 3. Running jobs

v2 adds a **Run** button between Generate and Load.

- Jobs run as child processes from a worker pool. The user sets how many run at once
  and how many threads each gets; the default is one thread per job and as many jobs
  as there are cores, because many small jobs side by side beat one threaded job.
- Work happens off the Swing event thread. The dialog shows done, running, failed
  and remaining counts, and has a Stop button.
- A job with a finished output is skipped, so Run is also Resume.
- Failed jobs are listed with the last lines of their output and can be retried
  after changing options.
- `run-psi4.sh` is still written, for running the same folder on a cluster.

Engines whose `command` is null (Gaussian on a machine without it, for instance)
keep the v1 flow: generate, run elsewhere, load.

### 4. Accuracy

The v1 default, UB3LYP/6-31G, is far from chemical accuracy. v2 changes the defaults
and what is reported, and leaves the choice of method open.

| Item | v1 | v2 |
| --- | --- | --- |
| Default method | UB3LYP | A dispersion-corrected hybrid; proposal: ωB97X-D |
| Default basis | 6-31G | def2-TZVP |
| Energy storage | 32-bit float | 64-bit double |
| Surface value | Absolute energy, hartree | Interaction energy, kcal/mol |
| Colour range | Min to max of loaded data | Same by default; user can fix the range |
| Legend | None | Colour bar with units, method and basis |

Interaction energy needs two extra jobs per surface, the molecule alone and the probe
alone, at the same level of theory. They go in the manifest like any other job.

Two optional features follow from this:

- **Two-stage surfaces.** Run the whole surface at a cheap level, then re-run only
  the points below an energy threshold at the expensive level. This is where an xtb
  engine would pay off.
- **Counterpoise correction** for small basis sets, as a checkbox. With def2-TZVP or
  larger it is usually not needed.

What v2 will not claim: each point is the energy of a probe at a fixed position on a
rigid molecule. It is a reactivity map, not a set of reaction energies or barriers.

### 5. Surface handling

- The sampling surface gets a fixed ID (`radmap_sample`) and the result surface
  another (`radmap_result`). v1 takes "the last mesh of any kind in the viewer" and
  starts by deleting every isosurface the user has.
- The sampling surface is generated with Jmol's `ignore` option set so that water is
  not dropped. In v1 a lone water molecule gives an empty surface.
- Interpolation from sample points to the display mesh keeps the Gaussian weighting
  but exposes its width (0.2 Å in v1, hard-coded) and falls back to the nearest
  sample when all weights underflow, instead of producing NaN.

### 6. Relationship to Jmol

RADMAP v1 is a copy of all of Jmol 12.3.26 with about ten files added or changed.
That copy is why the build needed work to run on a current JDK, and it is 14 years
behind upstream.

Proposal: keep the fork for v2.0 and move RADMAP's own code into its own package
(`org.radmap`), touching Jmol only at the menu hook. Moving to stock Jmol as a
library dependency is worth doing but is a separate project: RADMAP reaches into
Jmol's mesh internals, which have changed since 12.3, and the replacement would need
a spike to confirm that current Jmol's scripting interface can hand back mesh
vertices and accept per-vertex colours.

## Bugs to fix

Severity: **A** changes reported numbers, **B** causes a failure or wrong display,
**C** is cosmetic or robustness.

### Need a decision before fixing

These three change FlexSurface results, and the intended behaviour is not clear from
the code.

| # | Sev | Where | Problem | Proposed fix |
| --- | --- | --- | --- | --- |
| 1 | A | `DEDXSurfaceUtils.Point3fVecMath.project` | Divides by the vector length once too often, so the projection is only right for atom pairs exactly one unit apart | Standard projection |
| 2 | A | `DEDXSurfaceUtils.flexWeight` | Bond weighting is `exp(-(d² - 1)/2c²)`, not a Gaussian centred on a bond length; and the positions it is applied to are in bohr while the constants look like ångströms | `exp(-(d - d₀)²/2c²)` with stated units |
| 3 | A | `DEDXSurfaceUtils.totalDEDX` | Sum leaves out the last atom, which is the probe. May be intended | Keep, but name it and document it; or include the probe |

### Straightforward

| # | Sev | Where | Problem | Fix |
| --- | --- | --- | --- | --- |
| 4 | A | `DataVertex`, all parsers | Energies held as floats; rounding reaches about 0.5 kcal/mol for large systems | Doubles throughout |
| 5 | B | `ColorIndexUtil` | WB scheme passes its range reversed, so every value is the same grey | Pass `-hi, -lo` |
| 6 | B | FlexSurface dialog | The A and C fields are read and passed down, then ignored | Use them |
| 7 | B | `loadIsosurfacePES` and both flex loaders | Empty or unreadable folder throws; so does a missing mesh | Check, and tell the user |
| 8 | B | `generateIsosurfacePESfiles` | Missing save folder: nothing written, no message | Create the folder or report |
| 9 | B | Sampling surface | Water-only molecule gives no surface | See [Surface handling](#5-surface-handling) |
| 10 | B | Interpolation | A display vertex far from every sample gets 0/0 | Nearest-sample fallback |
| 11 | B | `loadTotalDEDXGaussianOutFiles` | Writes its data to `VertexDataFile flex.txt`, overwriting the flex data | Own file name |
| 12 | B | `IsosurfacePESDialog.DFT_LIST` | `NCTH147` typo, so density fitting is disabled for HCTH147 | Fix spelling |
| 13 | B | Gaussian parsers | Each search loop reads one line past its match; values are picked by position after splitting on single spaces | Replaced by the engine reader, using regular expressions |
| 14 | C | `Point3fVecMath.cross` | Wrong sign on the y component. Unused | Fix or delete |
| 15 | C | Both dialogs | Buttons use mouse listeners, so keyboard activation does nothing; `paint` re-packs the window on every repaint | Action listeners; pack once |
| 16 | C | Both dialogs | Long work runs on the event thread and freezes the window | Background worker, as for Run |
| 17 | C | `IsosurfacePESDialog` | Multiplicity can be set to 0; method and basis lists have duplicate entries | Minimum 1; dedupe |
| 18 | C | `RandomFunctions`, `isosurfacePES` | Windows path separators built by hand | `java.nio.file.Path` |
| 19 | C | Throughout | Debug output always on; `DEBUG` is a constant `true` | Jmol's `Logger`, off by default |

Each fix lands with a test that fails before it and passes after.

## Plan

| Phase | Contents | Outcome |
| --- | --- | --- |
| 0 | Validate the current Psi4 mode against real Psi4 on ethylene (done for a few jobs; a full surface is still to do) | Gaussian-free workflow works |
| 1 | Bugs 4 to 19 | v1 behaviour, minus the defects |
| 2 | `Engine` interface, manifest, doubles end to end; move code to `org.radmap` | Clean base for the rest |
| 3 | Run button with worker pool, progress, resume, failure list | No terminal needed |
| 4 | Interaction energies, kcal/mol, legend, new defaults | Surfaces that can be quoted |
| 5 | Bugs 1 to 3 once decided; optional two-stage surfaces and xtb | FlexSurface on a sound footing |

Phases 0 and 1 are independent and small. Phase 2 is the only one that reshapes
existing code.

## Testing

- Unit tests per engine: input text, and parsing of real output files checked into
  `test/resources` (one finished, one failed, one truncated for each engine).
- The existing headless generate-and-load round trip, extended to the manifest.
- An opt-in integration test that runs three real Psi4 jobs when `psi4` is on the
  `PATH`, and is skipped otherwise.
- One reference surface (ethylene with an H probe) with stored energies, to catch
  accidental changes in sampling or interpolation.

## Risks

| Risk | Mitigation |
| --- | --- |
| The Psi4 input or output handling is wrong in a way the stand-in tests cannot see | Phase 0 comes first |
| SCF convergence failures for a radical probe close to the molecule | Failure list and retry in the runner; `soscf` and damping options documented |
| Cost: def2-TZVP is perhaps 10 to 50 times 6-31G per job | Two-stage surfaces; the resolution setting; job count shown before generating |
| Psi4 is awkward to install on Windows | Document the conda route; Gaussian engine remains |
| v2 FlexSurface values differ from published v1 figures | Decide bugs 1 to 3 explicitly; keep a "v1 compatible" switch if needed |

## Open questions

1. Bugs 1 to 3: what was the flex weighting meant to be, and should the probe count
   towards total dE/dX?
2. Should FlexSurface use forces at the generated geometry (Psi4 mode today) or after
   a couple of optimisation steps (Gaussian mode today)? They answer different
   questions.
3. Is ωB97X-D/def2-TZVP an acceptable default, or should the default stay cheap with
   the accurate level one click away?
4. Does Gaussian support need to stay for writing new jobs, or only for reading old
   folders?
5. Is v2 still a Jmol fork, or is the move to stock Jmol in scope?
6. The citation in both dialogs still says "2012 (TO BE UPDATED)". What should it be?
