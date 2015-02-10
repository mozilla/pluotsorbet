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

class TestArrayAccess implements Testlet {
  public int getExpectedPass() { return 74; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }
  TestHarness th;

  public void test(TestHarness th) {
    this.th = th;

    boolean_array();
    byte_array();
    char_array();
    short_array();
    int_array();
    long_array();
    float_array();
    double_array();
    object_array();
    array_array();
    multi_int_array();
    multi_object_array();
    multi_partial_array();
  }

  private void boolean_array() {
    final boolean[] array = new boolean[]{false, true};
    th.check(!array[0]);
    th.check(array[1]);
  }

  private void byte_array() {
    final byte[] array = new byte[]{127, -1};
    th.check(array[0], 127);
    th.check(array[1], -1);
  }

  private void char_array() {
    final char[] array = new char[]{'c', '$'};
    th.check(array[0], 'c');
    th.check(array[1], '$');
  }

  private void short_array() {
    final short[] array = new short[]{32767, -1};
    th.check(array[0], 32767);
    th.check(array[1], -1);
  }

  private void int_array() {
    final int[] array = new int[]{0, 1};
    th.check(array[0], 0);
    th.check(array[1], 1);
  }

  private void long_array() {
    final long[] array = new long[]{0, 1};
    th.check(array[0], 0);
    th.check(array[1], 1);
  }

  private void float_array() {
    final float[] array = new float[]{0, 1};
    th.check(array[0], 0);
    th.check(array[1], 1);
  }

  private void double_array() {
    final double[] array = new double[]{0, 1};
    th.check(array[0], 0);
    th.check(array[1], 1);
  }

  private void object_array() {
    final Object[] array = new Object[]{null, "s"};
    th.check(array[0] == null);
    th.check(array[1], "s");
  }

  private void array_array() {
    final Object[] array = new Object[]{null, new Object[2]};
    th.check(array[0] == null);
    th.check(array[1].getClass().getName(), "[Ljava.lang.Object;");
  }

  private void multi_int_array() {
    final int outer = 2;
    final int middle = 3;
    final int inner = 4;

    final int[][][] ary = new int[outer][middle][inner]; // multianewarray

    int n = 0;
    for (int i = 0; i < outer; ++i)
      for (int j = 0; j < middle; ++j)
        for (int k = 0; k < inner; ++k)
          ary[i][j][k] = n++;

    n = 0;
    for (int i = 0; i < outer; ++i) {
      for (int j = 0; j < middle; ++j) {
        for (int k = 0; k < inner; ++k) {
          th.check(ary[i][j][k], n++);
        }
      }
    }
  }

  private void multi_object_array() {
    final int outer = 2;
    final int middle = 3;
    final int inner = 4;

    final Integer[][][] ary = new Integer[outer][middle][inner]; // multianewarray

    int n = 0;
    for (int i = 0; i < outer; ++i)
      for (int j = 0; j < middle; ++j)
        for (int k = 0; k < inner; ++k)
          ary[i][j][k] = new Integer(n++);

    n = 0;
    for (int i = 0; i < outer; ++i) {
      for (int j = 0; j < middle; ++j) {
        for (int k = 0; k < inner; ++k) {
          th.check(ary[i][j][k], new Integer(n++));
        }
      }
    }
  }

  private void multi_partial_array() {
    final int outer = 2;
    final int middle = 3;

    final int[][][] ary = new int[outer][middle][]; // multianewarray

    for (int i = 0; i < outer; ++i) {
      for (int j = 0; j < middle; ++j) {
        th.check(String.valueOf(ary[i][j]), "null");
      }
    }
  }
}
