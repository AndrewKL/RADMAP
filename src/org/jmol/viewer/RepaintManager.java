/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-02-29 00:41:24 -0400 (Wed, 29 Feb 2012) $
 * $Revision: 16839 $
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
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.viewer;

import org.jmol.api.JmolRendererInterface;
import org.jmol.g3d.Graphics3D;
import org.jmol.modelset.ModelSet;
import org.jmol.shape.Shape;
import org.jmol.shape.ShapeRenderer;
import org.jmol.util.Logger;
import org.jmol.util.Rectangle;

import java.util.BitSet;

import javax.vecmath.Point3f;

class RepaintManager {

  private Viewer viewer;
  private ShapeManager shapeManager;
  private ShapeRenderer[] renderers;
  
  RepaintManager(Viewer viewer, ShapeManager shapeManager) {
    this.viewer = viewer;
    this.shapeManager = shapeManager;
  }

  /////////// thread management ///////////
  
  private int holdRepaint = 0;
  boolean repaintPending;

  void pushHoldRepaint() {
    ++holdRepaint;
    //System.out.println("repaintManager pushHoldRepaint holdRepaint=" + holdRepaint + " thread=" + Thread.currentThread().getName());
  }

  void popHoldRepaint(boolean andRepaint) {
    --holdRepaint;
    //System.out.println("repaintManager popHoldRepaint holdRepaint=" + holdRepaint + " thread=" + Thread.currentThread().getName());
    if (holdRepaint <= 0) {
      holdRepaint = 0;
      if (andRepaint) {
        repaintPending = true;
        //System.out.println("RM popholdrepaint TRUE " + (test++));
        viewer.repaint();
      }
    }
  }

  boolean refresh() {
    if (repaintPending)
      return false;
    repaintPending = true;
    if (holdRepaint == 0) {
      //System.out.println("RM refresh() " + (test++));
      viewer.repaint();
    }
    return true;
  }

  synchronized void repaintDone() {
    repaintPending = false;
    //System.out.println("repaintManager repaintDone thread=" + Thread.currentThread().getName());
    notify(); // to cancel any wait in requestRepaintAndWait()
  }

  synchronized void requestRepaintAndWait() {
    //System.out.println("RM requestRepaintAndWait() " + (test++));
    viewer.repaint();
    try {
      //System.out.println("repaintManager requestRepaintAndWait I am waiting for a repaint: thread=" + Thread.currentThread().getName());
      wait(viewer.getRepaintWait());  // more than a second probably means we are locked up here
      if (repaintPending) {
        Logger.error("repaintManager requestRepaintAndWait timeout");
        repaintDone();
      }
    } catch (InterruptedException e) {
      //System.out.println("repaintManager requestRepaintAndWait interrupted thread=" + Thread.currentThread().getName());
    }
    //System.out.println("repaintManager requestRepaintAndWait I am no longer waiting for a repaint: thread=" + Thread.currentThread().getName());
  }

  /////////// renderer management ///////////
  
  void clear(int iShape) {
    if (renderers ==  null)
      return;
    if (iShape >= 0)
      renderers[iShape] = null;
    else
      for (int i = 0; i < JmolConstants.SHAPE_MAX; ++i)
        renderers[i] = null;
  }

  private ShapeRenderer getRenderer(int shapeID, Graphics3D g3d) {
    if (renderers[shapeID] != null)
      return renderers[shapeID];
    String className = JmolConstants.getShapeClassName(shapeID) + "Renderer";
    try {
      Class<?> shapeClass = Class.forName(className);
      ShapeRenderer renderer = (ShapeRenderer) shapeClass.newInstance();
      renderer.setViewerG3dShapeID(viewer, g3d, shapeID);
      return renderers[shapeID] = renderer;
    } catch (Exception e) {
      Logger.error("Could not instantiate renderer:" + className, e);
      return null;
    }
  }

  /////////// actual rendering ///////////
  
  private boolean logTime;
  
