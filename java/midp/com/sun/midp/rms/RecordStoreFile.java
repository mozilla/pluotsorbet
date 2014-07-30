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
class RecordStoreFile implements AbstractRecordStoreFile {

    /** Handle to read/write record store data to */
    private int handle = -1;

    /**
     * Returns an array of the names of record stores owned by the
     * MIDlet suite. Note that if the MIDlet suite does not
     * have any record stores, this function will return NULL.
     *
     * @param suiteId ID of the MIDlet suite that owns the record store
     *
     * @return an array of record store names.
     */
    static String[] listRecordStores(int suiteId) {
        String filenameBase = RmsEnvironment.getSecureFilenameBase(suiteId);
        String[] array = new String[getNumberOfStores(filenameBase)];

        if (array.length > 0) {
            getRecordStoreList(filenameBase, array);
            return array;
        } else {
            return null;
        }
    }

    /**
     * Get the number of record stores for a MIDlet suite.
     *
     * @param filenameBase filename base of the suite
     *
     * @return the number of installed suites
     */
    private static native int getNumberOfStores(String filenameBase);

    /**
     * Retrieves the list of record stores a MIDlet suites owns.
     *
     * @param filenameBase filename base of the suite
     * @param names an empty array of suite IDs to fill, call
     *     getNumberOfSuites to know how big to make the array
     */
    private static native void getRecordStoreList(String filenameBase, 
                                                  String[] names);

    /**
     * Remove all the Record Stores for a suite.
     *
     * @param filenameBase filename base of the suite
     */
    static native void removeRecordStores0(String filenameBase);

    /** Pass-through method to allow conversion of suite id to filename base
     *
     * @param suiteId ID of the suite
     */
    static void removeRecordStores(int suiteId) {
        removeRecordStores0(RmsEnvironment.getSecureFilenameBase(suiteId));
    }
    /**
     * Approximation of remaining space in storage for a new record store.
     *
     * Usage Warning:  This may be a slow operation if
     * the platform has to look at the size of each file
     * stored in the MIDP memory space and include its size
     * in the total.
     *
     * @param filenameBase filename base of the MIDlet suite that owns the
     *                     record store
     *
     * @return the approximate space available to create a
     *         record store in bytes.
     */
    static native int spaceAvailableNewRecordStore0(String filenameBase,
                                                   int storageId);

    /** Pass through method to enable getting the storage id to pass into
      * native
      */
    static int spaceAvailableNewRecordStore(int suiteId) {

        return spaceAvailableNewRecordStore0(
            RmsEnvironment.getSecureFilenameBase(suiteId), 
            RmsEnvironment.getStorageAreaId(suiteId));
    }

    /**
     * Constructs a new RecordStoreFile instance.
     *
     * This process involves a few discrete steps and concludes with
     * with opening a RandomAccessStream that this RecordStoreFile
     * instance will use for persistant storage.
     *
     * The steps in constructing a RecordStoreFile instance are:
     * <ul>
     *  <li>The storage path for the desired MIDlet suite
     *  is acquired in argument <code>uidPath</code>.  The caller
     *  must get this path using the <code>getUniqueIdPath()</code>
     *  method before calling this constructor.
     *
     *  <li>This result is then connected with a new
     *  <code>RandomAccessStream</code> where record data for this
     *  instance is stored..
     * </ul>
     *
     * @param suiteId ID of the MIDlet suite that owns the record store
     * @param name name of the record store
     * @param extension the extension for the record store file
     *
     * @exception IOException if there is an error opening the file.
     */
    RecordStoreFile(int suiteId, String name,
                    int extension) throws IOException {
        handle = openRecordStoreFile(
                     RmsEnvironment.getSecureFilenameBase(suiteId), 
                     name, extension);
    }

    /**
     * Open a native record store file.
     *
     * NOTE: open() needs to be non-static so we can use the 'this'
     * pointer to register the cleanup routine. When we have full
     * native finalization, open() can be made 'static' again.
     *
     * @param filenameBase filename base of the MIDlet suite that owns the
     *                     record store
     * @param name name of the record store
     * @param extension the extension for the record store file
     *
     * @return handle to a record store file
     *
     * @exception IOException if there is an error opening the file.
     */
    private native int openRecordStoreFile(String filenameBase,
                                           String name,
                                           int extension) throws IOException;

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
    public int spaceAvailable(int suiteId) {
        return spaceAvailableRecordStore(handle, 
                   RmsEnvironment.getSecureFilenameBase(suiteId),
                   RmsEnvironment.getStorageAreaId(suiteId));
    }

    /**
     * Approximation of remaining space in storage for a record store.
     *
     * Usage Warning:  This may be a slow operation if
     * the platform has to look at the size of each file
     * stored in the MIDP memory space and include its size
     * in the total.
     *
     * @param handle to an open record store
     * @param filenameBase filename base of the MIDlet suite that owns the
     *                     record store
     *
     * @return the approximate space available to grow the
     *         record store in bytes.
     */
    private static native int spaceAvailableRecordStore(int handle,
                                                        String filenameBase,
                                                        int storageId);

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
    public void seek(int pos) throws IOException {
        setPosition(handle, pos);
    }

