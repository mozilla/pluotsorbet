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

class TestFinally implements Testlet {
  public int getExpectedPass() { return 4; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }
  private static int foo() {
    try {
      int a = 1;
      int b = 0;
      return a / b;
    } catch (Exception e) {
      return 1;
    } finally {
      return 2;
    }
  }

  private static int foo2() {
    try {
      throw new Exception();
    } finally {
      return 3;
    }
  }

  public void test(TestHarness th) {
    th.check(TestFinally.foo(), 2);
    th.check(TestFinally.foo2(), 3);
    boolean done = false;
    try {
      th.check(true);      // jsr
      done = true;
      return;
    } finally {
      th.check(done);
    }                              // ret
  }
}
