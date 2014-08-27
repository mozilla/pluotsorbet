package gnu.testlet.vm;

import java.util.Vector;
import gnu.testlet.*;

public class InterfaceTest implements Testlet {
    private interface A {
        public void x();

        public static final String[] foo = {"A", "B"};
    }

    private interface B extends A {
    }

    private interface C extends B {
    }

    private class X implements C {
        public void x() {
            th.check(foo[0], "A");
        }
    }

    static TestHarness th;

    public void test(TestHarness th) {
        InterfaceTest.th = th;

        X x = new X();
        C c = x;
        c.x();

        B b = x;
        b.x();

        A a = x;
        a.x();

        Vector vec = new Vector();
        vec.addElement(x);

        C c2 = (C)vec.elementAt(0);
        c2.x();

        B b2 = (B)vec.elementAt(0);
        b2.x();

        A a2 = (A)vec.elementAt(0);
        a2.x();
    }
}
