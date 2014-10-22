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

class TestSwitch implements Testlet {
  public void test(TestHarness th) {
    int j;

    // tableswitch
    String actual = "";
    for (int i = 9; i < 13; i += 1) {
      switch (i) {
        case 10:
          j = 10;
          break;
        case 11:
          j = 11;
          break;
        case 12:
          j = 12;
          break;
        case 13:
          j = 13;
          break;
        case 14:
          j = 14;
          break;
        case 15:
          j = 15;
          break;
        case 16:
          j = 16;
          break;
        case 17:
          j = 17;
          break;
        case 18:
          j = 18;
          break;
        default:
          j = 99;
          break;
      }
      actual += j;
    }
    th.check(actual, "99101112");

    // lookupswitch
    actual = "";
    for (int i = 0; i < 70; i += 10) {
      switch (i) {
        case 10:
          j = 10;
          break;
        case 20:
          j = 20;
          break;
        case 30:
          j = 30;
          break;
        case 40:
          j = 40;
          break;
        case 50:
          j = 50;
          break;
        default:
          j = 99;
          break;
      }
      actual += j;
    }
    th.check(actual, "99102030405099");
  }
}
