package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import gnu.testlet.TestUtils;

public class VideoPlayerTest extends MIDlet implements PlayerListener {
    private VideoControl videoControl;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());

            videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
            try {
                videoControl.setDisplayLocation(20, 20);
                videoControl.setDisplaySize(77, 42);
            } catch (MediaException me) {
                System.out.println("FAIL");
            }

            videoControl.setVisible(true);

            System.out.println("PAINTED");
        }
    }

    public void startApp() {
        try {
            String dirPath = System.getProperty("fileconn.dir.private");
            FileConnection file = (FileConnection)Connector.open(dirPath + "test.webm", Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/javax/microedition/media/test.webm");
            os.write(TestUtils.read(is));
            os.close();

            Player player = Manager.createPlayer(dirPath + "test.webm");

            player.addPlayerListener(this);

            player.realize();

            videoControl = (VideoControl)player.getControl("VideoControl");

            TestCanvas test = new TestCanvas();
            test.setFullScreenMode(true);
            Display.getDisplay(this).setCurrent(test);

            player.start();

            file.delete();
            file.close();
        } catch (Exception e) {
            System.out.println("FAIL - Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public void playerUpdate(Player player, String event, Object eventData) {
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}

