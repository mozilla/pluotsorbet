package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

class S {
    public static String s = "abc";
}

public class TestStringIntern implements Testlet {
    public int getExpectedPass() { return 15; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
        try {
            String m1 = "marco";
            String m2 = "marco";
            String m3 = "marcos".substring(0, 5);

            // Sanity checking
            th.check(m1 == m2);
            th.check(m1 != m3);
            th.check(m1.equals(m2));
            th.check(m1.equals(m3));

            th.check(m1.intern() == m1, "m1.intern() == m1");
            th.check(m1.intern() == m2.intern(), "m1.intern() == m2.intern()");
            th.check(m1.intern() == m3.intern(), "m1.intern() == m3.intern()");

            th.check(m2.intern() == m1, "m2.intern() == m1");
            th.check(m2.intern() == m2, "m2.intern() == m2");
            th.check(m2.intern() != m3, "m2.intern() != m3");

            th.check(m3.intern() == m1, "m3.intern() == m1");
            th.check(m3.intern() == m2, "m3.intern() == m2");
            th.check(m3.intern() != m3, "m3.intern() == m3");

            // Check that constant strings are interned
            th.check("abc" == "abc");
            th.check("abc" == S.s);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
