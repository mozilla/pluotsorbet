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
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import com.sun.jsr082.bluetooth.SDPClient;

/*
 * Service record implementation.
 */
public final class ServiceRecordImpl implements ServiceRecord {

    /* Maxumum quantity of attributes in one request */
    static final int RETRIEVABLE_MAX;

    /*
     * Maximum number of concurrent service searches that can
     * exist at any one time.
     */
    static final int TRANS_MAX;

    /* Remote device service provided by. */
    private RemoteDevice remoteDevice = null;

    /* Service notifier. */
    private BluetoothNotifier notifier = null;

    /* Attribues of the record. */
    private Hashtable attributesTable = null;

    /* Bit scale that keeps service classes. */
    private int serviceClasses = 0;

    /* Mask to identify attribute IDs out of range. */
    private static final int MASK_OVERFLOW = 0xffff0000;

    /* Mask of incorrect class bits. */
    private static final int MASK_INCORRECT_CLASS = 0xff003fff;

    /* ServiceRecordHandle attribute ID. */
    public static final int SERVICE_RECORD_HANDLE = 0x0000;

    /* ProtocolDescriptorList attribute ID. */
    public static final int PROTOCOL_DESCRIPTOR_LIST = 0x0004;

    /* Service class attribute id. */
    public static final int SERVICE_CLASS_ATTR_ID = 0x0001;

    /* Name attribute id. */
    public static final int NAME_ATTR_ID = 0x0100;

    /* Protocol type. */
    private int protocol = BluetoothUrl.UNKNOWN;

    /* Bluetooth address of device service record came from. */
    private String btaddr = null;

    /* PSM or channel id. */
    private int port = -1;

    /* Record handle */
    private int recHandle = 0;
    /* SDPClient from where this ServiceRecord is created */
    public SDPClient sdpClient = null;
    
    static {
        int retrievableMax = 5; // default value
        try {
            retrievableMax = Integer.parseInt(LocalDevice.getProperty(
                "bluetooth.sd.attr.retrievable.max"));
        } catch (NumberFormatException e) {
            System.err.println("Internal error: ServiceRecordImpl: "
                    + "improper retrievable.max value");
        }
        RETRIEVABLE_MAX = retrievableMax;

        int transMax = 10;  // default value
        try {
            transMax = Integer.parseInt(LocalDevice.getProperty(
                "bluetooth.sd.trans.max"));
        } catch (NumberFormatException e) {
            System.err.println("Internal error: ServiceRecordImpl: "
                    + "improper trans.max value");
        }
        TRANS_MAX = transMax;
    }

    /*
     * Creates service records on client device.
     *
     * @param device server device
     * @param attrIDs attributes IDs
     * @param attrValues attributes values
     */
    public ServiceRecordImpl(RemoteDevice device, int[] attrIDs,
            DataElement[] attrValues) {
        init(attrIDs, attrValues);
        remoteDevice = device;
    }


    /*
     * Creates service records for the given notifier.
     *
     * @param notifier notifier to be associated with this service record
     * @param attrIDs attributes IDs
     * @param attrValues attributes values
     */
    public ServiceRecordImpl(BluetoothNotifier notifier, int[] attrIDs,
            DataElement[] attrValues) {
        init(attrIDs, attrValues);
        this.notifier = notifier;
    }

    /*
     * Creates a copy of this record. The copy recieves new instances of
     * attributes values which are of types <code>DataElement.DATSEQ</code>
     * or <code>DataElement.DATALT</code> (the only data element types that
     * can be modified after creation).
     *
     * @return new instance, a copy of this one.
     */
    public synchronized ServiceRecordImpl copy() {
        int count = attributesTable.size();
        int[] attrIDs = new int[count];
        DataElement[] attrValues = new DataElement[count];

        Enumeration ids = attributesTable.keys();
        Enumeration values = attributesTable.elements();

        for (int i = 0; i < count; i++) {
            attrIDs[i] = ((Integer)ids.nextElement()).intValue();
            // no nedd to copy elements here; service record constructor
            // performs the copying
            attrValues[i] = (DataElement)values.nextElement();
        }

        ServiceRecordImpl servRec = new ServiceRecordImpl(notifier,
                      attrIDs, attrValues);
        servRec.serviceClasses = serviceClasses;
        return servRec;
    }

    /*
     * Returns service record handle.
     *
     * @return service record handle, or 0 if the record is not in SDDB.
     */
    public int getHandle() {
        DataElement handle = getAttributeValue(SERVICE_RECORD_HANDLE);
        return handle != null ? (int)handle.getLong() : 0;
    }

