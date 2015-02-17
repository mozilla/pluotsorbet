package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class CanvasWithHeaderTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x0000FFFF);
            g.fillRect(0, 0, getWidth(), getHeight());
            System.out.println("PAINTED");
        }
    }

    public CanvasWithHeaderTest() {
        display = Display.getDisplay(this);
    }

    public void startApp() {
        TestCanvas test = new TestCanvas();
        test.addCommand(new Command("Quit", Command.EXIT, 1));
        test.addCommand(new Command("OK", Command.OK, 1));
        test.addCommand(new Command("Back", Command.BACK, 1));
        test.setTitle("Canvas Test");
        display.setCurrent(test);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}

