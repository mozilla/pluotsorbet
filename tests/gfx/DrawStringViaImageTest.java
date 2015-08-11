package gfx;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import com.nokia.mid.ui.*;

public class DrawStringViaImageTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics graphics) {
            // Color the background so we can see the image on top of it.
            graphics.setColor(0x00FF0000);
            graphics.fillRect(0, 0, getWidth(), getHeight());

            Image image = Image.createImage(150, 150);

            Graphics g = image.getGraphics();
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, 150, 150);
            g.setColor(0);
            g.setStrokeStyle(Graphics.SOLID);
            g.drawString("Roxors", 50, 50, Graphics.TOP | Graphics.LEFT);

            int[] rgbData = new int[150 * 150];
            image.getRGB(rgbData, 0, 150, 0, 0, 150, 150);
            g.drawRGB(rgbData, 0, 150, 0, 0, 150, 150, true);

            graphics.drawImage(image, 50, 50, Graphics.TOP | Graphics.LEFT);

            System.out.println("PAINTED");
        }
    }

    public DrawStringViaImageTest() {
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
