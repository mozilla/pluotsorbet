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

package com.sun.satsa.util;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;

/**
 * Used to represent each Type, Length, Value structure in a DER buffer.
 */
public class TLV {

    /*
     * This class is used to parse DER encoded data (BER indefinite
     * length is also supported) and assemble new DER data structures
     * from elements. It is not safe to modify elements of constructed
     * DER value that was created using parsing constructor or copy()
     * method.
     */

    /** ASN context specific flag used in types (0x80). */
    public static final int CONTEXT = 0x80;
    /** ASN constructed flag used in types (0x20). */
    public static final int CONSTRUCTED = 0x20;
    /** ASN constructed flag used in types (0x20). */
    public static final int EXPLICIT = CONSTRUCTED;
    /** ANY_STRING type used as a place holder. [UNIVERSAL 0] */
    public static final int ANY_STRING_TYPE = 0x00; // our own impl
    /** ASN BOOLEAN type used in certificate parsing. [UNIVERSAL 1] */
    public static final int BOOLEAN_TYPE    = 1;
    /** ASN INTEGER type used in certificate parsing. [UNIVERSAL 2] */
    public static final int INTEGER_TYPE    = 2;
    /** ASN BIT STRING type used in certificate parsing. [UNIVERSAL 3] */
    public static final int BITSTRING_TYPE  = 3;
    /** ASN OCTET STRING type used in certificate parsing. [UNIVERSAL 4] */
    public static final int OCTETSTR_TYPE   = 4;
    /** ASN NULL type used in certificate parsing. [UNIVERSAL 5] */
    public static final int NULL_TYPE       = 5;
    /** ASN OBJECT ID type used in certificate parsing. [UNIVERSAL 6] */
    public static final int OID_TYPE        = 6;
    /** ASN ENUMERATED type. [UNIVERSAL 10] */
    public static final int ENUMERATED_TYPE        = 10;
    /** ASN UTF8String type used in certificate parsing. [UNIVERSAL 12] */
    public static final int UTF8STR_TYPE    = 12;
    /**
     *  ASN SEQUENCE type used in certificate parsing.
     * [UNIVERSAL CONSTRUCTED 16]
     */
    public static final int SEQUENCE_TYPE   = CONSTRUCTED + 16;
    /**
     * ASN SET type used in certificate parsing.
     * [UNIVERSAL CONSTRUCTED 17]
     */
    public static final int SET_TYPE        = CONSTRUCTED + 17;
    /** ASN PrintableString type used in certificate parsing. [UNIVERSAL 19] */
    public static final int PRINTSTR_TYPE   = 19;
    /** ASN TELETEX STRING type used in certificate parsing. [UNIVERSAL 20] */
    public static final int TELETEXSTR_TYPE = 20;
    /** ASN IA5 STRING type used in certificate parsing. [UNIVERSAL 22] */
    public static final int IA5STR_TYPE     = 22;
    /** ASN UCT time type used in certificate parsing. [UNIVERSAL 23] */
    public static final int UCT_TIME_TYPE   = 23;
    /**
     * ASN Generalized time type used in certificate parsing.
     * [UNIVERSAL 24]
     */
    public static final int GEN_TIME_TYPE   = 24;
    /**
     * ASN UniversalString type used in certificate parsing.
     * [UNIVERSAL 28].
     */
    public static final int UNIVSTR_TYPE    = 28;
    /** ASN BIT STRING type used in certificate parsing. [UNIVERSAL 30] */
    public static final int BMPSTR_TYPE  = 30;
    /**
     * Context specific explicit type for certificate version.
     * [CONTEXT EXPLICIT 0]
     */
    public static final int VERSION_TYPE    = CONTEXT + EXPLICIT + 0;
    /**
     * Context specific explicit type for certificate extensions.
     * [CONTEXT EXPLICIT 3]
     */
    public static final int EXTENSIONS_TYPE = CONTEXT + EXPLICIT + 3;

    /** Raw DER type. */
    public int type;
    /** Number of bytes that make up the value. */
    public int length;
    /** Offset of the value. */
    public int valueOffset;
    /** Non-null for constructed types, the first child TLV. */
    public TLV child;
    /** The next TLV in the parent sequence. */
    public TLV next;
    /** Buffer that contains the DER encoded TLV. */
    public byte[] data;

