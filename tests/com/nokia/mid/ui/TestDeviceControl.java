package com.nokia.mid.ui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestDeviceControl implements Testlet {
    public void test(TestHarness th) {
        try {
            DeviceControl.stopVibra();

            DeviceControl.startVibra(0, 0);
            DeviceControl.stopVibra();

            try {
                DeviceControl.startVibra(-1, 100);
                th.fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException e) {
                th.check(true);
            }

            try {
                DeviceControl.startVibra(200, 100);
                th.fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException e) {
                th.check(true);
            }

            try {
                DeviceControl.startVibra(50, -1);
                th.fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException e) {
                th.check(true);
            }

            DeviceControl.startVibra(100, 100);
            Thread.sleep(50);
            DeviceControl.stopVibra();

            // All done.
            th.check(true);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
