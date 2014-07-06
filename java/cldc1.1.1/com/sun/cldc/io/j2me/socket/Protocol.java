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

package com.sun.cldc.io.j2me.socket;

import java.io.*;
import javax.microedition.io.*;
import com.sun.cldc.io.*;

/**
 * Connection to the J2ME socket API.
 *
 * @version 1.0 1/16/2000
 */

public class Protocol implements ConnectionBaseInterface, StreamConnection {

    /** Socket object used by native code */
    int handle;

    /** Access mode */
    private int mode;

    /** Open count */
    int opens = 0;

    /** Connection open flag */
    private boolean copen = false;

    /** Input stream open flag */
    protected boolean isopen = false;

    /** Output stream open flag */
    protected boolean osopen = false;

    /**
     * Open the connection
     */
    public void open(String name, int mode, boolean timeouts)
        throws IOException
    {
        throw new RuntimeException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                   "Should not be called"
/* #endif */
        );
    }

    /**
     * Open the connection
     * @param name the target for the connection. It must be in this
     *        format: "//<name or IP number>:<port number>"
     * @param mode read/write mode of the connection (currently ignored).
     * @param timeouts A flag to indicate that the called wants timeout
     *        exceptions (currently ignored).
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
            throws IOException {
        if (!name.startsWith("//")) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "bad socket connection name: " + name
/* #endif */
            );
        }
        int i = name.indexOf(':');
        if (i < 0) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "bad socket connection name: port missing"
/* #endif */
            );
        }
        String hostname = name.substring(2, i);
        int port;
        try {
            port = Integer.parseInt(name.substring(i+1));
        } catch (NumberFormatException e) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "bad socket connection name: bad port"
/* #endif */
            );
        }
        // cstring is always NUL terminated (note the extra byte allocated).
        // This avoids awkward char array manipulation in C code.
        byte cstring[] = new byte[hostname.length() + 1];
        for (int n=0; n<hostname.length(); n++) {
            cstring[n] = (byte)(hostname.charAt(n));
        }
        if ((this.handle = open0(cstring, port, mode)) < 0) {
            int errorCode = this.handle & 0x7fffffff;
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "connection failed: error = " + errorCode
/* #endif */
            );
        }
        opens++;
        copen = true;
        this.mode = mode;
        return this;
     }

    /**
     * Open the connection
     * @param handle an already formed socket handle
     * <p>
     * This function is only used by com.sun.cldc.io.j2me.socketserver;
     */
    public void open(int handle, int mode) throws IOException {
        this.handle = handle;
        opens++;
        copen = true;
        this.mode = mode;
    }

    /**
     * Ensure connection is open
     */
    void ensureOpen() throws IOException {
        if (!copen) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Connection closed"
/* #endif */
            );
        }
    }

    /**
     * Returns an input stream for this socket.
     *
     * @return     an input stream for reading bytes from this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    synchronized public InputStream openInputStream() throws IOException {
        ensureOpen();
        if ((mode&Connector.READ) == 0) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Connection not open for reading"
/* #endif */
            );
        }
        if (isopen) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Input stream already opened"
/* #endif */
            );
        }
        isopen = true;
        InputStream in = new PrivateInputStream(this);
        opens++;
        return in;
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing bytes to this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    synchronized public OutputStream openOutputStream() throws IOException {
        ensureOpen();
        if ((mode&Connector.WRITE) == 0) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Connection not open for writing"
/* #endif */
            );
        }
        if (osopen) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Output stream already opened"
/* #endif */
            );
        }
        osopen = true;
        OutputStream os = new PrivateOutputStream(this);
        opens++;
        return os;
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    synchronized public void close() throws IOException {
        if (copen) {
            copen = false;
            realClose();
        }
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    synchronized void realClose() throws IOException {
        if (--opens == 0) {
             close0(this.handle);
        }
    }

    /**
     * Open and return a data input stream for a connection.
     *
     * @return                 An input stream
     * @exception IOException  If an I/O error occurs
     */
    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    /**
     * Open and return a data output stream for a connection.
     *
     * @return                 An input stream
     * @exception IOException  If an I/O error occurs
     */
    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

   /*
    * A note about readByte()
    *
    * This function will return an unsigned byte, or -1.
    * -1 means that EOF was reached.
    */

    protected static native int open0(byte hostname[], int port, int mode);
    protected static native int readBuf(int handle, byte b[], int off,
                                         int len);
    protected static native int readByte(int handle);
    protected static native int writeBuf(int handle, byte b[], int off,
                                          int len);
    protected static native int writeByte(int handle, int b);
    protected static native int available0(int handle);
    protected static native void close0(int handle);
}

