/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-02-03 00:44:36 -0400 (Fri, 03 Feb 2012) $
 * $Revision: 16724 $
 *
 * Copyright (C) 2000-2005  The Jmol Development Team
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
package org.jmol.popup;

import java.util.Properties;

import org.jmol.i18n.GT;
import org.jmol.util.TextFormat;

class MainPopupResourceBundle extends PopupResource {

  private final static String MENU_NAME = "popupMenu";

  @Override
  public String getMenuName() {
    return MENU_NAME; 
  }
  
  MainPopupResourceBundle(String menuStructure, Properties menuText) {
    super(menuStructure, menuText);
  }

  @Override
  protected void buildStructure(String menuStructure) {
    addItems(menuContents);
    addItems(structureContents);
    setStructure(menuStructure);
  }
    
  private static String Box(String cmd) {
    return "if (showBoundBox or showUnitcell) {"+cmd+"} else {boundbox on;"+cmd+";boundbox off}";
  }

  private static String[][] menuContents = {
    
      {   "@COLOR", "black white red orange yellow green cyan blue indigo violet"},      
      {   "@AXESCOLOR", "gray salmon maroon olive slateblue gold orchid"},
      
      {   MENU_NAME,
          "FRAMESbyModelComputedMenu configurationComputedMenu - selectMenuText viewMenu renderMenu colorMenu - surfaceMenu FILEUNITMenu - "
              + "zoomMenu spinMenu VIBRATIONMenu spectraMenu "
              + "FRAMESanimateMenu - "
              + "measureMenu pickingMenu - showConsole showMenu fileMenu computationMenu - "
              + "languageComputedMenu aboutComputedMenu" },
              
      {   "selectMenuText",
          "hideNotSelectedCB showSelectionsCB - selectAll selectNone invertSelection - elementsComputedMenu SYMMETRYSelectComputedMenu - "
              + "PDBproteinMenu PDBnucleicMenu PDBheteroMenu PDBcarboMenu PDBnoneOfTheAbove" },

      {   "PDBproteinMenu", 
          "PDBaaResiduesComputedMenu - "
              + "allProtein proteinBackbone proteinSideChains - "
              + "polar nonpolar - "
              + "positiveCharge negativeCharge noCharge" },
              
      {   "PDBcarboMenu",
          "PDBcarboResiduesComputedMenu - allCarbo" },

      {   "PDBnucleicMenu",
          "PDBnucleicResiduesComputedMenu - allNucleic nucleicBackbone nucleicBases - DNA RNA - "
              + "atPairs auPairs gcPairs" },
              
      {   "PDBheteroMenu",
          "PDBheteroComputedMenu - allHetero Solvent Water - "
              + "Ligand exceptWater nonWaterSolvent" },

      {   "viewMenu",
          "front left right top bottom back" },

      {   "renderMenu",
          "perspectiveDepthCB showBoundBoxCB showUNITCELLCB showAxesCB stereoMenu - renderSchemeMenu - atomMenu labelMenu bondMenu hbondMenu ssbondMenu - "
              + "PDBstructureMenu [set_axes]Menu [set_boundbox]Menu [set_UNITCELL]Menu" },

      {   "renderSchemeMenu",
          "renderCpkSpacefill renderBallAndStick "
              + "renderSticks renderWireframe PDBrenderCartoonsOnly PDBrenderTraceOnly" },
                            
      {   "atomMenu",
          "showHydrogensCB - atomNone - "
              + "atom15 atom20 atom25 atom50 atom75 atom100" },

      {   "bondMenu",
          "bondNone bondWireframe - "
              + "bond100 bond150 bond200 bond250 bond300" },

      {   "hbondMenu",
          "hbondCalc hbondNone hbondWireframe - "
              + "PDBhbondSidechain PDBhbondBackbone - "
              + "hbond100 hbond150 hbond200 hbond250 hbond300" },

      {   "ssbondMenu",
          "ssbondNone ssbondWireframe - "
              + "PDBssbondSidechain PDBssbondBackbone - "
              + "ssbond100 ssbond150 ssbond200 ssbond250 ssbond300" },

      {   "PDBstructureMenu",
          "structureNone - "
              + "backbone cartoon cartoonRockets ribbons rockets strands trace" },

      {   "VIBRATIONvectorMenu",
          "vectorOff vectorOn vector3 vector005 vector01 - "
              + "vectorScale02 vectorScale05 vectorScale1 vectorScale2 vectorScale5" },

      {   "stereoMenu",
          "stereoNone stereoRedCyan stereoRedBlue stereoRedGreen stereoCrossEyed stereoWallEyed" },

      {   "labelMenu",
          "labelNone - " + "labelSymbol labelName labelNumber - "
              + "labelPositionMenu" },

      {   "labelPositionMenu",
          "labelCentered labelUpperRight labelLowerRight labelUpperLeft labelLowerLeft" },

      {   "colorMenu",
          "colorrasmolCB - [color_atoms]Menu [color_bonds]Menu [color_hbonds]Menu [color_ssbonds]Menu colorPDBStructuresMenu [color_isosurface]Menu"
              + " - [color_labels]Menu [color_vectors]Menu - [color_axes]Menu [color_boundbox]Menu [color_UNITCELL]Menu [color_background]Menu" },

      { "[color_atoms]Menu", "schemeMenu - @COLOR - opaque translucent" },
      { "[color_bonds]Menu", "none - @COLOR - opaque translucent" },
      { "[color_hbonds]Menu", null },
      { "[color_ssbonds]Menu", null },
      { "[color_labels]Menu", null },
      { "[color_vectors]Menu", null },
      { "[color_backbone]Menu", "none - schemeMenu - @COLOR - opaque translucent" },
      { "[color_cartoon]sMenu", null },
      { "[color_ribbon]sMenu", null },
      { "[color_rockets]Menu", null },
      { "[color_strands]Menu", null },
      { "[color_trace]Menu", null },
      { "[color_background]Menu", "@COLOR" },
      { "[color_isosurface]Menu", "@COLOR - opaque translucent" },
      { "[color_axes]Menu", "@AXESCOLOR" },
      { "[color_boundbox]Menu", null },
      { "[color_UNITCELL]Menu", null },


      {   "colorPDBStructuresMenu",
          "[color_backbone]Menu [color_cartoon]sMenu [color_ribbon]sMenu [color_rockets]Menu [color_strands]Menu [color_trace]Menu" },

      {   "schemeMenu",
          "cpk - formalcharge partialcharge#CHARGE - altloc#PDB amino#PDB chain#PDB group#PDB molecule monomer#PDB shapely#PDB structure#PDB relativeTemperature#BFACTORS fixedTemperature#BFACTORS" },

      {   "zoomMenu",
          "zoom50 zoom100 zoom150 zoom200 zoom400 zoom800 - "
              + "zoomIn zoomOut" },

      {   "spinMenu",
          "spinOn spinOff - " + "[set_spin_X]Menu [set_spin_Y]Menu [set_spin_Z]Menu - "
              + "[set_spin_FPS]Menu" },

      {   "VIBRATIONMenu", 
          "vibrationOff vibrationOn VIBRATIONvectorMenu" },

          {   "spectraMenu", 
          "hnmrMenu cnmrMenu" },

      {   "FRAMESanimateMenu",
          "animModeMenu - play pause resume stop - nextframe prevframe rewind - playrev restart - "
              + "FRAMESanimFpsMenu" },

      {   "FRAMESanimFpsMenu", 
          "animfps5 animfps10 animfps20 animfps30 animfps50" },

      {   "measureMenu",
          "showMeasurementsCB - "
              + "measureOff measureDistance measureAngle measureTorsion PDBmeasureSequence - "
              + "measureDelete measureList - distanceNanometers distanceAngstroms distancePicometers" },

      {   "pickingMenu",
          "pickOff pickCenter pickIdent pickLabel pickAtom "
              + "pickMolecule pickElement PDBpickChain PDBpickGroup SYMMETRYpickSite pickSpin" },

      {   "computationMenu",
          "minimize modelkit"
              /* calculateVolume*/ },

              
      {   "showMenu",
          "showHistory showFile showFileHeader - "
              + "showOrient showMeasure - "
              + "showSpacegroup showState SYMMETRYshowSymmetry UNITCELLshow - showIsosurface showMo - extractMOL" },

      {   "fileMenu",
          "SIGNEDloadFileOrUrl SIGNEDloadPdb SIGNEDloadScript - "
              + "reload SIGNEDloadFileUnitCell - "
              + "writeFileTextVARIABLE writeState writeHistory SIGNEDwriteJmol SIGNEDwriteIsosurface - SIGNEDwriteGif SIGNEDwriteJpg SIGNEDwritePng SIGNEDwritePngJmol SIGNEDwritePovray - "
              + "SIGNEDwriteVrml SIGNEDwriteX3d SIGNEDwriteIdtf SIGNEDwriteMaya" },

      { "[set_spin_X]Menu", "s0 s5 s10 s20 s30 s40 s50" },
      { "[set_spin_Y]Menu", null },
      { "[set_spin_Z]Menu", null },
      { "[set_spin_FPS]Menu", null },

      {   "animModeMenu", 
          "onceThrough palindrome loop" },


      {   "surfaceMenu",
          "surfDots surfVDW surfSolventAccessible14 surfSolvent14 surfMolecular CHARGEsurfMEP surfMoComputedMenuText - surfOpaque surfTranslucent surfOff" },

      {   "FILEUNITMenu",
          "SYMMETRYShowComputedMenu SYMMETRYhide FILEMOLload FILEUNITone FILEUNITnine FILEUNITnineRestricted FILEUNITninePoly" },

      {   "[set_axes]Menu", 
          "off#axes dotted - byPixelMenu byAngstromMenu" },

      { "[set_boundbox]Menu", null },
      { "[set_UNITCELL]Menu", null },

      {   "byPixelMenu", 
          "1p 3p 5p 10p" },

      {   "byAngstromMenu", 
          "10a 20a 25a 50a 100a" },

/*
 *       {   "optionsMenu", 
          "rasmolChimeCompatibility" },
*/

/*  this was not working, but now these entries are inserted dynamically into a submenu:
      {   "aboutComputedMenu", 
          "APPLETjmolUrl APPLETmouseManualUrl APPLETtranslationUrl - " },
*/
      {   "aboutComputedMenu", 
          "- " },

  };
  
  
  
