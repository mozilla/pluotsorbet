package gnu.testlet.vm;

import gnu.testlet.*;

public class ConditionsTest implements Testlet {
	public int getExpectedPass() { return 19; }
	public int getExpectedFail() { return 0; }
	public int getExpectedKnownFail() { return 0; }
	public void test(TestHarness th) {
		String a, b, c;
		a = "smt";
		b = a;
		c = "smt2";
		int d = 9;
		int e = -9;
		int f = 0;
		String g = null;
		th.check(a == b);
		th.check(a != c);
		th.check(a != null);
		th.check(g == null);
		th.check(d == 9);
		th.check(d > 7);
		th.check(d >= 9);
		th.check(d >= 7);
		th.check(7 < d);
		th.check(9 <= d);
		th.check(7 <= d);
		th.check(d != 6);
		th.check(d >= 0);
		th.check(e <= 0);
		th.check(f >= 0);
		th.check(f <= 0);
		th.check(f == 0);
		th.check(e != 0);
		th.check(d != 0);
	}
}
