package org.jmol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import javax.vecmath.Point3f;

import org.junit.jupiter.api.Test;

class DataVertexTest {

  @Test
  void holdsPointAndValue() {
    Point3f point = new Point3f(1, 2, 3);
    DataVertex vertex = new DataVertex(point, -0.5f);

    assertSame(point, vertex.getPoint3f());
    assertEquals(-0.5f, vertex.energy);
  }

  @Test
  void printsAsSpaceSeparatedRow() {
    assertEquals(" 1.0 2.0 3.0 -0.5", new DataVertex(new Point3f(1, 2, 3),
        -0.5f).toString());
  }
}
