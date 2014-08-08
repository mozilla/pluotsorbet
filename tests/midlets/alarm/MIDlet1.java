package tests.alarm;

import javax.microedition.midlet.*;
import javax.microedition.io.PushRegistry;
import java.util.Date;

public class MIDlet1 extends MIDlet {
    public MIDlet1() {
    }

    public void startApp() {
        System.out.println("Hello World from MIDlet1");
        try {
            PushRegistry.registerAlarm("tests.alarm.MIDlet2", new Date().getTime() + 1000);
        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