    /**
     * Constructs a TLV structure, recursing down for constructed types.
     * @param buffer DER buffer
     * @param offset where to start parsing
     * @exception IndexOutOfBoundsException if the DER is corrupt
     * @throws TLVException in case of parsing error
     */
    public TLV(byte[] buffer, int offset) throws TLVException {

        try {
            data = buffer;
            type = buffer[offset++] & 0xff;

            if ((type & 0x1f) == 0x1f) {
                // multi byte type, 7 bits per byte,
                // only last byte bit 8 as zero
                throw new TLVException("Invalid tag");
            }

            int size = buffer[offset++] & 0xff;
            boolean indefinite = (size == 128);
            if (indefinite) {
                if ((type & 0x20) == 0) {
                    throw new TLVException("Invalid length");
                }
            } else
                if (size >= 128) {
                    int sizeLen = size - 128;

                    // NOTE: for now, all sizes must fit int 3 bytes
                    if (sizeLen > 3) {
                        throw new TLVException("TLV is too large");
                    }

                    size = 0;
                    while (sizeLen > 0) {
                        size = (size << 8) + (buffer[offset++] & 0xff);
                        sizeLen--;
                    }
                }

            length = size;
            valueOffset = offset;

            if ((type & 0x20) == 0 || length == 0) {
                return;
            }

            // constructed and not empty

            TLV prev = null;
            while (true) {
                if (indefinite && data[offset] == 0 &&
                        data[offset + 1] == 0) {
                    length = offset - valueOffset;
                    return;
                }

                TLV temp = new TLV(buffer, offset);
                offset = (data[offset + 1] == (byte) 0x80 ? 2 : 0) +
                        temp.valueOffset + temp.length;

                if (prev == null) {
                    child = temp;
                } else {
                    prev.next = temp;
                }
                prev = temp;

                if (indefinite) {
                    continue;
                }
                if (offset == valueOffset + length) {
                    break;
                }
                if (offset > valueOffset + length) {
                    throw new TLVException("incorrect structure");
                }
            }
        } catch (NullPointerException npe) {
            throw new TLVException("parser error");
        } catch (IndexOutOfBoundsException iobe) {
            throw new TLVException("parser error");
        } catch (NumberFormatException nfe) {
            throw new TLVException("parser error");
        }
    }

    /**
     * Constructs a TLV structure.
     * @param tag tag of new TLV
     */
    public TLV(int tag) {
        type = tag;
    }

    /**
     * Constructs a TLV structure.
     * @param tag tag of the new TLV
     * @param bytes value of the new TLV
     */
    public TLV(int tag, byte[] bytes) {
        type = tag;
        valueOffset = 0;
        length = bytes.length;
        data = bytes;
    }

    /**
     * Constructs a TLV structure.
     * @param tag tag of the new TLV
     * @param bytes data for new TLV
     * @param offset of data
     */
    public TLV(int tag, byte[] bytes, int offset) {
        type = tag;
        valueOffset = offset;
        length = bytes.length - offset;
        data = bytes;
    }

    /**
     * Creates UTCTime TLV structure for given date.
     * @param time date
     * @return TLV value representing this date
     */
    public static TLV createUTCTime(Calendar time) {
        byte[] data = new byte[13];
        putDigits(data, 0, time.get(Calendar.YEAR));
        putDigits(data, 2, time.get(Calendar.MONTH) + 1);
        putDigits(data, 4, time.get(Calendar.DAY_OF_MONTH));
        putDigits(data, 6, time.get(Calendar.HOUR_OF_DAY));
        putDigits(data, 8, time.get(Calendar.MINUTE));
        putDigits(data, 10, time.get(Calendar.SECOND));
        data[12] = 0x5a;
        return new TLV(UCT_TIME_TYPE, data);
    }

    /**
     * Creates TLV object of type sequence.
     * @return new object
     */
    public static TLV createSequence() {
        return new TLV(SEQUENCE_TYPE);
    }

    /**
     * Creates TLV object of type integer.
     * @param data value
     * @return new object
     */
    public static TLV createInteger(byte[] data) {
        return new TLV(INTEGER_TYPE, data);
    }

    /**
     * Creates TLV object of type octet string.
     * @param data value
     * @return new object
     */
    public static TLV createOctetString(byte[] data) {
        return new TLV(OCTETSTR_TYPE, data);
    }

