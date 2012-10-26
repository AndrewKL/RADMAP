package org.jmol.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import org.jmol.shape.Mesh;
import org.jmol.shape.MeshCollection;
import org.jmol.shape.Shape;

import org.jmol.viewer.Viewer;

public class DEDXSurfaceUtils {
    
  static final boolean DEBUG=true;
  
  public final int FLEX_SURFACE = 1;
  public final int TOTAL_DEDX_SURFACE = 1;
  
  
  
  public static DataVertex readFlexDataFromGaussianOutFile(String path, float vectorParamA, float vectorParamC){
    float x = 0;
    float y = 0;
    float z = 0;
    float flexweight = -1;
    BufferedReader br = null;
    String title ="";
    
    boolean foundtitle = false;
    boolean foundNumAtoms = false;
    Point3f[] atomlocation = null;
    Point3f[] atomderivatives = null;
    
    
    //String Path = "C:\\Documents and Settings\\s0966645\\workspace\\JMOL\\RA\\RA  0.out";
    
    if(DEBUG)System.out.println("Path: "+path);
    //read the gaussian file
    try {
 
      String sCurrentLine;
       br = new BufferedReader(new FileReader(path));
 
      while ((sCurrentLine = br.readLine()) != null && !foundtitle) {
        //if(DEBUG)System.out.println("sCurrentLine: "+sCurrentLine);
        if(sCurrentLine.startsWith(" RA ")){
          title = sCurrentLine;
          //if(DEBUG){System.out.println("Title Found:  "+title);}
          foundtitle = true;
        } 
      }
      
      //finding the number of atoms
      int numAtoms = 0;
      while ((sCurrentLine = br.readLine()) != null && !foundNumAtoms) {
        //if(DEBUG)System.out.println("sCurrentLine: "+sCurrentLine);
        if(sCurrentLine.startsWith(" Charge =")){
          
          while(!(sCurrentLine = br.readLine()).equals(" ")) numAtoms++;
          foundNumAtoms = true;
        } 
      }
      
      if(DEBUG)System.out.println("num atoms found : "+numAtoms);
      
      
      
      
      while((sCurrentLine = br.readLine()) != null){
        if(sCurrentLine.contains("Variable       Old X    -DE/DX   Delta X   Delta X   Delta X     New X")){
          atomlocation = new Point3f[numAtoms];
          atomderivatives = new Point3f[numAtoms];
          if(DEBUG)System.out.println("derivative data found");
          br.readLine();
          String[] subString;
          for(int i=0; i<atomlocation.length;i++){
            atomlocation[i]=new Point3f();
            atomderivatives[i]=new Point3f();
            
            //read x
            //if(DEBUG)System.out.println("derivative loop: "+i);
            sCurrentLine = br.readLine();
            sCurrentLine=sCurrentLine.replaceAll("        ", " ");
            sCurrentLine=sCurrentLine.replaceAll("       ", " ");
            sCurrentLine=sCurrentLine.replaceAll("   ", " ");
            sCurrentLine=sCurrentLine.replaceAll("  ", " ");
            subString = sCurrentLine.split(" ");
            //if(DEBUG) System.out.println("processed x string:"+sCurrentLine);
            //if(DEBUG) System.out.println("processed x substring[2] :"+subString[2]);
            //if(DEBUG) System.out.println("processed x substring[3] :"+subString[3]);
            atomlocation[i].x = Float.valueOf(subString[2]).floatValue();
            atomderivatives[i].x = Float.valueOf(subString[3]).floatValue();
            
            
            //read y
            sCurrentLine = br.readLine();
            sCurrentLine=sCurrentLine.replaceAll("        ", " ");
            sCurrentLine=sCurrentLine.replaceAll("       ", " ");
            sCurrentLine=sCurrentLine.replaceAll("   ", " ");
            sCurrentLine=sCurrentLine.replaceAll("  ", " ");
            subString = sCurrentLine.split(" ");
            atomlocation[i].y = Float.valueOf(subString[2]).floatValue();
            atomderivatives[i].y = Float.valueOf(subString[3]).floatValue();
            //if(DEBUG) System.out.println("processed y string:"+sCurrentLine);
            
            //read z
            sCurrentLine = br.readLine();
            sCurrentLine=sCurrentLine.replaceAll("        ", " ");
            sCurrentLine=sCurrentLine.replaceAll("       ", " ");
            sCurrentLine=sCurrentLine.replaceAll("   ", " ");
            sCurrentLine=sCurrentLine.replaceAll("  ", " ");
            subString = sCurrentLine.split(" ");
            atomlocation[i].z = Float.valueOf(subString[2]).floatValue();
            atomderivatives[i].z = Float.valueOf(subString[3]).floatValue();
            //if(DEBUG) System.out.println("processed z string:"+sCurrentLine);
            
            //if(DEBUG) System.out.println("processed p3fs 0: "+atomlocation[i]+"  part 2: "+atomderivatives[i]);
            
          }
          
          
          
        }
      
      }
      
     } catch (IOException e) {
       e.printStackTrace();
     } finally {
       try {
         if (br != null)br.close();
     } catch (IOException ex) {
        ex.printStackTrace();
     }
       if(title==null||atomlocation==null)return null;
       
       String[] splittitlestring = title.split(" ");
       
       
       x = Float.valueOf(splittitlestring[4]).floatValue();
       y = Float.valueOf(splittitlestring[5]).floatValue();
       z = Float.valueOf(splittitlestring[6]).floatValue();
       
       /*cyclicing through all the connections between all atoms to 
        * see which one are relavant and how they move.  use a gaussian radial 
        * weighting function to select only the atom pairs that reasonable represent
        * a bond
        * 
        * then a projection is done to see if the atoms are moving perpendicularly 
        * to each other
        */
       
       float numerator = 0;
       float denominator =0;
       
       float a = 2;
       float c= (float) 0.5;
       float bondlength = (float)1.0;
       
       if(DEBUG)System.out.println("calculating vector data");
       for(int i =0;i<atomlocation.length-1;i++){
         for(int j =i+1;j<atomlocation.length;j++){
           //if(DEBUG)System.out.println("Comparing atom "+i+" with atom "+j);
           
           Point3f distancevector= Point3fVecMath.sub(atomlocation[i],atomlocation[j]);
           Point3f totalDEDX = Point3fVecMath.sub(atomderivatives[i],atomderivatives[j]);
           Point3f projectedDEDX = Point3fVecMath.project(totalDEDX, distancevector);
           Point3f antiprojectedDEDX = Point3fVecMath.sub(totalDEDX, projectedDEDX);
           
           
           float distance = atomlocation[i].distance(atomlocation[j]);
           float distanceweight = a * (float)Math.exp(-(distance*distance-bondlength)/2/c/c);
           numerator += distanceweight*Point3fVecMath.abslength(antiprojectedDEDX);
           denominator += distanceweight;
           
           //if(DEBUG)System.out.println("distance vector: "+distancevector);
           //if(DEBUG)System.out.println("DE i vector: "+atomderivatives[i]);
           //if(DEBUG)System.out.println("DE j vector: "+atomderivatives[j]);
           //if(DEBUG)System.out.println("DE total vector: "+totalDEDX);
           //if(DEBUG)System.out.println("projected DE vector: "+projectedDEDX);
           //if(DEBUG)System.out.println("antiprojDE vec: "+i+" "+j+" "+antiprojectedDEDX+
           //    "  length: "+Point3fVecMath.length(antiprojectedDEDX));
           
           
           //if(DEBUG)System.out.println("distance vector: "+distancevector);
           //if(DEBUG)System.out.println("distance vector length: "+Point3fVecMath.length(distancevector));
           
         }
       }
       flexweight = numerator/denominator;
       
       
       
    
     }
    
    
    if(DEBUG)System.out.println("flexweight: "+flexweight);
    return new DataVertex(new Point3f(x,y,z), flexweight);
  
  }
  
