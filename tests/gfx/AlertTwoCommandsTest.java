package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class AlertTwoCommandsTest extends MIDlet {
    public void startApp() {
        Alert alert = new Alert("Hello World", "Some text", null, AlertType.INFO);
        alert.setTimeout(Alert.FOREVER);
        Display.getDisplay(this).setCurrent(alert);

        Command yes2 = new Command("Yes 2", Command.OK, 3);

        alert.addCommand(new Command("Yes", Command.OK, 1));
        alert.addCommand(yes2);

        try {
            do {
                Thread.sleep(100);
            } while (!alert.isShown());
        } catch (InterruptedException e) {
            System.out.println("FAIL");
        }

        // Remove a command and add a new one that is supposed to
        // have a different style.

        alert.removeCommand(yes2);
        alert.addCommand(new Command("No", Command.BACK, 2));

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
}
