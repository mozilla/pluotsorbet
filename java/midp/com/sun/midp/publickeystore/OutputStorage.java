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

package com.sun.midp.publickeystore;

import java.io.*;

/**
 * Write fields to an OutputStream.
 */
class OutputStorage extends Storage {
    /** stream to write to */
    private DataOutputStream out;

    /**
     * Constructs an OutputStorage for an OutputStream.
     * @param output the output storage output stream.
     * @exception IOException if the storage version cannot be written
     */
    OutputStorage(OutputStream output) throws IOException {
        out = new DataOutputStream(output);

        out.writeByte(CURRENT_VERSION);
    }

    /**
     * Stores a byte array field as tag, BINARY_TYPE, value.
     * @param tag number to unique to this field
     * @param value value of field
     */
    void writeValue(byte tag, byte[] value) throws IOException {
        out.writeByte(tag);
        out.writeByte(BINARY_TYPE);

        /*
         * must write our own length, because DataOutputStream does not handle
         * handle byte arrays.
         */
        out.writeShort(value.length);
        out.write(value);
    }

    /**
     * Stores a String field as tag, STRING_TYPE, value.
     * @param tag number to unique to this field
     * @param value value of field
     */
    void writeValue(byte tag, String value) throws IOException {
        out.writeByte(tag);
        out.writeByte(STRING_TYPE);
        out.writeUTF(value);
    }

    /**
     * Stores a long field as tag, LONG_TYPE, value.
     * @param tag number to unique to this field
     * @param value value of field
     */
    void writeValue(byte tag, long value) throws IOException {
        out.writeByte(tag);
        out.writeByte(LONG_TYPE);
        out.writeLong(value);
    }

    /**
     * Stores a boolean field as tag, BOOLEAN_TYPE, value.
     * @param tag number to unique to this field
     * @param value value of field
     */
    void writeValue(byte tag, boolean value) throws IOException {
        out.writeByte(tag);
        out.writeByte(BOOLEAN_TYPE);
        out.writeBoolean(value);
    }
}
