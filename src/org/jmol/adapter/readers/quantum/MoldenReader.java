package org.jmol.adapter.readers.quantum;

import org.jmol.adapter.smarter.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jmol.api.JmolAdapter;
import org.jmol.util.Logger;

/**
 * A molecular structure and orbital reader for MolDen files.
 * See http://www.cmbi.ru.nl/molden/molden_format.html
 * 
 * updated by Bob Hanson <hansonr@stolaf.edu> for Jmol 12.0/12.1
 * 
 * @author Matthew Zwier <mczwier@gmail.com>
 */

public class MoldenReader extends MopacSlaterReader {
  
  private boolean loadGeometries;
  private boolean loadVibrations;
  private boolean vibOnly;
  private boolean optOnly;
 
  private String orbitalType = "";
  private int modelAtomCount;
  
  @Override
  protected void initializeReader() {
    vibOnly = checkFilter("VIBONLY");
    optOnly = checkFilter("OPTONLY");
    loadGeometries = !vibOnly && desiredVibrationNumber < 0 && !checkFilter("NOOPT");
    loadVibrations = !optOnly && desiredModelNumber < 0 && !checkFilter("NOVIB");
   
    if (checkFilter("ALPHA"))
      filter = "alpha";
    else if (checkFilter("BETA"))
      filter = "beta";
    else
      filter = null;
  }

  @Override
  protected boolean checkLine() throws Exception {
    if (!line.contains("["))
      return true;
    line = line.toUpperCase().trim();
    if (!line.startsWith("["))
      return true;
    Logger.info(line);
    if (line.indexOf("[ATOMS]") == 0) {
      readAtoms();
      modelAtomCount = atomSetCollection.getFirstAtomSetAtomCount();
      return false;
    }
    if (line.indexOf("[GTO]") == 0)
      return readGaussianBasis();
    if (line.indexOf("[MO]") == 0) 
      return (!readMolecularOrbitals || readMolecularOrbitals());
    if (line.indexOf("[FREQ]") == 0)
      return (!loadVibrations || readFreqsAndModes());
    if (line.indexOf("[GEOCONV]") == 0)
      return (!loadGeometries || readGeometryOptimization());
    checkOrbitalType(line);
    return true;
  }

  @Override
  public void finalizeReader() {
    // a hack to make up for Molden's ** limitation in writing shell information
    // assumption is that there is an atom of the same type just prior to the missing one.
    if (bsBadIndex.isEmpty())
      return;
    try {
      int ilast = 0;
      Atom[] atoms = atomSetCollection.getAtoms();
      int nAtoms = atomSetCollection.getAtomCount();
      bsAtomOK.set(nAtoms);
      int n = shells.size();
      for (int i = 0; i < n; i++) {
        int iatom = shells.get(i)[0];
        if (iatom != Integer.MAX_VALUE) {
          ilast = atoms[iatom].elementNumber;
          continue;
        }
        for (int j = bsAtomOK.nextClearBit(0); j >= 0; j = bsAtomOK
            .nextClearBit(j + 1)) {
          if (atoms[j].elementNumber == ilast) {
            shells.get(i)[0] = j;
            Logger.info("MoldenReader assigning shells starting with " + i
                + " for ** to atom " + (j + 1) + " z " + ilast);
            for (; ++i < n && !bsBadIndex.get(i)
                && shells.get(i)[0] == Integer.MAX_VALUE;)
              shells.get(i)[0] = j;
            i--;
            bsAtomOK.set(j);
            break;
          }
        }
      }
    } catch (Exception e) {
      Logger.error("Molden reader could not assign shells -- abandoning MOs");
      atomSetCollection.setAtomSetAuxiliaryInfo("moData", null);
    }

  }
  private void readAtoms() throws Exception {
    /* 
     [Atoms] {Angs|AU}
     C     1    6         0.0076928100       -0.0109376700        0.0000000000
     H     2    1         0.0779745600        1.0936027600        0.0000000000
     H     3    1         0.9365572000       -0.7393011000        0.0000000000
     H     4    1         1.1699572800        0.2075167300        0.0000000000
     H     5    1        -0.4338802400       -0.3282176500       -0.9384614500
     H     6    1        -0.4338802400       -0.3282176500        0.9384614500
     */
    
    String coordUnit = getTokens()[1];
    
    int nPrevAtom = 0, nCurAtom = 0;
   
    boolean isAU = (coordUnit.indexOf("ANGS") < 0); 
    if (isAU && coordUnit.indexOf("AU") < 0) {
      throw new Exception("invalid coordinate unit " + coordUnit + " in [Atoms]"); 
    }
    
    float f = (isAU ? ANGSTROMS_PER_BOHR : 1);
    while (readLine() != null && line.indexOf('[') < 0) {    
      String [] tokens = getTokens();
      if (tokens.length < 6)
        continue;
      Atom atom = atomSetCollection.addNewAtom();
      atom.atomName = tokens[0];
      // tokens[1] is the atom number.  Since sane programs shouldn't list
      // these out of order, just throw an exception if one is encountered
      // out of order (for now)
      nCurAtom = parseInt(tokens[1]);
      if (nPrevAtom > 0 && nCurAtom != nPrevAtom + 1 ) { 
        throw new Exception("out of order atom in [Atoms]");
      } 
      nPrevAtom = nCurAtom;
      atom.elementNumber = (short) parseInt(tokens[2]);
      setAtomCoord(atom, parseFloat(tokens[3]) * f, 
          parseFloat(tokens[4]) * f, 
          parseFloat(tokens[5]) * f);
    }    
  }
  
