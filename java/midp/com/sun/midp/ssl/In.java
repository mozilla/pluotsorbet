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

package com.sun.midp.ssl;

import java.io.InputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * This class is a subclass of InputStream and obtains its input bytes
 * from an SSL connection.
 * <P />
 * @see com.sun.midp.ssl.SSLStreamConnection
 * @see com.sun.midp.ssl.Out
 */ 
class In extends InputStream {
    /** Indicates the input stream is closed. */
    private boolean isClosed = false;

    /** Underlying SSL record layer from which bytes are read. */
    private Record rec;
    
    /** Start of plain text in data buffer. */
    private int start;
    /** Count of unread bytes left in data buffer. */
    private int cnt;
    /** Handle for current SSL stream connection. */
    private SSLStreamConnection ssc;
    /** Signals end of stream. */
    private boolean endOfStream = false;

    /**
     * Refills the internal store of decrypted bytes. Called when 
     * the byte count in the store reaches zero.
     *
     * @param block if true the method will not return until data is available,
     *              or end of stream
     *
     * @exception IOException is thrown, if an I/O error occurs filling the
     * the buffer
     */
    private void refill(boolean block) throws IOException {
        if (endOfStream) {
            return;
        }

        for (; ;) {
            rec.rdRec(block, Record.APP);
            if (rec.plainTextLength == -1) {
                endOfStream = true;
                return;
            }

            // Do not unblock on a zero byte record unless asked
            if (!block || rec.plainTextLength > 0) {
                break;
            }
        }

        cnt = rec.plainTextLength;
        start = 0;
    }

    /**
     * Creates a new In object.
     * <P />
     * @param r Record layer object from which input bytes are read
     * @param c SSLStreamConnection object this In object is a part of
     */ 
    In(Record r, SSLStreamConnection c) {
        rec = r;
        ssc = c;
    }

    /**
     * Reads a byte from this input stream. The method blocks if no
     * input is available.
     * <P />
     * @return the next byte of data, or -1 if end of stream is reached
     * @exception IOException if an I/O error occurs
     */ 
    public int read() throws IOException {
        int val;
        if (isClosed) {
            throw new InterruptedIOException("Stream closed");
        }

        synchronized(rec) {
            if (cnt == 0) {
                refill(true);
                if (cnt == 0) {
                    return -1; // end of stream
                }
            }
    
            val = rec.inputData[start++] & 0xff;
            cnt--;
        }

        return val;
    }
    
    /**
     * Reads up to <CODE>b.length</CODE> bytes of data from this
     * input stream into the byte array <CODE>b</CODE>. Blocks until 
     * some input is available. This is equivalent to 
     * <CODE>read(b, 0, b.length)</CODE>.
     * <P />
     * @param b the buffer into which data is read
     * @return the actual number of bytes read into the buffer, or -1
     * if there is no more data and the end of input stream has been
     * reached
     * @exception IOException if an I/O error occurs
     */ 
    public int read(byte[] b) throws IOException {
	    return read(b, 0, b.length);
    }

    /**
     * Reads up to <CODE>len</CODE> bytes of data from this input stream
     * into <CODE>b</CODE> starting at offset <CODE>off</CODE>.
     * <P />
     * @param b buffer into which data is read
     * @param off starting offset where data is read
     * @param len maximum number of bytes to be read
     * return the actual number of bytes read into the buffer, or -1
     * if there is no more data and the end of input stream has been
     * reached
     * @return number of bytes read
     * @exception IOException if an I/O error occurs
     */ 
    public int read(byte[] b, int off, int len) throws IOException {

        int i = 0;
        int numBytes;

        if (isClosed) {
            throw new InterruptedIOException("Stream closed");
        }

        synchronized(rec) {
            if (cnt == 0) {
                // Record buffer empty, block until it is refilled.
                refill(true);
                if (cnt == 0) {
                    return -1; // end of stream
                }
            }

            if (len > cnt) {
                numBytes = cnt;
            } else {
                numBytes = len;
            }

            System.arraycopy(rec.inputData, start, b, off, numBytes);
            start += numBytes;
            cnt -= numBytes;
        }

        return numBytes;
    }

    /**
     * Close the stream connection.
     *
     * @exception IOException is thrown, if an I/O error occurs while
     * shutting down the connection
     */
    synchronized public void close() throws IOException {
        if (isClosed) {
            return;
        }

        isClosed = true;
        if (ssc != null) {
            ssc.inputStreamState = SSLStreamConnection.CLOSED;
            rec.closeInputStream();
            ssc.cleanupIfNeeded();
        }
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.  The next caller might be the same thread or
     * another thread.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {
        if (isClosed) {
            throw new InterruptedIOException("Stream closed");
        }

        synchronized(rec) {
            if (cnt == 0) {
                // The record buffer is empty, try to refill it without blocking.
                refill(false);
            }
            return cnt;
        }
    }

    /*
     * The remaining methods: markSupported(), mark(int), 
     * reset() need not be overridden
     */ 
}
