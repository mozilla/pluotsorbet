/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

package com.nokia.mid.ui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import com.nokia.mid.ui.VirtualKeyboard;

class TestKeyboardVisibilityListener implements com.nokia.mid.ui.KeyboardVisibilityListener {
    boolean isExpectingShow = false;
    boolean isExpectingHide = false;
    TestHarness th;

    TestKeyboardVisibilityListener(TestHarness harness) {
        th = harness;
    }

    // Making this method `synchronized` caused an
    // `IllegalMonitorStateException` to be thrown. Using a
    // `synchronized(this)` statement around the method body
    // as a workaround.
    public void showNotify(int keyboardCategory) {
        synchronized(this) {
            th.check(isExpectingShow, true);
            isExpectingShow = false;
            TestVirtualKeyboard.verifyKeyboardShown(th);
            notify();
        }
    }

    // Making this method `synchronized` caused an
    // `IllegalMonitorStateException` to be thrown. Using a
    // `synchronized(this)` statement around the method body
    // as a workaround.
    public void hideNotify(int keyboardCategory) {
        synchronized(this) {
            th.check(isExpectingHide, true);
            isExpectingHide = false;
            TestVirtualKeyboard.verifyKeyboardHidden(th);
            notify();
        }
    }
}

public class TestVirtualKeyboard extends Canvas implements Testlet {
    public native static void hideKeyboard();
    public native static void showKeyboard();

    public static void verifyKeyboardHidden(TestHarness th) {
        th.check(VirtualKeyboard.isVisible(), false);

        // We may consider checking these values but it's not clear from
        // documentation what we should expect them to be
        // th.check(VirtualKeyboard.getXPosition(), 0);
        // th.check(VirtualKeyboard.getYPosition(), /* Height of window */);
        // th.check(VirtualKeyboard.getWidth(), 0);
        // th.check(VirtualKeyboard.getHeight(), 0);
    }

    public static void verifyKeyboardShown(TestHarness th) {
        th.check(VirtualKeyboard.isVisible(), true);
        th.check(VirtualKeyboard.getXPosition(), 0);
        // th.check(VirtualKeyboard.getYPosition() + VirtualKeyboard.getHeight(), /* Window height */);
        // th.check(VirtualKeyboard.getWidth(), /* Width of window */);
    }

    public void test(TestHarness th) {
        TestKeyboardVisibilityListener listener = new TestKeyboardVisibilityListener(th);
        th.check(null == listener, false);
        VirtualKeyboard.setVisibilityListener(listener);



        // `getCustomKeyboardControl` is unimplemented
        boolean gotExpectedException = false;
        try {
            VirtualKeyboard.getCustomKeyboardControl();
        } catch (IllegalArgumentException e) {
            gotExpectedException = true;
        } catch (Exception e) {
            gotExpectedException = false;
        }

        th.check(gotExpectedException, "getCustomKeyboardControl throws IllegalArgumentException");

        // `suppressSizeChanged` is unimplemented but it doesn't throw
        VirtualKeyboard.suppressSizeChanged(true);
        th.check(true, "suppressSizeChanged called without Exception");



        verifyKeyboardHidden(th);

        // These are separate `synchronized` sections in case there are
        // notifications pending for the listener. We want them to be
        // processed between calls to `wait`.

        // Test making the keyboard visible
        synchronized(listener) {
            listener.isExpectingShow = true;
            showKeyboard();
            while(true) {
                try {
                    listener.wait();
                    break;
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }

        // Test hiding the keyboard
        synchronized(listener) {
            listener.isExpectingHide = true;
            hideKeyboard();
            while (true) {
                try {
                    listener.wait();
                    break;
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }

        VirtualKeyboard.setVisibilityListener(null);
    }

    protected void paint(Graphics graphics) {}
}
