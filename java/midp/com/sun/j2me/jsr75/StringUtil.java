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

package com.sun.j2me.jsr75;

import java.util.Vector;

/**
 * Supporting methods for interpreting vCard and vCalendar encodings.
 *
 */
public class StringUtil {
    /**
     * Parses a separated list of strings into a string array.
     * An escaped separator (backslash followed by separatorChar) is not
     * treated as a separator.
     *
     * @param data input list to be parsed
     * @param separatorChar the character used to separate items
     * @param startingPoint Only use the part of the string that
     *     follows this index
     * @param skipFirstIfEmpty whether the first element should be skiped
     *     if it's empty (data starts with the separator).
     *     This flag is used to support empty category name
     * @return a non-null string array containing string elements
     */
    public static String[] split(String data, char separatorChar,
            int startingPoint, boolean skipFirstIfEmpty) {
        if (startingPoint == data.length()) {
            return new String[0];
        }

        // support for empty tokens:
        // if data starts with separator, just skip it
        if (skipFirstIfEmpty && data.charAt(startingPoint) == separatorChar) {
            startingPoint++;
        }

        // tokenize elements
        Vector elementList = new Vector();
        int startSearchAt = startingPoint;
        int startOfElement = startingPoint;
        for (int i; (i = data.indexOf(separatorChar, startSearchAt)) != -1; ) {
            if (i != 0 && data.charAt(i - 1) == '\\') {
                // escaped separator. don't treat it as a real separator
                startSearchAt = i + 1;
            } else {
                String element = data.substring(startOfElement, i);
                elementList.addElement(element);
                startSearchAt = startOfElement = i + 1;
            }
        }

        // there is no separator found
        if (elementList.size() == 0) {
            return new String[] { data.substring(startOfElement) };
        }

        // add the last element
        elementList.addElement(data.substring(startOfElement));

        // convert Vector to array
        int size = elementList.size();
        String[] elements = new String[size];
        for (int i = 0; i < size; i++) {
            elements[i] = (String) elementList.elementAt(i);
        }

        return elements;
    }

    /**
     * Parses a separated list of strings into a string array.
     * An escaped separator (backslash followed by separatorChar) is not
     * treated as a separator.
     *
     * @param data input list to be parsed
     * @param separatorChar the character used to separate items
     * @param startingPoint Only use the part of the string that
     *     follows this index
     * @return a non-null string array containing string elements
     */
    public static String[] split(String data, char separatorChar,
            int startingPoint) {
        return split(data, separatorChar, startingPoint, true);
    }

    /**
     * Joins the elements of a string array together into a single string.
     *
     * @param elements the string array
     * @param separator the string to be included between each pair of
     * successive elements
     * @return a string containing, alternately, elements of the string array
     * and the separator string
     */
    public static String join(String[] elements, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(elements[i]);
        }
        return sb.toString();
    }
}
