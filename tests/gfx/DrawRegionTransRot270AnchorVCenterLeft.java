package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.game.Sprite;

public class DrawRegionTransRot270AnchorVCenterLeft extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            Image image;
            try {
              image = Image.createImage("/gfx/images/colorRects.png");
            } catch (java.io.IOException e) {
              System.out.println("FAIL - " + e);
              return;
            }

            screenG.setColor(255, 255, 255);
            screenG.fillRect(0, 0, getWidth(), getHeight());

            screenG.drawRegion(image, 10, 15, 70, 95, Sprite.TRANS_ROT270, getWidth() / 2, getHeight() / 2, Graphics.VCENTER | Graphics.LEFT);
            System.out.println("PAINTED");
        }
    }

    public DrawRegionTransRot270AnchorVCenterLeft() {
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

