package gnu.testlet.vm;

import gnu.testlet.*;

public class ThrowableTest implements Testlet {
	public int getExpectedPass() { return 4; }
	public int getExpectedFail() { return 0; }
	public int getExpectedKnownFail() { return 0; }
	public void test(TestHarness th) {
		Throwable t = new NullPointerException();
		String s = t.getClass().getName();
		th.check(t.toString().equals(s));
		th.check(t.getMessage() == null);
		t = new OutOfMemoryError("test");
		s = t.getClass().getName() + ": " + t.getMessage();
		th.check(t.getMessage().equals("test"));
		th.check(t.toString().equals(s));
	}
}
