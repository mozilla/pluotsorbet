package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestThread implements Testlet {
    private boolean result = false;

    class RuntimeExceptionThread extends Thread {
        public void run() {
             throw new RuntimeException("runtime exception");
        }
    }

    public void test(TestHarness th) {
        try {
            Thread t = new RuntimeExceptionThread();
            t.start();
            t.join();
            result = true;
        } catch (InterruptedException e) {
            th.fail("unexpected InterruptedException");
        }

        th.check(result);
    }
}

