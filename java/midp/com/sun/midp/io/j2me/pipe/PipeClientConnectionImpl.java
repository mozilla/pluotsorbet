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
package com.sun.midp.io.j2me.pipe;

import com.sun.midp.io.j2me.pipe.serviceProtocol.PipeServiceProtocol;
import com.sun.midp.io.ConnectionBaseAdapter;
import com.sun.midp.io.pipe.PipeConnection;
import com.sun.midp.links.ClosedLinkException;
import com.sun.midp.links.Link;
import com.sun.midp.links.LinkMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import javax.microedition.io.Connection;
import com.sun.midp.security.SecurityToken;
import java.util.Vector;
import javax.microedition.io.Connector;

/**
 * Implementation of PipeConnection interface. Uses Links as bearer. Uses
 * com.sun.midp.io.j2me.pipe.serviceProtocol.* for organazing messaging over
 * bearer.
 */
class PipeClientConnectionImpl extends ConnectionBaseAdapter implements PipeConnection {

    private static final boolean DEBUG = false;
    private static final String CLOSE_OUTPUT_COMMAND = "closeOutputStream";
    private PipeServiceProtocol pipe;
    private SecurityToken token;
    private Object suiteId;
    private String serverName;
    private String version;
    private Link sendLink;
    private Link receiveLink;
    private Thread receiverThread;
    private Receiver receiver;
    private Vector receiveQueue = new Vector(1);
    private int topReceivedMsgOffset;
    private IOException receiveStatus = new IOException();
    private int receiveQueueByteCount;
    private boolean receivedEOF;

    PipeClientConnectionImpl(SecurityToken token, PipeServiceProtocol pipe) {
        this.pipe = pipe;
        this.token = token;
        serverName = pipe.getServerName();
        version = pipe.getServerVersionRequested();
    }

    PipeClientConnectionImpl(Object suiteId, String serverName, String version, SecurityToken token) {
        this.token = token;
        this.suiteId = suiteId;
        this.serverName = serverName;
        this.version = version;
    }

    void establishTransfer(int mode) throws IOException {
        receiveLink = pipe.getInboundLink();
        sendLink = pipe.getOutboundLink();

        initStreamConnection(mode);

        if (mode == Connector.READ || mode == Connector.READ_WRITE) {
            receiver = new Receiver();
            receiverThread = new Thread(receiver);
            receiveStatus = null;
            receiverThread.start();
        }

    }

    public InputStream openInputStream() throws IOException {
        InputStream is = super.openInputStream();

        return is;
    }

    protected void notifyClosedInput() {
        if (DEBUG)
            debugPrint("input closed");

        super.notifyClosedInput();

        receiveLink.close();

        if (receiveStatus == null) {
            // input was closed by application, not because of receiver failure
            receiveStatus = new IOException();
        }
    }

    protected void notifyClosedOutput() {
        if (DEBUG)
            debugPrint("output closed");

        super.notifyClosedOutput();

        sendLink.close();
    }

    void establish(int mode) throws IOException {
        pipe = PipeServiceProtocol.getService(token);
        pipe.bindClient(serverName, version);

        establishTransfer(mode);
    }

    public Connection openPrim(String name, int mode, boolean timeouts) throws IOException {
        throw new IOException("This method should not be called because it should not exist. Please refactor");
    }

    protected void disconnect() throws IOException {
        if (DEBUG)
            debugPrint("disconnected");

        synchronized (receiver) {
            receiver.notify();
        }
    }

    public int available() throws IOException {

        if (DEBUG)
            debugPrint("available " + receiveQueueByteCount + " bytes");

        return receiveQueueByteCount;
    }

