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
public abstract class SDPClientTransaction extends SDPClientTransactionBase implements Runnable {

    protected static final boolean DEBUG = false;
    
    /* PDU ID (see Bluetooth Specification 1.2 Vol 3 page 131) */
    byte pduID;
    /* Transcation ID used to identify this transaction. */
    int ssTransID;
    /* Effective transaction ID. */
    int pduTransID;
    /* Length of all parameters. */
    long parameterLength;
    /* Continuation state used with partial responses. */
    byte[] continuationState = null;
    /* Listener to report request result to. */
    SDPResponseListener listener;
    /* Maps transaction IDs to ServiceTransaction objects. */
    private static Hashtable transactions = new Hashtable();

    protected JavaSDPClient client = null;
    
    public static SDPClientTransaction findTransaction( int pduTransactionID ) {
    	SDPClientTransaction result = null;
    	if (pduTransactionID > 0) {
    		synchronized (transactions) {
    			result = (SDPClientTransaction)transactions.get( new Integer( pduTransactionID ) );
    		}
    	}
    	return result;
    }
    
    /*
     * Cancels all current transactions. Called in case of I/O failure
     * in the underlying L2CAP connection.
     */
    public static void cancelAll(int reason) {
    	// force actual release of resources
        synchronized (transactions) {
			Enumeration e = transactions.keys();
			while (e.hasMoreElements()) {
				Integer id = (Integer) e
						.nextElement();
				if (id != null) {
					SDPClientTransaction tr = (SDPClientTransaction)transactions.get(id);
					if (tr != null) {
						tr.cancel(reason);
					}
				}
			}
			transactions.clear();
		}
    }
    
    /*
     * Class constructor.
     *
     * @param pduID protocol data unit ID
     * @param ssTransactionID transaction ID of the first request
     * @param listener listener object which will receive
     *                 completion and error notifications
     */
    public SDPClientTransaction(JavaSDPClient client, int pduID, int ssTransactionID,
        SDPResponseListener listener) {
        this.pduID = (byte)pduID;
        this.ssTransID = ssTransactionID;
        pduTransID = newTransactionID();
        this.listener = listener;
        this.client = client;
        if (DEBUG) {
        	System.out.println(" Transaction[" + getID() + "] created");
        }
    }

    /*
     * Updates the effective transaction ID with a new value.
     */
    private void updatePduTransactionID() {
        synchronized (transactions) {
			transactions.remove(new Integer(pduTransID));
			pduTransID = newTransactionID();
			transactions.put(new Integer(pduTransID), this);
		}
    }
    
    public String getID() {
    	return ("" + ssTransID + "_" + pduID);
    }

    /*
     * Starts this transaction.
     *
     * @throws IOException when an I/O error occurs
     */
    public void run() {
    	if (client == null) {
    		return;
    	}
        synchronized (transactions) {
        	transactions.put( new Integer( pduTransID ), this);
		}
        continuationState = null;
        SDPClientReceiver.start(client);
        try {
        	submitRequest();
        	if (DEBUG) {
        		System.out.println( "Transaction[" + getID() + "]: request sent. Waiting completion" );
        	}
        	synchronized (this) {
				wait();
			}
        } catch (InterruptedException ie) {
        	ie.printStackTrace();
        } catch (IOException ioe) {
        	ioe.printStackTrace();
        } finally {
            finish();
            if (DEBUG) {
            	System.out.println( "Transaction[" + getID() + "] ended" );
            }
        }
    }
    
    public void start() {
    	(new Thread(this)).start();
    }

    /*
     * Terminates this transaction and reports error to the listener.
     *
     * @param reason error code which will be reported
     */
    public void cancel(int reason) {
        listener.errorResponse(reason, "", ssTransID);
        finish();
        if (DEBUG) {
        	System.out.println( "Transaction[" + getID() + "]: canceled" );
        }
    }

    /*
     * Ends this transaction by unregistering it in the outer class.
     */
    public void finish() {
        synchronized (transactions) {
			transactions.remove(new Integer(pduTransID));
		}
        if (client != null) {
        	client.removeTransaction(getID());
        }
    	SDPClientReceiver.stop(client);
    	synchronized (this) {
			notify();
		}    	
    }

    /*
     * Reads error PDU, ends this transaction and reports listener the
     * error code retrieved.
     *
     * @param length length of PDU's parameters
     */
    public void error(int length) throws IOException {
        if (DEBUG) {
        	System.out.println( "Transaction[" + getID() + "] notify error" );
        }
        short errorCode = client.getConnection().getReaderWriter().readShort();
        byte[] infoBytes = client.getConnection().getReaderWriter().readBytes(length - 2);
        listener.errorResponse(errorCode, new String(infoBytes),
                ssTransID);
        finish();
    }

