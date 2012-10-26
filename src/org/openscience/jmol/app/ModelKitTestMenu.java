package org.openscience.jmol.app;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

//import org.jmol.Test2;
//import org.jmol.Test2.AppCloser;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolViewer;
import org.openscience.jmol.app.jmolpanel.JmolPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/* $RCSfile$
 * $Author$ Andrew Long Andrew.long.3001@gmail.com
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2011  The Jmol Development Team
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */

/**
 * 
 */
public class ModelKitTestMenu extends JPanel {

  /**
   * 
   */
  
  public FragmentPanel fragmentPanel;
  public ModelKitTestMenu() {
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[]{0, 0};
    gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
    gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
    gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
    setLayout(gridBagLayout);
    
    fragmentPanel = new FragmentPanel(FragmentPanel.strXyzHOH);
    //fragmentPanel.setSize(300, 300);
     
    GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.insets = new Insets(0, 0, 5, 0);
    gbc_panel.gridx = 0;
    gbc_panel.gridy = 0;

    add(fragmentPanel, gbc_panel);
    
    JTabbedPane tabbedPane = new JTabbedPane();
    //ImageIcon icon = createImageIcon("images/middle.gif");
     
    JComponent panel1 = makePBlockPanel();
    tabbedPane.addTab("Organic", null, panel1, "Does nothing");
    tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
     
    JComponent panel2 = makeTextPanel("Biochem");
    tabbedPane.addTab("Biochem", null, panel2, "nothing");
    tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
     
    JComponent panel3 = makeTextPanel("Inorganic");
    tabbedPane.addTab("Inorganic", null, panel3, "Still does nothing");
    tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
     
    JComponent panel4 = makeTextPanel(
            "Panel #4 (has a preferred size of 410 x 50).");
    panel4.setPreferredSize(new Dimension(300, 50));
       
        
    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
    gbc_panel_1.insets = new Insets(0, 0, 5, 0);
    gbc_panel_1.gridx = 0;
    gbc_panel_1.gridy = 1;
    add(tabbedPane, gbc_panel_1);
    
    
    GridBagConstraints gbc_panel_2 = new GridBagConstraints();
    gbc_panel_2.anchor = GridBagConstraints.NORTHWEST;
    gbc_panel_2.gridx = 0;
    gbc_panel_2.gridy = 2;
    add(makeModifySystemPanel(), gbc_panel_2);
     
    //The following line enables to use scrolling tabs.
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    
    // TODO
  }
  
  protected JComponent makePBlockPanel() {
    JPanel panel = new JPanel(new GridLayout(2,5));
    //panel.setSize(300, 300);
    TitledBorder linkTitle = BorderFactory.createTitledBorder("Add Atom");
    panel.setBorder(linkTitle);
    
    JButton bButton = new JButton("B");
    bButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
      }
    });
    panel.add(bButton);
    
    JButton cButton = new JButton("C");
    panel.add(cButton);
    
    JButton nButton = new JButton("N");
    panel.add(nButton);
    
    JButton oButton = new JButton("O");
    panel.add(oButton);
    
    JButton flButton = new JButton("Fl");
    panel.add(flButton);
    
    JButton alButton = new JButton("Al");
    panel.add(alButton);
    
    JButton siButton = new JButton("Si");
    panel.add(siButton);
    
    JButton pButton = new JButton("P");
    panel.add(pButton);
    
    JButton sButton = new JButton("S");
    panel.add(sButton);
    
    JButton clButton = new JButton("cl");
    panel.add(clButton);
    
    return panel;
  }
  
  protected JComponent makeModifySystemPanel(){
    JPanel panel = new JPanel(new GridLayout(3,1));
    panel.setSize(300, 300);
    TitledBorder linkTitle = BorderFactory.createTitledBorder("Modify Current System");
    panel.setBorder(linkTitle);
    
    JButton modBondLengthButton = new JButton("Modify Bond Length");
    panel.add(modBondLengthButton);
    
    JButton modAngleButton = new JButton("Modify Angle");
    panel.add(modAngleButton);
    
    JButton modDihedralButton = new JButton("Modify Dihedral");
    panel.add(modDihedralButton);
    
    JButton deleteAtom = new JButton("Delete Atom");
    deleteAtom.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      }
    });
    panel.add(deleteAtom);
    
    JButton dragAtomAndMin = new JButton("Drag Atom And Min");
    panel.add(dragAtomAndMin);
    
    
    
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

 
  
  public class FragmentPanel extends JPanel {

    // Main application
    //public static void main(String[] args) {
    //  new Test2(strXyzHOH);
    //}
    
    private JmolViewer viewer;
    private JmolAdapter adapter;
    private Dimension currentSize = new Dimension(300,300);

    public FragmentPanel() {
      adapter = new SmarterJmolAdapter();
      viewer = JmolViewer.allocateViewer(this, adapter);
      //JFrame newFrame = new JFrame();
      //newFrame.getContentPane().add(this);
      //setSize(300, 300);
      //newFrame.setVisible(true);
    }

    public FragmentPanel(String model) {
      adapter = new SmarterJmolAdapter();
      viewer = JmolViewer.allocateViewer(this, adapter);
      //JFrame newFrame = new JFrame();
      //newFrame.getContentPane().add(this);
      this.setSize(300, 300);
      //newFrame.setVisible(true);
      //newFrame.addWindowListener(new AppCloser());
      viewer.loadInline(model);
    }

    public final static String strXyzHOH = 
        "3\n" +
        "water\n" +
        "O  0.0 0.0 0.0\n" +
        "H  0.76923955 -0.59357141 0.0\n" +
        "H -0.76923955 -0.59357141 0.0\n";

    

      @Override
      public void paint(Graphics g) {
        getSize(currentSize);
        viewer.renderScreenImage(g, currentSize.width, currentSize.height);
      }

      /**
       * To shutdown when run as an application.  This is a
       * fairly lame implementation.   A more self-respecting
       * implementation would at least check to see if a save
       * was needed.
       */
      protected final class AppCloser extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
          System.exit(0);
      }
    }

  }
  
  private static void createAndShowGUI() {
    
    JFrame frame = new JFrame("test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Create and set up the content pane.
    JComponent newContentPane = new ModelKitTestMenu();
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

}
