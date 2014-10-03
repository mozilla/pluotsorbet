package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class AlertTest extends MIDlet implements CommandListener {
    private Display display;
    private Alert alert;

    public AlertTest() {
        display = Display.getDisplay(this);
    }

    public void startApp() {
        alert = new Alert("Hello World", "Some text", null, AlertType.INFO);
        alert.setCommandListener(this);
        display.setCurrent(alert);

        try {
            do {
                Thread.sleep(100);
            } while (!alert.isShown());
        } catch (InterruptedException e) {
            System.out.println("FAIL");
        }

        System.out.println("PAINTED");
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command choice, Displayable displayable) {
    }
}
