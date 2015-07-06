package com.sun.midp.midlet;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import javax.microedition.midlet.MIDlet;
import javax.microedition.io.ConnectionNotFoundException;

public class AddContactTest implements Testlet {
    public int getExpectedPass() { return 3; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    native boolean hasNumber();
    native String getNumber();

    public void test(TestHarness th) {
        try {
            MIDletPeer midletPeer = new MIDletPeer();

            midletPeer.platformRequest("x-contacts:add?number=3393333333");
            th.check(hasNumber());
            th.check(getNumber(), "3393333333");

            midletPeer.platformRequest("x-contacts:add?");
            th.check(!hasNumber());
        } catch (ConnectionNotFoundException e) {
            th.fail("Unexpected exception: " + e);
        }
    }
};
