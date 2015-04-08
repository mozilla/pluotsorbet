package tests.background;

import javax.microedition.midlet.MIDlet;
import com.sun.cldc.isolate.Isolate;

public class ForegroundExitMIDlet extends MIDlet {
    public void startApp() {
        Isolate.currentIsolate().exit(0);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
