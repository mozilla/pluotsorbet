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

public class TestCompare implements Testlet {
  TestHarness th;

  public void test(TestHarness th) {
    this.th = th;

    zero_cmp();
    i_cmp();
    l_cmp();
    f_cmp();
    d_cmp();
    a_cmp();
    null_cmp();
    str_cmp();
  }

  void zero_cmp() {
    int i = -1;

    if (i != 0) th.check(true); else th.fail(); // ifeq
    if (i == 0) th.fail(); else th.check(true); // ifne
    if (i >= 0) th.fail(); else th.check(true); // iflt
    if (i <  0) th.check(true); else th.fail(); // ifge
    if (i <= 0) th.check(true); else th.fail(); // ifgt
    if (i >  0) th.fail(); else th.check(true); // ifle
  }

  void i_cmp() {
    int i = -1;
    int j = 0;

    if (i != j) th.check(true); else th.fail(); // if_icmpeq
    if (i == j) th.fail(); else th.check(true); // if_icmpne
    if (i >= j) th.fail(); else th.check(true); // if_icmplt
    if (i <  j) th.check(true); else th.fail(); // if_icmpge
    if (i <= j) th.check(true); else th.fail(); // if_icmpgt
    if (i >  j) th.fail(); else th.check(true); // if_icmple
  }

  void l_cmp() {
    long a = 1;
    long b = 2;

    if (a <  b) th.check(true); else th.fail(); // lcmp(-1)
    if (a == b) th.fail(); else th.check(true);
    if (a >  b) th.fail(); else th.check(true);

    if (a <  a) th.fail(); else th.check(true);
    if (a == a) th.check(true); else th.fail(); // lcmp(0)
    if (a >  a) th.fail(); else th.check(true);

    if (b <  a) th.fail(); else th.check(true);
    if (b == a) th.fail(); else th.check(true);
    if (b >  a) th.check(true); else th.fail(); // lcmp(1)
  }

  void f_cmp() {
    float a = 1;
    float b = 2;

    if (a <  b) th.check(true); else th.fail(); // fcmp[lg](-1)
    if (a == b) th.fail(); else th.check(true);
    if (a >  b) th.fail(); else th.check(true);

    if (a <  a) th.fail(); else th.check(true);
    if (a == a) th.check(true); else th.fail(); // fcmp[lg](0)
    if (a >  a) th.fail(); else th.check(true);

    if (b <  a) th.fail(); else th.check(true);
    if (b == a) th.fail(); else th.check(true);
    if (b >  a) th.check(true); else th.fail(); // fcmp[lg](1)
  }

  void d_cmp() {
    double a = 1;
    double b = 2;

    if (a <  b) th.check(true); else th.fail(); // dcmp[lg](-1)
    if (a == b) th.fail(); else th.check(true);
    if (a >  b) th.fail(); else th.check(true);

    if (a <  a) th.fail(); else th.check(true);
    if (a == a) th.check(true); else th.fail(); // dcmp[lg](0)
    if (a >  a) th.fail(); else th.check(true);

    if (b <  a) th.fail(); else th.check(true);
    if (b == a) th.fail(); else th.check(true);
    if (b >  a) th.check(true); else th.fail(); // dcmp[lg](1)
  }

  void a_cmp() {
    Object a = null;
    Object b = null;

    if (a == b) th.check(true); else th.fail(); // if_acmpne
    if (a != b) th.fail(); else th.check(true); // if_acmpeq
  }

  void null_cmp() {
    Object o = null;

    if (o == null) th.check(true); else th.fail(); // ifnonnull
    if (o != null) th.fail(); else th.check(true); // ifnull
   }

  void str_cmp() {
    String s1 = "abc";
    String s2 = "abc";
    String s3 = "ab";
    s3 = s3 + "c";
    th.check(s1 == s2);
    th.check(!(s1 == s3));
  }
}
