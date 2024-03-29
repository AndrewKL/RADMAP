/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-04-02 14:18:23 -0300 (Mon, 02 Apr 2012) $
 * $Revision: 16955 $
 *
 * Copyright (C) 2003-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.shapespecial;

import org.jmol.shape.AtomShape;
import org.jmol.util.BitSetUtil;
import org.jmol.util.Escape;
import org.jmol.util.FastBitSet;
import org.jmol.util.Logger;

import org.jmol.atomdata.RadiusData;
import org.jmol.g3d.Graphics3D;
import org.jmol.geodesic.EnvelopeCalculation;
import org.jmol.modelset.Atom;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

public class Dots extends AtomShape {

  public EnvelopeCalculation ec;
  public boolean isSurface = false;

  final static float SURFACE_DISTANCE_FOR_CALCULATION = 10f;

  BitSet bsOn = new BitSet();
  private BitSet bsSelected, bsIgnore;

  static int MAX_LEVEL = EnvelopeCalculation.MAX_LEVEL;

  int thisAtom;
  float thisRadius;
  int thisArgb;

  RadiusData rdLast = new RadiusData();

  @Override
  public void initShape() {
    super.initShape();
    translucentAllowed = false; //except for geosurface
    ec = new EnvelopeCalculation(viewer, atomCount, mads);
  }

  @Override
  public int getSize(int atomIndex) {
    // mads are actually radii not diameters
    return (mads == null ? (int) (ec.getRadius(atomIndex) * 2000) : mads[atomIndex]*2);
  }
  
  @Override
  public void setProperty(String propertyName, Object value, BitSet bs) {

    if ("init" == propertyName) {
      initialize();
      return;
    }

    if ("translucency" == propertyName) {
      if (!translucentAllowed)
        return; // no translucent dots, but ok for geosurface
    }

    if ("ignore" == propertyName) {
      bsIgnore = (BitSet) value;
      return;
    }

    if ("select" == propertyName) {
      bsSelected = (BitSet) value;
      return;
    }

    // next four are for serialization
    if ("radius" == propertyName) {
      thisRadius = ((Float) value).floatValue();
      if (thisRadius > Atom.RADIUS_MAX)
        thisRadius = Atom.RADIUS_MAX;
      return;
    }
    if ("colorRGB" == propertyName) {
      thisArgb = ((Integer) value).intValue();
      return;
    }
    if ("atom" == propertyName) {
      thisAtom = ((Integer) value).intValue();
      atoms[thisAtom].setShapeVisibility(myVisibilityFlag, true);
      ec.allocDotsConvexMaps(atomCount);
      return;
    }
    if ("dots" == propertyName) {
      isActive = true;
      ec.setFromBits(thisAtom, (BitSet) value);
      atoms[thisAtom].setShapeVisibility(myVisibilityFlag, true);
      if (mads == null) {
        ec.setMads(null);
        mads = new short[atomCount];
        for (int i = 0; i < atomCount; i++)          
          if (atoms[i].isInFrame() && atoms[i].isShapeVisible(myVisibilityFlag)) 
            // was there a reason we were not checking for hidden?
            mads[i] = (short) (ec.getAppropriateRadius(i) * 1000);
        ec.setMads(mads);
      }
      mads[thisAtom] = (short) (thisRadius * 1000f);
      if (colixes == null) {
        colixes = new short[atomCount];
        paletteIDs = new byte[atomCount];
      }
      colixes[thisAtom] = Graphics3D.getColix(thisArgb);
      bsOn.set(thisAtom);
      //all done!
      return;
    }

    if ("refreshTrajectories" == propertyName) {
      bs = (BitSet) ((Object[]) value)[1];
      Matrix4f m4 = (Matrix4f) ((Object[]) value)[2];
      if (m4 == null) // end of compare command
        return;
      Matrix3f m = new Matrix3f();
      m4.get(m);
      ec.reCalculate(bs, m);
      return;
    }

    if (propertyName == "deleteModelAtoms") {
      int firstAtomDeleted = ((int[])((Object[])value)[2])[1];
      int nAtomsDeleted = ((int[])((Object[])value)[2])[2];
      BitSetUtil.deleteBits(bsOn, bs);
      ec.deleteAtoms(firstAtomDeleted, nAtomsDeleted);
      // pass to AtomShape via super
    }

    super.setProperty(propertyName, value, bs);
  }

  void initialize() {
    bsSelected = null;
    bsIgnore = null;
    isActive = false;
    if (ec == null)
      ec = new EnvelopeCalculation(viewer, atomCount, mads);
  }

