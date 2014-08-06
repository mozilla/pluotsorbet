package gnu.testlet.vm;

import gnu.testlet.*;

public class FieldNotFoundException implements Testlet {
    void throw1(TestHarness th) {
        boolean caught = false;
        try {
          boolean missingField = org.mozilla.test.ClassWithMissingField.missingField;
        } catch (Exception e) {
            // Despite the test's name, the VM raises a generic RuntimeException
            // because CLDC doesn't provide a FieldNotFoundException class.
            th.check(e instanceof RuntimeException);
            th.check(e.getMessage(), "org/mozilla/test/ClassWithMissingField.missingField.Z not found");
            caught = true;
        }
        th.check(caught);
    }

    public void test(TestHarness th) {
        throw1(th);
    }
}
