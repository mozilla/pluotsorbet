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

import java.io.IOException;

class TestThrownException implements Testlet {
  private static void testHardwareException() {
    int i = 1;
    int j = 0;
    int k = i / j;
  }

  private static void testSoftwareException() {
    Float f = Float.valueOf("abc");
  }

  private static void testUserException() throws IOException {
    throw new IOException();
  }

  private static void testRethrownException() throws Exception {
    try {
      throw new Exception();
    } catch (Exception e) {
      throw e;
    }
  }

  private static void testNullException() {
    Object foo = null;
    foo.hashCode();
  }

  private static void testReThrownThruSynchronizedSection() throws Exception {
    Object lock = new Object();
    synchronized (lock) {
      try {
        throw new RuntimeException("MyException");
      } catch (Exception e) {
        throw e;
      }
    }
  }

  static void trouble(int choice) throws Exception {
    if (choice == 1) testHardwareException();
    if (choice == 2) testSoftwareException();
    if (choice == 3) testUserException();
    if (choice == 4) testRethrownException();
    if (choice == 5) testNullException();
    if (choice == 6) testReThrownThruSynchronizedSection();
  }

  public void test(TestHarness th) {
    for (int i = 1; i <= 6; ++i) {
      try {
        trouble(i);
        th.fail("Exception expected");
      } catch (Exception e) {
        th.check(true, "Exception expected");
      }
    }
  }
}
