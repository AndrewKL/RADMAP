package org.openscience.jmol.app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.ActionManager;
import org.jmol.viewer.Viewer;

public class FragmentPanel extends JPanel {

  // Main application
  //public static void main(String[] args) {
  //  new Test2(strXyzHOH);
  //}
  
  /**
   * 
   */
  private final ModelKitSideMenu FragmentPanel;
  public Viewer fragmentViewer;
  private JmolAdapter adapter;
  private Dimension currentSize = new Dimension(250,250);

  public FragmentPanel(ModelKitSideMenu modelKitSideMenu) {
    FragmentPanel = modelKitSideMenu;
    adapter = new SmarterJmolAdapter();
    fragmentViewer = (Viewer)JmolViewer.allocateViewer(this, adapter);
    setPreferredSize(currentSize);
    fragmentViewer.setPickingMode(null,ActionManager.PICKING_LOCATION_FOR_ADDITION);
    fragmentViewer.runScriptImmediately("selectionhalos on");
    //JFrame newFrame = new JFrame();
    //newFrame.getContentPane().add(this);
    //setSize(300, 300);
    //newFrame.setVisible(true);
  }

  public FragmentPanel(ModelKitSideMenu modelKitSideMenu, String model) {
    FragmentPanel = modelKitSideMenu;
    adapter = new SmarterJmolAdapter();
    fragmentViewer = (Viewer)JmolViewer.allocateViewer(this, adapter);
    setPreferredSize(currentSize);
    fragmentViewer.loadInline(model);
    fragmentViewer.setPickingMode(null,ActionManager.PICKING_LOCATION_FOR_ADDITION);
    fragmentViewer.runScriptImmediately("selectionhalos on");
  }
  
  public void setFragment(String model){
    fragmentViewer.loadInline(model);
  }
  

  public final static String H2O = 
      "3\n" +
      "water\n" +
      "O  0.0 0.0 0.0\n" +
      "H  0.76923955 -0.59357141 0.0\n" +
      "H -0.76923955 -0.59357141 0.0\n";
  
  public final static String NH3 = 
      "4\n" +
          "NH3\n"+
          "N    -0.00016   -0.00016   -0.00016\n"+
          "H    -0.63516   -0.63516    0.63484\n"+
          "H    -0.63516    0.63484   -0.63516\n"+
          "H     0.63484   -0.63516   -0.63516\n";
  
  





  
  

  

    @Override
    public void paint(Graphics g) {
      getSize(currentSize);
      fragmentViewer.renderScreenImage(g, currentSize.width, currentSize.height);
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