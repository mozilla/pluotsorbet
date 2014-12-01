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
package com.sun.jsr082.obex;

import javax.microedition.io.Connection;
import java.io.IOException;

public interface ObexTransport extends Connection {

    /*
     * Reads the packet data into specified buffer.
     * <p>
     * If the specified buffer length is 0, then 0 data
     * will be read into this buffer, and the rest of packet
     * data is lost.
     * <p>
     *
     *
     * @param inData the data array to fill with received bytes.
     *
     * @exception IOException if a local or remote connection
     * is closed or I/O error has happen.
     *
     * @exception NullPointerException if the specified buffer is null.
     */
    public int read(byte[] inData) throws IOException;

    /*
     *
     *
     * @param outData the buffer with the data to be sent.
     *
     * @param len the number of bytes to be sent.
     *
     * @exception IOException if a local or remote connection
     * is closed or I/O error has happen.
     *
     * @exception NullPointerException if the specified buffer is null.
     */
    public void write(byte[] outData, int len) throws IOException;

    /*
     * Determines the amount of data (maximum packet size) that can
     * be successfully sent in a single write operation. If the size
     * of data is greater than the maximum packet size, then then only
     * the first maximum packet size bytes of the packet are sent,
     * and the rest will be discarded.
     * <p>
     * If the returned values is 0, this means the transport
     * implementation is based on a stream protocol, i.e.
     * any packet size may be used.
     *
     * @return the maximum number of bytes that can be sent/received
     * in a single call to read()/ write() without losing any data.
     */
    public int getMaximumPacketSize();

    /*
     * Get underlying connection.
     */
    public Connection getUnderlyingConnection();
} // end of interface 'ObexTransport' definition
