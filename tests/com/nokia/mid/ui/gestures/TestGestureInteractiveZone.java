package com.nokia.mid.ui.gestures;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestGestureInteractiveZone implements Testlet {
    public int getExpectedPass() { return 10; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness th) {
        try {
            GestureInteractiveZone giz = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_TAP);

            th.check(giz.supports(GestureInteractiveZone.GESTURE_TAP), "GestureInteractiveZone supports GESTURE_TAP");
            th.check(!giz.supports(GestureInteractiveZone.GESTURE_LONG_PRESS), "GestureInteractiveZone doesn't support GESTURE_LONG_PRESS");

            th.check(giz.contains(5, 5), "GestureInteractiveZone contains any point if no rect is set");

            giz.setRectangle(5, 5, 50, 50);

            th.check(giz.contains(5, 5), "GestureInteractiveZone contains (5,5)");
            th.check(giz.contains(55, 55), "GestureInteractiveZone contains (55,55)");
            th.check(!giz.contains(4, 5), "GestureInteractiveZone doesn't contain (4,5)");
            th.check(!giz.contains(5, 4), "GestureInteractiveZone doesn't contain (5,4)");
            th.check(!giz.contains(55, 56), "GestureInteractiveZone doesn't contain (55,56)");
            th.check(!giz.contains(56, 55), "GestureInteractiveZone doesn't contain (56,55)");

            giz.setRectangle(1, 1, 30, 30);
            th.check(giz.contains(4,5), "GestureInteractiveZone now contains (4,5)");
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
