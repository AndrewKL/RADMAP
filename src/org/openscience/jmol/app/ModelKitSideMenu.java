package org.openscience.jmol.app;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.ActionManager;
import org.jmol.viewer.Viewer;

/* $RCSfile$
 * $Author$ Andrew Long Andrew.long.3001@gmail.com
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2011  Andrew Long
 *
 * Contact: jmol-developers@lists.sf.net or me(Andrew) for questions about this class
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */

/**
 * 
 */
public class ModelKitSideMenu extends JPanel {

  /**
   * 
   */
  public static boolean DEBUG = true;
  public FragmentPanel fragmentPanel;
  public Viewer viewer;
  
  
  
  public ModelKitSideMenu(JmolViewer viewer2) {
    this.setVisible(false);
    viewer = (Viewer)viewer2;
    
    
    
    JPanel interiorPanel = new JPanel();
    
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[]{0, 0};
    gridBagLayout.rowHeights = new int[]{320, 0, 0, 0};
    gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
    gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
        
    interiorPanel.setLayout(gridBagLayout);
    
    fragmentPanel = new FragmentPanel(this, FragmentPanel.H2O);
    
     
    GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.insets = new Insets(0, 0, 5, 0);
    gbc_panel.gridx = 0;
    gbc_panel.gridy = 0;
    gbc_panel.anchor = GridBagConstraints.NORTHWEST;
    
    interiorPanel.add(fragmentPanel, gbc_panel);
    
    JTabbedPane tabbedPane = new JTabbedPane();
    
     
    JComponent panel1 = makePBlockPanel();
    tabbedPane.addTab("Organic", null, panel1, "Organic Panel");
    tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
     
    JComponent panel2 = makeTextPanel("Biochem");
    tabbedPane.addTab("Biochem", null, panel2, "Biochem Panel");
    tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
     
    JComponent panel3 = makeTextPanel("Inorganic");
    tabbedPane.addTab("Inorganic", null, panel3, "Inorganic Panel");
    tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
    
        
    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
    gbc_panel_1.insets = new Insets(0, 0, 5, 0);
    gbc_panel_1.gridx = 0;
    gbc_panel_1.gridy = 1;
    gbc_panel_1.anchor = GridBagConstraints.NORTH;
    interiorPanel.add(tabbedPane, gbc_panel_1);
    
    
    GridBagConstraints gbc_panel_2 = new GridBagConstraints();
    gbc_panel_2.gridx = 0;
    gbc_panel_2.gridy = 2;
    gbc_panel_2.anchor = GridBagConstraints.NORTH;
    interiorPanel.add(makeSetBondPanel(), gbc_panel_2);
    
    GridBagConstraints gbc_panel_3 = new GridBagConstraints();
    gbc_panel_3.gridx = 0;
    gbc_panel_3.gridy = 3;
    gbc_panel_3.anchor = GridBagConstraints.NORTH;
    interiorPanel.add(makeModifySystemPanel(), gbc_panel_3);
     
    //The following line enables to use scrolling tabs.
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    
    setLayout(new FlowLayout());
    add(interiorPanel);
    

  }
  
