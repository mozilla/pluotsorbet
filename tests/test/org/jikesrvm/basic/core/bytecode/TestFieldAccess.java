/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package test.org.jikesrvm.basic.core.bytecode;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

class TestFieldAccess implements Testlet {
  public int getExpectedPass() { return 18; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }
  static boolean s0 = true;
  static byte s1 = -1;
  static char s2 = 'A';
  static short s3 = -3;
  static int s4 = -4;
  static long s5 = -5;
  static float s6 = -6;
  static double s7 = -7;
  static Object s8 = new TestFieldAccess();

  boolean x0 = true;
  byte x1 = -1;
  char x2 = 'A';
  short x3 = -3;
  int x4 = -4;
  long x5 = -5;
  float x6 = -6;
  double x7 = -7;
  Object x8 = this;

  public String toString() { return "Instance of " + getClass().getName(); }

  public void test(TestHarness th) {
    th.check(TestFieldAccess.s0);
    th.check(TestFieldAccess.s1, -1);
    th.check(TestFieldAccess.s2, 'A');
    th.check(TestFieldAccess.s3, -3);
    th.check(TestFieldAccess.s4, -4);
    th.check(TestFieldAccess.s5, -5);
    th.check(TestFieldAccess.s6, -6.0);
    th.check(TestFieldAccess.s7, -7.0);
    th.check(TestFieldAccess.s8, "Instance of test.org.jikesrvm.basic.core.bytecode.TestFieldAccess");

    TestFieldAccess b = new TestFieldAccess();
    th.check(b.x0);
    th.check(b.x1, -1);
    th.check(b.x2, 'A');
    th.check(b.x3, -3);
    th.check(b.x4, -4);
    th.check(b.x5, -5);
    th.check(b.x6, -6.0);
    th.check(b.x7, -7.0);
    th.check(b.x8, "Instance of test.org.jikesrvm.basic.core.bytecode.TestFieldAccess");
  }
}
