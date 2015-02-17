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
package com.sun.jsr082.bluetooth;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.UUID;

/*
 * SDPClient class provides a client side of SDP connection as described in
 * Bluetooth Specification version 1.2.
 */
public interface SDPClient {

    /*
     * Closes connection of this client to the specified server.
     *
     * @throws IOException if no connection is open
     */
    public void close() throws IOException;

    /*
     * Initiates ServiceAttribute transaction that retrieves
     * specified attribute values from a specific service record.
     */
    public void serviceAttributeRequest(int serviceRecordHandle, int[] attrSet,
        int transactionID, SDPResponseListener listener) throws IOException;

    /*
     * Initiates ServiceSearchAttribute transaction that searches for services
     * on a server by UUIDs specified and retrieves values of specified
     * parameters for service records found.
     */
    public void serviceSearchAttributeRequest(int[] attrSet, UUID[] uuidSet,
        int transactionID, SDPResponseListener listener) throws IOException;

}
