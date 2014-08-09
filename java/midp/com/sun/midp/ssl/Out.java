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

import java.io.OutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * This class is a subclass of OutputStream and is used for
 * writing data to an SSL connection.
 * <P />
 * @see com.sun.midp.ssl.SSLStreamConnection
 * @see com.sun.midp.ssl.In
 */ 
class Out extends OutputStream {
    /**
     * The maximum SSL record size to write, currently 2048.
     * RFC 2246 specifies it can be up to 2^14 + 2048, however
     * breaking up streams into smaller chunks make more sense, lower
     * memory usage for small devices and interspacing encryption and
     * network writes may work better on congested wireless networks.
     */
    private static final int MAX_RECORD_SIZE = 2048;

    /** Indicates the output stream is closed. */
    private boolean isClosed = false;

    /** Underlying SSL record layer to which bytes are written. */
    private Record rec;
    /** Handle to current SSL stream connection. */
    private SSLStreamConnection ssc;
    /** A reusable buffer for the <code>write</code> method. */
    private byte[] buf = new byte[1];

    /**
     * Creates a new Out object.
     * <P />
     * @param r SSL record layer object to which bytes are written
     * @param c SSLStreamConnection object this Out object is a part of
     */ 
    Out(Record r, SSLStreamConnection c) {
        rec = r;
        ssc = c;
    }
    
    /**
     * Writes the specified byte to this output stream.
     * <P />
     * @param b byte to be written
     * @exception IOException if I/O error occurs
     */ 
    public void write(int b) throws IOException {
        buf[0] = (byte) b;
        write(buf, 0, 1);
    }
    
    /**
     * Writes all the bytes in the specified byte array to this 
     * output stream. This is equivalent to write(b, 0, b.length).
     * <P />
     * @param b byte array containing data to be written
     * @exception IOException if I/O error occurs
     */ 
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes <CODE>len</CODE> bytes starting at offset 
     * <CODE>off</CODE> from byte array <CODE>b</CODE> to this 
     * output stream.
     * <P />
     * @param b byte array containing data to be written
     * @param off starting offset of data to be written
     * @param len number of bytes to be written
     * @exception IOException if I/O error occurs
     */ 
    public void write(byte[] b, int off, int len) throws IOException {
        if (isClosed) {
            throw new InterruptedIOException("Stream closed");
        }

        synchronized(rec) {
            int bytesToWrite = MAX_RECORD_SIZE;
            while (len > 0) {
                if (len < bytesToWrite) {
                    bytesToWrite = len;
                }

                rec.wrRec(Record.APP, b, off, bytesToWrite);
                len -= bytesToWrite;
                off += bytesToWrite;
            }
        }
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
            ssc.outputStreamState = SSLStreamConnection.CLOSED;
            rec.closeOutputStream();
            ssc.cleanupIfNeeded();
        }
    }
    
    // Other methods: flush() need not be over ridden
}
