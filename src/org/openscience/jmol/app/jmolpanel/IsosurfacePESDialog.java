package org.openscience.jmol.app.jmolpanel;

import org.jmol.api.*;
import org.jmol.i18n.GT;
import org.jmol.util.ColorIndexUtil;
import org.jmol.util.isosurfacePES;
import org.jmol.viewer.Viewer;

import java.io.File;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;



public class IsosurfacePESDialog extends JDialog implements ActionListener, ChangeListener {
  
  /*
  * By Andrew K Long  Andrew.long.3001@gmail.com
  * Based off of GaussianDialog By Andy Turner, atrog@sourceforge.net
  * 
  */
  
  static final String[] COLOR_SCHEME_LIST = {
    "ROYGB", 
    "BGYOR",
    "RWB",
    "BWR",
    "BW",
    "WB",
    "BWZebra"};
  
  int selectedColorScheme = ColorIndexUtil.ROYGB;
  
  
  JmolViewer viewer;
  
  private JPanel container;
  private JTextField checkField, optsField, selectField, isosurfaceComputedResolutionField, isosurfaceComputedProbeRadiusField, isosurfaceDisplayedResolutionField, fragmentField, moleculeNameField, saveFolderField, loadFolderField;
  private JComboBox memBox, methBox, basisBox, dfBox;
  private JSpinner procSpinner, chargeSpinner, multSpinner;
  private JButton setSaveFolderButton, generateFilesButton, testAndViewMeshButton, loadFilesButton, cancelButton, setLoadFolderButton;
  private JFileChooser fileChooser;
  private JTextArea editArea;
  private JTabbedPane inputTabs;
  private String check, mem, proc, meth, route, charge, mult, fragment, select, moleculename, saveFolder, loadFolder, linkSection;
  private float probeRadius;
  
  private static final boolean DEBUG = true;
  
  private static final String DEFAULT_METHOD = "UB3LYP";
  private static final String DEFAULT_BASIS = "6-31G";
  private static final String DEFAULT_CHARGE = "0";
  private static final String DEFAULT_MULT = "2";
  private float computeResolution, displayedResolution;
  private static final String[] BASIS_LIST = {"Gen",
    "6-31G",
    "3-21G",
    "3-21G*",
    "3-21G**",
    "6-21G",
    "4-31G",
    "6-31G",
    "6-311G",
    "D95V",
    "D95",
    "SHC",
    "CEP-4G",
    "CEP-31G",
    "CEP-121G",
    "LanL2MB",
    "LanL2DZ",
    "SDD",
    "SDDAll",
    "cc-pVDZ",
    "cc-pVTZ",
    "cc-pVQZ",
    "cc-pV5Z",
    "cc-pV6Z",
    "aug-cc-pVDZ",
    "aug-cc-pVTZ",
    "aug-cc-pVQZ",
    "aug-cc-pV5Z",
    "aug-cc-pV6Z",
    "SV",
    "SVP",
    "TZV",
    "TZVP",
    "MidiX",
    "EPR-II",
    "EPR-III",
    "UGBS",
    "UGBS1P",
    "UGBS2P",
    "UGBS3P",
    "MTSmall",
    "DGDZVP",
    "DGDZVP2",
    "DGTZVP"};
  private static final String[] METHOD_LIST = {"UB3LYP",
    "HF",
    "MP2",
    "MP3",
    "MP4",
    "CCSD(T)",
    "CIS",
    "CISD",
    "LSDA",
    "BLYP",
    "BP86",
    "BPW91",
    "OLYP",
    "OP86",
    "OPW91",
    "PBEPBE",
    "VSXC",
    "HCTH93",
    "HCTH147",
    "HCTH407",
    "TPSSTPSS",
    "B3LYP",
    "UB3LYP",
    "B3PW91",
    "AM1",
    "PM3",
    "CNDO",
    "INDO",
    "MNDO",
    "MINDO3",
    "ZINDO",
    "UFF",
    "AMBER",
    "DREIDING",
    "Huckel"};
  private static final String[] DF_LIST = {"None",
    "Auto",
    "DGA1",
    "DGA2"};
  private static final String[] MEMORY_LIST = {"Default",
    "100MB",
    "500MB",
    "1GB",
    "2GB",
    "4GB",
    "7GB",
    "15GB"};
  
