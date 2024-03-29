/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-03-30 11:40:16 -0500 (Fri, 30 Mar 2007) $
 * $Revision: 7273 $
 *
 * Copyright (C) 2007 Miguel, Bob, Jmol Development
 *
 * Contact: hansonr@stolaf.edu
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.jvxl.readers;

import java.io.BufferedReader;
import java.util.BitSet;

import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;

import org.jmol.api.Interface;
import org.jmol.api.QuantumPlaneCalculationInterface;
import org.jmol.atomdata.AtomData;
import org.jmol.util.Logger;
import org.jmol.util.Parser;

abstract class VolumeFileReader extends SurfaceFileReader {

  protected boolean endOfData;
  protected boolean negativeAtomCount;
  protected int atomCount;
  protected int nSurfaces;
  protected boolean isAngstroms;
  protected boolean canDownsample;
  private int[] downsampleRemainders;
  private boolean preProcessPlanes;

  VolumeFileReader(SurfaceGenerator sg, BufferedReader br) {
    super(sg, br);
    canDownsample = isProgressive = isXLowToHigh = true;
    jvxlData.wasCubic = true;
    boundingBox = params.boundingBox;
    if (params.qmOrbitalType == Parameters.QM_TYPE_NCI_SCF) {
      hasColorData = (params.parameters == null || params.parameters[1] >= 0);
      preProcessPlanes = true;
      params.insideOut = !params.insideOut;
    }
  }

  private int nData;

  protected float recordData(float value) {
    if (Float.isNaN(value))
      return value;
    if (value < dataMin)
      dataMin = value;
    if (value > dataMax)
      dataMax = value;
    dataMean += value;
    nData++;
    return value;
  }

  boolean readerClosed;

  @Override
  protected void closeReader() {
    if (readerClosed)
      return;
    readerClosed = true;
    super.closeReader();
    if (nData == 0 || dataMax == -Float.MAX_VALUE)
      return;
    dataMean /= nData;
    Logger.info("VolumeFileReader closing file: " + nData
        + " points read \ndata min/max/mean = " + dataMin + "/" + dataMax + "/"
        + dataMean);
  }

  @Override
  protected boolean readVolumeParameters(boolean isMapData) {
    endOfData = false;
    nSurfaces = readVolumetricHeader();
    if (nSurfaces == 0)
      return false;
    if (nSurfaces < params.fileIndex) {
      Logger
          .warn("not enough surfaces in file -- resetting params.fileIndex to "
              + nSurfaces);
      params.fileIndex = nSurfaces;
    }
    return true;
  }

  Point4f thePlane;

  @Override
  protected boolean readVolumeData(boolean isMapData) {
    if (!gotoAndReadVoxelData(isMapData))
      return false;
    if (!vertexDataOnly)
      Logger.info("JVXL read: " + nPointsX + " x " + nPointsY + " x "
          + nPointsZ + " data points");
    return true;
  }

  private int readVolumetricHeader() {
    try {
      readParameters();
      if (atomCount == Integer.MIN_VALUE)
        return 0;

      Logger.info("voxel grid origin:" + volumetricOrigin);

      int downsampleFactor = params.downsampleFactor;
      boolean downsampling = (canDownsample && downsampleFactor > 0);
      if (downsampling) {
        downsampleRemainders = new int[3];
        Logger.info("downsample factor = " + downsampleFactor);
        for (int i = 0; i < 3; ++i) {
          int n = voxelCounts[i];
          downsampleRemainders[i] = n % downsampleFactor;
          voxelCounts[i] /= downsampleFactor;
          volumetricVectors[i].scale(downsampleFactor);
          Logger.info("downsampling axis " + (i + 1) + " from " + n + " to "
              + voxelCounts[i]);
        }
      }
      for (int i = 0; i < 3; ++i) {
        line = voxelCounts[i] + " " + volumetricVectors[i].x + " "
            + volumetricVectors[i].y + " " + volumetricVectors[i].z;
        jvxlFileHeaderBuffer.append(line).append('\n');
        Logger.info("voxel grid count/vector:" + line);
        if (!isAngstroms)
          volumetricVectors[i].scale(ANGSTROMS_PER_BOHR);
      }
      scaleIsosurface(params.scale);
      volumeData.setVolumetricXml();
      return nSurfaces;
    } catch (Exception e) {
      Logger.error(e.toString());
      return 0;
    }
  }