  private BitSet bsAtomOK = new BitSet();
  private BitSet bsBadIndex = new BitSet();
  
  private boolean readGaussianBasis() throws Exception {
    /* 
     [GTO]
       1 0
      s   10 1.00
       0.8236000000D+04  0.5309998617D-03
       0.1235000000D+04  0.4107998930D-02
       0.2808000000D+03  0.2108699451D-01
       0.7927000000D+02  0.8185297868D-01
       0.2559000000D+02  0.2348169388D+00
       0.8997000000D+01  0.4344008869D+00
       0.3319000000D+01  0.3461289099D+00
       0.9059000000D+00  0.3937798974D-01
       0.3643000000D+00 -0.8982997660D-02
       0.1285000000D+00  0.2384999379D-02
      s   10 1.00
     */
    shells = new ArrayList<int[]>();
    List<float[]> gdata = new ArrayList<float[]>();
    int atomIndex = 0;
    int gaussianPtr = 0;
    int nCoef = 0;

    while (readLine() != null
        && !((line = line.trim()).length() == 0 || line.charAt(0) == '[')) {
      // First, expect the number of the atomic center
      // The 0 following the atom index is now optional
      String[] tokens = getTokens();

      // Molden may have ** in place of the atom index when there are > 99 atoms
      atomIndex = parseInt(tokens[0]) - 1;
      if (atomIndex == Integer.MAX_VALUE) {
        bsBadIndex.set(shells.size());
      } else {
        bsAtomOK.set(atomIndex);
      }
      // Next is a sequence of shells and their primitives
      while (readLine() != null && line.trim().length() > 0) {
        // Next line has the shell label and a count of the number of primitives
        tokens = getTokens();
        String shellLabel = tokens[0].toUpperCase();
        int nPrimitives = parseInt(tokens[1]);
        int[] slater = new int[4];

        slater[0] = atomIndex;
        slater[1] = JmolAdapter.getQuantumShellTagID(shellLabel);
        slater[2] = gaussianPtr;
        slater[3] = nPrimitives;
        nCoef += getDfCoefMaps()[slater[1]].length;

        for (int ip = nPrimitives; --ip >= 0;) {
          // Read ip primitives, each containing an exponent and one (s,p,d,f)
          // or two (sp) contraction coefficient(s)
          String[] primTokens = getTokens(readLine());
          int nTokens = primTokens.length;
          float orbData[] = new float[nTokens];

          for (int d = 0; d < nTokens; d++)
            orbData[d] = parseFloat(primTokens[d]);
          gdata.add(orbData);
          gaussianPtr++;
        }
        shells.add(slater);
      }
      // Next atom
    }

    float[][] garray = new float[gaussianPtr][];
    for (int i = 0; i < gaussianPtr; i++) {
      garray[i] = gdata.get(i);
    }
    moData.put("shells", shells);
    moData.put("gaussians", garray);
    Logger.info(shells.size() + " slater shells read");
    Logger.info(garray.length + " gaussian primitives read");
    Logger.info(nCoef + " MO coefficients expected for orbital type " + orbitalType);
    atomSetCollection.setAtomSetAuxiliaryInfo("moData", moData);
    return false;
  }
  
