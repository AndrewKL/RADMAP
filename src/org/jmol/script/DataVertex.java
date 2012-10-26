package org.jmol.script;

import javax.vecmath.Point3f;

public class DataVertex{
  public Point3f xyz;
  public float energy;
  
  public DataVertex(Point3f point,float energyin){
    xyz = point;
    energy = energyin;
  }
  @Override
  public String toString(){
    //return "x: "+xyz.x+"  y: "+xyz.y+"  z: "+xyz.z+" E: "+energy;
    return " "+xyz.x+" "+xyz.y+" "+xyz.z+" "+energy;
  }
  
  public Point3f getPoint3f(){
    return xyz;
  }
}