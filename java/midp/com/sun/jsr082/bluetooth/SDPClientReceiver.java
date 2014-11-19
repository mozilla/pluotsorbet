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

public class SDPClientReceiver implements Runnable {

	private static Hashtable receivers = new Hashtable();
	private static int instanceCount = 0;
	private JavaSDPClient client = null;
	private static final boolean DEBUG= false;
	
    /*
     * Identifies if receiving thread is running (false) or not (true).
     */
    private Thread receiverThread = null;
	
	protected SDPClientReceiver(JavaSDPClient client) {
		this.client = client;
		receiverThread = new Thread(this);
		receiverThread.start();
		if (DEBUG) {
			System.out.println("# Receiver internal thread started");
		}
	}
	
	public static synchronized void start(JavaSDPClient client) {
		SDPClientReceiver receiver = (SDPClientReceiver)receivers.get(client);		
		if (receiver == null) {
			receiver = new SDPClientReceiver(client);
            receivers.put(client, receiver);
			instanceCount = 1;
		} else {
			instanceCount++;
		}
		if (DEBUG) {
			System.out.println("# Receiver[" + instanceCount + "] started");
		}
	}
	
	public static synchronized void stop(JavaSDPClient client) {
		SDPClientReceiver receiver = (SDPClientReceiver)receivers.get(client);	
		if (receiver == null) {
			return;
		}
		if (DEBUG) {
			System.out.println("# Receiver[" + instanceCount + "] stopped");
		}
		if (--instanceCount > 0) {
			return;
		} else {
			receiver.finish(client.getConnection());
			receivers.remove(client);
		}
	}
            
    /* Cancels receiving responses. */
    public static synchronized void cancel() {
    	Enumeration clientRefs = receivers.keys();
    	while( clientRefs.hasMoreElements() ) {
    		Object ref = clientRefs.nextElement();
    		if (ref != null) {
    			SDPClientReceiver rvr = (SDPClientReceiver)receivers.get(ref);
                Thread tmp = rvr.receiverThread;
    			rvr.finish(((JavaSDPClient)ref).getConnection());
                try {
                    tmp.join();
                } catch (InterruptedException ie) {
                }
                if (DEBUG) { 
                	System.out.println("# Receiver internal thread stopped");
                }
                    
    		}
    	}
    	receivers.clear();
		if (DEBUG) {
			System.out.println("# Receiver[all] canceled");
		}
    }
    
    protected synchronized void finish(SDPClientConnection conn) {
		if (receiverThread != null) {
			Thread tmp = receiverThread;
			receiverThread = null;
			if (conn != null) {
				conn.release();
				if (DEBUG) {
					System.out.println("# Receiver: Connection released");
				}
			}
		}
    }

    /*
     * The <code>run()</code> method.
     *
     * @see java.lang.Runnable
     */
    public void run() {
    	Thread current = Thread.currentThread();
        while (client != null && client.getConnection() != null && current == receiverThread ) {
            try {
            	byte pduID = client.getConnection().getReaderWriter().readByte();
            	short transID = client.getConnection().getReaderWriter().readShort();
            	short length = client.getConnection().getReaderWriter().readShort();
            	SDPClientTransaction trans = SDPClientTransaction.findTransaction(transID);
            	if (trans != null) {
            		if (DEBUG) {
            			System.out.println( "# Receiver processResponse:" + trans.getID() );
            		}
            		trans.processResponse(pduID, length);
            	} else {
                    if (DEBUG) {
                    	System.out.println("#Receiver transaction: " + transID + " not found");
                    }
                    // transaction we are not aware of; skip this pdu
                	client.getConnection().getReaderWriter().readBytes(length);
                	if (current == receiverThread) {
                		throw new IOException("Invalid transaction id: " + transID);
                	}
            	}
            } catch (IOException ioe) {
            	if (current == receiverThread) {
            		SDPClientTransaction.cancelAll(SDPResponseListener.IO_ERROR);
            	}
            }
        }
        receiverThread = null;
		if (DEBUG) {
			System.out.println( "# Receiver internal thread exit" );
		}
    }
}
