package com.nokia.mid.s40.io;

import java.io.IOException;
import javax.microedition.io.Connection;

public abstract interface LocalProtocolConnection extends Connection {
    public abstract String getLocalName() throws IOException;
    public abstract String getRemoteName() throws IOException;
    public abstract String getDomain();
    public abstract String getSecurityPolicy();
}
