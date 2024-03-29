/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-03-21 13:31:10 -0300 (Wed, 21 Mar 2012) $
 * $Revision: 16916 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
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

package org.jmol.g3d;

import org.jmol.util.ColorUtil;
import org.jmol.util.Int2IntHash;
import org.jmol.util.Shader;

/**
 *<p>
 * Implements a color index model using a colix as a
 * <strong>COLor IndeX</strong>.
 *</p>
 *<p>
 * A colix is a color index represented as a short int.
 *</p>
 *<p>
 * The value 0 is considered a null value ... for no color. In Jmol this
 * generally means that the value is inherited from some other object.
 *</p>
 *<p>
 * The value 1 is used to indicate that color only is to be inherited. 
 * 
 * 0x0001 INHERIT_OPAQUE -- opaque, but with the color coming from the parent.
 * 0x4001 INHERIT_TRANSLUCENT -- translucent but with the color coming from the parent.
 * 
 * The value 2 is used to indicate that one of the palettes is to be used. 
 * 
 * 0x0002 PALETTE, opaque
 * 0x4002 PALETTE, translucent
 * 
 * Palettes themselves are coded separately in a Palette ID that is tracked with
 *</p>
 *
 * @author Miguel, miguel@jmol.org 
 */

class Colix3D {

  private static int colixMax = Graphics3D.SPECIAL_COLIX_MAX;
  private static int[] argbs = new int[128];
  private static int[] argbsGreyscale;
  private static int[][] ashades = new int[128][];
  private static int[][] ashadesGreyscale;
  private static final Int2IntHash colixHash = new Int2IntHash();
  private static final int RAW_RGB_INT = Graphics3D.RAW_RGB;

  Colix3D() {
  }
  
  static short getColix(int argb) {
    if (argb == 0)
      return 0;
    int translucentFlag = 0;
    if ((argb & 0xFF000000) != 0xFF000000) {
      //if ((argb & 0xFF000000) == 0) {
      //  Logger.error("zero alpha channel + non-zero rgb not supported");
      //  throw new IndexOutOfBoundsException();
      //}
      argb |= 0xFF000000;
      translucentFlag = Graphics3D.TRANSLUCENT_50;
    }
    int c = colixHash.get(argb);
    if ((c & RAW_RGB_INT) == RAW_RGB_INT)
      translucentFlag = 0;
    return (c > 0 ? (short) (c | translucentFlag)
        : (short) (allocateColix(argb) | translucentFlag));
  }

  synchronized static int allocateColix(int argb) {
    // double-check to make sure that someone else did not allocate
    // something of the same color while we were waiting for the lock
    if ((argb & 0xFF000000) != 0xFF000000)
      throw new IndexOutOfBoundsException();
    for (int i = colixMax; --i >= Graphics3D.SPECIAL_COLIX_MAX; )
      if (argb == argbs[i])
        return (short)i;
    if (colixMax == argbs.length) {
      int oldSize = colixMax;
      int newSize = oldSize * 2;
      if (newSize > Graphics3D.LAST_AVAILABLE_COLIX + 1)
        newSize = Graphics3D.LAST_AVAILABLE_COLIX + 1;
      int[] t0 = new int[newSize];
      System.arraycopy(argbs, 0, t0, 0, oldSize);
      argbs = t0;

      if (argbsGreyscale != null) {
        t0 = new int[newSize];
        System.arraycopy(argbsGreyscale, 0, t0, 0, oldSize);
        argbsGreyscale = t0;
      }

      int[][] t2 = new int[newSize][];
      System.arraycopy(ashades, 0, t2, 0, oldSize);
      ashades = t2;

      if (ashadesGreyscale != null) {
        t2 = new int[newSize][];
        System.arraycopy(ashadesGreyscale, 0, t2, 0, oldSize);
        ashadesGreyscale = t2;
      }
    }
    argbs[colixMax] = argb;
    //System.out.println("Colix "+colixMax + " = "+Integer.toHexString(argb) + " " + argb);
    if (argbsGreyscale != null)
      argbsGreyscale[colixMax] = ColorUtil.calcGreyscaleRgbFromRgb(argb);
    colixHash.put(argb, colixMax);
    return (colixMax < Graphics3D.LAST_AVAILABLE_COLIX ? colixMax++ : colixMax);
  }

