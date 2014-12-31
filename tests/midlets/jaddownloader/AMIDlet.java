package tests.jaddownloader;

import javax.microedition.midlet.*;

public class AMIDlet extends midlets.TestMidlet {
    public void startApp() {
        try {
            compare(getAppProperty("MIDlet-Name"), "AMIDlet");
            compare(getAppProperty("MIDlet-Vendor"), "MIDlet Suite Vendor");
            compare(getAppProperty("MIDlet-Version"), "1.0.0");
        } catch (NullPointerException ex) {
            fail(ex);
        }

        finish();
        System.out.println("DONE");
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
