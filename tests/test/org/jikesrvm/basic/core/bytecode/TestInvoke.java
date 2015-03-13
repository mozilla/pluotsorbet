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

class TestInvoke implements Testlet {
  public int getExpectedPass() { return 14; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  static TestHarness th;

  static boolean typeAinit1 = false, typeAinit2 = false, typeAinit3 = false, typeBinit1 = false, typeBinit2 = false, typeCinit = false;
  static boolean typeAf = false;

  static interface MyInterface {
    void performMagic();
  }

  static class TypeA {
    TypeA() {
      if (!typeAinit1) {
        typeAinit1 = true;
        th.check(true, "TypeA constructor called");
        return;
      }

      if (!typeAinit2) {
        typeAinit2 = true;
        th.check(true, "TypeA constructor called");
        return;
      }

      if (!typeAinit3) {
        typeAinit3 = true;
        th.check(true, "TypeA constructor called");
        return;
      }

      th.check(false, "TypeA constructor is called three times");
    }

    void f() {
      typeAf = true;
      th.check(true, "TypeA::f called");
    }
  }

  static class TypeB extends TypeA {
    TypeB() {
      if (!typeBinit1) {
        typeBinit1 = true;
        th.check(typeAinit2, "TypeA and TypeB constructors called");
        return;
      }

      if (!typeBinit2) {
        typeBinit2 = true;
        th.check(typeAinit3, "TypeA and TypeB constructors called");
        return;
      }

      th.check(false, "TypeB constructor is called two times");
    }

    //invokevirtual
    void f() {
      th.check(typeAf, "TypeA::f already called");
      th.check(true, "TypeB::f called");
    }

    //invokestatic
    static int g(int value) { return 3 + value; }
  }

  static class TypeC extends TypeB implements MyInterface {
    TypeC() {
      if (!typeCinit) {
        typeCinit = true;
        th.check(typeAinit3 && typeBinit2, "TypeA, TypeB and TypeC constructors called");
        return;
      }

      th.check(false, "TypeC constructor is called once");
    }

    void test() {
      th.check(true, "TypeC::test called");
      myPrivate();
    }

    //invokeinterface
    public void performMagic() {
      th.check(true, "TypeC::performMagic called");
    }

    //invokespecial
    private void myPrivate() {
      th.check(true, "TypeC::myPrivate called");
    }
  }

  public void test(TestHarness th) {
    this.th = th;

    final TypeA a = new TypeA();
    final TypeB b = new TypeB();
    final TypeC c = new TypeC();

    callF(a);
    callF(b);
    callPerformMagic(c);

    c.test();

    th.check(TypeB.g(39), 42);

    th.check(TypeC.g(13), 16);
  }

  private static void callF(TypeA a) {
    a.f();
  }

  private static void callPerformMagic(MyInterface myInterface) {
    myInterface.performMagic();
  }
}
