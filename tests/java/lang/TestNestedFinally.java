package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestNestedFinally implements Testlet {
    public int getExpectedPass() { return 2; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    private boolean foo1Reached = false;
    private int foo2Value = 1;

    private void foo1() throws Exception {
        synchronized(this) {
            try {
                throw new Exception();
            } finally {
                foo1Reached = true;
                System.out.println("foo1 inner finally reached!");
            }
        }
    }

    private void foo2() throws Exception {
        // We should reach the inner finally statement first, then the outer
        // finally and set foo2Value to 20.
        // If we miss either finally statement, or reach in reverse order,
        // foo2Value will be set to other value.
        try {
            try {
                throw new Exception();
            } finally {
                ++foo2Value;
                System.out.println("foo2 inner finally reached!");
            }
        } finally {
            foo2Value *= 10;
            System.out.println("foo2 outer finally reached!");
        }
    }

    public void test(TestHarness th) {
        try {
            foo1();
            th.fail();
        } catch (Exception e) {
            th.check(foo1Reached);
        }

        try {
            foo2();
            th.fail();
        } catch (Exception e) {
            th.check(foo2Value, 20);
        }
    }
}

