/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-03-30 11:40:16 -0500 (Fri, 30 Mar 2007) $
 * $Revision: 7273 $
 *
 * Copyright (C) 2007 Miguel, Bob, Jmol Development
 *
 * Contact: hansonr@stolaf.edu
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.jvxl.readers;

import org.jmol.api.Interface;
import org.jmol.api.MepCalculationInterface;

class IsoMepReader extends AtomDataReader {

  protected String type;

  IsoMepReader(SurfaceGenerator sg) {
    super(sg);
    type = "Mep";
  }
    
  /////// molecular electrostatic potential ///////

  @Override
  protected void setup(boolean isMapData) {
    super.setup(isMapData);
    doAddHydrogens = false;
    getAtoms(params.bsSelected, doAddHydrogens, true, false, false, false, false, params.mep_marginAngstroms);
    setHeader("MEP", "");
    setRanges(params.mep_ptsPerAngstrom, params.mep_gridMax);    
  }

  @Override
  protected void generateCube() {
    newVoxelDataCube();
    MepCalculationInterface m = (MepCalculationInterface) Interface.getOptionInterface("quantum." + type + "Calculation");
    m.calculate(volumeData, bsMySelected, atomData.atomXyz,
          params.theProperty, params.mep_calcType);
  }
}
