package org.jmol.script;

public class ExampleCommand {
  public static void run(){
    System.out.println("exampleCommand has been run");
  }
  public static void main(String args[]){
    ExampleCommand.run();
    System.out.println(" | OR opeartor");
    int scriptCommand            = (1 << 12);
    int expression           = (1 << 20);
    int mathfunc             = (1 << 27) | expression;  
    int write            = 21 | 0 << 9 | mathfunc | scriptCommand;
    System.out.println(" int scriptCommand            = (1 << 12);" + scriptCommand);
    System.out.println("int expression           = (1 << 20);" + expression);
    System.out.println("write"+write);
  }
}
