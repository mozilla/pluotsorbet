package com.nokia.mid.s40.io;

import java.io.IOException;

public abstract interface LocalMessageProtocolServerConnection extends LocalProtocolServerConnection {
    public abstract LocalMessageProtocolConnection acceptAndOpen() throws IOException;
}
