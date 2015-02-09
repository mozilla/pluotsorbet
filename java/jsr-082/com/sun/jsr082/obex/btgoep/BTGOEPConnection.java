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
package com.sun.jsr082.obex.btgoep;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.Connection;
import com.sun.j2me.main.Configuration;
import com.sun.jsr082.obex.ObexTransport;

/*
 * Provides underlying stream connection used as transport by shared obex
 * implementation.
 */
public class BTGOEPConnection implements ObexTransport {

    private StreamConnection sock;
    private InputStream is;
    private OutputStream os;

    /*
     * Create BTGOEPConnection
     * @param sock Stream connection for the transport layer
     */
    protected BTGOEPConnection(StreamConnection sock) throws IOException {
        this.sock = sock;
        is = sock.openInputStream();
        os = sock.openOutputStream();
    }

    /*
     * Closes connection as well as the input stream and
     * the output stream openning for this connection.
     * @throws IOException if I/O error.
     */
    public void close() throws IOException {
        IOException ioe = null;

        try {
            is.close();
        } catch (IOException e) {
            ioe = e;
        }

        try {
            os.close();
        } catch (IOException e) {
            ioe = e;
        }

        try {
            sock.close();
        } catch (IOException e) {
            ioe = e;
        }

        // catch IOException if any of the above call has thrown one
        if (ioe != null) {
            throw ioe;
        }
    }

    /*
     * Reads the packet data into specified buffer.
     * <p>
     * If the specified buffer length is 0, then 0 data
     * will be read into this buffer, and the rest of packet
     * data is lost.
     * <p>
     *
     * @param inData the data array to fill with received bytes.
     * @exception IOException if a local or remote connection
     * is closed or I/O error has happen.
     *
     * @exception NullPointerException if the specified buffer is null.
     */
    public int read(byte[] inData) throws IOException {
        readFully(inData, 0, 3); // read header
        int packetLength = decodeLength16(inData, 1);
        if (packetLength < 3 || packetLength > inData.length) {
            throw new IOException("protocol error");
        }

        readFully(inData, 3, packetLength - 3);
        return packetLength;
    }

    /*
     *
     * @param outData the buffer with the data to be sent.
     * @param len the number of bytes to be sent.
     * @exception IOException if a local or remote connection
     * is closed or I/O error has happen.
     *
     * @exception NullPointerException if the specified buffer is null.
     */
    public void write(byte[] outData, int len) throws IOException {
        os.write(outData, 0, len);
        os.flush();
    }

    /*
     * Determines the amount of data (maximum packet size) that can
     * be successfully sent in a single write operation. If the size
     * of data is greater than the maximum packet size, then then only
     * the first maximum packet size bytes of the packet are sent,
     * and the rest will be discarded.
     * <p>
     *
     * @return the maximum number of bytes that can be sent/received
     * in a single call to read()/ write() without losing any data.
     */
    public int getMaximumPacketSize() {
        return Configuration.getIntProperty(
                    "obex.packetLength.max", 4096);
    }

    /*
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.
     * @param      array     the buffer into which the data is read.
     * @param      offset   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      size   the maximum number of bytes to read.
     * @exception  IOException  if an I/O error occurs.
     */
    private final void readFully(byte[] array, int offset, int size)
            throws IOException {
        while (size != 0) {
            int count = is.read(array, offset, size);
            if (count == -1) {
                throw new IOException("read error");
            }
            offset += count;
            size -= count;
        }
    }

    private final int decodeLength16(byte[] buffer, int off) {
        return ((((int)buffer[off]) & 0xFF) << 8)
            + (((int)buffer[off + 1]) & 0xFF);
    }

    /*
     * Get underlying connection.
     */
    public Connection getUnderlyingConnection() {
        return sock;
    }
}
