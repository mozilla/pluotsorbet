package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class CanvasTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());
            System.out.println("PAINTED");
        }
    }

    public CanvasTest() {
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

