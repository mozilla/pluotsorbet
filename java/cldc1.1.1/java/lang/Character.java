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
import com.sun.cldc.i18n.uclc.*;

/**
 * The Character class wraps a value of the primitive type <code>char</code>
 * in an object. An object of type <code>Character</code> contains a
 * single field whose type is <code>char</code>.
 * <p>
 * In addition, this class provides several methods for determining
 * the type of a character and converting characters from uppercase
 * to lowercase and vice versa.
 * <p>
 * Character information is based on the Unicode Standard, version 3.0.
 * However, in order to reduce footprint, by default the character
 * property and case conversion operations in CLDC are available
 * only for the ISO Latin-1 range of characters.  Other Unicode
 * character blocks can be supported as necessary.
 * <p>
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   JDK1.0, CLDC 1.0
 */

/*
 * Implementation note:
 * 
 * The character property and case conversion facilities 
 * provided by this CLDC implementation can be
 * extended by overriding an implementation class called 
 * DefaultCaseConverter.  Refer to the end of this file
 * for details.
 */

public final class Character extends Object {

    /**
     * The minimum radix available for conversion to and from Strings.
     *
     * @see     java.lang.Integer#toString(int, int)
     * @see     java.lang.Integer#valueOf(java.lang.String)
     */
    public static final int MIN_RADIX = 2;

    /**
     * The maximum radix available for conversion to and from Strings.
     *
     * @see     java.lang.Integer#toString(int, int)
     * @see     java.lang.Integer#valueOf(java.lang.String)
     */
    public static final int MAX_RADIX = 36;

    /**
     * The constant value of this field is the smallest value of type
     * <code>char</code>.
     *
     * @since   JDK1.0.2
     */
    public static final char   MIN_VALUE = '\u0000';

    /**
     * The constant value of this field is the largest value of type
     * <code>char</code>.
     *
     * @since   JDK1.0.2
     */
    public static final char   MAX_VALUE = '\uffff';

    /**
     * The value of the Character.
     */
    private char value;

    /**
     * Constructs a <code>Character</code> object and initializes it so
     * that it represents the primitive <code>value</code> argument.
     *
     * @param  value   value for the new <code>Character</code> object.
     */
    public Character(char value) {
        this.value = value;
    }

    /**
     * Returns the value of this Character object.
     * @return  the primitive <code>char</code> value represented by
     *          this object.
     */
    public char charValue() {
        return value;
    }

    /**
     * Returns a hash code for this Character.
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        return (int)value;
    }

    /**
     * Compares this object against the specified object.
     * The result is <code>true</code> if and only if the argument is not
     * <code>null</code> and is a <code>Character</code> object that
     * represents the same <code>char</code> value as this object.
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Character) {
            return value == ((Character)obj).charValue();
        }
        return false;
    }

    /**
     * Returns a String object representing this character's value.
     * Converts this <code>Character</code> object to a string. The
     * result is a string whose length is <code>1</code>. The string's
     * sole component is the primitive <code>char</code> value represented
     * by this object.
     *
     * @return  a string representation of this object.
     */
    public String toString() {
        char buf[] = {value};
        return String.valueOf(buf);
    }

