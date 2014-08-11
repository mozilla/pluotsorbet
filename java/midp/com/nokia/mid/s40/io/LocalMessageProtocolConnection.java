package com.nokia.mid.s40.io;

import java.io.IOException;

public abstract interface LocalMessageProtocolConnection extends LocalProtocolConnection {
	public abstract LocalMessageProtocolMessage newMessage(byte[] data);
    public abstract int receive(byte[] message) throws IOException;
    public abstract void receive(LocalMessageProtocolMessage message) throws IOException;
    public abstract void send(byte[] message, int offset, int length) throws IOException;
}
