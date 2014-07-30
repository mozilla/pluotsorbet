/*
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

package com.sun.midp.io;




import com.sun.cldc.io.ConnectionBaseInterface;


import com.sun.midp.midlet.*;

import java.io.*;

import javax.microedition.io.*;

/**
 * Protocol classes extend this class to gain some of the common functionality
 * needed to implement a CLDC Generic Connection.
 * <p>
 * The common functionality includes:</p>
 * <ul>
 * <li>Supplies the input and output stream classes for a StreamConnection</li>
 * <li>Limits the number of streams opened according to mode, but the limit
 * can be overridden. Read-write allows 1 input and 1 output, write-only
 * allows 1 output, read-only allows 1 input</li>
 * <li>Only "disconnects" when the connection and all streams are closed</li>
 * <li>Throws I/O exceptions when used after being closed</li>
 * <li>Provides a more efficient implementation of
 * {@link InputStream#read(byte[], int, int)}, which is called by
 * {@link InputStream#read()}
 * <li>Provides a more efficient implementation of
 * {@link OutputStream#write(byte[], int, int)}, which is called by
 * {@link OutputStream#write(int)}
 * </ul>
 * <p align="center">
 * <b>Class Relationship Diagram</b></p>
 * <p align="center">
 * <img src="doc-files/ConnectionBaseAdapter.gif" border=0></p>
 *
 * @version 3.0 9/1/2000
 */
