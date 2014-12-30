/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

package midlets;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class TestAlertWithGauge extends MIDlet {
    public void startApp() {
        Display display = Display.getDisplay(this);

        Alert alert = new Alert("Prova", "Some text some text some text", null, AlertType.INFO);
        alert.setTimeout(Alert.FOREVER);

        Gauge gauge = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
        alert.setIndicator(gauge);

        display.setCurrent(alert);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
