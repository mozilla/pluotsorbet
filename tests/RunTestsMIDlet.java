/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

import gnu.testlet.*;

import com.sun.cldchi.jvm.JVM;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.*;
import java.lang.Exception;
import java.util.Vector;

public class RunTestsMIDlet extends MIDlet {
    int classPass = 0, classFail = 0, pass = 0, fail = 0, knownFail = 0, unknownPass = 0;

    void runTest(String name) {
        name = name.replace('/', '.');

        System.out.println("Running " + name);

        Form form = new Form(name);
        Display display = Display.getDisplay(this);
        j2mejsTestHarness harness = new j2mejsTestHarness(name, display);
        harness.setScreenAndWait(form);

        Class c = null;
        try {
            c = Class.forName(name);
        } catch (Exception e) {
            harness.fail("Can't load test: " + e);
        }
        Object obj = null;
        try {
            obj = c.newInstance();
        } catch (Exception e) {
            harness.fail("Can't instantiate test: " + e);
        }
        Testlet t = (Testlet) obj;
        try {
            t.test(harness);
        } catch (Exception e) {
            harness.fail("Test threw an unexpected exception: " + e);
        }
        harness.report();
        boolean classPassed = true;
        if (harness.passed() != t.getExpectedPass()) {
            classPassed = false;
            System.err.println(name + ": test expected " + t.getExpectedPass() + " passes, got " + harness.passed());
        }
        if (harness.failed() != t.getExpectedFail()) {
            classPassed = false;
            System.err.println(name + ": test expected " + t.getExpectedFail() + " failures, got " + harness.failed());
        }
        if (harness.knownFailed() != t.getExpectedKnownFail()) {
            classPassed = false;
            System.err.println(name + ": test expected " + t.getExpectedKnownFail() + " known failures, got " + harness.knownFailed());
        }
        if (classPassed) {
            classPass++;
        } else {
            classFail++;
            System.err.println(name + ": class fail");
        }
        pass += harness.passed();
        fail += harness.failed();
        knownFail += harness.knownFailed();
        unknownPass += harness.unknownPassed();
    }

    public void startApp() {
        String arg = getAppProperty("arg-0").replace('.', '/');

        // Join Testlets and MIDletTestlets because we want to run them both
        // in RunTestsMIDlet.
        String[] testlets = new String[Testlets.list.length + MIDletTestlets.list.length];
        for (int i = 0; i < Testlets.list.length; i++) {
            testlets[i] = Testlets.list[i];
        }
        for (int i = 0; i < MIDletTestlets.list.length; i++) {
            testlets[i + Testlets.list.length] = MIDletTestlets.list[i];
        }

        long then = JVM.monotonicTimeMillis();

        if (arg != null && arg.length() > 0) {
            Vector v = new Vector();
            for (int n = 0; n < testlets.length; ++n) {
                String name = testlets[n];
                if (name != null) {
                    v.addElement(testlets[n]);
                }
            }

            int i = 0;
            while (arg != null && arg.length() > 0) {
                if (v.contains(arg)) {
                    runTest(arg);
                } else {
                    System.err.println("can't find test " + arg);
                }

                arg = getAppProperty("arg-" + ++i).replace('.', '/');
            }
        } else {
            for (int n = 0; n < testlets.length; ++n) {
                String name = testlets[n];
                if (name != null) {
                    runTest(name);
                }
            }
        }
        System.out.println("TOTALS: " + pass + " pass, " + fail + " fail, " + knownFail + " known fail, " +
                           unknownPass + " unknown pass");
        System.out.println("DONE: " + classPass + " class pass, " + classFail + " class fail, "  + (JVM.monotonicTimeMillis() - then) + "ms");
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
