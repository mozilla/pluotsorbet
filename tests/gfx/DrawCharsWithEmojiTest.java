package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import gnu.testlet.TestUtils;

public class DrawCharsWithEmojiTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            String emoji1 = TestUtils.getEmojiString("1f1ee1f1f9");
            String emoji2 = TestUtils.getEmojiString("1f609");
            String emoji3 = TestUtils.getEmojiString("2320e3");

            String text = "A stri" + emoji1 + "ng wit" + emoji2 + "h emoj" + emoji3 + "i";

            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(0);
            g.setStrokeStyle(Graphics.SOLID);
            g.drawChars(text.toCharArray(), 0, text.toCharArray().length, 50, 50, Graphics.TOP | Graphics.LEFT);
            System.out.println("PAINTED");
        }
    }

    public DrawCharsWithEmojiTest() {
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

