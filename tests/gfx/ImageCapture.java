package gfx;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.IOException;

public class ImageCapture extends MIDlet {
    private Player player;
    private VideoControl videoControl;
    private Image capturedImage;
    private TestCanvas testCanvas;

    class CaptureThread extends Thread {
        public void run() {
            try {
                byte[] imageData;

                imageData = videoControl.getSnapshot("encoding=jpeg");

                capturedImage = Image.createImage(imageData, 0, imageData.length);

                player.close();
                player = null;
                videoControl = null;

                testCanvas.repaint();
            } catch (Exception e) {
                System.out.println("Unexpected exception: " + e);
                e.printStackTrace();
                System.out.println("FAIL");
            }
        }
    }

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (capturedImage != null) {
                g.drawImage(capturedImage, 20, 20, Graphics.TOP | Graphics.LEFT);

                System.out.println("PAINTED");
            } else {
                videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
                try {
                    videoControl.setDisplayLocation(20, 20);
                    videoControl.setDisplaySize(77, 42);
                } catch (MediaException me) {
                    System.out.println("Unexpected exception: " + me);
                    me.printStackTrace();
                    System.out.println("FAIL");
                }

                videoControl.setVisible(true);

                new CaptureThread().start();
            }
        }
    }

    public void startApp() {
        try {
            player = Manager.createPlayer("capture://image");
            player.realize();
            player.prefetch();

            videoControl = (VideoControl)player.getControl("VideoControl");

            testCanvas = new TestCanvas();
            testCanvas.setFullScreenMode(true);
            Display.getDisplay(this).setCurrent(testCanvas);

            player.start();
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
            System.out.println("FAIL");
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        if (player != null) {
            player.deallocate();
            player.close();
        }
    }
}
