package tests.isolate;

import com.sun.cldc.isolate.*;
import tests.isolate.IsolatedClass;

public class TestIsolate {
    public static void main(String args[]) {
        System.out.println(IsolatedClass.val);

        new IsolatedClass().main(new String[] { "a" } );

        System.out.println(IsolatedClass.val);

        try {
            Isolate iso1 = new Isolate("tests.isolate.IsolatedClass", new String[] { "1" });
            iso1.start();

            System.out.println(IsolatedClass.val);

            Isolate iso2 = new Isolate("tests.isolate.IsolatedClass", new String[] { "2" });
            iso2.start();

            System.out.println(IsolatedClass.val);

            iso1.waitForExit();
            iso2.waitForExit();

            System.out.println(IsolatedClass.val);
        } catch(Exception e) {
            e.printStackTrace();
        }

        new IsolatedClass().main(new String[] { "r" });

        System.out.println(IsolatedClass.val);

        new IsolatedClass().main(new String[] { "c" });

        System.out.println(IsolatedClass.val);

        System.out.println("DONE");
    }
}
