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

package java.lang;

/**
 * The Short class is the standard wrapper for short values.
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   JDK1.1, CLDC 1.0
 */
public final
class Short {

    /**
     * The minimum value a Short can have.
     */
    public static final short MIN_VALUE = -32768;

    /**
     * The maximum value a Short can have.
     */
    public static final short MAX_VALUE = 32767;

    /**
     * Assuming the specified String represents a short, returns
     * that short's value. Throws an exception if the String cannot
     * be parsed as a short.  The radix is assumed to be 10.
     *
     * @param s       the String containing the short
     * @return        The short value represented by the specified string
     * @exception     NumberFormatException If the string does not
     *                contain a parsable short.
     */
    public static short parseShort(String s) throws NumberFormatException {
        return parseShort(s, 10);
    }

    /**
     * Assuming the specified String represents a short, returns
     * that short's value in the radix specified by the second
     * argument. Throws an exception if the String cannot
     * be parsed as a short.
     *
     * @param s       the String containing the short
     * @param radix   the radix to be used
     * @return        The short value represented by the specified string in
     *                the specified radix.
     * @exception     NumberFormatException If the String does not
     *                contain a parsable short.
     */
    public static short parseShort(String s, int radix)
        throws NumberFormatException {
        int i = Integer.parseInt(s, radix);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException();
        return (short)i;
    }

    /**
     * The value of the Short.
     */
    private short value;

    /**
     * Constructs a Short object initialized to the specified short value.
     *
     * @param value     the initial value of the Short
     */
    public Short(short value) {
        this.value = value;
    }

    /**
     * Returns the value of this Short as a short.
     *
     * @return the value of this Short as a short.
     */
    public short shortValue() {
        return value;
    }

    /**
     * Returns a String object representing this Short's value.
     */
    public String toString() {
        return String.valueOf((int)value);
    }

    /**
     * Returns a hashcode for this Short.
     */
    public int hashCode() {
        return (int)value;
    }

    /**
     * Compares this object to the specified object.
     *
     * @param obj       the object to compare with
     * @return          true if the objects are the same; false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Short) {
            return value == ((Short)obj).shortValue();
        }
        return false;
    }

}
