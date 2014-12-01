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

import javax.bluetooth.DataElement;
import javax.bluetooth.UUID;

/*
 * Provides ServiceSearch transaction functionality.
 */
public class ClientServiceSearchTransaction extends SDPClientTransaction {

    /* ServiceSearchPattern (BT Spec 1.2 Vol 3 page 135). */
    DataElement serviceSearchPattern;
    /* Acquired service record handles. */
    int[] handleList;
    /* Current position in the handleList. */
    int offset;

    /*
     * Constructs ServiceSearchTransaction object.
     */
    public ClientServiceSearchTransaction(JavaSDPClient client, int transactionID,
            SDPResponseListener listener, UUID[] uuidSet) {
        super(client, SDPClientTransaction.SDP_SERVICE_SEARCH_REQUEST, transactionID, listener);
        serviceSearchPattern = new DataElement(DataElement.DATSEQ);
        for (int i = 0; i < uuidSet.length; i++) {
            serviceSearchPattern.addElement(new DataElement(
                    DataElement.UUID, uuidSet[i]));
        }
        parameterLength = super.des.getDataSize(serviceSearchPattern) + 2;
    }

    /*
     * Writes transaction-specific parameters into the PDU.
     *
     * @throws IOException when an I/O error occurs
     */
    void writeParameters() throws IOException {
        super.client.getConnection().getReaderWriter().writeDataElement(serviceSearchPattern);
        super.client.getConnection().getReaderWriter().writeShort((short)MAX_SERVICE_RECORD_COUNT);
    }

    /*
     * Reads transaction-specific parameters from the PDU.
     *
     * @param length length of PDU's parameters
     * @throws IOException when an I/O error occurs
     */
    void readParameters(int length) throws IOException {
        int totalServiceRecordCount = super.client.getConnection().getReaderWriter().readShort();
        int currentServiceRecordCount = super.client.getConnection().getReaderWriter().readShort();
        if (handleList == null && totalServiceRecordCount > 0) {
            handleList = new int[totalServiceRecordCount];
        }
        for (int i = 0; i < currentServiceRecordCount; i++) {
            handleList[offset] = super.client.getConnection().getReaderWriter().readInteger();
            offset++;
        }
        System.out.println("<<< " + totalServiceRecordCount + " ServiceRecords found" );
    }

    /*
     * Completes the transaction by calling corresponding listener's
     * method with the data retrieved.
     */
    public void complete() {
        listener.serviceSearchResponse(handleList, ssTransID);
    }
}
