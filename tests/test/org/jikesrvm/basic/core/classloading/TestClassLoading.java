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
package test.org.jikesrvm.basic.core.classloading;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

class TestClassLoading implements Testlet {
  public int getExpectedPass() { return 9; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 6; }
  TestHarness th;

  public void test(TestHarness th) {
    this.th = th;

    doTest("java.lang.String", true, true);
    todo1Test("java.lang.Number", true, false); //Can't instantiate abstract classes
    todo2Test("java.lang.Integer", true, false); //Can't instantiate as no default constructor

    todo2Test("[Ljava.lang.String;", true, false); //Can't instantiate arrays
    todo2Test("[[Ljava.lang.String;", true, false); //Can't instantiate arrays
    todo2Test("[I", true, false); //Can't instantiate arrays
    todo2Test("[[I", true, false); //Can't instantiate arrays

    doTest("I", false, false); //Can not load classes for primitives
    doTest("NoExist", false, false); //Non existent class
  }

  private void doTest(final String classname, boolean found, boolean successful) {
    final Class c;
    try {
      c = Class.forName(classname);
      th.check(found, "Class.forName(" + classname + ") found");
    } catch (final ClassNotFoundException e) {
      th.check(!found, "Class.forName(" + classname + ") not found");
      return;
    }

    try {
      c.newInstance();
      th.check(successful, "Class.forName(" + classname + ").newInstance() successful");
    } catch (final Throwable throwable) {
      th.check(!successful, "Class.forName(" + classname + ").newInstance() not successful");
    }
  }

  private void todo1Test(final String classname, boolean found, boolean successful) {
    final Class c;
    try {
      c = Class.forName(classname);
      th.todo(found, "Class.forName(" + classname + ") found");
    } catch (final ClassNotFoundException e) {
      th.todo(!found, "Class.forName(" + classname + ") not found");
      return;
    }

    try {
      c.newInstance();
      th.check(successful, "Class.forName(" + classname + ").newInstance() successful");
    } catch (final Throwable throwable) {
      th.check(!successful, "Class.forName(" + classname + ").newInstance() not successful");
    }
  }

  private void todo2Test(final String classname, boolean found, boolean successful) {
    final Class c;
    try {
      c = Class.forName(classname);
      th.check(found, "Class.forName(" + classname + ") found");
    } catch (final ClassNotFoundException e) {
      th.check(!found, "Class.forName(" + classname + ") not found");
      return;
    }

    try {
      c.newInstance();
      th.todo(successful, "Class.forName(" + classname + ").newInstance() successful");
    } catch (final Throwable throwable) {
      th.todo(!successful, "Class.forName(" + classname + ").newInstance() not successful");
    }
  }
}