  private static String[][] structureContents = {

      { "colorrasmolCB", ""},
      { "hideNotSelectedCB", "set hideNotSelected true | set hideNotSelected false; hide(none)" },
      { "perspectiveDepthCB", ""},
      { "showAxesCB", "set showAxes true | set showAxes false;set axesMolecular" },
      { "showBoundBoxCB", ""},
      { "showHydrogensCB", ""},
      { "showMeasurementsCB", ""},
      { "showSelectionsCB", ""},
      { "showUNITCELLCB", ""},

      { "selectAll", "SELECT all" },
      { "selectNone", "SELECT none" },
      { "invertSelection", "SELECT not selected" },
   
      { "allProtein", "SELECT protein" },
      { "proteinBackbone", "SELECT protein and backbone" },
      { "proteinSideChains", "SELECT protein and not backbone" },
      { "polar", "SELECT protein and polar" },
      { "nonpolar", "SELECT protein and not polar" },
      { "positiveCharge", "SELECT protein and basic" },
      { "negativeCharge", "SELECT protein and acidic" },
      { "noCharge", "SELECT protein and not (acidic,basic)" },
      { "allCarbo", "SELECT carbohydrate" },

      { "allNucleic", "SELECT nucleic" },
      { "DNA", "SELECT dna" },
      { "RNA", "SELECT rna" },
      { "nucleicBackbone", "SELECT nucleic and backbone" },
      { "nucleicBases", "SELECT nucleic and not backbone" },
      { "atPairs", "SELECT a,t" },
      { "gcPairs", "SELECT g,c" },
      { "auPairs", "SELECT a,u" },
      { "A", "SELECT a" },
      { "C", "SELECT c" },
      { "G", "SELECT g" },
      { "T", "SELECT t" },
      { "U", "SELECT u" },

      { "allHetero", "SELECT hetero" },
      { "Solvent", "SELECT solvent" },
      { "Water", "SELECT water" },
      // same as ligand    { "exceptSolvent", "SELECT hetero and not solvent" },
      { "nonWaterSolvent", "SELECT solvent and not water" },
      { "exceptWater", "SELECT hetero and not water" },
      { "Ligand", "SELECT ligand" },

      // not implemented    { "Lipid", "SELECT lipid" },
      { "PDBnoneOfTheAbove", "SELECT not(hetero,protein,nucleic,carbohydrate)" },

      { "front", Box( "moveto 2.0 front;delay 1" ) },
      { "left", Box( "moveto 1.0 front;moveto 2.0 left;delay 1"  ) },
      { "right", Box( "moveto 1.0 front;moveto 2.0 right;delay 1"  ) },
      { "top", Box( "moveto 1.0 front;moveto 2.0 top;delay 1"  ) },
      { "bottom", Box( "moveto 1.0 front;moveto 2.0 bottom;delay 1"  ) },
      { "back", Box( "moveto 1.0 front;moveto 2.0 back;delay 1"  ) },

      { "renderCpkSpacefill", "restrict bonds not selected;select not selected;spacefill 100%;color cpk" },
      { "renderBallAndStick", "restrict bonds not selected;select not selected;spacefill 23%AUTO;wireframe 0.15;color cpk" },
      { "renderSticks", "restrict bonds not selected;select not selected;wireframe 0.3;color cpk" },
      { "renderWireframe", "restrict bonds not selected;select not selected;wireframe on;color cpk" },
      { "PDBrenderCartoonsOnly", "restrict bonds not selected;select not selected;cartoons on;color structure" },
      { "PDBrenderTraceOnly", "restrict bonds not selected;select not selected;trace on;color structure" },

      { "atomNone", "cpk off" },
      { "atom15", "cpk 15%" },
      { "atom20", "cpk 20%" },
      { "atom25", "cpk 25%" },
      { "atom50", "cpk 50%" },
      { "atom75", "cpk 75%" },
      { "atom100", "cpk on" },

      { "bondNone", "wireframe off" },
      { "bondWireframe", "wireframe on" },
      { "bond100", "wireframe .1" },
      { "bond150", "wireframe .15" },
      { "bond200", "wireframe .2" },
      { "bond250", "wireframe .25" },
      { "bond300", "wireframe .3" },

      { "hbondCalc", "hbonds calculate" },
      { "hbondNone", "hbonds off" },
      { "hbondWireframe", "hbonds on" },
      { "PDBhbondSidechain", "set hbonds sidechain" },
      { "PDBhbondBackbone", "set hbonds backbone" },
      { "hbond100", "hbonds .1" },
      { "hbond150", "hbonds .15" },
      { "hbond200", "hbonds .2" },
      { "hbond250", "hbonds .25" },
      { "hbond300", "hbonds .3" },

      { "ssbondNone", "ssbonds off" },
      { "ssbondWireframe", "ssbonds on" },
      { "PDBssbondSidechain", "set ssbonds sidechain" },
      { "PDBssbondBackbone", "set ssbonds backbone" },
      { "ssbond100", "ssbonds .1" },
      { "ssbond150", "ssbonds .15" },
      { "ssbond200", "ssbonds .2" },
      { "ssbond250", "ssbonds .25" },
      { "ssbond300", "ssbonds .3" },

      { "structureNone",
          "backbone off;cartoons off;ribbons off;rockets off;strands off;trace off;" },
      { "backbone", "restrict not selected;select not selected;backbone 0.3" },
      { "cartoon", "restrict not selected;select not selected;set cartoonRockets false;cartoons on" },
      { "cartoonRockets", "restrict not selected;select not selected;set cartoonRockets;cartoons on" },
      { "ribbons", "restrict not selected;select not selected;ribbons on" },
      { "rockets", "restrict not selected;select not selected;rockets on" },
      { "strands", "restrict not selected;select not selected;strands on" },
      { "trace", "restrict not selected;select not selected;trace 0.3" },

      { "vibrationOff", "vibration off" },
      { "vibrationOn", "vibration on" },

      { "vectorOff", "vectors off" },
      { "vectorOn", "vectors on" },
      { "vector3", "vectors 3" },
      { "vector005", "vectors 0.05" },
      { "vector01", "vectors 0.1" },
      { "vectorScale02", "vector scale 0.2" },
      { "vectorScale05", "vector scale 0.5" },
      { "vectorScale1", "vector scale 1" },
      { "vectorScale2", "vector scale 2" },
      { "vectorScale5", "vector scale 5" },

      { "stereoNone", "stereo off" },
      { "stereoRedCyan", "stereo redcyan 3" },
      { "stereoRedBlue", "stereo redblue 3" },
      { "stereoRedGreen", "stereo redgreen 3" },
      { "stereoCrossEyed", "stereo -5" },
      { "stereoWallEyed", "stereo 5" },

      { "labelNone", "label off" },
      { "labelSymbol", "label %e" },
      { "labelName", "label %a" },
      { "labelNumber", "label %i" },

      { "labelCentered", "set labeloffset 0 0" },
      { "labelUpperRight", "set labeloffset 4 4" },
      { "labelLowerRight", "set labeloffset 4 -4" },
      { "labelUpperLeft", "set labeloffset -4 4" },
      { "labelLowerLeft", "set labeloffset -4 -4" },

      { "zoom50", "zoom 50" },
      { "zoom100", "zoom 100" },
      { "zoom150", "zoom 150" },
      { "zoom200", "zoom 200" },
      { "zoom400", "zoom 400" },
      { "zoom800", "zoom 800" },
      { "zoomIn", "move 0 0 0 40 0 0 0 0 1" },
      { "zoomOut", "move 0 0 0 -40 0 0 0 0 1" },

      { "spinOn", "spin on" },
      { "spinOff", "spin off" },

      { "s0", "0" },
      { "s5", "5" },
      { "s10", "10" },
      { "s20", "20" },
      { "s30", "30" },
      { "s40", "40" },
      { "s50", "50" },

      { "onceThrough", "anim mode once#" },
      { "palindrome", "anim mode palindrome#" },
      { "loop", "anim mode loop#" },
      { "play", "anim play#" },
      { "pause", "anim pause#" },
      { "resume", "anim resume#" },
      { "stop", "anim off#" },
      
      { "nextframe", "frame next#" },
      { "prevframe", "frame prev#" },
      { "playrev", "anim playrev#" },
      
      { "rewind", "anim rewind#" },
      { "restart", "anim on#" },
      
      { "animfps5", "anim fps 5#" },
      { "animfps10", "anim fps 10#" },
      { "animfps20", "anim fps 20#" },
      { "animfps30", "anim fps 30#" },
      { "animfps50", "anim fps 50#" },

      { "measureOff", "set pickingstyle MEASURE OFF; set picking OFF" },
      { "measureDistance",
          "set pickingstyle MEASURE; set picking MEASURE DISTANCE" },
      { "measureAngle", "set pickingstyle MEASURE; set picking MEASURE ANGLE" },
      { "measureTorsion",
          "set pickingstyle MEASURE; set picking MEASURE TORSION" },
      { "PDBmeasureSequence",
          "set pickingstyle MEASURE; set picking MEASURE SEQUENCE" },
      { "measureDelete", "measure delete" },
      { "measureList", "console on;show measurements" },
      { "distanceNanometers", "select *; set measure nanometers" },
      { "distanceAngstroms", "select *; set measure angstroms" },
      { "distancePicometers", "select *; set measure picometers" },

      { "pickOff", "set picking off" },
      { "pickCenter", "set picking center" },
      //    { "pickDraw" , "set picking draw" },
      { "pickIdent", "set picking ident" },
      { "pickLabel", "set picking label" },
      { "pickAtom", "set picking atom" },
      { "PDBpickChain", "set picking chain" },
      { "pickElement", "set picking element" },
      { "PDBpickGroup", "set picking group" },
      { "pickMolecule", "set picking molecule" },
      { "SYMMETRYpickSite", "set picking site" },
      { "pickSpin", "set picking spin" },
      { "SYMMETRYpickSymmetry", "set picking symmetry" },

      { "showConsole", "console" },
      { "showFile", "console on;show file" },
      { "showFileHeader", "console on;getProperty FileHeader" },
      { "showHistory", "console on;show history" },
      { "showIsosurface", "console on;show isosurface" },
      { "showMeasure", "console on;show measure" },
      { "showMo", "console on;show mo" },
      { "showModel", "console on;show model" },
      { "showOrient", "console on;show orientation" },
      { "showSpacegroup", "console on;show spacegroup" },
      { "showState", "console on;show state" },
      
      { "reload", "load \"\"" },
      { "SIGNEDloadPdb", "load ?PdbId?" },      
      { "SIGNEDloadFileOrUrl", "load ?" },      
      { "SIGNEDloadFileUnitCell", "load ? {1 1 1}" },      
      { "SIGNEDloadScript", "script ?.spt" },      

      { "writeFileTextVARIABLE", "if (_applet && !_signedApplet) { console;show file } else { write file \"?FILE?\"}" },      
      { "writeState", "if (_applet && !_signedApplet) { console;show state } else { write state \"?FILEROOT?.spt\"}" },      
      { "writeHistory", "if (_applet && !_signedApplet) { console;show history } else { write history \"?FILEROOT?.his\"}" },     
      { "SIGNEDwriteJmol", "write \"?FILEROOT?.jmol\"" },      
      { "SIGNEDwriteIsosurface", "write isosurface \"?FILEROOT?.jvxl\"" },      
      { "SIGNEDwriteGif", "write image \"?FILEROOT?.gif\"" },      
      { "SIGNEDwriteJpg", "write image \"?FILEROOT?.jpg\"" },      
      { "SIGNEDwritePng", "write image \"?FILEROOT?.png\"" },      
      { "SIGNEDwritePngJmol", "write PNGJ \"?FILEROOT?.png\"" },      
      { "SIGNEDwritePovray", "write POVRAY \"?FILEROOT?.pov\"" },      
      { "SIGNEDwriteVrml", "write VRML \"?FILEROOT?.wrl\"" },      
      { "SIGNEDwriteX3d", "write X3D \"?FILEROOT?.x3d\"" },      
      { "SIGNEDwriteIdtf", "write IDTF \"?FILEROOT?.idtf\"" },      
      { "SIGNEDwriteMaya", "write MAYA \"?FILEROOT?.ma\"" },       
      { "SYMMETRYshowSymmetry", "console on;show symmetry" },
      { "UNITCELLshow", "console on;show unitcell" },
      { "extractMOL", "console on;getproperty extractModel \"visible\" " },
      
       { "minimize", "minimize" },    
       { "modelkit", "set modelkitmode" },    
      //  { "calculateVolume", "console on;print \"Volume = \" + {*}.volume() + \" Ang^3\"" },     
      
      { "surfDots", "dots on" },
      { "surfVDW", "isosurface delete resolution 0 solvent 0 translucent" },
      { "surfMolecular", "isosurface delete resolution 0 molecular translucent" },
      { "surfSolvent14",
          "isosurface delete resolution 0 solvent 1.4 translucent" },
      { "surfSolventAccessible14",
          "isosurface delete resolution 0 sasurface 1.4 translucent" },
      { "CHARGEsurfMEP",
          "isosurface delete resolution 0 vdw color range all map MEP translucent" },
      { "surfOpaque", "mo opaque;isosurface opaque" },
      { "surfTranslucent", "mo translucent;isosurface translucent" },
      { "surfOff", "mo delete;isosurface delete;select *;dots off" },
      { "SYMMETRYhide", "draw sym_* delete" },
      { "FILEMOLload",
      "save orientation;load \"\";restore orientation;center" },
      { "FILEUNITone",
          "save orientation;load \"\" {1 1 1} ;restore orientation;center" },
      { "FILEUNITnine",
          "save orientation;load \"\" {444 666 1} ;restore orientation;center" },
      { "FILEUNITnineRestricted",
          "save orientation;load \"\" {444 666 1} ;restore orientation; unitcell on; display cell=555;center visible;zoom 200" },
      { "FILEUNITninePoly",
          "save orientation;load \"\" {444 666 1} ;restore orientation; unitcell on; display cell=555; polyhedra 4,6 (displayed);center (visible);zoom 200" },

      { "1p", "on" },
      { "3p", "3" },
      { "5p", "5" },
      { "10p", "10" },

      { "10a", "0.1" },
      { "20a", "0.20" },
      { "25a", "0.25" },
      { "50a", "0.50" },
      { "100a", "1.0" },
  };
  
