/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2006-03-15 07:52:29 -0600 (Wed, 15 Mar 2006) $
 * $Revision: 4614 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
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

package org.jmol.adapter.readers.more;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3f;

import org.jmol.adapter.smarter.AtomSetCollectionReader;
import org.jmol.util.Escape;
import org.jmol.util.Logger;
import org.jmol.util.Parser;

/*
 * TLS output reader -- data only; no atoms
 *  
 */

public class TlsDataOnlyReader extends AtomSetCollectionReader {

  private List<Map<String, Object>> vTlsModels;
  private StringBuffer sbTlsErrors;
  private int tlsGroupID;

  @Override
  protected void initializeReader() throws Exception {
    readTlsData();
    continuing = false;
  }
  
  //  ## REFMAC ORDER: t11 t22 t33 t12 t13 t23
  //  ## REFMAC ORDER: l11 l22 l33 l12 l13 l23
  //  ## REFMAC ORDER: <S22 - S11> <S11 - S33> <S12> <S13> <S23> <S21> <S31> <S32>

  private final static String[] TLnn = new String[] { "11", "22", "33", "12", "13", "23" };
  private final static String[] Snn = new String[] { "22", "11", "12", "13", "23", "21", "31", "32" };

  private void readTlsData() throws Exception {
    vTlsModels = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> tlsGroups;
    Map<String, Object> tlsGroup = null;
    List<Map<String, Object>> ranges = null;
    Map<String, Object> range = null;
    tlsGroups = new ArrayList<Map<String, Object>>();
    while (readLine() != null) {
      String[] tokens = getTokens(line.replace('\'', ' '));
      if (tokens.length == 0)
        continue;
      if (tokens[0].equals("TLS")) {
        tlsGroup = new Hashtable<String, Object>();
        ranges = new ArrayList<Map<String, Object>>();
        tlsGroup.put("ranges", ranges);
        tlsGroups.add(tlsGroup);
        tlsGroup.put("id", Integer.valueOf(++tlsGroupID));
      } else if (tokens[0].equals("RANGE")) {
        /*
         RANGE  'A   1.' 'A   7.' ALL
        */
        range = new Hashtable<String, Object>();
        char chain1 = tokens[1].charAt(0);
        char chain2 = tokens[3].charAt(0);
        int res1 = Parser.parseInt(tokens[2]);
        int res2 = Parser.parseInt(tokens[4]);
        // ALL ? 
        if (chain1 == chain2) {
          range.put("chains", "" + chain1 + chain2);
          if (res1 <= res2) {
            range.put("residues", new int[] { res1, res2 });
            ranges.add(range);
          } else {
            tlsAddError(" TLS group residues are not in order (range ignored)");
          }
        } else {
          tlsAddError(" TLS group chains are different (range ignored)");
        }
      } else if (tokens[0].equals("ORIGIN")) {
        /*
        ORIGIN    11.6283   8.2746  10.6813
        */
        Point3f origin = new Point3f();
        tlsGroup.put("origin", origin);
        origin.set(parseFloat(tokens[1]), parseFloat(tokens[2]),
            parseFloat(tokens[3]));
        if (Float.isNaN(origin.x) || Float.isNaN(origin.y)
            || Float.isNaN(origin.z)) {
          origin.set(Float.NaN, Float.NaN, Float.NaN);
          tlsAddError("invalid origin: " + line);
        }
      } else if (tokens[0].equals("T") || tokens[0].equals("L")
          || tokens[0].equals("S")) {
        /*
        T     0.0631   0.0710   0.0706   0.0097  -0.0103  -0.0070
        L     3.3102   4.0562   3.5219   1.4620  -1.0250  -1.1798
        S     0.0297  -0.0733   0.0732   0.0069  -0.1241  -0.1282   0.0034   0.0501        
         */
        char tensorType = tokens[0].charAt(0);
        String[] nn = (tensorType == 'S' ? Snn : TLnn);
        float[][] tensor = new float[3][3];
        tlsGroup.put("t" + tensorType, tensor);

        for (int i = 1; i < tokens.length; i++) {
          int ti = nn[i].charAt(0) - '1';
          int tj = nn[i].charAt(1) - '1';
          tensor[ti][tj] = parseFloat(tokens[++i]);
          if (ti < tj)
            tensor[tj][ti] = tensor[ti][tj];
        }
        if (tensorType == 'S')
          tensor[0][0] = -tensor[0][0];
        for (int i = 0; i < 3; i++)
          for (int j = 0; j < 3; j++)
            if (Float.isNaN(tensor[i][j])) {
              tlsAddError("invalid tensor: " + Escape.escapeArray(tensor));
            }
      }
    }
    Logger.info(tlsGroupID + " TLS groups read");
    Hashtable<String, Object> groups = new Hashtable<String, Object>();
    groups.put("groupCount", Integer.valueOf(tlsGroupID));
    groups.put("groups", tlsGroups);
    vTlsModels.add(groups);
    htParams.put("vTlsModels", vTlsModels);
  }

  private void tlsAddError(String error) {
    if (sbTlsErrors == null)
      sbTlsErrors = new StringBuffer();
    sbTlsErrors.append(fileName).append('\t').append("TLS group ").append(
        tlsGroupID).append('\t').append(error).append('\n');
  }

}
