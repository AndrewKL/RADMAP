/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2008-04-26 01:52:03 -0500 (Sat, 26 Apr 2008) $
 * $Revision: 9314 $
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

package org.jmol.util;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


public class Quadric {

  public float[] lengths;
  public Vector3f[] vectors;
  public boolean isThermalEllipsoid = true;
  public float scale = 1;
  
  @Override
  public String toString() {
    return (vectors == null ? "" + lengths[0] : 
      vectors[0] + "\t" + lengths[0] + "\n"
      + vectors[1] + "\t" + lengths[1] + "\n"
      + vectors[2] + "\t" + lengths[2] + "\n");
  }
  
  public Quadric(Vector3f[] vectors, float[] lengths, boolean isThermal) {
    this.vectors = vectors;
    this.lengths = lengths;
    isThermalEllipsoid = isThermal;
  }
  
  //////////  Ellipsoid Code ///////////
  //
  // Bob Hanson, 4/2008
  //
  // several useful methods designed for Jmol
  //
  // but of potentially many uses
  //
  //////////////////////////////////////

  private static float ONE_OVER_ROOT2_PI = (float) (Math.sqrt(0.5) / Math.PI);
  public Quadric(double[] bcart) {
    isThermalEllipsoid = true;
    lengths = new float[3];
    vectors = new Vector3f[3];
    getAxesForEllipsoid(bcart, vectors, lengths);

    for (int i = 0; i < 3; i++)
      lengths[i] *= ONE_OVER_ROOT2_PI;
  }

  public void rotate(Matrix4f mat) {
    if (vectors != null)
    for (int i = 0; i < 3; i++)
      mat.transform(vectors[i]);
  }
  
  public void setSize(int size) {
    scale = (isThermalEllipsoid ? Quadric.getRadius(size) : size < 1 ? 0 : size / 100.0f);
  }

  public static void getAxesForEllipsoid(double[] coef, Vector3f[] unitVectors, float[] lengths) {
    
    // assumes an ellipsoid centered on 0,0,0
    // called by UnitCell for the initial creation of Object[] ellipsoid
    
    double[][] mat = new double[3][3];
    mat[0][0] = coef[0]; //XX
    mat[1][1] = coef[1]; //YY
    mat[2][2] = coef[2]; //ZZ
    mat[0][1] = mat[1][0] = coef[3] / 2; //XY
    mat[0][2] = mat[2][0] = coef[4] / 2; //XZ
    mat[1][2] = mat[2][1] = coef[5] / 2; //YZ
    new Eigen(mat, unitVectors, lengths);
  }

  public static Matrix3f setEllipsoidMatrix(Vector3f[] unitAxes, float[] lengths, Vector3f vTemp, Matrix3f mat) {
    /*
     * Create a matrix that transforms cartesian coordinates
     * into ellipsoidal coordinates, where in that system we 
     * are drawing a sphere. 
     *
     */
    
    for (int i = 0; i < 3; i++) {
      vTemp.set(unitAxes[i]);
      vTemp.scale(lengths[i]);
      mat.setColumn(i, vTemp);
    }
    mat.invert(mat);
    return mat;
  }

  public static void getEquationForQuadricWithCenter(float x, float y, float z, Matrix3f mToElliptical, 
                                             Vector3f vTemp, Matrix3f mTemp, double[] coef, Matrix4f mDeriv) {
    /* Starting with a center point and a matrix that converts cartesian 
     * or screen coordinates to ellipsoidal coordinates, 
     * this method fills a float[10] with the terms for the 
     * equation for the ellipsoid:
     * 
     * c0 x^2 + c1 y^2 + c2 z^2 + c3 xy + c4 xz + c5 yz + c6 x + c7 y + c8 z - 1 = 0 
     * 
     * I made this up; I haven't seen it in print. -- Bob Hanson, 4/2008
     * 
     */
    
    vTemp.set(x, y, z);
    mToElliptical.transform(vTemp);
    double f = 1 - vTemp.dot(vTemp); // J
    mTemp.transpose(mToElliptical);
    mTemp.transform(vTemp);
    mTemp.mul(mToElliptical);
    coef[0] = mTemp.m00 / f;     // A = aXX
    coef[1] = mTemp.m11 / f;     // B = aYY
    coef[2] = mTemp.m22 / f;     // C = aZZ
    coef[3] = mTemp.m01 * 2 / f; // D = aXY
    coef[4] = mTemp.m02 * 2 / f; // E = aXZ
    coef[5] = mTemp.m12 * 2 / f; // F = aYZ
    coef[6] = -2 * vTemp.x / f;  // G = aX
    coef[7] = -2 * vTemp.y / f;  // H = aY
    coef[8] = -2 * vTemp.z / f;  // I = aZ
    coef[9] = -1;                // J = -1
    
    /*
     * f = Ax^2 + By^2 + Cz^2 + Dxy + Exz + Fyz + Gx + Hy + Iz + J
     * df/dx = 2Ax +  Dy +  Ez + G
     * df/dy =  Dx + 2By +  Fz + H
     * df/dz =  Ex +  Fy + 2Cz + I
     */
    
    if (mDeriv == null)
      return;
    mDeriv.setIdentity();
    mDeriv.m00 = (float) (2 * coef[0]);
    mDeriv.m11 = (float) (2 * coef[1]);
    mDeriv.m22 = (float) (2 * coef[2]);
  
    mDeriv.m01 = mDeriv.m10 = (float) coef[3];
    mDeriv.m02 = mDeriv.m20 = (float) coef[4];
    mDeriv.m12 = mDeriv.m21 = (float) coef[5];
  
    mDeriv.m03 = (float) coef[6];
    mDeriv.m13 = (float) coef[7];
    mDeriv.m23 = (float) coef[8];
  }

