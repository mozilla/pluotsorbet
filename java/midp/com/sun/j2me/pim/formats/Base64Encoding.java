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

package com.sun.j2me.pim.formats;

import java.io.ByteArrayOutputStream;

/**
 * Converter to and from Base64 encoding. Base64 encoding is defined in
 * RFC 2045.
 *
 */
public class Base64Encoding {
    
    /** Constants for conversion of binary data to Base64. From RFC 2045. */
    private static char[] BASE64_CHARS = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
        'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9',
        '+', '/'
    };
    
    /**
     * Constants for conversion of Base64 data to binary. The inverse
     * of the above table. 
     */
    private static byte[] BASE64_BYTES = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, 0, -1, -1,
        -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, -1, -1, -1, -1,
        -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1
    };
    
    /**
     * Converts a BASE64 string to a byte array.
     *
     * @param sdata the string to be converted
     * @return the byte array content of the string
     */
    public static byte[] fromBase64(String sdata) {
        if (sdata == null || sdata.length() < 2) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // copy the string to an array, and pad the end of the data with
        // '=' characters.
        int length = sdata.length();
        char[] data = new char[length + 2];
        sdata.getChars(0, length, data, 0);
        data[length] = '=';
        data[length + 1] = '=';
        for (int i = nextCharIndex(data, 0); i < data.length; ) {
            int char0 = data[i = nextCharIndex(data, i)];
            if (char0 == '=') {
                break;
            }
            int char1 = data[i = nextCharIndex(data, i + 1)];
            if (char1 == '=') {
                // stop here. this is not a valid Base64 fragment
                break;
            }
            int char2 = data[i = nextCharIndex(data, i + 1)];
            int char3 = data[i = nextCharIndex(data, i + 1)];
            i = nextCharIndex(data, i + 1);
            out.write(BASE64_BYTES[char0] << 2 | BASE64_BYTES[char1] >> 4);
            if (char2 == '=') {
                // only one byte
            } else {
                int value = BASE64_BYTES[char1] << 4 | BASE64_BYTES[char2] >> 2;
                out.write(value & 0xff);
                if (char3 == '=') {
                    // only 2 bytes
                } else {
                    value = BASE64_BYTES[char2] << 6 | BASE64_BYTES[char3];
                    out.write(value & 0xff);
                }
            }
        }
        return out.toByteArray();
    }
    
    /**
     * Gets the index of the first character in the given array that
     * contains valid Base64 data. Starts searching from the given
     * index "i".
     * @param data input buffer
     * @param i starting offset in the input buffer
     * @return offset of first BASE64 char
     */
    private static int nextCharIndex(char[] data, int i) {
        while (i < data.length &&
        (data[i] > 0x7f || BASE64_BYTES[data[i]] == -1)) {
            
            i ++;
        }
        return i;
    }
    /**
     * Converts a byte array to a BASE64 string.
     * @param data the binary data to be converted
     * @param lineLength the length of lines to be created
     * @param indent the number of blank spaces to write at the beginning
     * of each line
     * @return BASE64 string
     */
    public static String toBase64(byte[] data, int lineLength, int indent) {
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < indent; j++)
            sb.append(' ');
        for (int i = 0, charsInLine = 0; i < data.length; ) {
            int byte0 = ((int) data[i++]) & 0xff;
            int byte1 = (i < data.length) ? ((int) data[i++]) & 0xff : 0x100;
            int byte2 = (i < data.length) ?  ((int) data[i++]) & 0xff : 0x100;

            sb.append(BASE64_CHARS[ byte0 >> 2 ]);
            if (byte1 == 0x100) {
                sb.append(BASE64_CHARS[ (byte0 << 4) & 0x30]);
                sb.append("==");
            } else {
                sb.append(BASE64_CHARS[ (byte0 << 4 | byte1 >> 4) & 0x3f]);
                if (byte2 == 0x100) {
                    sb.append(BASE64_CHARS[ (byte1 << 2) & 0x3f]);
                    sb.append('=');
                } else {
                    sb.append(BASE64_CHARS[ (byte1 << 2 | byte2 >> 6) & 0x3f]);
                    sb.append(BASE64_CHARS[ byte2 & 0x3f ]);
                }
            }
            charsInLine += 4;
            if (charsInLine + 4 > lineLength && i < data.length) {
                charsInLine = 0;                
                sb.append("\r\n");
                for (int j = 0; j < indent; j++) {
                    sb.append(' ');
                }
            }
        }
        return sb.toString();
    }

}
