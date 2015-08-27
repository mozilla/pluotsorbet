package tests.isolate;

import com.sun.cldc.isolate.*;
import tests.isolate.IsolatedClass;
import gnu.testlet.DumpTestHarness;

import java.lang.String;

public class TestIsolate {
    public static void main(String args[]) {
        DumpTestHarness th = new DumpTestHarness();
        th.check(IsolatedClass.val, "a", "Initial IsolatedClass static value is correct.");

        new IsolatedClass().main(new String[] { "b" } );

        th.check(IsolatedClass.val, "ab",  "IsolatedClass static val append works.");

        Isolate myIso = Isolate.currentIsolate();
        int myIsoId = myIso.id();
        th.check(myIsoId > -1, "Valid isolate ID.");

        Isolate[] isolates = Isolate.getIsolates();

        th.check(isolates.length == 1, "Only one isolate exists.");

        th.check(isolates[0].id() == myIso.id(), "Isolate ID is correct.");

        try {
            Isolate iso1 = new Isolate("tests.isolate.IsolatedClass", new String[] { "1", "new isolate" });
            Isolate iso2 = new Isolate("tests.isolate.IsolatedClass", new String[] { "2", "new isolate" });

            int iso1Id = iso1.id();
            th.check(iso1Id > myIsoId, "First isolate id is larger than main isolate id.");
            th.check(iso2.id() > iso1Id, "Second isolate id is larger than first isolate id.");

            th.check(Isolate.getIsolates().length, 1, "1 isolate started.");

            iso1.start();
            th.check(IsolatedClass.val, "ab", "IsolatedClass static value not modified by iso1 starting.");

            iso2.start();
            th.check(IsolatedClass.val, "ab", "IsolatedClass static value not modified by iso2 starting.");

            th.check(Isolate.getIsolates().length, 3, "3 isolates created.");

            iso1.waitForExit();
            iso2.waitForExit();
            th.check(IsolatedClass.val, "ab", "IsolatedClass static value not modified by new isolates after exit.");

            th.check(Isolate.getIsolates().length, 1, "1 isolate left.");

            th.check(iso1.isTerminated(), "iso1 is terminated.");
            th.check(iso2.isTerminated(), "iso2 is terminated.");
        } catch(Exception e) {
            e.printStackTrace();
            th.fail("Exception: " + e);
        }

        new IsolatedClass().main(new String[] { "c" });

        th.check(IsolatedClass.val, "abc", "IsolatedClass static val append still works.");

        th.check(!myIso.isTerminated(), "Main isolate still running");

        System.out.println("DONE");
    }
}
