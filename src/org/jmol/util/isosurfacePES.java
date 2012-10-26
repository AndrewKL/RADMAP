package org.jmol.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


import javax.vecmath.Point3f;


import org.jmol.shape.Mesh;
import org.jmol.shape.MeshCollection;
import org.jmol.shape.Shape;

import org.jmol.viewer.Viewer;


public class isosurfacePES {
  
  /*
   * By Andrew Long andrew.long.3001@gmail.com
   * 
   * Color Index Implementation pilfered from JMOL
   */

  static final boolean DEBUG=true;
  
  

  
  public static String generateGaussianMoleculeSpecification(Viewer viewer){
    
    
    return viewer.getData("visible","USER:%-2e %10.5x %10.5y %10.5z");
  }
  
  
 
  
  public static int generateGaussianInputFile(String filename, 
                                              String percentSection, 
                                              String routeSection, 
                                              String titleSection, 
                                              int charge, 
                                              int multiplicity, 
                                              String moleculeSpecification){
    //Generates a new Gaussian input file with the name filename with the various sections specified above. 
    if (DEBUG) {System.out.println("filename:"+filename);}
    
    PrintWriter outputprintwriter;
    try {
      outputprintwriter = new PrintWriter(filename);
    } catch (FileNotFoundException e) {
      return -1;
    }
    //writing the file itself
    outputprintwriter.println(percentSection); outputprintwriter.println();
    outputprintwriter.println(routeSection); outputprintwriter.println();
    outputprintwriter.println(titleSection); outputprintwriter.println();
    outputprintwriter.println(charge+" "+multiplicity);
    outputprintwriter.println(moleculeSpecification);

    boolean diditfail=outputprintwriter.checkError();
    if (DEBUG) {System.out.println("diditfail:"+diditfail);}
    
    outputprintwriter.close();
  
    if (DEBUG) {System.out.println("finished generateGaussianInputFile");}
    return 0;
  }
  
  public static int generateIsosurfaceVerticesFile(Viewer theViewer){
    if(DEBUG){System.out.println("generatingIsosurfaceverticesFiles");}
    
    //find the mesh
    Shape[] shapes = theViewer.getShapeManager().getShapes();
    MeshCollection themeshcollection;
    Mesh theMesh =null;
    if(DEBUG){System.out.println("shapes length: "+shapes.length);}
    for(int i=0;i<shapes.length;i++){
      if(DEBUG){System.out.println("finding mesh loop:  "+shapes[i]);}
      if(shapes[i] instanceof MeshCollection){
        if(DEBUG){System.out.println("found mesh collection:  "+shapes[i]);}
        
        themeshcollection = (MeshCollection) shapes[i];
        for(int j=0;j<themeshcollection.meshes.length;j++){
          if(DEBUG){System.out.println("cycling through meshes:  "+themeshcollection.meshes[j]);}
          if(themeshcollection.meshes[j]!=null){
            theMesh=themeshcollection.meshes[j];
          }
        }
      }
    }
    if(DEBUG){System.out.println("current themesh value:  "+theMesh);}
    if(theMesh==null){return -1;}
    
    
    //write out the vertices
    PrintWriter outputprintwriter;
    String filename = "isosurfacecoordinatefile.txt";
    try {
      if(DEBUG){System.out.println("starting printwriter: gogogogogogo");}
      outputprintwriter = new PrintWriter(filename);
      outputprintwriter.println("isosurface coordinate file");
      for(int i=0;i<theMesh.vertices.length;i++){
        if(DEBUG){System.out.println("current vertex:  "+theMesh.vertices[i]);}
        if(theMesh.vertices[i]!=null){
          float x =theMesh.vertices[i].x;
          float y =theMesh.vertices[i].y;
          float z =theMesh.vertices[i].z;
          String linetoprint="  ("+x+", "+y+", "+z+")";
          if(DEBUG){System.out.println("linetoprint:  "+linetoprint);}
          outputprintwriter.println(linetoprint);
        }
        
        
      }
      outputprintwriter.close();
    } catch (FileNotFoundException e) {
      return -2;
    }
    return 0;
    
  }
  
  public static int generateBatchFileForIsosurface(Mesh theMesh, String folderLocation){
    


    PrintWriter batchFilePrintWriter;
    
    String filename;
    int errornumber = 0;
    try{
      
      if(DEBUG){System.out.println("batch file location:   "+folderLocation+"RA Batch File.bcf");}
      
      batchFilePrintWriter = new PrintWriter(folderLocation+"/RA Batch File.bcf");
      batchFilePrintWriter.println("!");
      batchFilePrintWriter.println("! batchfile list");
      batchFilePrintWriter.println("!start=1");
      batchFilePrintWriter.println("!");
      //write out the vertices into output files
              
      for(int i=0;i<theMesh.vertices.length;i++){
        
        if(theMesh.vertices[i]!=null){
          
          filename = "RA  "+i;
          batchFilePrintWriter.println(filename+".gjf"+" , "+filename+".out");
        }
        
      }
      batchFilePrintWriter.close();
      
    }catch (FileNotFoundException e) {
      System.out.println("error: file not found");
    }

    return errornumber;
  }
  
