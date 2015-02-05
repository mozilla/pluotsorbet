package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestArrayPrameter implements Testlet {
    public int getExpectedPass() { return 10; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    private boolean testBooleanArray(boolean[] array) {
        return true;
    }

    private boolean testByteArray(byte[] array) {
        return true;
    }

    private boolean testCharArray(char[] array) {
        return true;
    }

    private boolean testDoubleArray(double[] array) {
        return true;
    }

    private boolean testFloatArray(float[] array) {
        return true;
    }

    private boolean testIntArray(int[] array) {
        return true;
    }

    private boolean testLongArray(long[] array) {
        return true;
    }

    private boolean testObjectArray(Object[] array) {
        return true;
    }

    private boolean testShortArray(short[] array) {
        return true;
    }

    private boolean testMultiDArray(Object[][] array) {
        return true;
    }

    public void test(TestHarness th) {
       th.check(testBooleanArray(null));
       th.check(testByteArray(null));
       th.check(testCharArray(null));
       th.check(testDoubleArray(null));
       th.check(testFloatArray(null));
       th.check(testIntArray(null));
       th.check(testLongArray(null));
       th.check(testObjectArray(null));
       th.check(testShortArray(null));
       th.check(testMultiDArray(null));
    }
}

