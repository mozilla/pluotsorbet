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

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.bluetooth.DataElement;

import com.sun.jsr082.bluetooth.SDPClient;

import java.util.Enumeration;
import java.util.Hashtable;

/*
 * This class saves information about every service descovery request
 * to provide functionality of DiscoveryAgent using multiple requests
 * via SDPClient (Service Descovery Protocol) by ServiceSelector
 * and ServiceSearcher classes.
 */
abstract public class ServiceSearcherBase implements SDPResponseListener {

    /*
     * maximum number of allowed UUIDS in search uuids sequence
     */
    private static final int MAX_ALLOWED_UUIDS = 12;
    private static final Object FAKE_VALUE = new Object();
    /* Mask to determine an attribute ID out of range. */
    private static final int MASK_OVERFLOW = 0xffff0000;

    /* RemoteDevice whose response to be listened. */
    RemoteDevice btDev;

    /*
     * The UUIDs from SDP_ServiceSearchRequest or
     * SDP_ServiceSearchAttrbuteRequest.
     *
     * @see SDPClient#serviceSearchRequest
     * @see SDPClient#serviceSearchAttributeRequest
     */
    UUID[] uuidSet;

    /*
     * Attributes list from SDP_ServiceSearchAttrbuteRequest.
     *
     * @see SDPClient#serviceSearchAttributeRequest
     */
    int[] attrSet;

    /*
     * Creates ServiceSearcherBase and save all required info in it.
     */
    ServiceSearcherBase() {
        super();
    }

    /*
     * Creates an instance of ServiceSearcherBase
     *
     * @param attrSet list of attributes whose values are requested.
     * @param uuidSet list of UUIDs that indicate services relevant to request.
     * @param btDev remote Bluetooth device to listen response from.
     * @param discListener discovery listener.
     */
    ServiceSearcherBase(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev) {
        super();
        initialize(attrSet, uuidSet, btDev);
    }

    /*
     * Initializes an instance of ServiceSearcherBase
     *
     * @param attrSet list of attributes whose values are requested.
     * @param uuidSet list of UUIDs that indicate services relevant to request.
     * @param btDev remote Bluetooth device to listen response from.
     * @param discListener discovery listener.
     */
    protected void initialize(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev)
        throws IllegalArgumentException, NullPointerException {

        this.btDev = btDev;
        this.attrSet = ServiceSearcherBase.extendByStandardAttrs(attrSet);
        this.uuidSet = ServiceSearcherBase.removeDuplicatedUuids(uuidSet);
    }

    /*
     * Informs this listener about errors during Service Discovering process.
     *
     * @param errorCode error code recieved from server or generated locally
     * due to transaction terminating.
     *
     * @param info detail information about the error
     *
     * @param transactionID ID of transaction response recieved within.
     */
    abstract public void errorResponse(int errorCode, String info,
        int transactionID);

    /*
     * Informs this listener about found services records.
     *
     * @param handleList service records handles returned by server within
     * SDP_ServiceSearchResponse.
     *
     * @param transactionID ID of transaction response recieved within.
     */
    abstract public void serviceSearchResponse(int[] handleList,
        int transactionID);

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
    abstract public void serviceAttributeResponse(int[] attrIDs,
            DataElement[] attributeValues, int transactionID);

    /*
     * Informs this listener about attributes of fisrt found service record.
     */
    abstract public void serviceSearchAttributeResponse(int[] attrIDs,
            DataElement[] attributeValues, int transactionID);
    
    public static int[] extendByStandardAttrs( int[] attrSet ) 
        throws IllegalArgumentException {
        /* Extend attributes IDs with standard values from the spec except duplicates */
        Hashtable uniquies = new Hashtable();
        /* appending by user required attributes */
        if (attrSet != null) {
            if (attrSet.length <= 0 || attrSet.length > ServiceRecordImpl.RETRIEVABLE_MAX) {
                throw new IllegalArgumentException("Invalid attribute set length");
            }
            for (int i=0; i<attrSet.length; i++) {
               if (uniquies.put(new Integer(attrSet[i]), FAKE_VALUE) != null ) {
                   throw new IllegalArgumentException( "Duplicate attribute Ox" + Integer.toHexString(attrSet[i])  );
               }
               if ((attrSet[i] & MASK_OVERFLOW) != 0) {
                   throw new IllegalArgumentException("Illegal attribute ID");
               }
            }        
        }
        /* Adding standard attributes */
        for (int i=0; i<5; i++) {
            uniquies.put(new Integer(i), FAKE_VALUE);
        }
        int [] extendedAttributes = new int[uniquies.size()];        
        Enumeration attrs = uniquies.keys();
        for( int i=0; attrs.hasMoreElements(); i++ ) {
            extendedAttributes[i] = ((Integer) attrs.nextElement()).intValue();
        }
        attrSort(extendedAttributes);
        return extendedAttributes;
    }

    /*
     * Sorts an integer array in ascending order.
     */
    private static void attrSort(int[] data) {

        for (int k = 0; k < data.length - 1; k++)
        {
            boolean isSorted = true;

            for (int i = 1; i < data.length - k; i++)
            {
                if (data[i] < data[i - 1])
                {
                    int tmp = data[i];
                    data[i] = data[i - 1];
                    data[i - 1] = tmp;

                    isSorted = false;

                }
            }

            if (isSorted) 
                break;
        }
    }
    
    public static UUID[] removeDuplicatedUuids( UUID[] uuidSet ) 
        throws IllegalArgumentException, NullPointerException {
        Hashtable uniquies = new Hashtable();
        UUID[] uuids;
        if ((uuidSet != null) && (uuidSet.length > 0)) {
        /* uuid checking */
            for (int i = 0; i < uuidSet.length; i++) {

                if (uuidSet[i] == null) {
                    throw new NullPointerException("Invalid UUID. Null");
                }

                /* check UUID duplication */
                if (uniquies.put(uuidSet[i], FAKE_VALUE) != null) {
                    throw new IllegalArgumentException("Duplicated UUID: " + 
                            uuidSet[i]);
                }
            }

            uuids = new UUID[uniquies.size()];
            Enumeration keys = uniquies.keys();

            for (int i = 0; keys.hasMoreElements(); i++) {
                uuids[i] = (UUID) keys.nextElement();
            }
        } else {
            uuids = new UUID[0];
        }
        return uuids;
    }
}
