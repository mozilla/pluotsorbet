package gnu.testlet.vm;

import gnu.testlet.*;

public class ExceptionTest implements Testlet {
    void throw1(TestHarness th) {
	boolean caught = false;
	try {
	    throw new RuntimeException("Foo");
	} catch (Exception e) {
	    th.check(e.getMessage(), "Foo");
	    caught = true;
	}
	th.check(caught);
	int i = 8;
	try {
	    i /= 0;
	} catch (Exception e) {
	    i++;
	}
	th.check(i == 9);
    }

    public void test(TestHarness th) {
	throw1(th);
    }
}