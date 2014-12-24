package gnu.testlet.vm;

import gnu.testlet.*;

public class LongTest implements Testlet {
    static long h = 0x0123456789abcdefL;
    static long a = 5;
    static long b = 7;
    static long c = 50;
    static long d = 5;

    public void test(TestHarness th) {
        th.check(("" + h), "81985529216486895");
    	th.check(a + b, 12L);
    	th.check(a - b, -2L);
    	th.check(a * b, 35L);
    	th.check(c / a, 10L);
    	th.check(c % b, 1L);

        th.check(a < b);
        th.check(b > a);
        th.check(a == d);
    }
}
