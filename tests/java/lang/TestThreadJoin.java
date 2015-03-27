package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestThreadJoin implements Testlet {
    public int getExpectedPass() { return 1; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness th) {
        Thread t = new Thread() {
            public void run() {
            }
        };
        t.start();

        long start = System.currentTimeMillis();
        try {
            t.join();
        } catch (InterruptedException e) {
        }
        th.check(System.currentTimeMillis() - start < 500);
    }
}
