package gnu.testlet.vm;

import gnu.testlet.*;

public class NativeTest implements Testlet {
    native static int getInt();
    native static int fromJavaString(String string);
    native static int decodeUtf8(byte[] string);
    native static long getLongReturnLong(long val);
    native static int getLongReturnInt(long val);
    native static long getIntReturnLong(int val);
    native static void throwException();
    native static void throwExceptionAfterPause();
    native static int returnAfterPause();
    native int nonStatic(int val);
    native static boolean newFunction();
    native static boolean dumbPipe();

    public void test(TestHarness th) {
        th.check(getInt(), 0xFFFFFFFF);

        String s = "marco";
        th.check(s.substring(0, 0), "");
        th.check(fromJavaString(s.substring(0, 0)), fromJavaString(""));
        th.check(fromJavaString(s.substring(0, 1)), fromJavaString("m"));

        th.check(fromJavaString("\0"), 1);
        th.check(decodeUtf8("\0".getBytes()), 1);
        th.check(fromJavaString(""), 0);
        th.check(decodeUtf8("".getBytes()), 0);

        th.check(getLongReturnLong(2L), 42L);

        th.check(getLongReturnInt(2L), 42);

        th.check(getIntReturnLong(2), 42L);

        try {
          throwException();
          th.fail("Exception expected");
        } catch (NullPointerException e) {
          th.check(e.getMessage(), "An exception");
        }

        try {
          throwExceptionAfterPause();
          th.fail("Exception expected");
        } catch (NullPointerException e) {
          th.check(e.getMessage(), "An exception");
        }

        th.check(returnAfterPause(), 42);

        th.check(nonStatic(2), 42);

        th.check(newFunction());
        th.check(dumbPipe());
    }
}
