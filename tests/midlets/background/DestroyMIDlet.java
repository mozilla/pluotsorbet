package tests.background;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.*;
import com.sun.midp.main.MIDletSuiteUtils;

public class DestroyMIDlet extends MIDlet {
    int started = 0;

    static native void sendDestroyMIDletEvent();
    static native void sendExecuteMIDletEvent();
    static native void maybePrintDone();

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());
            System.out.println("PAINTED");
        }

        protected void pointerReleased(int x, int y) {
            sendDestroyMIDletEvent();
        }
    }

    public void startApp() {
        TestCanvas test = new TestCanvas();
        test.setFullScreenMode(true);
        Display.getDisplay(this).setCurrent(test);

        System.out.println("startApp" + (++started));

        maybePrintDone();
    }

    public void pauseApp() {
        System.out.println("pauseApp");
    }

    public void destroyApp(boolean unconditional) {
        System.out.println("destroyApp");
        sendExecuteMIDletEvent();
    }
};
