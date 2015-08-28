package tests.isolate;

import com.sun.cldc.isolate.*;
import gnu.testlet.DumpTestHarness;

public class IsolatedClass {
    static public String val = "a";

    public static void main(String args[]) {
        DumpTestHarness th = new DumpTestHarness();
        val += args[0];
        if (args.length > 1) {
          if (args[0].equals("1")) {
            th.check(val, "a1", "First isolate static value is correct.");
          } else if (args[0].equals("2")) {
            th.check(val, "a2", "Second isolate static value is correct.");
          } else {
            th.fail("Bad arg value: " + args[0]);
          }
        }
    }
}