  public static DataVertex[] loadFlexGaussianOutFiles(String folderLocation, float vectorParamA, float vectorParamC){
    ArrayList<DataVertex> dataVertices = new ArrayList<DataVertex>();
    
    
    //String curDir = System.getProperty("user.dir")+"\\RA\\";
    File dir = new File(folderLocation);

    String[] filesInDir = dir.list();
    
    for(int i=0;i<filesInDir.length;i++){
      if(filesInDir[i].endsWith(".out")&&filesInDir[i].startsWith("RA")){
        DataVertex toBeAdded = readFlexDataFromGaussianOutFile(folderLocation+"/"+filesInDir[i], vectorParamA, vectorParamC);
        if(toBeAdded!=null)dataVertices.add(toBeAdded);
      }
    }
    //Object[] E = dataVertices.toArray();
    //if(DEBUG)System.out.println("Printing DataVertices");
    //if(DEBUG)for(int i=0;i<E.length;i++)System.out.println(E[i]);
    
    
    Object[] obs = dataVertices.toArray();
    //if(DEBUG)System.out.println("converting data types to datavertex");
    DataVertex[] data = new DataVertex[obs.length];
    for(int i=0;i<obs.length;i++){
      data[i]=(DataVertex) obs[i];
      System.out.println(data[i]);
    }
    isosurfacePES.writeVertexDataFile(folderLocation, data,"flex");
    //if(DEBUG)System.out.println("converted data types to datavertex");
    return data; 
  }
  
