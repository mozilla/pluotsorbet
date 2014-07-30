/*
 *   
 *
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

package com.sun.midp.pki;

/**
 * This class implements miscellaneous utility methods including
 * those used for conversion of BigIntegers to byte arrays, 
 * hexadecimal printing of byte arrays etc.
 */ 
public class Utils {

    /** Hexadecimal digits. */
    private static char[] hc = {
        '0', '1', '2', '3', '4', '5', '6', '7', 
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    
    /**
     * Converts a subsequence of bytes in a byte array into a 
     * corresponding string of hexadecimal digits, each separated by a ":". 
     *
     * @param b byte array containing the bytes to be converted
     * @param off starting offset of the byte subsequence inside b
     * @param len number of bytes to be converted
     * @return a string of corresponding hexadecimal digits or
     * an error string
     */ 
    public static String hexEncode(byte[] b, int off, int len) {
        return new String(hexEncodeToChars(b, off, len));
    }

    /**
     * Converts a subsequence of bytes in a byte array into a 
     * corresponding string of hexadecimal digits, each separated by a ":". 
     *
     * @param b byte array containing the bytes to be converted
     * @param off starting offset of the byte subsequence inside b
     * @param len number of bytes to be converted
     * @return a string of corresponding hexadecimal digits or
     * an error string
     */ 
    public static char[] hexEncodeToChars(byte[] b, int off, int len) {
        char[] r;
        int v;
        int i;
        int j;
        
        if ((b == null) || (len == 0)) {
            return new char[0];
        }

        if ((off < 0) || (len < 0)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        if (len == 1) {
            r = new char[len * 2];
        } else {
            r = new char[(len * 3) - 1];
        }

        for (i = 0, j = 0; ; ) {
            v = b[off + i] & 0xff;
            r[j++] = hc[v >>> 4];
            r[j++] = hc[v & 0x0f];

            i++;
            if (i >= len) {
                break;
            }

            r[j++] = ':';
        }

        return r;
    }

    /**
     * Converts a byte array into a corresponding string of hexadecimal
     * digits. This is equivalent to hexEncode(b, 0, b.length).
     * <P />
     * @param b byte array to be converted
     * @return corresponding hexadecimal string
     */ 
    public static String hexEncode(byte[] b) {
        if (b == null)
            return ("");
        else 
            return hexEncode(b, 0, b.length);
    }

    /**
     * Converts a long value to a cooresponding 8-byte array 
     * starting with the most significant byte.
     * <P />
     * @param n 64-bit long integer value
     * @return a corresponding 8-byte array in network byte order
     */ 
    public static byte[] longToBytes(long n) {
        byte[] b = new byte[8];
        
        for (int i = 0; i < 64; i += 8) {
            b[i >> 3] = (byte) ((n >> (56 - i)) & 0xff);
        }
        return b;
    }

    /**
     * Checks if two byte arrays match.
     * <P />
     * @param a first byte array
     * @param aOff starting offset for comparison within a
     * @param b second byte array
     * @param bOff starting offset for comparison within b
     * @param len number of bytes to be compared
     * @return true if the sequence of len bytes in a starting at
     * aOff matches those in b starting at bOff, false otherwise
     */ 
    public static boolean byteMatch(byte[] a, int aOff, 
                             byte[] b, int bOff, int len) {
        if ((a.length < aOff + len) ||
            (b.length < bOff + len)) return false;
        
        for (int i = 0; i < len; i++) {
            if (a[i + aOff] != b[i + bOff])
                return false;
        }

        return true;
    }    
}
