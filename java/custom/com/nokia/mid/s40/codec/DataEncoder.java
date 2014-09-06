package com.nokia.mid.s40.codec;

import java.io.IOException;

public class DataEncoder {
    private String data;

    public DataEncoder(String name) throws IOException {
        throw new RuntimeException("DataEncoder not implemented");
    }
    
    public byte[] getData() throws IOException {
        throw new RuntimeException("DataEncoder::getData() not implemented");
    }
    
    public void putStart(int tag, String name) throws IOException {
        throw new RuntimeException("DataEncoder::putStart(int,String) not implemented");
    }
    
    public void putEnd(int tag, String name) throws IOException {
        throw new RuntimeException("DataEncoder::putEnd(int,String) not implemented");
    }
    
    public void put(int tag, String name, String value) throws IOException {
        throw new RuntimeException("DataEncoder::put(int,String,String) not implemented");
    }
    
    public void put(int tag, String name, boolean value) throws IOException {
        throw new RuntimeException("DataEncoder::put(int,String,boolean) not implemented");
    }
    
    public void put(int tag, String name, long value) throws IOException {
        throw new RuntimeException("DataEncoder::put(int,String,long) not implemented");
    }
    
    public void put(int tag, String name, double value) throws IOException {
        throw new RuntimeException("DataEncoder::put(int,String,double) not implemented");
    }
    
    public void put(String aString, byte[] aArray, int aInt) throws IOException {
        throw new RuntimeException("DataEncoder::put(String,byte[],int) not implemented");
    }
}