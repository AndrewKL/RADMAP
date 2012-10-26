import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;


public class RandomFunctions {
  
  public static void splitBatchFile(String location,String folderLocation, int numberofbatchfiles){

    PrintWriter[] batchFilePrintWriter;
    BufferedReader br = null;
    
    
    
    try{
      
      //if(DEBUG){System.out.println("batch file location:   "+location+"RA Batch File.bcf");}
      
      batchFilePrintWriter = new PrintWriter[numberofbatchfiles];
      String sCurrentLine;
      
      br = new BufferedReader(new FileReader(location));
      System.out.println("creating batch files");
      for(int i=0;i<numberofbatchfiles;i++){
        System.out.println(folderLocation+"\\partialbatchfile"+i+".bcf");
        batchFilePrintWriter[i]=new PrintWriter(folderLocation+"\\partialbatchfile"+i+".bcf");
        batchFilePrintWriter[i].println("!");
        batchFilePrintWriter[i].println("! batchfile list");
        batchFilePrintWriter[i].println("!start=1");
        batchFilePrintWriter[i].println("!");
      }
      int counter =0;
      
      System.out.println("writing files");
          
      while ((sCurrentLine = br.readLine()) != null) {
        System.out.println("Reading line: "+sCurrentLine);
        if(!sCurrentLine.startsWith("!")&&!sCurrentLine.startsWith(" !") ){
          batchFilePrintWriter[counter%numberofbatchfiles].println(sCurrentLine);
          counter++;
        }
      }
      for(int i=0;i<numberofbatchfiles;i++){
        batchFilePrintWriter[i].close();
        System.out.println("Closing batch file number: "+i);
      }
      
    }catch (FileNotFoundException e) {
      System.out.println("error: file not found");
    } catch (IOException e) {
      System.out.println("error: read error");
    }
  }
  
  public static void main(String[] args){
    JFileChooser fileChooser = new JFileChooser();
    JFileChooser folderChooser = new JFileChooser();
    String location = "";
    String folderLocation = "";
    
    folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
    int ierr = fileChooser.showDialog(null, "Set");
    if (ierr == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      location=file.getAbsolutePath();
    }
    
    ierr = folderChooser.showDialog(null, "Set");
    if (ierr == JFileChooser.APPROVE_OPTION) {
      File file = folderChooser.getSelectedFile();
      folderLocation=file.getAbsolutePath();
    }
    
    //String location = "C:\\Users\\Stranger\\Google Drive\\IsosurfacePES RA\\Calcs\\RA FLP Hdot\\RA Batch File.bcf";
    int numberofbatchfiles =3;
    splitBatchFile(location,folderLocation,numberofbatchfiles);
    
  }

}