  private boolean readMolecularOrbitals() throws Exception {
    /*
      [MO]
       Ene=     -11.5358
       Spin= Alpha
       Occup=   2.000000
         1   0.99925949663
         2  -0.00126378192
         3   0.00234724545
     [and so on]
       110   0.00011350764
       Ene=      -1.3067
       Spin= Alpha
       Occup=   1.984643
         1  -0.00865451496
         2   0.79774685891
         3  -0.01553604903
     */
    
    
    while(checkOrbitalType(readLine())) {
      //
    }
      
    fixOrbitalType();
    // TODO we are assuming Jmol-cannonical order for orbital coefficients.
    // see BasisFunctionReader
    // TODO no check here for G orbitals
    
    String[] tokens = getMoTokens(line);
    while (tokens != null && tokens[0].indexOf('[') < 0) {
      Map<String, Object> mo = new Hashtable<String, Object>();
      List<String> data = new ArrayList<String>();
      float energy = Float.NaN;
      float occupancy = Float.NaN;
      String symmetry = null;      
      String key;
      while (parseInt(key = tokens[0]) == Integer.MIN_VALUE) {
        if (key.startsWith("Ene")) {
          energy = parseFloat(tokens[1]);          
        } else if (key.startsWith("Occup")) {
          occupancy = parseFloat(tokens[1]);
        } else if (key.startsWith("Sym")) {
          symmetry = tokens[1];
        } else if (key.startsWith("Spin")) {
          alphaBeta = tokens[1].toLowerCase();
        }
        tokens = getMoTokens(null);
      }
      while (tokens != null && parseInt(tokens[0]) != Integer.MIN_VALUE) {
        if (tokens.length != 2)
          throw new Exception("invalid MO coefficient specification");
        // tokens[0] is the function number, and tokens[1] is the coefficient
        data.add(tokens[1]);
        tokens = getMoTokens(null);
      }
      
      float[] coefs = new float[data.size()];
      for (int i = data.size(); --i >= 0;)
        coefs[i] = parseFloat(data.get(i));
      String l = line;
      line = "";
      if (filterMO()) {
        mo.put("coefficients", coefs);
        if (!Float.isNaN(energy))
          mo.put("energy", new Float(energy));
        if (!Float.isNaN(occupancy))
          mo.put("occupancy", new Float(occupancy));
        if (symmetry != null)
          mo.put("symmetry", symmetry);
        if (alphaBeta.length() > 0)
          mo.put("type", alphaBeta);
        setMO(mo);
        if (Logger.debugging) {
          Logger.debug(coefs.length + " coefficients in MO " + orbitals.size() );
        }
      }
      line = l;
    }
    Logger.debug("read " + orbitals.size() + " MOs");
    setMOs("eV");
    return false;
  }
  
  private String[] getMoTokens(String line) throws Exception {
    return (line == null && (line = readLine()) == null ? null : getTokens(line.replace('=',' ')));
  }

  private boolean checkOrbitalType(String line) {
    if (line.length() > 3 && "5D 6D 7F 10".indexOf(line.substring(1,3)) >= 0) {
      orbitalType += line;
      fixOrbitalType();
      return true;
    }
    return false;
  }

  private void fixOrbitalType() {
    if (orbitalType.contains("5D")) {
      fixSlaterTypes(JmolAdapter.SHELL_D_CARTESIAN, JmolAdapter.SHELL_D_SPHERICAL);
      fixSlaterTypes(JmolAdapter.SHELL_F_CARTESIAN, JmolAdapter.SHELL_F_SPHERICAL);
    } 
    if (orbitalType.contains("10F")) {
      fixSlaterTypes(JmolAdapter.SHELL_F_SPHERICAL, JmolAdapter.SHELL_F_CARTESIAN);
    } 
  }

