package tests.isolate;

import com.sun.cldc.isolate.*;
import tests.isolate.IsolatedClass;

public class TestIsolate {
    public static void main(String args[]) {
        System.out.println(IsolatedClass.val);

        new IsolatedClass().main(new String[] { "a" } );

        System.out.println(IsolatedClass.val);

        Isolate myIso = Isolate.currentIsolate();
        System.out.println(myIso.id());

        if (Isolate.getIsolates().length == 1) {
            System.out.println("1 isolate");
        }

        try {
            Isolate iso1 = new Isolate("tests.isolate.IsolatedClass", new String[] { "1" });
            Isolate iso2 = new Isolate("tests.isolate.IsolatedClass", new String[] { "2" });

            System.out.println(iso1.id());
            System.out.println(iso2.id());

            if (Isolate.getIsolates().length == 1) {
                System.out.println("1 isolate");
            }

            iso1.start();

            System.out.println(IsolatedClass.val);

            iso2.start();

            System.out.println(IsolatedClass.val);

            if (Isolate.getIsolates().length == 3) {
                System.out.println("3 isolates");
            }

            iso1.waitForExit();
            iso2.waitForExit();

            System.out.println(IsolatedClass.val);

            if (Isolate.getIsolates().length == 1) {
                System.out.println("1 isolate");
            }

            if (iso1.isTerminated() && iso2.isTerminated()) {
                System.out.println("Isolates terminated");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        new IsolatedClass().main(new String[] { "r" });

        System.out.println(IsolatedClass.val);

        new IsolatedClass().main(new String[] { "c" });

        System.out.println(IsolatedClass.val);

        if (!myIso.isTerminated()) {
            System.out.println("Main isolate still running");
        }

        System.out.println("DONE");
    }
}