  @Override
  protected void setSize(RadiusData rd, BitSet bsSelected) {
    if (rd == null)
      rd = new RadiusData(0, RadiusData.EnumType.ABSOLUTE, null);
    if (this.bsSelected != null)
      bsSelected = this.bsSelected;

    // if mad == 0 then turn it off
    // 1 van der Waals (dots) or +1.2, calconly)
    // -1 ionic/covalent
    // 2 - 1001 (mad-1)/100 * van der Waals
    // 1002 - 11002 (mad - 1002)/1000 set radius 0.0 to 10.0 angstroms
    // 11003- 13002 (mad - 11002)/1000 set radius to vdw + additional radius
    // Short.MIN_VALUE -- ADP min
    // Short.MAX_VALUE -- ADP max

    if (Logger.debugging) {
      Logger.debug("Dots.setSize " + rd.value);
    }

    boolean isVisible = true;
    float setRadius = Float.MAX_VALUE;
    isActive = true;

    switch (rd.factorType) {
    case OFFSET:
      break;
    case ABSOLUTE:
      if (rd.value == 0)
        isVisible = false;
      setRadius = rd.value;
      // fall through
    default:
      rd.valueExtended = viewer.getCurrentSolventProbeRadius();
    }

    float maxRadius;
    switch (rd.vdwType) {
    case ADPMIN:
    case ADPMAX:
    case HYDRO:
    case TEMP:
      maxRadius = setRadius;
      break;
    case IONIC:
      maxRadius = modelSet.getMaxVanderwaalsRadius() * 2; // TODO?
      break;
    default:
      maxRadius = modelSet.getMaxVanderwaalsRadius();
    }

    if (Logger.debugging)
      Logger.startTimer();

    // combine current and selected set
    boolean newSet = (rdLast.value != rd.value
        || rdLast.valueExtended != rd.valueExtended || rdLast.factorType != rd.factorType
        || rdLast.vdwType != rd.vdwType || ec.getDotsConvexMax() == 0);

    // for an solvent-accessible surface there is no torus/cavity issue.
    // we just increment the atom radius and set the probe radius = 0;

    if (isVisible) {
      for (int i = bsSelected.nextSetBit(0); i >= 0; i = bsSelected
          .nextSetBit(i + 1))
        if (!bsOn.get(i)) {
          bsOn.set(i);
          newSet = true;
        }
    } else {
      boolean isAll = (bsSelected == null);
      int i0 = (isAll ? atomCount - 1 : bsSelected.nextSetBit(0));
      for (int i = i0; i >= 0; i = (isAll ? i - 1 : bsSelected
          .nextSetBit(i + 1)))
        bsOn.set(i, false);
    }

    for (int i = atomCount; --i >= 0;) {
      atoms[i].setShapeVisibility(myVisibilityFlag, bsOn.get(i));
    }
    if (!isVisible)
      return;
    if (newSet) {
      mads = null;
      ec.newSet();
    }

    // always delete old surfaces for selected atoms
    FastBitSet[] dotsConvexMaps = ec.getDotsConvexMaps();
    if (dotsConvexMaps != null) {
      for (int i = atomCount; --i >= 0;)
        if (bsOn.get(i)) {
          dotsConvexMaps[i] = null;
        }
    }
    // now, calculate surface for selected atoms

    if (dotsConvexMaps == null) {
      colixes = new short[atomCount];
      paletteIDs = new byte[atomCount];
    }
    ec.calculate(rd, maxRadius, bsOn, bsIgnore, !viewer.getDotSurfaceFlag(),
        viewer.getDotsSelectedOnlyFlag(), isSurface, true);

    rdLast = rd;

    if (Logger.debugging)
      Logger.checkTimer("dots generation time");
  }

  @Override
  public void setModelClickability() {
    for (int i = atomCount; --i >= 0;) {
      Atom atom = atoms[i];
      if ((atom.getShapeVisibilityFlags() & myVisibilityFlag) == 0
          || modelSet.isAtomHidden(i))
        continue;
      atom.setClickable(myVisibilityFlag);
    }
  }

  @Override
  public String getShapeState() {
    FastBitSet[] dotsConvexMaps = ec.getDotsConvexMaps();
    if (dotsConvexMaps == null || ec.getDotsConvexMax() == 0)
      return "";
    StringBuffer s = new StringBuffer();
    Map<String, BitSet> temp = new Hashtable<String, BitSet>();
    int atomCount = viewer.getAtomCount();
    String type = (isSurface ? "geoSurface " : "dots ");
    for (int i = 0; i < atomCount; i++) {
      if (!bsOn.get(i) || dotsConvexMaps[i] == null)
        continue;
      if (bsColixSet != null && bsColixSet.get(i))
        setStateInfo(temp, i, getColorCommand(type, paletteIDs[i], colixes[i]));
      FastBitSet bs = dotsConvexMaps[i];
      if (!bs.isEmpty()) {
        float r = ec.getAppropriateRadius(i);
        appendCmd(s, type + i + " radius " + r + " "
            + Escape.escape(bs.toBitSet()));
      }
    }
    s.append(getShapeCommands(temp, null));
    return s.toString();
  }

}
