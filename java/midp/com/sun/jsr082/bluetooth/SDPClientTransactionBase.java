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

/*
 * This abstract class provides base functionality for all SDP
 * transactions.
 */
public abstract class SDPClientTransactionBase {
    /*
     * Helper object which serializes and restores
     * <code>DataElement</code>s.
     */
    protected DataElementSerializer des = new DataElementSerializer();

    /* ID of SDP_ErrorResponse protocol data unit. */
    public static final int SDP_ERROR_RESPONSE = 0x01;

    /* ID of SDP_ServiceSearchRequest protocol data unit. */
    public static final int SDP_SERVICE_SEARCH_REQUEST = 0x02;

    /* ID of SDP_ServiceSearchResponse protocol data unit. */
    public static final int SDP_SERVICE_SEARCH_RESPONSE = 0x03;

    /* ID of SDP_ServiceAttributeRequest protocol data unit. */
    public static final int SDP_SERVICE_ATTRIBUTE_REQUEST = 0x04;

    /* ID of SDP_ServiceAttributeResponse protocol data unit. */
    public static final int SDP_SERVICE_ATTRIBUTE_RESPONSE = 0x05;

    /* ID of SDP_ServiceSearchAttributeRequest protocol data unit. */
    public static final int SDP_SERVICE_SEARCH_ATTRIBUTE_REQUEST = 0x06;

    /* ID of SDP_ServiceSearchAttributeResponse protocol data unit. */
    public static final int SDP_SERVICE_SEARCH_ATTRIBUTE_RESPONSE = 0x07;

    /* Max retrievable service record handles. Maybe defined via property. */
    public static final int MAX_SERVICE_RECORD_COUNT = 0x0fff;

    /* Max total size of retrievable attributes. Maybe defined via property. */
    public static final int MAX_ATTRIBUTE_BYTE_COUNT = 0xffff;

    /*
     * The lowest possible value of transaction ID.
     * The number must be positive.
     */
    public static final int firstTransactionID = 0x0001;

    /* The maximum possible value of transaction ID. */
    public static final int maxTransactionID = 0xffff;

    /* Next transaction ID. */
    protected static int effectiveTransactionID = firstTransactionID;

    /*
     * Retrieves next new transaction ID.
     *
     * @return new transaction ID.
     */
    public static synchronized short newTransactionID() {
        int transactionID = effectiveTransactionID++;
        if (effectiveTransactionID > maxTransactionID) {
            // strictly speaking, this is not quite safe,
            // need revisit : if we have a pending
            // transaction after 64K of subsequent calls
            effectiveTransactionID = firstTransactionID;
        }
        return (short)transactionID;
    }

    /*
     * Frees transaction ID.
     *
     * @param transactionID the ID to free.
     */
    public static synchronized void freeTransactionID(short transactionID) {
        // empty in this implementation
    }
    
    
}
