package com.sun.cldchi.jvm;

public class JVM {
    public static native void unchecked_char_arraycopy(char[] src,
						       int srcOffset,
						       char[] dst,
						       int dstOffset,
						       int length);

    public static native void unchecked_int_arraycopy(int[] src,
						      int srcOffset,
						      int[] dst,
						      int dstOffset, 
						      int length);

    public static native void unchecked_obj_arraycopy(Object[] src,
						      int srcOffset,
						      Object[] dst,
						      int dstOffset,
						      int length);

    public static native long monotonicTimeMillis();
}