  @Override
  protected String[] getWordContents() {
    
    boolean wasTranslating = GT.getDoTranslate();
    GT.setDoTranslate(true);
    String[] words = new String[] {
        "modelSetMenu", GT.$("No atoms loaded"),
        
        "configurationComputedMenu", GT.$("Configurations"),
        "elementsComputedMenu", GT.$("Element"),
        "FRAMESbyModelComputedMenu", GT.$("Model/Frame"),
        "languageComputedMenu", GT.$("Language"),
        "PDBaaResiduesComputedMenu", GT.$("By Residue Name"),
        "PDBnucleicResiduesComputedMenu", GT.$("By Residue Name"),
        "PDBcarboResiduesComputedMenu", GT.$("By Residue Name"),
        "PDBheteroComputedMenu", GT.$("By HETATM"),
        "surfMoComputedMenuText", GT.$("Molecular Orbitals ({0})"),
        "SYMMETRYSelectComputedMenu", GT.$("Symmetry"),
        "SYMMETRYShowComputedMenu", GT.$("Space Group"),
        "SYMMETRYhide", GT.$("Hide Symmetry"),
        "hiddenModelSetText", GT.$("Model information"),
        "selectMenuText", GT.$("Select ({0})"),
        "allModelsText", GT.$("All {0} models"),
        "configurationMenuText", GT.$("Configurations ({0})"),
        "modelSetCollectionText", GT.$("Collection of {0} models"),
        "atomsText", GT.$("atoms: {0}"),
        "bondsText", GT.$("bonds: {0}"),
        "groupsText", GT.$("groups: {0}"),
        "chainsText", GT.$("chains: {0}"),
        "polymersText", GT.$("polymers: {0}"),
        "modelMenuText", GT.$("model {0}"),
        "viewMenuText", GT.$("View {0}"),
        "mainMenuText", GT.$("Main Menu"),
        "biomoleculesMenuText", GT.$("Biomolecules"),
        "biomoleculeText", GT.$("biomolecule {0} ({1} atoms)"),
        "loadBiomoleculeText", GT.$("load biomolecule {0} ({1} atoms)"),
        
        
//        "selectMenu", GT.$("Select"),
        "selectAll", GT.$("All"),
        "selectNone", GT.$("None"),
        "hideNotSelectedCB", GT.$("Display Selected Only"),
        "invertSelection", GT.$("Invert Selection"),

        "viewMenu", GT.$("View"),
        "front", GT.$("Front"),
        "left", GT.$("Left"),
        "right", GT.$("Right"),
        "top", TextFormat.split(GT.$("Top[as in \"view from the top, from above\" - (translators: remove this bracketed part]"), '[')[0],
        "bottom", GT.$("Bottom"),
        "back", GT.$("Back"),

        "PDBproteinMenu", GT.$("Protein"),
        "allProtein", GT.$("All"),
        "proteinBackbone", GT.$("Backbone"),
        "proteinSideChains", GT.$("Side Chains"),
        "polar", GT.$("Polar Residues"),
        "nonpolar", GT.$("Nonpolar Residues"),
        "positiveCharge", GT.$("Basic Residues (+)"),
        "negativeCharge", GT.$("Acidic Residues (-)"),
        "noCharge", GT.$("Uncharged Residues"),
        "PDBnucleicMenu", GT.$("Nucleic"),
        "allNucleic", GT.$("All"),
        "DNA", GT.$("DNA"),
        "RNA", GT.$("RNA"),
        "nucleicBackbone", GT.$("Backbone"),
        "nucleicBases", GT.$("Bases"),
        "atPairs", GT.$("AT pairs"),
        "gcPairs", GT.$("GC pairs"),
        "auPairs", GT.$("AU pairs"),
        "PDBheteroMenu", GT.$("Hetero"),
        "allHetero", GT.$("All PDB \"HETATM\""),
        "Solvent", GT.$("All Solvent"),
        "Water", GT.$("All Water"),
        "nonWaterSolvent",
            GT.$("Nonaqueous Solvent") + " (solvent and not water)",
        "exceptWater", GT.$("Nonaqueous HETATM") + " (hetero and not water)",
        "Ligand", GT.$("Ligand"),

        "allCarbo", GT.$("All"),
        "PDBcarboMenu", GT.$("Carbohydrate"),
        "PDBnoneOfTheAbove", GT.$("None of the above"),

        "renderMenu", GT.$("Style"),
        "renderSchemeMenu", GT.$("Scheme"),
        "renderCpkSpacefill", GT.$("CPK Spacefill"),
        "renderBallAndStick", GT.$("Ball and Stick"),
        "renderSticks", GT.$("Sticks"),
        "renderWireframe", GT.$("Wireframe"),
        "PDBrenderCartoonsOnly", GT.$("Cartoon"),
        "PDBrenderTraceOnly", GT.$("Trace"),

        "atomMenu", GT.$("Atoms"),
        "atomNone", GT.$("Off"),
        "atom15", GT.$("{0}% van der Waals", "15"),
        "atom20", GT.$("{0}% van der Waals", "20"),
        "atom25", GT.$("{0}% van der Waals", "25"),
        "atom50", GT.$("{0}% van der Waals", "50"),
        "atom75", GT.$("{0}% van der Waals", "75"),
        "atom100", GT.$("{0}% van der Waals", "100"),

        "bondMenu", GT.$("Bonds"),
        "bondNone", GT.$("Off"),
        "bondWireframe", GT.$("On"),
        "bond100", GT.$("{0} \u00C5", "0.10"),
        "bond150", GT.$("{0} \u00C5", "0.15"),
        "bond200", GT.$("{0} \u00C5", "0.20"),
        "bond250", GT.$("{0} \u00C5", "0.25"),
        "bond300", GT.$("{0} \u00C5", "0.30"),

        "hbondMenu", GT.$("Hydrogen Bonds"),
        "hbondNone", GT.$("Off"),
        "hbondCalc", GT.$("Calculate"),
        "hbondWireframe", GT.$("On"),
        "PDBhbondSidechain", GT.$("Set H-Bonds Side Chain"),
        "PDBhbondBackbone", GT.$("Set H-Bonds Backbone"),
        "hbond100", GT.$("{0} \u00C5", "0.10"),
        "hbond150", GT.$("{0} \u00C5", "0.15"),
        "hbond200", GT.$("{0} \u00C5", "0.20"),
        "hbond250", GT.$("{0} \u00C5", "0.25"),
        "hbond300", GT.$("{0} \u00C5", "0.30"),

        "ssbondMenu", GT.$("Disulfide Bonds"),
        "ssbondNone", GT.$("Off"),
        "ssbondWireframe", GT.$("On"),
        "PDBssbondSidechain", GT.$("Set SS-Bonds Side Chain"),
        "PDBssbondBackbone", GT.$("Set SS-Bonds Backbone"),
        "ssbond100", GT.$("{0} \u00C5", "0.10"),
        "ssbond150", GT.$("{0} \u00C5", "0.15"),
        "ssbond200", GT.$("{0} \u00C5", "0.20"),
        "ssbond250", GT.$("{0} \u00C5", "0.25"),
        "ssbond300", GT.$("{0} \u00C5", "0.30"),

        "PDBstructureMenu", GT.$("Structures"),
        "structureNone", GT.$("Off"),
        "backbone", GT.$("Backbone"),
        "cartoon", GT.$("Cartoon"),
        "cartoonRockets", GT.$("Cartoon Rockets"),
        "ribbons", GT.$("Ribbons"),
        "rockets", GT.$("Rockets"),
        "strands", GT.$("Strands"),
        "trace", GT.$("Trace"),

        "VIBRATIONMenu", GT.$("Vibration"),
        "vibrationOff", GT.$("Off"),
        "vibrationOn", GT.$("On"),
        "VIBRATIONvectorMenu", GT.$("Vectors"),
        "spectraMenu", GT.$("Spectra"),
        "hnmrMenu", GT.$("1H-NMR"),
        "cnmrMenu", GT.$("13C-NMR"),
        "vectorOff", GT.$("Off"),
        "vectorOn", GT.$("On"),
        "vector3", GT.$("{0} pixels", "3"),
        "vector005", GT.$("{0} \u00C5", "0.05"),
        "vector01", GT.$("{0} \u00C5", "0.10"),
        "vectorScale02", GT.$("Scale {0}", "0.2"),
        "vectorScale05", GT.$("Scale {0}", "0.5"),
        "vectorScale1", GT.$("Scale {0}", "1"),
        "vectorScale2", GT.$("Scale {0}", "2"),
        "vectorScale5", GT.$("Scale {0}", "5"),

        "stereoMenu", GT.$("Stereographic"),
        "stereoNone", GT.$("None"),
        "stereoRedCyan", GT.$("Red+Cyan glasses"),
        "stereoRedBlue", GT.$("Red+Blue glasses"),
        "stereoRedGreen", GT.$("Red+Green glasses"),
        "stereoCrossEyed", GT.$("Cross-eyed viewing"),
        "stereoWallEyed", GT.$("Wall-eyed viewing"),

        "labelMenu", GT.$("Labels"),

        "labelNone", GT.$("None"),
        "labelSymbol", GT.$("With Element Symbol"),
        "labelName", GT.$("With Atom Name"),
        "labelNumber", GT.$("With Atom Number"),

        "labelPositionMenu", GT.$("Position Label on Atom"),
        "labelCentered", GT.$("Centered"),
        "labelUpperRight", GT.$("Upper Right"),
        "labelLowerRight", GT.$("Lower Right"),
        "labelUpperLeft", GT.$("Upper Left"),
        "labelLowerLeft", GT.$("Lower Left"),

        "colorMenu", GT.$("Color"),
        "[color_atoms]Menu", GT.$("Atoms"),

        "schemeMenu", GT.$("By Scheme"),
        "cpk", GT.$("Element (CPK)"),
        "altloc#PDB", GT.$("Alternative Location"),
        "molecule", GT.$("Molecule"),
        "formalcharge", GT.$("Formal Charge"),
        "partialcharge#CHARGE", GT.$("Partial Charge"),
        "relativeTemperature#BFACTORS", GT.$("Temperature (Relative)"),
        "fixedTemperature#BFACTORS", GT.$("Temperature (Fixed)"),

        "amino#PDB", GT.$("Amino Acid"),
        "structure#PDB", GT.$("Secondary Structure"),
        "chain#PDB", GT.$("Chain"),
        "group#PDB", GT.$("Group"),
        "monomer#PDB", GT.$("Monomer"),
        "shapely#PDB", GT.$("Shapely"),

        "none", GT.$("Inherit"),
        "black", GT.$("Black"),
        "white", GT.$("White"),
        "cyan", GT.$("Cyan"),

        "red", GT.$("Red"),
        "orange", GT.$("Orange"),
        "yellow", GT.$("Yellow"),
        "green", GT.$("Green"),
        "blue", GT.$("Blue"),
        "indigo", GT.$("Indigo"),
        "violet", GT.$("Violet"),

        "salmon", GT.$("Salmon"),
        "olive", GT.$("Olive"),
        "maroon", GT.$("Maroon"),
        "gray", GT.$("Gray"),
        "slateblue", GT.$("Slate Blue"),
        "gold", GT.$("Gold"),
        "orchid", GT.$("Orchid"),

        "opaque", GT.$("Make Opaque"),
        "translucent", GT.$("Make Translucent"),

        "[color_bonds]Menu", GT.$("Bonds"),
        "[color_hbonds]Menu", GT.$("Hydrogen Bonds"),
        "[color_ssbonds]Menu", GT.$("Disulfide Bonds"),
        "colorPDBStructuresMenu", GT.$("Structures"),
        "[color_backbone]Menu", GT.$("Backbone"),
        "[color_trace]Menu", GT.$("Trace"),
        "[color_cartoon]sMenu", GT.$("Cartoon"),
        "[color_ribbon]sMenu", GT.$("Ribbons"),
        "[color_rockets]Menu", GT.$("Rockets"),
        "[color_strands]Menu", GT.$("Strands"),
        "[color_labels]Menu", GT.$("Labels"),
        "[color_background]Menu", GT.$("Background"),
        "[color_isosurface]Menu", GT.$("Surfaces"),
        "[color_vectors]Menu", GT.$("Vectors"),
        "[color_axes]Menu", GT.$("Axes"),
        "[color_boundbox]Menu", GT.$("Boundbox"),
        "[color_UNITCELL]Menu", GT.$("Unit cell"),

        "zoomMenu", GT.$("Zoom"),
        "zoom50", "50%",
        "zoom100", "100%",
        "zoom150", "150%",
        "zoom200", "200%",
        "zoom400", "400%",
        "zoom800", "800%",
        "zoomIn", GT.$("Zoom In"),
        "zoomOut", GT.$("Zoom Out"),

        "spinMenu", GT.$("Spin"),
        "spinOn", GT.$("On"),
        "spinOff", GT.$("Off"),

        "[set_spin_X]Menu", GT.$("Set X Rate"),
        "[set_spin_Y]Menu", GT.$("Set Y Rate"),
        "[set_spin_Z]Menu", GT.$("Set Z Rate"),
        "[set_spin_FPS]Menu", GT.$("Set FPS"),

        "s0", "0",
        "s5", "5",
        "s10", "10",
        "s20", "20",
        "s30", "30",
        "s40", "40",
        "s50", "50",

        "FRAMESanimateMenu", GT.$("Animation"),
        "animModeMenu", GT.$("Animation Mode"),
        "onceThrough", GT.$("Play Once"),
        "palindrome", GT.$("Palindrome"),
        "loop", GT.$("Loop"),
        
        "play", GT.$("Play"),
        "pause", GT.$("Pause"),
        "resume", GT.$("Resume"),
        "stop", GT.$("Stop"),
        "nextframe", GT.$("Next Frame"),
        "prevframe", GT.$("Previous Frame"),
        "rewind", GT.$("Rewind"),
        "playrev", GT.$("Reverse"),
        "restart", GT.$("Restart"),

        "FRAMESanimFpsMenu", GT.$("Set FPS"),
        "animfps5", "5",
        "animfps10", "10",
        "animfps20", "20",
        "animfps30", "30",
        "animfps50", "50",

        "measureMenu", GT.$("Measurements"),
        "measureOff", GT.$("Double-Click begins and ends all measurements"),
        "measureDistance", GT.$("Click for distance measurement"),
        "measureAngle", GT.$("Click for angle measurement"),
        "measureTorsion", GT.$("Click for torsion (dihedral) measurement"),
        "PDBmeasureSequence", GT.$("Click two atoms to display a sequence in the console"),
        "measureDelete", GT.$("Delete measurements"),
        "measureList", GT.$("List measurements"),
        "distanceNanometers", GT.$("Distance units nanometers"),
        "distanceAngstroms", GT.$("Distance units Angstroms"),
        "distancePicometers", GT.$("Distance units picometers"),

        "pickingMenu", GT.$("Set picking"),
        "pickOff", GT.$("Off"),
        "pickCenter", GT.$("Center"),
        //    "pickDraw" , GT.$("moves arrows"),
        "pickIdent", GT.$("Identity"),
        "pickLabel", GT.$("Label"),
        "pickAtom", GT.$("Select atom"),
        "PDBpickChain", GT.$("Select chain"),
        "pickElement", GT.$("Select element"),
        "PDBpickGroup", GT.$("Select group"),
        "pickMolecule", GT.$("Select molecule"),
        "SYMMETRYpickSite", GT.$("Select site"),
        "SYMMETRYpickSymmetry", GT.$("Show symmetry operation"),
        "pickSpin", GT.$("Spin"),

        "showMenu", GT.$("Show"),
        "showConsole", GT.$("Console"),
        "showFile", GT.$("File Contents"),
        "showFileHeader", GT.$("File Header"),
        "showHistory", GT.$("History"),
        "showIsosurface", GT.$("Isosurface JVXL data"),
        "showMeasure", GT.$("Measurements"),
        "showMo", GT.$("Molecular orbital JVXL data"),
        "showModel", GT.$("Model"),
        "showOrient", GT.$("Orientation"),
        "showSpacegroup", GT.$("Space group"),
        "SYMMETRYshowSymmetry", GT.$("Symmetry"),
        "showState", GT.$("Current state"),
        
        "fileMenu", GT.$("File"),
        "reload", GT.$("Reload"),      
        "SIGNEDloadPdb", GT.$("Open from PDB"),      
        "SIGNEDloadFileOrUrl", GT.$("Open file or URL"),      
        "SIGNEDloadFileUnitCell", GT.$("Load full unit cell"),      
        "SIGNEDloadScript", GT.$("Open script"),      

        "writeFileTextVARIABLE", GT.$("Save a copy of {0}"),
        "writeState", GT.$("Save script with state"),      
        "writeHistory", GT.$("Save script with history"),      
        "SIGNEDwriteJpg", GT.$("Export {0} image", "JPG"),      
        "SIGNEDwritePng", GT.$("Export {0} image", "PNG"),      
        "SIGNEDwritePngJmol", GT.$("Export {0} image", "PNG+JMOL"),      
        "SIGNEDwriteGif", GT.$("Export {0} image", "GIF"),    
        "SIGNEDwritePovray", GT.$("Export {0} image", "POV-Ray"),      
        "SIGNEDwriteJmol", GT.$("Save all as JMOL file (zip)"),      
        "SIGNEDwriteIsosurface", GT.$("Save JVXL isosurface"),      
        "SIGNEDwriteVrml", GT.$("Export {0} 3D model", "VRML"),      
        "SIGNEDwriteX3d", GT.$("Export {0} 3D model", "X3D"),      
        "SIGNEDwriteIdtf", GT.$("Export {0} 3D model", "IDTF"),      
        "SIGNEDwriteMaya", GT.$("Export {0} 3D model", "Maya"),      

        "computationMenu", GT.$("Computation"),      
        "minimize", GT.$("Optimize structure"),      
        "modelkit", GT.$("Model kit"),      
        //"calculateVolume", GT.$("Molecular volume"),   
                
        "UNITCELLshow", GT.$("Unit cell"),
        "extractMOL", GT.$("Extract MOL data"),

        "surfaceMenu", GT.$("Surfaces"),
        "surfDots", GT.$("Dot Surface"),
        "surfVDW", GT.$("van der Waals Surface"),
        "surfMolecular", GT.$("Molecular Surface"),
        "surfSolvent14", GT.$("Solvent Surface ({0}-Angstrom probe)", "1.4"),
        "surfSolventAccessible14",
            GT.$("Solvent-Accessible Surface (VDW + {0} Angstrom)", "1.4"),
        "CHARGEsurfMEP", GT.$("Molecular Electrostatic Potential"),
        "surfOpaque", GT.$("Make Opaque"),
        "surfTranslucent", GT.$("Make Translucent"),
        "surfOff", GT.$("Off"),

        "FILEUNITMenu", GT.$("Symmetry"),
        "FILEMOLload", GT.$("Reload {0}", "(molecular)"),
        "FILEUNITone", GT.$("Reload {0}", "{1 1 1}"),
        "FILEUNITnine", GT.$("Reload {0}", "{444 666 1}"),
        "FILEUNITnineRestricted", GT.$("Reload {0} + Display {1}", new Object[] { "{444 666 1}", "555" } ),
        "FILEUNITninePoly", GT.$("Reload + Polyhedra"),
        

        "[set_axes]Menu", GT.$("Axes"), 
        "[set_boundbox]Menu", GT.$("Boundbox"),
        "[set_UNITCELL]Menu", GT.$("Unit cell"),

        "off#axes", GT.$("Hide"), 
        "dotted", GT.$("Dotted"),

        "byPixelMenu", GT.$("Pixel Width"), 
        "1p", GT.$("{0} px", "1"),
        "3p", GT.$("{0} px", "3"), 
        "5p", GT.$("{0} px", "5"),
        "10p", GT.$("{0} px", "10"),

        "byAngstromMenu", GT.$("Angstrom Width"),
        "10a", GT.$("{0} \u00C5", "0.10"),
        "20a", GT.$("{0} \u00C5", "0.20"),
        "25a", GT.$("{0} \u00C5", "0.25"),
        "50a", GT.$("{0} \u00C5", "0.50"),
        "100a", GT.$("{0} \u00C5", "1.0"),

//        "optionsMenu", GT.$("Compatibility"),
        "showSelectionsCB", GT.$("Selection Halos"),
        "showHydrogensCB", GT.$("Show Hydrogens"),
        "showMeasurementsCB", GT.$("Show Measurements"),
        "perspectiveDepthCB", GT.$("Perspective Depth"),      
        "showBoundBoxCB", GT.$("Boundbox"),
        "showAxesCB", GT.$("Axes"),
        "showUNITCELLCB", GT.$("Unit cell"),      
        "colorrasmolCB", GT.$("RasMol Colors"),
        "aboutComputedMenu", GT.$("About..."),
        
        //"rasmolChimeCompatibility", GT.$("RasMol/Chime Settings"),

        "APPLETjmolUrl", "http://www.jmol.org",
        "APPLETmouseManualUrl", GT.$("Mouse Manual"),
        "APPLETtranslationUrl", GT.$("Translations")
    };
 
    GT.setDoTranslate(wasTranslating);
    return words;
  }
  
  @Override
  String getMenuAsText(String title) {
    return "# Jmol.mnu " + title + "\n\n" +
           "# Part I -- Menu Structure\n" +
           "# ------------------------\n\n" +
           dumpStructure(menuContents) + "\n\n" +
           "# Part II -- Key Definitions\n" +
           "# --------------------------\n\n" +
           dumpStructure(structureContents) + "\n\n" +
           "# Part III -- Word Translations\n" +
           "# -----------------------------\n\n" +
           dumpWords();
  }

  private String dumpWords() {
    String[] wordContents = getWordContents();
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < wordContents.length; i++) {
      String key = wordContents[i++];
      if (structure.getProperty(key) == null)
        s.append(key).append(" | ").append(wordContents[i]).append('\n');
    }
    return s.toString();
  }
  
  private String dumpStructure(String[][] items) {
    String previous = "";
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < items.length; i++) {
      String key = items[i][0];
      String label = words.getProperty(key);
      if (label != null)
        key += " | " + label;
      s.append(key).append(" = ")
       .append(items[i][1] == null ? previous : (previous = items[i][1]))
       .append('\n');
    }
    return s.toString();
  }
 

  
}
