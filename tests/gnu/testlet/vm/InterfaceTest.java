package gnu.testlet.vm;

import java.io.*;
import java.util.Date;
import gnu.testlet.*;

public class InterfaceTest implements Testlet {
    private interface A {
	public void x();

	public static final String[] foo = {"A", "B"};
    }

    private interface B extends A {
    }

    private class X implements B {
	public void x() {
	    th.check(foo[0], "A");
	}
    }

    static TestHarness th;

    public void test(TestHarness th) {
	InterfaceTest.th = th;

	X x = new X();
	B b = x;
	b.x();
    }
}
