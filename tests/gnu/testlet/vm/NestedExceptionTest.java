package gnu.testlet.vm;

import gnu.testlet.*;

public class NestedExceptionTest implements Testlet {
    private static boolean firstFinallyCalled = false;
    private static boolean finallyReturnCalled = false;
    private static boolean finallyThrowCalled = false;

    void throw1(TestHarness th) {
        try {
            throw new Throwable();
        } catch (Throwable e) {
            th.check(true, "Exception caught");
        } finally {
            firstFinallyCalled = true;
            th.check(true, "Finally called");
        }
    }

    int finallyReturn() throws Throwable {
        try {
            return 42;
        } catch (Throwable e) {
            throw e;
        } finally {
            finallyReturnCalled = true;
        }
    }

    void finallyThrow() throws Throwable {
        try {
            throw new Throwable();
        } finally {
            finallyThrowCalled = true;
        }
    }

    public void test(TestHarness th) {
        try {
            throw1(th);
        } catch (Throwable e) {
            th.fail("Unexpected exception");
        } finally {
            th.check(firstFinallyCalled, "Finally called");
        }

        try {
            th.check(finallyReturn(), 42);
            th.check(finallyReturnCalled);
        } catch (Throwable e) {
            th.fail("Unexpected exception: " + e);
        }

        try {
            finallyThrow();
            th.fail("Exception expected");
        } catch (Throwable e) {
            th.check(true, "Exception expected");
        }
    }
}
