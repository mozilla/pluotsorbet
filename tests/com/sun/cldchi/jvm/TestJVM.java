package com.sun.cldchi.jvm;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestJVM implements Testlet {
    public int getExpectedPass() { return 10; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness harness) {
        String[] strings = {
            "str0",
            "str1",
            "str2",
            "str3",
            "str4"
        };

        JVM.unchecked_obj_arraycopy(strings, 1, strings, 3, 2);
        harness.check(strings[0], "str0");
        harness.check(strings[1], "str1");
        harness.check(strings[2], "str2");
        harness.check(strings[3], "str1");
        harness.check(strings[4], "str2");

        JVM.unchecked_obj_arraycopy(strings, 0, strings, 2, 3);
        harness.check(strings[0], "str0");
        harness.check(strings[1], "str1");
        harness.check(strings[2], "str0");
        harness.check(strings[3], "str1");
        harness.check(strings[4], "str2");
    }
}
