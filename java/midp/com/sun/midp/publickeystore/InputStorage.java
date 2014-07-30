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
 * Retrieves stored fields from an InputStream.
 * Since Java Microedition has no serialization, this is a simple substitute.
 */
class InputStorage extends Storage {
    /** stream to read from */
    private DataInputStream in;

    /**
     * Constructs an InputStorage for an InputStream.
     * @param input the input storage input stream.
     * @exception IOException if the storage version cannot be read
     */
    InputStorage(InputStream input) throws IOException {
        in = new DataInputStream(input);

        // skip past the version number.
        in.readByte();
    }

    /**
     * Reads a field that was stored as tag, type, value set.
     * @param tag byte array of one byte to hold the tag of the field that
     *            was read
     * @return value of field that was stored, or null if there are no more
     *         fields
     * @exception IOException if the input storage was corrupted
     */
    Object readValue(byte[] tag) throws IOException {
        byte type;

        try {
            try {
                in.readFully(tag, 0, 1);
            } catch (EOFException eofe) {
                // this just means there are no more fields in storage
                return null;
            }

            type = in.readByte();
            if (type == BINARY_TYPE) {
                int len;
                byte[] value;

                /*
                 * must read the length first, because DataOutputStream does
                 * not handle handle byte arrays.
                 */
                len = in.readUnsignedShort();
                if (len < 0) {
                    throw new IOException();
                }

                value = new byte[len];
                in.readFully(value);
                return value;
            }

            if (type == STRING_TYPE) {
                return in.readUTF();
            }

            if (type == LONG_TYPE) {
                return new Long(in.readLong());
            }

            if (type == BOOLEAN_TYPE) {
                return new Boolean(in.readBoolean());
            }

            throw new IOException();
        } catch (IOException e) {
            throw new IOException("input storage corrupted");
        }
    }
}
