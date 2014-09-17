package com.nokia.mid.s40.codec;

import java.io.IOException;

public class DataDecoder {
    public DataDecoder(String name, byte[] data, int offset, int length) throws IOException {
        init(data, offset, length);
    }

    private native void init(byte[] data, int offset, int length);

    public native String getName() throws IOException;

    public native int getType() throws IOException;

    public native boolean listHasMoreItems() throws IOException;

    public native void getStart(int tag) throws IOException;

    public native void getEnd(int tag) throws IOException;

    public native String getString(int tag) throws IOException;

    public native long getInteger(int tag) throws IOException;

    public double getFloat(int tag) throws IOException {
        throw new RuntimeException("DataDecoder::getName(int) not implemented");
    }

    public native boolean getBoolean() throws IOException;

    public byte[] getByteArray() throws IOException {
        throw new RuntimeException("DataDecoder::getByteArray() not implemented");
    }
}
