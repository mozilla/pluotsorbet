package gnu.testlet.vm;

import gnu.testlet.*;

public class OverrideTest implements Testlet {
    static int overridden() {
        return 1;
    }

    public void test(TestHarness th) {
        th.check(overridden(), 2);
    }
}
