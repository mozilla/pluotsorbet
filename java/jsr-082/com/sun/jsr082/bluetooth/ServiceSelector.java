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
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.DataElement;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import com.sun.jsr082.bluetooth.SDPClient;

/*
 * This class provides functionality of DiscoveryAgent.selectService()
 * (see JSR 82 texts for details) using SDP serviceAttribute request.
 */
class ServiceSelector extends ServiceSearcherBase {

    /* Set to false in RR version - then the javac skip the code. */
    private static final boolean DEBUG = false;

    /* this class name for debug. */
    private static final String cn = "ServiceSelector";

    /* Keeps attributes values retrieved from SDP_ServiceAttributeResponse. */
    private DataElement[] attrValues = null;

    /* Keeps an IOException if any occured or SDP_ErrorResponceRecieved. */
    private IOException ioExcpt = null;

    /*
     * Creates ServiceDiscoverer and save all required info in it.
     */
    ServiceSelector(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev) {
        super(attrSet, uuidSet, btDev);
    }

    /*
     * Recieves error information retrieved from SDP_ErrorResponse and
     * copmpletes the request activity by error reason.
     */
    public void errorResponse(int errorCode, String info, int transactionID) {
        if (DEBUG) {
            System.out.println(cn + ".errorResponse: called");
        }

        synchronized (this) {
            ioExcpt = new IOException(info);
            notify();
        }
    }

    /*
     * Base class method not relevant to this subclass, it must never be called.
     */
    public void serviceSearchResponse(int[] handleList, int transactionID) {
        if (DEBUG) {
            System.out.println(cn + ".serviceSearchResponse: unexpected call");
        }
        throw new RuntimeException("unexpected call");
    }

    /*
     * Base class method not relevant to this subclass, it must never be called.
     */
    public void serviceAttributeResponse(int[] attrIDs,
            DataElement[] attributeValues, int transactionID) {
        if (DEBUG) {
            System.out.println(cn +
                    ".serviceAttributeResponse: unexpected call");
        }
        throw new RuntimeException("unexpected call");
    }

    /*
     * Receives arrays of service record attributes and their values retrieved
     * from SDP_ServiceSearchAttributeResponse.
     */
    public void serviceSearchAttributeResponse(int[] attrIDs,
            DataElement[] attributeValues, int transactionID) {
        if (DEBUG) {
            System.out.println(cn + ".serviceSearchAttributeResponse: called");
        }
        synchronized (this) {
            attrSet = attrIDs;
            attrValues = attributeValues;
            notify();
        }
    }

    /*
     * Performs SERVICESEARCHATTRIBUTE transaction and returns newly created
     * <code>ServiceRecordImpl</code> instance with attributes and values
     * returned by server within SDP_serviceSearchAttributeResponse.
     *
     * @return newly created <code>ServiceRecordImpl</code> instance with
     * attributes and values returned by server if the transaction has completed
     * successfully and attributes list retrieved is not empty,
     * <code>null</code> otherwise.
     */
    ServiceRecord getServiceRecord() {
        SDPClient sdp = null;
        short transactionID = SDPClientTransactionBase.newTransactionID();

        try {
//            sdp = new JavaSDPClient(btDev.getBluetoothAddress());
           sdp = ServiceDiscovererFactory.getServiceDiscoverer().
                getSDPClient(btDev.getBluetoothAddress());

            if (sdp != null) {
                sdp.serviceSearchAttributeRequest(attrSet, uuidSet,
                        transactionID, this);
            }
        } catch (IOException ioe) {
            if (DEBUG) {
                ioe.printStackTrace();
            }
            ioExcpt = ioe;
        }

        synchronized (this) {
            if (ioExcpt == null && attrValues == null && sdp != null) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    // ignore (break waiting)
                }
            }
        }

        try {
            if (sdp != null) {
                sdp.close();
            }
        } catch (IOException ioe) {
            if (DEBUG) {
                ioe.printStackTrace();
            }
            // ignore
        }

        if (ioExcpt != null) {
            return null;
        } else if (attrValues == null) {
            return null;
        } else if (attrValues.length == 0) {
            return null;
        } else {
            return new ServiceRecordImpl(btDev, attrSet, attrValues);
        }
    }
}
