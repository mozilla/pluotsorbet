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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.bluetooth.DataElement;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.UUID;

public class SDPClientConnection {
    /* L2CAP URL starting string. */
    private static final String SDP_L2CAP_URL_BEGIN = "//";

    /* L2CAP URL trailing string. */
    private static final String SDP_L2CAP_URL_END = ":0001";
    
    private static Hashtable connections = new Hashtable();
	
	/* Bluetooth address of the server. */
    private String address;

    /*
     * Reference counter keeps the number of SDP connections which
     * use this transport. When this value reaches zero, the L2CAP
     * connection is closed and the transport is removed from the global
     * SDPClient.transportStorage hashtable.
     */
    private int refCount = 0;

    /* The L2CAP (logical link) connection. */
    private L2CAPConnection connection;

    /*
     * Object that performs reading from and writing to L2CAP connection.
     */
    private DataL2CAPReaderWriter rw;

    /* Lock for synchronizing reading from connection. */
    public Object readLock = new Object();

    /* Lock for synchronizing writing to connection. */
    public Object writeLock = new Object();


    /*
     * Constructs <code>SDPClientConnection</code> instance that reflects transport
     * connections to the specified server.
     *
     * @param bluetoothAddress bluetooth address of the server
     */
    protected SDPClientConnection(String bluetoothAddress) throws IOException {
        address = bluetoothAddress;
        connection = (L2CAPConnection)SDP.getL2CAPConnection(
            SDP_L2CAP_URL_BEGIN + bluetoothAddress + SDP_L2CAP_URL_END);
        rw = new DataL2CAPReaderWriter(connection);
    }
    
    public static SDPClientConnection getSDPClientConnection( String bluetoothAddress ) throws IOException {
    	SDPClientConnection result = null;
    	synchronized (connections) {
			result = (SDPClientConnection)connections.get(bluetoothAddress);
			if( result == null ) {
				result = new SDPClientConnection( bluetoothAddress );
				connections.put(bluetoothAddress, result);
				result.addRef();
			}
		}
    	return result;
    }
    
    public static void closeAll() {
    	synchronized (connections) {
    		Enumeration addrs = connections.keys();
    		while ( addrs.hasMoreElements() ) {
    			String addr = (String)addrs.nextElement();
    			if (addr != null) {
    				SDPClientConnection conn = (SDPClientConnection)connections.get(addr);
    				if (conn != null) {
    					conn.release();
    				}
    			}
    		}
    		connections.clear();
		}
    }

    /*
     * Increases reference counter. This object and the underlying L2CAP
     * connection will live while the counter is positive.
     */
    protected synchronized void addRef() {
        refCount++;
    }

    /*
     * Decreases reference counter. If the counter becomes equal to zero,
     * L2CAP connection is closed and the transport is removed from the
     * global SDPClient.transportStorage hashtable.
     */
    public synchronized void release() {
        refCount--;
        if (refCount <= 0) {
            try {
                connection.close();
            } catch (IOException e) {
                // just ignore it, we're done with this object anyway
            }
            synchronized (connections) {
				connections.remove(this.address);
			}
        }
    }
    
    public DataL2CAPReaderWriter getReaderWriter() {
    	return rw;
    }

    public L2CAPConnection getL2CAPConnection() {
    	return connection;
    }


}
