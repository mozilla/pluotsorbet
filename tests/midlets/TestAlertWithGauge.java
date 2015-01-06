/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

package midlets;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class TestAlertWithGauge extends MIDlet implements CommandListener {
    Command cmdYes, cmdNo;

    public void startApp() {
        Display display = Display.getDisplay(this);

        cmdYes = new Command("Yes", Command.OK, 1);
        cmdNo = new Command("No", Command.CANCEL, 1);

        Alert alert = new Alert("Prova", "Some text some text some text", null, AlertType.INFO);
        alert.setTimeout(Alert.FOREVER);
        alert.addCommand(cmdYes);
        alert.addCommand(cmdNo);
        alert.setCommandListener(this);

        Gauge gauge = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
        alert.setIndicator(gauge);

        display.setCurrent(alert);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == cmdYes) {
            System.out.println("You pressed 'Yes'");
        } else if (command == cmdNo) {
            System.out.println("You pressed 'No'");
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
