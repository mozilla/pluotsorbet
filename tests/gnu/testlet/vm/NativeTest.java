package gnu.testlet.vm;

import gnu.testlet.*;

public class NativeTest implements Testlet {
    native static int getInt();

    public void test(TestHarness th) {
        th.check(getInt(), 0xFFFFFFFF);
    }
}
