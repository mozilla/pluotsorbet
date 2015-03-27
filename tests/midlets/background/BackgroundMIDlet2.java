package tests.background;

import java.io.IOException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.io.Connector;
import com.nokia.mid.s40.io.LocalMessageProtocolServerConnection;
import com.nokia.mid.s40.bg.BGUtils;

public class BackgroundMIDlet2 extends MIDlet {
    public BackgroundMIDlet2() {
    }

    public void startApp() {
        System.out.println("Hello World from background MIDlet");

        try {
            LocalMessageProtocolServerConnection server = (LocalMessageProtocolServerConnection)Connector.open("localmsg://:mozilla");
        } catch (IOException e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        BGUtils.setBGMIDletResident(true);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