/**
 * Input stream for the connection
 */
class PrivateInputStream extends InputStream {

    /**
     * Pointer to the connection
     */
    private Protocol parent;

    /**
     * End of file flag
     */
    boolean eof = false;

    /**
     * Constructor
     * @param pointer to the connection object
     *
     * @exception  IOException  if an I/O error occurs.
     */
    /* public */ PrivateInputStream(Protocol parent) throws IOException {
        this.parent = parent;
    }

    /**
     * Check the stream is open
     *
     * @exception  IOException  if it is not.
     */
    void ensureOpen() throws IOException {
        if (parent == null) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Stream closed"
/* #endif */
            );
        }
    }

    /**
     * Reads the next byte of data from the input stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    synchronized public int read() throws IOException {
        int res;
        ensureOpen();
        if (eof) {
            return -1;
        }
        res = Protocol.readByte(parent.handle);
        if (res == -1) {
            eof = true;
        }
        if (parent == null) {
            throw new InterruptedIOException();
        }
        return res;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
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
    synchronized public int read(byte b[], int off, int len)
            throws IOException {
        ensureOpen();
        if (eof) {
            return -1;
        }
        if (len == 0) {
            return 0;
        }
        // Check for array index out of bounds, and NullPointerException,
        // so that the native code doesn't need to do it
        int test = b[off] + b[off + len - 1];

        int n = 0;
        while (n < len) {
            int count = Protocol.readBuf(parent.handle, b, off + n, len - n);
            if (count == -1) {
                eof = true;
                if (n == 0) {
                    n = -1;
                }
                break;
            }
            n += count;
            if (n == len) {
                break;
            }
        }
        if (parent == null) {
            throw new InterruptedIOException();
        }
        return n;
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.
     *
     * @return     the number of bytes that can be read from this input stream.
     * @exception  IOException  if an I/O error occurs.
     */
    synchronized public int available() throws IOException {
        ensureOpen();
        return Protocol.available0(parent.handle);
    }

    /**
     * Close the stream.
     *
     * @exception  IOException  if an I/O error occurs
     */
    public void close() throws IOException {
        if (parent != null) {
            ensureOpen();
            parent.realClose();
            parent.isopen = false;
            parent = null;
        }
    }
}

/**
 * Output stream for the connection
 */
class PrivateOutputStream extends OutputStream {

    /**
     * Pointer to the connection
     */
    private Protocol parent;

    /**
     * Constructor
     * @param pointer to the connection object
     *
     * @exception  IOException  if an I/O error occurs.
     */
    /* public */ PrivateOutputStream(Protocol parent) throws IOException {
        this.parent = parent;
    }

    /**
     * Check the stream is open
     *
     * @exception  IOException  if it is not.
     */
    void ensureOpen() throws IOException {
        if (parent == null) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Stream closed"
/* #endif */
            );
        }
    }

    /**
     * Writes the specified byte to this output stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     */
    synchronized public void write(int b) throws IOException {
        ensureOpen();
        while (true) {
            int res = Protocol.writeByte(parent.handle, b);
            if (res != 0) {
                // IMPL_NOTE: should EOFException be thrown if write fails?
                return;
            }
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    synchronized public void write(byte b[], int off, int len)
            throws IOException {
        ensureOpen();
        if (len == 0) {
            return;
        }

        // Check for array index out of bounds, and NullPointerException,
        // so that the native code doesn't need to do it
        int test = b[off] + b[off + len - 1];

        int n = 0;
        while (true) {
            n += Protocol.writeBuf(parent.handle, b, off + n, len - n);
            if (n == len) {
                break;
            }
        }
    }

    /**
     * Close the stream.
     *
     * @exception  IOException  if an I/O error occurs
     */
    public void close() throws IOException {
        if (parent != null) {
            ensureOpen();
            parent.realClose();
            parent.osopen = false;
            parent = null;
        }
    }

}