    /**
     * Sets the position within <code>recordStream</code> to
     * <code>pos</code>.  This will implicitly grow
     * the underlying stream if <code>pos</code> is made greater
     * than the current length of the storage stream.
     *
     * @param handle handle to a record store file
     * @param pos position within the file to move the current_pos
     *        pointer to.
     *
     * @exception IOException if there is a problem with the seek.
     */
    private static native void setPosition(int handle, int pos)
        throws IOException;

    /**
     * Write all of <code>buf</code> to <code>recordStream</code>.
     *
     * @param buf buffer to read out of.
     *
     * @exception IOException if a write error occurs.
     */
    public void write(byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

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
    public void write(byte[] buf, int offset, int numBytes)
            throws IOException {
        if (numBytes == 0) {
            return;
        }

        // Test before we goto the native code
        // This expression will cause a ArrayOutOfBoundsException if the values
        // passed for offset and numBytes is not valid and is much faster then
        // explicitly checking the values with if statements.
        int test = buf[offset] + buf[numBytes - 1] +
            buf[offset + numBytes - 1];

        writeBytes(handle, buf, offset, numBytes);
    }

    /**
     * Write <code>buf</code> to <code>recordStream</code>, starting
     * at <code>offset</code> and continuing for <code>numBytes</code>
     * bytes.
     *
     * @param handle handle to a record store file
     * @param buf buffer to read out of.
     * @param offset starting point write offset, from beginning of buffer.
     * @param numBytes the number of bytes to write.
     *
     * @exception IOException if a write error occurs.
     */
    private static native void writeBytes(int handle, byte[] buf, int offset,
                                   int numBytes) throws IOException;

    /**
     * Commit pending writes
     *
     * @exception IOException if an error occurs while flushing
     *            <code>recordStream</code>.
     */
    public void commitWrite() throws IOException {

        commitWrite(handle);
    }

    /**
     * Commit pending writes
     *
     * @param handle handle to a record store file
     *
     * @exception IOException if an error occurs while flushing
     *            <code>recordStream</code>.
     */
    private native static void commitWrite(int handle) throws IOException;

    /**
     * Read up to <code>buf.length</code> into <code>buf</code>.
     *
     * @param buf buffer to read in to.
     *
     * @return the number of bytes read.
     *
     * @exception IOException if a read error occurs.
     */
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

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
    public int read(byte[] buf, int offset, int numBytes) throws IOException {
        if (numBytes == 0) {
            return 0;
        }

        // Test before we goto the native code
        // This expression will cause a ArrayOutOfBoundsException if the values
        // passed for offset and numBytes is not valid and is much faster then
        // explicitly checking the values with if statements.
        int test = buf[offset] + buf[numBytes - 1] +
            buf[offset + numBytes - 1];

        return readBytes(handle, buf, offset, numBytes);
    }

    /**
     * Read up to <code>buf.length</code> into <code>buf</code>
     * starting at offset <code>offset</code> in <code>recordStream
     * </code> and continuing for up to <code>numBytes</code> bytes.
     *
     * @param handle handle to a record store file
     * @param buf buffer to read in to.
     * @param offset starting point read offset, from beginning of buffer.
     * @param numBytes the number of bytes to read.
     *
     * @return the number of bytes read.
     *
     * @exception IOException if a read error occurs.
     */
    private static native int readBytes(int handle, byte[] buf, int offset,
                                        int numBytes) throws IOException;

    /**
     * Disconnect from <code>recordStream</code> if it is
     * non null.  May be called more than once without error.
     *
     * @exception IOException if an error occurs closing
     *            <code>recordStream</code>.
     */
    public void close() throws IOException {
        int temp;

        if (handle == -1) {
            return;
        }

        temp = handle;
        handle = -1;
        closeFile(temp);
    }

    /**
     * Disconnect from <code>recordStream</code> if it is
     * non null.  May be called more than once without error.
     *
     * @param handle handle to a record store file
     *
     * @exception IOException if an error occurs closing
     *            <code>recordStream</code>.
     */
    private native static void closeFile(int handle) throws IOException;

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
    public void truncate(int size) throws IOException {
        truncateFile(handle, size);
    }

    /**
     * Sets the length of this <code>RecordStoreFile</code>
     * <code>size</code> bytes.  If this file was previously
     * larger than <code>size</code> the extra data is lost.
     *
     * <code>size</code> must be <= the current length of
     * <code>recordStream</code>
     *
     * @param handle handle to a record store file
     * @param size new size for this file.
     *
     * @exception IOException if an error occurs, or if
     * <code>size</code> is less than zero.
     */
    private static native void truncateFile(int handle,
                                            int size) throws IOException;

    /**
     * Ensures native resources are freed when Object is collected.
     */



    private native void finalize();

}
