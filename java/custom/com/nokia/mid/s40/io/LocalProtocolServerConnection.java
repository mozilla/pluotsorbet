package com.nokia.mid.s40.io;

import java.io.IOException;
import javax.microedition.io.Connection;

public abstract interface LocalProtocolServerConnection extends Connection {
    public abstract String getLocalName() throws IOException;
    public abstract String getClientDomain();
    public abstract String getClientSecurityPolicy();
}
