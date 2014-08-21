package com.nokia.mid.s40.io;

public abstract interface LocalMessageProtocolMessage {
    public abstract byte[] getData();
    public abstract void setData(byte[] data);
    public abstract int getLength();
    public abstract boolean isReallocatable();
    public abstract void setReallocatable(boolean reallocatable);
}
