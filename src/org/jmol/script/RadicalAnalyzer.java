package org.jmol.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import org.jmol.g3d.Graphics3D;
import org.jmol.modelset.ModelSet;
import org.jmol.shape.Mesh;
import org.jmol.shape.MeshCollection;
import org.jmol.shape.Shape;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.Viewer;

public class RadicalAnalyzer {
  static final boolean DEBUG=true;
  
  private final static int GRAY = 0xFF808080;
  

  public final static String BYELEMENT_PREFIX  = "byelement";
  public final static String BYRESIDUE_PREFIX = "byresidue";
  private final static String BYELEMENT_JMOL = BYELEMENT_PREFIX + "_jmol"; 
  private final static String BYELEMENT_RASMOL = BYELEMENT_PREFIX + "_rasmol";
  private final static String BYRESIDUE_SHAPELY = BYRESIDUE_PREFIX + "_shapely"; 
  private final static String BYRESIDUE_AMINO = BYRESIDUE_PREFIX + "_amino"; 
  
  public final static int CUSTOM = -1;
  public final static int ROYGB = 0;
  public final static int BGYOR = 1;
  public final static int JMOL = 2;
  public final static int RASMOL = 3;
  public final static int SHAPELY = 4;
  public final static int AMINO = 5;
  public final static int RWB   = 6;
  public final static int BWR   = 7;
  public final static int LOW   = 8;
  public final static int HIGH  = 9;
  public final static int BW  = 10;
  public final static int WB  = 11;
  public final static int USER = -12;
  public final static int RESU = -13;
  public final static int ALT = 14; // == 0

  
  
  public static String generateGaussianMoleculeSpecification(ModelSet theModelSet){
    String EOL= System.getProperty("line.separator");
    

    String moleculestring = "";
    Point3f currentAtomLocation;
    String currentMoleculeSpecificationLine;
    for(int i=0;i<theModelSet.atoms.length;i++){
      currentAtomLocation = theModelSet.getAtomPoint3f(i);
      currentMoleculeSpecificationLine = theModelSet.getElementSymbol(i)+" 0 "+currentAtomLocation.x+" "+currentAtomLocation.y+" "+currentAtomLocation.z+" "+EOL;
      System.out.println(currentMoleculeSpecificationLine);
      moleculestring += currentMoleculeSpecificationLine;
    }
    
    return moleculestring;
  }
  
  public static int generateGaussianInputFile(ModelSet theModelSet){
    String filename = "testfilemodelset.gjf";
    String percentSection = "%chk=JMOLgeneratedInputeFile.chk";
    String routeSection = "# hf sp test";
    String titleSection = "title section ";
    int charge = 0;
    int multiplicity = 1;
    String moleculeSpecification = generateGaussianMoleculeSpecification(theModelSet);
    
    return generateGaussianInputFile(filename,percentSection,routeSection,titleSection,charge,multiplicity,moleculeSpecification);
    
  }
  