    /*
     * Sets service record handle.
     *
     * @param handle new service record handle value
     */
    public void setHandle(int handle) {
        Integer attrID = new Integer(SERVICE_RECORD_HANDLE);
        attributesTable.remove(attrID);
        attributesTable.put(attrID, new DataElement(
                DataElement.U_INT_4, handle));
        recHandle = handle;
    }

    /*
     * Returns notifier that has created this record.
     * @return corresponding notifier.
     */
    public BluetoothNotifier getNotifier() {
        return notifier;
    }

    /*
     * Creates attributes table and fills it up by values given.
     * @param attrIDs attributes IDs
     * @param attrValues attributes values
     */
    private void init(int[] attrIDs, DataElement[] attrValues) {
        attributesTable = new Hashtable(attrIDs.length + 1);
        attrsInit(attrIDs, attrValues);
    }

    /*
     * Fills up attributes table by values given.
     * @param attrIDs attributes IDs
     * @param attrValues attributes values
     */
    private void attrsInit(int[] attrIDs, DataElement[] attrValues) {
        for (int i = 0; i < attrIDs.length; i++) {
            attributesTable.put(new Integer(attrIDs[i]),
                    dataElementCopy(attrValues[i]));
        }
    }

    /*
     * Creates a copy of DataElement if it's necessary.
     * @param original data element to be copied if its type
     *        allows value modification
     * @return copy of data element
     */
    private DataElement dataElementCopy(DataElement original) {
        if ((original.getDataType() == DataElement.DATSEQ)
                || (original.getDataType() == DataElement.DATALT)) {
            DataElement copy = new DataElement(original.getDataType());
            Enumeration elements = (Enumeration) original.getValue();

            while (elements.hasMoreElements()) {
                copy.addElement(dataElementCopy((DataElement)
                        elements.nextElement()));
            }
            return copy;
        } else {
            return original;
        }
    }

    // JAVADOC COMMENT ELIDED
    public DataElement getAttributeValue(int attrID) {
        if ((attrID & MASK_OVERFLOW) != 0) {
            throw new IllegalArgumentException(
                    "attrID isn't a 16-bit unsigned integer");
        }
        DataElement attrValue = (DataElement) attributesTable.get(new
                Integer(attrID));

        if (attrValue == null) {
            return null;
        } else {
            return dataElementCopy(attrValue);
        }
    }

    // JAVADOC COMMENT ELIDED
    public RemoteDevice getHostDevice() {
        return remoteDevice;
    }

    // JAVADOC COMMENT ELIDED
    public synchronized int[] getAttributeIDs() {
        int[] attrIDs = new int[attributesTable.size()];
        Enumeration e = attributesTable.keys();

        for (int i = 0; i < attrIDs.length; i++) {
            attrIDs[i] = ((Integer) e.nextElement()).intValue();
        }
        return attrIDs;
    }

