package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class DrawAndFillArcTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(255, 0, 0);
            g.drawArc(getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), 90, 300);
            g.setColor(0, 0, 255);
            g.fillArc(getWidth() / 2, 200, 200, 80, 10, 170);
            System.out.println("PAINTED");
        }
    }

    public DrawAndFillArcTest() {
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

