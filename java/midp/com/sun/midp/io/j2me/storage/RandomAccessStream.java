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

package com.sun.midp.io.j2me.storage;

import java.io.*;

import javax.microedition.io.Connector;
import javax.microedition.io.Connection;

import com.sun.j2me.security.AccessController;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import com.sun.midp.io.ConnectionBaseAdapter;
import com.sun.midp.io.Util;

/** A secure storage file stream that can be repositioned. */
public class RandomAccessStream extends ConnectionBaseAdapter {
    /** Signals the native code to open a truncated file for read-write. */
    public static final int READ_WRITE_TRUNCATE = -(Connector.READ_WRITE);

    /** Native file handle, set to -1 when not connected. */
    private int handle = -1;

    /** Constructs a RandomAccessStream. */
    public RandomAccessStream() {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);
    };

    /**
     * Constructs a RandomAccess Stream for callers that have a
     * different permissions than the currently running MIDlet suite.
     *
     * @param callerSecurityToken security token of the caller
     */
    public RandomAccessStream(SecurityToken callerSecurityToken) {
        callerSecurityToken.checkIfPermissionAllowed(Permissions.AMS);
    };

    /**
     * Initialize the StreamConnection and return it.
     *
     * @param name             URL for the connection,
     *                         without the protocol part
     * @param mode             I/O access mode, see {@link Connector}
     * @param timeouts         flag to indicate that the caller
     *                         wants timeout exceptions
     * @return                 this Connection object
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the connection cannot
     *                                        be found.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public Connection openPrim(String name, int mode,
                               boolean timeouts) throws IOException {
        /*
         * This is not class is not for public use but this method is required
         * by the super class.
         */
        throw new SecurityException();
    }

    /**
     * Called to connect a RandomAccessStream to a file in storage.
     *
     * @param name  contains storage filename
     * @param mode  {@link javax.microedition.io.Connector} READ, WRITE,
     *              or READ_WRITE
     * @exception IOException  if an I/O error occurs
     */
    public void connect(String name, int mode) throws IOException {
        if (handle != -1) {
            throw new
                IOException("Disconnect the stream before reconnecting.");
        }

        handle = open(name, mode);

        /* This object is re-used by internal methods */
        connectionOpen = true;

        if (mode == Connector.READ) {
            maxOStreams = 0;
        } else {
            maxOStreams = 1;
        }

        maxIStreams = 1;
    };

    /**
     * Disconnect the stream.
     * <p>
     * Any streams obtained with either <code>openInputStream</code> or
     * <code>openOutputStream</code> will throw an exception if used after
     * this call.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    public void disconnect() throws IOException {
        if (handle == -1) {
            return;
        }

        close(handle);
        handle = -1;
        connectionOpen = false;
    };

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, blocks until at least one byte is available.
     * <p>
     * Do not use this method if <code>openInputStream</code> has been called
     * since the input stream may be buffering data.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int readBytes(byte b[], int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }

        // Test before we goto the native code
        // This expression will cause a ArrayOutOfBoundsException if the values
        // passed for offset and numBytes is not valid and is much faster then
        // explicitly checking the values with if statements.
        int test = b[off] + b[len - 1] + b[off + len - 1];

        return read(handle, b, off, len);
    };

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, but does not block if no bytes available.
     * <p>
     * The <code>readBytesNonBlocking</code> method of
     * <code>ConnectionBaseAdapter</code> does nothing and returns 0.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    protected int readBytesNonBlocking(byte b[], int off, int len)
            throws IOException {
        // Read bytes should be non blocking
        return readBytes(b, off, len);
    };

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     * <p>
     * Do not use this method if <code>openOutputStream</code> has been called
     * since the output stream may be buffering data.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @return     number of bytes written
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    public int writeBytes(byte b[], int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }

        // Test before we goto the native code
        // This expression will cause a ArrayOutOfBoundsException if the values
        // passed for offset and numBytes is not valid and is much faster then
        // explicitly checking the values with if statements.
        int test = b[off] + b[len - 1] + b[off + len - 1];

        write(handle, b, off, len);
        return len;
    };

    /**
     * Write the given stream fully into this one.
     *
     * @param in stream write from
     * @return number of bytes written from the stream
     * @exception IOException  if an I/O error occurs
     */
    public int writeStream(InputStream in) throws IOException {
        byte[] temp = new byte[1024];
        int bytesRead;
        int totalBytesWritten = 0;

        for (;;) {
            bytesRead = in.read(temp);
            if (bytesRead == -1) {
                return totalBytesWritten;
            }

            writeBytes(temp, 0, bytesRead);
            totalBytesWritten += bytesRead;
        }
    }

    /**
     * Commit pending writes of this stream.
     *
     * @exception IOException  if an I/O error occurs
     */
    public void commitWrite() throws IOException {
        commitWrite(handle);
    }

    /**
     * Set the absolute position of this stream.
     * <p>
     * Do not use this method if either <code>openInputStream</code> or
     * <code>openOutputStream</code> has been called
     * since the streams may be buffering data.
     *
     * @param absolutePosition position from the byte 0 of this stream
     * @exception IOException  if an I/O error occurs
     */
    public void setPosition(int absolutePosition) throws IOException {
        position(handle, absolutePosition);
    }

    /**
     * Get the size of this stream.
     *
     * @return size of this stream in bytes
     * @exception IOException  if an I/O error occurs
     */
    public int getSizeOf() throws IOException {
        return sizeOf(handle);
    }

    /**
     * Truncate the size of the stream to <code>size</code> bytes.
     * This method cannot be used to make the size of the
     * underlying stream larger.
     *
     * @param size new size of this stream in bytes
     *
     * @exception IOException if an I/O error occurs or
     *            <code>size</code> is larger than the stream size.
     */
    public void truncate(int size) throws IOException {
        truncateStream(handle, size);
    }

    /*
     * NOTE: open() needs to be non-static so we can use the 'this'
     * pointer to register the cleanup routine. When we have full
     * native finalization, open() can be made 'static' again.
     */
    /**
     * Open a stream to a native file.
     *
     * @param filename  filename
     * @param mode        {@link javax.microedition.io.Connector} READ, WRITE,
     *                    or READ_WRITE
     *
     * @return handle to a native stream
     *
     * @exception IOException  if an I/O error occurs
     */
    private native int open(String filename, int mode) throws IOException;

    /**
     * Close a native stream.
     *
     * @param handle native stream handle
     *
     * @exception IOException  if an I/O error occurs
     */
    private static native void close(int handle) throws IOException;

    /**
     * Read at least one byte from a native stream.
     *
     * @param handle native stream handle
     * @param buffer where to put the bytes
     * @param offset where in the buffer to starting putting bytes
     * @param length how many bytes to read
     *
     * @return number of bytes read of -1 for the end of stream
     *
     * @exception IOException  if an I/O error occurs
     */
    private static native int read(int handle, byte[] buffer, int offset,
                                   int length) throws IOException;

    /**
     * Write bytes to a native stream.
     *
     * @param handle native stream handle
     * @param buffer what bytes write
     * @param offset where in the buffer the bytes start
     * @param length how many bytes to write
     *
     * @exception IOException  if an I/O error occurs
     */
    private static native void write(int handle, byte[] buffer, int offset,
                                     int length) throws IOException;

    /**
     * Commits or flushes pending writes.
     *
     * @param handle native stream handle
     *
     * @exception IOException  if an I/O error occurs
     */
    private static native void commitWrite(int handle) throws IOException;

    /**
     * Set the current position of a native stream.
     *
     * @param handle  native stream handle
     * @param absolutePosition desired position from the beginning of the
     *   stream.
     *
     * @exception IOException  if an I/O error occurs
     */
    private static native void position(int handle, int absolutePosition)
        throws IOException;

    /**
     * Get the total size of a native stream.
     *
     * @param handle  native stream handle
     *
     * @return size of the stream in bytes
     *
     * @exception IOException  if an I/O error occurs
     */
    private static native int sizeOf(int handle) throws IOException;

    /**
     * Set the size of a native stream.
     *
     * @param handle native stream handle
     * @param size size to truncate the native stream to.
     *
     * @exception IOException if an I/O error occurs
     */
    private static native void truncateStream(int handle, int size)
        throws IOException;

    /**
     * Ensures native resources are freed when Object is collected.
     */



    private native void finalize();

}