  public static void testAndViewSurface(Viewer theViewer, float calcresolution,float probeRadius) {
    //used to view potential surface
    if(probeRadius<=0)probeRadius=(float)1.2;
    
    theViewer.runScriptImmediately("isosurface delete"); //clears any leftover isosurfaces
    //theViewer.runScriptImmediately("isosurface resolution "+calcresolution+" solvent "+probeRadius+" dots mesh nofill");
    //theViewer.runScriptImmediately("isosurface translucent");
    theViewer.script("isosurface resolution "+calcresolution+" solvent "+probeRadius+" dots mesh nofill");
  }
  
  public static void generateIsosurfacePESfiles(Viewer theViewer, int charge,
                                                int multiplicity,float calcresolution,float probeRadius, String fragment, String folderLocation, String moleculename, String routeSection, String percentSection) {
    //Main function use to generate all of the output files. including batchfile and a copy of the molcule
    
    if(probeRadius<=0)probeRadius=(float)1.2;
    theViewer.runScriptImmediately("isosurface delete"); //clears any leftover isosurfaces
    theViewer.runScriptImmediately("isosurface resolution "+calcresolution+" solvent "+probeRadius+" dots mesh nofill");
    
    moleculename = moleculename.replaceAll(" ", "");
    
    if(DEBUG){System.out.println("generatingIsosurfacePESFiles: charge: "+charge+"  multiplicity:  "+multiplicity+"  fragment: "+fragment);}
    
    //find the mesh
    Shape[] shapes = theViewer.getShapeManager().getShapes();
    MeshCollection themeshcollection;
    Mesh theMesh =null;
    if(DEBUG){System.out.println("shapes length: "+shapes.length);}
    for(int i=0;i<shapes.length;i++){
      if(DEBUG){System.out.println("finding mesh loop:  "+shapes[i]);}
      if(shapes[i] instanceof MeshCollection){
        if(DEBUG){System.out.println("found mesh collection:  "+shapes[i]);}
        
        themeshcollection = (MeshCollection) shapes[i];
        for(int j=0;j<themeshcollection.meshes.length;j++){
          if(DEBUG){System.out.println("cycling through meshes:  "+themeshcollection.meshes[j]);}
          if(themeshcollection.meshes[j]!=null){
            theMesh=themeshcollection.meshes[j];
          }
        }
      }
    }
    if(DEBUG){System.out.println("current themesh value:  "+theMesh);}
    if(theMesh==null){return;}
    
    String molecularSection = generateGaussianMoleculeSpecification(theViewer);
    String filename;
    if (percentSection==null) percentSection="%mem=6MW";
    if(routeSection==null) routeSection="#opt(MaxCycles=2) ub3lyp/6-31g"; 
    String titleSection; 
     
    
    int errornumber = 0;

    //String currentDir = System.getProperty("user.dir")+"/RA/";
    
    for(int i=0;i<theMesh.vertices.length;i++){
      if(DEBUG){System.out.println("current vertex:  "+theMesh.vertices[i]);}
      if(theMesh.vertices[i]!=null){
        float x =theMesh.vertices[i].x;
        float y =theMesh.vertices[i].y;
        float z =theMesh.vertices[i].z;
        String linetoprint=fragment+" 0 "+x+" "+y+" "+z;
        filename = "RA  "+i;
        titleSection="RA "+linetoprint;
        
        //writing gaussian file for vertex
        if(DEBUG){System.out.println("linetoprint:  "+linetoprint);}
        errornumber += generateGaussianInputFile(folderLocation+"/"+filename+".gjf",
            percentSection,
            routeSection,
            titleSection,
            charge,
            multiplicity,
            molecularSection+linetoprint);
        

        
        
      }
    }
    
    //creating batchfile
    
    errornumber += generateBatchFileForIsosurface(theMesh, folderLocation);
    
    if(DEBUG)System.out.println("isosurfacePES errornumber:  "+errornumber);
    
    theViewer.script("isosurface resolution "+calcresolution+" molecular dots mesh nofill");
    
    String writeCommand = "write \""+folderLocation+"\\"+moleculename+".mol\"";
    writeCommand = writeCommand.replace("\\", "/");
    if(DEBUG)System.out.println(writeCommand);
    theViewer.runScriptImmediately(writeCommand);

    return;
    
  }
  
  

  