    // JAVADOC COMMENT ELIDED
    public synchronized boolean populateRecord(int[] attrIDs)
        throws IOException {
        Hashtable dupChecker = new Hashtable();
        Object checkObj = new Object();

        if (remoteDevice == null) {
            throw new RuntimeException("local ServiceRecord");
        }

        if (attrIDs.length == 0) {
            throw new IllegalArgumentException("attrIDs size is zero");
        }

        if (attrIDs.length > RETRIEVABLE_MAX) {
            throw new IllegalArgumentException(
                    "attrIDs size exceeds retrievable.max");
        }

        for (int i = 0; i < attrIDs.length; i++) {
            if ((attrIDs[i] & MASK_OVERFLOW) != 0) {
                throw new IllegalArgumentException("attrID does not represent "
                    + "a 16-bit unsigned integer");
            }

            // check attribute ID duplication
            if (dupChecker.put(new Integer(attrIDs[i]), checkObj) != null) {
                throw new IllegalArgumentException(
                        "duplicated attribute ID");
            }
        }

        // obtains transaction ID for request
        short transactionID = SDPClientTransactionBase.newTransactionID();

        // SDP connection and listener. They are initialized in try blok.
        SDPClient sdp = null;
        SRSDPListener listener = null;

        try {
            // prepare data for request
            DataElement handleEl = (DataElement) attributesTable.get(
                    new Integer(SERVICE_RECORD_HANDLE));
            int handle = (int) handleEl.getLong();

            // create and prepare SDP listner
            listener = new SRSDPListener();

            // create SDP connection and ..
            if (sdpClient == null) {
                sdp = ServiceDiscovererFactory.getServiceDiscoverer().
                        getSDPClient(remoteDevice.getBluetoothAddress());
            } else {
                sdp = sdpClient;
            }

            // ... and make request
            sdp.serviceAttributeRequest(handle, attrIDs, transactionID,
                    listener);

            synchronized (listener) {
                if ((listener.ioExcpt == null)
                        && (listener.attrValues == null)) {
                    try {
                        listener.wait();
                    } catch (InterruptedException ie) {
                        // ignore (breake waiting)
                    }
                }
            }
        } finally {

            // Closes SDP connection and frees transaction ID in any case
            SDPClientTransactionBase.freeTransactionID(transactionID);

            // if connection was created try to close it
            if (sdp != null) {
                try {
                    sdp.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }

        if (listener.ioExcpt != null) {
            throw listener.ioExcpt;
        } else if (listener.attrValues == null) {
            return false;
        } else if (listener.attrValues.length == 0) {
            return false;
        } else {
            attrsInit(listener.attrIDs, listener.attrValues);
            return true;
        }
    }

    // JAVADOC COMMENT ELIDED
    public synchronized String getConnectionURL(int requiredSecurity,
                                                boolean mustBeMaster) {
        // protocol, btaddr, port
        retrieveUrlCommonParams();
        if (protocol == BluetoothUrl.UNKNOWN) {
        	return null;
        }
        BluetoothUrl url = BluetoothUrl.createClientUrl(
                protocol, btaddr, port);

        if (mustBeMaster) {
            url.master = true;
        } else {
            url.master = false;
        }

        switch (requiredSecurity) {
        case NOAUTHENTICATE_NOENCRYPT:
            break;
        case AUTHENTICATE_ENCRYPT:
            url.encrypt = true;
        case AUTHENTICATE_NOENCRYPT:
            url.authenticate = true;
            break;
        default:
            throw new IllegalArgumentException("unsupported security type: "
                    + requiredSecurity);
        }

        return url.toString();
    }

    /*
     * Retrieves service protocol, device address and port (PSM or channel)
     * from service record attributes. Results are set to
     * <code>protocol</code>, <code>btaddr</code> and <code>port</code>
     * variables correspondingly.
     */
    private void retrieveUrlCommonParams() {
        if (protocol != BluetoothUrl.UNKNOWN) {
            // already retrieved
            return;
        }

        if (remoteDevice != null) {
            btaddr = remoteDevice.getBluetoothAddress();
        } else {
            try {
                btaddr = LocalDevice.getLocalDevice().getBluetoothAddress();
            } catch (BluetoothStateException bse) {
                throw new IllegalArgumentException("cannot generate url");
            }
        }

        /*
         * There are three protocols supported -
         * they are obex or rfcomm or l2cap. So, if obex is
         * found in ProtocolDescriptorList, the protocol is btgoep,
         * if RFCOMM is found (and no obex) - the btspp, otherwise
         * the protocol is btl2cap.
         */
        DataElement protocolList = getAttributeValue(PROTOCOL_DESCRIPTOR_LIST);
        if (protocolList == null) {
        	return;
        }
        Enumeration val = (Enumeration) protocolList.getValue();
        int type = -1; // 0 = l2cap, 1 = spp, 2 = obex
        final UUID L2CAP_UUID = new UUID(0x0100);
        final UUID RFCOMM_UUID = new UUID(0x0003);
        final UUID OBEX_UUID = new UUID(0x0008);

        // go through all of the protocols in the protocols list
        while (val.hasMoreElements()) {
            DataElement protoDE = (DataElement) val.nextElement();

            // application adds a garbage in protocolList - ignore
            if (protoDE.getDataType() != DataElement.DATSEQ) {
                continue;
            }
            Enumeration protoEnum = (Enumeration) protoDE.getValue();
            int tmpPort = -1;
            int tmpType = -1;

            // look on protocol details
            while (protoEnum.hasMoreElements()) {
                DataElement de = (DataElement) protoEnum.nextElement();

                // may be PSM or channel id
                if (de.getDataType() == DataElement.U_INT_1 ||
                        de.getDataType() == DataElement.U_INT_2)  {
                    tmpPort = (int) de.getLong();
                } else if (de.getDataType() == DataElement.UUID) {
                    UUID protoUUID = (UUID) de.getValue();

                    if (protoUUID.equals(L2CAP_UUID)) {
                        tmpType = 0;
                    } else if (protoUUID.equals(RFCOMM_UUID)) {
                        tmpType = 1;
                    } else if (protoUUID.equals(OBEX_UUID)) {
                        tmpType = 2;
                    }
                }
            }

            /*
             * ok, new protocol has been parsed - let's check if it
             * is over the previous one or not.
             *
             * Note, that OBEX protocol may appear before the RFCOMM
             * one - in this case the port (channel id) is not set -
             * need to check this case separately.
             */
            if (tmpType > type) {
                type = tmpType;

                // no "port" for obex type (obex = 2)
                if (tmpType != 2) {
                    port = tmpPort;
                }
            } else if (tmpType == 1) {
                port = tmpPort;
            }
        }

        switch (type) {
        case 0:
            protocol = BluetoothUrl.L2CAP;
            break;
        case 1:
            protocol = BluetoothUrl.RFCOMM;
            break;
        case 2:
            protocol = BluetoothUrl.OBEX;
            break;
        default:
            throw new IllegalArgumentException("wrong protocol list");
        }
    }

    /*
     * Retrieve service classes bits provided by corresponing service
     * at local device.
     *
     * @return an integer that keeps the service classes bits
     */
    public int getDeviceServiceClasses() {
        if (remoteDevice != null) {
            throw new RuntimeException(
                "This ServiceRecord was created by a call to "
                + "DiscoveryAgent.searchServices()");
        }

        // it's necessary to improve these code
        return serviceClasses;
    }

    // JAVADOC COMMENT ELIDED
    public synchronized void setDeviceServiceClasses(int classes) {
        // checks that it's service record from remote device
        if (remoteDevice != null) {
            throw new RuntimeException("This ServiceRecord was created"
                    + " by a call to DiscoveryAgent.searchServices()");
        }

        // checks correction of set classbits
        if ((classes & MASK_INCORRECT_CLASS) != 0) {
            throw new IllegalArgumentException("attempt to set incorrect bits");
        }
        serviceClasses = classes;
    }

    // JAVADOC COMMENT ELIDED
    public synchronized boolean setAttributeValue(
            int attrID, DataElement attrValue) {

        if ((attrID & MASK_OVERFLOW) != 0) {
            throw new IllegalArgumentException(
                    "attrID does not represent a 16-bit unsigned integer");
        }

        if (attrID == SERVICE_RECORD_HANDLE) {
            throw new IllegalArgumentException(
                    "attrID is the value of ServiceRecordHandle (0x0000)");
        }

        if (remoteDevice != null) {
            throw new RuntimeException(
                    "can't update ServiceRecord of the RemoteDevice");
        }
        Object key = new Integer(attrID);

        if (attrValue == null) {
            return attributesTable.remove(key) != null;
        } else {
            attributesTable.put(key, dataElementCopy(attrValue));
            return true;
        }
    }

    /*
     * SDP responce listener that is used within <code>populateRecord()</code>
     * processing.
     */
    class SRSDPListener implements SDPResponseListener {
        /* Attributes values retrieved form remote device. */
        DataElement[] attrValues = null;
        /* Keeps an IOException to be thrown. */
        IOException ioExcpt = null;
        /* IDs of attributes to be retrieved. */
        int[] attrIDs = null;

        /*
         * Receives error response.
         * @param errorCode error code
         * @param info error information
         * @param transactionID transaction ID
         */
        public void errorResponse(int errorCode, String info,
                int transactionID) {
            synchronized (this) {
                ioExcpt = new IOException(info);
                notify();
            }
        }

        /*
         * Implements required SDPResponseListener method,
         * must not be called.
         * @param handleList no matter
         * @param transactionID no matter
         */
        public void serviceSearchResponse(int[] handleList,
                int transactionID) {
            throw new RuntimeException("unexpected call");
        }

        /*
         * Receives arrays of service record attributes and their values.
         * @param attributeIDs list of attributes whose values were requested
         *        from server.
         * @param attributeValues values returned by server within.
         * @param transactionID ID of transaction response recieved within.
         */
        public void serviceAttributeResponse(int[] attributeIDs,
            DataElement[] attributeValues, int transactionID) {
            synchronized (this) {
                attrIDs = attributeIDs;
                attrValues = attributeValues;
                notify();
            }
        }

        /*
         * Implements required SDPResponseListener method,
         * must not be called.
         * @param attrIDs no matter
         * @param attributeValues no matter
         * @param transactionID no matter
         */
        public void serviceSearchAttributeResponse(int[] attrIDs,
            DataElement[] attributeValues, int transactionID) {
            throw new RuntimeException("unexpected call");
        }
    }
}
