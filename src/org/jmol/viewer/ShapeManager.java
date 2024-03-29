/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2010-02-15 07:31:37 -0600 (Mon, 15 Feb 2010) $
 * $Revision: 12396 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development
 *
 * Contact: jmol-developers@lists.sf.net, jmol-developers@lists.sf.net
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
package org.jmol.viewer;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

import org.jmol.atomdata.RadiusData;
import org.jmol.constant.EnumPalette;
import org.jmol.constant.EnumVdw;
import org.jmol.g3d.Graphics3D;
import org.jmol.modelset.Atom;
import org.jmol.modelset.Group;
import org.jmol.modelset.ModelSet;
import org.jmol.script.Token;
import org.jmol.shape.Shape;
import org.jmol.util.JmolMolecule;
import org.jmol.util.Logger;

public class ShapeManager {

  private Graphics3D g3d;
  private ModelSet modelSet;
  private Shape[] shapes;
  private Viewer viewer;

  public ShapeManager(Viewer viewer, ModelSet modelSet) {
    // from ParallelProcessor
    this(viewer);
    resetShapes();
    loadDefaultShapes(modelSet);
  }

  ShapeManager(Viewer viewer) {
    this.viewer = viewer;
    g3d = viewer.getGraphics3D();
  }

  // public methods 
  
  public void findNearestShapeAtomIndex(int x, int y, Atom[] closest, BitSet bsNot) {
    if (shapes != null)
      for (int i = 0; i < shapes.length && closest[0] == null; ++i)
        if (shapes[i] != null)
          shapes[i].findNearestAtomIndex(x, y, closest, bsNot);
  }

  public Shape[] getShapes() {
    return shapes;
  }
  
  public Object getShapeProperty(int shapeID, String propertyName, int index) {
    if (shapes == null || shapes[shapeID] == null)
      return null;
    viewer.setShapeErrorState(shapeID, "get " + propertyName);
    Object result = shapes[shapeID].getProperty(propertyName, index);
    viewer.setShapeErrorState(-1, null);
    return result;
  }

  public boolean getShapeProperty(int shapeID, String propertyName, Object[] data) {
    if (shapes == null || shapes[shapeID] == null)
      return false;
    viewer.setShapeErrorState(shapeID, "get " + propertyName);
    boolean result = shapes[shapeID].getProperty(propertyName, data);
    viewer.setShapeErrorState(-1, null);
    return result;
  }

  /**
   * Returns the shape type index for a shape object given the object name.
   * @param objectName (string) string name of object
   * @return shapeType (int) integer corresponding to the shape type index
   *                   see ShapeManager.shapes[].
   */
  public int getShapeIdFromObjectName(String objectName) {
    if (shapes != null)
      for (int i = JmolConstants.SHAPE_MIN_SPECIAL; i < JmolConstants.SHAPE_MAX_MESH_COLLECTION; ++i)
        if (shapes[i] != null && shapes[i].getIndexFromName(objectName) >= 0)
          return i;
    return -1;
  }

  public void loadDefaultShapes(ModelSet newModelSet) {
    modelSet = newModelSet;
    if (shapes != null)
    for (int i = 0; i < shapes.length; ++i)
      if (shapes[i] != null)
        shapes[i].setModelSet(newModelSet);
    loadShape(JmolConstants.SHAPE_BALLS);
    loadShape(JmolConstants.SHAPE_STICKS);
    loadShape(JmolConstants.SHAPE_MEASURES);
    loadShape(JmolConstants.SHAPE_BBCAGE);
    loadShape(JmolConstants.SHAPE_UCCAGE);
  }

  public Shape loadShape(int shapeID) {
    if (shapes == null)
      return null;
    if (shapes[shapeID] != null)
      return shapes[shapeID];
    if (shapeID == JmolConstants.SHAPE_HSTICKS
        || shapeID == JmolConstants.SHAPE_SSSTICKS
        || shapeID == JmolConstants.SHAPE_STRUTS)
      return null;
    String className = JmolConstants.getShapeClassName(shapeID);
    try {
      Class<?> shapeClass = Class.forName(className);
      Shape shape = (Shape) shapeClass.newInstance();
      viewer.setShapeErrorState(shapeID, "allocate");
      shape.initializeShape(viewer, g3d, modelSet, shapeID);
      viewer.setShapeErrorState(-1, null);
      return shapes[shapeID] = shape;
    } catch (Exception e) {
      Logger.error("Could not instantiate shape:" + className, e);
      return null;
    }
  }

