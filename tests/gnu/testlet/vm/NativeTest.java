package gnu.testlet.vm;

import gnu.testlet.*;

public class NativeTest implements Testlet {
    native static int getInt();

    public void test(TestHarness th) {
        th.todo(getInt(), 0xFFFFFFFF); // got (4294967295), expected (-1)
    }
}
