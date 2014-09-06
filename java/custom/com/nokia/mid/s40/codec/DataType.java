package com.nokia.mid.s40.codec;

public class DataType {
    public static final int BOOLEAN = 0;
    public static final int CHAR = 1;
    public static final int BYTE = 2;
    public static final int WCHAR = 3;
    public static final int SHORT = 4;
    public static final int USHORT = 5;
    public static final int LONG = 6;
    public static final int ULONG = 7;
    public static final int FLOAT = 8;
    public static final int DOUBLE = 9;
    public static final int STRING = 10;
    public static final int WSTRING = 11;
    public static final int URI = 12;
    public static final int METHOD = 13;
    public static final int STRUCT = 14;
    public static final int LIST = 15;
    public static final int ARRAY = 16;
    public static final int GROUP_NOT_SUPPORTED = 0;
    public static final int GROUP_INTEGER = 1;
    public static final int GROUP_FLOAT = 2;
    public static final int GROUP_STRING = 3;
    public static final int GROUP_STRUCT = 4;
    
    public static int getTypeGroup(int aInt) {
        throw new RuntimeException("DataType::getTypeGroup(int) not implemented");
    }
}
