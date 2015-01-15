package tests.jaddownloader;

import javax.microedition.midlet.MIDlet;
import javax.microedition.io.ConnectionNotFoundException;

public class AMIDletUpdater extends MIDlet {
    public void startApp() {
        try {
            platformRequest("http://localhost:8000/tests/Manifest1Updated.jad");
        } catch (ConnectionNotFoundException e) {
            e.printStackTrace();
            System.out.println("FAIL");
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