  private static final String NOBASIS_LIST =   "AM1 PM3 CNDO INDO MNDO MINDO3 ZINDO UFF AMBER DREIDING Huckel";
  private static final String DFT_LIST =   "LSDA BLYP BP86 BPW91 OLYP OP86 OPW91 PBEPBE VSXC HCTH93 NCTH147 HCTH407 TPSSTPSS B3LYP B3PW91 UB3LYP";
  
  
  public IsosurfacePESDialog(JFrame f, JmolViewer viewer) {
    super(f, false);
    this.viewer = viewer;
    probeRadius = 0;
    
    setTitle(GT._("IsosurfacePES"));
    
    container = new JPanel();
    container.setLayout(new GridBagLayout());
    inputTabs = new JTabbedPane();
    
    JPanel basicPanel = buildBasicPanel();
    /*inputTabs.addTab(GT._("Basic"), null, basicPanel);
    JPanel advancedPanel = buildAdvancedPanel();
    inputTabs.addTab(GT._("Advanced"), null, advancedPanel);
    
    inputTabs.addChangeListener(this);*/
    
    JPanel filePanel = buildFilePanel();
    JPanel buttonPanel = buildButtonPanel();
    
    GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.insets = new Insets(0, 0, 5, 0);
    gbc_panel.gridx = 0;
    gbc_panel.gridy = 0;
    gbc_panel.anchor = GridBagConstraints.NORTHWEST;
    
    container.add(basicPanel, gbc_panel);
    
    gbc_panel.gridy = 1;
    container.add(filePanel, gbc_panel);
    
    gbc_panel.gridy = 2;
    container.add(buttonPanel, gbc_panel);
    
    gbc_panel.gridy = 3;
    container.add(buildReferencePanel(), gbc_panel);
    
    
    getContentPane().add(container);
    
    
    
    pack();
    centerDialog();
    updateUI();
  }
  
  private JPanel buildReferencePanel(){
    JPanel showPanel = new JPanel(new BorderLayout());
    String newline = System.getProperty("line.separator");
    
    
    
    String referenceInfo = "Thanks for using this tool.  The Authors of this tool kindly request that you use the following citation if this tool was of use to you " 
        + newline+newline+
    		"REF: Andrew K. Long, Jason A.C. Clyburne \"RADMAP: An isosurface potential energy surface program\" 2012 (TO BE UPDATED)";
    JTextArea textArea = new JTextArea(referenceInfo);
    
    textArea.setColumns(50);
    textArea.setLineWrap(true);
    textArea.setRows(5);
    textArea.setWrapStyleWord(true);
    
    JScrollPane scrollpane = new JScrollPane(textArea);
    
    showPanel.add(scrollpane);
    return showPanel;
  }
  
