/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-04-05 09:09:32 -0300 (Thu, 05 Apr 2012) $
 * $Revision: 16972 $
 *
 * Copyright (C) 2002-2005  The Jmol Development Team
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
package org.openscience.jmol.app.jmolpanel;

import java.util.Hashtable;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JCheckBox;

import org.jmol.console.KeyJMenu;
import org.jmol.console.KeyJMenuItem;
import org.jmol.console.KeyJCheckBox;
import org.jmol.console.KeyJCheckBoxMenuItem;
import org.jmol.console.KeyJRadioButtonMenuItem;
import org.jmol.i18n.GT;

class GuiMap {

  Map<String, AbstractButton> map = new Hashtable<String, AbstractButton>();
  
  Map<String, String> labels;
  
  // keys here refer to keys listed in org.openscience.jmol.Properties.Jmol-resources.properties
  // actions are either defined there, as xxxScript=, or by 
  // Actions created in DisplayPanel.java
  
  private void setupLabels() {
    labels = new Hashtable<String, String>();
    labels.put("macros", GT.$("&Macros"));
    labels.put("file", GT.$("&File"));
    labels.put("newwin", GT.$("&New"));
    labels.put("open", GT.$("&Open"));
    labels.put("openTip", GT.$("Open a file."));
    labels.put("openurl", GT.$("Open &URL"));
    labels.put("openpdb", GT.$("&Get PDB"));
    labels.put("openmol", GT.$("Get &MOL"));
    labels.put("reloadScript", GT.$("&Reload"));
    
    labels.put("openJSpecViewScript", "JSpecView");
    
    labels.put("editor", GT.$("Scrip&t Editor..."));  // new %t 11.7.45
    labels.put("console", GT.$("Conso&le..."));
    labels.put("jconsole", GT.$("Jmol Java &Console"));
    labels.put("atomsetchooser", GT.$("AtomSet&Chooser..."));
    labels.put("saveas", GT.$("&Save As..."));
    labels.put("exportMenu", GT.$("&Export"));
    labels.put("export", GT.$("Export &Image..."));
    labels.put("exportTip", GT.$("Save current view as an image."));
    labels.put("toweb", GT.$("Export to &Web Page..."));
    labels.put("towebTip", GT.$("Export one or more views to a web page."));
    labels.put("povray", GT.$("Render in POV-&Ray..."));
    labels.put("povrayTip", GT.$("Render in POV-Ray"));
    labels.put("write", GT.$("Write &State..."));
    labels.put("writeTip", GT.$("Save current view as a Jmol state script."));
    labels.put("print", GT.$("&Print..."));
    labels.put("printTip", GT.$("Print view."));
    labels.put("close", GT.$("&Close"));
    labels.put("exit", GT.$("E&xit"));
    labels.put("recentFiles", GT.$("Recent &Files..."));
    labels.put("edit", GT.$("&Edit"));
    // labels.put("makecrystal", GT. $("Make crystal..."));
    labels.put("selectall", GT.$("Select &All"));
    labels.put("deselectall", GT.$("Deselect All"));
    labels.put("copyImage", GT.$("Copy &Image"));
    labels.put("copyScript", GT.$("Copy Script"));
    labels.put("prefs", GT.$("Pr&eferences..."));
    labels.put("pasteClipboard", GT.$("&Paste"));
    labels.put("editSelectAllScript", GT.$("Select &All"));
    labels.put("selectMenu", GT.$("&Select"));
    labels.put("selectMenuText", GT.$("&Select"));
    labels.put("selectAllScript", GT.$("&All"));
    labels.put("selectNoneScript", GT.$("&None"));
    labels.put("selectHydrogenScript", GT.$("Hydrogen"));
    labels.put("selectCarbonScript", GT.$("Carbon"));
    labels.put("selectNitrogenScript", GT.$("Nitrogen"));
    labels.put("selectOxygenScript", GT.$("Oxygen"));
    labels.put("selectPhosphorusScript", GT.$("Phosphorus"));
    labels.put("selectSulfurScript", GT.$("Sulfur"));
    labels.put("selectAminoScript", GT.$("Amino"));
    labels.put("selectNucleicScript", GT.$("Nucleic"));
    labels.put("selectWaterScript", GT.$("Water"));
    labels.put("selectHeteroScript", GT.$("Hetero"));
    labels.put("display", GT.$("&Display"));
    labels.put("atomMenu", GT.$("&Atom"));
    labels.put("atomNoneScript", GT.$("&None"));
    labels.put("atom15Script", GT.$("{0}% van der Waals", "15"));
    labels.put("atom20Script", GT.$("{0}% van der Waals", "20"));
    labels.put("atom25Script", GT.$("{0}% van der Waals", "25"));
    labels.put("atom100Script", GT.$("{0}% van der Waals", "100"));
    labels.put("bondMenu", GT.$("&Bond"));
    labels.put("bondNoneScript", GT.$("&None"));
    labels.put("bondWireframeScript", GT.$("&Wireframe"));
    labels.put("bond100Script", GT.$("{0} \u00C5", "0.10"));
    labels.put("bond150Script", GT.$("{0} \u00C5", "0.15"));
    labels.put("bond200Script", GT.$("{0} \u00C5", "0.20"));
    labels.put("labelMenu", GT.$("&Label"));
    labels.put("labelNoneScript", GT.$("&None"));
    labels.put("labelSymbolScript", GT.$("&Symbol"));
    labels.put("labelNameScript", GT.$("&Name"));
    labels.put("labelNumberScript", GT.$("&Number"));
    labels.put("labelCenteredScript", GT.$("&Centered"));
    labels.put("labelUpperRightScript", GT.$("&Upper right"));
    labels.put("vectorMenu", GT.$("&Vector"));
    labels.put("vectorOffScript", GT.$("&None"));
    labels.put("vectorOnScript", GT.$("&On"));
    labels.put("vector3Script", GT.$("{0} pixels", "3"));
    labels.put("vector005Script", GT.$("{0} \u00C5", "0.05"));
    labels.put("vector01Script", GT.$("{0} \u00C5", "0.1"));
    labels.put("vectorScale02Script", GT.$("Scale {0}", "0.2"));
    labels.put("vectorScale05Script", GT.$("Scale {0}", "0.5"));
    labels.put("vectorScale1Script", GT.$("Scale {0}", "1"));
    labels.put("vectorScale2Script", GT.$("Scale {0}", "2"));
    labels.put("vectorScale5Script", GT.$("Scale {0}", "5"));
    labels.put("zoomMenu", GT.$("&Zoom"));
    labels.put("zoom100Script", GT.$("{0}%", "100"));
    labels.put("zoom150Script", GT.$("{0}%", "150"));
    labels.put("zoom200Script", GT.$("{0}%", "200"));
    labels.put("zoom400Script", GT.$("{0}%", "400"));
    labels.put("zoom800Script", GT.$("{0}%", "800"));
    labels.put("perspectiveCheck", GT.$("&Perspective Depth"));
    labels.put("axesCheck", GT.$("A&xes"));
    labels.put("boundboxCheck", GT.$("B&ounding Box"));
    labels.put("hydrogensCheck", GT.$("&Hydrogens"));
    labels.put("vectorsCheck", GT.$("V&ectors"));
    labels.put("measurementsCheck", GT.$("&Measurements"));
    labels.put("resize", GT.$("Resi&ze"));
    labels.put("view", GT.$("&View"));
    labels.put("front", GT.$("&Front"));
    labels.put("top", GT.$("&Top"));
    labels.put("bottom", GT.$("&Bottom"));
    labels.put("right", GT.$("&Right"));
    labels.put("left", GT.$("&Left"));
    labels.put("transform", GT.$("Tr&ansform..."));
    labels.put("definecenter", GT.$("Define &Center"));
    labels.put("tools", GT.$("&Tools"));
    labels.put("Calculations", GT.$("&Calculations"));
    labels.put("gauss", GT.$("&Gaussian..."));
    labels.put("isosurfacePES", GT.$("&isosurfacePES"));
    labels.put("FlexSurface", GT.$("&FlexSurface"));    
    labels.put("viewMeasurementTable", GT.$("&Measurements") + "...");
    labels.put("distanceUnitsMenu", GT.$("Distance &Units"));
    labels.put("distanceNanometersScript", GT.$("&Nanometers 1E-9"));
    labels.put("distanceAngstromsScript", GT.$("&Angstroms 1E-10"));
    labels.put("distancePicometersScript", GT.$("&Picometers 1E-12"));
    labels.put("animateMenu", GT.$("&Animate..."));
    labels.put("vibrateMenu", GT.$("&Vibrate..."));
    labels.put("graph", GT.$("&Graph..."));
    labels.put("chemicalShifts", GT.$("Calculate chemical &shifts..."));
    labels.put("crystprop", GT.$("&Crystal Properties"));
    labels.put("animateOnceScript", GT.$("&Once"));
    labels.put("animateLoopScript", GT.$("&Loop"));
    labels.put("animatePalindromeScript", GT.$("P&alindrome"));
    labels.put("animateStopScript", GT.$("&Stop animation"));
    labels.put("animateRewindScript", GT.$("&Rewind to first frame"));
    labels.put("animateRewindScriptTip", GT.$("Rewind to first frame"));
    labels.put("animateNextScript", GT.$("Go to &next frame"));
    labels.put("animateNextScriptTip", GT.$("Go to next frame"));
    labels.put("animatePrevScript", GT.$("Go to &previous frame"));
    labels.put("animatePrevScriptTip", GT.$("Go to previous frame"));
    labels.put("animateAllScript", GT.$("All &frames"));
    labels.put("animateAllScriptTip", GT.$("All frames"));
    labels.put("animateLastScript", GT.$("Go to &last frame"));
    labels.put("animateLastScriptTip", GT.$("Go to last frame"));
    labels.put("vibrateStartScript", GT.$("Start &vibration"));
    labels.put("vibrateStopScript", GT.$("&Stop vibration"));
    labels.put("vibrateRewindScript", GT.$("&First frequency"));
    labels.put("vibrateNextScript", GT.$("&Next frequency"));
    labels.put("vibratePrevScript", GT.$("&Previous frequency"));
    labels.put("surfaceTool", GT.$("SurfaceTool..."));
    labels.put("surfaceToolTip", GT.$("Control Display of Surfaces"));
    labels.put("help", GT.$("&Help"));
    labels.put("about", GT.$("About Jmol"));
    labels.put("uguide", GT.$("User Guide"));
    labels.put("whatsnew", GT.$("What's New"));
    labels.put("Prefs.showHydrogens", GT.$("Hydrogens"));
    labels.put("Prefs.showMeasurements", GT.$("Measurements"));
    labels.put("Prefs.perspectiveDepth", GT.$("Perspective Depth"));
    labels.put("Prefs.showAxes", GT.$("Axes"));
    labels.put("Prefs.showBoundingBox", GT.$("Bounding Box"));
    labels.put("Prefs.axesOrientationRasmol", GT
        .$("RasMol/Chime compatible axes orientation/rotations"));
    labels.put("Prefs.openFilePreview", GT
        .$("File Preview (requires restarting Jmol)"));
    labels.put("Prefs.clearHistory", GT
        .$("Clear history (requires restarting Jmol)"));
    labels.put("Prefs.isLabelAtomColor", GT.$("Use Atom Color"));
    labels.put("Prefs.isBondAtomColor", GT.$("Use Atom Color"));
    labels.put("rotateScriptTip", GT.$("Rotate molecule."));
    labels.put("pickScriptTip", GT
        .$("Select a set of atoms using SHIFT-LEFT-DRAG."));
    labels.put("pickMeasureScriptTip", GT
        .$("Click atoms to measure distances"));
    labels.put("pickCenterScriptTip", GT
        .$("Click an atom to center on it"));
    labels.put("homeTip", GT.$("Return molecule to home position."));
    labels.put("modelkitScriptTip", GT.$("Open the model kit."));
    labels.put("JavaConsole.clear", GT.$("Clear"));
  }

