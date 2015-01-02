package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class RectAfterText extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(0);
            g.setStrokeStyle(Graphics.SOLID);
            g.drawString("Top left", 0, 0, Graphics.TOP | Graphics.LEFT);

            int y = Font.getDefaultFont().getBaselinePosition();

            g.setColor(255, 0, 0);
            g.fillRect(0, y, 10, 10);

            g.setColor(0);
            g.fillRect(getWidth()/2 - 10, getHeight()/2 - 10, 20, 20);
            g.drawString("Center", getWidth()/2, getHeight()/2 - 10, Graphics.HCENTER | Graphics.BASELINE);

            System.out.println("PAINTED");
        }
    }

    public RectAfterText() {
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

