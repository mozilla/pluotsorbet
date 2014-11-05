package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class ImageRenderingTest extends MIDlet implements CommandListener {
    private Command quitCommand;
    private Display display;
    private Image image;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            if (image != null) {
                g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
            }
            System.out.println("PAINTED");
        }
    }

    public ImageRenderingTest() {
        display = Display.getDisplay(this);
        try {
            image = Image.createImage("/gfx/images/FirefoxLogo.png");
        } catch (java.io.IOException e) {
            System.out.println("FAIL - " + e);
        }

        quitCommand = new Command("Quit", Command.EXIT, 2);
    }

    public void startApp() {
        TestCanvas test = new TestCanvas();
        test.addCommand(quitCommand);
        test.setCommandListener(this);
        display.setCurrent(test);
    }

    public void pauseApp() {
        System.out.println("App paused");
    }

    public void destroyApp(boolean unconditional) {
        System.out.println("Goodbye, world");
    }

    public void commandAction(Command c, Displayable s) {
        if (c == quitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }
}

