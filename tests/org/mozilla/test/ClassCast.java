package org.mozilla.test;

import gnu.testlet.*;

public class ClassCast implements Testlet {
    public int getExpectedPass() { return 1; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    private interface Interface {}

    class ClassA implements Interface {}

    abstract class ClassB extends ClassA {}

    class ClassC extends ClassB {}

    static TestHarness th;

    public void test(TestHarness th) {
        // This test illuminates an issue where ClassB's interfaces are setup
        // in the runtime before the super class has been loaded.
        Object o = new ClassC();
        try {
            Interface iface = (Interface)o;
            th.check(true);
        } catch (ClassCastException e) {
            th.fail("Unexpected exception: " + e);
        }
    }
}
