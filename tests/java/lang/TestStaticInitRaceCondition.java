package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

class Monkey {
    public static final String name;

    static {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        name = "Monkey";
    }
}

public class TestStaticInitRaceCondition extends Thread implements Testlet {
    private TestHarness th;
    public int getExpectedPass() { return 2; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness th) {
        this.th = th;
        start();
        run();
        try {
            this.join();
        } catch (InterruptedException e) {
            th.fail();
        }
    }

    public void run() {
        th.check(Monkey.name, "Monkey");
    }
}