    /**
     * Creates TLV object of type OID.
     * @param oid OID in text form
     * @return new object
     */
    public static TLV createOID(String oid) {
        return new TLV(TLV.OID_TYPE, Utils.StringToOID(oid));
    }

    /**
     * Creates TLV object of type UTF8 string.
     * @param s string value
     * @return new object
     */
    public static TLV createUTF8String(String s) {
        return new TLV(TLV.UTF8STR_TYPE, Utils.stringToBytes(s));
    }

    /**
     * Creates TLV object of type IA5 string.
     * @param s string value
     * @throws TLVException if illegal string has been provided
     * @return new object
     */
    public static TLV createIA5String(String s)  throws TLVException {
        int len = (s == null ? 0 : s.length());
        
        if (len == 0) {
            return new TLV(TLV.IA5STR_TYPE, new byte[] {});
        }
        byte[] b = new byte[len];
        
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 127) {
                b[i] = (byte)c;
            } else {
                throw new TLVException("Illegal string for IA5:" + s);
            }
        }
        return new TLV(TLV.IA5STR_TYPE, b);
    }

    /**
     * Creates TLV object of type integer.
     * @param value value
     * @return new object
     */
    public static TLV createInteger(long value) {

        int check = (value < 0) ? -1 : 0;

        int i = 1;
        while (i < 8) {
            if (value >> (i * 8) == check) {
                byte v = (byte) (value >> ((i - 1) * 8));
                if (value < 0 ? v > 0 : v < 0) {
                    i++;
                }
                break;
            }
            i++;
        }

        byte[] data = new byte[i];
        while (i > 0) {
            i--;
            data[i] = (byte) value;
            value = value >> 8;
        }
        return new TLV(TLV.INTEGER_TYPE, data);
    }

    /**
     * Creates a copy of this TLV. The value of field next of the new
     * TLV is null.
     * @return a copy of this TLV
     */
    public TLV copy() {
        try {
            return new TLV(getDERData(), 0);
        } catch (TLVException e) {}
        return null;
    }

    /**
     * Sets next element for this TLV object.
     * @param next the next object
     * @return the passed value to allow chaining
     */
    public TLV setNext(TLV next) {
        this.next = next;
        return next;
    }

    /**
     * Sets child element for this TLV object.
     * @param child the child object
     * @return the passed value to allow chaining
     */
    public TLV setChild(TLV child) {
        this.child = child;
        return child;
    }

    /**
     * Sets the (implicit) tag value for this object.
     * @param tag tag value
     * @return <code>this</code> value to allow call chaining
     */
    public TLV setTag(int tag) {
        this.type = tag;
        return this;
    }

    /**
     * Returns the value field of this TLV.
     * @return the value field of this TLV
     */
    public byte[] getValue() {

        if (data == null) {
            getDERSize();
        }
        byte[] x = new byte[length];
        getValue_(x, 0);
        return x;
    }

    /**
     * Returns DER encoded TLV.
     * @return DER encoded TLV
     */
    public byte[] getDERData() {

        byte[] x = new byte[getDERSize()];
        getDERData_(x, 0);
        return x;
    }

    /**
     * Returns DER encoded TLV.
     * @param buffer target buffer
     * @param offset offset in the buffer
     * @return value length
     */
    public int getDERData(byte[] buffer, int offset) {

        getDERSize();
        return getDERData_(buffer, offset);
    }

    /**
     * Returns the size of DER encoded TLV.
     * @return the size of DER encoded TLV
     */
    public int getDERSize() {

        if (data == null) {
            length = 0;
            TLV c = child;
            while (c != null) {
                length += c.getDERSize();
                c = c.next;
            }
        }
        return length + getTLSize();
    }

    /**
     * Returns integer value.
     * @return integer value
     * @throws TLVException if this TLV doesn't represent integer value
     */
    public int getInteger() throws TLVException {

        if (type != INTEGER_TYPE && ((type & 0xf0) != 0x80)) {
            throw new TLVException("invalid type - getInteger");
        }
        return getIntegerValue();
    }

    /**
     * Returns octet string value as integer.
     * @return integer value
     * @throws TLVException if this TLV is not octet string
     */
    public int getId() throws TLVException {

        if (type != OCTETSTR_TYPE) {
            throw new TLVException("invalid type - getId");
        }
        return getIntegerValue();
    }

    /**
     * Returns the value of enumerated type.
     * @return the value
     * @throws TLVException if TLV type is invalid
     */
    public int getEnumerated() throws TLVException {

        if (type != ENUMERATED_TYPE) {
            throw new TLVException("invalid type - getEnumerated");
        }
        return getIntegerValue();
    }

    /**
     * Returns the value of TLV as integer.
     * @return the integer value
     * @throws TLVException if the value is too long
     */
    private int getIntegerValue() throws TLVException {

        int l = data[valueOffset] < 0 ? -1 : 0;
        int check = l << 24;

        for (int i = 0; i < length; i++) {
            if ((l & 0xff000000) != check) {
                throw new TLVException("Integer value is too big");
            }
            l = (l << 8) | (data[valueOffset + i] & 0xff);
        }
        return l;
    }

    /**
     * Returns string represented by this UTF8 string.
     * @return string value
     * @throws TLVException if TLV type is invalid
     */
    public String getUTF8() throws TLVException {

        if (type != UTF8STR_TYPE && ((type & 0xf0) != 0x80)) {
            throw new TLVException("invalid type - getUTF8");
        }

        try {
            return new String(data, valueOffset, length, Utils.utf8);
        } catch (UnsupportedEncodingException e) {
            throw new TLVException("invalid encoding");
        }
    }

    /**
     * Returns true if this value represents string.
     * @return true if this value represents string
     */
    public boolean isString() {
        return (type == TELETEXSTR_TYPE ||
                type == PRINTSTR_TYPE ||
                type == UNIVSTR_TYPE ||
                type == UTF8STR_TYPE ||
                type == BMPSTR_TYPE);
    }

    /**
     * Returns time represented by this TLV.
     * @return time value
     * @throws TLVException if TLV type is invalid
     */
    public Calendar getTime() throws TLVException {

        if (type != GEN_TIME_TYPE &&
            type != UCT_TIME_TYPE) {
            throw new TLVException("invalid type - getType");
        }

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(new Date());

        int offset;
        int year;
        if (type == GEN_TIME_TYPE) {
            year = getTimeComponent(0, 4);
            offset = 4;
        } else {
            year = getTimeComponent(0, 2);
            year += (year >= 50) ? 1900 : 2000;
            offset = 2;
        }
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, getTimeComponent(offset, 2) - 1);
        offset += 2;
        c.set(Calendar.DAY_OF_MONTH, getTimeComponent(offset, 2));
        offset += 2;
        c.set(Calendar.HOUR_OF_DAY, getTimeComponent(offset, 2));
        offset += 2;
        c.set(Calendar.MINUTE, getTimeComponent(offset, 2));
        offset += 2;
        c.set(Calendar.SECOND, getTimeComponent(offset, 2));

        return c;
    }

    /**
     * Returns decoded BCD value.
     * @param offset value offset
     * @param len value length
     * @return decoded value
     */
    private int getTimeComponent(int offset, int len) {

        int value = 0;
        while (len-- > 0) {
            value = value * 10 + (data[valueOffset + offset++] - 0x30);
        }
        return value;
    }

    /**
     * Skips optional element of DER structure with given tag.
     * @param type tag of optional value
     * @return this object if type doesn't match or the next one if it
     * does
     */
    public TLV skipOptional(int type) {

        if (this.type == type) {
            return next;
        }
        return this;
    }

    /**
     * Returns the value of flag stored in bitsring value.
     * @param index flag index
     * @return true if the flag is set
     * @throws TLVException if TLV type is invalid
     */
    public boolean checkFlag(int index) throws TLVException {

        if (type != BITSTRING_TYPE) {
            throw new TLVException("invalid type - checkFlag");
        }

        int i = (length - 1) * 8  - data[valueOffset];
        if (index >= i) {
            return false;
        }

        return ((data[valueOffset + 1 + (index/8)] << index % 8) & 0x80)
                != 0;
    }

    /**
     * Compares the value of this TLV with given value.
     * @param data the value to be compared
     * @return true if TLV object contains the same value
     */
    public boolean valueEquals(byte[] data) {
        return Utils.byteMatch(this.data, valueOffset, length,
                               data, 0, data.length);
    }

    /**
     * Places two ASCII encoded decimal digits into byte array.
     * @param data byte aray
     * @param offset the index of the first byte
     * @param value the value to be placed into the buffer
     */
    private static void putDigits(byte[] data, int offset, int value) {

        value = value % 100;
        data[offset++] = (byte) (0x30 | (value / 10));
        data[offset++] = (byte) (0x30 | (value % 10));
    }

    /**
     * IMPL_NOTE delete
     * Print the a TLV structure, recursing down for constructed types.
     * /
    public void print() {
        print(System.out, 0);
    }

    /**
     * IMPL_NOTE delete
     * Print the a TLV structure, recursing down for constructed types.
     * @param out output stream
     * /
    public void print(PrintStream out) {
        print(out, 0);
    }

    /**
     * IMPL_NOTE delete
     * Prints the a TLV structure, recursing down for constructed types.
     * @param out output stream
     * @param level what level this TLV is at
     * /
    private void print(PrintStream out, int level) {

        for (int i = 0; i < level; i++) {
            out.print("    ");
        }

        byte[] buffer;

        if (data != null) {
            buffer = data;
        } else {
            buffer = getDERData();
        }

        if (child == null) {
            out.print("Type: 0x" + Integer.toHexString(type) +
                             " length: " + length + " value: ");
            if (type == PRINTSTR_TYPE ||
                type == TELETEXSTR_TYPE ||
                type == UTF8STR_TYPE ||
                type == IA5STR_TYPE ||
                type == UNIVSTR_TYPE) {
                try {
                    out.print(new String(buffer, valueOffset, length,
                              Utils.utf8));
                } catch (UnsupportedEncodingException e) {
                    // ignore
                }
            } else if (type == OID_TYPE) {
                out.print(Utils.OIDtoString(buffer, valueOffset, length));
            } else {
                out.print(Utils.hexNumber(buffer, valueOffset, length));
            }

            out.println("");
        } else {
            if (type == SET_TYPE) {
                out.print("Set:");
            } else {
                out.print("Sequence:");
            }

            out.println("  (0x" + Integer.toHexString(type) +
                             " " + length + ")");

            child.print(out, level + 1);
        }

        if (next != null) {
            next.print(out, level);
        }
    }
/*  */

    /**
     * Places the value field of this TLV into the buffer.
     * @param buffer target buffer
     * @param offset index of the first byte
     * @return value length
     */
    private int getValue_(byte[] buffer, int offset) {

        if (data == null) {
            TLV c = child;
            while (c != null) {
                offset += c.getDERData(buffer, offset);
                c = c.next;
            }
        } else {
            System.arraycopy(data, valueOffset, buffer, offset, length);
        }
        return length;
    }

    /**
     * Places tag and length values into the buffer.
     * @param x byte buffer
     * @param i offset
     * @return value offset in the buffer
     */
    private int putHeader(byte[] x, int i) {

        x[i++] = (byte) type;

        if (length < 128) {
            x[i++] = (byte) length;
        } else
        if (length < 256) {
            x[i++] = (byte) 0x81;
            x[i++] = (byte) length;
        } else {
            x[i++] = (byte) 0x82;
            x[i++] = (byte) (length >> 8);
            x[i++] = (byte) length;
        }
        return i;
    }

    /**
     * Returns DER encoded TLV.
     * @param buffer target buffer
     * @param offset offset in the buffer
     * @return value length
     */
    private int getDERData_(byte[] buffer, int offset) {

        int initialOffset = offset;
        offset = putHeader(buffer, offset);

        if (data == null) {
            TLV c = child;
            while (c != null) {
                offset += c.getDERData_(buffer, offset);
                c = c.next;
            }
        } else {
            System.arraycopy(data, valueOffset, buffer, offset, length);
            offset += length;
        }
        return (offset - initialOffset);
    }

    /**
     * Returns the size of tag and length encoding.
     * @return the size of tag and length encoding
     */
    private int getTLSize() {

        int TLSize = 2;
        if (length >= 128) {
            int i = length;
            while (i != 0) {
                TLSize++;
                i = i >> 8;
            }
        }
        return TLSize;
    }

    /**
     * Compares this object with other TLV object.
     * @param t TLV object
     * @return true if both objects have the same type and contain
     * the same data
     */
    public boolean match(TLV t) {

        if (type != t.type) {
            return false;
        }
        if (t.data == null) {
            t = t.copy();
        }
        if (data == null) {
            t.match(this);
        }
        return Utils.byteMatch(data, valueOffset, length,
                               t.data, t.valueOffset, t.length);
    }

}
