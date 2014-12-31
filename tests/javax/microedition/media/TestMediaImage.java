package javax.microedition.media;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

public class TestMediaImage implements Testlet, PlayerListener {
    TestHarness th;

    byte[] read(InputStream is) throws IOException {
        int l = is.available();
        byte[] buffer = new byte[l+1];
        int length = 0;

        while ((l = is.read(buffer, length, buffer.length - length)) != -1) {
            length += l;
            if (length == buffer.length) {
                byte[] b = new byte[buffer.length + 4096];
                System.arraycopy(buffer, 0, b, 0, length);
                buffer = b;
            }
        }

        return buffer;
    }

    public void test(TestHarness th) {
        this.th = th;

        try {
            Form form = new Form("Test");

            FileConnection file = (FileConnection)Connector.open("file:////test.jpg", Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/org/mozilla/io/test.jpg");
            os.write(read(is));
            os.close();

            Player player = Manager.createPlayer("file:////test.jpg");

            player.addPlayerListener(this);

            player.realize();

            VideoControl videoControl = (VideoControl)player.getControl("VideoControl");

            th.check(videoControl.getSourceHeight(), 195);
            th.check(videoControl.getSourceWidth(), 195);

            Item videoItem = (Item)videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
            form.append(videoItem);

            player.start();

            // We have to remove the listener now that we're done testing.
            // Otherwise, it could receive additional calls to playerUpdate,
            // including after we've returned, and the harness has tallied
            // our total number of passed tests!
            player.removePlayerListener(this);

            file.delete();
            file.close();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        th.check(true);
        player.close();
    }
}

