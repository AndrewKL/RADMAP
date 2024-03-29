/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-09-25 15:50:06 -0300 (Sun, 25 Sep 2011) $
 * $Revision: 16115 $
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

import org.jmol.util.Logger;

class WrappedAppletLoader extends Thread {

  private AppletWrapper appletWrapper;
  private String wrappedAppletClassName;

  //private final static int minimumLoadSeconds = 0;

  WrappedAppletLoader(AppletWrapper appletWrapper, String wrappedAppletClassName) {
    this.appletWrapper = appletWrapper;
    this.wrappedAppletClassName = wrappedAppletClassName;
  }

  @Override
  public void run() {
    long startTime = System.currentTimeMillis();
    if (Logger.debugging) {
      Logger.debug("WrappedAppletLoader.run(" + wrappedAppletClassName + ")");
    }
    TickerThread tickerThread = new TickerThread(appletWrapper);
    tickerThread.start();
    WrappedApplet wrappedApplet = null;
    try {
      Class<?> wrappedAppletClass = Class.forName(wrappedAppletClassName);
      wrappedApplet = (WrappedApplet) wrappedAppletClass.newInstance();
      wrappedApplet.setAppletWrapper(appletWrapper);
      wrappedApplet.init();
    } catch (Exception e) {
      Logger.error("Could not instantiate wrappedApplet class"
          + wrappedAppletClassName, e);
    }
    long loadTimeSeconds = (System.currentTimeMillis() - startTime + 500) / 1000;
    if (Logger.debugging) {
      Logger.debug(wrappedAppletClassName + " load time = " + loadTimeSeconds
          + " seconds");
    }
    tickerThread.keepRunning = false;
    tickerThread.interrupt();
    appletWrapper.wrappedApplet = wrappedApplet;
    appletWrapper.repaint();
    wrappedApplet.jmolReady();
  }
}

class TickerThread extends Thread {
  AppletWrapper appletWrapper;
  boolean keepRunning = true;

  TickerThread(AppletWrapper appletWrapper) {
    this.appletWrapper = appletWrapper;
    this.setName("AppletLoaderTickerThread");
  }

  @Override
  public void run() {
    do {
      try {
        Thread.sleep(999);
      } catch (InterruptedException ie) {
        break;
      }
      appletWrapper.repaintClock();
    } while (keepRunning);
  }
}