  private boolean readFreqsAndModes() throws Exception {
    String[] tokens;
    List<String> frequencies = new ArrayList<String>();
    BitSet bsOK = new BitSet();
    int iFreq = 0;
    while (readLine() != null && line.indexOf('[') < 0) {
      String f = getTokens()[0];
      bsOK.set(iFreq++, parseFloat(f) != 0);
      frequencies.add(f);
    }
    int nFreqs = frequencies.size();
    skipTo("[FR-COORD]");
    if (!vibOnly)
      readAtomSet("frequency base geometry", true, true);
    skipTo("[FR-NORM-COORD]");
    boolean haveVib = false;
    for (int nFreq = 0; nFreq < nFreqs; nFreq++) {
      skipTo("vibration");
      if (!bsOK.get(nFreq) || !doGetVibration(++vibrationNumber)) 
        continue;
      if (haveVib)
        atomSetCollection.cloneLastAtomSet();
      haveVib = true;
      atomSetCollection.setAtomSetFrequency(null, null, Double.valueOf(frequencies.get(nFreq)).toString(), null);
      int i0 = atomSetCollection.getLastAtomSetAtomIndex();
      for (int i = 0; i < modelAtomCount; i++) {
        tokens = getTokens(readLine());
        atomSetCollection.addVibrationVector(i + i0,
            parseFloat(tokens[0]) * ANGSTROMS_PER_BOHR,
            parseFloat(tokens[1]) * ANGSTROMS_PER_BOHR,
            parseFloat(tokens[2]) * ANGSTROMS_PER_BOHR);
      }
    }
    return true;
  }
  
  /*
[GEOCONV]
energy
-.75960756002000E+02
-.75961091052100E+02
-.75961320555300E+02
-.75961337317300E+02
-.75961338487700E+02
-.75961338493500E+02
max-force
0.15499000000000E-01
0.11197000000000E-01
0.50420000000000E-02
0.15350000000000E-02
0.42000000000000E-04
0.60000000000000E-05
[GEOMETRIES] XYZ
     3

 o  0.00000000000000E+00 0.00000000000000E+00 -.36565628831562E+00
 h  -.77567072215814E+00 0.00000000000000E+00 0.18282805096053E+00
 h  0.77567072215814E+00 0.00000000000000E+00 0.18282805096053E+00

  */
  private boolean readGeometryOptimization() throws Exception {
    List<String> energies = new ArrayList<String>();
    readLine(); // energy
    while (readLine() != null 
        && line.indexOf("force") < 0)
      energies.add(Double.valueOf(line.trim()).toString());
    skipTo("[GEOMETRIES] XYZ");
    int nGeom = energies.size();
    int firstModel = (optOnly || desiredModelNumber >= 0 ? 0 : 1);
    modelNumber = firstModel; // input model counts as model 1; vibrations do not count
    boolean haveModel = false;
    if (desiredModelNumber == 0 || desiredModelNumber == nGeom)
      desiredModelNumber = nGeom; 
    else
      setMOData(null);
    for (int i = 0; i < nGeom; i++) {
      readLines(2);
      if (doGetModel(++modelNumber, null)) {
        readAtomSet("Step " + (modelNumber - firstModel) + "/" + nGeom + ": " + energies.get(i), false, 
            !optOnly || haveModel);
        haveModel = true;
      } else {
        readLines(modelAtomCount);
      }
    }
    return true;
  }

  private void skipTo(String key) throws Exception {
    if (line == null || !line.contains(key))
      discardLinesUntilContains(key);
    
  }

  private void readAtomSet(String atomSetName, boolean isBohr, boolean asClone) throws Exception {
    if (asClone && desiredModelNumber < 0)
      atomSetCollection.cloneFirstAtomSet(0);
    float f = (isBohr ? ANGSTROMS_PER_BOHR : 1);
    atomSetCollection.setAtomSetName(atomSetName);
    if (atomSetCollection.getAtomCount() == 0) {
      while (readLine() != null && line.indexOf('[') < 0) {    
        String [] tokens = getTokens();
        if (tokens.length != 4)
          continue;
        Atom atom = atomSetCollection.addNewAtom();
        atom.atomName = tokens[0];
        setAtomCoord(atom, parseFloat(tokens[1]) * f, 
            parseFloat(tokens[2]) * f, 
            parseFloat(tokens[3]) * f);
      }    
      modelAtomCount = atomSetCollection.getLastAtomSetAtomCount();
      return;
    }
    Atom[] atoms = atomSetCollection.getAtoms();
    int i0 = atomSetCollection.getLastAtomSetAtomIndex();
    for (int i = 0; i < modelAtomCount; i++) {
      String[] tokens = getTokens(readLine());
      Atom atom = atoms[i + i0];
      setAtomCoord(atom, parseFloat(tokens[1]) * f, 
          parseFloat(tokens[2]) * f, 
          parseFloat(tokens[3]) * f);
    }
  }
}