  public static boolean getQuardricZ(double x, double y, 
                                   double[] coef, double[] zroot) {
    
    /* simple quadratic formula for:
     * 
     * c0 x^2 + c1 y^2 + c2 z^2 + c3 xy + c4 xz + c5 yz + c6 x + c7 y + c8 z - 1 = 0 
     * 
     * or:
     * 
     * c2 z^2 + (c4 x + c5 y + c8)z + (c0 x^2 + c1 y^2 + c3 xy + c6 x + c7 y - 1) = 0
     * 
     * so:
     * 
     *  z = -(b/2a) +/- sqrt( (b/2a)^2 - c/a )
     */
    
    double b_2a = (coef[4] * x + coef[5] * y + coef[8]) / coef[2] / 2;
    double c_a = (coef[0] * x * x + coef[1] * y * y + coef[3] * x * y 
        + coef[6] * x + coef[7] * y - 1) / coef[2];
    double f = b_2a * b_2a - c_a;
    if (f < 0)
      return false;
    f = Math.sqrt(f);
    zroot[0] = (-b_2a - f);
    zroot[1] = (-b_2a + f);
    return true;
  }

  public static int getOctant(Point3f pt) {
    int i = 0;
    if (pt.x < 0)
      i += 1;
    if (pt.y < 0)
      i += 2;
    if (pt.z < 0)
      i += 4;
    return i;
  }

  // from ORTEP manual ftp://ftp.ornl.gov/pub/ortep/man/pdf/chap6.pdf
  
  private static float[] crtval = new float[] {
    0.3389f, 0.4299f, 0.4951f, 0.5479f, 0.5932f, 0.6334f, 0.6699f, 0.7035f,
    0.7349f, 0.7644f, 0.7924f, 0.8192f, 0.8447f, 0.8694f, 0.8932f, 0.9162f,
    0.9386f, 0.9605f, 0.9818f, 1.0026f, 1.0230f, 1.0430f, 1.0627f, 1.0821f,
    1.1012f, 1.1200f, 1.1386f, 1.1570f, 1.1751f, 1.1932f, 1.2110f, 1.2288f,
    1.2464f, 1.2638f, 1.2812f, 1.2985f, 1.3158f, 1.3330f, 1.3501f, 1.3672f,
    1.3842f, 1.4013f, 1.4183f, 1.4354f, 1.4524f, 1.4695f, 1.4866f, 1.5037f,
    1.5209f, 1.5382f, 1.5555f, 1.5729f, 1.5904f, 1.6080f, 1.6257f, 1.6436f,
    1.6616f, 1.6797f, 1.6980f, 1.7164f, 1.7351f, 1.7540f, 1.7730f, 1.7924f,
    1.8119f, 1.8318f, 1.8519f, 1.8724f, 1.8932f, 1.9144f, 1.9360f, 1.9580f,
    1.9804f, 2.0034f, 2.0269f, 2.0510f, 2.0757f, 2.1012f, 2.1274f, 2.1544f,
    2.1824f, 2.2114f, 2.2416f, 2.2730f, 2.3059f, 2.3404f, 2.3767f, 2.4153f,
    2.4563f, 2.5003f, 2.5478f, 2.5997f, 2.6571f, 2.7216f, 2.7955f, 2.8829f,
    2.9912f, 3.1365f, 3.3682f 
  };
  
  final public static float getRadius(int prob) {
    return crtval[prob < 1 ? 0 : prob > 99 ? 98 : prob - 1];
  }


}