  /*public static void main(String[] args){
    String path = "C:\\Users\\Stranger\\Google Drive\\IsosurfacePES RA\\Calcs\\RA CO2 OPT Cart";
    loadFlexGaussianOutFiles(path);
  }*/
  
  public static void loadFlexSurface(Viewer theViewer,String folderLocation, String resolution, int colorScheme, float vectorParamA, float vectorParamC){
    
    if(DEBUG)System.out.println("load FlexSurface folderLocation: "+folderLocation);
    
    theViewer.runScriptImmediately("isosurface delete"); //clears any leftover isosurfaces
    theViewer.runScriptImmediately("isosurface resolution "+resolution+" molecular dots mesh translucent");
    
    DataVertex[] flexData = loadFlexGaussianOutFiles(folderLocation, vectorParamA, vectorParamC);
    float min = flexData[0].energy;//not really energy its the flew "value"
    float max = flexData[0].energy;
    float average=flexData[0].energy/flexData.length;
    
    for(int i=1;i<flexData.length;i++){
      average+=(flexData[i].energy/flexData.length);
      if(flexData[i].energy > max){max=flexData[i].energy;}
      else if(flexData[i].energy < min){min=flexData[i].energy;}
    }
    
    System.out.println("min:  "+min);
    System.out.println("max:  "+max);
    System.out.println("average:  "+average);
    float range = max-min;
    System.out.println("range:  "+range);
    
    
    if(DEBUG){System.out.println("generatingIsosurfaceverticesFiles");}
    
    //find the mesh
    Shape[] shapes = theViewer.getShapeManager().getShapes();
    MeshCollection themeshcollection;
    Mesh theMesh =null;
    //if(DEBUG){System.out.println("shapes length: "+shapes.length);}
    for(int i=0;i<shapes.length;i++){
      //if(DEBUG){System.out.println("finding mesh loop:  "+shapes[i]);}
      if(shapes[i] instanceof MeshCollection){
        //if(DEBUG){System.out.println("found mesh collection:  "+shapes[i]);}
        
        themeshcollection = (MeshCollection) shapes[i];
        for(int j=0;j<themeshcollection.meshes.length;j++){
          //if(DEBUG){System.out.println("cycling through meshes:  "+themeshcollection.meshes[j]);}
          if(themeshcollection.meshes[j]!=null){
            theMesh=themeshcollection.meshes[j];
          }
        }
      }
    }
    if(DEBUG){System.out.println("current themesh value:  "+theMesh);}
    
    
    if(DEBUG)System.out.println("Matching Vertices gaussian interpolation");
    
    theMesh.vertexColixes = new short[theMesh.vertexCount];
    theMesh.isColorSolid = false;
    
    //generating energy data
    float numerator;
    float denominator;
    
    float a = 2;
    float c= (float) 0.2;
    
    for(int i=0;i<theMesh.vertexCount;i++){
      numerator=0;
      denominator=0;
      Point3f currentpoint = theMesh.vertices[i];
      for(int j=0;j<flexData.length;j++){
        DataVertex currentDV = flexData[j];
        Point3f datalocation = currentDV.xyz;
        float distance = currentpoint.distance(datalocation);
        //if(DEBUG)System.out.println("distance:  "+distance);
        float distanceweight = a * (float)Math.exp(-distance*distance/2/c/c);
        numerator += distanceweight*currentDV.energy;
        denominator += distanceweight;
        

      }
      //if(DEBUG)System.out.println("num/denom: "+numerator+" / "+denominator);
      theMesh.vertexColixes[i]=ColorIndexUtil.getColorIndexFromPalette(numerator/denominator,min,max,colorScheme,true);
      //if(DEBUG)System.out.println("Colix Set: "+getColorIndexFromPalette(numerator/denominator,min,max,true));
    }
    
    theViewer.script("isosurface translucent");
    
    
  }
  
