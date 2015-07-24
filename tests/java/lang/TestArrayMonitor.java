package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestArrayMonitor implements Testlet {
    public int getExpectedPass() { return 1; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    private final Object[] a = new Object[7];
    private final Object[][] b = new Object[7][];
    boolean done = false;

    class NotifyThreadArray extends Thread {
        public void run() {
            synchronized (a) {
                done = true;
                a.notify();
            }
        }
    }

    class NotifyThreadMultiArray extends Thread {
        public void run() {
            synchronized (b) {
                done = true;
                b.notify();
            }
        }
    }

    public void test(TestHarness th) {
        Thread notifyThreadArray = new NotifyThreadArray();
        notifyThreadArray.start();

        try {
            synchronized (a) {
                while (!done) {
                    a.wait();
                }
            }

            notifyThreadArray.join();
        } catch (InterruptedException e) {
            th.fail("Unexpected exception: " + e);
        }

        done = false;

        Thread notifyThreadMultiArray = new NotifyThreadMultiArray();
        notifyThreadMultiArray.start();

        try {
            synchronized (b) {
                while (!done) {
                    b.wait();
                }
            }

            notifyThreadMultiArray.join();
        } catch (InterruptedException e) {
            th.fail("Unexpected exception: " + e);
        }

        th.check(true);
    }
}

