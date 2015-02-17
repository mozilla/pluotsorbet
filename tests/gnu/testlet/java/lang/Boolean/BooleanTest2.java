package gnu.testlet.java.lang.Boolean;

import gnu.testlet.*;

public class BooleanTest2 implements Testlet {
    public int getExpectedPass() { return 11; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
	th.check(Boolean.TRUE.booleanValue());
	th.check(!Boolean.FALSE.booleanValue());
	Boolean b = new Boolean(true);
	Boolean b2 = new Boolean(false);
	Boolean b3 = new Boolean(true);
	th.check(b.booleanValue());
	th.check(!b2.booleanValue());
	th.check(b.toString(), "true");
	th.check(b2.toString(), "false");
	th.check(b.hashCode(), 1231);
	th.check(b2.hashCode(), 1237);
	th.check(!b.equals(null));
	th.check(!b.equals(b2));
	th.check(b.equals(b3));
    }
}
