package midlets;

import java.io.IOException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.content.Registry;
import com.nokia.mid.s40.bg.BGUtils;

public class BackgroundContentHandlerRegisterMIDlet extends MIDlet {
    static native void sendShareMessage();

    public void startApp() {
        try {
            System.out.println("Hello World from background MIDlet");

            Registry.getRegistry(getClass().getName())
                    .register("midlets.ContentHandlerMIDlet",
                              new String[] { "image/jpeg", "image/png", "image/gif", "audio/amr", "audio/mp3", "video/3gpp", "video/mp4" },
                              null,
                              new String[] { "share" },
                              null,
                              null,
                              null);

            // Test that sharing works if the FG MIDlet isn't running yet.
            sendShareMessage();

            BGUtils.setBGMIDletResident(true);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