  abstract protected void readParameters() throws Exception;

  // generally useful:

  protected String skipComments(boolean allowBlankLines) throws Exception {
    StringBuffer sb = new StringBuffer();
    while (readLine() != null
        && (allowBlankLines && line.length() == 0 || line.indexOf("#") == 0))
      sb.append(line).append('\n');
    return sb.toString();
  }

  protected void readVoxelVector(int voxelVectorIndex) throws Exception {
    readLine();
    Vector3f voxelVector = volumetricVectors[voxelVectorIndex];
    if ((voxelCounts[voxelVectorIndex] = parseInt(line)) == Integer.MIN_VALUE) //unreadable
      next[0] = line.indexOf(" ");
    voxelVector.set(parseFloat(), parseFloat(), parseFloat());
    if (isAnisotropic)
      setVectorAnisotropy(voxelVector);
  }

  protected int downsampleFactor;
  private int nSkipX, nSkipY, nSkipZ;

  protected void initializeSurfaceData() {
    downsampleFactor = params.downsampleFactor;
    nSkipX = 0;
    nSkipY = 0;
    nSkipZ = 0;
    if (canDownsample && downsampleFactor > 0) {
      nSkipX = downsampleFactor - 1;
      nSkipY = downsampleRemainders[2] + (downsampleFactor - 1)
          * (nSkipZ = (nPointsZ * downsampleFactor + downsampleRemainders[2]));
      nSkipZ = downsampleRemainders[1] * nSkipZ + (downsampleFactor - 1)
          * nSkipZ * (nPointsY * downsampleFactor + downsampleRemainders[1]);
    }

    if (params.thePlane != null) {
      params.cutoff = 0f;
    } else if (isJvxl) {
      params.cutoff = (params.isBicolorMap || params.colorBySign ? 0.01f : 0.5f);
    }
    nDataPoints = 0;
    next[0] = 0;
    line = "";
    jvxlNSurfaceInts = 0;
  }

  @Override
  protected void readSurfaceData(boolean isMapData) throws Exception {
    /*
     * possibilities:
     * 
     * cube file data only -- monochrome surface (single pass)
     * cube file with plane (color, two pass)
     * cube file data + cube file color data (two pass)
     * jvxl file no color data (single pass)
     * jvxl file with color data (single pass)
     * jvxl file with plane (single pass)
     * 
     * cube file with multiple MO data will be interspersed 
     * 
     * 
     */
    /* 
     * This routine is used twice in the case of color mapping. 
     * First (isMapData = false) to read the surface values, which
     * might be a plane, then (isMapData = true) to color them based 
     * on a second data set.
     * 
     * Planes are compatible with data sets that return actual 
     * numbers at all grid points -- cube files, orbitals, functionXY,
     * and solvent/molecular surface calculations.
     *  
     * It is possible to map a QM orbital onto a plane. In the first pass we defined
     * the plane; in the second pass we just calculate the new voxel values and return.
     * 
     * Starting with Jmol 11.7.25, JVXL files do not create voxelData[][][]
     * and instead just fill a bitset, thus saving nx*ny*nz*8 - (nx*ny*nz/32) bytes in memory
     * 
     */

    initializeSurfaceData();
    if (isProgressive && !isMapData || isJvxl) {
      nDataPoints = volumeData.setVoxelCounts(nPointsX, nPointsY, nPointsZ);
      voxelData = null;
      if (isJvxl)
        jvxlVoxelBitSet = getVoxelBitSet(nDataPoints);
    } else if (isMapData && volumeData.hasPlane()) {
      volumeData.setVoxelMap();
      float f = volumeData.getToPlaneParameter(); // was mappingPlane
      for (int x = 0; x < nPointsX; ++x) {
        for (int y = 0; y < nPointsY; ++y) {
          for (int z = 0; z < nPointsZ; ++z) {
            float v = recordData(getNextVoxelValue()); // was mappingPlane
            if (volumeData.isNearPlane(x, y, z, f))
              volumeData.setVoxelMapValue(x, y, z, v);
            if (nSkipX != 0)
              skipVoxels(nSkipX);
          }
          if (nSkipY != 0)
            skipVoxels(nSkipY);
        }
        if (nSkipZ != 0)
          skipVoxels(nSkipZ);
      }
    } else {
      voxelData = new float[nPointsX][][];
      // Note downsampling not allowed for JVXL files
      // This filling of voxelData should only be needed
      // for mapped data.

      for (int x = 0; x < nPointsX; ++x) {
        float[][] plane = new float[nPointsY][];
        voxelData[x] = plane;
        for (int y = 0; y < nPointsY; ++y) {
          float[] strip = new float[nPointsZ];
          plane[y] = strip;
          for (int z = 0; z < nPointsZ; ++z) {
            strip[z] = recordData(getNextVoxelValue());
            if (nSkipX != 0)
              skipVoxels(nSkipX);
          }
          if (nSkipY != 0)
            skipVoxels(nSkipY);
        }
        if (nSkipZ != 0)
          skipVoxels(nSkipZ);
      }
      //Jvxl getNextVoxelValue records the data read on its own.
    }
    volumeData.setVoxelData(voxelData);
  }

