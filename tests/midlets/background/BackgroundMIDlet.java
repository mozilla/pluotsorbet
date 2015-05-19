package tests.background;

import java.io.IOException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.io.Connector;
import com.nokia.mid.s40.bg.BGUtils;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;

public class BackgroundMIDlet extends MIDlet {
    public BackgroundMIDlet() {
    }

    public void startApp() {
        try {
            LocalMessageProtocolConnection client = (LocalMessageProtocolConnection)Connector.open("localmsg://mozilla");
        } catch (IOException e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
            return;
        }

        BGUtils.setBGMIDletResident(true);

        System.out.println("Hello World from background MIDlet");
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
