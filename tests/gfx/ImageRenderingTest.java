package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class ImageRenderingTest extends MIDlet {
    private Display display;
    private Image image;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (image != null) {
                g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
            }
            System.out.println("PAINTED");
        }
    }

    public ImageRenderingTest() {
        display = Display.getDisplay(this);
        try {
            image = Image.createImage("/gfx/images/FirefoxLogo.png");
        } catch (java.io.IOException e) {
            System.out.println("FAIL - " + e);
        }
    }

    public void startApp() {
        TestCanvas test = new TestCanvas();
        test.setFullScreenMode(true);
        display.setCurrent(test);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}