  // For a progressive reader, we need to build two planes at a time
  // and keep them indexed. reading x low to high, we will first encounter
  // plane 0, then plane 1.
  // Note that we cannot do this when the file is being opened for
  // mapping. In that case we either need ALL the points
  // or, for some readers (IsoMOReader, NCI versions of file reader)
  // we can process mapped data as individual points in one pass.

  @Override
  public float[] getPlane(int x) {
    if (x == 0)
      initPlanes();
    if (preProcessPlanes)
      return getPlaneProcessed(x);
    float[] plane = super.getPlane(x);
    if (qpc == null)
      getPlane(plane, true);
    return plane;
  }

  private float[][] yzPlanesRaw;
  private int iPlaneRaw;

  /**
   * Retrieve raw file planes and pass them to the calculation object for
   * processing into new data.
   * 
   * Bob Hanson hansonr@stolaf.edu 6/7/2011
   * 
   * @param x
   * @return plane (for testing)
   */
  public float[] getPlaneProcessed(int x) {
    float[] plane;
    if (iPlaneRaw == 0) {
      qpc = (QuantumPlaneCalculationInterface) Interface
          .getOptionInterface("quantum.NciCalculation");
      AtomData atomData = new AtomData();
      atomData.modelIndex = -1; // -1 here means fill ALL atoms; any other
      // means "this model only"
      atomData.bsSelected = params.bsSelected;
      sg.fillAtomData(atomData, AtomData.MODE_FILL_COORDS);
      qpc.setupCalculation(volumeData, sg.getBsSelected(), null, null, null,
          atomData.atomXyz, -1, null, null, null, null, null, null, null,
          params.theProperty, true, null, params.parameters, params.testFlags);
      iPlaneRaw = 1;
      qpc.setPlanes(yzPlanesRaw = new float[4][yzCount]);
      if (hasColorData) {
        //float nan = qpc.getNoValue();
        getPlane(yzPlanesRaw[0], false);
        getPlane(yzPlanesRaw[1], false);
        plane = yzPlanes[0];
        for (int i = 0; i < yzCount; i++)
          plane[i] = Float.NaN;
        return plane;
      }
      iPlaneRaw = -1;
    }
    float nan = qpc.getNoValue();
    int x1 = nPointsX - 1;
    switch (iPlaneRaw) {
    case -1:
      plane = yzPlanes[x % 2];
      x1++;
      break;
    case 3:
      plane = yzPlanesRaw[0];
      yzPlanesRaw[0] = yzPlanesRaw[1];
      yzPlanesRaw[1] = yzPlanesRaw[2];
      yzPlanesRaw[2] = yzPlanesRaw[3];
      yzPlanesRaw[3] = plane;
      plane = yzPlanesRaw[iPlaneRaw];
      break;
    default:
      iPlaneRaw++;
      plane = yzPlanesRaw[iPlaneRaw];
    }
    if (x < x1) {
      getPlane(plane, false);
      qpc.calcPlane(x, plane = yzPlanes[x % 2]);
      for (int i = 0; i < yzCount; i++)
        if (plane[i] != nan)
          recordData(plane[i]);
    } else {
      for (int i = 0; i < yzCount; i++)
        plane[i] = Float.NaN;
    }
    return plane;
  }

