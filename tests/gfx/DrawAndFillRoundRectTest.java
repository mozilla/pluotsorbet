package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class DrawAndFillRoundRectTest extends MIDlet {
    private Command quitCommand;
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(255, 0, 0);
            g.drawRoundRect(0, 0, getWidth(), getHeight(), getWidth() / 2, getHeight() / 2);
            g.setColor(0, 0, 255);
            g.fillRoundRect(20, 30, 200, 80, 100, 80);
            System.out.println("PAINTED");
        }
    }

    public DrawAndFillRoundRectTest() {
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

