package com.nokia.mid.s40.codec;

import java.io.IOException;

public class DataDecoder {
    public DataDecoder(String name, byte[] data, int offset, int length) throws IOException {
        throw new RuntimeException("DataDecoder not implemented");
    }

    public String getName() throws IOException {
        throw new RuntimeException("DataDecoder::getName() not implemented");
    }

    public int getType() throws IOException {
        throw new RuntimeException("DataDecoder::getType() not implemented");
    }

    public boolean listHasMoreItems() throws IOException {
        throw new RuntimeException("DataDecoder::listHasMoreItems() not implemented");
    }

    public void getStart(int tag) throws IOException {
        throw new RuntimeException("DataDecoder::getStart(int) not implemented");
    }

    public void getEnd(int tag) throws IOException {
        throw new RuntimeException("DataDecoder::getEnd(int) not implemented");
    }

    public String getString(int tag) throws IOException {
        throw new RuntimeException("DataDecoder::getString(int) not implemented");
    }

    public long getInteger(int tag) throws IOException {
        throw new RuntimeException("DataDecoder::getInteger(int) not implemented");
    }

    public double getFloat(int tag) throws IOException {
        throw new RuntimeException("DataDecoder::getName(int) not implemented");
    }

    public boolean getBoolean() throws IOException {
        throw new RuntimeException("DataDecoder::getBoolean() not implemented");
    }

    public byte[] getByteArray() throws IOException {
        throw new RuntimeException("DataDecoder::getByteArray() not implemented");
    }
}
