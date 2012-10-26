package org.openscience.jmol.app.jmolpanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.GridLayout;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JTextArea;

import org.jmol.i18n.GT;
import org.jmol.util.ColorIndexUtil;
import org.jmol.util.DEDXSurfaceUtils;

import org.jmol.viewer.Viewer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.border.TitledBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;



public class FlexSurfaceDialog extends JDialog implements ActionListener, ChangeListener {
  JDialog selfreference;
  JTextField resolutionTextField;
  JTextField loadLocationTextField;
  float vectorParamA = (float) 2.0;
  float vectorParamC =(float) 1.0 ;
  
  JTextField aParamTextField;
  JTextField cParamTextField;
  
  //private int colorscheme;
  //private String resolution;
  Viewer viewer;
  
 private static final boolean DEBUG = true;
  

  
  
  
  int selectedColorScheme = ColorIndexUtil.ROYGB;
  

  
  
  
  public FlexSurfaceDialog(JFrame f,Viewer incviewer) {
    
    super(f, false);
    setTitle(GT._("Flex Surface"));
    
    viewer = incviewer;
    selfreference=this;
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[]{382, 0};
    gridBagLayout.rowHeights = new int[]{38, 79, 79, 91, 0};
    gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
    gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    getContentPane().setLayout(gridBagLayout);
    
    JPanel folderSelectPanel = new JPanel();
    GridBagConstraints gbc_folderSelectPanel = new GridBagConstraints();
    gbc_folderSelectPanel.fill = GridBagConstraints.BOTH;
    gbc_folderSelectPanel.insets = new Insets(0, 0, 5, 0);
    gbc_folderSelectPanel.gridx = 0;
    gbc_folderSelectPanel.gridy = 0;
    getContentPane().add(folderSelectPanel, gbc_folderSelectPanel);
    
    loadLocationTextField = new JTextField();
    loadLocationTextField.setColumns(20);
    folderSelectPanel.add(loadLocationTextField);
    
    
    JButton btnSelectLoadFolder = new JButton("Select Load Folder");
    btnSelectLoadFolder.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        JFileChooser fileChooser = new JFileChooser();
        
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int ierr = fileChooser.showDialog(selfreference, "Set");
        if (ierr == JFileChooser.APPROVE_OPTION) {
          File file = fileChooser.getSelectedFile();
          loadLocationTextField.setText(file.getAbsolutePath());
        }
      }
    });
    folderSelectPanel.add(btnSelectLoadFolder);
    
    JPanel configPanel = new JPanel();
    configPanel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    GridBagConstraints gbc_configPanel = new GridBagConstraints();
    gbc_configPanel.fill = GridBagConstraints.BOTH;
    gbc_configPanel.insets = new Insets(0, 0, 5, 0);
    gbc_configPanel.gridx = 0;
    gbc_configPanel.gridy = 1;
    getContentPane().add(configPanel, gbc_configPanel);
    configPanel.setLayout(new GridLayout(0, 2, 0, 0));
    
    JLabel lblDisplaySurfaceResolution = new JLabel("Display Surface Resolution");
    configPanel.add(lblDisplaySurfaceResolution);
    
    resolutionTextField = new JTextField();
    resolutionTextField.setText("5.0");
    configPanel.add(resolutionTextField);
    resolutionTextField.setColumns(10);
    
    JLabel lblColorScheme = new JLabel("Color Scheme");
    configPanel.add(lblColorScheme);
    
    final JComboBox colorSchemeComboBox = new JComboBox(ColorIndexUtil.COLOR_SCHEME_LIST);
    colorSchemeComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        selectedColorScheme=ColorIndexUtil.colorSchemeNameToInt(ColorIndexUtil.COLOR_SCHEME_LIST[colorSchemeComboBox.getSelectedIndex()]);
      }
    });
    configPanel.add(colorSchemeComboBox);
    
    JPanel vectorParamPanel = createVectorParamPanel();
    vectorParamPanel.setLayout(new GridLayout(0, 2, 0, 0));
    vectorParamPanel.setBorder(new TitledBorder(null, "Vector Weighting", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    GridBagConstraints gbc_vectorParam = new GridBagConstraints();
    gbc_vectorParam.fill = GridBagConstraints.BOTH;
    gbc_vectorParam.insets = new Insets(0, 0, 5, 0);
    gbc_vectorParam.gridx = 0;
    gbc_vectorParam.gridy = 2;
    getContentPane().add(vectorParamPanel, gbc_vectorParam);
    
    
    
    JPanel buttonPanel = new JPanel();
    GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
    gbc_buttonPanel.fill = GridBagConstraints.BOTH;
    gbc_buttonPanel.gridx = 0;
    gbc_buttonPanel.gridy = 3;
    getContentPane().add(buttonPanel, gbc_buttonPanel);
    buttonPanel.setLayout(new BorderLayout(2, 4));
    
    JPanel panel = new JPanel();
    buttonPanel.add(panel, BorderLayout.NORTH);
    
    JButton btnLoadSurface = new JButton("Load Surface");
    btnLoadSurface.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if(DEBUG)System.out.println("loading flex surface files A: "+vectorParamA+"  C: "+vectorParamC);
        updateParams();
        
        DEDXSurfaceUtils.loadFlexSurface(viewer, loadLocationTextField.getText(), resolutionTextField.getText(),selectedColorScheme, vectorParamA, vectorParamC);
      }
    });
    
    JButton btnLoadTotalDedx = new JButton("Load Total DEDX surface");
    btnLoadTotalDedx.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if(DEBUG)System.out.println("loading DEDX surface files A: "+vectorParamA+"  C: "+vectorParamC);
        updateParams();
        
        DEDXSurfaceUtils.loadTotalDEDXSurface(viewer, loadLocationTextField.getText(), resolutionTextField.getText(),selectedColorScheme);
        
      }
    });
    panel.add(btnLoadTotalDedx);
    panel.add(btnLoadSurface);
    
    JButton btnCancel = new JButton("Cancel");
    btnCancel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        dispose();
      }
    });
    panel.add(btnCancel);
    
    JTextArea txtrReferenceInfo = new JTextArea();
    txtrReferenceInfo.setLineWrap(true);
    txtrReferenceInfo.setRows(3);
    txtrReferenceInfo.setColumns(3);
    txtrReferenceInfo.setText("REF: Andrew K. Long, Jason A.C. Clyburne \"RADMAP: An isosurface potential energy surface program\" 2012 (TO BE UPDATED)");
    buttonPanel.add(txtrReferenceInfo);
    
    this.pack();
  }
  
  private JPanel createVectorParamPanel(){
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(0, 2, 0, 0));
    
    JLabel aParamLabel = new JLabel("A");
    panel.add(aParamLabel);
    
    aParamTextField = new JTextField();
    
    aParamTextField.setText("2.0");
    panel.add(aParamTextField);
    aParamTextField.setColumns(10);
    
    JLabel cParamLabel = new JLabel("C");
    panel.add(cParamLabel);
    
    cParamTextField = new JTextField();
    
    cParamTextField.setText("0.5");
    panel.add(cParamTextField);
    cParamTextField.setColumns(10);
    
    return panel;
  }
  
  @SuppressWarnings("boxing") 
  void updateParams(){
    vectorParamC = Float.valueOf(cParamTextField.getText());
    vectorParamA = Float.valueOf(aParamTextField.getText());
  }

  public void stateChanged(ChangeEvent e) {
    // TODO
    
  }

  public void actionPerformed(ActionEvent e) {
    // TODO
    
  }
  
  

  

}
