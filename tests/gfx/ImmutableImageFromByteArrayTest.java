package gfx;

import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;
import gnu.testlet.TestUtils;

public class ImmutableImageFromByteArrayTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            screenG.setColor(0x00FFFFFF);
            screenG.fillRect(0, 0, getWidth(), getHeight());

            try {
                InputStream is = getClass().getResourceAsStream("/gfx/images/FirefoxLogo.png");
                byte[] imageData = TestUtils.read(is);

                Image image = Image.createImage(imageData, 0, imageData.length);
                screenG.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);

                System.out.println("PAINTED");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("FAIL");
            }
        }
    }

    public ImmutableImageFromByteArrayTest() {
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