  void render(Graphics3D g3d, ModelSet modelSet, boolean isFirstPass) {
    if (modelSet == null || !viewer.mustRenderFlag())
      return;
    logTime = false;//viewer.getTestFlag(2);
    if (logTime)
      Logger.startTimer();
    viewer.finalizeTransformParameters();
    try {
      g3d.renderBackground();
      if (isFirstPass)  {
        int[] minMax = shapeManager.transformAtoms(bsAtoms, ptOffset);
        bsAtoms = null;
        if (minMax != null)
          renderCrossHairs(g3d, minMax);
        renderSelectionRubberBand(g3d);
      }
      if (renderers == null)
        renderers = new ShapeRenderer[JmolConstants.SHAPE_MAX];
      for (int i = 0; i < JmolConstants.SHAPE_MAX && g3d.currentlyRendering(); ++i) {
        Shape shape = shapeManager.getShape(i);
        if (shape == null)
          continue;
        getRenderer(i, g3d).render(g3d, modelSet, shape);
        if (logTime)
          Logger.checkTimer("render time " + JmolConstants.getShapeClassName(i));
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.error("rendering error? ");
    }
  }

  String renderExport(String type, Graphics3D g3d, ModelSet modelSet,
                      String fileName) {

    JmolRendererInterface g3dExport = null;
    Object output = null;
    boolean isOK;
    viewer.finalizeTransformParameters();
    try {
      shapeManager.transformAtoms(null, null);
      output = (fileName == null ? new StringBuffer() : fileName);
      Class<?> export3Dclass = Class.forName("org.jmol.export.Export3D");
      g3dExport = (JmolRendererInterface) export3Dclass.newInstance();
      isOK = viewer.initializeExporter(g3dExport, type, output);
    } catch (Exception e) {
      isOK = false;
    }
    if (!isOK) {
      Logger.error("Cannot export " + type);
      return null;
    }
    g3dExport.renderBackground();
    for (int i = 0; i < JmolConstants.SHAPE_MAX; ++i) {
      Shape shape = shapeManager.getShape(i);
      if (shape != null)
        getRenderer(i, g3d).render(g3dExport, modelSet, shape);
    }
    return g3dExport.finalizeOutput();
  }

  /////////// Allow during-rendering mouse operations ///////////
  
  private BitSet bsAtoms;
  private Point3f ptOffset = new Point3f();
  
  void setSelectedTranslation(BitSet bsAtoms, char xyz, int xy) {
    this.bsAtoms = bsAtoms;
    switch (xyz) {
    case 'X':
    case 'x':
      ptOffset.x += xy;
      break;
    case 'Y':
    case 'y':
      ptOffset.y += xy;
      break;
    case 'Z':
    case 'z':
      ptOffset.z += xy;
      break;
    }
    //System.out.println(xyz + " " + xy + " " + ptOffset);
  }
  
  /////////// special rendering ///////////
  
  private void renderCrossHairs(Graphics3D g3d, int[] minMax) {
    // this is the square and crosshairs for the navigator
    Point3f navOffset = new Point3f(viewer.getNavigationOffset());
    boolean antialiased = g3d.isAntialiased();
    float navDepth = viewer.getNavigationDepthPercent();
    g3d.setColix(navDepth < 0 ? Graphics3D.RED
        : navDepth > 100 ? Graphics3D.GREEN : Graphics3D.GOLD);
    int x = Math.max(Math.min(viewer.getScreenWidth(), (int) navOffset.x), 0);
    int y = Math.max(Math.min(viewer.getScreenHeight(), (int) navOffset.y), 0);
    int z = (int) navOffset.z + 1;
    // TODO: fix for antialiasDisplay
    int off = (antialiased ? 8 : 4);
    int h = (antialiased ? 20 : 10);
    int w = (antialiased ? 2 : 1);
    g3d.drawRect(x - off, y, z, 0, h, w);
    g3d.drawRect(x, y - off, z, 0, w, h);
    g3d.drawRect(x - off, y - off, z, 0, h, h);
    off = h;
    h = h >> 1;
    g3d.setColix(minMax[1] < navOffset.x ? Graphics3D.YELLOW
            : Graphics3D.GREEN);
    g3d.drawRect(x - off, y, z, 0, h, w);
    g3d.setColix(minMax[0] > navOffset.x ? Graphics3D.YELLOW
            : Graphics3D.GREEN);
    g3d.drawRect(x + h, y, z, 0, h, w);
    g3d.setColix(minMax[3] < navOffset.y ? Graphics3D.YELLOW
            : Graphics3D.GREEN);
    g3d.drawRect(x, y - off, z, 0, w, h);
    g3d.setColix(minMax[2] > navOffset.y ? Graphics3D.YELLOW
            : Graphics3D.GREEN);
    g3d.drawRect(x, y + h, z, 0, w, h);
  }

  private void renderSelectionRubberBand(Graphics3D g3d) {
    Rectangle band = viewer.getRubberBandSelection();
    if (band != null && g3d.setColix(viewer.getColixRubberband()))
      g3d.drawRect(band.x, band.y, 0, 0, band.width, band.height);
  }
  
}