  public static int generateGaussianInputFile(){
  //this is a test function
    String EOL = System.getProperty("line.separator");

    String filename = "testfile.gjf";
    String percentSection = "asdfasdfasdF";
    String routeSection = "asdfasdfasdfasdf";
    String titleSection = "title section asdfasdfsdfasdf";
    int charge = 0;
    int multiplicity = 1;
    String moleculeSpecification = "H 0.000 0.000 0.000"+EOL+"H 1.000 0.000 0.000";
    
    return generateGaussianInputFile(filename,percentSection,routeSection,titleSection,charge,multiplicity,moleculeSpecification);
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
  
  public static int generateBatchFileForIsosurface(Mesh theMesh){
    


    PrintWriter batchFilePrintWriter;
    String currentDir = System.getProperty("user.dir")+"/RA/";
    String filename;
    int errornumber = 0;
    try{
      //if(DEBUG){System.out.println("starting printwriter: gogogogogogo");}
      if(DEBUG){System.out.println("batch file location:   "+currentDir+"RA Batch File.bcf");}
      
      batchFilePrintWriter = new PrintWriter(currentDir+"RA Batch File.bcf");
      batchFilePrintWriter.println("!");
      batchFilePrintWriter.println("! batchfile list");
      batchFilePrintWriter.println("!start=1");
      batchFilePrintWriter.println("!");
      //write out the vertices into output files
      
      
      
      for(int i=0;i<theMesh.vertices.length;i++){
        if(DEBUG){System.out.println("Batch File current vertex:  "+theMesh.vertices[i]);}
        if(theMesh.vertices[i]!=null){
          //float x =theMesh.vertices[i].x;
          //float y =theMesh.vertices[i].y;
          //float z =theMesh.vertices[i].z;
          //String linetoprint="H 0 "+x+" "+y+" "+z;
          filename = "RA  "+i;
          
          
          //writing gaussian file for vertex
          //if(DEBUG){System.out.println("linetoprint:  "+linetoprint);}
          
          
          //adding line to the batch file
          if(DEBUG){System.out.println("adding line to batch file:  "+filename+".gjf"+" , "+filename+".out");}
          batchFilePrintWriter.println(filename+".gjf"+" , "+filename+".out");
        }
        
      }
      batchFilePrintWriter.close();
    }catch (FileNotFoundException e) {
      System.out.println("error: file not found");
    }


    return errornumber;
  }
  
  public static int generateIsosurfaceGaussianFiles(Viewer theViewer){
    if(DEBUG){System.out.println("generatingIsosurfaceGaussianFiles");}
    
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
    
    String molecularSection = generateGaussianMoleculeSpecification(theViewer.getModelSet());
    String filename;
    String percentSection="%mem=6MW";
    String routeSection="#opt(MaxCycles=2) ub3lyp/6-31g"; 
    String titleSection; 
    int charge = 1; 
    int multiplicity =1; 
    
    int errornumber = 0;

    String currentDir = System.getProperty("user.dir")+"/RA/";
    
    for(int i=0;i<theMesh.vertices.length;i++){
      if(DEBUG){System.out.println("current vertex:  "+theMesh.vertices[i]);}
      if(theMesh.vertices[i]!=null){
        float x =theMesh.vertices[i].x;
        float y =theMesh.vertices[i].y;
        float z =theMesh.vertices[i].z;
        String linetoprint="H 0 "+x+" "+y+" "+z;
        filename = "RA  "+i;
        titleSection="RA "+linetoprint;
        
        //writing gaussian file for vertex
        if(DEBUG){System.out.println("linetoprint:  "+linetoprint);}
        errornumber += generateGaussianInputFile(currentDir+filename+".gjf",
            percentSection,
            routeSection,
            titleSection,
            charge,
            multiplicity,
            molecularSection+linetoprint);
        

        
        
      }
    }
    
    //creating batchfile
    
    errornumber += generateBatchFileForIsosurface(theMesh);
    


    return errornumber;
    
  }
  
  

  

  public static DataVertex loadGaussianOutFile(String path){
    float x = 0;
    float y = 0;
    float z = 0;
    float energy = 0;
    BufferedReader br = null;
    String title ="";
    String energyLine="";
    boolean foundtitle = false;
    boolean foundenergy = false;
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
       System.out.println("energy: "+energy);
    
     }
    
    
    return new DataVertex(new Point3f(x,y,z), energy);
  }
  public static DataVertex loadGaussianOutFile(){
    String curDir = System.getProperty("user.dir");
    File dir = new File(curDir+"\\RA\\");
    
    return loadGaussianOutFile(dir+"\\RA  0.out");
  }
  
  public static DataVertex[] loadGaussianOutFiles(){
    ArrayList<DataVertex> dataVertices = new ArrayList<DataVertex>();
    
    String curDir = System.getProperty("user.dir")+"\\RA\\";
    File dir = new File(curDir);

    String[] filesInDir = dir.list();
    
    for(int i=0;i<filesInDir.length;i++){
      if(filesInDir[i].endsWith(".out")){
        //if(DEBUG)System.out.println("Adding File: "+filesInDir[i]);
        dataVertices.add(loadGaussianOutFile(curDir+filesInDir[i]));
      }
    }
    //Object[] E = dataVertices.toArray();
    //if(DEBUG)System.out.println("Printing DataVertices");
    //if(DEBUG)for(int i=0;i<E.length;i++)System.out.println(E[i]);
    
    Object[] obs = dataVertices.toArray();
    //if(DEBUG)System.out.println("converting data types to datavertex");
    DataVertex[] data = new DataVertex[obs.length];
    for(int i=0;i<obs.length;i++)data[i]=(DataVertex) obs[i];
    //if(DEBUG)System.out.println("converted data types to datavertex");
    return data; 
  }
  
