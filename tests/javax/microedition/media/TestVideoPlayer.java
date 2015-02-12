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

public class TestVideoPlayer implements Testlet, PlayerListener {
    public int getExpectedPass() { return 3; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    boolean started = false;

    public void test(TestHarness th) {
        try {
            Form form = new Form("Test");

            String dirPath = System.getProperty("fileconn.dir.private");
            FileConnection file = (FileConnection)Connector.open(dirPath + "test.mp4", Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/javax/microedition/media/test.mp4");
            os.write(TestUtils.read(is));
            os.close();

            Player player = Manager.createPlayer(dirPath + "test.mp4");

            player.addPlayerListener(this);

            player.realize();

            VideoControl videoControl = (VideoControl)player.getControl("VideoControl");

            th.check(videoControl.getSourceHeight(), 42);
            th.check(videoControl.getSourceWidth(), 77);

            Item videoItem = (Item)videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
            form.append(videoItem);

            player.start();

            synchronized (this) {
                while (!started) {
                    this.wait();
                }
            }
            th.check(started);

            player.close();

            file.delete();
            file.close();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        if (event.equals("started")) {
            started = true;
            synchronized (this) {
                this.notify();
            }
        }
    }
}

