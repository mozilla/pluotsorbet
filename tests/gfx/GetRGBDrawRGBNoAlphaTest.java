package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class GetRGBDrawRGBNoAlphaTest extends MIDlet {
    private Command quitCommand;
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            Image image = DirectUtils.createImage(getWidth(), getHeight(), 0);
            Graphics g = image.getGraphics();

            g.setColor(255, 0, 0);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(0, 0, 255);
            g.fillRect(20, 30, 200, 80);

            int[] pixels = new int[getWidth() * getHeight()];
            image.getRGB(pixels, 0, getWidth(), 0, 0, getWidth(), getHeight());

            g.setColor(0, 0, 0);
            g.fillRect(0, 0, getWidth(), getHeight());

            screenG.drawRGB(pixels, 0, getWidth(), 0, 0, getWidth(), getHeight(), false);

            Image image2 = DirectUtils.createImage(160, 40, 0x0000FF00);

            int[] pixels2 = new int[160 * 40];
            image2.getRGB(pixels2, 0, 160, 0, 0, 160, 40);

            if (pixels2[0] != 0x0000FF00) {
                System.out.println("TODO - Pixel is " + pixels2[0]);
            }

            screenG.drawRGB(pixels2, 0, 160, 40, 50, 160, 40, false);

            System.out.println("PAINTED");
        }
    }

    public GetRGBDrawRGBNoAlphaTest() {
        display = Display.getDisplay(this);
    }

    public void startApp() {
        TestCanvas test = new TestCanvas();
        display.setCurrent(test);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}