    /*
     * Completes the transaction by calling corresponding listener's
     * method with the data retrieved.
     */
    abstract void complete();

    /*
     * Writes transaction-specific parameters into the PDU.
     *
     * @throws IOException when an I/O error occurs
     */
    abstract void writeParameters() throws IOException;

    /*
     * Reads transaction-specific parameters from the PDU.
     *
     * @param length length of PDU's parameters
     * @throws IOException when an I/O error occurs
     */
    abstract void readParameters(int length) throws IOException;

    /*
     * Gets next SDP server response, if any, and passes it to the
     * corresponding listener. If a response is received, the transaction
     * it belongs to is stopped.
     *
     * @throws IOException if an I/O error occurs
     */
    boolean processResponse(byte pduID, short length ) throws IOException {
        boolean completed = false;
        try {
			synchronized (client.getConnection().readLock) {
			    if (DEBUG) {
			    	System.out.println("Transaction[" + getID() + "] pdu_id: " + pduID + "  response " + length + " bytes received. processing...");
			    }

			    if (pduID == SDPClientTransaction.SDP_ERROR_RESPONSE) {
			        if (DEBUG) {
			        	System.out.println("Transaction[" + getID() + "]: Error response received");
			        }
			        error(length);
			        return true;
			    }
			    if (DEBUG) {
			    	System.out.println("Transaction[" + getID() + "] read parameters...");
			    }
			    readParameters(length);
			    if (DEBUG) {
			    	System.out.println("Transaction[" + getID() + "] read continuation state...");
			    }
			    completed = readContinuationState();
			    if (DEBUG) {
			    	System.out.println("Transaction[" + getID() + "] request should" + (completed ? "n't" : "") + " be resubmitted");
			    }
			}
		    continueResponseProcessing();
		} finally {
			if (completed) {
				synchronized (this) {
					notify();
				}
				if (DEBUG) {
					System.out.println("Transaction[" + getID() + "] finished");
				}
			}
		}
        return completed;
}

/*
 * Processes this transaction by either re-submitting the original
 * request if the last response was incomplete, or providing the
 * listener with the results if the transaction was completed.
 *
 * @throws IOException when an I/O error occurs
 */
private void continueResponseProcessing() throws IOException {
    if (continuationState != null) {
        try {
            if (DEBUG) {
            	System.out.println("Transaction[" + getID() + "] resubmitt request");
            }
            resubmitRequest();
        } catch (IOException e) {
            if (DEBUG) {
            	System.out.println("Transaction[" + getID() + "] error: " + e);
            }
            cancel(SDPResponseListener.IO_ERROR);
            throw e;
        }
    } else {
        if (DEBUG) {
        	System.out.println( "Transaction[" + getID() + "] notify completed" );
        }
        complete();
    }
}

/*
 * Re-submits the original request with continuation state
 * data received with the incomplete response.
 *
 * @throws IOException when an I/O error occurs
 */
private void submitRequest() throws IOException {
    synchronized (client.getConnection().writeLock) {
    	client.getConnection().getReaderWriter().writeByte(pduID);
    	client.getConnection().getReaderWriter().writeShort((short)pduTransID);
    	client.getConnection().getReaderWriter().writeShort((short)(parameterLength + 1));
        writeParameters();
        if (continuationState != null) {
        	client.getConnection().getReaderWriter().writeByte((byte)continuationState.length);
        	client.getConnection().getReaderWriter().writeBytes(continuationState);
        } else {
            client.getConnection().getReaderWriter().writeByte((byte)0x00);
        }
        client.getConnection().getReaderWriter().flush();
        if (DEBUG) {
        	System.out.println("Transaction[" + getID() + "] request " + parameterLength + 1 + " bytes sent");
        }
    }
}

private void resubmitRequest() throws IOException {
    updatePduTransactionID();
    submitRequest();
}

/*
 * Extracts continuation state parameter.
 *
 * @return true if the continuation state is present,
 *         false otherwise
 * @throws IOException when an I/O error occurs
 */
private boolean readContinuationState() throws IOException {
    byte infoLength = client.getConnection().getReaderWriter().readByte();
    if (infoLength == 0) {
        continuationState = null;
    } else {
        continuationState = client.getConnection().getReaderWriter().readBytes(infoLength);
    }
    return (infoLength == 0);
}
    
}
