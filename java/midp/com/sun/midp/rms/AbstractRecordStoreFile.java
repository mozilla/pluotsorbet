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

package com.sun.midp.rms;

import java.io.IOException;

/**
 * A RecordStoreFile is a file abstraction layer between a
 * a RecordStore and an underlying persistent storage mechanism.
 * The underlying storage methods are provided by the
 * RandomAccessStream and File classes.
 *
 * RecordStoreFile confines the namespace of a record store to
 * the scope of the MIDlet suite of its creating application.
 * It also ensures unicode recordstore names are ascii filesystem safe.
 *
 * The RecordStoreImpl class can be implemented directly using the
 * RandomAccessStream and File classes.  However,
 * RecordStoreFile served as the java/native code boundary for
 * RMS in the MIDP 1.0 release.  It exists now for
 * backwards compatibility with older ports.
 */
interface AbstractRecordStoreFile {

    /** extension for RecordStore database files */
    static final int DB_EXTENSION = 0;

    /** extension for RecordStore database files */
    static final int IDX_EXTENSION = 1;

    /**
     * Approximation of remaining space in storage.
     *
     * Usage Warning:  This may be a slow operation if
     * the platform has to look at the size of each file
     * stored in the MIDP memory space and include its size
     * in the total.
     *
     * @param suiteId ID of the MIDlet suite that owns the record store
     *        can be null
     *
     * @return the approximate space available to grow the
     *         record store in bytes.
     */
    int spaceAvailable(int suiteId);

    /**
     * Sets the position within <code>recordStream</code> to
     * <code>pos</code>.  This will implicitly grow
     * the underlying stream if <code>pos</code> is made greater
     * than the current length of the storage stream.
     *
     * @param pos position within the file to move the current_pos
     *        pointer to.
     *
     * @exception IOException if there is a problem with the seek.
     */
    void seek(int pos) throws IOException;

    /**
     * Write all of <code>buf</code> to <code>recordStream</code>.
     *
     * @param buf buffer to read out of.
     *
     * @exception IOException if a write error occurs.
     */
    void write(byte[] buf) throws IOException;

    /**
     * Write <code>buf</code> to <code>recordStream</code>, starting
     * at <code>offset</code> and continuing for <code>numBytes</code>
     * bytes.
     *
     * @param buf buffer to read out of.
     * @param offset starting point write offset, from beginning of buffer.
     * @param numBytes the number of bytes to write.
     *
     * @exception IOException if a write error occurs.
     */
    void write(byte[] buf, int offset, int numBytes) throws IOException;

    /**
     * Commit pending writes
     *
     * @exception IOException if an error occurs while flushing
     *            <code>recordStream</code>.
     */
    void commitWrite() throws IOException;

    /**
     * Read up to <code>buf.length</code> into <code>buf</code>.
     *
     * @param buf buffer to read in to.
     *
     * @return the number of bytes read.
     *
     * @exception IOException if a read error occurs.
     */
    int read(byte[] buf) throws IOException;

    /**
     * Read up to <code>buf.length</code> into <code>buf</code>
     * starting at offset <code>offset</code> in <code>recordStream
     * </code> and continuing for up to <code>numBytes</code> bytes.
     *
     * @param buf buffer to read in to.
     * @param offset starting point read offset, from beginning of buffer.
     * @param numBytes the number of bytes to read.
     *
     * @return the number of bytes read.
     *
     * @exception IOException if a read error occurs.
     */
    int read(byte[] buf, int offset, int numBytes) throws IOException;

    /**
     * Disconnect from <code>recordStream</code> if it is
     * non null.  May be called more than once without error.
     *
     * @exception IOException if an error occurs closing
     *            <code>recordStream</code>.
     */
    void close() throws IOException;

    /**
     * Sets the length of this <code>RecordStoreFile</code>
     * <code>size</code> bytes.  If this file was previously
     * larger than <code>size</code> the extra data is lost.
     *
     * <code>size</code> must be <= the current length of
     * <code>recordStream</code>
     *
     * @param size new size for this file.
     *
     * @exception IOException if an error occurs, or if
     * <code>size</code> is less than zero.
     */
    void truncate(int size) throws IOException;
}
