/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */
package com.sun.mmedia;

/**
 * Utility class that provides various array conversion
 *
 */
public final class FormatConversionUtils {

    /** 
     * Private constructor to prevent instantiation
     */
    private FormatConversionUtils(){}

    /**
     * Converts image int[] array (32-bit per pixel) 
     * to byte[] xRGB array (4 bytes per pixel) 
     * int<->byte[4] conversion is platform specific !  
     *
     * @param ints integer array to convert
     * @return same array but converted to byte[] (in big-endian format)
     */
    public static byte[] intArrayToByteArray(int[] ints) {
        if (ints == null) return null;
        
        byte[] bytes = new byte[ints.length * 4];

        int intcount, bytecount;
        for (intcount = 0, bytecount = 0; intcount < ints.length; ) {
            bytes[bytecount++] = (byte)((ints[intcount] & 0xFF000000) >> 24);
            bytes[bytecount++] = (byte)((ints[intcount] & 0x00FF0000) >> 16);
            bytes[bytecount++] = (byte)((ints[intcount] & 0x0000FF00) >>  8);
            bytes[bytecount++] = (byte)((ints[intcount] & 0x000000FF)      );
            intcount++;
        }
        return bytes;
    }

    /**
     * Converts image byte[] xRGB array (4 bytes per pixel)
     * to int[] array (32-bit per pixel), 
     * int<->byte[4] conversion is platform specific !  
     *
     * @param bytes byte array to convert
     * @return same array but converted to int[] (in big-endian format)
     */
    public static int[] byteArrayToIntArray(byte[] bytes) {
        if (bytes == null) return null;
        
        int[] ints = new int[bytes.length / 4];

        int intcount, bytecount;
        for (intcount = 0, bytecount = 0; bytecount < bytes.length; ) {
            ints[intcount] = 
                (( ((int)(bytes[bytecount + 0])) << 24) & 0xFF000000) |  //A
                (( ((int)(bytes[bytecount + 1])) << 16) & 0x00FF0000) |  //R
                (( ((int)(bytes[bytecount + 2])) <<  8) & 0x0000FF00) |  //G
                (( ((int)(bytes[bytecount + 3]))      ) & 0x000000FF);   //B

            intcount++;
            bytecount+=4;
        }
        return ints;
    }
    
    /**
     * Creates a copy of a string array.
     * Primary usage of this method is to create copies of 
     * preset, parameter, metadata and other arrays, returned to user. 
     *
     * @param array String array to copy
     * @return copy of array
     */
    public static String[] stringArrayCopy(String[] array) {
        if (array == null) return null;
        
        String[] copy = new String[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }
}
