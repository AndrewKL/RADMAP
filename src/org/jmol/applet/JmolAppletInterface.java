/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-02-07 08:42:17 -0400 (Tue, 07 Feb 2012) $
 * $Revision: 16745 $
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
package org.jmol.applet;

import org.jmol.api.JmolScriptInterface;

import netscape.javascript.JSObject;

/**
 * This is the API of methods that are available to JavaScript
 * via LiveConnect to the Jmol applet.
 * 
 * DONT FORGET TO ADD THESE FUNCTIONS TO src/JmolApplet.java !!!
 * 
 */

public interface JmolAppletInterface extends JmolScriptInterface {

  public String getPropertyAsString(String infoType);
  public String getPropertyAsString(String infoType, String paramInfo);
  public String getPropertyAsJSON(String infoType);
  public String getPropertyAsJSON(String infoType, String paramInfo);
  public Object getProperty(String infoType);
  public Object getProperty(String infoType, String paramInfo);
  public String loadInlineString(String strModel, String script, boolean isAppend);
  public String loadInlineArray(String[] strModels, String script, boolean isAppend);
  public String loadNodeId(String nodeId);
  public String loadDOMNode(JSObject DOMNode);

  // Note -- some Macintosh-based browsers cannot distinguish methods
  // with the same name but with different method signatures
  // so the following are not reliable and are thus deprecated
  
  /**
   * @deprecated
   * @param strModel
   * @return         error or null
   */
  @Deprecated
  public String loadInline(String strModel);

  /**
   * @deprecated
   * @param strModels
   * @return         error or null
   */ 
  @Deprecated
  public String loadInline(String[] strModels);

  /**
   * @deprecated
   * @param strModel
   * @param script
   * @return         error or null
   */
  @Deprecated
  public String loadInline(String strModel, String script);

  /**
   * @deprecated
   * @param strModels
   * @param script
   * @return         error or null
   */
  @Deprecated
  public String loadInline(String[] strModels, String script);
}
