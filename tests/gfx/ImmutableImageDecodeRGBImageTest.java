package gfx;

import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;
import gnu.testlet.TestUtils;

public class ImmutableImageDecodeRGBImageTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            screenG.setColor(0xFFFF0000);
            screenG.fillRect(0, 0, getWidth(), getHeight());

            int[] rgb = new int[200 * 80];
            for (int i = 0; i < rgb.length; i++) {
                rgb[i] = 0xFF0000FF;
            }

            Image image = Image.createRGBImage(rgb, 200, 80, true);
            screenG.drawImage(image, 20, 30, Graphics.TOP | Graphics.LEFT);

            System.out.println("PAINTED");
        }
    }

    public ImmutableImageDecodeRGBImageTest() {
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