  public static void loadRadicalAnalyzer(Viewer theViewer){
    
    DataVertex[] energyData = loadGaussianOutFiles();
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
    
    
    if(DEBUG)System.out.println("Matching Vertices");
    
    theMesh.vertexColixes = new short[theMesh.vertexCount];
    theMesh.isColorSolid = false;
    
    //generating energy data
    for(int i=0;i<theMesh.vertexCount;i++){
      for(int j=0;j<energyData.length;j++){
        if(theMesh.vertices[i].x==energyData[j].xyz.x 
            && theMesh.vertices[i].y==energyData[j].xyz.y 
            && theMesh.vertices[i].z==energyData[j].xyz.z){
          if(DEBUG)System.out.println("match found EnergyData[j]:  "+energyData[j]);
          if(DEBUG)System.out.println("VertexColixes[i]:  "+theMesh.vertexColixes[i]);
          theMesh.vertexColixes[i]=getColorIndexFromPalette(energyData[j].energy,min,max,true);
          if(DEBUG)System.out.println("Colix Set");
        }
      }
    }
    
    
  }
  
  public static void loadRadicalAnalyzerGaussian(Viewer theViewer){
    
    DataVertex[] energyData = loadGaussianOutFiles();
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
    
    
    if(DEBUG)System.out.println("Matching Vertices");
    
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
      theMesh.vertexColixes[i]=getColorIndexFromPalette(numerator/denominator,min,max,true);
      if(DEBUG)System.out.println("Colix Set: "+getColorIndexFromPalette(numerator/denominator,min,max,true));
    }
    
    
  }
  
  public static short getColorIndexFromPalette(float val, float lo,
                                        float hi,
                                        boolean isTranslucent) {
    //if(DEBUG)System.out.println("getColorIndexFromPalette go");
    short colix = Graphics3D.getColix(getArgbFromPalette(val, lo, hi));
    if (isTranslucent) {
      float f = (hi - val) / (hi - lo); 
    if (f > 1)
      f = 1; // transparent
    else if (f < 0.125f) // never fully opaque
      f = 0.125f;
    colix = Graphics3D.getColixTranslucent(colix, true, f);
    }
    return colix;
   }
  public static int getArgbFromPalette(float val, float lo, float hi) {
    if (Float.isNaN(val))
      return GRAY;
    
      return JmolConstants.argbsRoygbScale[quantize(val, lo, hi, JmolConstants.argbsRoygbScale.length)];
    
    
   
  }
  
  public final static int quantize(float val, float lo, float hi, int segmentCount) {
    /* oy! Say you have an array with 10 values, so segmentCount=10
     * then we expect 0,1,2,...,9  EVENLY
     * If f = fractional distance from lo to hi, say 0.0 to 10.0 again,
     * then one might expect 10 even placements. BUT:
     * (int) (f * segmentCount + 0.5) gives
     * 
     * 0.0 ---> 0
     * 0.5 ---> 1
     * 1.0 ---> 1
     * 1.5 ---> 2
     * 2.0 ---> 2
     * ...
     * 8.5 ---> 9
     * 9.0 ---> 9
     * 9.5 ---> 10 --> 9
     * 
     * so the first bin is underloaded, and the last bin is overloaded.
     * With integer quantities, one would not notice this, because
     * 0, 1, 2, 3, .... --> 0, 1, 2, 3, .....
     * 
     * but with fractional quantities, it will be noticeable.
     * 
     * What we really want is:
     * 
     * 0.0 ---> 0
     * 0.5 ---> 0
     * 1.0 ---> 1
     * 1.5 ---> 1
     * 2.0 ---> 2
     * ...
     * 8.5 ---> 8
     * 9.0 ---> 9
     * 9.5 ---> 9
     * 
     * that is, no addition of 0.5. 
     * Instead, I add 0.0001, just for discreteness sake.
     * 
     * Bob Hanson, 5/2006
     * 
     */
    float range = hi - lo;
    if (range <= 0 || Float.isNaN(val))
      return segmentCount / 2;
    float t = val - lo;
    if (t <= 0)
      return 0;
    float quanta = range / segmentCount;
    int q = (int)(t / quanta + 0.0001f);  //was 0.5f!
    if (q >= segmentCount)
      q = segmentCount - 1;
    return q;
  }

  
  
  public static void main(String args[]){
    
    
    
  }
  
  

}