public abstract class ConnectionBaseAdapter implements ConnectionBaseInterface,
    StreamConnection {

    /** Flag indicating if the connection is open. */
    protected boolean connectionOpen = false;
    /** Number of input streams that were opened. */
    protected int iStreams = 0;
    /**
     * Maximum number of open input streams. Set this
     * to zero to prevent openInputStream from giving out a stream in
     * write-only mode.
     */
    protected int maxIStreams = 1;
    /** Number of output streams were opened. */
    protected int oStreams = 0;
    /**
     * Maximum number of output streams. Set this
     * to zero to prevent openOutputStream from giving out a stream in
     * read-only mode.
     */
    protected int maxOStreams = 1;

    /**
     * Initialize the StreamConnection and return it.
     *
     * @param name             URL for the connection, without the
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
    public abstract Connection openPrim(String name, int mode,
                                        boolean timeouts) throws IOException;

    /**
     * Check the mode argument and initialize the StreamConnection.
     * Any permissions checks should be
     * checked before this method is called because the TCK expects the
     * security check to fail before the arguments are called even though
     * the spec does not mandate it.
     *
     * @param mode             I/O access mode, see {@link Connector}
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public void initStreamConnection(int mode) throws IOException {
        switch (mode) {
        case Connector.READ:
        case Connector.WRITE:
        case Connector.READ_WRITE:
            break;

        default:
            throw new IllegalArgumentException("Illegal mode");
        }

        connectionOpen = true;
    }

    /**
     * Returns an input stream.
     *
     * @return     an input stream for writing bytes to this port.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    public InputStream openInputStream() throws IOException {
        InputStream i;

        ensureOpen();

        if (maxIStreams == 0) {
            throw new IOException("no more input streams available");
        }

        i = new BufferedInputStream(new BaseInputStream(this));
        maxIStreams--;
        iStreams++;
        return i;
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
     * Returns an output stream.
     *
     * @return     an output stream for writing bytes to this port.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    public OutputStream openOutputStream() throws IOException {
        OutputStream o;

        ensureOpen();

        if (maxOStreams == 0) {
            throw new IOException("no more output streams available");
        }

        o = new BaseOutputStream(this);
        maxOStreams--;
        oStreams++;
        return o;
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

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    public void close() throws IOException {
        if (connectionOpen) {
            connectionOpen = false;
            closeCommon();
        }
    }

    /**
     * Called once by each child input stream.
     * If the input stream is marked open, it will be marked closed and
     * the if the connection and output stream are closed the disconnect
     * method will be called.
     *
     * @exception IOException if the subclass throws one
     */
    protected void closeInputStream() throws IOException {
        iStreams--;
        notifyClosedInput();
        closeCommon();
    }

    /**
     * Called once by each child output stream.
     * If the output stream is marked open, it will be marked closed and
     * the if the connection and input stream are closed the disconnect
     * method will be called.
     *
     * @exception IOException if the subclass throws one
     */
    protected void closeOutputStream() throws IOException {
        oStreams--;
        notifyClosedOutput();
        closeCommon();
    }

    /**
     * Disconnect if the connection and all the streams are closed.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    void closeCommon() throws IOException {
        if (!connectionOpen && iStreams == 0 && oStreams == 0) {
            disconnect();
        }
    }

    /**
     * Notify blocked Java threads waiting for an input data
     * that all InputStream instances of the connection are closed
     */
    protected void notifyClosedInput() {};

    /**
     * Notify blocked Java threads trying to output data
     * that all OutputStream instances of the connection are closed 
     */
    protected void notifyClosedOutput() {};

    /**
     * Check if the connection is open.
     *
     * @exception  IOException  is thrown, if the stream is not open.
     */
    protected void ensureOpen() throws IOException {
        if (!connectionOpen) {
            throw new IOException("Connection closed");
        }
    }

    /**
     * Free up the connection resources.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    protected abstract void disconnect() throws IOException;

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, blocks until at least one byte is available.
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
    protected abstract int readBytes(byte b[], int off, int len)
        throws IOException;

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.  The next caller might be the same thread or
     * another thread. This classes implementation always returns
     * <code>0</code>. It is up to subclasses to override this method.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {
        return 0;
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
     * @return     number of bytes written
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    protected abstract int writeBytes(byte b[], int off, int len)
        throws IOException;

    /**
     * Forces any buffered output bytes to be written out.
     * The general contract of <code>flush</code> is
     * that calling it is an indication that, if any bytes previously
     * written that have been buffered by the connection,
     * should immediately be written to their intended destination.
     * <p>
     * The <code>flush</code> method of <code>ConnectionBaseAdapter</code>
     * does nothing.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    protected void flush() throws IOException {
    }

    /**
     * Tests if input stream for a connection supports the <code>mark</code> and
     * <code>reset</code> methods.
     *
     * <p> The <code>markSupported</code> method of
     * <code>ConnectionBaseAdapter</code> returns <code>false</code>.
     *
     * <p> Subclasses should override this method if they support own mark/reset
     * functionality.
     *
     * @return  <code>true</code> if input stream for this connection supports
     *           the <code>mark</code> and <code>reset</code> methods;
     *           <code>false</code> otherwise.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * Marks the current position in input stream for a connection.
     * A subsequent call to the <code>reset</code> method repositions this
     * stream at the last marked position so that subsequent reads re-read
     * the same bytes.
     *
     * <p> The <code>mark</code> method of <code>ConnectionBaseAdapter</code>
     *  does nothing.
     *
     * <p> Subclasses should override this method if they support own mark/reset
     * functionality.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     java.io.InputStream#reset()
     */
    public synchronized void mark(int readlimit) {}

    /**
     * Repositions input stream for a connection to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     *
     * <p> The method <code>reset</code> for <code>ConnectionBaseAdapter</code>
     * class does nothing and always throws an <code>IOException</code>.
     *
     * <p> Subclasses should override this method if they support own mark/reset
     * functionality.
     *
     * @exception  IOException  if this stream has not been marked or if the
     *                          mark has been invalidated.
     * @see     java.io.InputStream#reset()
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     */
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}

/**
 * Input stream for the connection
 */
class BaseInputStream extends InputStream {

    /** Pointer to the connection. */
    private ConnectionBaseAdapter parent;

    /** Buffer for single char reads. */
    byte[] buf = new byte[1];

    /**
      * Buffer for mark/reset funtionality support.
      * <code>null</code> value indicates <code>mark</code> was not called or
      * <code>readlimit</code> value of the last <code>mark</code> was exceeded.
      */
    byte[] markBuf = null;

    /** The size of data stored in <code>markBuf</code>. */
    int markSize = 0;

    /** Current position in <code>markBuf</code> to read data from. */
    int markPos = 0;

    /**
     * Indicates whether <code>reset</code> method was called.
     * If so, data is read from <code>markBuf</code> buffer
     * otherwise via <code>parent.readBytes</code> method.
     */
    boolean isReadFromBuffer = false;

    /**
     * Constructs a BaseInputStream for a ConnectionBaseAdapter.
     *
     * @param parent pointer to the connection object
     *
     * @exception  IOException  if an I/O error occurs.
     */
    BaseInputStream(ConnectionBaseAdapter parent) throws IOException {
        this.parent = parent;
    }

