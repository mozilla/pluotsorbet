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
 * JavaSDPClient class provides a client side of SDP connection as described in
 * Bluetooth Specification version 1.2.
 */
public class JavaSDPClient implements SDPClient{

	/* Transport connection for this client. */
    private SDPClientConnection connection = null;

    /* Bluetooth address this client is connected to. */
    private String address = null;

    private Hashtable ssTrnas = new Hashtable();
    
    private static final boolean DEBUG = false;
    
    /*
     * Constructs an <code>JavaSDPClient<code> object and opens SDP connection
     * to the remote device with the specified Bluetooth address.
     *
     * @param bluetoothAddress bluetooth address of SDP server
     */
    public JavaSDPClient(String bluetoothAddress) throws IOException {
        address = bluetoothAddress;
		try {
            connection = SDPClientConnection.getSDPClientConnection(address);
        } catch (IOException e) {
            //TODO add proper handling routines
            e.printStackTrace();
        }
    }
    
    /*
     * Closes connection of this client to the specified server.
     *
     * @throws IOException if no connection is open
     */
    public void close() throws IOException {
    	if (DEBUG) {
    		System.out.println("* JavaSDPClient: finishing transactions");
    	}
		Enumeration trs = (Enumeration) ssTrnas.keys();
        synchronized (ssTrnas) {
			while (trs.hasMoreElements()) {
				Object key = trs.nextElement();
				SDPClientTransaction tr = (SDPClientTransaction) ssTrnas
						.get(key);
				if (tr != null) {
					tr.finish();
				}
			}
			ssTrnas.clear();
		}
        if (DEBUG) {
			System.out.println("* JavaSDPClient: closing connection");
		}
		if (connection != null) {
        	connection.release();
        	connection = null;
        }
        if (DEBUG) {
			System.out.println("* JavaSDPClient: Canceling all receivers");
		}
		SDPClientReceiver.cancel();
        if (DEBUG) {
			System.out.println("* JavaSDPClient: closed");
		}
    }
    
    public void removeTransaction( String transID ) {
    	synchronized (ssTrnas) {
			ssTrnas.remove( transID );
		}
    }
    
    public SDPClientConnection getConnection() {
    	return connection;
    }
    
    /*
     * Initiates ServiceSearch transaction that is used to search for
     * services that have all the UUIDs specified on a server.
     */
    public void serviceSearchRequest(UUID[] uuidSet, int transactionID,
        SDPResponseListener listener) throws IOException {
    	SDPClientTransaction tr = null;
        tr = new ClientServiceSearchTransaction(this, transactionID, listener, uuidSet);
        synchronized (ssTrnas) {
        	ssTrnas.put(tr.getID(), tr);
		}
        tr.start();
    }

    /*
     * Initiates ServiceAttribute transaction that retrieves
     * specified attribute values from a specific service record.
     */
    public void serviceAttributeRequest(int serviceRecordHandle, int[] attrSet,
        int transactionID, SDPResponseListener listener) throws IOException {
    	SDPClientTransaction tr = new ClientServiceAttributeTransaction(this, transactionID, listener, serviceRecordHandle, attrSet);
        synchronized (ssTrnas) {
        	ssTrnas.put(tr.getID(), tr);
		}
        tr.start();
    }

    /*
     * Initiates ServiceSearchAttribute transaction that searches for services
     * on a server by UUIDs specified and retrieves values of specified
     * parameters for service records found.
     */
    public void serviceSearchAttributeRequest(int[] attrSet, UUID[] uuidSet,
        int transactionID, SDPResponseListener listener) throws IOException {
    	SDPClientTransaction tr = new ClientServiceSearchAttributeTransaction( this, transactionID, listener, attrSet, uuidSet );
        synchronized (ssTrnas) {
        	ssTrnas.put(tr.getID(), tr);
		}
        tr.start();
    }

    /*
     * Cancels transaction with given ID.
     */
    public boolean cancelServiceSearch(int transactionID) {
    	SDPClientTransaction tr = null;
    	boolean isCanceled = false;
    	synchronized (ssTrnas) {
    		String id = "" + transactionID + "_" + SDPClientTransaction.SDP_SERVICE_SEARCH_REQUEST;    		
			tr = (SDPClientTransaction)ssTrnas.get( id );
			ssTrnas.remove(id);
		}
		if (tr != null) {
			tr.cancel(SDPResponseListener.TERMINATED);
			isCanceled = true;
		}
    	return isCanceled;
    }

}