  String getLabel(String key) {
    if (labels == null)
      setupLabels();
    String label = labels.get(key);
    return label;
  }

  JMenu newJMenu(String key) {
    return new KeyJMenu(key, getLabel(key), map);
  }
  
  JMenuItem newJMenuItem(String key) {
    return new KeyJMenuItem(key, getLabel(key), map);
  }
  JCheckBoxMenuItem newJCheckBoxMenuItem(String key, boolean isChecked) {
    return new KeyJCheckBoxMenuItem(key, getLabel(key), map, isChecked);
  }
  JRadioButtonMenuItem newJRadioButtonMenuItem(String key) {
    return new KeyJRadioButtonMenuItem(key, getLabel(key), map);
  }
  JCheckBox newJCheckBox(String key, boolean isChecked) {
    return new KeyJCheckBox(key, getLabel(key), map, isChecked);
  }
  JButton newJButton(String key) {
    JButton jb = new JButton(getLabel(key));
    map.put(key, jb);
    return jb;
  }

  Object get(String key) {
    return map.get(key);
  }

  void setSelected(String key, boolean b) {
    ((AbstractButton)get(key)).setSelected(b);
  }

  void setEnabled(String key, boolean b) {
    ((AbstractButton)get(key)).setEnabled(b);
  }

  public void updateLabels() {
    boolean doTranslate = GT.getDoTranslate();
    GT.setDoTranslate(true);
    setupLabels();
    KeyJMenuItem.setAbstractButtonLabels(map, labels);
    GT.setDoTranslate(doTranslate);
  }


}

