/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

import gnu.testlet.*;

import com.sun.cldchi.jvm.JVM;
import java.lang.Exception;
import java.util.Vector;

public class RunTests {
    int classPass = 0, classFail = 0, pass = 0, fail = 0, knownFail = 0, unknownPass = 0;

    void runTest(String name) {
        name = name.replace('/', '.');

        System.out.println("Running " + name);

        j2mejsTestHarness harness = new j2mejsTestHarness(name, null);

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

    public void go(String[] args) {
        long then = JVM.monotonicTimeMillis();

        if (args.length > 0 && args[0] != null) {
            Vector v = new Vector();
            for (int n = 0; n < Testlets.list.length; ++n) {
                v.addElement(Testlets.list[n]);
            }

            for (int i = 0; i < args.length; i++) {
                String arg = args[i].replace('.', '/');

                if (v.contains(arg)) {
                    runTest(arg);
                } else {
                    System.err.println("can't find test " + arg);
                }
            }
        } else {
            for (int n = 0; n < Testlets.list.length; ++n) {
                String name = Testlets.list[n];
                if (name == null)
                    break;
                runTest(name);
                Thread.yield();
            }
        }
        System.out.println("TOTALS: " + pass + " pass, " + fail + " fail, " + knownFail + " known fail, " +
                           unknownPass + " unknown pass");
        System.out.println("DONE: " + classPass + " class pass, " + classFail + " class fail, "  + (JVM.monotonicTimeMillis() - then) + "ms");
    }

    public static void main(String[] args) {
        new RunTests().go(args);
    }
};
