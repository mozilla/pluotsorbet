package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestExceptionLocation implements Testlet {

    private void foo1() {
        int a[] = new int[1];
        // This will trigger an array index exception, that should not be
        // handled by the following try/catch block.
        a[10] = 1;
        try {
            int i = 0;
        } catch (Exception e) {
            // This should not happen
        }
    }

    public void test(TestHarness th) {
        try {
            foo1();
            th.fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            th.check(true);
        }
    }
}