  public void refreshShapeTrajectories(int baseModel, BitSet bs, Matrix4f mat) {
    Integer Imodel = Integer.valueOf(baseModel);
    BitSet bsModelAtoms = viewer.getModelUndeletedAtomsBitSet(baseModel);
    for (int i = 0; i < JmolConstants.SHAPE_MAX; i++)
      if (shapes[i] != null)
        setShapeProperty(i, "refreshTrajectories", new Object[] { Imodel, bs, mat }, bsModelAtoms);    
  }

  public void releaseShape(int shapeID) {
    if (shapes != null) 
      shapes[shapeID] = null;  
  }
  
  public void resetShapes() {
    if (!viewer.noGraphicsAllowed())
      shapes = new Shape[JmolConstants.SHAPE_MAX];
  }
  
  public void setShapeSize(int shapeID, int size, RadiusData rd, BitSet bsSelected) {
    if (shapes == null)
      return;
    if (bsSelected == null && 
        (shapeID != JmolConstants.SHAPE_STICKS || size != Integer.MAX_VALUE))
      bsSelected = viewer.getSelectionSet(false);
    if (rd != null && rd.value != 0 && rd.vdwType == EnumVdw.TEMP)
      modelSet.getBfactor100Lo();
    viewer.setShapeErrorState(shapeID, "set size");
    if (rd != null && rd.value != 0 || rd == null && size != 0)
      loadShape(shapeID);
    if (shapes[shapeID] != null) {
      shapes[shapeID].setShapeSize(size, rd, bsSelected);
    }
    viewer.setShapeErrorState(-1, null);
  }

  public void setLabel(String strLabel, BitSet bsSelection) {
    if (strLabel != null) { // force the class to load and display
      loadShape(JmolConstants.SHAPE_LABELS);
      setShapeSize(JmolConstants.SHAPE_LABELS, 0, null, bsSelection);
    }
    setShapeProperty(JmolConstants.SHAPE_LABELS, "label", strLabel, bsSelection);
  }

  public void setShapeProperty(int shapeID, String propertyName, Object value,
                               BitSet bsSelected) {
    if (shapes == null || shapes[shapeID] == null)
      return;
    viewer.setShapeErrorState(shapeID, "set " + propertyName);
    shapes[shapeID].setShapeProperty(propertyName.intern(), value, bsSelected);
    viewer.setShapeErrorState(-1, null);
  }

  // methods local to Viewer and other managers
  
  boolean checkFrankclicked(int x, int y) {
    Shape frankShape = shapes[JmolConstants.SHAPE_FRANK];
    return (frankShape != null && frankShape.wasClicked(x, y));
  }

  private final static int[] hoverable = {
    JmolConstants.SHAPE_ECHO, 
    JmolConstants.SHAPE_CONTACT,
    JmolConstants.SHAPE_ISOSURFACE,
    JmolConstants.SHAPE_DRAW,
    JmolConstants.SHAPE_FRANK,
  };
  
  private static int clickableMax = hoverable.length - 1;
  
  Map<String, Object> checkObjectClicked(int x, int y, int modifiers, BitSet bsVisible) {
    Shape shape;
    Map<String, Object> map = null;
    if (modifiers != 0
        && viewer.getBondPicking()
        && (map = shapes[JmolConstants.SHAPE_STICKS].checkObjectClicked(x, y,
            modifiers, bsVisible)) != null)
      return map;

    for (int i = 0; i < clickableMax; i++)
      if ((shape = shapes[hoverable[i]]) != null
          && (map = shape.checkObjectClicked(x, y, modifiers, bsVisible)) != null)
        return map;
    return null;
  }
 
  boolean checkObjectDragged(int prevX, int prevY, int x, int y, int modifiers,
                             BitSet bsVisible, int iShape) {
    boolean found = false;
    int n = (iShape > 0 ? iShape + 1 : JmolConstants.SHAPE_MAX);
    for (int i = iShape; !found && i < n; ++i)
      if (shapes[i] != null)
        found = shapes[i].checkObjectDragged(prevX, prevY, x, y, modifiers,
            bsVisible);
    return found;
  }

  boolean checkObjectHovered(int x, int y, BitSet bsVisible, boolean checkBonds) {
    Shape shape = shapes[JmolConstants.SHAPE_STICKS];
    if (checkBonds && shape != null
        && shape.checkObjectHovered(x, y, bsVisible))
      return true;
    for (int i = 0; i < hoverable.length; i++) {
      shape = shapes[hoverable[i]];
      if (shape != null && shape.checkObjectHovered(x, y, bsVisible))
        return true;
    }
    return false;
  }

  void deleteShapeAtoms(Object[] value, BitSet bs) {
    if (shapes != null)
      for (int j = 0; j < JmolConstants.SHAPE_MAX; j++)
        if (shapes[j] != null)
          setShapeProperty(j, "deleteModelAtoms", value, bs);
  }