  public static void loadTotalDEDXSurface(Viewer theViewer,String folderLocation, String resolution, int colorScheme){
    
    if(DEBUG)System.out.println("load FlexSurface folderLocation: "+folderLocation);
    
    theViewer.runScriptImmediately("isosurface delete"); //clears any leftover isosurfaces
    theViewer.runScriptImmediately("isosurface resolution "+resolution+" molecular dots mesh translucent");
    
    DataVertex[] totalDEDXData = loadTotalDEDXGaussianOutFiles(folderLocation);
    float min = totalDEDXData[0].energy;//not really energy its the flew "value"
    float max = totalDEDXData[0].energy;
    float average=totalDEDXData[0].energy/totalDEDXData.length;
    
    for(int i=1;i<totalDEDXData.length;i++){
      average+=(totalDEDXData[i].energy/totalDEDXData.length);
      if(totalDEDXData[i].energy > max){max=totalDEDXData[i].energy;}
      else if(totalDEDXData[i].energy < min){min=totalDEDXData[i].energy;}
    }
    
    System.out.println("min:  "+min);
    System.out.println("max:  "+max);
    System.out.println("average:  "+average);
    float range = max-min;
    System.out.println("range:  "+range);
    
    
    if(DEBUG){System.out.println("generatingIsosurfaceverticesFiles");}
    
    //find the mesh
    Shape[] shapes = theViewer.getShapeManager().getShapes();
    MeshCollection themeshcollection;
    Mesh theMesh =null;
    //if(DEBUG){System.out.println("shapes length: "+shapes.length);}
    for(int i=0;i<shapes.length;i++){
      //if(DEBUG){System.out.println("finding mesh loop:  "+shapes[i]);}
      if(shapes[i] instanceof MeshCollection){
        //if(DEBUG){System.out.println("found mesh collection:  "+shapes[i]);}
        
        themeshcollection = (MeshCollection) shapes[i];
        for(int j=0;j<themeshcollection.meshes.length;j++){
          //if(DEBUG){System.out.println("cycling through meshes:  "+themeshcollection.meshes[j]);}
          if(themeshcollection.meshes[j]!=null){
            theMesh=themeshcollection.meshes[j];
          }
        }
      }
    }
    if(DEBUG){System.out.println("current themesh value:  "+theMesh);}
    
    
    if(DEBUG)System.out.println("Matching Vertices gaussian interpolation");
    
    theMesh.vertexColixes = new short[theMesh.vertexCount];
    theMesh.isColorSolid = false;
    
    //generating energy data
    float numerator;
    float denominator;
    
    float a = 2;
    float c= (float) 0.2;
    
    for(int i=0;i<theMesh.vertexCount;i++){
      numerator=0;
      denominator=0;
      Point3f currentpoint = theMesh.vertices[i];
      for(int j=0;j<totalDEDXData.length;j++){
        DataVertex currentDV = totalDEDXData[j];
        Point3f datalocation = currentDV.xyz;
        float distance = currentpoint.distance(datalocation);
        //if(DEBUG)System.out.println("distance:  "+distance);
        float distanceweight = a * (float)Math.exp(-distance*distance/2/c/c);
        numerator += distanceweight*currentDV.energy;
        denominator += distanceweight;
        

      }
      //if(DEBUG)System.out.println("num/denom: "+numerator+" / "+denominator);
      theMesh.vertexColixes[i]=ColorIndexUtil.getColorIndexFromPalette(numerator/denominator,min,max,colorScheme,true);
      //if(DEBUG)System.out.println("Colix Set: "+getColorIndexFromPalette(numerator/denominator,min,max,true));
    }
    
    theViewer.script("isosurface translucent");
    
    
  }
  