   /**
     * Determines if the specified character is a lowercase character.
     * <p>
     * Note that by default CLDC only supports 
     * the ISO Latin-1 range of characters.
     * <p>
     * Of the ISO Latin-1 characters (character codes 0x0000 through 0x00FF),
     * the following are lowercase:
     * <p>
     * a b c d e f g h i j k l m n o p q r s t u v w x y z
     * &#92;u00DF &#92;u00E0 &#92;u00E1 &#92;u00E2 &#92;u00E3 &#92;u00E4 &#92;u00E5 &#92;u00E6 &#92;u00E7
     * &#92;u00E8 &#92;u00E9 &#92;u00EA &#92;u00EB &#92;u00EC &#92;u00ED &#92;u00EE &#92;u00EF &#92;u00F0
     * &#92;u00F1 &#92;u00F2 &#92;u00F3 &#92;u00F4 &#92;u00F5 &#92;u00F6 &#92;u00F8 &#92;u00F9 &#92;u00FA
     * &#92;u00FB &#92;u00FC &#92;u00FD &#92;u00FE &#92;u00FF
     *
     * @param   ch   the character to be tested.
     * @return  <code>true</code> if the character is lowercase;
     *          <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public static boolean isLowerCase(char ch) {
        return DefaultCaseConverter.isLowerCase(ch);
    }

   /**
     * Determines if the specified character is an uppercase character.
     * <p>
     * Note that by default CLDC only supports 
     * the ISO Latin-1 range of characters.
     * <p>
     * Of the ISO Latin-1 characters (character codes 0x0000 through 0x00FF),
     * the following are uppercase:
     * <p>
     * A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
     * &#92;u00C0 &#92;u00C1 &#92;u00C2 &#92;u00C3 &#92;u00C4 &#92;u00C5 &#92;u00C6 &#92;u00C7
     * &#92;u00C8 &#92;u00C9 &#92;u00CA &#92;u00CB &#92;u00CC &#92;u00CD &#92;u00CE &#92;u00CF &#92;u00D0
     * &#92;u00D1 &#92;u00D2 &#92;u00D3 &#92;u00D4 &#92;u00D5 &#92;u00D6 &#92;u00D8 &#92;u00D9 &#92;u00DA
     * &#92;u00DB &#92;u00DC &#92;u00DD &#92;u00DE
     *
     * @param   ch   the character to be tested.
     * @return  <code>true</code> if the character is uppercase;
     *          <code>false</code> otherwise.
     * @see     java.lang.Character#isLowerCase(char)
     * @see     java.lang.Character#toUpperCase(char)
     * @since   1.0
     */
    public static boolean isUpperCase(char ch) {
        return DefaultCaseConverter.isUpperCase(ch);
    }

    /**
     * Determines if the specified character is a digit.
     *
     * @param   ch   the character to be tested.
     * @return  <code>true</code> if the character is a digit;
     *          <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public static boolean isDigit(char ch) {
        return DefaultCaseConverter.isDigit(ch);
    }

    /**
     * The given character is mapped to its lowercase equivalent; if the
     * character has no lowercase equivalent, the character itself is
     * returned.
     * <p>
     * Note that by default CLDC only supports 
     * the ISO Latin-1 range of characters.
     *
     * @param   ch   the character to be converted.
     * @return  the lowercase equivalent of the character, if any;
     *          otherwise the character itself.
     * @see     java.lang.Character#isLowerCase(char)
     * @see     java.lang.Character#isUpperCase(char)
     * @see     java.lang.Character#toUpperCase(char)
     * @since   JDK1.0
     */
    public static char toLowerCase(char ch) {
        return DefaultCaseConverter.toLowerCase(ch);
    }

    /**
     * Converts the character argument to uppercase; if the
     * character has no uppercase equivalent, the character itself is
     * returned.
     * <p>
     * Note that by default CLDC only supports 
     * the ISO Latin-1 range of characters.
     *
     * @param   ch   the character to be converted.
     * @return  the uppercase equivalent of the character, if any;
     *          otherwise the character itself.
     * @see     java.lang.Character#isLowerCase(char)
     * @see     java.lang.Character#isUpperCase(char)
     * @see     java.lang.Character#toLowerCase(char)
     * @since   JDK1.0
     */
    public static char toUpperCase(char ch) {
        return DefaultCaseConverter.toUpperCase(ch);
    }

    /**
     * Returns the numeric value of the character <code>ch</code> in the
     * specified radix.
     *
     * @param   ch      the character to be converted.
     * @param   radix   the radix.
     * @return  the numeric value represented by the character in the
     *          specified radix.
     * @see     java.lang.Character#isDigit(char)
     * @since   JDK1.0
     */
    public static int digit(char ch, int radix) {
        return DefaultCaseConverter.digit(ch, radix);
    }
}
