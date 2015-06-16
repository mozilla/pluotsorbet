/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

import gnu.testlet.*;

import com.sun.cldchi.jvm.JVM;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.*;
import java.lang.Exception;
import java.util.Vector;

public class j2mejsTestHarness extends TestHarness {
    private String testName;
    private int testNumber = 0;
    private String testNote = null;
    private int pass = 0;
    private int fail = 0;
    private int knownFail = 0;
    private int unknownPass = 0;

    public j2mejsTestHarness(String note, Display d) {
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
}
