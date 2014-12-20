package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestLiteralStringPool implements Testlet {
    public static String s = "abc";

    public void test(TestHarness th) {
        th.check("abc" == s);
    }
}
