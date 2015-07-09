package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class DrawStringViaImageTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics graphics) {
            Image image = DirectUtils.createImage(150, 150, 0);
            Graphics g = image.getGraphics();

            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(0);
            g.setStrokeStyle(Graphics.SOLID);
            g.drawString("Roxors", 50, 50, Graphics.TOP | Graphics.LEFT);

            graphics.setColor(0x00FF0000);
            graphics.fillRect(0, 0, getWidth(), getHeight());
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

