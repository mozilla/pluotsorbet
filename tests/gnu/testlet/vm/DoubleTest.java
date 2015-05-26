package gnu.testlet.vm;

import gnu.testlet.*;

public class DoubleTest implements Testlet {
    public int getExpectedPass() { return 9; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    static double h = 123.456;
    static double a = 5;
    static double b = 7;
    static double c = 50;
    static double d = 5;

    public void test(TestHarness th) {
      th.check(("" + h), "123.456");
      th.check(a + b, 12D);
      th.check(a - b, -2D);
      th.check(a * b, 35D);
      th.check(c / a, 10D);
      th.check(c % b, 1D);

      th.check(a < b);
      th.check(b > a);
      th.check(a == d);
    }
}