  public static DataVertex[] loadTotalDEDXGaussianOutFiles(String folderLocation){
    ArrayList<DataVertex> dataVertices = new ArrayList<DataVertex>();
    
    
    //String curDir = System.getProperty("user.dir")+"\\RA\\";
    File dir = new File(folderLocation);

    String[] filesInDir = dir.list();
    
    for(int i=0;i<filesInDir.length;i++){
      if(filesInDir[i].endsWith(".out")&&filesInDir[i].startsWith("RA")){
        DataVertex toBeAdded = readTotalDEDXDataFromGaussianOutFile(folderLocation+"/"+filesInDir[i]);
        if(toBeAdded!=null)dataVertices.add(toBeAdded);
      }
    }
    //Object[] E = dataVertices.toArray();
    //if(DEBUG)System.out.println("Printing DataVertices");
    //if(DEBUG)for(int i=0;i<E.length;i++)System.out.println(E[i]);
    
    
    Object[] obs = dataVertices.toArray();
    //if(DEBUG)System.out.println("converting data types to datavertex");
    DataVertex[] data = new DataVertex[obs.length];
    for(int i=0;i<obs.length;i++){
      data[i]=(DataVertex) obs[i];
      System.out.println(data[i]);
    }
    isosurfacePES.writeVertexDataFile(folderLocation, data,"flex");
    //if(DEBUG)System.out.println("converted data types to datavertex");
    return data; 
  }
  
  
  public static DataVertex readTotalDEDXDataFromGaussianOutFile(String path){
    float x = 0;
    float y = 0;
    float z = 0;
    BufferedReader br = null;
    String title ="";
    float totalDEDX = 0;
    
    boolean foundtitle = false;
    boolean foundNumAtoms = false;
    Point3f[] atomlocation = null;
    Point3f[] atomderivatives = null;
    
    
    //String Path = "C:\\Documents and Settings\\s0966645\\workspace\\JMOL\\RA\\RA  0.out";
    
    if(DEBUG)System.out.println("Path: "+path);
    //read the gaussian file
    try {
 
      String sCurrentLine;
       br = new BufferedReader(new FileReader(path));
 
      while ((sCurrentLine = br.readLine()) != null && !foundtitle) {
        //if(DEBUG)System.out.println("sCurrentLine: "+sCurrentLine);
        if(sCurrentLine.startsWith(" RA ")){
          title = sCurrentLine;
          //if(DEBUG){System.out.println("Title Found:  "+title);}
          foundtitle = true;
        } 
      }
      
      //finding the number of atoms
      int numAtoms = 0;
      while ((sCurrentLine = br.readLine()) != null && !foundNumAtoms) {
        //if(DEBUG)System.out.println("sCurrentLine: "+sCurrentLine);
        if(sCurrentLine.startsWith(" Charge =")){
          
          while(!(sCurrentLine = br.readLine()).equals(" ")) numAtoms++;
          foundNumAtoms = true;
        } 
      }
      
      if(DEBUG)System.out.println("num atoms found : "+numAtoms);
      
      
      
      
      while((sCurrentLine = br.readLine()) != null){
        if(sCurrentLine.contains("Variable       Old X    -DE/DX   Delta X   Delta X   Delta X     New X")){
          atomlocation = new Point3f[numAtoms];
          atomderivatives = new Point3f[numAtoms];
          if(DEBUG)System.out.println("derivative data found");
          br.readLine();
          String[] subString;
          for(int i=0; i<atomlocation.length;i++){
            atomlocation[i]=new Point3f();
            atomderivatives[i]=new Point3f();
            
            //read x
            //if(DEBUG)System.out.println("derivative loop: "+i);
            sCurrentLine = br.readLine();
            sCurrentLine=sCurrentLine.replaceAll("        ", " ");
            sCurrentLine=sCurrentLine.replaceAll("       ", " ");
            sCurrentLine=sCurrentLine.replaceAll("   ", " ");
            sCurrentLine=sCurrentLine.replaceAll("  ", " ");
            subString = sCurrentLine.split(" ");
            //if(DEBUG) System.out.println("processed x string:"+sCurrentLine);
            //if(DEBUG) System.out.println("processed x substring[2] :"+subString[2]);
            //if(DEBUG) System.out.println("processed x substring[3] :"+subString[3]);
            atomlocation[i].x = Float.valueOf(subString[2]).floatValue();
            atomderivatives[i].x = Float.valueOf(subString[3]).floatValue();
            
            
            //read y
            sCurrentLine = br.readLine();
            sCurrentLine=sCurrentLine.replaceAll("        ", " ");
            sCurrentLine=sCurrentLine.replaceAll("       ", " ");
            sCurrentLine=sCurrentLine.replaceAll("   ", " ");
            sCurrentLine=sCurrentLine.replaceAll("  ", " ");
            subString = sCurrentLine.split(" ");
            atomlocation[i].y = Float.valueOf(subString[2]).floatValue();
            atomderivatives[i].y = Float.valueOf(subString[3]).floatValue();
            //if(DEBUG) System.out.println("processed y string:"+sCurrentLine);
            
            //read z
            sCurrentLine = br.readLine();
            sCurrentLine=sCurrentLine.replaceAll("        ", " ");
            sCurrentLine=sCurrentLine.replaceAll("       ", " ");
            sCurrentLine=sCurrentLine.replaceAll("   ", " ");
            sCurrentLine=sCurrentLine.replaceAll("  ", " ");
            subString = sCurrentLine.split(" ");
            atomlocation[i].z = Float.valueOf(subString[2]).floatValue();
            atomderivatives[i].z = Float.valueOf(subString[3]).floatValue();
            //if(DEBUG) System.out.println("processed z string:"+sCurrentLine);
            
            //if(DEBUG) System.out.println("processed p3fs 0: "+atomlocation[i]+"  part 2: "+atomderivatives[i]);
            
          }
          
          
          
        }
      
      }
      
     } catch (IOException e) {
       e.printStackTrace();
     } finally {
       try {
         if (br != null)br.close();
     } catch (IOException ex) {
        ex.printStackTrace();
     }
       if(title==null||atomlocation==null)return null;
       
       String[] splittitlestring = title.split(" ");
       
       
       x = Float.valueOf(splittitlestring[4]).floatValue();
       y = Float.valueOf(splittitlestring[5]).floatValue();
       z = Float.valueOf(splittitlestring[6]).floatValue();
       
       /*cyclicing through all the connections between all atoms to 
        * see which one are relavant and how they move.  use a gaussian radial 
        * weighting function to select only the atom pairs that reasonable represent
        * a bond
        * 
        * then a projection is done to see if the atoms are moving perpendicularly 
        * to each other
        */      
       
       
       if(DEBUG)System.out.println("calculating DEDX vector data");
       for(int i =0;i<atomlocation.length-1;i++){
         //if(DEBUG)System.out.println("DEDX of atom: "+atomderivatives[i]);
         totalDEDX +=Point3fVecMath.length(atomderivatives[i]);

       }
    
     }
    
    
    if(DEBUG)System.out.println("totalDEDX: "+totalDEDX);
    return new DataVertex(new Point3f(x,y,z), totalDEDX);
  
  }
  
  
  
