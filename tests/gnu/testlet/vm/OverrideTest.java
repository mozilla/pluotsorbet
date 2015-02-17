package gnu.testlet.vm;

import gnu.testlet.*;

public class OverrideTest implements Testlet {
    public int getExpectedPass() { return 2; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    static int overridden() {
        return 1;
    }

    public void test(TestHarness th) {
        th.check(overridden(), 2);

        // Check again to make sure the method is still overridden correctly
        // after it has been called once and cached.
        th.check(overridden(), 2);
    }
}
