package tests.background;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.*;
import com.sun.midp.main.MIDletSuiteUtils;

public class PauseMIDlet extends MIDlet {
    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public void startApp() {
        TestCanvas test = new TestCanvas();
        test.setFullScreenMode(true);
        Display.getDisplay(this).setCurrent(test);

        System.out.println("startApp");
    }

    public void pauseApp() {
        System.out.println("pauseApp");
    }

    public void destroyApp(boolean unconditional) {
        System.out.println("destroyApp");
    }
};
