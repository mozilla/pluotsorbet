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
import java.util.Vector;

/*
 * This class is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED 
public class DataElement {

    // JAVADOC COMMENT ELIDED 
    public static final int NULL = 0x0000;

    // JAVADOC COMMENT ELIDED 
    public static final int U_INT_1 = 0x0008;

    // JAVADOC COMMENT ELIDED 
    public static final int U_INT_2 = 0x0009;

    // JAVADOC COMMENT ELIDED 
    public static final int U_INT_4 = 0x000A;

    // JAVADOC COMMENT ELIDED 
    public static final int U_INT_8 = 0x000B;

    // JAVADOC COMMENT ELIDED 
    public static final int U_INT_16 = 0x000C;

    // JAVADOC COMMENT ELIDED 
    public static final int INT_1 = 0x0010;

    // JAVADOC COMMENT ELIDED 
    public static final int INT_2 = 0x0011;

    // JAVADOC COMMENT ELIDED 
    public static final int INT_4 = 0x0012;

    // JAVADOC COMMENT ELIDED 
    public static final int INT_8 = 0x0013;

    // JAVADOC COMMENT ELIDED 
    public static final int INT_16 = 0x0014;

    // JAVADOC COMMENT ELIDED 
    public static final int URL = 0x0040;

    // JAVADOC COMMENT ELIDED 
    public static final int UUID = 0x0018;

    // JAVADOC COMMENT ELIDED 
    public static final int BOOL = 0x0028;

    // JAVADOC COMMENT ELIDED 
    public static final int STRING = 0x0020;

    // JAVADOC COMMENT ELIDED 
    public static final int DATSEQ = 0x0030;

    // JAVADOC COMMENT ELIDED 
    public static final int DATALT = 0x0038;

    /* Keeps the specified or derived type of the element. */
    private int valueType;

    /* Keeps the boolean value for the type BOOL. */
    private boolean booleanValue;

    /* Keeps the long value for the types *INT*. */
    private long longValue;

    /*
     * Keeps the misc type value for the rest of types.
     *
     * This field also keeps the value for the type DATALT and DATSEQ.
     * In this case it's a Vector. The access to the Vector elements
     * is synchronized in cldc (according the source code). But,
     * this is not documented, so we make a synchronize access
     * to this field to fit any cldc implementation.
     */
    private Object miscValue;

    // JAVADOC COMMENT ELIDED 
    public DataElement(int valueType) {
        switch (valueType) {
        case NULL: /* miscValue = null in this case. */
            break;
        case DATALT: /* falls through */
        case DATSEQ:
            this.miscValue = new Vector();
            break;
        default:
            throw new IllegalArgumentException(
                    "Invalid valueType for this constructor: " + valueType);
        }
        this.valueType = valueType;
    }

    // JAVADOC COMMENT ELIDED 
    public DataElement(boolean bool) {
        valueType = BOOL;
        booleanValue = bool;
    }

    // JAVADOC COMMENT ELIDED 
    public DataElement(int valueType, long value) {
        long min = 0;
        long max = 0;

        switch (valueType) {
        case U_INT_1:
            max = 0xffL;
            break;
        case U_INT_2:
            max = 0xffffL;
            break;
        case U_INT_4:
            max = 0xffffffffL;
            break;
        case INT_1:
            min = Byte.MIN_VALUE;
            max = Byte.MAX_VALUE;
            break;
        case INT_2:
            min = -0x8000L;
            max = 0x7fffL;
            break;
        case INT_4:
            min = Integer.MIN_VALUE;
            max = Integer.MAX_VALUE;
            break;
        case INT_8:
            min = Long.MIN_VALUE;
            max = Long.MAX_VALUE;
            break;
        default:
            throw new IllegalArgumentException(
                    "Invalid 'valueType' for this constructor: " + valueType);
        }

        // check if value in the valid rangle for this type
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    "Invalid 'value' for specified type: " + value);
        }
        this.valueType = valueType;
        this.longValue = value;
    }

    // JAVADOC COMMENT ELIDED 
    public DataElement(int valueType, Object value) {
        boolean isCorrectValue = true;

        switch (valueType) {
        case URL: /* falls through */
        case STRING:
            isCorrectValue = value instanceof String;
            break;
        case UUID:
            isCorrectValue = value instanceof UUID;
            break;
        case INT_16: /* falls through */
        case U_INT_16:
            isCorrectValue = value instanceof byte[]
                    && ((byte[]) value).length == 16;
            break;
        case U_INT_8:
            isCorrectValue = value instanceof byte[]
                    && ((byte[]) value).length == 8;
            break;
        default:
            throw new IllegalArgumentException(
                    "Invalid 'valueType' for this constructor: " + valueType);
        }

        // check if value in the valid rangle for this type
        if (!isCorrectValue) {
            throw new IllegalArgumentException(
                    "Invalid 'value' for specified type: " + value);
        }
        this.valueType = valueType;
        this.miscValue = value;
    }

    // JAVADOC COMMENT ELIDED 
    public synchronized void addElement(DataElement elem) {

        /*
         * We can't optimize this by invoking the
         * this.insertElementAt(elem, getSize()), because
         * the ClassCastException may be thrown from getSize()
         * which gives us improper stack trace.
         */
        if (valueType != DATSEQ && valueType != DATALT) {
            throw new ClassCastException(
                    "Invalid element type for this method: " + valueType);
        }

        if (elem == null) {
            throw new NullPointerException("Specified element is null");
        }
        ((Vector) miscValue).addElement(elem);
    }

    // JAVADOC COMMENT ELIDED 
    public synchronized void insertElementAt(DataElement elem, int index) {
        if (valueType != DATSEQ && valueType != DATALT) {
            throw new ClassCastException(
                    "Invalid element type for this method: " + valueType);
        }

        if (elem == null) {
            throw new NullPointerException("Specified element is null");
        }

        /*
         * We can't use the Vector.insertElementAt check for out of
         * bounds, because Vector throws ArrayIndexOutOfBoundsException
         * in this case.
         */
        if (index < 0 || index > ((Vector) miscValue).size()) {
            throw new IndexOutOfBoundsException(
                    "Specified index is out of range");
        }
        ((Vector) miscValue).insertElementAt(elem, index);
    }

    // JAVADOC COMMENT ELIDED 
    public synchronized int getSize() {
        if (valueType != DATSEQ && valueType != DATALT) {
            throw new ClassCastException(
                    "Invalid element type for this method: " + valueType);
        }
        return ((Vector) miscValue).size();
    }

    // JAVADOC COMMENT ELIDED 
    public boolean removeElement(DataElement elem) {
        if (valueType != DATSEQ && valueType != DATALT) {
            throw new ClassCastException(
                    "Invalid element type for this method: " + valueType);
        }

        if (elem == null) {
            throw new NullPointerException("Specified element is null");
        }

        /*
         * The Bluetooth spec says the two DataElement equals if their
         * references are equal. According to cldc1.1 ref impl sources,
         * the Vector uses 'equals' call, and the Object.equls uses
         * a references compare, so we may not care about doing this here.
         */
        return ((Vector) miscValue).removeElement(elem);
    }

    // JAVADOC COMMENT ELIDED 
    public int getDataType() {
        return valueType;
    }

    // JAVADOC COMMENT ELIDED 
    public long getLong() {
        switch (valueType) {
        case U_INT_1: /* falls through */
        case U_INT_2: /* falls through */
        case U_INT_4: /* falls through */
        case INT_1: /* falls through */
        case INT_2: /* falls through */
        case INT_4: /* falls through */
        case INT_8:
            break;
        default:
            throw new ClassCastException(
                    "Invalid element type for this method: " + valueType);
        }
        return longValue;
    }

    // JAVADOC COMMENT ELIDED 
    public boolean getBoolean() {
        if (valueType != BOOL) {
            throw new ClassCastException(
                    "Invalid element type for this method: " + valueType);
        }
        return booleanValue;
    }

    // JAVADOC COMMENT ELIDED 
    public synchronized Object getValue() {
        Object retValue = miscValue;

        /*
         * According to cldc & bluetooth specifications, the String and UUID
         * are immutable, so we may not return a clone object to safe
         * the stored one.
         *
         * The Vector.elements() returns an Enumeration, which does not allow
         * to break the Vector either.
         *
         * The array may be modified by reference, so we have to return
         * a clone.
         */
        switch (valueType) {
        case URL: /* falls through */
        case STRING: /* falls through */
        case UUID:
            break;
        case DATALT: /* falls through */
        case DATSEQ:
            retValue = ((Vector) miscValue).elements();
            break;
        case U_INT_8: /* falls through */
        case U_INT_16: /* falls through */
        case INT_16:
            int length = ((byte[]) miscValue).length;
            retValue = new byte[length];
            System.arraycopy(miscValue, 0, retValue, 0, length);
            break;
        default:
            throw new ClassCastException(
                    "Invalid element type for this method: " + valueType);
        }
        return retValue;
    }
} // end of class 'DataElement' definition
