package tests.midlets;

import java.io.IOException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.content.Registry;

public class ContentHandlerStarterMIDlet extends MIDlet {
    static native void sendShareMessage();
    static native void startMIDlet();

    public void startApp() {
        try {
            System.out.println("Hello World from starter MIDlet");

            Registry.getRegistry(getClass().getName())
                    .register("tests.midlets.ContentHandlerMIDlet",
                              new String[] { "image/jpeg", "image/png", "image/gif", "audio/amr", "audio/mp3", "video/3gpp", "video/mp4" },
                              null,
                              new String[] { "share" },
                              null,
                              null,
                              null);

            // Test that sharing works if the FG MIDlet isn't running yet.
            sendShareMessage();

            startMIDlet();
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
