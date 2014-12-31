package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class DirectUtilsClipBeforeOnScreen2 extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
        	int size = getWidth() * getHeight();
            short[] pixels = new short[size];
            for (int i = 0; i < size; i++) {
            	pixels[i] = -4081;
            }

            screenG.setColor(255, 0, 0);
            screenG.fillRect(0, 0, getWidth(), getHeight());

            screenG.setClip(20, 30, 200, 80);

            DirectGraphics dDraw = DirectUtils.getDirectGraphics(screenG);

            dDraw.drawPixels(pixels, true, 0, getWidth(), 0, 0, getWidth(), getHeight(), 0, DirectGraphics.TYPE_USHORT_4444_ARGB);

            System.out.println("PAINTED");
        }
    }

    public DirectUtilsClipBeforeOnScreen2() {
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

