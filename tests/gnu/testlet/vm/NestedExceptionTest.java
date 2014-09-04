package gnu.testlet.vm;

import gnu.testlet.*;

public class NestedExceptionTest implements Testlet {
    private static boolean firstFinallyCalled = false;
    private static boolean finallyReturnCalled = false;
    private static boolean finallyReturnReturned = false;
    private static boolean finallyThrowCalled = false;
    private static boolean finallyThrowCaught = false;

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

    int finallyReturn(TestHarness th) throws Throwable {
        try {
            return 42;
        } catch (Throwable e) {
            throw e;
        } finally {
            finallyReturnCalled = true;
            th.check(!finallyReturnReturned, "finallyReturn hasn't yet returned a value");
        }
    }

    void finallyThrow(TestHarness th) throws Throwable {
        try {
            throw new Throwable();
        } finally {
            finallyThrowCalled = true;
            th.check(!finallyThrowCaught, "finallyThrow hasn't yet thrown an exception");
        }
    }

    int returnInFinally() {
        try {
        } finally {
            return 42;
        }
    }

    int returnInFinallyAfterThrow() throws Exception {
        try {
            throw new Exception("try");
        } finally {
            return 42;
        }
    }

    void throwInFinally() throws Exception {
        try {
        } finally {
            throw new Exception("finally");
        }
    }

    void throwInBoth() throws Exception {
        try {
            throw new Exception("try");
        } finally {
            throw new Exception("finally");
        }
    }

    void tryAndCatchBothThrow() throws Exception {
        try {
            throw new Exception("try");
        } catch (Exception e) {
            throw new Exception("catch");
        }
    }

    int tryAndCatchBothThrowWithFinally() throws Exception {
        try {
            throw new Exception("try");
        } catch (Exception e) {
            throw new Exception("catch");
        } finally {
            return 42;
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
            th.check(finallyReturn(th), 42);
            finallyReturnReturned = true;
            th.check(finallyReturnCalled);
        } catch (Throwable e) {
            th.fail("Unexpected exception: " + e);
        }

        try {
            finallyThrow(th);
            th.fail("Exception expected");
        } catch (Throwable e) {
            finallyThrowCaught = true;
            th.check(finallyThrowCalled, "Exception expected");
        }

        th.check(returnInFinally(), 42);

        try {
            th.check(returnInFinallyAfterThrow(), 42);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
        }

        try {
            throwInFinally();
            th.fail("Expected exception");
        } catch (Exception e) {
            th.check(e.getMessage(), "finally");
        }

        try {
            throwInBoth();
            th.fail("Expected exception");
        } catch (Exception e) {
            th.check(e.getMessage(), "finally");
        }

        try {
            tryAndCatchBothThrow();
            th.fail("Expected exception");
        } catch (Exception e) {
            th.check(e.getMessage(), "catch");
        }

        try {
            th.check(tryAndCatchBothThrowWithFinally(), 42);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
        }
    }
}
