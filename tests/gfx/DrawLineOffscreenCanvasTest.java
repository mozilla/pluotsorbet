package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class DrawLineOffscreenCanvasTest extends MIDlet {
    private Display display;

    class TestCanvas extends Canvas {
        protected void paint(Graphics screenG) {
            Image image = Image.createImage(getWidth(), getHeight());
            Graphics g = image.getGraphics();

            g.setColor(255, 0, 0);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(0, 0, 255);
            g.drawLine(20, 80, 20, getHeight() - 80);
            g.drawLine(10, 10, getWidth() - 10, getHeight() - 10);
            g.drawLine(200, 80, 200, getHeight() - 80);

            screenG.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);

            System.out.println("PAINTED");
        }
    }

    public DrawLineOffscreenCanvasTest() {
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