  protected JComponent makePBlockPanel() {
    JPanel panel = new JPanel(new GridLayout(2,5));
    //panel.setSize(300, 300);
    TitledBorder linkTitle = BorderFactory.createTitledBorder("Add Atom");
    panel.setBorder(linkTitle);
    
    JButton bButton = new JButton("B");
    bButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("bButton");
        viewer.setPickingMode("assignAtom_B",-1);
      }
    });
    panel.add(bButton);
    
    JButton cButton = new JButton("C");
    cButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("cButton");
        viewer.setPickingMode("assignAtom_C",-1);
      }
    });
    panel.add(cButton);
    

    
    JButton nButton = new JButton("N");
    nButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("bButton");
        viewer.setPickingMode("assignAtom_N",-1);
        fragmentPanel.setFragment(FragmentPanel.NH3);
      }
    });
    panel.add(nButton);
    
    JButton oButton = new JButton("O");
    oButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("oButton");
        viewer.setPickingMode("assignAtom_O",-1);
        fragmentPanel.setFragment(FragmentPanel.H2O);
      }
    });
    panel.add(oButton);
    
    JButton flButton = new JButton("Fl");
    flButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("flButton");
        viewer.setPickingMode("assignAtom_f",-1);
      }
    });
    panel.add(flButton);
    
    JButton alButton = new JButton("Al");
    alButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("alButton");
        viewer.setPickingMode("assignAtom_Al",-1);
      }
    });
    panel.add(alButton);
    
    JButton siButton = new JButton("Si");
    siButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("siButton");
        viewer.setPickingMode("assignAtom_Si",-1);
      }
    });
    panel.add(siButton);
    
    JButton pButton = new JButton("P");
    pButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("pButton");
        viewer.setPickingMode("assignAtom_P",-1);
      }
    });
    panel.add(pButton);
    
    JButton sButton = new JButton("S");
    sButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("sButton");
        viewer.setPickingMode("assignAtom_S",-1);
      }
    });
    panel.add(sButton);
    
    JButton clButton = new JButton("Cl");
    clButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("clButton");
        viewer.setPickingMode("assignAtom_Cl",-1);
      }
    });
    panel.add(clButton);
    
    return panel;
  }
  
  protected JComponent makeSetBondPanel(){
    JPanel panel = new JPanel(new GridLayout(0,3));
    TitledBorder linkTitle = BorderFactory.createTitledBorder("Set Bond");
    panel.setBorder(linkTitle);
    
    JButton singleBondButton = new JButton("Single");
    panel.add(singleBondButton);
    singleBondButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("singleBondButton");
        viewer.setPickingMode("assignBond_1",-1);
      }
    });
    
    JButton doubleBondButton = new JButton("Double");
    panel.add(doubleBondButton);
    doubleBondButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("doubleBondButton");
        viewer.setPickingMode("assignBond_2",-1);
      }
    });
    
    JButton tripleBondButton = new JButton("Triple");
    panel.add(tripleBondButton);
    tripleBondButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("tripleBondButton");
        viewer.setPickingMode("assignBond_3",-1);
      }
    });
    
    
    return panel;
    
    
    
  }
  
  protected JComponent makeModifySystemPanel(){
    JPanel panel = new JPanel(new GridLayout(4,0));
    //panel.setSize(300, 300);
    TitledBorder linkTitle = BorderFactory.createTitledBorder("Modify Current System");
    panel.setBorder(linkTitle);
    
    /*JButton modBondLengthButton = new JButton("Modify Bond Length");
    panel.add(modBondLengthButton);
    
    JButton modAngleButton = new JButton("Modify Angle");
    panel.add(modAngleButton);
    
    JButton modDihedralButton = new JButton("Modify Dihedral");
    panel.add(modDihedralButton);*/
    
    JButton deleteAtom = new JButton("Delete Atom");
    panel.add(deleteAtom);
    deleteAtom.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("Delete Button Push");
        viewer.setPickingMode(null,ActionManager.PICKING_DELETE_ATOM);
      }
    });
    
    JButton dragButton = new JButton("Drag");
    panel.add(dragButton);
    dragButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("Delete Button Push");
        viewer.setPickingMode(null,ActionManager.PICKING_DRAG_ATOM);
      }
    });
    
    
    JButton dragAndMinButton = new JButton("Drag And Minimize");
    panel.add(dragAndMinButton);
    dragAndMinButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("Delete Button Push");
        viewer.setPickingMode(null,ActionManager.PICKING_DRAG_MINIMIZE);
      }
    });
    
    JButton minimizeButton = new JButton("Minimize");
    panel.add(minimizeButton);
    minimizeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(DEBUG)System.out.println("minimizebutton");
        viewer.minimize(Integer.MAX_VALUE, 0, null, null, 0, false, false, false);
      }
    });
    
    
    
    return panel;
    
  }
  
  protected JComponent makeTextPanel(String text) {
    JPanel panel = new JPanel(false);
    JLabel filler = new JLabel(text);
    filler.setHorizontalAlignment(SwingConstants.CENTER);
    panel.setLayout(new GridLayout(1, 1));
    panel.add(filler);
    return panel;
  }

 
  
  private static void createAndShowGUI() {
    
    JFrame frame = new JFrame("test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Create and set up the content pane.
    JComponent newContentPane = new ModelKitSideMenu(null);
    newContentPane.setOpaque(true); //content panes must be opaque
    frame.setContentPane(newContentPane);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }
  
  
  public static void main(String[] args) {
    
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        @SuppressWarnings("synthetic-access")
        public void run() {
            createAndShowGUI();
        }
    });
  }
  
  @Override
  public void paint(Graphics g) {
    //System.out.println("sidemenu paint: "+viewer.getModelkitMode());
    this.setVisible(viewer.getModelkitMode());//not the only point at which this is toggled.
    super.paint(g);
  }
  


}
