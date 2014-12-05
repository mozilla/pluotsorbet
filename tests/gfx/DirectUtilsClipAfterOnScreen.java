package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class DirectUtilsClipAfterOnScreen extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            try {
                screenG.setColor(255, 0, 0);
                screenG.fillRect(0, 0, getWidth(), getHeight());
                screenG.setColor(0, 0, 255);
                screenG.fillRect(20, 30, 200, 80);

                DirectGraphics dGet = DirectUtils.getDirectGraphics(screenG);

                short[] pixels = new short[getWidth() * getHeight()];
                dGet.getPixels(pixels, 0, getWidth(), 0, 0, getWidth(), getHeight(), DirectGraphics.TYPE_USHORT_4444_ARGB);
                
                screenG.setColor(0, 255, 0);
                screenG.fillRect(0, 0, getWidth(), getHeight());

                DirectGraphics dDraw = DirectUtils.getDirectGraphics(screenG);

                screenG.setClip(20, 30, 200, 80);

                dDraw.drawPixels(pixels, true, 0, getWidth(), 0, 0, getWidth(), getHeight(), 0, DirectGraphics.TYPE_USHORT_4444_ARGB);
            } catch (Exception e) {
                System.out.println("TODO");
            }

            System.out.println("PAINTED");
        }
    }

    public DirectUtilsClipAfterOnScreen() {
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

