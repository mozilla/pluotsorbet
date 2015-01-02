package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class DirectUtilsCreateImageTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            Image image = DirectUtils.createImage(getWidth(), getHeight(), 0);
            Graphics g = image.getGraphics();

            g.setColor(255, 0, 0);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(0, 0, 255);
            g.fillRect(20, 30, 200, 80);

            screenG.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);

            System.out.println("PAINTED");
        }
    }

    public DirectUtilsCreateImageTest() {
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

