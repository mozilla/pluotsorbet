package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class ClippingWithAnchorTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            Image image = Image.createImage(70, 70);
            Graphics g = image.getGraphics();
            g.setColor(255, 0, 0);
            g.fillRect(10, 10, 60, 60);

            screenG.setColor(255, 255, 255);
            screenG.fillRect(0, 0, getWidth(), getHeight());
            screenG.setClip(35, 35, 40, 40);
            screenG.drawImage(image, 30, 30, Graphics.VCENTER | Graphics.HCENTER);

            System.out.println("PAINTED");
        }
    }

    public ClippingWithAnchorTest() {
        display = Display.getDisplay(this);
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

