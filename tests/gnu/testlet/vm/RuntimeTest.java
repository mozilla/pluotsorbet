package gnu.testlet.vm;

import gnu.testlet.*;

public class RuntimeTest implements Testlet {
    public void test(TestHarness th) {
		Runtime r = Runtime.getRuntime();
		th.check(r.freeMemory() < r.totalMemory());
		r.gc();
		// r.exit(99);
	}
}
