package com.nokia.mid.ui.frameanimator;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestFrameAnimator implements Testlet, FrameAnimatorListener {
    public int getExpectedPass() { return 13; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
        FrameAnimator animator = new FrameAnimator();
        th.check(animator.isRegistered(), false);
        th.check(FrameAnimator.getNumRegisteredFrameAnimators(), 0);

        try {
            animator.register(0, 0, (short) 0, (short) 0, null);
            th.fail("listener is null");
        } catch (Exception e) {
            th.check(e.getMessage(), "listener is null");
        }

        try {
            animator.register(-65536, 0, (short) 0, (short) 0, this);
            th.fail("coordinate out of bounds");
        } catch (Exception e) {
            th.check(e.getMessage(), "coordinate out of bounds");
        }

        try {
            animator.register(65536, 0, (short) 0, (short) 0, this);
            th.fail("coordinate out of bounds");
        } catch (Exception e) {
            th.check(e.getMessage(), "coordinate out of bounds");
        }

        try {
            animator.register(0, -65536, (short) 0, (short) 0, this);
            th.fail("coordinate out of bounds");
        } catch (Exception e) {
            th.check(e.getMessage(), "coordinate out of bounds");
        }

        try {
            animator.register(0, 65536, (short) 0, (short) 0, this);
            th.fail("coordinate out of bounds");
        } catch (Exception e) {
            th.check(e.getMessage(), "coordinate out of bounds");
        }

        animator.register(0, 0, (short) 0, (short) 0, this);
        th.check(animator.isRegistered(), true);
        th.check(FrameAnimator.getNumRegisteredFrameAnimators(), 1);

        try {
            animator.register(0, 0, (short) 0, (short) 0, this);
            th.fail("FrameAnimator already registered");
        } catch (Exception e) {
            th.check(e.getMessage(), "FrameAnimator already registered");
        }

        animator.unregister();
        th.check(animator.isRegistered(), false);
        th.check(FrameAnimator.getNumRegisteredFrameAnimators(), 0);

        try {
            animator.unregister();
            th.fail("FrameAnimator not registered");
        } catch (Exception e) {
            th.check(e.getMessage(), "FrameAnimator not registered");
        }
    }

    public void animate(FrameAnimator animator, int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame) {}
}
