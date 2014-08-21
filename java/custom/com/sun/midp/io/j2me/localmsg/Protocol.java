package com.sun.midp.io.j2me.localmsg;

import org.mozilla.io.LocalMsgConnection;
import com.sun.cldc.io.ConnectionBaseInterface;
import javax.microedition.io.Connection;
import java.io.IOException;

public class Protocol implements ConnectionBaseInterface {
    public Connection openPrim(String name, int mode, boolean timeouts) throws IOException {
        return new LocalMsgConnection(name, mode, timeouts);
    }
}
