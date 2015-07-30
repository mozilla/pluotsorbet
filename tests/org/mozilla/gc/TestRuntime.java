package org.mozilla.gc;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestRuntime implements Testlet {
    public int getExpectedPass() { return 5; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    void collectAll() {
        long freeMemory;
        long endFreeMemory;
        do {
            freeMemory = Runtime.getRuntime().freeMemory();
            Runtime.getRuntime().gc();
            endFreeMemory = Runtime.getRuntime().freeMemory();
        } while (endFreeMemory > freeMemory);
    }

    public void test(TestHarness th) {
        System.out.println("freeMemory0: " + Runtime.getRuntime().freeMemory());

        collectAll();

        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();

        System.out.println("freeMemory1: " + Runtime.getRuntime().freeMemory());

        th.check(totalMemory > freeMemory, "Total memory is strictly greater than free memory");
        th.check(freeMemory > 0, "Free memory is strictly greater than 0");

        long[] array = new long[1048576];

        System.out.println("freeMemory2: " + Runtime.getRuntime().freeMemory());

        th.check(totalMemory, Runtime.getRuntime().totalMemory(), "Total memory doesn't change");
        th.check(freeMemory > Runtime.getRuntime().freeMemory(), "Free memory decreases after allocating some objects");

        freeMemory = Runtime.getRuntime().freeMemory();

        array = null;

        collectAll();

        System.out.println("freeMemory3: " + Runtime.getRuntime().freeMemory());

        th.check(freeMemory < Runtime.getRuntime().freeMemory(), "Free memory increases after a collection");
    }
}
