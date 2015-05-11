/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

import gnu.testlet.*;

import com.sun.cldchi.jvm.JVM;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.*;
import java.lang.Exception;
import java.util.Vector;

public class RunShellTests {
    private static class Harness extends TestHarness {
        private String testName;
        private int testNumber = 0;
        private String testNote = null;
        private int pass = 0;
        private int fail = 0;
        private int knownFail = 0;
        private int unknownPass = 0;

        public Harness(String note, Display d) {
            super(d);
            this.testName = note;
        }

        public void setNote(String note) {
            testNote = note;
        }

        public void debug(String msg) {
            System.out.println(testName + "-" + testNumber + ": " + msg + ((testNote != null) ? (" [" + testNote + "]") : ""));
        }

        public void check(boolean ok) {
            if (ok) {
                ++pass;
            }
            else {
                ++fail;
                debug("fail");
            }
            ++testNumber;
            setNote(null);
        }

        public void todo(boolean ok) {
            if (ok) {
                ++unknownPass;
                debug("unknown pass");
            }
            else
                ++knownFail;
            ++testNumber;
            setNote(null);
        }

        public void report() {
            System.out.println(testName + ": " + pass + " pass, " + fail + " fail, " + knownFail + " known fail, " +
                               unknownPass + " unknown pass");
        }

        public int passed() {
            return pass;
        }

        public int failed() {
            return fail;
        }

        public int knownFailed() {
            return knownFail;
        }

        public int unknownPassed() {
            return unknownPass;
        }
    };

    int classPass = 0, classFail = 0, pass = 0, fail = 0, knownFail = 0, unknownPass = 0;

    void runTest(String name) {
        name = name.replace('/', '.');

        System.out.println("Running " + name);

        Harness harness = new Harness(name, null);

        Class c = null;
        try {
            c = Class.forName(name);
        } catch (Exception e) {
            System.err.println(e);
            harness.fail("Can't load test");
        }
        Object obj = null;
        try {
            obj = c.newInstance();
        } catch (Exception e) {
            System.err.println(e);
            harness.fail("Can't instantiate test");
        }
        Testlet t = (Testlet) obj;
        try {
            t.test(harness);
        } catch (Exception e) {
            System.err.println(e);
            harness.fail("Test threw an unexpected exception");
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

    public static void main(String [] args) {
        new RunShellTests().run();
    }

    public void run() {
        long then = JVM.monotonicTimeMillis();
        String arg = null;

        if (arg != null && arg.length() > 0) {
            Vector v = new Vector();
            for (int n = 0; n < ShellTestlets.list.length; ++n) {
                v.addElement(ShellTestlets.list[n]);
            }

            int i = 0;
            while (arg != null && arg.length() > 0) {
                if (v.contains(arg)) {
                    runTest(arg);
                } else {
                    System.err.println("can't find test " + arg);
                }
            }
        } else {
            for (int n = 0; n < ShellTestlets.list.length; ++n) {
                String name = ShellTestlets.list[n];
                if (name == null)
                    break;
                runTest(name);
            }
        }
        System.out.println("TOTALS: " + pass + " pass, " + fail + " fail, " + knownFail + " known fail, " +
                           unknownPass + " unknown pass");
        System.out.println("DONE: " + classPass + " class pass, " + classFail + " class fail, "  + (JVM.monotonicTimeMillis() - then) + "ms");
    }
};
