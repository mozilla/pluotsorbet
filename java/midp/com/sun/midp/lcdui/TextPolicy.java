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

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.TextField;

/**
 * Class to handle conforming text to specified constraints
 */
public class TextPolicy {

    /**
     * Check is this is a valid decimal
     *
     * @param array string to check
     * @return true if this is a valid string
     */
    private static boolean checkDecimal(String array) {

        int len = array.length();

        /*
         * If the whole content is "-", ".", or "-.",
         * this is invalid.
         */
        if ((len == 1 && array.charAt(0) == '-') ||
            (len == 1 && array.charAt(0) == '.') ||
            (len == 2 && array.charAt(0) == '-' && array.charAt(1) == '.')) {
            return false;
        }

        /*
         * For decimal constraint, it is probably easier to re-validate the
         * whole content, than to try to validate the inserted data in
         * relation to the existing data around it.
         */
        boolean hasSeparator = false;
        for (int i = 0; i < len; i++) {
            char    c = array.charAt(i);

            /*
             * valid characters are
             *   [0-9],
             *   '-' at the first pos,
             *   '.' as the decimal separator.
             */
            if (c == '.') {
                if (!hasSeparator) {
                    hasSeparator = true;
                } else {
                    return false;
                }
            } else if (((c < '0') || (c > '9')) &&
                       (c != '-'  || i != 0)) {
                return false;
            }
        }
        return true;

    }

    /**
     * Check is this is a valid numeric
     *
     * @param array string to check
     * @return true if this is a valid string
     */
    private static boolean checkNumeric(String array) {

        int len = array.length();

        /* If the whole content is just a minus sign, this is invalid. */
        if (len == 1 && array.charAt(0) == '-') {
            return false;
        }

        int offset = 0;

        //
        // if first character is a minus sign then don't let the loop
        // below see it.
        //
        if (array.charAt(0) == '-') {
            offset++;
        }

        /*
         * Now we can just validate the inserted data. If we see a minus
         * sign then it must be in the wrong place because of the check
         * above
         */
        for (; offset < len; offset++) {
            char c = array.charAt(offset);
            if (((c < '0') || (c > '9'))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check is this is a valid phone number
     *
     * @param array string to check
     * @return true if this is a valid string
     */
    private static boolean checkPhoneNumber(String array) {
        int len = array.length();
        for (int i = 0; i < len; i++) {
            char c = array.charAt(i);
            if (((c < '0') || (c > '9')) &&
                (!(c == '#' || c == '*' || c == '+'))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check is this is a valid string given the constraints
     *
     * @param dca string to check
     * @param constraints the constraints 
     * @return true if this is a valid string
     */
    public static boolean isValidString(DynamicCharacterArray dca, 
                                        int constraints) { 

        if (dca.length() == 0) {
            return true;
        }

        switch (constraints & TextField.CONSTRAINT_MASK) {
            case TextField.ANY:         return true;
            case TextField.DECIMAL:     return checkDecimal(dca.toString());
            case TextField.EMAILADDR:   return true;
            case TextField.NUMERIC:     return checkNumeric(dca.toString());
            case TextField.PHONENUMBER: return checkPhoneNumber(dca.toString());
            case TextField.URL:         return true;
        }
        return false;
    } 
  
}

