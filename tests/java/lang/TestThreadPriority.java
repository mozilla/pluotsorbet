package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestThreadPriority implements Testlet {
    private static String result = "";

    class Prioritized extends Thread {
        public Prioritized(int priority) {
            setPriority(priority);
        }

        public void run() {
            TestThreadPriority.result += this.getPriority() + " ";
        }
    }

    public void test(TestHarness th) {
        int[] priorities = new int[] {1, 9, 5, 7};
        for (int i = 0; i < priorities.length; i++) {
            new Prioritized(priorities[i]).start();
        }

        String expected = "9 7 5 1 ";
        try {
            while (result.length() < expected.length()) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            th.fail("unexpected InterruptedException");
        }

        th.check(result, expected);
    }
}