  public static class Point3fVecMath{
    public static float dot(Point3f a, Point3f b){
      return (a.x*b.x)+(a.y*b.y)+(a.z*b.z);
    }
    public static Point3f add(Point3f a, Point3f b){
      return new Point3f(a.x+b.x,a.y+b.y,a.z+b.z);
    }
    public static Point3f sub(Point3f a, Point3f b){
      return new Point3f(a.x-b.x,a.y-b.y,a.z-b.z);
    }
    public static Point3f cross(Point3f a, Point3f b){
      return new Point3f(a.y*b.z-a.z*b.y,a.x*b.z-a.z*b.x,a.x*b.y-b.x*a.y);
    }
    public static float length(Point3f a){
      return (float)Math.sqrt(a.x*a.x+a.y*a.y+a.z*a.z);
    }
    public static float abslength(Point3f a){
      return Math.abs((float)Math.sqrt(a.x*a.x+a.y*a.y+a.z*a.z));
    }
    public static Point3f mult(Point3f a, float b){
      return new Point3f(b*a.x,b*a.y,b*a.z);
    }
    public static Point3f div(Point3f a, float b){
      return new Point3f(a.x/b,a.y/b,a.z/b);
    }
    public static Point3f normal(Point3f a){
      return Point3fVecMath.div(a, Point3fVecMath.length(a));
    }
    public static Point3f project(Point3f a, Point3f b){
     //projects a onto b
     return Point3fVecMath.mult(Point3fVecMath.normal(b), Point3fVecMath.dot(a, b)/(b.x*b.x+b.y*b.y+b.z*b.z));
    }

  }

}
