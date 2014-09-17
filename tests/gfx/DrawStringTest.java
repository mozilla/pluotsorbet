package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class DrawStringTest extends MIDlet {
    private Command quitCommand;
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);;
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(0);
            g.setStrokeStyle(Graphics.SOLID);
            g.drawString("Top left", 0, 0, Graphics.TOP | Graphics.LEFT);
            System.out.println("PAINTED");
        }
    }

    public DrawStringTest() {
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

