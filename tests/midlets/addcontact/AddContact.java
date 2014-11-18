package tests.addcontact;

import javax.microedition.midlet.*;
import javax.microedition.io.*;

public class AddContact extends MIDlet {
    public void startApp() {
        try {
            platformRequest("x-contacts:add?number=+393333333333");
        } catch (ConnectionNotFoundException e) {
            System.out.println("Error while adding contact");
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
