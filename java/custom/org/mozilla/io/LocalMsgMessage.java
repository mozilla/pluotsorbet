package org.mozilla.io;

import com.nokia.mid.s40.io.LocalMessageProtocolMessage;

public class LocalMsgMessage implements LocalMessageProtocolMessage {
    private byte[] data;
    private boolean reallocatable;

    public LocalMsgMessage(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        if (data != null) {
            return data.length;
        }

        return 0;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setReallocatable(boolean reallocatable) {
        this.reallocatable = reallocatable;
    }

    public boolean isReallocatable() {
        return reallocatable;
    }
}
