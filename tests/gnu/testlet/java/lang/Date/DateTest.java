package gnu.testlet.java.lang.Date;

import java.util.Date;
import gnu.testlet.*;

public class DateTest implements Testlet {
    public int getExpectedPass() { return 5; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
		Date d = new Date();
		// This test is not reliable:
		// compare(d.getTime(), System.currentTimeMillis());
		Date d2 = new Date(4873984739798L);
		th.check(d2.getTime(), 4873984739798L);
		th.check(!d.equals(d2));
		th.check(d2.hashCode(), -803140168);
		th.check(d2.toString().indexOf("2124") != -1);
		d.setTime(4873984739798L);
		th.check(d.equals(d2));
	}
}
