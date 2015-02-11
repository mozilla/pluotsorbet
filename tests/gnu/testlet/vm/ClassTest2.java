package gnu.testlet.vm;

import java.io.*;
import java.util.Date;
import gnu.testlet.*;

public class ClassTest2 implements Testlet {
	public int getExpectedPass() { return 11; }
	public int getExpectedFail() { return 0; }
	public int getExpectedKnownFail() { return 0; }
	public void test(TestHarness th) {
	    try {
			Class c = Class.forName("java.util.Date");
			th.check(c.getName(), "java.util.Date");
			Object o = new Object();
			th.check(!c.isArray());
			th.check(o.getClass().isAssignableFrom(c));
			th.check(!c.isAssignableFrom(o.getClass()));
			th.check(!c.isAssignableFrom("test".getClass()));
			th.check(!c.isInstance("test"));
			th.check(o.getClass().isInstance("test"));
			th.check(!c.isInterface());
			Date d = (Date)c.newInstance();
			th.check("test".getClass().getName(), "java.lang.String");
			c = this.getClass();
			InputStream s = c.getResourceAsStream("test.png");
			th.check(s.available(), 291);
			th.check(s.read(), 137);
	    } catch (Exception e) {
			e.printStackTrace();
			th.check(false);
	    }
	}
}
