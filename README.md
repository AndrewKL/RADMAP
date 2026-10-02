# RADMAP

RADMAP maps how reactive each part of a molecule is towards a small probe fragment
(by default a hydrogen radical) and paints the result onto the molecule's surface.

It is a fork of the [Jmol](https://jmol.sourceforge.net/) molecular viewer
(version 12.3.26) with two extra tools added to the desktop application. RADMAP does
not run any quantum chemistry itself: it writes input files for
[Gaussian](https://gaussian.com/) or the open source [Psi4](https://psicode.org/),
you run them, and RADMAP reads the outputs back in.

> Andrew K. Long, Jason A. C. Clyburne. "RADMAP: An isosurface potential energy
> surface program", 2012.

## How it works

1. **Build a surface.** RADMAP asks Jmol for a solvent-accessible surface around the
   loaded molecule (`isosurface resolution <r> solvent <probe radius>`). Each vertex
   of that mesh is a position where the probe fragment will be placed.
2. **Generate one calculation per vertex.** For every vertex it writes a Gaussian or
   Psi4 input file containing the molecule plus the fragment at that vertex, along
   with a batch file or script that runs all the jobs.
3. **Run the jobs.** This happens outside RADMAP, on whatever machine you like.
4. **Load the results.** RADMAP reads the final energy from each output file, interpolates those energies onto a molecular surface with a Gaussian
   distance weighting, and colours the surface from the lowest to the highest energy.

The result is an isosurface potential energy surface (IPES): a picture of where the
probe is most and least stable around the molecule.

## Requirements

- JDK 21 to 25. The code still contains Jmol's applet classes, which need
  `java.applet`; that package is removed in JDK 26.
- Maven 3.9 or newer.
- Psi4 or Gaussian, to run the generated jobs. Neither is needed to build or start
  RADMAP. Psi4 installs with conda:

  ```sh
  brew install miniforge
  conda create -n psi4 -c conda-forge psi4
  ```

## Build and run

```sh
./start-radmap-jmol
```

The script builds the jar the first time and passes any arguments on to Jmol. To do
the two steps by hand:

```sh
mvn package
java -jar target/radmap-0.1.0-SNAPSHOT-all.jar
```

`mvn package` produces two jars in `target/`:

| Jar | Contents |
| --- | --- |
| `radmap-0.1.0-SNAPSHOT-all.jar` | RADMAP plus its dependencies; runnable on its own |
| `radmap-0.1.0-SNAPSHOT.jar` | RADMAP classes and resources only |

To run from the build tree without packaging, use `mvn compile exec:java`.

The usual Jmol command-line options work. For example, to run a script with no
window and then exit:

```sh
java -jar target/radmap-0.1.0-SNAPSHOT-all.jar -n -x -J 'load mymolecule.xyz; ...'
```

## Using RADMAP

Both tools are in the **Calculations** menu. Load a molecule first.

### isosurfacePES

| Field | Meaning |
| --- | --- |
| Program | Psi4 (the default) or Gaussian; see [Psi4 mode](#psi4-mode) for what differs |
| Amount of Memory, Number of Processors | Written as `%mem` and `%nproc` in each input file |
| Method, Basis Set, Density Fitting Basis Set, Job Options | Make up the Gaussian route line; the default is `# UB3LYP/6-31G scf=xqc` |
| Total Charge, Multiplicity | Charge and multiplicity of the molecule plus the fragment; the multiplicity defaults to 2 |
| Molecule Name | File name for the saved copy of the molecule |
| Save Folder Location, Load Folder Location | Where input files are written and where output files are read from; both default to `ISPES` in the working directory |
| isosurface computed resolution | Mesh density of the sampling surface, in points per ångström. This sets the number of Gaussian jobs |
| isosurface computed probe radius | Distance of the sampling surface from the van der Waals surface, in ångströms |
| isosurface displayed resolution | Mesh density of the surface the results are drawn on |
| fragment | Element symbol of the probe atom |
| Color Scheme | ROYGB, BGYOR, RWB, BWR, BW, WB or BWZebra |
| Forces for FlexSurface (Psi4) | Psi4 only: run a gradient instead of an energy, so the outputs also work in the FlexSurface dialog |

The buttons follow the workflow:

- **Test and View Mesh** draws the sampling surface without writing anything, so you
  can see how many points you will get.
- **Generate Files** writes the inputs into the save folder. The folder must already
  exist.
- **Load Files** reads the outputs from the load folder and colours the surface.
- **Run with Psi4** (Psi4 only) does all of it in one go: it generates the files,
  runs them with a progress bar, and loads the results when they finish.

The job count grows quickly with the computed resolution: ethylene at the default
resolution of 1.5 and probe radius of 1.2 gives 174 jobs.

### Psi4 mode

Psi4 is the default program. Compared with Gaussian mode:

- Method and Basis Set offer Psi4 names (`b3lyp`, `b3lyp-d3bj`, `wb97x-d`,
  `def2-TZVP` and so on). Both boxes are editable, so any method or basis Psi4
  accepts can be typed in.
- Job Options holds Psi4 options separated by semicolons, for example
  `soscf true; maxiter 200`. Each becomes a `set` line.
- Density Fitting Basis Set is disabled; Psi4 uses density fitting by default.
- Number of Processors becomes the default thread count per job.

**Run with Psi4** is the quickest route. It creates the save folder if needed, writes
the inputs, and runs them in the background, as many at once as there are cores
divided by the thread count. The progress bar shows finished and failed jobs, and the
button turns into **Stop** while it runs. When the jobs end the surface is loaded. A
few things to know:

- The "Psi4 command" field is filled in automatically if `psi4` is on the `PATH` or
  in a conda environment named `psi4`. Otherwise enter the full path, or a command
  such as `conda run -n psi4 psi4`.
- Clicking it again resumes: jobs whose input has not changed and that already have
  a result are skipped, so a stopped run continues and failed jobs can be retried
  after changing the options.
- If the folder holds job files from a different, larger surface, it asks before
  deleting them, because loading reads every output in the folder.

To run the jobs yourself instead, for example on another machine, **Generate Files**
writes the `RA_<n>.in` files and a `run-psi4.sh` script. Run the script in the save
folder:

```sh
./run-psi4.sh            # one job at a time
JOBS=4 ./run-psi4.sh     # four jobs side by side
```

The script skips jobs that already finished, so it can be stopped and restarted.
`THREADS` overrides the thread count per job. It needs `psi4` on the `PATH` and a
POSIX shell.

**Load Files** works the same for both programs and recognises each output by its
content, so nothing needs selecting when loading.

Psi4 mode has been checked against Psi4 1.11 with a handful of ethylene jobs.

### FlexSurface

This dialog reads the same kind of folder, but uses the forces on the atoms rather
than the energy. With Gaussian that means outputs from geometry optimisations in
Cartesian coordinates (for example `opt(cartesian,MaxCycles=2)` in the job options),
because the forces are read from the `-DE/DX` table. With Psi4, tick the forces box
before generating. The two are not identical: Gaussian reports the forces at the
last optimisation step, Psi4 at the geometry as generated.

- **Load Surface** colours the surface by how strongly the probe pushes bonded atoms
  sideways relative to each other.
- **Load Total DEDX surface** colours it by the summed magnitude of the forces on the
  atoms.

### Side panel

The main window also has an "Add Atom" panel on the left for building and editing
structures.

## File formats

Files written to the save folder by **Generate Files**:

| File | Contents |
| --- | --- |
| `RA  <n>.gjf` | Gaussian input for vertex `n`. The name has two spaces |
| `RA Batch File.bcf` | Gaussian batch control file pairing each `.gjf` with its `.out` |
| `<molecule name>.mol` | The molecule as loaded |

In Psi4 mode the folder instead holds:

| File | Contents |
| --- | --- |
| `RA_<n>.in` | Psi4 input for vertex `n` |
| `run-psi4.sh` | Runs every input that has no finished output |
| `radmap-jobs.txt` | The inputs that make up the current surface, one per line |
| `<molecule name>.mol` | The molecule as loaded |

A Psi4 input ends with a few lines of Python that print the probe position, the
energy and, if requested, the forces on lines starting `RADMAP_PROBE`,
`RADMAP_ENERGY` and `RADMAP_FORCE`. Loading reads only those lines.

Each Gaussian input file has the title line `RA <fragment> 0 <x> <y> <z>`. Gaussian echoes it
into the output, and that is how RADMAP recovers the probe position when loading.

When loading, RADMAP reads every file in the folder whose name starts with `RA` and
ends with `.out`. It also writes the values it extracted, one `x y z value` row per
job, to `VertexDataFile energy.txt` (or `VertexDataFile flex.txt` from the
FlexSurface dialog) for analysis elsewhere.

`RandomFunctions` in the default package is a small helper that splits a batch file
into three, for running the jobs on several machines.

## Tests

```sh
mvn test
```

The tests are in `test/` and cover the RADMAP code only: Gaussian and Psi4 input
generation, output parsing, the colour mapping, and a generate-then-load round trip through a headless
viewer. The Jmol code RADMAP is built on has no tests.

## Where the code is

Everything lives under `src/`, in Jmol's original layout with resources next to the
sources. Nearly all of it is unmodified Jmol. The RADMAP parts are:

| Path | Purpose |
| --- | --- |
| `org/jmol/util/isosurfacePES.java` | Input generation, output parsing and surface colouring |
| `org/jmol/util/Psi4Engine.java` | Psi4 input files, run script and output parsing |
| `org/jmol/util/Psi4Runner.java` | Runs the Psi4 jobs in parallel, with resume and cancel |
| `org/jmol/util/DEDXSurfaceUtils.java` | Force-based (flex and total dE/dX) surfaces |
| `org/jmol/util/ColorIndexUtil.java`, `DataVertex.java` | Colour palettes and the per-vertex data record |
| `org/openscience/jmol/app/jmolpanel/IsosurfacePESDialog.java`, `FlexSurfaceDialog.java` | The two dialogs |
| `org/openscience/jmol/app/ModelKitSideMenu.java`, `FragmentPanel.java` | The side panel |
| `org/jmol/script/RadicalAnalyzer.java` | An earlier prototype, not used by the application |

## Dependencies

| Library | Version | Used for |
| --- | --- | --- |
| `javax.vecmath:vecmath` | 1.5.2 | Vector maths throughout Jmol |
| `commons-cli:commons-cli` | 1.11.0 | Command-line parsing |
| `com.github.librepdf:openpdf` | 3.0.5 | PDF export; replaces iText 2 |

Small libraries that Jmol vendored as source (`com.json`, `com.obrador`,
`com.sparshui`) are still compiled from `src/`.

## Differences from stock Jmol 12.3.26

Besides the RADMAP tools, getting the code to build on a current JDK needed a few
changes:

- The translation function `GT._()` is now `GT.$()`, because `_` is a reserved word
  since Java 9.
- PDF export uses OpenPDF (`org.openpdf.text`) in place of iText.
- JSpecView is not included, so spectrum synchronisation with Jmol does nothing.
- `JsonNioService` is left out of the build because it needs the naga library, which
  is not on Maven Central. Molecular Playground kiosk mode therefore does not work.
- The browser applet classes compile but cannot run: current JVMs have no browser
  plugin.

## Known limitations

- The "Vector Weighting" A and C fields in the FlexSurface dialog are read but not
  used; the weighting is fixed in the code.
- The total dE/dX sum leaves out the last atom in the output, which is the probe.
- Loading fails if the load folder has no usable `RA*.out` files.
- Jmol leaves water molecules out of a solvent surface, so a molecule that is just
  water gives an empty sampling surface and no input files.
- The source tree still contains Windows touch-screen binaries and other files
  inherited from Jmol that the build ignores.

## Licence

Jmol is distributed under the GNU Lesser General Public License, and RADMAP, as a
derivative of it, is too. The bundled JPEG encoder in `com/obrador` has its own
terms; see `src/com/obrador/license.txt`.