  private void getPlane(float[] plane, boolean doRecord) {
    try {
      for (int y = 0, ptyz = 0; y < nPointsY; ++y) {
        for (int z = 0; z < nPointsZ; ++z) {
          float v = getNextVoxelValue();
          if (doRecord)
            recordData(v);
          plane[ptyz++] = v;
          if (nSkipX != 0)
            skipVoxels(nSkipX);
        }
        if (nSkipY != 0)
          skipVoxels(nSkipY);
      }
      if (nSkipZ != 0)
        skipVoxels(nSkipZ);
    } catch (Exception e) {
      // ignore
    }
  }

  protected Point3f[] boundingBox;

  @Override
  public float getValue(int x, int y, int z, int ptyz) {
    // if (x == 0 && ptyz + 1 == yzCount) {
    //first value -- ALWAYS send
    // }
    if (boundingBox != null) {
      volumeData.voxelPtToXYZ(x, y, z, ptTemp);
      if (ptTemp.x < boundingBox[0].x || ptTemp.x > boundingBox[1].x
          || ptTemp.y < boundingBox[0].y || ptTemp.y > boundingBox[1].y
          || ptTemp.z < boundingBox[0].z || ptTemp.z > boundingBox[1].z)
        return Float.NaN;
    }
    return super.getValue(x, y, z, ptyz);
  }

  private void skipVoxels(int n) throws Exception {
    // not allowed for JVXL data
    for (int i = n; --i >= 0;)
      getNextVoxelValue();
  }

  /**
   * 
   * @param nPoints
   * @return JVXL bitset
   * @throws Exception
   */
  protected BitSet getVoxelBitSet(int nPoints) throws Exception {
    // jvxlReader will use this to read the surface voxel data
    return null;
  }

  protected float getNextVoxelValue() throws Exception {
    float voxelValue = 0;
    if (nSurfaces > 1 && !params.blockCubeData) {
      for (int i = 1; i < params.fileIndex; i++)
        nextVoxel();
      voxelValue = nextVoxel();
      for (int i = params.fileIndex; i < nSurfaces; i++)
        nextVoxel();
    } else {
      voxelValue = nextVoxel();
    }
    return voxelValue;
  }

  protected float nextVoxel() throws Exception {
    float voxelValue = parseFloat();
    if (Float.isNaN(voxelValue)) {
      while (readLine() != null && Float.isNaN(voxelValue = parseFloat(line))) {
      }
      if (line == null) {
        if (!endOfData)
          Logger.warn("end of file reading cube voxel data? nBytes=" + nBytes
              + " nDataPoints=" + nDataPoints + " (line):" + line);
        endOfData = true;
        line = "0 0 0 0 0 0 0 0 0 0";
      }
    }
    return voxelValue;
  }

  @Override
  protected void gotoData(int n, int nPoints) throws Exception {
    if (!params.blockCubeData)
      return;
    if (n > 0)
      Logger.info("skipping " + n + " data sets, " + nPoints + " points each");
    for (int i = 0; i < n; i++)
      skipData(nPoints);
  }

