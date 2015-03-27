/*
 *
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*
 * (c) Copyright 2001, 2002 Motorola, Inc.  ALL RIGHTS RESERVED.
 */
package javax.bluetooth;

/*
 * This class is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED
public class UUID {

    // JAVADOC COMMENT ELIDED
    private long highBits;

    // JAVADOC COMMENT ELIDED
    private long lowBits;

    // JAVADOC COMMENT ELIDED
    private static final long BASE_UUID_HIGHT = 0x1000L;

    // JAVADOC COMMENT ELIDED
    private static final long BASE_UUID_LOW = 0x800000805F9B34FBL;

    // JAVADOC COMMENT ELIDED
    private static final char[] digits = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'A', 'B',
        'C', 'D', 'E', 'F'
    };

    // JAVADOC COMMENT ELIDED
    public UUID(long uuidValue) {

        // check the specified value is out of range
        if (uuidValue < 0 || uuidValue > 0xffffffffL) {
            throw new IllegalArgumentException(
                    "The 'uuidValue' is out of [0, 2^32 - 1] range: "
                    + uuidValue);
        }

        /*
         * Create a UUID from 16/32 bits value.
         *
         * 128_bit_value = 16_bit_value * 2^96 + Bluetooth_Base_UUID
         * 128_bit_value = 32_bit_value * 2^96 + Bluetooth_Base_UUID
         *
         * No need to check the "overflow/negative", because
         * uuidValue is 32 bits & BASE_UUID_HIGHT is 16 bits.
         */
        highBits = (uuidValue << 32) | BASE_UUID_HIGHT;
        lowBits = BASE_UUID_LOW;
    }

    // JAVADOC COMMENT ELIDED
    public UUID(String uuidValue, boolean shortUUID) {
        if (uuidValue == null) {
            throw new NullPointerException("Specified 'uuidValue' is null");
        }

        /*
         * The zero length is double checked by the parsing operation,
         * but the NumberFormatException is thrown in that case -
         * we need IllegalArgumentException according to spec.
         */
        if (uuidValue.length() == 0 || (shortUUID && uuidValue.length() > 8) ||
                uuidValue.length() > 32) {
            throw new IllegalArgumentException(
                    "Invalid length of specified 'uuidValue': "
                    + uuidValue.length());
        }

        // check if sign character presents
        if (uuidValue.indexOf('-') != -1) {
            throw new NumberFormatException(
                    "The '-' character is not allowed: " + uuidValue);
        }

        /*
         * 16-bit or 32-bit UUID case.
         */
        if (shortUUID) {

            // this checks the format and may throw a NumberFormatException
            long val = Long.parseLong(uuidValue, 16);

            /*
             * create a UUID from 16/32 bits value.
             *
             * No need to check the "overflow/negative", because
             * lVal is 32 bits & BASE_UUID_HIGHT is 16 bits.
             */
            highBits = (val << 32) | BASE_UUID_HIGHT;
            lowBits = BASE_UUID_LOW;
            return;
        }

        /*
         * 128-bit UUID case.
         */
        highBits = 0x0L;

        // simple case (optimization)
        if (uuidValue.length() < 16) {
            lowBits = Long.parseLong(uuidValue, 16);
            return;
        }

        /*
         * We have to do a 32 bits parsing, because the
         * Long.parseLong("ffff ffff ffff ffff") does not
         * parse such an unsigned number.
         */
        int l = uuidValue.length();
        lowBits = Long.parseLong(uuidValue.substring(l - 8), 16);
        lowBits |= (Long.parseLong(uuidValue.substring(l - 16, l - 8), 16)
                << 32);

        if (l == 16) {
            return;
        }

        if (l <= 24) {
            highBits = Long.parseLong(uuidValue.substring(0, l - 16), 16);
        } else {
            highBits = Long.parseLong(uuidValue.substring(l - 24, l - 16), 16);
            highBits |= (Long.parseLong(uuidValue.substring(0, l - 24), 16)
                    << 32);
        }
    }

    // JAVADOC COMMENT ELIDED
    public String toString() {

        /*
         * This implementation is taken from cldc1.1 Integer#toUnsignedString
         * one. The implementation which uses Integer#toHexString() is
         * 2-3 times slower, so such a code duplication is required here.
         */
        int[] ints = new int[] {
            (int) (lowBits & 0xffffffffL),
            (int) (lowBits >>> 32 & 0xffffffffL),
            (int) (highBits & 0xffffffffL),
            (int) (highBits >>> 32 & 0xffffffffL)
        };
        int charPos = 32;
        char[] buf = new char[charPos];
        int shift = 4;
        int mask = 0xf;
        int needZerosIndex = -1;

        /*
         * check with part of value requires the zero characters.
         *
         * I.e. the original algorithm gives as an 1 character
         * for the value '1', but we may want 00000001.
         */
        for (int i = 3; i >= 0; i--) {
            if (ints[i] != 0) {
                needZerosIndex = i - 1;
                break;
            }
        }

        /*
         * Process parts of UUID from low parts to high ones.
         */
        for (int i = 0; i < ints.length; i++) {

            /*
             * The 16 bits are zero & no need to fill with 0,
             * and it's not a UUID with value '0' (i != 0).
             */
            if (ints[i] == 0 && needZerosIndex < i && i != 0) {
                continue;
            }

            for (int j = 0; j < 8; j++) {
                buf[--charPos] = digits[ints[i] & mask];
                ints[i] >>>= shift;
            }
        }
        return new String(buf, charPos, (32 - charPos));
    }

    // JAVADOC COMMENT ELIDED
    public boolean equals(Object value) {
        return value instanceof UUID &&
                lowBits == ((UUID) value).lowBits &&
                highBits == ((UUID) value).highBits;
    }

    // JAVADOC COMMENT ELIDED
    public int hashCode() {
        return (int) (highBits ^ highBits >> 32 ^ lowBits ^ lowBits >> 32);
    }
} // end of class 'UUID' definition
