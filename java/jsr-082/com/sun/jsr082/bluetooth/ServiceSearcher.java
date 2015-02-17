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
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;

import com.sun.jsr082.bluetooth.JavaSDPClient;

import java.util.Hashtable;
import java.util.Vector;

/*
 * This class saves information about responses to SDP_ServiceSearchRequest and
 * SDP_ServiceAttributeRequest requests and provides functionality of
 * DiscoveryAgent.serviceSearch using multiple requests via JavaSDPClient (Service
 * Discovery Protocol)
 * 
 */
public class ServiceSearcher extends ServiceSearcherBase implements
        ServiceDiscoverer {

    /* Set to false in RR version - then the javac skip the code. */
    private static final boolean DEBUG = false;

    /* this class name for debug. */
    private static final String cn = "ServiceSearcher";

    /* ID of transaction this listener works in. */
    private short transactionID;

    /*
     * Listens for service discovery and is informed as a service is discovered.
     */
    private DiscoveryListener discListener;

    /* SDP client to send requests. */
    private JavaSDPClient sdp;

    /* Service records handles retrieved from a server response. */
    private int[] handles;

    /* Number of service records handles processed. */
    private int processedHandle;

    /* Indicates if this listener is inactive. */
    private boolean inactive = false;

    /* Indicates if service search has been canceled. */
    private boolean canceled = false;

    /* Indicates if listener notification has been called. */
    private boolean notified = false;

    /* Current quantity of service discovering requests. */
    private static int requestCounter = 0;

    /* Keeps the references of current service search requests. */
    private static Hashtable searchers = new Hashtable();

    /*
     * Creates ServiceSearcher and save all required info in it.
     * 
     */
    public ServiceSearcher() {

    }

    /*
     * Returns an <code>JavaSDPClient<code> object and opens SDP connection
     * to the remote device with the specified Bluetooth address.
     *
     * @param bluetoothAddress bluetooth address of SDP server
     */
    public SDPClient getSDPClient(String bluetoothAddress) {
        try {
            sdp = new JavaSDPClient(bluetoothAddress);
        } catch (IOException ioe) {
            
        }
        return (SDPClient)sdp;
    }

    /*
     * Start searching services under the given conditions
     * 
     * @param attrSet
     *            list of attributes whose values are requested.
     * @param uuidSet
     *            list of UUIDs that indicate services relevant to request.
     * @param btDev
     *            remote Bluetooth device to listen response from.
     * @param discListener
     *            discovery listener.
     */
    public int searchService(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev,
            DiscoveryListener discListener) throws BluetoothStateException,
            IllegalArgumentException {

        if (DEBUG) {
            System.out.println("- serviceSearcher: initializing");
        }
        initialize(attrSet, uuidSet, btDev);

        if (discListener == null) {
            throw new NullPointerException("DiscoveryListener is null");
        }
        this.discListener = discListener;

        return start();
    }

    /*
     * Starts SDP_ServiceSearchRequest.
     * 
     * @see JavaSDPClient#serviceSearchRequest
     * 
     * @return ID of transaction that has been initiated by the request.
     */
    private int start() throws BluetoothStateException {
        if (DEBUG) {
            System.out.println("- serviceSearcher: start");
        }
        synchronized (ServiceSearcher.class) {
            if (requestCounter == ServiceRecordImpl.TRANS_MAX) {
                throw new BluetoothStateException(
                        "Too much concurrent requests");
            }
            requestCounter++;
        }
        transactionID = SDPClientTransaction.newTransactionID();
        searchers.put(new Integer(transactionID), this);
        synchronized (this) {
            notified = false;
        }

        handles = null;
        processedHandle = 0;
        try {
            sdp = new JavaSDPClient(btDev.getBluetoothAddress());
            sdp.serviceSearchAttributeRequest(attrSet, uuidSet, transactionID,
                    this);
            // sdp.serviceSearchRequest(uuidSet, transactionID, this);
        } catch (IOException ioe) {
            if (DEBUG) {
                ioe.printStackTrace();
            }

            synchronized (this) {
                stop();
                new NotifyListenerRunner(
                        DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE);
            }
        }

        if (DEBUG) {
            System.out.println("- serviceSearch: started with transaction: "
                    + transactionID);
        }
        return transactionID;
    }

    /*
     * Receives SDP_ErrorResponse and completes the search request activity by
     * error reason.
     * 
     * @param errorCode
     *            error code form SDP_ErrorResponse.
     * @param info
     *            error details firm SDP_ErrorResponse.
     * @param transactionID
     *            ID of transaction response got within.
     */
    public void errorResponse(int errorCode, String info, int transactionID) {
        if (DEBUG) {
            System.out.println(cn + ".errorResponse: called");
        }

        stop();

        if ((errorCode == SDP_INVALID_VERSION)
                || (errorCode == SDP_INVALID_SYNTAX)
                || (errorCode == SDP_INVALID_PDU_SIZE)
                || (errorCode == SDP_INVALID_CONTINUATION_STATE)
                || (errorCode == SDP_INSUFFICIENT_RESOURCES)) {
            notifyListener(DiscoveryListener.SERVICE_SEARCH_ERROR);
            System.err.println(info);
        } else if (errorCode == SDP_INVALID_SR_HANDLE) {
            notifyListener(DiscoveryListener.SERVICE_SEARCH_NO_RECORDS);
            System.err.println(info);
        } else if (errorCode == IO_ERROR) {
            notifyListener(DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE);
        } else if (errorCode == TERMINATED) {
            new NotifyListenerRunner(
                    DiscoveryListener.SERVICE_SEARCH_TERMINATED);
        }
    }

    /*
     * Receives array of handles retrieved form SDP_serviceSearchResponse.
     * 
     * @param handleList
     *            service record handles retrieved from
     *            SDP_srviceSearchResponse.
     * @param transactionID
     *            ID of transaction response has been received in.
     */
    public void serviceSearchResponse(int[] handleList, int transactionID) {
        if (DEBUG) {
            System.out.println(cn + ".serviceSearchResponse: called");
        }

        // there is no reason to perform response processing if search
        // is canceled
        if (isCanceled()) {
            return;
        }

        if (handleList == null || handleList.length == 0) {
            stop();
            notifyListener(DiscoveryListener.SERVICE_SEARCH_NO_RECORDS);
            return;
        }

        synchronized (this) {
            handles = handleList;
            processedHandle = 0;
        }

        try {
            // there is no reason to request service attributes if service
            // search is canceled
            if (isCanceled()) {
                return;
            }
            sdp.serviceAttributeRequest(handles[processedHandle], attrSet,
                    transactionID, this);
        } catch (IOException ioe) {
            if (DEBUG) {
                ioe.printStackTrace();
            }
            stop();
            notifyListener(DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE);
        }
    }

    /*
     * Receives arrays of service record attributes and their values retrieved
     * from server response.
     */
    public void serviceAttributeResponse(int[] attrIDs,
            DataElement[] attributeValues, int transactionID) {
        if (DEBUG) {
            System.out.println(cn + ".serviceAttributeResponse: called");
        }

        // there is no reason to process service attributes if service
        // search is canceled
        if (isCanceled()) {
            return;
        }

        synchronized (this) {
            processedHandle++;
        }

        if (attributeValues != null) {
            ServiceRecordImpl[] serviceRecordSet = new ServiceRecordImpl[1];
            serviceRecordSet[0] = new ServiceRecordImpl(btDev, attrIDs,
                    attributeValues);
            try {
                // The spec for DiscoveryAgent.cancelServiceSearch() says:
                // "After receiving SERVICE_SEARCH_TERMINATED event,
                // no further servicesDiscovered() events will occur
                // as a result of this search."
                if (isCanceled()) {
                    return;
                }
                System.out.println("serviceSearch: notify serviceDiscovered");
                discListener.servicesDiscovered(this.transactionID,
                        serviceRecordSet);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (processedHandle == handles.length) {
            stop();
            if (DEBUG) {
                System.out
                        .println("serviceSearch: notify service search completed");
            }
            notifyListener(DiscoveryListener.SERVICE_SEARCH_COMPLETED);
            return;
        }

        try {
            // there is no reason to continue attributes discovery if search
            // is canceled
            if (isCanceled()) {
                return;
            }
            sdp.serviceAttributeRequest(handles[processedHandle], attrSet,
                    transactionID, this);
        } catch (IOException ioe) {
            if (DEBUG) {
                ioe.printStackTrace();
            }
            stop();
            notifyListener(DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE);
        }
    }

    /*
     * Base class method not relevant to this subclass, must never be called.
     */
    public void serviceSearchAttributeResponse(int[] attrIDs,
            DataElement[] attributeValues, int transactionID) {
        if (DEBUG) {
            System.out.println(cn + ".serviceSearchAttributeResponse: called");
        }
        // there is no reason to process service attributes if service
        // search is canceled
        if (isCanceled()) {
            return;
        }

        if (attrIDs == null || attrIDs.length <= 0 || attributeValues == null
                || attrIDs.length != attributeValues.length) {
            notifyListener(DiscoveryListener.SERVICE_SEARCH_NO_RECORDS);
            return;
        }
        int firstPos = 0;
        Vector responceRecords = new Vector();
        for (int i = 1; i < attrIDs.length; i++) {
            if (attrIDs[i] == 0) {
                responceRecords.addElement(getOneRecord(attrIDs,
                                attributeValues, firstPos, i));
                firstPos = i;
            }
        }
        responceRecords.addElement(getOneRecord(attrIDs,
                        attributeValues, firstPos, attrIDs.length));

        ServiceRecordImpl[] records = new ServiceRecordImpl[responceRecords.size()];
        for (int i=0; i<responceRecords.size(); i++) {
            records[i]=(ServiceRecordImpl)responceRecords.elementAt(i);
        }

        try {
            // The spec for DiscoveryAgent.cancelServiceSearch() says:
            // "After receiving SERVICE_SEARCH_TERMINATED event,
            // no further servicesDiscovered() events will occur
            // as a result of this search."
            if (isCanceled()) {
                return;
            }
            if (DEBUG) {
                System.out.println("serviceSearch: notify serviceDiscovered");
            }
            discListener.servicesDiscovered(this.transactionID, records);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        stop();
        if (DEBUG) {
            System.out
                    .println("serviceSearch: notify service_search_completed");
        }
        notifyListener(DiscoveryListener.SERVICE_SEARCH_COMPLETED);
        return;
    }

    /*
     * Returns one ServiceRecord from the incoming attributes list
     */
    private ServiceRecordImpl getOneRecord(int[] attrIDs,
            DataElement[] attributeValues, int firstPos, int curPos) {

        int aLength = curPos - firstPos;
        int[] aIDs = new int[aLength];
        DataElement[] aValues = new DataElement[aLength];
        System.arraycopy(attrIDs, firstPos, aIDs, 0, aLength);
        System.arraycopy(attributeValues, firstPos, aValues, 0, aLength);
        ServiceRecordImpl record = 
                          new ServiceRecordImpl(btDev, aIDs, aValues);
        return record;
    }

    /*
     * Finishes the service searcher activity.
     */
    private void stop() {
        JavaSDPClient sdp;
        searchers.remove(new Integer(transactionID));

        synchronized (this) {
            if (this.sdp == null) {
                return;
            }
            inactive = true;
            sdp = this.sdp;
            this.sdp = null;
        }

        synchronized (ServiceSearcher.class) {
            requestCounter--;
        }

        try {
            sdp.close();
        } catch (IOException ioe) {
            if (DEBUG) {
                ioe.printStackTrace();
            }
            // ignore
        }
    }

    /*
     * Cancels current transaction.
     * 
     * @return false if there is no current transaction, cancels it and returns
     *         true otherwise.
     */
    boolean cancel() {
        synchronized (this) {
            if (inactive) {
                return false;
            }
            inactive = true;

            if (sdp == null) {
                return false;
            }

            if (canceled) {
                return false;
            }
            canceled = true;
        }

        // cancel running effective transaction if any.
        // if sdp.cancelServiceSearch returns false (there is no running
        // transactions) then call the notification directly.
        if (!sdp.cancelServiceSearch(transactionID)) {
            new NotifyListenerRunner(
                    DiscoveryListener.SERVICE_SEARCH_TERMINATED);
        }

        return true;
    }

    /*
     * Determines whether the service search has been canceled by the
     * application and did not complete.
     * 
     * @return <code>true</code> indicates the service search has been
     *         canceled; <code>false</code> the application has not called
     *         cancel operation
     */
    private boolean isCanceled() {
        return canceled;
    }

    /*
     * Cancels transaction with given ID.
     * 
     * @param transactionID
     *            ID of transaction to be cancelled.
     * 
     * @return false if there is no open transaction with ID given, true
     *         otherwise.
     */
    public boolean cancel(int transactionID) {
        ServiceSearcher carrier = (ServiceSearcher) searchers.get(new Integer(
                transactionID));

        if (carrier == null) {
            return false;
        } else {
            return carrier.cancel();
        }
    }

    /*
     * Notifies the listener that service search has been completed with
     * specified response code.
     * 
     * @param respCode
     *            response code.
     */
    private void notifyListener(int respCode) {
        // guard against multiple notification calls
        synchronized (this) {
            if (!notified) {
                notified = true;
            } else {
                return;
            }
        }

        if (DEBUG) {
            String codeStr = "Undefined";

            switch (respCode) {
            case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
                codeStr = "SERVICE_SEARCH_COMPLETED";
                break;

            case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
                codeStr = "SERVICE_SEARCH_DEVICE_NOT_REACHABLE";
                break;

            case DiscoveryListener.SERVICE_SEARCH_ERROR:
                codeStr = "SERVICE_SEARCH_ERROR";
                break;

            case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
                codeStr = "SERVICE_SEARCH_NO_RECORDS";
                break;

            case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
                codeStr = "SERVICE_SEARCH_TERMINATED";
                break;
            default:
            }
            if (DEBUG) {
                System.out.println("serviceSearchCompleted:");
                System.out.println("\ttransID=" + transactionID);
                System.out.println("\trespCode=" + codeStr);
                System.out.println("\tinactive=" + inactive);
            }
        }

        try {
            discListener.serviceSearchCompleted(transactionID, respCode);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /*
     * Runnable for launching <code>notifyListener()</code> in a separate
     * thread.
     * 
     * @see #notifyListener(int)
     */
    class NotifyListenerRunner implements Runnable {
        /* Response code to pass to a listener. */
        int respCode;

        /*
         * Constructs Runnable instance to pass single response code, starts a
         * thread for that.
         * 
         * @param respCode
         *            response code value
         */
        NotifyListenerRunner(int respCode) {
            this.respCode = respCode;
            new Thread(this).start();
        }

        /*
         * The <code>run()</code> method.
         * 
         * @see java.lang.Runnable
         */
        public void run() {
            notifyListener(respCode);
        }
    }
}
