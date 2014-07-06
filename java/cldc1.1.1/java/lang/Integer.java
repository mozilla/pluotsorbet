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
 * The Integer class wraps a value of the primitive type <code>int</code>
 * in an object. An object of type <code>Integer</code> contains a
 * single field whose type is <code>int</code>.
 * <p>
 * In addition, this class provides several methods for converting
 * an <code>int</code> to a <code>String</code> and a
 * <code>String</code> to an <code>int</code>, as well as other
 * constants and methods useful when dealing with an
 * <code>int</code>.
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   JDK1.0, CLDC 1.0
 */
public final class Integer {

    /**
     * The smallest value of type <code>int</code>. The constant
     * value of this field is <tt>-2147483648</tt>.
     */
    public static final int MIN_VALUE = 0x80000000;

    /**
     * The largest value of type <code>int</code>. The constant
     * value of this field is <tt>2147483647</tt>.
     */
    public static final int MAX_VALUE = 0x7fffffff;

    /**
     * All possible chars for representing a number as a String
     */
    final static char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    /**
     * Creates a string representation of the first argument in the
     * radix specified by the second argument.
     * <p>
     * If the radix is smaller than <code>Character.MIN_RADIX</code> or
     * larger than <code>Character.MAX_RADIX</code>, then the radix
     * <code>10</code> is used instead.
     * <p>
     * If the first argument is negative, the first element of the
     * result is the ASCII minus character <code>'-'</code>
     * (<tt>'&#92;u002d'</tt>). If the first
     * argument is not negative, no sign character appears in the result.
     * <p>
     * The remaining characters of the result represent the magnitude of
     * the first argument. If the magnitude is zero, it is represented by
     * a single zero character <tt>'0'</tt> (<tt>'&#92;u0030'</tt>); otherwise,
     * the first character of the representation of the magnitude will
     * not be the zero character.
     * The following ASCII characters are used as digits:
     * <blockquote><pre>
     *   0123456789abcdefghijklmnopqrstuvwxyz
     * </pre></blockquote>
     * These are <tt>'&#92;u0030'</tt> through <tt>'&#92;u0039'</tt> and
     * <tt>'&#92;u0061'</tt> through <tt>'&#92;u007a'</tt>. If the
     * <tt>radix</tt> is <var>N</var>, then the first <var>N</var> of these
     * characters are used as radix-<var>N</var> digits in the order shown.
     * Thus, the digits for hexadecimal (radix 16) are
     * <blockquote><pre>
     * <tt>0123456789abcdef</tt>.
     * </pre></blockquote>
     *
     * @param   i       an integer.
     * @param   radix   the radix.
     * @return  a string representation of the argument in the specified radix.
     * @see     java.lang.Character#MAX_RADIX
     * @see     java.lang.Character#MIN_RADIX
     */
    public static String toString(int i, int radix) {

        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        char buf[] = new char[33];
        boolean negative = (i < 0);
        int charPos = 32;

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = digits[-(i % radix)];
            i = i / radix;
        }
        buf[charPos] = digits[-i];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (33 - charPos));
    }

    /**
     * Creates a string representation of the integer argument as an
     * unsigned integer in base&nbsp;16.
     * <p>
     * The unsigned integer value is the argument plus 2<sup>32</sup> if
     * the argument is negative; otherwise, it is equal to the argument.
     * This value is converted to a string of ASCII digits in hexadecimal
     * (base&nbsp;16) with no extra leading <code>0</code>s. If the
     * unsigned magnitude is zero, it is represented by a single zero
     * character <tt>'0'</tt> (<tt>'&#92;u0030'</tt>); otherwise, the first
     * character of the representation of the unsigned magnitude will
     * not be the zero character. The following characters are used as
     * hexadecimal digits:
     * <blockquote><pre>
     * 0123456789abcdef
     * </pre></blockquote>
     * These are the characters <tt>'&#92;u0030'</tt> through <tt>'&#92;u0039'</tt>
     * and <tt>'u\0039'</tt> through <tt>'&#92;u0066'</tt>.
     *
     * @param   i   an integer.
     * @return  the string representation of the unsigned integer value
     *          represented by the argument in hexadecimal (base&nbsp;16).
     * @since   JDK1.0.2
     */
    public static String toHexString(int i) {
        return toUnsignedString(i, 4);
    }

    /**
     * Creates a string representation of the integer argument as an
     * unsigned integer in base 8.
     * <p>
     * The unsigned integer value is the argument plus 2<sup>32</sup> if
     * the argument is negative; otherwise, it is equal to the argument.
     * This value is converted to a string of ASCII digits in octal
     * (base&nbsp;8) with no extra leading <code>0</code>s.
     * <p>
     * If the unsigned magnitude is zero, it is represented by a single
     * zero character <tt>'0'</tt> (<tt>'&#92;u0030'</tt>); otherwise, the
     * first character of the representation of the unsigned magnitude will
     * not be the zero character. The octal digits are:
     * <blockquote><pre>
     * 01234567
     * </pre></blockquote>
     * These are the characters <tt>'&#92;u0030'</tt> through <tt>'&#92;u0037'</tt>.
     *
     * @param   i   an integer
     * @return  the string representation of the unsigned integer value
     *          represented by the argument in octal (base&nbsp;8).
     * @since   JDK1.0.2
     */
    public static String toOctalString(int i) {
        return toUnsignedString(i, 3);
    }

    /**
     * Creates a string representation of the integer argument as an
     * unsigned integer in base&nbsp;2.
     * <p>
     * The unsigned integer value is the argument plus 2<sup>32</sup>if
     * the argument is negative; otherwise it is equal to the argument.
     * This value is converted to a string of ASCII digits in binary
     * (base&nbsp;2) with no extra leading <code>0</code>s.
     *
     * If the unsigned magnitude is zero, it is represented by a single
     * zero character <tt>'0'</tt> (<tt>'&#92;u0030'</tt>); otherwise, the
     * first character of the representation of the unsigned magnitude
     * will not be the zero character. The characters <tt>'0'</tt>
     * (<tt>'&#92;u0030'</tt>) and <tt>'1'</tt> (<tt>'&#92;u0031'</tt>) are used
     * as binary digits.
     *
     * @param   i   an integer.
     * @return  the string representation of the unsigned integer value
     *          represented by the argument in binary (base&nbsp;2).
     * @since   JDK1.0.2
     */
    public static String toBinaryString(int i) {
        return toUnsignedString(i, 1);
    }

    /**
     * Convert the integer to an unsigned number.
     */
    private static String toUnsignedString(int i, int shift) {
        char[] buf = new char[32];
        int charPos = 32;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[--charPos] = digits[i & mask];
            i >>>= shift;
        } while (i != 0);

        return new String(buf, charPos, (32 - charPos));
    }

    /**
     * Returns a new String object representing the specified integer. The
     * argument is converted to signed decimal representation and returned
     * as a string, exactly as if the argument and radix <tt>10</tt> were
     * given as arguments to the {@link #toString(int, int)} method.
     *
     * @param   i   an integer to be converted.
     * @return  a string representation of the argument in base&nbsp;10.
     */
    public static String toString(int i) {
        return toString(i, 10);
    }

    /**
     * Parses the string argument as a signed integer in the radix
     * specified by the second argument. The characters in the string
     * must all be digits of the specified radix (as determined by
     * whether {@link java.lang.Character#digit(char, int)} returns a
     * nonnegative value), except that the first character may be an
     * ASCII minus sign <code>'-'</code> (<code>'&#92;u002d'</code>) to
     * indicate a negative value. The resulting integer value is returned.
     * <p>
     * An exception of type <tt>NumberFormatException</tt> is thrown if any
     * of the following situations occurs:
     * <ul>
     * <li>The first argument is <tt>null</tt> or is a string of length zero.
     * <li>The radix is either smaller than
     * {@link java.lang.Character#MIN_RADIX} or
     * larger than {@link java.lang.Character#MAX_RADIX}.
     * <li>Any character of the string is not a digit of the specified radix,
     * except that the first character may be a minus sign <tt>'-'</tt>
     * (<tt>'&#92;u002d'</tt>) provided that the string is longer than length 1.
     * <li>The integer value represented by the string is not a value of type
     * <tt>int</tt>.
     * </ul><p>
     * Examples:
     * <blockquote><pre>
     * parseInt("0", 10) returns 0
     * parseInt("473", 10) returns 473
     * parseInt("-0", 10) returns 0
     * parseInt("-FF", 16) returns -255
     * parseInt("1100110", 2) returns 102
     * parseInt("2147483647", 10) returns 2147483647
     * parseInt("-2147483648", 10) returns -2147483648
     * parseInt("2147483648", 10) throws a NumberFormatException
     * parseInt("99", 8) throws a NumberFormatException
     * parseInt("Kona", 10) throws a NumberFormatException
     * parseInt("Kona", 27) returns 411787
     * </pre></blockquote>
     *
     * @param      s   the <code>String</code> containing the integer.
     * @param      radix   the radix to be used.
     * @return     the integer represented by the string argument in the
     *             specified radix.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable integer.

     */
    public static int parseInt(String s, int radix)
                throws NumberFormatException
    {
        if (s == null) {
            throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "null"
/* #endif */
            );
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "radix " + radix +
/// skipped                       " less than Character.MIN_RADIX"
/* #endif */
            );
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "radix " + radix +
/// skipped                       " greater than Character.MAX_RADIX"
/* #endif */
            );
        }

        int result = 0;
        boolean negative = false;
        int i = 0, max = s.length();
        int limit;
        int multmin;
        int digit;

        if (max > 0) {
            if (s.charAt(0) == '-') {
                negative = true;
                limit = Integer.MIN_VALUE;
                i++;
            } else {
                limit = -Integer.MAX_VALUE;
            }
            multmin = limit / radix;
            if (i < max) {
                digit = Character.digit(s.charAt(i++),radix);
                if (digit < 0) {
                    throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                               s
/* #endif */
                    );
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++),radix);
                if (digit < 0) {
                    throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                               s
/* #endif */
                    );
                }
                if (result < multmin) {
                    throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                               s
/* #endif */
                    );
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                               s
/* #endif */
                    );
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       s
/* #endif */
            );
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else {    /* Only got "-" */
                throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                           s
/* #endif */
                );
            }
        } else {
            return -result;
        }
    }

    /**
     * Parses the string argument as a signed decimal integer. The
     * characters in the string must all be decimal digits, except that
     * the first character may be an ASCII minus sign <code>'-'</code>
     * (<tt>'&#92;u002d'</tt>) to indicate a negative value. The resulting
     * integer value is returned, exactly as if the argument and the radix
     * 10 were given as arguments to the
     * {@link #parseInt(java.lang.String, int)} method.
     *
     * @param      s   a string.
     * @return     the integer represented by the argument in decimal.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable integer.
     */
    public static int parseInt(String s) throws NumberFormatException {
        return parseInt(s,10);
    }

    /**
     * Returns a new Integer object initialized to the value of the
     * specified String. The first argument is interpreted as representing
     * a signed integer in the radix specified by the second argument,
     * exactly as if the arguments were given to the
     * {@link #parseInt(java.lang.String, int)} method. The result is an
     * <code>Integer</code> object that represents the integer value
     * specified by the string.
     * <p>
     * In other words, this method returns an <code>Integer</code> object
     * equal to the value of:
     * <blockquote><pre>
     * new Integer(Integer.parseInt(s, radix))
     * </pre></blockquote>
     *
     * @param      s   the string to be parsed.
     * @param      radix the radix of the integer represented by string
     *             <tt>s</tt>
     * @return     a newly constructed <code>Integer</code> initialized to the
     *             value represented by the string argument in the specified
     *             radix.
     * @exception  NumberFormatException  if the String cannot be
     *             parsed as an <code>int</code>.
     */
    public static Integer valueOf(String s, int radix) throws NumberFormatException {
        return new Integer(parseInt(s,radix));
    }

    /**
     * Returns a new Integer object initialized to the value of the
     * specified String. The argument is interpreted as representing a
     * signed decimal integer, exactly as if the argument were given to
     * the {@link #parseInt(java.lang.String)} method. The result is an
     * <tt>Integer</tt> object that represents the integer value specified
     * by the string.
     * <p>
     * In other words, this method returns an <tt>Integer</tt> object equal
     * to the value of:
     * <blockquote><pre>
     * new Integer(Integer.parseInt(s))
     * </pre></blockquote>
     *
     * @param      s   the string to be parsed.
     * @return     a newly constructed <code>Integer</code> initialized to the
     *             value represented by the string argument.
     * @exception  NumberFormatException  if the string cannot be parsed
     *             as an integer.
     */
    public static Integer valueOf(String s) throws NumberFormatException
    {
        return new Integer(parseInt(s, 10));
    }

    /**
     * The value of the Integer.
     *
     * @serial
     */
    private int value;

    /**
     * Constructs a newly allocated <code>Integer</code> object that
     * represents the primitive <code>int</code> argument.
     *
     * @param   value   the value to be represented by the <code>Integer</code>.
     */
    public Integer(int value) {
        this.value = value;
    }

    /**
     * Returns the value of this Integer as a byte.
     *
     * @return the value of this Integer as a byte.
     *
     * @since   JDK1.1
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * Returns the value of this Integer as a short.
     *
     * @return the value of this Integer as a short.
     *
     * @since   JDK1.1
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * Returns the value of this Integer as an int.
     *
     * @return  the <code>int</code> value represented by this object.
     */
    public int intValue() {
        return value;
    }

    /**
     * Returns the value of this Integer as a <tt>long</tt>.
     *
     * @return  the <code>int</code> value represented by this object that is
     *          converted to type <code>long</code> and the result of the
     *          conversion is returned.
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * Returns the value of this Integer as a <tt>float</tt>.
     *
     * @return  the <code>int</code> value represented by this object is
     *          converted to type <code>float</code> and the result of the
     *          conversion is returned.
     * @since   CLDC 1.1
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * Returns the value of this Integer as a <tt>double</tt>.
     *
     * @return  the <code>int</code> value represented by this object is
     *          converted to type <code>double</code> and the result of the
     *          conversion is returned.
     * @since   CLDC 1.1
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * Returns a String object representing this Integer's value. The
     * value is converted to signed decimal representation and returned
     * as a string, exactly as if the integer value were given as an
     * argument to the {@link java.lang.Integer#toString(int)} method.
     *
     * @return  a string representation of the value of this object in
     *          base&nbsp;10.
     */
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Returns a hashcode for this Integer.
     *
     * @return  a hash code value for this object, equal to the
     *          primitive <tt>int</tt> value represented by this
     *          <tt>Integer</tt> object.
     */
    public int hashCode() {
        return value;
    }

    /**
     * Compares this object to the specified object.
     * The result is <code>true</code> if and only if the argument is not
     * <code>null</code> and is an <code>Integer</code> object that contains
     * the same <code>int</code> value as this object.
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return value == ((Integer)obj).intValue();
        }
        return false;
    }

}
