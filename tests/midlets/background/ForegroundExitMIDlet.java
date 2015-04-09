package tests.background;

import javax.microedition.midlet.MIDlet;
import com.sun.cldc.isolate.Isolate;

public class ForegroundExitMIDlet extends MIDlet {
    public void startApp() {
        // TODO: Find a better way to do this, with a public J2ME function
        // and not an internal one.
        Isolate.currentIsolate().exit(0);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
