package tests.imagecapture;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.IOException;

public class ImageCapture extends MIDlet {
    private Player player;
    private VideoControl videoControl;

    class TestCanvas extends Canvas {
        private Image capturedImage;

        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (capturedImage != null) {
                g.drawImage(capturedImage, 0, 0, Graphics.TOP | Graphics.LEFT);
            } else {
                videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
                try {
                    videoControl.setDisplayLocation(0, 0);
                    videoControl.setDisplaySize(getWidth(), getHeight());
                } catch (MediaException me) {
                    System.out.println("Unexpected exception: " + me);
                    me.printStackTrace();
                }

                videoControl.setVisible(true);
            }
        }

        protected void pointerReleased(int x, int y) {
            try {
                byte[] imageData;

                imageData = videoControl.getSnapshot("encoding=jpeg");

                capturedImage = Image.createImage(imageData, 0, imageData.length);

                player.close();
                player = null;
                videoControl = null;

                this.repaint();
            } catch (Exception e) {
                System.out.println("Unexpected exception: " + e);
                e.printStackTrace();
            }
        }
    }

    public void startApp() {
        try {
            player = Manager.createPlayer("capture://image");
            player.realize();
            player.prefetch();

            videoControl = (VideoControl)player.getControl("VideoControl");

            TestCanvas test = new TestCanvas();
            test.setFullScreenMode(true);
            Display.getDisplay(this).setCurrent(test);

            player.start();
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
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