  private JPanel buildBasicPanel() {
    
    JPanel showPanel = new JPanel(new BorderLayout());
    
    
    
    
    
    
    JPanel linkPanel = new JPanel(new BorderLayout());
    TitledBorder linkTitle = BorderFactory.createTitledBorder("link0 Section");
    linkPanel.setBorder(linkTitle);
    
    JPanel linkLabels = new JPanel(new GridLayout(2,1));
    JPanel linkControls = new JPanel(new GridLayout(2,1));
    
    //JLabel checkLabel = new JLabel(GT._("Checkpoint File: "));
    //linkLabels.add(checkLabel);
    checkField = new JTextField(20);
    //linkControls.add(checkField);*/
    
    JLabel memLabel = new JLabel(GT._("Amount of Memory:"));
    linkLabels.add(memLabel);
    memBox = new JComboBox(MEMORY_LIST);
    linkControls.add(memBox);
    memBox.setSelectedIndex(0);
    
    JLabel procLabel = new JLabel(GT._("Number of Processors:"));
    linkLabels.add(procLabel);
    SpinnerModel procModel = new SpinnerNumberModel(1, 1, 16, 1);
    procSpinner = new JSpinner(procModel);
    procSpinner.setEditor(new JSpinner.NumberEditor(procSpinner, "#"));
    linkControls.add(procSpinner);
    
    linkPanel.add(linkLabels, BorderLayout.LINE_START);
    linkPanel.add(linkControls, BorderLayout.CENTER);
    
    showPanel.add(linkPanel, BorderLayout.NORTH);
    
    
    
    JPanel routePanel = new JPanel(new BorderLayout());
    TitledBorder routeTitle = BorderFactory.createTitledBorder(GT._("Route"));
    routePanel.setBorder(routeTitle);
    
    
    
    
    //route box
    
    JPanel routeLabels = new JPanel(new GridLayout(4,1));
    JPanel routeControls = new JPanel(new GridLayout(4,1));
    
    JLabel methLabel = new JLabel(GT._("Method: "));
    routeLabels.add(methLabel);
    methBox = new JComboBox(METHOD_LIST);
    routeControls.add(methBox);
    methBox.setSelectedIndex(0);
    methBox.addActionListener(this);
    
    JLabel basisLabel = new JLabel(GT._("Basis Set: "));
    routeLabels.add(basisLabel);
    basisBox = new JComboBox(BASIS_LIST);
    routeControls.add(basisBox);
    basisBox.setSelectedIndex(1);
    
    
    JLabel dfLabel = new JLabel(GT._("Density Fitting Basis Set (DFT Only): "));
    routeLabels.add(dfLabel);
    dfBox = new JComboBox(DF_LIST);
    routeControls.add(dfBox);
    dfBox.setSelectedIndex(0);
    
    JLabel optsLabel = new JLabel(GT._("Job Options: "));
    routeLabels.add(optsLabel);
    optsField = new JTextField(20);
    routeControls.add(optsField);
    optsField.setText("scf=xqc");
    
    
    
    routePanel.add(routeLabels, BorderLayout.LINE_START);
    routePanel.add(routeControls, BorderLayout.CENTER);
    
    showPanel.add(routePanel, BorderLayout.CENTER);
    
    //molPanel  
    JPanel molPanel = new JPanel(new BorderLayout());
    TitledBorder molTitle =
    BorderFactory.createTitledBorder(GT._("Molecular Properties"));
    molPanel.setBorder(molTitle);
    
    JPanel molLabels = new JPanel(new GridLayout(3,1));
    JPanel molControls = new JPanel(new GridLayout(3,1));
    
    JLabel chargeLabel = new JLabel(GT._("Total Charge: "));
    molLabels.add(chargeLabel);
    SpinnerModel chargeModel = new SpinnerNumberModel(0, -10, 10, 1);
    chargeSpinner = new JSpinner(chargeModel);
    chargeSpinner.setEditor(new JSpinner.NumberEditor(chargeSpinner, "#"));
    molControls.add(chargeSpinner);
    
    JLabel multLabel = new JLabel(GT._("Multiplicity: "));
    molLabels.add(multLabel);
    SpinnerModel multModel = new SpinnerNumberModel(2, 0, 10, 1);
    multSpinner = new JSpinner(multModel);
    multSpinner.setEditor(new JSpinner.NumberEditor(multSpinner, "#"));
    molControls.add(multSpinner);
    
    JLabel selectLabel = new JLabel(GT._("Selection: "));
    molLabels.add(selectLabel);
    selectField = new JTextField(20);
    selectField.setText("visible");
    molControls.add(selectField);
    
    molPanel.add(molLabels, BorderLayout.LINE_START);
    molPanel.add(molControls, BorderLayout.CENTER);
    
    showPanel.add(molPanel, BorderLayout.SOUTH);
    
    return showPanel;
  }
  
  /*private JPanel buildAdvancedPanel() {
  
  JPanel editPanel = new JPanel(new BorderLayout());
  TitledBorder editTitle = BorderFactory.createTitledBorder("Edit Gaussian Input File");
  editPanel.setBorder(editTitle);
  
  
  
  editArea = new JTextArea();
  JScrollPane editPane = new JScrollPane(editArea);
  editPane.setPreferredSize(new Dimension(150,100));
  
  editPanel.add(editPane, BorderLayout.CENTER);
  
  return editPanel;
  
  }*/
  
