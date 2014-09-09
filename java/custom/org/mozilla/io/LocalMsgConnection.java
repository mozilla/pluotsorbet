package org.mozilla.io;

import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.io.LocalMessageProtocolServerConnection;
import java.io.IOException;

public class LocalMsgConnection implements LocalMessageProtocolConnection, LocalMessageProtocolServerConnection {
    private String name;

    public LocalMsgConnection(String name) {
        this.name = name;
    }

    public native void init(String name);

    public LocalMsgConnection(String name, int mode, boolean timeouts) {
        this(name);
        init(name);
    }

    public native void closeConnection();

    public void close() throws IOException {
      closeConnection();
    }

    public native int receiveData(byte[] message);

    public int receive(byte[] message) {
        receiveData(message);
        return 0;
    }

    public void receive(LocalMessageProtocolMessage message) throws IOException {
        byte[] msg = new byte[4096];
        int length = receiveData(msg);
        byte[] data = new byte[length];
        System.arraycopy(msg, 0, data, 0, length);
        message.setData(data);
    }

    public native void sendData(byte[] message, int offset, int length);

    public void send(byte[] message, int offset, int length) {
        sendData(message, offset, length);
    }

    public LocalMessageProtocolMessage newMessage(byte[] data) {
        return new LocalMsgMessage(data);
    }

    public native void waitConnection();

    public LocalMessageProtocolConnection acceptAndOpen() {
        waitConnection();
        return this;
    }

    public final String getSecurityPolicy() {
        return null;
    }

    public final String getClientSecurityPolicy() {
        return null;
    }

    public final String getDomain() {
        return null;
    }

    public final String getClientDomain() {
        return null;
    }

    public final String getRemoteName() {
        return null;
    }

    public final String getLocalName() {
        return null;
    }
}