  private synchronized static void calcArgbsGreyscale() {
    if (argbsGreyscale != null)
      return;
    int[] a = new int[argbs.length];
    for (int i = argbs.length; --i >= Graphics3D.SPECIAL_COLIX_MAX;)
      a[i] = ColorUtil.calcGreyscaleRgbFromRgb(argbs[i]);
    argbsGreyscale = a;
  }

  final static int getArgb(short colix) {
    return argbs[colix & Graphics3D.OPAQUE_MASK];
  }

  final static int getArgbGreyscale(short colix) {
    if (argbsGreyscale == null)
      calcArgbsGreyscale();
    return argbsGreyscale[colix & Graphics3D.OPAQUE_MASK];
  }

  final static int[] getShades(int argb, boolean asGrey) {
    if (asGrey) {
      if (argbsGreyscale == null)
        calcArgbsGreyscale();
      argbsGreyscale[Graphics3D.LAST_AVAILABLE_COLIX] = ColorUtil.calcGreyscaleRgbFromRgb(argb);
    }
    return ashades[Graphics3D.LAST_AVAILABLE_COLIX] = Shader.getShades(argb, false);
  }

  final static int[] getShades(short colix) {
    colix &= Graphics3D.OPAQUE_MASK;
    int[] shades = ashades[colix];
    if (shades == null)
      shades = ashades[colix] = Shader.getShades(argbs[colix], false);
    return shades;
  }

  final static int[] getShadesGreyscale(short colix) {
    colix &= Graphics3D.OPAQUE_MASK;
    if (ashadesGreyscale == null)
      ashadesGreyscale = new int[ashades.length][];
    int[] shadesGreyscale = ashadesGreyscale[colix];
    if (shadesGreyscale == null)
      shadesGreyscale = ashadesGreyscale[colix] =
        Shader.getShades(argbs[colix], true);
    return shadesGreyscale;
  }

  final synchronized static void flushShades() {
    for (int i = colixMax; --i >= 0; )
      ashades[i] = null;
    Shader.sphereShadingCalculated = false;
  }

  /*
  Int2IntHash hashMix2 = new Int2IntHash(32);

  short getColixMix(short colixA, short colixB) {
    if (colixA == colixB)
      return colixA;
    if (colixA <= 0)
      return colixB;
    if (colixB <= 0)
      return colixA;
    int translucentMask = colixA & colixB & Graphics3D.TRANSLUCENT_MASK;
    colixA &= ~Graphics3D.TRANSLUCENT_MASK;
    colixB &= ~Graphics3D.TRANSLUCENT_MASK;
    int mixId = ((colixA < colixB)
                 ? ((colixA << 16) | colixB)
                 : ((colixB << 16) | colixA));
    int mixed = hashMix2.get(mixId);
    if (mixed == Integer.MIN_VALUE) {
      int argbA = argbs[colixA];
      int argbB = argbs[colixB];
      int r = (((argbA & 0x00FF0000)+(argbB & 0x00FF0000)) >> 1) & 0x00FF0000;
      int g = (((argbA & 0x0000FF00)+(argbB & 0x0000FF00)) >> 1) & 0x0000FF00;
      int b = (((argbA & 0x000000FF)+(argbB & 0x000000FF)) >> 1);
      int argbMixed = 0xFF000000 | r | g | b;
      mixed = getColix(argbMixed);
      hashMix2.put(mixId, mixed);
    }
    return (short)(mixed | translucentMask);
  }
  */
  
  final static int[] predefinedArgbs = {
    0xFF000000, // black
    0xFFFFA500, // orange
    0xFFFFC0CB, // pink
    0xFF0000FF, // blue
    0xFFFFFFFF, // white
    0xFF00FFFF, // cyan
    0xFFFF0000, // red
    0xFF008000, // green -- really!
    0xFF808080, // gray
    0xFFC0C0C0, // silver
    0xFF00FF00, // lime  -- no kidding!
    0xFF800000, // maroon
    0xFF000080, // navy
    0xFF808000, // olive
    0xFF800080, // purple
    0xFF008080, // teal
    0xFFFF00FF, // magenta
    0xFFFFFF00, // yellow
    0xFFFF69B4, // hotpink
    0xFFFFD700, // gold
  };

  static {
    for (int i = 0; i < predefinedArgbs.length; ++i)
      getColix(predefinedArgbs[i]);
  }

}
