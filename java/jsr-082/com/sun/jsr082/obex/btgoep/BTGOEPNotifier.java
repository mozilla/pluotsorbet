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

import java.io.IOException;
import javax.bluetooth.*;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.Connection;
import com.sun.jsr082.bluetooth.ServiceRecordImpl;
import com.sun.jsr082.obex.ObexTransportNotifier;
import com.sun.jsr082.obex.ObexTransport;

/*
 * Provides underlying stream notifier to shared obex implementation.
 */
public class BTGOEPNotifier implements ObexTransportNotifier {

    /* Keeps notifier for transport layer */
    private StreamConnectionNotifier notifier;

    /* Keeps OBEX UUID for service record construction. */
    static public final DataElement DE_OBEX_UUID =
        new DataElement(DataElement.UUID, new UUID(0x0008));

    /*
     * Create BTGOEP Notifier
     * @param notifier notifier for transport layer
     * @exception IOException if an error occured while service record
     * creation
     */
    protected BTGOEPNotifier(StreamConnectionNotifier notifier)
        throws IOException {
        this.notifier = notifier;
    }

    /*
     * Accepts client connection to the service this notifier is assigned to.
     *
     * @return connection to a client just accepted on transport layer.
     * @exception IOException if an error occured on transport layer.
     */
    public ObexTransport acceptAndOpen() throws IOException {
        return createTransportConnection(
                (StreamConnection)(notifier.acceptAndOpen()));
    }

    /*
     * Closes this notifier on the transport layer
     * @exception IOException if an error occured on transport layer
     */
    public void close() throws IOException {
        notifier.close();
    }

    /*
     * Create btgoep transport connection.
     * @param sock transport connection
     * @return BTGOEP Connection
     */
    protected BTGOEPConnection createTransportConnection(
            StreamConnection sock) throws IOException {
        return new BTGOEPConnection(sock);
    }

    /*
     * Get transport connection noifier
     * @return transport notifier
     */
    public Connection getUnderlyingConnection() {
        return notifier;
    }
}