  public static DataVertex loadGaussianOutFile(String path){
    float x = 0;
    float y = 0;
    float z = 0;
    float energy = 0;
    BufferedReader br = null;
    String title ="";
    String energyLine = null;
    boolean foundtitle = false;
    
    //String Path = "C:\\Documents and Settings\\s0966645\\workspace\\JMOL\\RA\\RA  0.out";
    
    if(DEBUG)System.out.print("Path:"+path);
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
      while((sCurrentLine = br.readLine()) != null){
        if(sCurrentLine.contains("SCF Done:")){
          energyLine=sCurrentLine;
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
       if(title==null||energyLine==null)return null;
       
       String[] splittitlestring = title.split(" ");
       
       /*System.out.println("splittitlestring: "+splittitlestring);
       for(int i = 0;i<splittitlestring.length;i++){
         System.out.println("sub splittitlestring: "+splittitlestring[i]);
       }*/
       x = Float.valueOf(splittitlestring[4]).floatValue();
       y = Float.valueOf(splittitlestring[5]).floatValue();
       z = Float.valueOf(splittitlestring[6]).floatValue();
       
       
       String[] splitenergystring = energyLine.split(" ");
       //System.out.println("splitenergystring: "+splitenergystring);
       /*for(int i = 0;i<splitenergystring.length;i++){
         System.out.println("sub splitenergystring: "+splitenergystring[i]);
       }*/
       
      
       energy = Float.valueOf(splitenergystring[7]).floatValue();
       System.out.println("energy: ^^^"+energy);
    
     }
    
    
    return new DataVertex(new Point3f(x,y,z), energy);
  }
  
  public static DataVertex[] loadGaussianOutFiles(String folderLocation){
    ArrayList<DataVertex> dataVertices = new ArrayList<DataVertex>();
    
    //String curDir = System.getProperty("user.dir")+"\\RA\\";
    File dir = new File(folderLocation);

    String[] filesInDir = dir.list();
    
    for(int i=0;i<filesInDir.length;i++){
      if(filesInDir[i].endsWith(".out")&&filesInDir[i].startsWith("RA")){
        DataVertex toBeAdded = loadGaussianOutFile(folderLocation+"/"+filesInDir[i]);
        if(toBeAdded!=null)dataVertices.add(toBeAdded);
      }
    }
    //if(DEBUG)System.out.println("Printing DataVertices");
    //if(DEBUG)for(int i=0;i<E.length;i++)System.out.println(E[i]);
        
    Object[] obs = dataVertices.toArray();
    //if(DEBUG)System.out.println("converting data types to datavertex");
    DataVertex[] data = new DataVertex[obs.length];
    for(int i=0;i<obs.length;i++){
      data[i]=(DataVertex) obs[i];
      System.out.println(data[i]);
    }
    isosurfacePES.writeVertexDataFile(folderLocation, data,"energy");
    //if(DEBUG)System.out.println("converted data types to datavertex");
    return data; 
  }
  
  public static void writeVertexDataFile(String location,DataVertex[] data,String fileName){
    String filename = location+"/VertexDataFile "+fileName+".txt";
    
    //writes the vertex data plus the energy in an easy to read file for data analysis.
    if (DEBUG) {System.out.println("writeVertexDataFile :"+filename);}
    
    PrintWriter outputprintwriter;
    try {
      outputprintwriter = new PrintWriter(filename);
      for(int i =0;i<data.length;i++){
        outputprintwriter.println(data[i].toString());
      }
      boolean diditfail=outputprintwriter.checkError();
      if (DEBUG) {System.out.println("diditfail:"+diditfail);}
      outputprintwriter.close();
    } catch (FileNotFoundException e) {
      System.out.println("writeVertexDataFile exception");
    }
    //writing the file itself
    if (DEBUG) {System.out.println("finished VertexDataFile");}
    
  }
  
  
  public static void loadIsosurfacePES(Viewer theViewer,String folderLocation, String resolution, int colorScheme){
    
    if(DEBUG)System.out.println("loadIsosurfacePES folderLocation: "+folderLocation);
    
    theViewer.runScriptImmediately("isosurface delete"); //clears any leftover isosurfaces
    theViewer.runScriptImmediately("isosurface resolution "+resolution+" molecular dots mesh translucent");
    
    DataVertex[] energyData = loadGaussianOutFiles(folderLocation);
    float min = energyData[0].energy;
    float max = energyData[0].energy;
    float average=energyData[0].energy/energyData.length;
    
    for(int i=1;i<energyData.length;i++){
      average+=(energyData[i].energy/energyData.length);
      if(energyData[i].energy > max){max=energyData[i].energy;}
      else if(energyData[i].energy < min){min=energyData[i].energy;}
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
    if(DEBUG){System.out.println("shapes length: "+shapes.length);}
    for(int i=0;i<shapes.length;i++){
      if(DEBUG){System.out.println("finding mesh loop:  "+shapes[i]);}
      if(shapes[i] instanceof MeshCollection){
        if(DEBUG){System.out.println("found mesh collection:  "+shapes[i]);}
        
        themeshcollection = (MeshCollection) shapes[i];
        for(int j=0;j<themeshcollection.meshes.length;j++){
          if(DEBUG){System.out.println("cycling through meshes:  "+themeshcollection.meshes[j]);}
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
      for(int j=0;j<energyData.length;j++){
        DataVertex currentDV = energyData[j];
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
  
  
  
  
 }

