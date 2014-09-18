package gnu.testlet.vm;

import gnu.testlet.*;

public class NativeTest implements Testlet {
    native static int getInt();
    native static int fromJavaString(String string);

    public void test(TestHarness th) {
        th.todo(getInt(), 0xFFFFFFFF); // got (4294967295), expected (-1)

        String s = "marco";
        th.check(s.substring(0, 0), "");
        th.check(fromJavaString(s.substring(0, 0)), fromJavaString(""));
        th.check(fromJavaString(s.substring(0, 1)), fromJavaString("m"));
    }
}
