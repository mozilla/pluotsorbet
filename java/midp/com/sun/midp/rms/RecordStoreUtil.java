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
 *
 * Copyright 2000 Motorola, Inc. All Rights Reserved.
 * This notice does not imply publication.
 */

package com.sun.midp.rms;
import javax.microedition.rms.RecordStoreException;

/**
 * A class implementing record store utility functions.
 */

public class RecordStoreUtil {
    /**
     * A convenience method for converting a byte array into
     * an int (assumes big-endian byte ordering).
     *
     * @param data the byte array returned from the database.
     * @param offset the offset into the array of the first byte to start from.
     *
     * @return an int corresponding to the first four bytes
     *         of the array passed in.
     */
    static int getInt(byte[] data, int offset) {
        int r = data[offset++];
        r = (r << 8) | ((int)(data[offset++]) & 0xff);
        r = (r << 8) | ((int)(data[offset++]) & 0xff);
        r = (r << 8) | ((int)(data[offset++]) & 0xff);
        return r;
    }

    /**
     * A convenience method for converting a byte array into
     * a long (assumes big-endian byte ordering).
     *
     * @param data the byte array returned from the database.
     * @param offset the offset into the array of the first byte to start from.
     * @return a long corresponding to the first eight bytes
     *         of the array passed in.
     */
    static long getLong(byte[] data, int offset) {
        long r = data[offset++];
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        return r;
    }


    /**
     * A convenience method for converting an integer into
     * a byte array.
     *
     * @param i the integer to turn into a byte array.
     * @param data a place to store the bytes of <code>i</code>.
     * @param offset starting point within <code>data<code> to
     *        store <code>i</code>.
     *
     * @return the number of bytes written to the array.
     */
    static int putInt(int i, byte[] data, int offset) {
        data[offset++] = (byte)((i >> 24) & 0xff);
        data[offset++] = (byte)((i >> 16) & 0xff);
        data[offset++] = (byte)((i >> 8) & 0xff);
        data[offset] = (byte)(i & 0xff);
        return 4;
    }


    /**
     * A convenience method for converting a long into
     * a byte array.
     *
     * @param l the <code>long<code> to turn into a byte array.
     * @param data a place to store the bytes of <code>l</code>.
     * @param offset Starting point within <code>data</code> to
     *        store <code>l</code>.
     *
     * @return the number of bytes written to the array.
     */
    static int putLong(long l, byte[] data, int offset) {
        data[offset++] = (byte)((l >> 56) & 0xff);
        data[offset++] = (byte)((l >> 48) & 0xff);
        data[offset++] = (byte)((l >> 40) & 0xff);
        data[offset++] = (byte)((l >> 32) & 0xff);
        data[offset++] = (byte)((l >> 24) & 0xff);
        data[offset++] = (byte)((l >> 16) & 0xff);
        data[offset++] = (byte)((l >> 8) & 0xff);
        data[offset] = (byte)(l & 0xff);
        return 8;
    }

    /**
     * A convenience method for calculating the block size given the data size.
     *
     * @param dataSize the size of the data in the block.
     *
     * @return an int corresponding to the size of the block padded to
     *         a multiple of the BLOCK HEADER SIZE.
     */
    static int calculateBlockSize(int dataSize) {
        final int block_header_size = AbstractRecordStoreImpl.BLOCK_HEADER_SIZE;
        int remainder = dataSize % block_header_size; // & 0x07;

        if (remainder == 0) {
            return dataSize + block_header_size;
        } else {
            return dataSize + (block_header_size - remainder) +
                block_header_size;
        }
    }

    /**
     * Looks to see if the storage file for record store
     * identified by <code>uidPath</code> exists
     *
     * @param filenameBase base of the MIDlet suite that owns the record store
     * @param name name of the record store
     * @param extension the extension for the record store file
     *
     * @return true if the file exists, false if it does not.
     */
    native static boolean exists(String filenameBase, String name, int extension);

    /**
     * Removes the storage file for record store <code>filename</code>
     * if it exists.
     *
     * @param filenameBase base of the MIDlet suite that owns the record store
     * @param name name of the record store
     * @param extension the extension for the record store file
     *
     * @throws RecordStoreException if deletion encountered an error
     *         internally.
     */
    native static void deleteFile(String filenameBase, String name, int extension)
            throws RecordStoreException;

    /**
     * Removes record store file without throwing an exception on failure.
     *
     * @param filenameBase base of the MIDlet suite that owns the record store
     * @param name name of the record store
     * @param extension the extension for the record store file
     *
     * @return <code>true</code> if file was found and deleted successfully,
     *         <code>false</code> otherwise.
     */
    static boolean quietDeleteFile(String filenameBase, String name, int extension) {
        try {
            deleteFile(filenameBase, name, extension);
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

}
