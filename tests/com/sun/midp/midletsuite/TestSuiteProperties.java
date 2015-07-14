package com.sun.midp.midletsuite;

import gnu.testlet.MIDletTestlet;
import gnu.testlet.TestHarness;

public class TestSuiteProperties implements MIDletTestlet {
    public int getExpectedPass() { return 4; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    native boolean hasNumber();
    native String getNumber();

    public void test(TestHarness th) {
        SuiteProperties suiteProperties = new SuiteProperties(1);
        th.check(suiteProperties.getProperty("MIDlet-1"), "RunTestsMIDlet,,RunTestsMIDlet");
        th.check(suiteProperties.getProperty("MIDlet-Name"), "RunTestsMIDlet");
        th.check(suiteProperties.getProperty("MIDlet-Version"), "1.0");
        th.check(suiteProperties.getProperty("MIDlet-Vendor"), "Mozilla");
    }
};
