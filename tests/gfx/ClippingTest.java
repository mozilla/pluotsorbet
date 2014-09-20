package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class ClippingTest extends MIDlet {
    private Command quitCommand;
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            Image image = Image.createImage(70, 70);
            Graphics g = image.getGraphics();
            g.setColor(255, 0, 0);
            g.fillRect(0, 0, 70, 70);

            screenG.setColor(255, 255, 255);
            screenG.fillRect(0, 0, getWidth(), getHeight());
            screenG.setClip(35, 35, 30, 30);
            screenG.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);

            System.out.println("PAINTED");
        }
    }

    public ClippingTest() {
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

