package org.openscience.jmol.app.jmolpanel;

import org.jmol.api.*;
import org.jmol.i18n.GT;
import org.jmol.util.ColorIndexUtil;
import org.jmol.util.Psi4Engine;
import org.jmol.util.Psi4Runner;
import org.jmol.util.isosurfacePES;
import org.jmol.viewer.Viewer;

import java.io.File;
import java.util.List;

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
import javax.swing.JCheckBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
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
  private JComboBox engineBox, memBox, methBox, basisBox, dfBox;
  private JCheckBox forcesBox;
  private JButton runPsi4Button;
  private JTextField psi4CommandField;
  private JProgressBar psi4ProgressBar;
  private JPanel psi4RunPanel;
  private Psi4Runner psi4Runner;
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
  
  private static final String GAUSSIAN = "Gaussian";
  private static final String PSI4 = "Psi4";
  private static final String[] ENGINE_LIST = {PSI4, GAUSSIAN};
  
  //the method and basis boxes are editable in Psi4 mode, so these are only suggestions
  private static final String[] PSI4_METHOD_LIST = {"b3lyp",
    "b3lyp-d3bj",
    "wb97x-d",
    "wb97m-v",
    "pbe0",
    "pbe0-d3bj",
    "pbe",
    "m06-2x",
    "hf",
    "mp2",
    "ccsd(t)"};
  private static final String[] PSI4_BASIS_LIST = {"6-31G",
    "6-31G*",
    "6-311+G**",
    "def2-SVP",
    "def2-TZVP",
    "def2-TZVPP",
    "cc-pVDZ",
    "cc-pVTZ",
    "aug-cc-pVDZ",
    "aug-cc-pVTZ"};
  private static final String GAUSSIAN_DEFAULT_OPTIONS = "scf=xqc";
  
  private static final String NOBASIS_LIST =   "AM1 PM3 CNDO INDO MNDO MINDO3 ZINDO UFF AMBER DREIDING Huckel";
  private static final String DFT_LIST =   "LSDA BLYP BP86 BPW91 OLYP OP86 OPW91 PBEPBE VSXC HCTH93 NCTH147 HCTH407 TPSSTPSS B3LYP B3PW91 UB3LYP";
  
  
  public IsosurfacePESDialog(JFrame f, JmolViewer viewer) {
    super(f, false);
    this.viewer = viewer;
    probeRadius = 0;
    
    setTitle(GT.$("IsosurfacePES"));
    
    container = new JPanel();
    container.setLayout(new GridBagLayout());
    inputTabs = new JTabbedPane();
    
    JPanel basicPanel = buildBasicPanel();
    /*inputTabs.addTab(GT.$("Basic"), null, basicPanel);
    JPanel advancedPanel = buildAdvancedPanel();
    inputTabs.addTab(GT.$("Advanced"), null, advancedPanel);
    
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
    gbc_panel.fill = GridBagConstraints.HORIZONTAL;
    container.add(buildPsi4RunPanel(), gbc_panel);
    gbc_panel.fill = GridBagConstraints.NONE;
    
    gbc_panel.gridy = 4;
    container.add(buildReferencePanel(), gbc_panel);
    
    
    getContentPane().add(container);
    
    
    
    engineChanged(); //sets the dialog up for the default program and packs it
    centerDialog();
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
    
    JPanel linkLabels = new JPanel(new GridLayout(3,1));
    JPanel linkControls = new JPanel(new GridLayout(3,1));
    
    JLabel engineLabel = new JLabel(GT.$("Program:"));
    linkLabels.add(engineLabel);
    engineBox = new JComboBox(ENGINE_LIST);
    linkControls.add(engineBox);
    engineBox.setSelectedIndex(0);
    engineBox.addActionListener(this);
    
    //JLabel checkLabel = new JLabel(GT.$("Checkpoint File: "));
    //linkLabels.add(checkLabel);
    checkField = new JTextField(20);
    //linkControls.add(checkField);*/
    
    JLabel memLabel = new JLabel(GT.$("Amount of Memory:"));
    linkLabels.add(memLabel);
    memBox = new JComboBox(MEMORY_LIST);
    linkControls.add(memBox);
    memBox.setSelectedIndex(0);
    
    JLabel procLabel = new JLabel(GT.$("Number of Processors:"));
    linkLabels.add(procLabel);
    SpinnerModel procModel = new SpinnerNumberModel(1, 1, 16, 1);
    procSpinner = new JSpinner(procModel);
    procSpinner.setEditor(new JSpinner.NumberEditor(procSpinner, "#"));
    linkControls.add(procSpinner);
    
    linkPanel.add(linkLabels, BorderLayout.LINE_START);
    linkPanel.add(linkControls, BorderLayout.CENTER);
    
    showPanel.add(linkPanel, BorderLayout.NORTH);
    
    
    
    JPanel routePanel = new JPanel(new BorderLayout());
    TitledBorder routeTitle = BorderFactory.createTitledBorder(GT.$("Route"));
    routePanel.setBorder(routeTitle);
    
    
    
    
    //route box
    
    JPanel routeLabels = new JPanel(new GridLayout(4,1));
    JPanel routeControls = new JPanel(new GridLayout(4,1));
    
    JLabel methLabel = new JLabel(GT.$("Method: "));
    routeLabels.add(methLabel);
    methBox = new JComboBox(METHOD_LIST);
    routeControls.add(methBox);
    methBox.setSelectedIndex(0);
    methBox.addActionListener(this);
    
    JLabel basisLabel = new JLabel(GT.$("Basis Set: "));
    routeLabels.add(basisLabel);
    basisBox = new JComboBox(BASIS_LIST);
    routeControls.add(basisBox);
    basisBox.setSelectedIndex(1);
    
    
    JLabel dfLabel = new JLabel(GT.$("Density Fitting Basis Set (DFT Only): "));
    routeLabels.add(dfLabel);
    dfBox = new JComboBox(DF_LIST);
    routeControls.add(dfBox);
    dfBox.setSelectedIndex(0);
    
    JLabel optsLabel = new JLabel(GT.$("Job Options: "));
    routeLabels.add(optsLabel);
    optsField = new JTextField(20);
    routeControls.add(optsField);
    optsField.setText(GAUSSIAN_DEFAULT_OPTIONS);
    optsField.setToolTipText(GT.$("Gaussian: route keywords. Psi4: options separated by semicolons, e.g. soscf true; maxiter 200"));
    
    
    
    routePanel.add(routeLabels, BorderLayout.LINE_START);
    routePanel.add(routeControls, BorderLayout.CENTER);
    
    showPanel.add(routePanel, BorderLayout.CENTER);
    
    //molPanel  
    JPanel molPanel = new JPanel(new BorderLayout());
    TitledBorder molTitle =
    BorderFactory.createTitledBorder(GT.$("Molecular Properties"));
    molPanel.setBorder(molTitle);
    
    JPanel molLabels = new JPanel(new GridLayout(3,1));
    JPanel molControls = new JPanel(new GridLayout(3,1));
    
    JLabel chargeLabel = new JLabel(GT.$("Total Charge: "));
    molLabels.add(chargeLabel);
    SpinnerModel chargeModel = new SpinnerNumberModel(0, -10, 10, 1);
    chargeSpinner = new JSpinner(chargeModel);
    chargeSpinner.setEditor(new JSpinner.NumberEditor(chargeSpinner, "#"));
    molControls.add(chargeSpinner);
    
    JLabel multLabel = new JLabel(GT.$("Multiplicity: "));
    molLabels.add(multLabel);
    SpinnerModel multModel = new SpinnerNumberModel(2, 0, 10, 1);
    multSpinner = new JSpinner(multModel);
    multSpinner.setEditor(new JSpinner.NumberEditor(multSpinner, "#"));
    molControls.add(multSpinner);
    
    JLabel selectLabel = new JLabel(GT.$("Selection: "));
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
    
    
    JLabel moleculeNameLabel = new JLabel(GT.$("Molecule Name: "));
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
    
    
    JLabel saveFolderLabel = new JLabel(GT.$("Save Folder Location: "));
    subFileLabels.add(saveFolderLabel);
    saveFolderField = new JTextField(30);
    subFileFields.add(saveFolderField);
    saveFolderField.setText(new File("ISPES").getAbsolutePath());
    setSaveFolderButton = new JButton("Folder...");
    setSaveFolderButton.addActionListener(this);
    subFileButtons.add(setSaveFolderButton);
    
    JLabel loadFolderLabel = new JLabel(GT.$("Load Folder Location: "));
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
    JPanel isosurfaceLabels = new JPanel(new GridLayout(6,1));
    JPanel isosurfaceControls = new JPanel(new GridLayout(6,1));
    
    JLabel computedResolutionLabel = new JLabel(GT.$("isosurface computed resolution: "));
    isosurfaceLabels.add(computedResolutionLabel);
    isosurfaceComputedResolutionField = new JTextField(20);
    
    isosurfaceControls.add(isosurfaceComputedResolutionField);
    isosurfaceComputedResolutionField.setText("1.5");
    
    JLabel computedProbeRadiusLabel = new JLabel(GT.$("isosurface computed probe radius: "));
    isosurfaceLabels.add(computedProbeRadiusLabel);
    isosurfaceComputedProbeRadiusField = new JTextField(20);
    
    isosurfaceControls.add(isosurfaceComputedProbeRadiusField);
    isosurfaceComputedProbeRadiusField.setText("1.2");
    
    JLabel displayedResolutionLabel = new JLabel(GT.$("isosurface displayed resolution: "));
    isosurfaceLabels.add(displayedResolutionLabel);
    isosurfaceDisplayedResolutionField = new JTextField(20);
    
    isosurfaceControls.add(isosurfaceDisplayedResolutionField);
    isosurfaceDisplayedResolutionField.setText("5");
    
    JLabel fragmentLabel = new JLabel(GT.$("fragment: "));
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
    
    JLabel forcesLabel = new JLabel(GT.$("Forces for FlexSurface (Psi4): "));
    isosurfaceLabels.add(forcesLabel);
    forcesBox = new JCheckBox(GT.$("compute gradients"));
    forcesBox.setEnabled(false);
    isosurfaceControls.add(forcesBox);
    
    isosurfacePanel.add(isosurfaceLabels, BorderLayout.LINE_START);
    isosurfacePanel.add(isosurfaceControls, BorderLayout.CENTER);
    
    showPanel.add(isosurfacePanel, BorderLayout.CENTER);
    
    
    
    return showPanel;
  }
  
  private static final String RUN_PSI4_LABEL = "Run with Psi4";
  private static final String STOP_PSI4_LABEL = "Stop";
  
  private JPanel buildPsi4RunPanel() {
    psi4RunPanel = new JPanel(new BorderLayout(5, 2));
    psi4RunPanel.setBorder(BorderFactory.createTitledBorder("Psi4 run"));
    
    psi4RunPanel.add(new JLabel(GT.$("Psi4 command: ")), BorderLayout.LINE_START);
    psi4CommandField = new JTextField(20);
    psi4CommandField.setText(Psi4Runner.findCommand());
    psi4CommandField.setToolTipText(GT.$("psi4 if it is on the PATH, otherwise the full path to it, or e.g. conda run -n psi4 psi4"));
    psi4RunPanel.add(psi4CommandField, BorderLayout.CENTER);
    
    psi4ProgressBar = new JProgressBar();
    psi4ProgressBar.setStringPainted(true);
    psi4ProgressBar.setString(GT.$("not running"));
    psi4RunPanel.add(psi4ProgressBar, BorderLayout.SOUTH);
    return psi4RunPanel;
  }
  
  private JPanel buildButtonPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    
    testAndViewMeshButton = new JButton(GT.$("Test and View Mesh"));
    testAndViewMeshButton.addActionListener(this);
    buttonPanel.add(testAndViewMeshButton);
    
    generateFilesButton = new JButton(GT.$("Generate Files"));
    generateFilesButton.addActionListener(this);
    buttonPanel.add(generateFilesButton);
    
    loadFilesButton = new JButton(GT.$("Load Files"));
    loadFilesButton.addActionListener(this);
    buttonPanel.add(loadFilesButton);
    
    runPsi4Button = new JButton(RUN_PSI4_LABEL);
    runPsi4Button.setToolTipText(GT.$("Generate the files, run them with Psi4 and load the results"));
    runPsi4Button.addActionListener(this);
    buttonPanel.add(runPsi4Button);
    
    cancelButton = new JButton(GT.$("Cancel"));
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
  
  private boolean isPsi4() {
    return PSI4.equals(engineBox.getSelectedItem());
  }
  
  private void engineChanged() {
    boolean psi4 = isPsi4();
    //the listener would otherwise fire while the models are being swapped
    methBox.removeActionListener(this);
    methBox.setModel(new DefaultComboBoxModel(psi4 ? PSI4_METHOD_LIST : METHOD_LIST));
    basisBox.setModel(new DefaultComboBoxModel(psi4 ? PSI4_BASIS_LIST : BASIS_LIST));
    methBox.setEditable(psi4);
    basisBox.setEditable(psi4);
    methBox.setSelectedIndex(0);
    basisBox.setSelectedIndex(psi4 ? 0 : 1);
    methBox.addActionListener(this);
    optsField.setText(psi4 ? "" : GAUSSIAN_DEFAULT_OPTIONS);
    forcesBox.setEnabled(psi4);
    runPsi4Button.setVisible(psi4);
    psi4RunPanel.setVisible(psi4);
    updateUI();
    pack();
  }
  
  private void setRunning(boolean running) {
    runPsi4Button.setText(running ? STOP_PSI4_LABEL : RUN_PSI4_LABEL);
    engineBox.setEnabled(!running);
    testAndViewMeshButton.setEnabled(!running);
    generateFilesButton.setEnabled(!running);
    loadFilesButton.setEnabled(!running);
    psi4CommandField.setEnabled(!running);
  }
  
  private void runWithPsi4() {
    //generates the files, runs them in the background and loads the results
    if (psi4Runner != null) {
      psi4ProgressBar.setString(GT.$("stopping..."));
      psi4Runner.cancel();
      return;
    }
    this.updateVars();
    final File folder = new File(saveFolder);
    if (!folder.isDirectory() && !folder.mkdirs()) {
      JOptionPane.showMessageDialog(this, GT.$("The save folder could not be created:") + "\n" + saveFolder);
      return;
    }
    String command = psi4CommandField.getText();
    if (Psi4Runner.checkCommand(command) == null) {
      JOptionPane.showMessageDialog(this, GT.$("Psi4 could not be started with the command:") + " " + command + "\n"
          + GT.$("Install Psi4, or put the full path to it in the Psi4 command field."));
      return;
    }
    int count = isosurfacePES.generatePsi4files((Viewer)this.viewer,Integer.parseInt(charge),Integer.parseInt(mult),computeResolution,probeRadius,fragment, saveFolder, moleculename,
        mem, Integer.parseInt(proc), methBox.getSelectedItem().toString(), basisBox.getSelectedItem().toString(), optsField.getText(), forcesBox.isSelected());
    if (count <= 0) {
      JOptionPane.showMessageDialog(this, GT.$("No files were written. Check that a molecule is loaded."));
      return;
    }
    List<File> stale = Psi4Runner.staleFiles(folder);
    if (stale.size() > 0) {
      int answer = JOptionPane.showConfirmDialog(this, stale.size() + " "
          + GT.$("job files in the save folder belong to a different surface and would be loaded with this one.") + "\n"
          + GT.$("Delete them and continue?"), GT.$("Run with Psi4"), JOptionPane.OK_CANCEL_OPTION);
      if (answer != JOptionPane.OK_OPTION)
        return;
      for (int i = 0; i < stale.size(); i++)
        stale.get(i).delete();
    }
    
    int threads = Integer.parseInt(proc);
    int parallelJobs = Math.max(1, Runtime.getRuntime().availableProcessors() / threads);
    final Psi4Runner runner = new Psi4Runner(command, folder, threads, parallelJobs);
    psi4Runner = runner;
    psi4ProgressBar.setMaximum(count);
    psi4ProgressBar.setValue(0);
    psi4ProgressBar.setString(GT.$("starting..."));
    setRunning(true);
    
    new SwingWorker<Integer, int[]>() {
      @Override
      protected Integer doInBackground() {
        return Integer.valueOf(runner.run(new Psi4Runner.Listener() {
          public void progress(int finished, int failed, int total, String job) {
            publish(new int[] {finished, failed, total});
          }
        }));
      }
      
      @Override
      protected void process(List<int[]> chunks) {
        int[] latest = chunks.get(chunks.size() - 1);
        psi4ProgressBar.setMaximum(latest[2]);
        psi4ProgressBar.setValue(latest[0] + latest[1]);
        psi4ProgressBar.setString(latest[0] + " / " + latest[2] + " " + GT.$("done")
            + (latest[1] > 0 ? ", " + latest[1] + " " + GT.$("failed") : ""));
      }
      
      @Override
      protected void done() {
        psi4Runner = null;
        setRunning(false);
        int finished = 0;
        try {
          finished = get().intValue();
        } catch (Exception e) {
          JOptionPane.showMessageDialog(IsosurfacePESDialog.this, GT.$("The Psi4 run failed:") + " " + e);
          return;
        }
        List<String> failed = runner.getFailedJobs();
        int total = psi4ProgressBar.getMaximum();
        if (runner.isCancelled()) {
          psi4ProgressBar.setString(GT.$("stopped") + ", " + finished + " / " + total + " " + GT.$("done"));
          return;
        }
        psi4ProgressBar.setString(finished + " / " + total + " " + GT.$("done")
            + (failed.size() > 0 ? ", " + failed.size() + " " + GT.$("failed") : ""));
        if (finished > 0) {
          loadFolderField.setText(saveFolder);
          loadIsosurfacePESfiles();
        }
        if (failed.size() > 0) {
          String names = failed.subList(0, Math.min(5, failed.size())).toString();
          JOptionPane.showMessageDialog(IsosurfacePESDialog.this, failed.size() + " " + GT.$("jobs gave no result, for example") + " " + names + ".\n"
              + GT.$("See their .out files in the save folder. Run again to retry only those."));
        }
      }
    }.execute();
  }
  
  private void updateUI() {
    updateVars();
    if (isPsi4()) {
      basisBox.setEnabled(true);
      dfBox.setEnabled(false);
      return;
    }
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
    if (isPsi4()) {
      int count = isosurfacePES.generatePsi4files((Viewer)this.viewer,Integer.parseInt(charge),Integer.parseInt(mult),computeResolution,probeRadius,fragment, saveFolder, moleculename,
          mem, Integer.parseInt(proc), methBox.getSelectedItem().toString(), basisBox.getSelectedItem().toString(), optsField.getText(), forcesBox.isSelected());
      if (count < 0)
        JOptionPane.showMessageDialog(this, GT.$("No files were written. Check that a molecule is loaded and that the save folder exists."));
      else
        JOptionPane.showMessageDialog(this, count + " " + GT.$("Psi4 input files written. Run them with") + " " + Psi4Engine.RUN_SCRIPT_NAME);
      return;
    }
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
    if (psi4Runner != null)
      psi4Runner.cancel();
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
    } else if (event.getSource() == runPsi4Button) {
      runWithPsi4();
    } else if (event.getSource() == engineBox) {
      engineChanged();
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