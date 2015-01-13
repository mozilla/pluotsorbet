package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class DirectGraphicsDrawPixelsWithXY extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
        	int size = 16 * 16;
            short[] pixels = new short[size];
            for (int i = 0; i < size; i++) {
            	pixels[i] = (short)0xF00F;
            }

            screenG.setColor(255, 0, 0);
            screenG.fillRect(0, 0, getWidth(), getHeight());

            screenG.setClip(12, 61, 18, 10);

            DirectUtils.getDirectGraphics(screenG).drawPixels(pixels, true, 0, 16, 12, 50, 16, 16, 0, DirectGraphics.TYPE_USHORT_4444_ARGB);

            System.out.println("PAINTED");
        }
    }

    public DirectGraphicsDrawPixelsWithXY() {
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

