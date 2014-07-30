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

package com.sun.midp.io;

import java.util.Vector;

/** Contains static utility methods for IO protocol classes to use. */
public abstract class Util {
    /**
     * Converts <code>string</code> into a null terminated
     * byte array.  Expects the characters in <code>string
     * </code> to be in th ASCII range (0-127 base 10).
     *
     * @param string the string to convert
     *
     * @return byte array with contents of <code>string</code>
     */
    public static byte[] toCString(String string) {
        int length = string.length();
        byte[] cString = new byte[length + 1];

        for (int i = 0; i < length; i++) {
            cString[i] = (byte)string.charAt(i);
        }

        return cString;
    }

    /**
     * Converts an ASCII null terminated byte array in to a
     * <code>String</code>.  Expects the characters in byte array
     * to be in th Ascii range (0-127 base 10).
     *
     * @param cString the byte array to convert
     *
     * @return string with contents of the byte array
     * @exception ArrayIndexOutOfBounds if the C string does not end with 0
     */
    public static String toJavaString(byte[] cString) {
        int i;
        String jString;

        // find the string length
        for (i = 0; cString[i] != 0; i++);

        try {
            return new String(cString, 0, i, "ISO8859_1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Create a vector of values from a string containing comma separated
     * values. The values cannot contain a comma. The output values will be
     * trimmed of whitespace. The vector may contain zero length strings
     * where there are 2 commas in a row or a comma at the end of the input
     * string.
     *
     * @param input input string of comma separated values
     *
     * @return vector of string values.
     */
    public static Vector getCommaSeparatedValues(String input) {
        return getDelimSeparatedValues(input, ',');
    }
    
    /**
     * Create a vector of values from a string containing delimiter separated
     * values. The values cannot contain the delimiter. The output values will
     * be trimmed of whitespace. The vector may contain zero length strings
     * where there are 2 delimiters in a row or a comma at the end of the input
     * string.
     *
     * @param input input string of delimiter separated values
     * @param delim the delimiter separating values
     * @return vector of string values.
     */
    public static Vector getDelimSeparatedValues(String input, char delim) {
        Vector output = new Vector(5, 5);
        int len;
        int start;
        int end;
        
        len = input.length();
        if (len == 0) {
            return output;
        }

        for (start = 0; ; ) {
            end = input.indexOf(delim, start);
            if (end == -1) {
                break;
            }

            output.addElement(input.substring(start, end).trim());
            start = end + 1;
        }

        end = len;
        output.addElement(input.substring(start, end).trim());

        return output;
    }

    /**
     * Parses out the media-type from the given HTTP content-type field and
     * converts it to lower case.
     * The media-type everything for the ';' that marks the parameters.
     *
     * @param contentType value of the content-type field
     *
     * @return media-type in lower case
     */
    public static String getHttpMediaType(String contentType) {
        int semiColon;

        if (contentType == null) {
            return null;
        }

        semiColon = contentType.indexOf(';');
        if (semiColon < 0) {
            return contentType.toLowerCase();
        }

        return contentType.substring(0, semiColon).toLowerCase();
    }
}
