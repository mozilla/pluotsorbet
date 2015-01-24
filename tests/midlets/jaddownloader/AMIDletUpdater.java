package tests.jaddownloader;

import javax.microedition.midlet.MIDlet;
import javax.microedition.io.ConnectionNotFoundException;

import com.sun.cldc.isolate.Isolate;

public class AMIDletUpdater extends MIDlet {
    public void startApp() {
        try {
            platformRequest("http://localhost:8000/tests/Manifest1Updated.jad");
        } catch (ConnectionNotFoundException e) {
            e.printStackTrace();
            System.out.println("FAIL");
        }
        Isolate.currentIsolate().exit(0);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
