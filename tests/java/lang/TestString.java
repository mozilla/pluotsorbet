package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.lang.String.*;

public class TestString implements Testlet {
    public int getExpectedPass() { return 5; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
        // Check a few places where StringTest.java is incomplete.

        th.check("abfdefg".lastIndexOf('f'), 5);
        th.check("ggggggg".lastIndexOf('g'), 6);
        th.check("abfdefg".lastIndexOf('f', 4), 2);
        th.check("foobar".endsWith("bar"));
        th.check(!"foobar".endsWith("baz"));
    }
}
