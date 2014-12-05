package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

public class MediaImageTest extends MIDlet implements PlayerListener {
    private Display display;
    private Image image;
    private Player player;
    private VideoControl videoControl;
    private Form form;
    private Item videoItem;

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

    public MediaImageTest() throws IOException {
        
    }

    public void startApp() {
        try {
            display = Display.getDisplay(this);
            form = new Form("Test");

            FileConnection file = (FileConnection)Connector.open("file:///test.jpg", Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/org/mozilla/io/test.jpg");
            os.write(read(is));
            os.close();

            player = Manager.createPlayer("file:///test.jpg");

            player.addPlayerListener(this);

            player.realize();

            videoControl = (VideoControl)player.getControl("VideoControl");

            videoItem = (Item)videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
            form.append(videoItem);

            player.start();

            display.setCurrent(form);

            file.delete();
            file.close();
        } catch (Exception e) {
            System.out.println("FAIL - Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        System.out.println("playerUpdate: " + event);
    }

    public void pauseApp() {
        System.out.println("App paused");
    }

    public void destroyApp(boolean unconditional) {
        System.out.println("Goodbye, world");
    }
}

