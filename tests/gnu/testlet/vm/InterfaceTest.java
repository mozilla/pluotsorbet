package gnu.testlet.vm;

import java.io.*;
import java.util.Date;
import gnu.testlet.*;

public class InterfaceTest implements Testlet {
    private interface A {
	public void x();
    }

    private interface B extends A {
    }

    private class X implements B {
	public void x() {
	    System.out.println("OK!");
	}
    }

    public void test(TestHarness th) {
	X x = new X();
	B b = x;
	b.x();
    }
}
