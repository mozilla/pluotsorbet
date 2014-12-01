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
package com.sun.jsr082.obex;

import javax.obex.Authenticator;
import javax.obex.ServerRequestHandler;
import javax.obex.ResponseCodes;
import javax.obex.HeaderSet;
import java.io.IOException;
import javax.microedition.io.Connection;

class ServerConnectionImpl extends ObexPacketStream
        implements Connection, Runnable {

    /* Debug information, should be false for RR. */
    private static final boolean DEBUG = false;

    ServerRequestHandler handler;

    private int owner;
    private boolean isConnected;

    private long connId;

    /* Current operation header size overflow flag. */
    boolean operationHeadersOverflow = false;

    /* Current operation state. */
    boolean operationClosed;

    void headerTooLarge() throws IOException {
        operationHeadersOverflow = true;
        operationClosed = true;
    }

    ServerConnectionImpl(ObexTransport transport,
            ServerRequestHandler handler, Authenticator auth)
            throws IOException {
        super(transport);
        this.handler = handler;
        isClient = false;
        authenticator = auth;

        // owner field of all created HeaderSets
        owner = HeaderSetImpl.OWNER_SERVER;
        new Thread(this).start();
    }

    public void run() {
        try {
            while (processRequest()) {
                // if connection closed
                if (isClosed()) {
                    break;
                }
            }
        } catch (Throwable t) {
            if (DEBUG) {
                System.out.println("ServerConnectionImpl thread exception");
                t.printStackTrace();
            }
        }
        if (DEBUG) {
            System.out.println("ServerConnectionImpl: client disconnected");
        }
        close();
    }

    /*
     * Modified sendPacket() function.
     * If packet is too large - sends OBEX_HTTP_REQ_TOO_LARGE response.
     */
    private int sendResponsePacket(byte[] head,
            HeaderSetImpl headers) throws IOException {
        int status = head[0] & 0xFF;
        packetBegin(head);
        packetAddConnectionID(getConnectionID(), headers);
        packetAddAuthResponses();
        packetAddHeaders(headers);
        if (!queuedHeaders.isEmpty() || operationHeadersOverflow) {
            queuedHeaders.removeAllElements();
            setPacketType(ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE);
        }
        packetEnd();
        return status;
    }

    private void doConnect() throws IOException {
        // NOTE client may not authenticated the server and wants to
        // so allow multiple connect requests
        HeaderSetImpl inputHeaderSet = new HeaderSetImpl(owner);
        HeaderSetImpl responseHeaderSet = new HeaderSetImpl(owner);

        // server side check
        if (buffer[3] != 0x10 || packetLength < 7) {
	    // IMPL_NOTE: It is not decided what to do if the OBEX version number
	    // is different from the one we support (which is presumably 1.0).
	    // Windows uses version 1.2, Linux uses version 1.1, and we
	    // probably want to work with both.
            // throw new IOException("unsupported client obex version");
        }
        // ignore flags
        // save maximum client supported packet size
        maxSendLength = decodeLength16(5);

        if (maxSendLength > OBEX_MAXIMUM_PACKET_LENGTH) {
            maxSendLength = OBEX_MAXIMUM_PACKET_LENGTH;
        }
        parsePacketHeaders(inputHeaderSet, 7);
        // processMissingAuthentications();

        int status = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;

        try {
            status = handler.onConnect(inputHeaderSet,
                    responseHeaderSet);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        status = validateStatus(status);

        if (status != ResponseCodes.OBEX_HTTP_OK) {
            // lets client will authenticate first
            authResponses.removeAllElements();
        }

        byte[] head = new byte[] {
            (byte)status,
            0, 0, // length will be here
            0x10, // version 1.0 of OBEX
            0x0,  // flags
            (byte) (OBEX_MAXIMUM_PACKET_LENGTH / 0x100), // maximum client
            (byte) (OBEX_MAXIMUM_PACKET_LENGTH % 0x100), // supported packet
            // length
        };

        status = sendResponsePacket(head, responseHeaderSet);

        if (status == ResponseCodes.OBEX_HTTP_OK) {
            isConnected = true;
        }
    }

    private boolean notConnected() throws IOException {
        if (!isConnected) {
            HeaderSetImpl headers = new HeaderSetImpl(owner);
            headers.setHeader(HeaderSet.DESCRIPTION, "not connected");
            sendPacket(PACKET_BAD_REQUEST, getConnectionID(), headers, true);
            return true;
        }
        return false;
    }

    void onAuthenticationFailure(byte[] username) throws IOException {
        try {
            if (DEBUG) {
                System.out.println("ServerConnectionImpl:"
                        + " handler.onAuthenticationFailure()");
            }
            handler.onAuthenticationFailure(username);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        operationClosed = true;
    }

    private void doDisconnect() throws IOException {
        if (notConnected()) {
            return;
        }
        HeaderSetImpl inputHeaderSet = new HeaderSetImpl(owner);
        HeaderSetImpl responseHeaderSet = new HeaderSetImpl(owner);

        parsePacketHeaders(inputHeaderSet, 3);
        // processMissingAuthentications();

        try {
            handler.onDisconnect(inputHeaderSet, responseHeaderSet);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        byte[] head = new byte[] {
            (byte) ResponseCodes.OBEX_HTTP_OK,
            0, 0, // length will be here
        };

        int status = sendResponsePacket(head, responseHeaderSet);

        if (status == ResponseCodes.OBEX_HTTP_OK) {
            isConnected = false;
        }
    }

    private void doPut(HeaderSetImpl inputHeaderSet) throws IOException {
        int status = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        ServerOperation op =
            new ServerOperation(this, inputHeaderSet);
        try {
            status = handler.onPut(op);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        status = validateStatus(status);
        if (operationHeadersOverflow) {
            status = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
        }
        op.destroy(status);
    }

    private void doDelete(HeaderSetImpl inputHeaderSet) throws IOException {
        HeaderSetImpl responseHeaderSet = new HeaderSetImpl(owner);
        int status = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        try {
            status = handler.onDelete(inputHeaderSet, responseHeaderSet);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        status = validateStatus(status);
        byte[] head = new byte[] {
            (byte)status,
            0, 0, // length will be here
        };

        sendResponsePacket(head, responseHeaderSet);
    }

    private void doPutOrDelete() throws IOException {
        if (notConnected()) {
            return;
        }
        HeaderSetImpl inputHeaderSet = new HeaderSetImpl(owner);

        int mode = ServerOperation.waitForData(this,
                inputHeaderSet, OPCODE_PUT);

        switch (mode) {
            case 0: sendPacket(PACKET_SUCCESS, getConnectionID(), null, true);
                    return;
            case 1: doPut(inputHeaderSet); return;
            case 2: doDelete(inputHeaderSet); return;
            default:return;
        }
    }

    private void doGet() throws IOException {
        if (notConnected()) {
            return;
        }
        int status = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        ServerOperation op =
            new ServerOperation(this);
        try {
            status = handler.onGet(op);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        status = validateStatus(status);
        if (operationHeadersOverflow) {
            status = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
        }
        op.destroy(status);
    }

    private void doSetPath() throws IOException {
        if (notConnected()) {
            return;
        }
        HeaderSetImpl inputHeaderSet = new HeaderSetImpl(owner);
        HeaderSetImpl responseHeaderSet = new HeaderSetImpl(owner);

        // check flags
        boolean create = ((buffer[3] & 2) == 0);
        boolean backup = ((buffer[3] & 1) == 1);

        parsePacketHeaders(inputHeaderSet, 5);
        // processMissingAuthentications();
        int status = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        try {
            status = handler.onSetPath(inputHeaderSet,
                    responseHeaderSet, backup, create);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        status = validateStatus(status);
        byte[] head = new byte[] {
            (byte)status,
            0, 0, // length will be here
        };
        sendResponsePacket(head, responseHeaderSet);
    }

    /*
     * Process one client request
     * @return false when connection closed
     */
    private boolean processRequest() throws IOException {
        try {
            recvPacket();
        } catch (IOException e) {
            return false;
        }
        HeaderSetImpl inputHeaderSet = new HeaderSetImpl(owner);
        operationHeadersOverflow = false;
        operationClosed = false;
        isEof = false;

        switch (packetType) {
            case OPCODE_CONNECT:
                doConnect();
                break;

            case OPCODE_DISCONNECT:
                doDisconnect();
                break;

            case OPCODE_PUT:
            case OPCODE_PUT | OPCODE_FINAL:
                doPutOrDelete();
                break;

            case OPCODE_GET:
            case OPCODE_GET | OPCODE_FINAL:
                doGet();
                break;
            case OPCODE_SETPATH:
                doSetPath();
                break;

            case OPCODE_ABORT:
                // ignore abort, it is too late, any of the operations is
                // finished
                byte[] head = new byte[] {
                    (byte) ResponseCodes.OBEX_HTTP_OK,
                    0, 0, // length will be here
                };
                sendResponsePacket(head, null);
                break;

            default:
                // wrong packet received, ignoring
                if (DEBUG) {
                    System.out.println("Wrong packet: id = "
                            + inputHeaderSet.packetType + " length = "
                            + packetLength);
                }
                sendPacket(PACKET_NOT_IMPLEMENTED, getConnectionID(),
			   null, true);
        }
        return true;
    }

    public void setConnectionID(long id) {
        try { // may by overloaded by user and throw exception
            connId = id;
            handler.setConnectionID(id);
        } catch (Throwable e) {
            // nothing
        }
    }

    public long getConnectionID() {
        try { // may by overloaded by user and throw exception
            long id = handler.getConnectionID();
            if (connId == id) {
                return -1;
            }
            connId = id;
            return id;
        } catch (Throwable e) {
            return -1;
        }
    }
}

