package org.jmol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jmol.g3d.Graphics3D;
import org.jmol.viewer.JmolConstants;
import org.junit.jupiter.api.Test;

class ColorIndexUtilTest {

  private static final int GRAY = 0xFF808080;

  @Test
  void quantizeSplitsTheRangeIntoEvenBins() {
    assertEquals(0, ColorIndexUtil.quantize(0f, 0f, 10f, 10));
    assertEquals(0, ColorIndexUtil.quantize(0.5f, 0f, 10f, 10));
    assertEquals(1, ColorIndexUtil.quantize(1f, 0f, 10f, 10));
    assertEquals(8, ColorIndexUtil.quantize(8.5f, 0f, 10f, 10));
    assertEquals(9, ColorIndexUtil.quantize(9.5f, 0f, 10f, 10));
  }

  @Test
  void quantizeClampsValuesOutsideTheRange() {
    assertEquals(0, ColorIndexUtil.quantize(-5f, 0f, 10f, 10));
    assertEquals(9, ColorIndexUtil.quantize(10f, 0f, 10f, 10));
    assertEquals(9, ColorIndexUtil.quantize(50f, 0f, 10f, 10));
  }

  @Test
  void quantizeUsesTheMiddleBinWhenThereIsNoRange() {
    assertEquals(5, ColorIndexUtil.quantize(3f, 7f, 7f, 10));
    assertEquals(5, ColorIndexUtil.quantize(3f, 10f, 0f, 10));
    assertEquals(5, ColorIndexUtil.quantize(Float.NaN, 0f, 10f, 10));
  }

  @Test
  void everySchemeNameMapsToItsConstant() {
    int[] expected = { ColorIndexUtil.ROYGB, ColorIndexUtil.BGYOR,
        ColorIndexUtil.RWB, ColorIndexUtil.BWR, ColorIndexUtil.BW,
        ColorIndexUtil.WB, ColorIndexUtil.BWZebra };
    assertEquals(expected.length, ColorIndexUtil.COLOR_SCHEME_LIST.length);
    for (int i = 0; i < expected.length; i++)
      assertEquals(expected[i], ColorIndexUtil
          .colorSchemeNameToInt(ColorIndexUtil.COLOR_SCHEME_LIST[i]));
  }

  @Test
  void unknownSchemeNameFallsBackToRoygb() {
    assertEquals(ColorIndexUtil.ROYGB, ColorIndexUtil
        .colorSchemeNameToInt("nope"));
  }

  @Test
  void everyListedSchemeHasAPalette() {
    for (String name : ColorIndexUtil.COLOR_SCHEME_LIST)
      assertTrue(ColorIndexUtil.getPaletteColorCount(ColorIndexUtil
          .colorSchemeNameToInt(name)) > 0, name);
    assertEquals(0, ColorIndexUtil.getPaletteColorCount(99));
  }

  @Test
  void paletteEndsMapToTheEndsOfTheScale() {
    int[] roygb = JmolConstants.argbsRoygbScale;
    assertEquals(roygb[0], ColorIndexUtil.getArgbFromPalette(0f, 0f, 1f,
        ColorIndexUtil.ROYGB));
    assertEquals(roygb[roygb.length - 1], ColorIndexUtil.getArgbFromPalette(
        1f, 0f, 1f, ColorIndexUtil.ROYGB));

    int[] rwb = JmolConstants.argbsRwbScale;
    assertEquals(rwb[0], ColorIndexUtil.getArgbFromPalette(0f, 0f, 1f,
        ColorIndexUtil.RWB));
    assertEquals(rwb[rwb.length - 1], ColorIndexUtil.getArgbFromPalette(1f,
        0f, 1f, ColorIndexUtil.RWB));
  }

  @Test
  void reversedSchemesRunTheOtherWay() {
    int[] roygb = JmolConstants.argbsRoygbScale;
    assertEquals(roygb[roygb.length - 1], ColorIndexUtil.getArgbFromPalette(
        0f, 0f, 1f, ColorIndexUtil.BGYOR));
    assertEquals(roygb[0], ColorIndexUtil.getArgbFromPalette(1f, 0f, 1f,
        ColorIndexUtil.BGYOR));

    int[] rwb = JmolConstants.argbsRwbScale;
    assertEquals(rwb[rwb.length - 1], ColorIndexUtil.getArgbFromPalette(0f,
        0f, 1f, ColorIndexUtil.BWR));
  }

  @Test
  void nanAndUnknownPalettesAreGray() {
    assertEquals(GRAY, ColorIndexUtil.getArgbFromPalette(Float.NaN, 0f, 1f,
        ColorIndexUtil.ROYGB));
    assertEquals(GRAY, ColorIndexUtil.getArgbFromPalette(0.5f, 0f, 1f, 99));
  }

  @Test
  void colixMatchesThePaletteColour() {
    short colix = ColorIndexUtil.getColorIndexFromPalette(0.25f, 0f, 1f,
        ColorIndexUtil.ROYGB, false);
    assertEquals(Graphics3D.getColix(ColorIndexUtil.getArgbFromPalette(0.25f,
        0f, 1f, ColorIndexUtil.ROYGB)), colix);
  }

  @Test
  void translucentColixKeepsTheColourButIsMarkedTranslucent() {
    short opaque = ColorIndexUtil.getColorIndexFromPalette(0.25f, 0f, 1f,
        ColorIndexUtil.ROYGB, false);
    short translucent = ColorIndexUtil.getColorIndexFromPalette(0.25f, 0f,
        1f, ColorIndexUtil.ROYGB, true);

    assertNotEquals(opaque, translucent);
    assertTrue(Graphics3D.isColixTranslucent(translucent));
    assertEquals(Graphics3D.getArgb(opaque), Graphics3D.getArgb(translucent));
  }
}
