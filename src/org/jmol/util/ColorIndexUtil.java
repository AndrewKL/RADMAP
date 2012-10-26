package org.jmol.util;

import org.jmol.g3d.Graphics3D;
import org.jmol.viewer.JmolConstants;

public class ColorIndexUtil {
  
  /*A set of quick easy static functions for generating colorindexs for Jmol to use. 
   *  mostly used by isosurfacePES and FlexSurface. piflered from the JMOL internal 
   *  implementation.
   *  
   *  -Andrew
   *  
   *  words for the wise colix=color index
   * 
   */
  
  public static final String[] COLOR_SCHEME_LIST = {
    "ROYGB", 
    "BGYOR",
    "RWB",
    "BWR",
    "BW",
    "WB",
    "BWZebra"};
  
  private final static int GRAY = 0xFF808080;
  public final static int ROYGB = 0;
  public final static int BGYOR = 1;
  public final static int RWB   = 6;
  public final static int BWR   = 7;
  public final static int BW  = 10;
  public final static int WB  = 11;
  public final static int BWZebra  = 12;

  

  public static short getColorIndexFromPalette(float val, float lo,
                                        float hi, int palette,
                                        boolean isTranslucent) {
    short colix = Graphics3D.getColix(getArgbFromPalette(val, lo, hi, palette));
    if (isTranslucent) {
      float f = (hi - val) / (hi - lo); 
      if (f > 1)
        f = 1; // transparent
      else if (f < 0.125f) // never fully opaque
        f = 0.125f;
      colix = Graphics3D.getColixTranslucent(colix, true, f);
    }
    return colix;
    
  }
  
  public static int getArgbFromPalette(float val, float lo, float hi, int palette) {
    if (Float.isNaN(val))
      return GRAY;
    int n = getPaletteColorCount(palette);
    switch (palette) {
    
    case BW:
      return JmolConstants.argbsBWScale[quantize(val, lo, hi, n)];
    case WB:
      return JmolConstants.argbsBWScale[quantize(-val, -lo, -hi, n)];
    case ROYGB:
      return JmolConstants.argbsRoygbScale[quantize(val, lo, hi, n)];
    case BGYOR:
      return JmolConstants.argbsRoygbScale[quantize(-val, -hi, -lo, n)];
    
    case RWB:
      return JmolConstants.argbsRwbScale[quantize(val, lo, hi, n)];
    case BWR:
      return JmolConstants.argbsRwbScale[quantize(-val, -hi, -lo, n)];
    case BWZebra:
      return JmolConstants.argbsBWZebraScale[quantize(-val, -hi, -lo, n)];
    
    default:
      return GRAY;
    }
  }
  
  
  
  public static void main(String[] args){
    //used to generate the argbsBWScale
    System.out.println("generating argbsBWScale");
    int[] b = new int[JmolConstants.argbsRoygbScale.length];
    for (int i = 0; i < b.length; i++) {
      float xff = (1f / b.length * i); 
      b[i] = ColorUtil.colorTriadToInt(xff, xff, xff);
    }
    
    System.out.println("public final static int[] argbsBWScale = {");
    for(int i=0;i<b.length;i++){
      System.out.println(","+b[i]);
      
    }
  }
  
  public static int getPaletteColorCount(int palette) {
    switch (palette) {
    
    case BW:
    case WB:
      
      return JmolConstants.argbsBWScale.length;
    case ROYGB:
    case BGYOR:
      return JmolConstants.argbsRoygbScale.length;
    
    case RWB:
    case BWR:
      return JmolConstants.argbsRwbScale.length;
    case BWZebra:
      return JmolConstants.argbsBWZebraScale.length;
    
    default:
      return 0;
    }
  }

  public final static int quantize(float val, float lo, float hi, int segmentCount) {
    /* oy! Say you have an array with 10 values, so segmentCount=10
     * then we expect 0,1,2,...,9  EVENLY
     * If f = fractional distance from lo to hi, say 0.0 to 10.0 again,
     * then one might expect 10 even placements. BUT:
     * (int) (f * segmentCount + 0.5) gives
     * 
     * 0.0 ---> 0
     * 0.5 ---> 1
     * 1.0 ---> 1
     * 1.5 ---> 2
     * 2.0 ---> 2
     * ...
     * 8.5 ---> 9
     * 9.0 ---> 9
     * 9.5 ---> 10 --> 9
     * 
     * so the first bin is underloaded, and the last bin is overloaded.
     * With integer quantities, one would not notice this, because
     * 0, 1, 2, 3, .... --> 0, 1, 2, 3, .....
     * 
     * but with fractional quantities, it will be noticeable.
     * 
     * What we really want is:
     * 
     * 0.0 ---> 0
     * 0.5 ---> 0
     * 1.0 ---> 1
     * 1.5 ---> 1
     * 2.0 ---> 2
     * ...
     * 8.5 ---> 8
     * 9.0 ---> 9
     * 9.5 ---> 9
     * 
     * that is, no addition of 0.5. 
     * Instead, I add 0.0001, just for discreteness sake.
     * 
     * Bob Hanson, 5/2006
     * 
     */
    float range = hi - lo;
    if (range <= 0 || Float.isNaN(val))
      return segmentCount / 2;
    float t = val - lo;
    if (t <= 0)
      return 0;
    float quanta = range / segmentCount;
    int q = (int)(t / quanta + 0.0001f);  //was 0.5f!
    if (q >= segmentCount)
      q = segmentCount - 1;
    return q;
  
  }
  
  public static int colorSchemeNameToInt(String name){
    if(name.equals("ROYGB")) return ColorIndexUtil.ROYGB;
    else if(name.equals("BGYOR")) return ColorIndexUtil.BGYOR;
    else if(name.equals("RWB")) return ColorIndexUtil.RWB;
    else if(name.equals("BWR")) return ColorIndexUtil.BWR;
    else if(name.equals("BW")) return ColorIndexUtil.BW;
    else if(name.equals("WB")) return ColorIndexUtil.WB;
    else if(name.equals("BWZebra")) return ColorIndexUtil.BWZebra;
    
    else return 0;
  }
           

}
