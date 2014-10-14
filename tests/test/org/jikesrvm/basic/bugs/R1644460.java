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
package test.org.jikesrvm.basic.bugs;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

/**
 * Test code demonstrating bug [ 1644460 ] DaCapo lusearch fails when opt compiled.
 *
 * Should be tested with -X:aos:initial_compiler=opt in at least one test-configuration.
 */
public class R1644460 implements Testlet {
  public static float b = 0.009765625f;
  public static float a = 10.57379f;

  public void test(TestHarness th) {
    final float c = a * b;
    th.check(c, 0.10325967f);
  }
}
