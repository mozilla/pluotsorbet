package gnu.testlet.vm;

import gnu.testlet.*;
import com.sun.cldc.isolate.*;
import gnu.testlet.vm.IsolatedClass;

public class TestIsolate implements Testlet {
    public int getExpectedPass() { return 10; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
        th.check(IsolatedClass.val, 1);

        IsolatedClass c = new IsolatedClass();
        c.main(new String[] { "1" });
        th.check(c.val, 2);
        th.check(IsolatedClass.val, 2);

        Isolate[] isolates = Isolate.getIsolates();
        th.check(isolates.length, 1);
        th.check(isolates[0].id(), Isolate.currentIsolate().id());

        try {
            Isolate iso = new Isolate("gnu.testlet.vm.IsolatedClass", new String[] { "5" });
            iso.start();

            isolates = Isolate.getIsolates();
            th.check(isolates.length, 2);
            th.check(isolates[0].id(), Isolate.currentIsolate().id());
            th.check(isolates[1].id(), iso.id());

            iso.waitForExit();
            th.check(IsolatedClass.val, 2);

            th.check(iso.isTerminated());
        } catch(Exception e) {
            th.fail("Unexpected exception: " + e.getMessage());
        }
    }
}
