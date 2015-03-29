package tests.background;

import java.io.IOException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.io.Connector;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;

public class ForegroundMIDlet extends MIDlet {
    public ForegroundMIDlet() {
    }

    public void startApp() {
        try {
            LocalMessageProtocolConnection client = (LocalMessageProtocolConnection)Connector.open("localmsg://mozilla");
        } catch (IOException e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
            return;
        }

        System.out.println("Hello World from foreground MIDlet");

        if (System.getProperty("prop1") != null && System.getProperty("prop2") != null) {
            System.out.println("prop1=" + System.getProperty("prop1") + " prop2=" + System.getProperty("prop2"));
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
