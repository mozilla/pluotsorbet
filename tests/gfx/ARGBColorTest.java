package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class ARGBColorTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(255, 0, 0);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(0, 0, 255);
            g.fillRect(20, 30, 200, 80);

            DirectUtils.getDirectGraphics(g).setARGBColor(0x00FFFFFF);
            if (DirectUtils.getDirectGraphics(g).getAlphaComponent() != 0) {
                System.out.println("FAIL");
            }
            g.fillRect(0, 0, getWidth(), getHeight());

            //  The alpha value is set to fully opaque when setColor is called
            g.setColor(0, 0, 0);
            if (DirectUtils.getDirectGraphics(g).getAlphaComponent() != 0xFF) {
                System.out.println("FAIL");
            }

            DirectUtils.getDirectGraphics(g).setARGBColor(0xFF00FF00);
            if (DirectUtils.getDirectGraphics(g).getAlphaComponent() != 0xFF) {
                System.out.println("FAIL");
            }
            g.fillRect(40, 50, 160, 40);

            // setARGBColor updates rgb color and gray color
            int grayBefore = g.getGrayScale();
            DirectUtils.getDirectGraphics(g).setARGBColor(0x00007700);
            if (g.getColor() != 0x00007700) {
                System.out.println("FAIL");
            }
            // Gray scale is computed, so only ensure it changed.
            if (g.getGrayScale() == grayBefore) {
                System.out.println("FAIL");
            }

            System.out.println("PAINTED");
        }
    }

    public ARGBColorTest() {
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

