/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.cldc.i18n.uclc;

import java.io.*;
import com.sun.cldc.i18n.*;

public class DefaultCaseConverter {
    public static boolean isLowerCase(char ch) {
	return DefaultCaseConverter.toLowerCase(ch) == ch;
    }

    public static boolean isUpperCase(char ch) {
	return DefaultCaseConverter.toUpperCase(ch) == ch;
    }

    public static native char toLowerCase(char ch);
    public static native char toUpperCase(char ch);

    /**
     * Determines if the specified character is a digit.
     * This is currently only supported for ISO Latin-1 digits: "0" through "9".
     *
     * @param   ch   the character to be tested.
     * @return  <code>true</code> if the character is a digit;
     *          <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Returns the numeric value of the character <code>ch</code>
     * in the specified radix.
     * This is only supported for ISO Latin-1 characters.
     *
     * @param   ch      the character to be converted.
     * @param   radix   the radix.
     * @return  the numeric value represented by the character in the
     *          specified radix.
     * @see     java.lang.Character#isDigit(char)
     * @since   JDK1.0
     */
    public static int digit(char ch, int radix) {
        int value = -1;
        if (radix >= Character.MIN_RADIX && radix <= Character.MAX_RADIX) {
          if (isDigit(ch)) {
              value = ch - '0';
          }
          else if (isUpperCase(ch) || isLowerCase(ch)) {
              // Java supradecimal digit
              value = (ch & 0x1F) + 9;
          }
        }
        return (value < radix) ? value : -1;
    }
}
