package org.mozilla.regression;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestGetfieldAastore implements Testlet {
    public int getExpectedPass() { return 2; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    String string = "string";
    int[] array = new int[7];
    Object[] container = new Object[2];

    public void test(TestHarness th) {
        container[0] = string;
        container[1] = array;
        th.check(container[0], string);
        th.check(container[1], array);
    }
}
