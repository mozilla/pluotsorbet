package tests.background;

import javax.microedition.midlet.MIDlet;

public class ForegroundMIDlet extends MIDlet {
    public ForegroundMIDlet() {
    }

    public void startApp() {
        System.out.println("Hello World from foreground MIDlet");

        if (System.getProperty("prop1") != null) {
            System.out.println(System.getProperty("prop1"));
        }

        if (System.getProperty("prop2") != null) {
            System.out.println(System.getProperty("prop2"));
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
