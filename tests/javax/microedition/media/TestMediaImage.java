package javax.microedition.media;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import gnu.testlet.TestUtils;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

public class TestMediaImage implements Testlet, PlayerListener {
    TestHarness th;
    boolean started = false;

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
            os.write(TestUtils.read(is));
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

            synchronized (this) {
                while (!started) {
                    this.wait();
                }
            }
            th.check(started);

            file.delete();
            file.close();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        System.out.println(event);
        if (event.equals("started")) {
            started = true;
            synchronized (this) {
                this.notify();
            }
        }
    }
}

