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

/**
 * According the the definition of instanceof the method must be resolved prior to any other checks.
 * See http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#instanceof.Description
 *
 * Also described in [ 1147107 ]  unresolved instanceof etc. on &lt;null&gt; not compliant.
 */
public class TestResolveOnInstanceof implements Testlet {
  public int getExpectedPass() { return 1; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  static interface A {
    String genString();
  }

  public void test(TestHarness th) {
    th.check(!doInstanceOf(new Object()));
  }

  static boolean doInstanceOf(final Object a) {
    return a instanceof A;
  }
}