  protected void skipData(int nPoints) throws Exception {
    int iV = 0;
    while (iV < nPoints)
      iV += countData(readLine());
  }

  private int countData(String str) {
    int count = 0;
    int ich = 0;
    int ichMax = str.length();
    char ch;
    while (ich < ichMax) {
      while (ich < ichMax && ((ch = str.charAt(ich)) == ' ' || ch == '\t'))
        ++ich;
      if (ich < ichMax)
        ++count;
      while (ich < ichMax && ((ch = str.charAt(ich)) != ' ' && ch != '\t'))
        ++ich;
    }
    return count;
  }

  /**
   * checks an atom line for "ANGSTROMS", possibly overriding the data's natural
   * units, BOHR (similar to Gaussian CUBE files).
   * 
   * @param isXLowToHigh
   * @param isAngstroms
   * @param strAtomCount
   * @param atomLine
   * @param bs
   * @return isAngstroms
   */
  protected static boolean checkAtomLine(boolean isXLowToHigh,
                                         boolean isAngstroms,
                                         String strAtomCount, String atomLine,
                                         StringBuffer bs) {
    if (atomLine.indexOf("ANGSTROMS") >= 0)
      isAngstroms = true;
    int atomCount = (strAtomCount == null ? Integer.MAX_VALUE : Parser
        .parseInt(strAtomCount));
    switch (atomCount) {
    case Integer.MIN_VALUE:
      atomCount = 0;
      atomLine = " " + atomLine.substring(atomLine.indexOf(" ") + 1);
      break;
    case Integer.MAX_VALUE:
      atomCount = Integer.MIN_VALUE;
      break;
    default:
      String s = "" + atomCount;
      atomLine = atomLine.substring(atomLine.indexOf(s) + s.length());
    }
    if (isAngstroms) {
      if (atomLine.indexOf("ANGSTROM") < 0)
        atomLine += " ANGSTROMS";
    } else {
      if (atomLine.indexOf("BOHR") < 0)
        atomLine += " BOHR";
    }
    atomLine = (atomCount == Integer.MIN_VALUE ? ""
        : (isXLowToHigh ? "+" : "-") + Math.abs(atomCount))
        + atomLine + "\n";
    bs.append(atomLine);
    return isAngstroms;
  }

  @Override
  protected float getSurfacePointAndFraction(float cutoff,
                                             boolean isCutoffAbsolute,
                                             float valueA, float valueB,
                                             Point3f pointA,
                                             Vector3f edgeVector, int x, int y,
                                             int z, int vA, int vB,
                                             float[] fReturn, Point3f ptReturn) {

    float zero = super.getSurfacePointAndFraction(cutoff, isCutoffAbsolute,
        valueA, valueB, pointA, edgeVector, x, y, z, vA, vB, fReturn, ptReturn);
    if (qpc == null || Float.isNaN(zero) || !hasColorData)
      return zero;
    /*
     * in the case of an NCI calculation, we need to process
     * the two end points vA an vB individually, then do
     * the interpolation.
     * 
     */
    vA = marchingCubes.getLinearOffset(x, y, z, vA);
    vB = marchingCubes.getLinearOffset(x, y, z, vB);
    return qpc.process(vA, vB, fReturn[0]);
  }
  
  private boolean isScaledAlready;
  private void scaleIsosurface(float scale) {
    if (isScaledAlready)
      return;
    isScaledAlready = true;
    if (isAnisotropic)
      setVolumetricAnisotropy();
    if (Float.isNaN(scale))
      return;
    Logger.info("applying scaling factor of " + scale);
    volumetricOrigin.scaleAdd((1 - scale) / 2, volumetricVectors[0]);
    volumetricOrigin.scaleAdd((1 - scale) / 2, volumetricVectors[1]);
    volumetricOrigin.scaleAdd((1 - scale) / 2, volumetricVectors[2]);
    volumetricVectors[0].scale(scale);
    volumetricVectors[1].scale(scale);
    volumetricVectors[2].scale(scale);
  }

}
