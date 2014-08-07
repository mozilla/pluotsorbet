package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class CanvasTest extends MIDlet implements CommandListener {
    private Command quitCommand;
    private Display display;

    class TestCanvas extends Canvas {
	protected void paint(Graphics g) {
	    g.setColor(0x00FFFFFF);;
	    g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
	    System.out.println("PAINTED");
	}
    }

    public CanvasTest() {
        display = Display.getDisplay(this);
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

