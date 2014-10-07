package java.lang;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestSystem implements Testlet {
    public void test(TestHarness th) {
        // Test a simple property with a constant value.
        th.check(System.getProperty("microedition.encoding"), "UTF-8");

        // Test com.nokia.mid.mnc and com.nokia.mid.networkID, whose values
        // can vary and are retrieved from a privileged API.  Their values are
        // also cached, so we retrieve them twice to ensure two calls return
        // the same value.
        String mnc = System.getProperty("com.nokia.mid.mnc");
        th.check(mnc.length() == 6);
        th.check(System.getProperty("com.nokia.mid.mnc"), mnc);
        String networkID = System.getProperty("com.nokia.mid.networkID");
        th.check(networkID.length() == 6);
        th.check(System.getProperty("com.nokia.mid.networkID"), networkID);
    }
}
