package tests.background;

import java.io.IOException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.io.Connector;
import com.nokia.mid.s40.io.LocalMessageProtocolServerConnection;
import com.nokia.mid.s40.bg.BGUtils;

// Test executing the Foreground MIDlet before starting the localmsg server.

public class BackgroundMIDlet1 extends MIDlet {
    public BackgroundMIDlet1() {
    }

    public void startApp() {
        System.out.println("Hello World from background MIDlet");

        BGUtils.setBGMIDletResident(true);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        try {
            LocalMessageProtocolServerConnection server = (LocalMessageProtocolServerConnection)Connector.open("localmsg://:mozilla");
        } catch (IOException e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