    /**
     * Check the stream is open
     *
     * @exception  InterruptedIOException  if it is not.
     */
    private void ensureOpen() throws InterruptedIOException {
        if (parent == null) {
            throw new InterruptedIOException("Stream closed");
        }
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.  The next caller might be the same thread or
     * another thread.
     *
     * <p>The <code>available</code> method always returns <code>0</code> if
     * {@link ConnectionBaseAdapter#available()} is
     * not overridden by the subclass.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {

        ensureOpen();

        return parent.available();
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read() throws IOException {
        if (read(buf, 0, 1) > 0) {
            return (buf[0] & 0xFF);
        }

        return -1;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * <code>len</code> bytes, but a smaller number may be read, possibly
     * zero. The number of bytes actually read is returned as an integer.
     *
     * <p> This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * <p> If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     *
     * <p> If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is
     * thrown.
     *
     * <p> If <code>len</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>b</code>.
     *
     * <p> The first byte read is stored into element <code>b[off]</code>, the
     * next one into <code>b[off+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
     * <code>b[off+len-1]</code> unaffected.
     *
     * <p> In every case, elements <code>b[0]</code> through
     * <code>b[off]</code> and elements <code>b[off+len]</code> through
     * <code>b[b.length-1]</code> are unaffected.
     *
     * <p> If the first byte cannot be read for any reason other than end of
     * file, then an <code>IOException</code> is thrown. In particular, an
     * <code>IOException</code> is thrown if the input stream has been closed.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.InputStream#read()
     */
    public int read(byte b[], int off, int len) throws IOException {
        int test;

        ensureOpen();

        if (len == 0) {
            return 0;
        }

        /*
         * test the parameters so the subclass will not have to.
         * this will avoid crashes in the native code
         */
        test = b[off] + b[len - 1] + b[off + len - 1];

        // use parent's mark/reset functionality
        // if the parent supports the own one
        if (parent.markSupported()) {
            return parent.readBytes(b, off, len);
        }

        // read data from mark buffer if reset method was called
        if (isReadFromBuffer) {
            int dataSize = markSize - markPos;
            if (dataSize > 0) {
                int copySize = (dataSize > len) ? len : dataSize;
                System.arraycopy(markBuf, markPos, b, off, copySize);
                markPos += copySize;

                // read data directly from the stream
                // if size of data in the buffer is not enough
                int readSize = 0;
                if (copySize < len) {
                    readSize = parent.readBytes(
                        b, off + copySize, len - copySize);

                    // check if eos is reached
                    if (readSize == -1) {
                        readSize = 0;
                    } else {
                        // check the mark buffer overflow
                        if (markSize + readSize > markBuf.length) {
                            markBuf = null;
                        // cache the data in the mark buffer
                        } else {
                            System.arraycopy(
                                b, off + copySize, markBuf, markSize, readSize);
                            markSize += readSize;
                        }
                    }

                    isReadFromBuffer = false;
                }

                return copySize + readSize;
            } else {
                isReadFromBuffer = false;
            }
        }

        int readSize = parent.readBytes(b, off, len);

        // fill mark buffer if exists
        if (markBuf != null) {
            if (readSize > 0) {
                // check the mark buffer overflow
                if (markSize + readSize > markBuf.length) {
                    markBuf = null;
                    // cache the data in the mark buffer
                } else {
                    System.arraycopy(b, off, markBuf, markSize, readSize);
                    markSize += readSize;
                }
            }
        }

        return readSize;
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        if (parent != null) {
            parent.closeInputStream();
            parent = null;
        }
	// free buffer used by mark/reset operations if it was allocated
	markBuf = null;
    }

    /**
     * Tests if this input stream supports the <code>mark</code> and
     * <code>reset</code> methods.
     *
     * <p>The <code>markSupported</code> method of
     * <code>BaseInputStream</code> returns <code>true</code>.
     *
     * @return  always <code>true</code>
     *
     * @see     BaseInputStream#mark(int)
     * @see     BaseInputStream#reset()
     * @see     java.io.InputStream#markSupported()
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the current position in this input stream. A subsequent call to
     * the <code>reset</code> method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     *
     * <p> The <code>readlimit</code> arguments tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     *
     * <p> The stream remembers all the bytes read after the call to
     * <code>mark</code> and stands ready to supply those same bytes again
     * if and whenever the method <code>reset</code> is called.
     *  However, the stream is not remember any data at all if more
     * than <code>readlimit</code> bytes are read from the stream before
     * <code>reset</code> is called.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     BaseInputStream#reset()
     * @see     java.io.InputStream#reset()
     * @see     java.io.InputStream#mark(int)
     */
    public synchronized void mark(int readlimit) {
        // check whether the stream is closed
        if (parent == null) {
            return;
        }

        // use parent's mark/reset functionality
        // if the parent supports the own one
        if (parent.markSupported()) {
            parent.mark(readlimit);
        } else {
            byte[] oldBuf = markBuf;
            markBuf = new byte[readlimit];

            // copy relevant data from old buffer if any
            if (isReadFromBuffer) {
                int oldDataSize = markSize - markPos;
                int copySize = (readlimit > oldDataSize) ?
                    oldDataSize : readlimit;
                System.arraycopy(oldBuf, markPos, markBuf, 0, copySize);
                markSize = copySize;
            } else {
                markSize = 0;
            }
            markPos = 0;
        }
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     *
     * <p> If the method <code>mark</code> has not been called since
     * the stream was created, or the number of bytes read from the stream
     * since <code>mark</code> was last called is larger than the argument
     * to <code>mark</code> at that last call, then an
     * <code>IOException</code> is thrown.
     *
     * <p> If such an <code>IOException</code> is not thrown, then the
     * stream is reset to a state such that all the bytes read since the
     * most recent call to <code>mark</code> will be resupplied
     * to subsequent callers of the <code>read</code> method, followed by
     * any bytes that otherwise would have been the next input data as of
     * the time of the call to <code>reset</code>.
     *
     * @exception  IOException  if this stream has not been marked or if the
     *                          mark has been invalidated;
     *                          or if the stream is closed
     * @see     BaseInputStream#mark(int)
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset(int)
     * @see     java.io.IOException
     */
    public synchronized void reset() throws IOException {
        ensureOpen();

        // use parent's mark/reset functionality
        // if the parent supports the own one
        if (parent.markSupported()) {
            parent.reset();
        } else {
            if (markBuf == null) {
                throw new IOException("Invalid mark position");
            }
            markPos = 0;
            isReadFromBuffer = true;
        }
    }
}


/**
 * Output stream for the connection
 */
class BaseOutputStream extends OutputStream {

    /** Pointer to the connection */
    ConnectionBaseAdapter parent;

    /** Buffer for single char writes */
    byte[] buf = new byte[1];

    /**
     * Constructs a BaseOutputStream for an ConnectionBaseAdapter.
     *
     * @param p parent connection
     */
    BaseOutputStream(ConnectionBaseAdapter p) {
        parent = p;
    }

    /**
     * Check the stream is open
     *
     * @exception  InterruptedIOException  if it is not.
     */
    private void ensureOpen() throws InterruptedIOException {
        if (parent == null) {
            throw new InterruptedIOException("Stream closed");
        }
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     */
    public void write(int b) throws IOException {
        buf[0] = (byte)b;
        write(buf, 0, 1);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * The general contract for <code>write(b, off, len)</code> is that
     * some of the bytes in the array <code>b</code> are written to the
     * output stream in order; element <code>b[off]</code> is the first
     * byte written and <code>b[off+len-1]</code> is the last byte written
     * by this operation.
     * <p>
     * If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     * <p>
     * If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    public void write(byte b[], int off, int len)
           throws IOException {
        int test;
        int bytesWritten;

        ensureOpen();

        if (len == 0) {
            return;
        }

        /*
         * test the parameters here so subclasses do not have to,
         * this will avoid a crash in the native code
         */
        test = b[off] + b[len - 1] + b[off + len - 1];

        /*
         * Polling the native code is done here to allow for simple
         * asynchronous native code to be written. Not all implementations
         * work this way (they block in the native code) but the same
         * Java code works for both.
         */
        for (bytesWritten = 0; ; ) {
            try {
                bytesWritten += parent.writeBytes(b, off + bytesWritten,
                                                  len - bytesWritten);
            } finally {
                if (parent == null) {
                    throw new InterruptedIOException("Stream closed");
                }
            }

            if (bytesWritten == len) {
                break;
            }
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out. The general contract of <code>flush</code> is
     * that calling it is an indication that, if any bytes previously
     * written have been buffered by the implementation of the output
     * stream, such bytes should immediately be written to their
     * intended destination.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void flush() throws IOException {
        ensureOpen();
        parent.flush();
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream. The general contract of <code>close</code>
     * is that it closes the output stream. A closed stream cannot perform
     * output operations and cannot be reopened.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        if (parent != null) {
            parent.closeOutputStream();
            parent = null;
        }
    }
}