  void deleteVdwDependentShapes(BitSet bs) {
    if (shapes[JmolConstants.SHAPE_ISOSURFACE] != null)
      shapes[JmolConstants.SHAPE_ISOSURFACE].setShapeProperty("deleteVdw", null, bs);
    if (shapes[JmolConstants.SHAPE_CONTACT] != null)
      shapes[JmolConstants.SHAPE_CONTACT].setShapeProperty("deleteVdw", null, bs);
  }

  float getAtomShapeValue(int tok, Group group, int atomIndex) {
    int iShape = JmolConstants.shapeTokenIndex(tok);
    if (iShape < 0 || shapes[iShape] == null) 
      return 0;
    int mad = shapes[iShape].getSize(atomIndex);
    if (mad == 0) {
      if ((group.shapeVisibilityFlags & shapes[iShape].myVisibilityFlag) == 0)
        return 0;
      mad = shapes[iShape].getSize(group);
    }
    return mad / 2000f;
  }

  void getObjectMap(Map<String, Token> map, boolean withDollar) {
    if (shapes == null)
      return;
    Boolean bDollar = Boolean.valueOf(withDollar);
      for (int i = JmolConstants.SHAPE_MIN_SPECIAL; i < JmolConstants.SHAPE_MAX_MESH_COLLECTION; ++i)
          getShapeProperty(i, "getNames", new Object[] { map , bDollar } );
  }

  Object getProperty(Object paramInfo) {
    if (paramInfo.equals("getShapes"))
      return shapes;
    return null;
  }

  private final BitSet bsRenderableAtoms = new BitSet();

  BitSet getRenderableBitSet() {
    return bsRenderableAtoms;
  }
  
  Shape getShape(int i) {
    //RepaintManager
    return (shapes == null ? null : shapes[i]);
  }
  
  Map<String, Object> getShapeInfo() {
    Map<String, Object> info = new Hashtable<String, Object>();
    StringBuffer commands = new StringBuffer();
    if (shapes != null)
      for (int i = 0; i < JmolConstants.SHAPE_MAX; ++i) {
        Shape shape = shapes[i];
        if (shape != null) {
          String shapeType = JmolConstants.shapeClassBases[i];
          List<Map<String, Object>> shapeDetail = shape.getShapeDetail();
          if (shapeDetail != null)
            info.put(shapeType, shapeDetail);
        }
      }
    if (commands.length() > 0)
      info.put("shapeCommands", commands.toString());
    return info;
  }

  void getShapeState(StringBuffer commands, boolean isAll) {
    if (shapes == null)
      return;
    String cmd;
    for (int i = 0; i < JmolConstants.SHAPE_MAX; ++i) {
      Shape shape = shapes[i];
      if (shape != null && (isAll || JmolConstants.isShapeSecondary(i))
          && (cmd = shape.getShapeState()) != null && cmd.length() > 1)
        commands.append(cmd);
    }
    commands.append("  select *;\n");
  }

  void mergeShapes(Shape[] newShapes) {
    if (newShapes == null)
      return;
    if (shapes == null)
      shapes = newShapes;
    else
      for (int i = 0; i < newShapes.length; ++i)
        if (newShapes[i] != null) {
          if (shapes[i] == null)
            loadShape(i);
          shapes[i].merge(newShapes[i]);
        }
  }

  void resetBioshapes(BitSet bsAllAtoms) {
    if (shapes == null)
      return;
    for (int i = 0; i < shapes.length; ++i)
      if (shapes[i] != null && shapes[i].isBioShape) {
        shapes[i].setModelSet(modelSet);
        shapes[i].setShapeSize(0, null, bsAllAtoms);
        shapes[i].setShapeProperty("color",
            EnumPalette.NONE, bsAllAtoms);
      }
  }

  void setAtomLabel(String strLabel, int i) {
    if (shapes == null)
      return;
    loadShape(JmolConstants.SHAPE_LABELS);
    shapes[JmolConstants.SHAPE_LABELS].setProperty("label:"+strLabel, Integer.valueOf(i), null);
  }
  
  void setModelVisibility() {
    if (shapes == null || shapes[JmolConstants.SHAPE_BALLS] == null)
      return;

    //named objects must be set individually
    //in the future, we might include here a BITSET of models rather than just a modelIndex

    // all these isTranslucent = f() || isTranslucent are that way because
    // in general f() does MORE than just check translucency. 
    // so isTranslucent = isTranslucent || f() would NOT work.

    BitSet bs = viewer.getVisibleFramesBitSet();
    
    //NOT balls (that is done later)
    for (int i = 1; i < JmolConstants.SHAPE_MAX; i++)
      if (shapes[i] != null)
        shapes[i].setVisibilityFlags(bs);
    // BALLS sets the JmolConstants.ATOM_IN_MODEL flag.
    shapes[JmolConstants.SHAPE_BALLS].setVisibilityFlags(bs);

    //set clickability -- this enables measures and such
    for (int i = 0; i < JmolConstants.SHAPE_MAX; ++i) {
      Shape shape = shapes[i];
      if (shape != null)
        shape.setModelClickability();
    }
  }

