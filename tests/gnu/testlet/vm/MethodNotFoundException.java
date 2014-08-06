package gnu.testlet.vm;

import gnu.testlet.*;

public class MethodNotFoundException implements Testlet {
    void throw1(TestHarness th) {
        boolean caught = false;
        try {
          org.mozilla.test.ClassWithMissingMethod.missingMethod();
        } catch (Exception e) {
            // Despite the test's name, the VM raises a generic RuntimeException
            // because CLDC doesn't provide a MethodNotFoundException class.
            th.check(e instanceof RuntimeException);
            th.check(e.getMessage(), "org/mozilla/test/ClassWithMissingMethod.missingMethod.()V not found");
            caught = true;
        }
        th.check(caught);
    }

    public void test(TestHarness th) {
        throw1(th);
    }
}
