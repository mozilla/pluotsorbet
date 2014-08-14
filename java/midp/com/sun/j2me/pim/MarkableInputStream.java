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

package com.sun.j2me.pim;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream that supports mark() with an infinite lookahead.
 *
 */
public class MarkableInputStream extends InputStream {
    /** Input stream. */
    private final InputStream in;
    /** Internal buffered stream. */
    private ByteArrayOutputStream baos;
    /** Current buffer. */
    private byte[] buffer;
    /** Current index in buffer. */
    private int bufferIndex;
    /**
     * Constructs a markable input stream.
     * @param in input data
     */
    public MarkableInputStream(InputStream in) {
        this.in = in;
    }


    /**
     * Checks if mark is supported.
     * @return <code>true</code> if mark is supported
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * This implementation of mark() supports infinite lookahead.
     *
     * @param lookahead The value of this parameter is ignored.
     */
    public synchronized void mark(int lookahead) {
        baos = new ByteArrayOutputStream();
    }

    /**
     * Reset the line markers.
     * @throws IOException if an error occurs accessing the
     * input stream
     */
    public synchronized void reset() throws IOException {
        if (baos == null) {
            throw new IOException("Cannot reset an unmarked stream");
        }
        if (baos.size() == 0) {
            // no data was read since the call to mark()
            baos = null;
        } else {
            buffer = baos.toByteArray();
            baos = null;
            bufferIndex = 0;
        }
    }

    /**
     * Closes the input stream.
     * @throws IOException if any error occurs
     */
    public void close() throws IOException {
        in.close();
        baos.close();
        baos = null;
        buffer = null;
    }

    /**
     * Reads a byte from the stream.
     * @return next byte from stream
     * @throws IOException if an error occurs
     */
    public int read() throws IOException {
        if (buffer != null &&
            bufferIndex < buffer.length) {
            return readFromBuffer();
        } else {
            return readFromStream();
        }
    }

    /**
     * Reads a byte from the internal buffer.
     * @return next byte from the buffer.
     */
    private int readFromBuffer() {
        int i = buffer[bufferIndex++];
        if (baos != null) {
            baos.write(i);
        }
        if (bufferIndex == buffer.length) {
            buffer = null;
        }
        return i;
    }

    /**
     * Reads next value from the input stream.
     * @return next value from stream
     * @throws IOException if an error occurs
     */
    private int readFromStream() throws IOException {
        int i = in.read();
        if (i != -1 && baos != null) {
            baos.write(i);
        }
        return i;
    }

    /**
     * Reads next block of bytes from the stream.
     * @param b buffer to hold data
     * @param offset in buffer for  data read
     * @param length size of data to read
     * @return number of bytes read
     * @throws IOException if an error occurs
     */
    public int read(byte[] b, int offset, int length) throws IOException {
        if (buffer != null) {
            return readFromBuffer(b, offset, length);
        } else {
            return readFromStream(b, offset, length);
        }
    }

    /**
     * Reads next block of bytes from the internal buffer.
     * @param b buffer to hold data
     * @param offset in buffer for  data read
     * @param length size of data to read
     * @return number of bytes read
     */
    private int readFromBuffer(byte[] b, int offset, int length) {
        int bytesRead = -1;
        if (length <= buffer.length - bufferIndex) {
            System.arraycopy(buffer, bufferIndex, b, offset, length);
            bufferIndex += length;
            bytesRead = length;
        } else {
            int count = buffer.length - bufferIndex;
            System.arraycopy(buffer, bufferIndex, b, offset, count);
            buffer = null;
            bytesRead = count;
        }
        if (baos != null) {
            baos.write(b, offset, bytesRead);
        }
        return bytesRead;
    }

    /**
     * Reads next block of bytes from the stream.
     * @param b buffer to hold data
     * @param offset in buffer for  data read
     * @param length size of data to read
     * @return number of bytes read
     * @throws IOException if an error occurs
     */
    private int readFromStream(byte[] b, int offset, int length)
        throws IOException {

        int i = in.read(b, offset, length);
        if (i != -1 && baos != null) {
            baos.write(b, offset, i);
        }
        return i;
    }

    /**
     * Reads next block of bytes from the stream.
     * @param b buffer to hold data
     * @return number of bytes read
     * @throws IOException if an error occurs
     */
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

}
