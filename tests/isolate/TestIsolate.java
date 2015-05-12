package tests.isolate;

import com.sun.cldc.isolate.*;
import tests.isolate.IsolatedClass;

import java.lang.String;

public class TestIsolate {
    static int dumpNumber = 0;
    public static void dump(String s) {
        System.out.println((dumpNumber++) + ": " + s);
    }
    public static void dump(int s) {
        dump(s + "");
    }

    public static void main(String args[]) {
        dump(IsolatedClass.val);

        new IsolatedClass().main(new String[] { "a" } );

        dump(IsolatedClass.val);

        Isolate myIso = Isolate.currentIsolate();
        dump(myIso.id());

        Isolate[] isolates = Isolate.getIsolates();

        if (isolates.length == 1) {
            dump("1 isolate");
        }

        if (isolates[0].id() == myIso.id()) {
            dump("Isolate ID correct");
        }

        try {
            Isolate iso1 = new Isolate("tests.isolate.IsolatedClass", new String[] { "1" });
            Isolate iso2 = new Isolate("tests.isolate.IsolatedClass", new String[] { "2" });

            dump(iso1.id());
            dump(iso2.id());

            if (Isolate.getIsolates().length == 1) {
                dump("1 isolate");
            }

            iso1.start();

            dump(IsolatedClass.val);

            iso2.start();

            dump(IsolatedClass.val);

            if (Isolate.getIsolates().length == 3) {
                dump("3 isolates");
            }

            iso1.waitForExit();
            iso2.waitForExit();

            dump(IsolatedClass.val);

            if (Isolate.getIsolates().length == 1) {
                dump("1 isolate");
            }

            if (iso1.isTerminated() && iso2.isTerminated()) {
                dump("Isolates terminated");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        new IsolatedClass().main(new String[] { "r" });

        dump(IsolatedClass.val);

        new IsolatedClass().main(new String[] { "c" });

        dump(IsolatedClass.val);

        if (!myIso.isTerminated()) {
            dump("Main isolate still running");
        }

        System.out.println("DONE");
    }
}
