/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-02-08 01:15:11 -0400 (Wed, 08 Feb 2012) $
 * $Revision: 16759 $
 *
 * Copyright (C) 2004-2005  The Jmol Development Team
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

/**
 * This class only exists so that people can declare
 * JmolApplet in applet tags without having to give a full package
 * specification
 *
 * see org.jmol.applet.Jmol
 *
 */


import org.jmol.api.JmolSyncInterface;
import org.jmol.applet.AppletWrapper;
import org.jmol.applet.JmolAppletInterface;

import netscape.javascript.JSObject;

public class JmolApplet extends AppletWrapper implements
    JmolAppletInterface {

  //protected void finalize() throws Throwable {
  //  System.out.println("JmolApplet finalize " + this);
  //  super.finalize();
  //}
  
  public JmolApplet() {
    super("org.jmol.applet.Jmol", "jmol75x29x8.gif", 3, preloadClasses);
    //System.out.println("JmolApplet constructor " + this);
    //BH focus test: this.setFocusable(false);
  }

  private final static String[] preloadClasses = { 
    "org.jmol.viewer.JmolConstants",                // 1b 
    "org.jmol.g3d.Graphics3D",                      // 1c
    "org.jmol.modelset.Atom",                       // 1d
    "org.jmol.util.Escape",                         // 1e
    "org.jmol.adapter.smarter.SmarterJmolAdapter",  // 1f 
    //"org.jmol.popup.JmolPopup", 
    };

  public String getPropertyAsString(String infoType) {
    return (wrappedApplet == null ? null : ""
        + wrappedApplet.getPropertyAsString("" + infoType));
  }

  public String getPropertyAsString(String infoType, String paramInfo) {
    return (wrappedApplet == null ? null : ""
        + wrappedApplet.getPropertyAsString("" + infoType, "" + paramInfo));
  }

  public String getPropertyAsJSON(String infoType) {
    return (wrappedApplet == null ? null : ""
        + wrappedApplet.getPropertyAsJSON("" + infoType));
  }

  public String getPropertyAsJSON(String infoType, String paramInfo) {
    return (wrappedApplet == null ? null : ""
        + wrappedApplet.getPropertyAsJSON("" + infoType, "" + paramInfo));
  }

  public Object getProperty(String infoType) {
    return (wrappedApplet == null ? null : wrappedApplet.getProperty(""
        + infoType));
  }

  public Object getProperty(String infoType, String paramInfo) {
    return (wrappedApplet == null ? null : wrappedApplet.getProperty(""
        + infoType, "" + paramInfo));
  }

  public String loadInlineArray(String[] strModels, String script, boolean isAppend) {
    if (wrappedApplet == null || strModels == null || strModels.length == 0)
        return null;
      String s = "" + strModels[0];
      if (s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
        String[] converted = new String[strModels.length];
        for (int i = 0; i < strModels.length; ++i)
          converted[i] = "" + strModels[i];
        return wrappedApplet.loadInlineArray(converted, "" + script, isAppend);
      }
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < strModels.length; ++i)
        sb.append(strModels[i]).append('\n');
      return wrappedApplet.loadInlineString(sb.toString(), "" + script, isAppend);
  }

  public String loadInlineString(String strModel, String script, boolean isAppend) {
    return (wrappedApplet == null ? null :
      wrappedApplet.loadInlineString("" + strModel, "" + script, isAppend));
  }

  // bizarre Mac OS X / Java bug:
  // Mac cannot differentiate between signatures String and String[]
  // so, instead, we deprecate these and go for the above two methods only.

  /**
   * @deprecated
   * @param strModel
   * @return         error or null
   */
  @Deprecated
  public String loadInline(String strModel) {
    return (wrappedApplet == null ? null :
      wrappedApplet.loadInline("" + strModel));
  }

  /**
   * @deprecated
   * @param strModel
   * @param script
   * @return         error or null
   */
  @Deprecated
  public String loadInline(String strModel, String script) {
    return (wrappedApplet == null ? null :
      wrappedApplet.loadInline("" + strModel, "" + script));
  }

  /**
   * @deprecated
   * @param strModels
   * @return         error or null
   */
  @Deprecated
  public String loadInline(String[] strModels) {
    return (wrappedApplet == null ? null :
      wrappedApplet.loadInline(strModels));
  }

  /**
   * @deprecated
   * @param strModels
   * @param script
   * @return         error or null
   */
  @Deprecated
  public String loadInline(String[] strModels, String script) {
    return (wrappedApplet == null ? null :
      wrappedApplet.loadInline(strModels, script));
  }

  public String loadNodeId(String nodeId) {
    return (wrappedApplet == null ? null :
      wrappedApplet.loadNodeId("" + nodeId));
  }

  public String loadDOMNode(JSObject DOMNode) {
    return (wrappedApplet == null ? null : wrappedApplet.loadDOMNode(DOMNode));
  }

  public void script(String script) {
    //System.out.println("JmolApplet script test " + script + " " + wrappedApplet);
    if (wrappedApplet != null)
      wrappedApplet.script("" + script);
  }

  public void syncScript(String script) {
    if (wrappedApplet != null)
      wrappedApplet.syncScript("" + script);
  }

  public Object setStereoGraphics(boolean isStereo) {
    return (wrappedApplet == null ? null : 
        wrappedApplet.setStereoGraphics(isStereo));
  }

  public String scriptNoWait(String script) {
    if (wrappedApplet != null)
      return "" + (wrappedApplet.scriptNoWait("" + script));
    return null;
  }

  public String scriptCheck(String script) {
    if (wrappedApplet != null)
      return "" + (wrappedApplet.scriptCheck("" + script));
    return null;
  }

  public String scriptWait(String script) {
    if (wrappedApplet != null)
      return "" + (wrappedApplet.scriptWait("" + script));
    return null;
  }

  public String scriptWait(String script, String statusParams) {
    if (statusParams == null)
      statusParams = "";
    if (wrappedApplet != null)
      return "" + (wrappedApplet.scriptWait("" + script, statusParams));
    return null;
  }
  
  public String scriptWaitOutput(String script) {
    if (wrappedApplet != null)
      return "" + (wrappedApplet.scriptWaitOutput("" + script));
    return null;
  }

  public void registerApplet(String id, String fullName) {
    // does not work looking for JSVApplet!
    JmolSyncInterface applet = (JmolSyncInterface) getAppletContext().getApplet(id);
    if (applet == null)
      System.out.println("could not find " + id);
    register(fullName, applet);
  }
  
  public void register(String id, JmolSyncInterface jsi) {
    if (wrappedApplet != null)
      wrappedApplet.register(id, jsi);
  }

}
