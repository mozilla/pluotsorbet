package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.midlet.*;
import com.nokia.mid.ui.*;

public class CreateImageWithRegionTest extends MIDlet {
    private Command quitCommand;
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            Image image;
            try {
                image = Image.createImage("/gfx/images/FirefoxLogo.png");
            } catch (java.io.IOException e) {
                System.out.println("FAIL - " + e);
                return;
            }

            Image image2 = Image.createImage(image, 30, 30, 120, 120, Sprite.TRANS_NONE);

            screenG.drawImage(image2, 10, 10, Graphics.TOP | Graphics.LEFT);

            System.out.println("PAINTED");
        }
    }

    public CreateImageWithRegionTest() {
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

