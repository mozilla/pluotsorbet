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
 * The Long class wraps a value of the primitive type <code>long</code>
 * in an object. An object of type <code>Long</code> contains a single
 * field whose type is <code>long</code>.
 * <p>
 * In addition, this class provides several methods for converting a
 * <code>long</code> to a <code>String</code> and a
 * <code>String</code> to a <code>long</code>, as well as other
 * constants and methods useful when dealing with a
 * <code>long</code>.
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   JDK1.0, CLDC 1.0
 */
public final class Long {
    /**
     * The smallest value of type <code>long</code>.
     */
    public static final long MIN_VALUE = 0x8000000000000000L;

    /**
     * The largest value of type <code>long</code>.
     */
    public static final long MAX_VALUE = 0x7fffffffffffffffL;

    /**
     * Creates a string representation of the first argument in the
     * radix specified by the second argument.
     * <p>
     * If the radix is smaller than <code>Character.MIN_RADIX</code> or
     * larger than <code>Character.MAX_RADIX</code>, then the radix
     * <code>10</code> is used instead.
     * <p>
     * If the first argument is negative, the first element of the
     * result is the ASCII minus sign <code>'-'</code>
     * (<code>'&#92;u002d'</code>. If the first argument is not negative,
     * no sign character appears in the result.
     * <p>
     * The remaining characters of the result represent the magnitude of
     * the first argument. If the magnitude is zero, it is represented by
     * a single zero character <code>'0'</code>
     * (<code>'&#92;u0030'</code>); otherwise, the first character of the
     * representation of the magnitude will not be the zero character.
     * The following ASCII characters are used as digits:
     * <blockquote><pre>
     *   0123456789abcdefghijklmnopqrstuvwxyz
     * </pre></blockquote>
     * These are <tt>'&#92;u0030'</tt> through <tt>'&#92;u0039'</tt>
     * and <tt>'&#92;u0061'</tt> through <tt>'&#92;u007a'</tt>. If the
     * radix is <var>N</var>, then the first <var>N</var> of these
     * characters are used as radix-<var>N</var> digits in the order
     * shown. Thus, the digits for hexadecimal (radix 16) are
     * <blockquote><pre>
     * <tt>0123456789abcdef</tt>.
     * </pre></blockquote>
     *
     * @param   i       a long.
     * @param   radix   the radix.
     * @return  a string representation of the argument in the specified radix.
     * @see     java.lang.Character#MAX_RADIX
     * @see     java.lang.Character#MIN_RADIX
     */
    public static String toString(long i, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = Integer.digits[(int)(-(i % radix))];
            i = i / radix;
        }
        buf[charPos] = Integer.digits[(int)(-i)];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (65 - charPos));
    }

    /**
     * Returns a new String object representing the specified integer.
     * The argument is converted to signed decimal representation and
     * returned as a string, exactly as if the argument and the radix
     * 10 were given as arguments to the
     * {@link #toString(long, int)} method that takes two arguments.
     *
     * @param   i   a <code>long</code> to be converted.
     * @return  a string representation of the argument in base&nbsp;10.
     */
    public static String toString(long i) {
        return toString(i, 10);
    }

    /**
     * Parses the string argument as a signed <code>long</code> in the
     * radix specified by the second argument. The characters in the
     * string must all be digits of the specified radix (as determined by
     * whether <code>Character.digit</code> returns a
     * nonnegative value), except that the first character may be an
     * ASCII minus sign <code>'-'</code> (<tt>'&#92;u002d'</tt> to indicate
     * a negative value. The resulting <code>long</code> value is returned.
     * <p>
     * Note that neither <tt>L</tt> nor <tt>l</tt> is permitted to appear at
     * the end of the string as a type indicator, as would be permitted in
     * Java programming language source code - except that either <tt>L</tt>
     * or <tt>l</tt> may appear as a digit for a radix greater than 22.
     * <p>
     * An exception of type <tt>NumberFormatException</tt> is thrown if any of
     * the following situations occurs:
     * <ul>
     * <li>The first argument is <tt>null</tt> or is a string of length zero.
     * <li>The <tt>radix</tt> is either smaller than
     *     {@link java.lang.Character#MIN_RADIX} or larger than
     *     {@link java.lang.Character#MAX_RADIX}.
     * <li>The first character of the string is not a digit of the
     *     specified <tt>radix</tt> and is not a minus sign <tt>'-'</tt>
     *     (<tt>'&#92;u002d'</tt>).
     * <li>The first character of the string is a minus sign and the
     *     string is of length 1.
     * <li>Any character of the string after the first is not a digit of
     *     the specified <tt>radix</tt>.
     * <li>The integer value represented by the string cannot be
     *     represented as a value of type <tt>long</tt>.
     * </ul><p>
     * Examples:
     * <blockquote><pre>
     * parseLong("0", 10) returns 0L
     * parseLong("473", 10) returns 473L
     * parseLong("-0", 10) returns 0L
     * parseLong("-FF", 16) returns -255L
     * parseLong("1100110", 2) returns 102L
     * parseLong("99", 8) throws a NumberFormatException
     * parseLong("Hazelnut", 10) throws a NumberFormatException
     * parseLong("Hazelnut", 36) returns 1356099454469L
     * </pre></blockquote>
     *
     * @param      s       the <code>String</code> containing the
     *                     <code>long</code>.
     * @param      radix   the radix to be used.
     * @return     the <code>long</code> represented by the string argument in
     *             the specified radix.
     * @exception  NumberFormatException  if the string does not contain a
     *                                    parsable integer.
     */
    public static long parseLong(String s, int radix)
              throws NumberFormatException
    {
      if (s == null) {
          throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                     "null"
/* #endif */
          );
      }

      if (radix < Character.MIN_RADIX) {
          throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                     "radix " + radix +
/// skipped                     " less than Character.MIN_RADIX"
/* #endif */
          );
      }
      if (radix > Character.MAX_RADIX) {
          throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                     "radix " + radix +
/// skipped                     " greater than Character.MAX_RADIX"
/* #endif */
          );
      }

      long result = 0;
      boolean negative = false;
      int i = 0, max = s.length();
      long limit;
      long multmin;
      int digit;

      if (max > 0) {
          if (s.charAt(0) == '-') {
              negative = true;
              limit = Long.MIN_VALUE;
              i++;
          } else {
              limit = -Long.MAX_VALUE;
          }
          multmin = limit / radix;
            if (i < max) {
                digit = Character.digit(s.charAt(i++),radix);
              if (digit < 0) {
                  throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                             s
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
/// skipped                             s
/* #endif */
                  );
              }
              if (result < multmin) {
                  throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                             s
/* #endif */
                  );
              }
              result *= radix;
              if (result < limit + digit) {
                  throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                             s
/* #endif */
                  );
              }
              result -= digit;
          }
      } else {
          throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                     s
/* #endif */
          );
      }
      if (negative) {
          if (i > 1) {
              return result;
          } else {    /* Only got "-" */
              throw new NumberFormatException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                         s
/* #endif */
              );
          }
      } else {
          return -result;
      }
    }

    /**
     * Parses the string argument as a signed decimal <code>long</code>.
     * The characters in the string must all be decimal digits, except
     * that the first character may be an ASCII minus sign
     * <code>'-'</code> (<code>&#92;u002d'</code>) to indicate a negative
     * value. The resulting long value is returned, exactly as if the
     * argument and the radix <tt>10</tt> were given as arguments to the
     * {@link #parseLong(String, int)} method that takes two arguments.
     * <p>
     * Note that neither <tt>L</tt> nor <tt>l</tt> is permitted to appear
     * at the end of the string as a type indicator, as would be permitted
     * in Java programming language source code.
     *
     * @param      s   a string.
     * @return     the <code>long</code> represented by the argument in decimal.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable <code>long</code>.
     */
    public static long parseLong(String s) throws NumberFormatException {
      return parseLong(s, 10);
    }

    /**
     * The value of the Long.
     */
    private long value;

    /**
     * Constructs a newly allocated <code>Long</code> object that
     * represents the primitive <code>long</code> argument.
     *
     * @param   value   the value to be represented by the
     *          <code>Long</code> object.
     */
    public Long(long value) {
        this.value = value;
    }

    /**
     * Returns the value of this Long as a long value.
     *
     * @return  the <code>long</code> value represented by this object.
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * Returns the value of this Long as a float.
     *
     * @return  the <code>long</code> value represented by this object is
     *          converted to type <code>float</code> and the result of
     *          the conversion is returned.
     * @since   CLDC 1.1
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * Returns the value of this Long as a double.
     *
     * @return  the <code>long</code> value represented by this object that
     *          is converted to type <code>double</code> and the result of
     *          the conversion is returned.
     * @since   CLDC 1.1
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * Returns a String object representing this Long's value.
     * The long integer value represented by this Long object is converted
     * to signed decimal representation and returned as a string, exactly
     * as if the long value were given as an argument to the
     * {@link #toString(long)} method that takes one argument.
     *
     * @return  a string representation of this object in base&nbsp;10.
     */
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Computes a hashcode for this Long. The result is the exclusive
     * OR of the two halves of the primitive <code>long</code> value
     * represented by this <code>Long</code> object. That is, the hashcode
     * is the value of the expression:
     * <blockquote><pre>
     * (int)(this.longValue()^(this.longValue()>>>32))
     * </pre></blockquote>
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        return (int)(value ^ (value >> 32));
    }

    /**
     * Compares this object against the specified object.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and is a <code>Long</code> object that
     * contains the same <code>long</code> value as this object.
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Long) {
            return value == ((Long)obj).longValue();
        }
        return false;
    }

}
