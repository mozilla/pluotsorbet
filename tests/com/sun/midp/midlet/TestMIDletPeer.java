/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

package com.sun.midp.midlet;

import javax.microedition.midlet.MIDlet;
import javax.microedition.io.ConnectionNotFoundException;

public class TestMIDletPeer extends MIDlet {
    public void startApp() {
        try {
            platformRequest("http://localhost:8000/tests/test.html");
        } catch (ConnectionNotFoundException e) {
            e.printStackTrace();
            System.out.println("FAIL");
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
