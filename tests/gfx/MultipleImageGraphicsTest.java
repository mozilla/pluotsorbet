package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class MultipleImageGraphicsTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
          Image img = Image.createImage(getWidth(), getHeight());
          Graphics g1 = img.getGraphics();
          Graphics g2 = img.getGraphics();

          g1.setColor(255, 0, 0);
          g2.setColor(0, 255, 0);

          g2.fillRect(0, 0, getWidth(), getHeight());
          g1.fillRect(0, 0, 25, 25);

          g2.setClip(25, 0, 25, 25);
          g1.fillRect(25, 25, 25, 25);

          screenG.drawImage(img, 0, 0, Graphics.TOP|Graphics.LEFT);

          System.out.println("PAINTED");
        }
    }

    public MultipleImageGraphicsTest() {
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

