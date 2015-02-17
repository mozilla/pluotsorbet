package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import gnu.testlet.TestUtils;

public class DrawStringWithCopyrightAndRegisteredSymbols extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(0);
            g.setStrokeStyle(Graphics.SOLID);
            String str = TestUtils.getEmojiString("a9") + TestUtils.getEmojiString("ae");
            g.drawString(str, 0, 0, Graphics.TOP | Graphics.LEFT);
            System.out.println("PAINTED");
        }
    }

    public DrawStringWithCopyrightAndRegisteredSymbols() {
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

