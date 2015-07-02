package org.mozilla.regression;

import java.util.Vector;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestVectorNull implements Testlet {
  public int getExpectedPass() { return 4; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  static native Object nativeThatReturnsNull();

  public void test(TestHarness th) {
    Vector a = new Vector();

    // Add a null element
    a.addElement(null);
    th.check(a.size(), 1);

    // Add a null value returned by a native
    a.addElement(nativeThatReturnsNull());
    th.check(a.size(), 2);

    // Add a null value from an array
    Object[] array = new Object[7];
    th.check(array[0], null);
    a.addElement(array[0]);
    th.check(a.size(), 3);
  }
}
