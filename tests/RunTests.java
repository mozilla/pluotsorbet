import gnu.testlet.*;

import javax.microedition.midlet.*;

public class RunTests extends MIDlet {
    private static class Harness extends TestHarness {
        private String testName;
        private int testNumber = 0;
        private String testNote = null;
        private int pass = 0;
        private int fail = 0;
        private int knownFail = 0;
        private int unknownPass = 0;

        public Harness(String note) {
            this.testName = note;
            System.out.println(testName);
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
            if (ok)
                ++unknownPass;
            else
                ++knownFail;
            ++testNumber;
            setNote(null);
        }

        public void report() {
            System.out.println(testName + ": " + pass + " pass, " + fail + " fail");
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

    public void startApp() {
        int pass = 0, fail = 0, knownFail = 0, unknownPass = 0;
        for (int n = 0; n < Testlets.list.length; ++n) {
            String name = Testlets.list[n];
            if (name == null)
                break;
            name = name.replace('/', '.');
            Harness harness = new Harness(name);
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
            t.test(harness);
            if (harness.failed() > 0)
                harness.report();
            pass += harness.passed();
            fail += harness.failed();
            knownFail += harness.knownFailed();
            unknownPass += harness.unknownPassed();
        }
        System.out.println("DONE: " + pass + " pass, " + fail + " fail, " + knownFail + " known fail, " + unknownPass + " unknown pass");
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
