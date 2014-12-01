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
package com.sun.jsr082.bluetooth;
import javax.bluetooth.DataElement;

/*
 * Common interface for SDP server responses listeners. Listenners are assigned
 * to active transactions and recieve SDP server responses.
 *
 */
public interface SDPResponseListener {
    /* Transaction cancelling reason: abnormal IO error. */
    static final int IO_ERROR = 0x010000;

    /* Transaction cancelling reason: transaction has been terminated. */
    static final int TERMINATED = 0x010001;

    /* Error code returned by SDP server: Invalid/unsupported SDP version. */
    static final int SDP_INVALID_VERSION = 0x01;

    /* Error code returned by SDP server: Invalid Service Record Handle. */
    static final int SDP_INVALID_SR_HANDLE = 0x02;

    /* Error code returned by SDP server: Invalid request syntax. */
    static final int SDP_INVALID_SYNTAX = 0x03;

    /* Error code returned by SDP server: Invalid PDU Size. */
    static final int SDP_INVALID_PDU_SIZE = 0x04;

    /* Error code returned by SDP server: Invalid Continuation State. */
    static final int SDP_INVALID_CONTINUATION_STATE = 0x05;

    /*
     * Error code returned by SDP server: Insufficient Resources to satisfy
     * Request. */
    static final int SDP_INSUFFICIENT_RESOURCES = 0x06;

    /*
     * Informs this listener about errors during Service Discovering process.
     *
     * @param errorCode error code recieved from server or generated locally
     * due to transaction terminating.
     *
     * @param info detail information  about the error
     *
     * @param transactionID ID of transaction response recieved within.
     */
    void errorResponse(int errorCode, String info, int transactionID);

    /*
     * Informs this listener about found services records.
     *
     * @param handleList service records handles returned by server within
     * SDP_ServiceSearchResponse.
     *
     * @param transactionID ID of transaction response recieved within.
     */
    void serviceSearchResponse(int[] handleList, int transactionID);

    /*
     * Informs this listener about found attributes of specified service record.
     *
     * @param attrIDs list of attributes whose values requested from server
     * within SDP_ServiceAttributesRequest.
     *
     * @param attributeValues values returned by server within
     * SDP_ServiceAttributesResponse.
     *
     * @param transactionID ID of transaction response recieved within.
     */
    void serviceAttributeResponse(int[] attrIDs,
            DataElement[] attributeValues, int transactionID);

    /*
     * Informs this listener about attributes of fisrt found service record.
     *
     * @param attrIDs list of attributes whose values requested from server
     * within SDP_ServiceSearchAttributesRequest.
     *
     * @param attributeValues values returned by server within
     * SDP_ServiceSearchAttributesResponse.
     *
     * @param transactionID ID of transaction response recieved within.
     */
    void serviceSearchAttributeResponse(int[] attrIDs,
            DataElement[] attributeValues, int transactionID);
}
