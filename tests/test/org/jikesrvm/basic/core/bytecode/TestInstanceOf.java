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

class TestInstanceOf implements Testlet {
  public int getExpectedPass() { return 10; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  static TestHarness th;

  static class Science {}
  static class Magic extends Science {}

  public void test(TestHarness th) {
    this.th = th;

    runTest("Magic()", new Magic(), "1110000000");
    runTest("Magic[2]", new Magic[2], "1001110000");
    runTest("Object[][]{new Magic[4],new Magic[4]}", new Object[][]{new Magic[4], new Magic[4]}, "1001001000");
    runTest("Magic[][]{new Magic[4],new Magic[4]}", new Magic[][]{new Magic[4], new Magic[4]}, "1001001110");
    runTest("int[2]", new int[2], "1000000001");
  }

  private static void runTest(final String name, final Object x3, String expected) {
    th.check(testInstanceOf(x3), expected);
    th.check(testCasts(x3), expected);
  }

  private static String testCasts(final Object x) {
    String result = "";

    try { final Object o = (Object) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }
    try { final Science o = (Science) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }
    try { final Magic o = (Magic) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }
    try { final Object[] o = (Object[]) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }
    try { final Magic[] o = (Magic[]) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }
    try { final Science[] o = (Science[]) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }
    try { final Object[][] o = (Object[][]) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }
    try { final Magic[][] o = (Magic[][]) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }
    try { final Science[][] o = (Science[][]) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }
    try { final int[] o = (int[]) x; result += "1"; } catch (final ClassCastException cce) { result += "0"; }

    return result;
  }

  private static String testInstanceOf(final Object x) {
    String result = "";

    result += (x instanceof Object) ? "1" : "0";
    result += (x instanceof Science) ? "1" : "0";
    result += (x instanceof Magic) ? "1" : "0";
    result += (x instanceof Object[]) ? "1" : "0";
    result += (x instanceof Science[]) ? "1" : "0";
    result += (x instanceof Magic[]) ? "1" : "0";
    result += (x instanceof Object[][]) ? "1" : "0";
    result += (x instanceof Science[][]) ? "1" : "0";
    result += (x instanceof Magic[][]) ? "1" : "0";
    result += (x instanceof int[]) ? "1" : "0";

    return result;
  }
}