  private final int[] navigationCrossHairMinMax = new int[4];

  int[] transformAtoms(BitSet bsAtoms, Point3f ptOffset) {
    if (bsAtoms != null) {
      // translateSelected operation
      Point3f ptCenter = viewer.getAtomSetCenter(bsAtoms);
      Point3f pt = new Point3f();
      viewer.transformPoint(ptCenter, pt);
      pt.add(ptOffset);
      viewer.unTransformPoint(pt, pt);
      pt.sub(ptCenter);
      viewer.setAtomCoordRelative(pt, bsAtoms);
      ptOffset.set(0, 0, 0);
    }
    bsRenderableAtoms.clear();
    Atom[] atoms = modelSet.atoms;
    Vector3f[] vibrationVectors = modelSet.vibrationVectors;
    for (int i = modelSet.getAtomCount(); --i >= 0;) {
      Atom atom = atoms[i];
      if ((atom.getShapeVisibilityFlags() & JmolConstants.ATOM_IN_FRAME) == 0)
        continue;
      bsRenderableAtoms.set(i);
      // note that this vibration business is not compatible with
      // PDB objects such as cartoons and traces, which 
      // use Cartesian coordinates, not screen coordinates
      Point3i screen = (vibrationVectors != null && atom.hasVibration() 
          ? viewer.transformPoint(atom, vibrationVectors[i]) 
              : viewer.transformPoint(atom));
      atom.screenX = screen.x;
      atom.screenY = screen.y;
      atom.screenZ = screen.z;
      atom.screenDiameter = viewer.scaleToScreen(screen.z, Math
          .abs(atom.madAtom));
    }

    if (viewer.getSlabEnabled()) {
      boolean slabByMolecule = viewer.getSlabByMolecule();
      boolean slabByAtom = viewer.getSlabByAtom();
      int minZ = g3d.getSlab();
      int maxZ = g3d.getDepth();
      if (slabByMolecule) {
        JmolMolecule[] molecules = modelSet.getMolecules();
        int moleculeCount = modelSet.getMoleculeCountInModel(-1);
        for (int i = 0; i < moleculeCount; i++) {
          JmolMolecule m = molecules[i];
          int j = 0;
          int pt = m.firstAtomIndex;
          if (!bsRenderableAtoms.get(pt))
            continue;
          for (; j < m.atomCount; j++, pt++)
            if (g3d.isClippedZ(atoms[pt].screenZ
                - (atoms[pt].screenDiameter >> 1)))
              break;
          if (j != m.atomCount) {
            pt = m.firstAtomIndex;
            for (int k = 0; k < m.atomCount; k++) {
              bsRenderableAtoms.clear(pt);
              atoms[pt++].screenZ = 0;
            }
          }
        }
      }
      for (int i = bsRenderableAtoms.nextSetBit(0); i >= 0; i = bsRenderableAtoms
          .nextSetBit(i + 1)) {
        Atom atom = atoms[i];
        if (g3d.isClippedZ(atom.screenZ
            - (slabByAtom ? atoms[i].screenDiameter >> 1 : 0))) {
          atom.setClickable(0);
          // note that in the case of navigation,
          // maxZ is set to Integer.MAX_VALUE.
          int r = (slabByAtom ? -1 : 1) * atom.screenDiameter / 2;
          if (atom.screenZ + r < minZ || atom.screenZ - r > maxZ
              || !g3d.isInDisplayRange(atom.screenX, atom.screenY)) {
            bsRenderableAtoms.clear(i);
          }
        }
      }
    }
    if (modelSet.getAtomCount() == 0 || !viewer.getShowNavigationPoint())
      return null;
    // set min/max for navigation crosshair rendering
    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (int i = bsRenderableAtoms.nextSetBit(0); i >= 0; i = bsRenderableAtoms
        .nextSetBit(i + 1)) {
      Atom atom = atoms[i];
      if (atom.screenX < minX)
        minX = atom.screenX;
      if (atom.screenX > maxX)
        maxX = atom.screenX;
      if (atom.screenY < minY)
        minY = atom.screenY;
      if (atom.screenY > maxY)
        maxY = atom.screenY;
    }
    navigationCrossHairMinMax[0] = minX;
    navigationCrossHairMinMax[1] = maxX;
    navigationCrossHairMinMax[2] = minY;
    navigationCrossHairMinMax[3] = maxY;
    return navigationCrossHairMinMax;
  }

}
