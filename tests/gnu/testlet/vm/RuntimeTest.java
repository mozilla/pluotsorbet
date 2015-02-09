package gnu.testlet.vm;

import gnu.testlet.*;

public class RuntimeTest implements Testlet {
    public int getExpectedPass() { return 1; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
		Runtime r = Runtime.getRuntime();
		th.check(r.freeMemory() < r.totalMemory());
		r.gc();
		// r.exit(99);
	}
}
