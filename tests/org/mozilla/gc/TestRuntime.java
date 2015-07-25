package org.mozilla.gc;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestRuntime implements Testlet {
    public int getExpectedPass() { return 5; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness th) {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();

        th.check(totalMemory > freeMemory, "Total memory is strictly greater than free memory");
        th.check(freeMemory > 0, "Free memory is strictly greater than 0");

        for (int i = 0; i < 1000; i++) {
            long[] array = new long[1000];
        }

        th.check(totalMemory, Runtime.getRuntime().totalMemory(), "Total memory doesn't change");
        th.check(freeMemory > Runtime.getRuntime().freeMemory(), "Free memory decreases after allocating some objects");

        freeMemory = Runtime.getRuntime().freeMemory();

        Runtime.getRuntime().gc();

        th.check(freeMemory < Runtime.getRuntime().freeMemory(), "Free memory increases after a collection");
    }
}