  private JPanel buildFilePanel() {
  
    JPanel showPanel = new JPanel(new BorderLayout(2,1));
    
    TitledBorder fileTitle = BorderFactory.createTitledBorder("IPES Properties");
    showPanel.setBorder(fileTitle);
    
    JPanel moleculeNamePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JPanel molceuleNameLabels = new JPanel(new GridLayout(1,2));
    JPanel moleculeNameFields = new JPanel(new GridLayout(1,2));
    
    
    JLabel moleculeNameLabel = new JLabel(GT._("Molecule Name: "));
    molceuleNameLabels.add(moleculeNameLabel);
    moleculeNameField = new JTextField(20);
    moleculeNameFields.add(moleculeNameField);
    moleculeNameField.setText("molecule name");
    
    moleculeNamePanel.add(molceuleNameLabels, BorderLayout.LINE_START);
    moleculeNamePanel.add(moleculeNameFields, BorderLayout.CENTER);
    showPanel.add(moleculeNamePanel, BorderLayout.NORTH);
    
    JPanel subFilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JPanel subFileLabels = new JPanel(new GridLayout(2,1));
    JPanel subFileFields = new JPanel(new GridLayout(2,1));
    JPanel subFileButtons = new JPanel(new GridLayout(2,1));
    
    
    JLabel saveFolderLabel = new JLabel(GT._("Save Folder Location: "));
    subFileLabels.add(saveFolderLabel);
    saveFolderField = new JTextField(30);
    subFileFields.add(saveFolderField);
    saveFolderField.setText(new File("ISPES").getAbsolutePath());
    setSaveFolderButton = new JButton("Folder...");
    setSaveFolderButton.addActionListener(this);
    subFileButtons.add(setSaveFolderButton);
    
    JLabel loadFolderLabel = new JLabel(GT._("Load Folder Location: "));
    subFileLabels.add(loadFolderLabel);
    loadFolderField = new JTextField(30);
    subFileFields.add(loadFolderField);
    loadFolderField.setText(new File("ISPES").getAbsolutePath());
    setLoadFolderButton = new JButton("Folder...");
    setLoadFolderButton.addActionListener(this);
    subFileButtons.add(setLoadFolderButton);
    
    subFilePanel.add(subFileLabels);
    subFilePanel.add(subFileFields);
    subFilePanel.add(subFileButtons);
    
    
    showPanel.add(subFilePanel, BorderLayout.SOUTH);
    
  //isosurface box
    
    JPanel isosurfacePanel = new JPanel(new BorderLayout());
    TitledBorder isosurfaceTitle = BorderFactory.createTitledBorder("Isosurface Section");
    
    isosurfacePanel.setBorder(isosurfaceTitle);
    JPanel isosurfaceLabels = new JPanel(new GridLayout(5,1));
    JPanel isosurfaceControls = new JPanel(new GridLayout(5,1));
    
    JLabel computedResolutionLabel = new JLabel(GT._("isosurface computed resolution: "));
    isosurfaceLabels.add(computedResolutionLabel);
    isosurfaceComputedResolutionField = new JTextField(20);
    
    isosurfaceControls.add(isosurfaceComputedResolutionField);
    isosurfaceComputedResolutionField.setText("1.5");
    
    JLabel computedProbeRadiusLabel = new JLabel(GT._("isosurface computed probe radius: "));
    isosurfaceLabels.add(computedProbeRadiusLabel);
    isosurfaceComputedProbeRadiusField = new JTextField(20);
    
    isosurfaceControls.add(isosurfaceComputedProbeRadiusField);
    isosurfaceComputedProbeRadiusField.setText("1.2");
    
    JLabel displayedResolutionLabel = new JLabel(GT._("isosurface displayed resolution: "));
    isosurfaceLabels.add(displayedResolutionLabel);
    isosurfaceDisplayedResolutionField = new JTextField(20);
    
    isosurfaceControls.add(isosurfaceDisplayedResolutionField);
    isosurfaceDisplayedResolutionField.setText("5");
    
    JLabel fragmentLabel = new JLabel(GT._("fragment: "));
    isosurfaceLabels.add(fragmentLabel);
    fragmentField = new JTextField(20);
    
    isosurfaceControls.add(fragmentField);
    fragmentField.setText("H");
    
    JLabel lblColorScheme = new JLabel("Color Scheme");
    isosurfaceLabels.add(lblColorScheme);
    
    final JComboBox colorSchemeComboBox = new JComboBox(COLOR_SCHEME_LIST);
    colorSchemeComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        selectedColorScheme=ColorIndexUtil.colorSchemeNameToInt(COLOR_SCHEME_LIST[colorSchemeComboBox.getSelectedIndex()]);
      }
    });
    isosurfaceControls.add(colorSchemeComboBox);
    
    isosurfacePanel.add(isosurfaceLabels, BorderLayout.LINE_START);
    isosurfacePanel.add(isosurfaceControls, BorderLayout.CENTER);
    
    showPanel.add(isosurfacePanel, BorderLayout.CENTER);
    
    
    
    return showPanel;
  }
  
  private JPanel buildButtonPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    
    testAndViewMeshButton = new JButton(GT._("Test and View Mesh"));
    testAndViewMeshButton.addActionListener(this);
    buttonPanel.add(testAndViewMeshButton);
    
    generateFilesButton = new JButton(GT._("Generate Files"));
    generateFilesButton.addActionListener(this);
    buttonPanel.add(generateFilesButton);
    
    loadFilesButton = new JButton(GT._("Load Files"));
    loadFilesButton.addActionListener(this);
    buttonPanel.add(loadFilesButton);
    
    cancelButton = new JButton(GT._("Cancel"));
    cancelButton.addActionListener(this);
    buttonPanel.add(cancelButton);
    
    getRootPane().setDefaultButton(generateFilesButton);
    return buttonPanel;
  }
  
  protected void centerDialog() {
  
    Dimension screenSize = this.getToolkit().getScreenSize();
    Dimension size = this.getSize();
    screenSize.height = screenSize.height / 2;
    screenSize.width = screenSize.width / 2;
    size.height = size.height / 2;
    size.width = size.width / 2;
    int y = screenSize.height - size.height;
    int x = screenSize.width - size.width;
    this.setLocation(x, y);
  }
  
  private void updateVars() {
    check = checkField.getText();
    mem = memBox.getSelectedItem().toString();
    proc = procSpinner.getValue().toString();
    select = selectField.getText();
    if (select.length() == 0) {
      select = "visible";
      selectField.setText(select);
    }
    
    charge = chargeSpinner.getValue().toString();
    if (charge.equals("")) charge = DEFAULT_CHARGE;
    mult = multSpinner.getValue().toString();
    if (mult.equals("")) mult = DEFAULT_MULT;
    
    String basis = basisBox.getSelectedItem().toString();
    if (basis.equals("")) basis = DEFAULT_BASIS;
    meth = methBox.getSelectedItem().toString();
    if (meth.equals("")) meth = DEFAULT_METHOD;
    if (NOBASIS_LIST.lastIndexOf(meth, NOBASIS_LIST.length()) >= 0) basis = "";
    if (!basis.equals("")) basis = "/" + basis;
    String df = dfBox.getSelectedItem().toString();
    if (DFT_LIST.lastIndexOf(meth, DFT_LIST.length()) < 0) df = "None";
    if (df.equals("None")) {
      df = "";
    } else {
      df = "/" + df;
    }
    computeResolution = Float.parseFloat(isosurfaceComputedResolutionField.getText());
    probeRadius = Float.parseFloat(isosurfaceComputedProbeRadiusField.getText());
    displayedResolution = Float.parseFloat(isosurfaceDisplayedResolutionField.getText());
    fragment = fragmentField.getText();
    
    saveFolder = saveFolderField.getText();
    //if (saveFolder.equals("ISPES")) file = "my_input.com";
    
    loadFolder = loadFolderField.getText();
    
    moleculename = moleculeNameField.getText();
    
    String opts = optsField.getText();
    route = "# " + meth + basis + df + " " + opts;
    
    String c = check;
    if (! c.equals("")) c = "%chk=" + c ;
    String m = mem;
    if (! m.equals("Default")) { 
      m = "%mem=" + m ;
    } else {
      m = "";
    }
    String p = proc;
    if (! p.equals("1")) {
      p = "%nproc=" + p;
    } else {
      p = "";
    }
    
    String EOL = System.getProperty("line.separator");

    
    linkSection = c+EOL+m+EOL+p;
    
    
  
  }
  
  private void updateUI() {
    updateVars();
    if (NOBASIS_LIST.lastIndexOf(meth, NOBASIS_LIST.length()) >= 0) {
      basisBox.setEnabled(false);
    } else {
      basisBox.setEnabled(true);
    }
    if (DFT_LIST.lastIndexOf(meth, DFT_LIST.length()) >= 0) {
      dfBox.setEnabled(true);
    } else {
      dfBox.setEnabled(false);
    }
    return;
  }
  

  
  private void generateIsosurfacePESFiles() {
    if(DEBUG)System.out.println("generating Isosurface PES files mult: "+mult);
    this.updateVars();
    isosurfacePES.generateIsosurfacePESfiles((Viewer)this.viewer,Integer.parseInt(charge),Integer.parseInt(mult),computeResolution,probeRadius,fragment, saveFolder, moleculename, route, linkSection);
  }
  
  private void testAndViewMesh() {
    if(DEBUG)System.out.println("test and View Mesh");
    this.updateVars();
    isosurfacePES.testAndViewSurface((Viewer)this.viewer,computeResolution,probeRadius);
  }
  
  private void loadIsosurfacePESfiles() {
    if(DEBUG)System.out.println("loading Isosurface PES files ");
    this.updateVars();
    isosurfacePES.loadIsosurfacePES((Viewer)this.viewer, loadFolder, Float.toString(displayedResolution),selectedColorScheme);
    
  }
  
  private void cancel() {
    dispose();
  }
  
  private void setSaveFolderLocation() {
    fileChooser = new JFileChooser();
    
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
    int ierr = fileChooser.showDialog(this, "Set");
    if (ierr == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      saveFolderField.setText(file.getAbsolutePath());
    }
  }
  private void setLoadFolderLocation() {
    fileChooser = new JFileChooser();
    
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
    int ierr = fileChooser.showDialog(this, "Set");
    if (ierr == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      loadFolderField.setText(file.getAbsolutePath());
    }
    
    
  }
  
  @Override
  public void paint(Graphics g){
    
    this.pack();
    super.paint(g);
    
  } // paint()
  
  private void tabSwitched() {
    if (inputTabs.getSelectedIndex() == 1) {
      getCommand();
    }
  }
  
  protected void getCommand() {
    updateVars();
    String c = check;
    if (! c.equals("")) c = "%chk=" + c + "\n";
    String m = mem;
    if (! m.equals("Default")) { 
      m = "%mem=" + m + "\n";
    } else {
      m = "";
    }
    String p = proc;
    if (! p.equals("1")) {
      p = "%nproc=" + p + "\n";
    } else {
      p = "";
    }
  
    String data = viewer.getData(select,"USER:%-2e %10.5x %10.5y %10.5z");
    
    editArea.setText(c + m + p + route + "\n\n" + 
    "Title: Created by Jmol version " + Viewer.getJmolVersion() + "\n\n" + charge + " " + mult +
    "\n" + data + "\n");
  }
  
  public void actionPerformed(ActionEvent event) {
    if (event.getSource() == generateFilesButton) {
      generateIsosurfacePESFiles();
      
    } else if (event.getSource() == testAndViewMeshButton) {
      testAndViewMesh();
    } else if (event.getSource() == cancelButton) {
      cancel();
    } else if (event.getSource() == setSaveFolderButton) {
      if(DEBUG)System.out.println("setSaveFolderButtonClicked");
      setSaveFolderLocation();
    } else if (event.getSource() == setLoadFolderButton) {
      if(DEBUG)System.out.println("setLoadFolderButtonClicked");
      setLoadFolderLocation();
    } else if (event.getSource() == methBox) {
      updateUI();
    } else if (event.getSource() == loadFilesButton){
      loadIsosurfacePESfiles();
    }
  }
  
  

  

  public void stateChanged(ChangeEvent event) {
    if (event.getSource() == inputTabs) {
      tabSwitched();
    }
  }

}