package gnu.testlet.vm;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.Vector;

public class TestInterfaceAbstractConcrete implements Testlet {
    public int getExpectedPass() { return 5; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    static TestHarness th;

    interface Int {
        public void a();
    }
     
    abstract class Abs implements Int {
        public void method() {
            th.check(true, "Abs::method() called");
            a();
        }
    }
     
    class Con extends Abs {
        public void a() {
            th.check(true, "Con::a() called");
        }
     
        public void method() {
            th.check(true, "Con::method() called");
            super.method();
        }
    }

    public void test(TestHarness th) {
        this.th = th;

        // invokevirtual
        Con con = new Con();
        con.method();

        Vector vec = new Vector();
        vec.addElement(con);

        // checkcast
        Int inter = (Int)vec.elementAt(0);
        // invokeinterface
        inter.a();

        // checkcast
        Abs abs = (Abs)vec.elementAt(0);
        // invokevirtual
        abs.a();
    }
}

