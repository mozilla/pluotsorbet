package com.nokia.mid.s40.codec;

import java.io.IOException;

public class DataEncoder {
    public DataEncoder(String name) throws IOException {
        init();
    }

    public native void init();

    public native byte[] getData() throws IOException;

    public native void putStart(int tag, String name) throws IOException;

    public native void putEnd(int tag, String name) throws IOException;

    public native void put(int tag, String name, String value) throws IOException;

    public native void put(int tag, String name, boolean value) throws IOException;

    public native void put(int tag, String name, long value) throws IOException;

    public native void put(int tag, String name, double value) throws IOException;

    public void put(String aString, byte[] aArray, int aInt) throws IOException {
        throw new RuntimeException("DataEncoder::put(String,byte[],int) not implemented");
    }
}