    protected synchronized int readBytes(byte[] b, int off, int len) throws IOException {
        if (DEBUG)
            debugPrint("readBytes len=" + len + ", can read " + (iStreams > 0));

        if (iStreams == 0) {
            if (DEBUG)
                debugPrint("readBytes input closed. isEOF " + receivedEOF +
                        ", status " + receiveStatus);

            if (receivedEOF)
                return -1;

            throw receiveStatus;
        }

        int originalOffset = off;
        synchronized (receiver) {

            while (len > 0) {
                if (receiveQueue.size() == 0) {
                    // need more bytes, check if receiver is running
                    if (receiveStatus == null && !receivedEOF) {
                        // fine, now wait for data to come
                        if (DEBUG)
                            debugPrint("readBytes: waiting for Receiver");
                        try {
                            receiver.wait();
                        } catch (InterruptedException ex) {
                            try {
                                closeInputStream();
                            } catch (IOException iOException) {
                                // ignore
                            }
                            throw new InterruptedIOException(ex.toString());
                        }
                    }

                    // check receiver status once more finish processing if no more
                    // data could be obtained
                    if (receiveStatus != null || receivedEOF) {
                        if (DEBUG)
                            debugPrint("readBytes: Receiver finshed with " + receiveStatus);
                        // we've got receiver stopped and no more data. close input
                        try {
                            closeInputStream();
                        } catch (IOException iOException) {
                            // ignore
                        }

                        if (receivedEOF) {
                            break;
                        } else {
                            throw receiveStatus;
                        }
                    }
                }

                // get bytes from receive queue
                byte[] bufferedMsg = (byte[]) receiveQueue.elementAt(0);
                int chunkSize = bufferedMsg.length - topReceivedMsgOffset;
                if (DEBUG)
                    debugPrint("readBytes: fetching data. " + chunkSize + " bytes available in message");
                if (chunkSize >= len) {
                    chunkSize = len;
                }
                System.arraycopy(bufferedMsg, topReceivedMsgOffset, b, off, chunkSize);
                topReceivedMsgOffset += chunkSize;
                if (topReceivedMsgOffset == bufferedMsg.length) {
                    receiveQueue.removeElementAt(0);
                    topReceivedMsgOffset = 0;
                }
                if (DEBUG)
                    debugPrint("readBytes len=" + len + ", read " + chunkSize);
                len -= chunkSize;
                off += chunkSize;
                receiveQueueByteCount -= chunkSize;

                if (receiveQueue.size() == 0) {
                    // check if we've provided requested data
                    // readBytes should block only if no data is available
                    // if single byte has been read it should return

                    break;
                }
            }
        }


        if (DEBUG)
            debugPrint("readBytes: read " + (off - originalOffset) + " bytes");

        int bytesRead = off - originalOffset;

        return bytesRead == 0 ? -1 : bytesRead;
    }

    private void debugPrint(String msg) {
        System.out.println("[pipe client conn " + Integer.toHexString(hashCode()) + "] " + msg);
    }

    protected int writeBytes(byte[] b, int off, int len) throws IOException {
        if (DEBUG)
            debugPrint("writeBytes len=" + len + " can write " + (oStreams > 0));

        if (oStreams == 0)
            throw new IOException();

        if (len == 0)
            return 0;

        LinkMessage lm = LinkMessage.newDataMessage(b, off, len);
        if (DEBUG)
            debugPrint("writeBytes: sending message");
        sendLink.send(lm);

        if (DEBUG)
            debugPrint("writeBytes: wrote " + len + " bytes");
        return len;
    }

    protected void closeOutputStream() throws IOException {
        if (sendLink.isOpen()) {
            try {
                LinkMessage lm = LinkMessage.newStringMessage(CLOSE_OUTPUT_COMMAND);
                sendLink.send(lm);
            } catch (IOException ex) {
                // ignore
            }
        }
        super.closeOutputStream();
    }

    public String getRequestedServerVersion() {
        return version;
    }

    public String getServerName() {
        return serverName;
    }

    private class Receiver implements Runnable {

        public void run() {

            while (!receivedEOF && receiveStatus == null) {
                LinkMessage lm;
                byte[] data = null;
                try {
                    if (DEBUG)
                        debugPrint("Receiver waiting");
                    lm = receiveLink.receive();
                    if (DEBUG)
                        debugPrint("Receiver got message");

                    if (lm.containsString()) {
                        String command = lm.extractString();
                        if (CLOSE_OUTPUT_COMMAND.equals(command)) {
                            receivedEOF = true;
                        } else {
                            // we should never get here but for the sake of consistency let's handle
                            // this case
                            receiveStatus = new IOException("Unsupported: " + command);
                        }
                    } else {
                        data = lm.extractData();
                    }

                } catch (IOException iOException) {
                    if (DEBUG)
                        debugPrint("Receiver got exception " + iOException);
                    receiveStatus = iOException;
                    if (receiveStatus instanceof ClosedLinkException)
                        receivedEOF = true;
                }

                synchronized (this) {
                    if (data != null) {
                        receiveQueue.addElement(data);
                        receiveQueueByteCount += data.length;
                    }
                    notify();
                }
            }
            if (DEBUG)
                debugPrint("Receiver finished");
        }
    }
}
