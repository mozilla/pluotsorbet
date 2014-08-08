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
package com.sun.midp.io.j2me.pipe.serviceProtocol;

import com.sun.cldc.isolate.Isolate;
import com.sun.midp.links.Link;
import com.sun.midp.links.LinkMessage;
import com.sun.midp.services.SystemServiceConnection;
import com.sun.midp.services.SystemServiceConnectionClosedException;
import com.sun.midp.services.SystemServiceConnectionListener;
import com.sun.midp.services.SystemServiceDataMessage;
import com.sun.midp.services.SystemServiceLinkMessage;
import com.sun.midp.services.SystemServiceMessage;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Part of AMS-side Pipe service. Each UserListner serves particular
 * client MIDlet (i.e. all pipe instances of that MIDlet). Together
 * with Pipe*ConnectionImpl classes this classes provides high-level
 * pipe connection negotiation protocol. It uses PipeServiceProtocol
 * for low-level communication.
 */
public class UserListener implements SystemServiceConnectionListener {

    private static final boolean DEBUG = false;
    private SystemServiceConnection conn;
    private Dispatcher dispatcher;

    UserListener(SystemServiceConnection conn, Dispatcher dispatcher) {
        this.conn = conn;
        this.dispatcher = dispatcher;
    }

    public void onMessage(SystemServiceMessage msg) {
        SystemServiceDataMessage outMsg;
        DataOutput out;

        try {
            DataInput in = ((SystemServiceDataMessage) msg).getDataInput();
            switch (in.readInt()) {
            case PipeServiceProtocol.MAGIC_BIND_PIPE_CLIENT:
                 {
                    // connection yield itself as pipe client. no need to keep track of it
                    String serverName = in.readUTF();
                    String serverVersionRequested = in.readUTF();
                    long targetIsolateId = in.readLong();
                    if (DEBUG)
                        PipeServiceProtocol.debugPrintS(" Dispatcher.onMessage for " + conn + ": REGISTER CLIENT " + serverName + ' ' + serverVersionRequested);
                    ServerEndpoint server = dispatcher.getServerEndpoint(serverName, serverVersionRequested);
                    if (server == null) {
                        fail("No pipe server found for given request");
                    } else {
                        connectIsolateToServer(targetIsolateId, server, serverVersionRequested);
                    }
                }
                break;

            case PipeServiceProtocol.MAGIC_BIND_PIPE_SERVER:
                 {
                    String serverName = in.readUTF();
                    String serverVersion = in.readUTF();
                    long targetIsolateId = in.readLong();
                    long endpointId = PipeServiceProtocol.generateEndpointId();

                    if (DEBUG)
                        PipeServiceProtocol.debugPrintS(" Dispatcher.onMessage for " + conn + ": REGISTER SERVER " + serverName + ' ' + serverVersion + ' ' + endpointId);

                    outMsg = SystemServiceMessage.newDataMessage();
                    out = outMsg.getDataOutput();
                    out.writeInt(PipeServiceProtocol.MAGIC_OK);
                    out.writeLong(endpointId);
                    conn.send(outMsg);

                    ServerEndpoint server = new ServerEndpoint(this, endpointId,
                            serverName, serverVersion, targetIsolateId);
                    dispatcher.addServerEndpoint(server);
                }
                break;

            case PipeServiceProtocol.MAGIC_ACCEPT_PIPE_SERVER:
                 {
                    long endpointId = in.readLong();
                    if (DEBUG)
                        PipeServiceProtocol.debugPrintS(" Dispatcher.onMessage for " + conn + ": ACCEPT SERVER " + endpointId);

                    ServerEndpoint server = (ServerEndpoint) dispatcher.getEndpoint(endpointId);
                    SystemServiceLinkMessage linkMsg = SystemServiceMessage.newLinkMessage();
                    Isolate serverIsolate = getIsolateById(server.getTargetIsolateId());
                    Link serverAcceptLink = Link.newLink(Isolate.currentIsolate(), serverIsolate);
                    linkMsg.setLink(serverAcceptLink);
                    server.setAcceptLink(serverAcceptLink);
                    conn.send(linkMsg);
                }
                break;

            case PipeServiceProtocol.MAGIC_CLOSE_PIPE_SERVER:
                 {
                    long endpointId = in.readLong();
                    ServerEndpoint server = (ServerEndpoint) dispatcher.getEndpoint(endpointId);
                    if (DEBUG)
                        PipeServiceProtocol.debugPrintS(" Dispatcher.onMessage for " + conn + ": CLOSE SERVER " + endpointId);
                    if (server == null) {
                        fail("No specified server found");
                    } else {
                        dispatcher.removeServerEndpoint(server);
                        server.close();
                        outMsg = SystemServiceMessage.newDataMessage();
                        out = outMsg.getDataOutput();
                        out.writeInt(PipeServiceProtocol.MAGIC_OK);
                        conn.send(outMsg);
                    }
                }
                break;

            default:
                fail("Pipe service: invalid request code");
            }
        } catch (SystemServiceConnectionClosedException ex) {
            if (DEBUG) {
                PipeServiceProtocol.debugPrintS(" ERR:");
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            if (DEBUG) {
                PipeServiceProtocol.debugPrintS(" ERR:");
                ex.printStackTrace();
            }
            fail(ex.toString());
        } catch (IllegalArgumentException ex) {
            if (DEBUG) {
                PipeServiceProtocol.debugPrintS(" ERR:");
                ex.printStackTrace();
            }
            fail(ex.toString());
        }
    }

    public void onConnectionClosed() {
        if (DEBUG)
            PipeServiceProtocol.debugPrintS(" Dispatcher.onConnectionClosed " + conn);
        dispatcher.removeAllEndpoints(this);
    }

    private void connectIsolateToServer(long clientIsolateId, ServerEndpoint server,
            String serverVersionRequested)
            throws IOException, SystemServiceConnectionClosedException {

        Isolate serverIsolate = getIsolateById(server.getTargetIsolateId());

        if (!checkIsolateAlive(serverIsolate)) {
            dispatcher.removeServerEndpoint(server);
        }

        Isolate clientIsolate = getIsolateById(clientIsolateId);

        connectIsolates(serverIsolate, clientIsolate, server, serverVersionRequested);
    }

    private void connectIsolates(Isolate server, Isolate client,
            ServerEndpoint serverPipe, String serverVersionRequested)
            throws IOException, SystemServiceConnectionClosedException {
        Link serverAcceptLink = serverPipe.getAcceptLink();
        if (serverAcceptLink == null || !serverAcceptLink.isOpen()) {
            fail("The requested server is not accepting connections");
        } else {
            serverPipe.setAcceptLink(null);
            Link linkToClient = Link.newLink(server, client);
            Link linkFromClient = Link.newLink(client, server);
            SystemServiceLinkMessage linkMsg;
            SystemServiceDataMessage dataMsg;
            DataOutput out;

            dataMsg = SystemServiceMessage.newDataMessage();
            out = dataMsg.getDataOutput();
            // send response to client with pair of ptp clinks
            out.writeInt(PipeServiceProtocol.MAGIC_OK);
            out.writeUTF(serverPipe.getServerVersion());
            conn.send(dataMsg);
            linkMsg = SystemServiceMessage.newLinkMessage();
            linkMsg.setLink(linkToClient);
            conn.send(linkMsg);
            linkMsg = SystemServiceMessage.newLinkMessage();
            linkMsg.setLink(linkFromClient);
            conn.send(linkMsg);

            // send ptp links to server
            ByteArrayOutputStream outStr = new ByteArrayOutputStream();
            out = new DataOutputStream(outStr);
            out.writeInt(PipeServiceProtocol.MAGIC_BIND_PIPE_CLIENT);
            out.writeUTF(serverVersionRequested);
            LinkMessage lmMsg = LinkMessage.newDataMessage(outStr.toByteArray());

            serverAcceptLink.send(lmMsg);
            lmMsg = LinkMessage.newLinkMessage(linkFromClient);
            serverAcceptLink.send(lmMsg);
            lmMsg = LinkMessage.newLinkMessage(linkToClient);
            serverAcceptLink.send(lmMsg);
            serverAcceptLink.close();
        }
    }

    private void fail(String cause) {
        try {
            SystemServiceDataMessage msg = SystemServiceMessage.newDataMessage();
            DataOutput out = msg.getDataOutput();
            out.writeInt(PipeServiceProtocol.MAGIC_FAIL);
            out.writeUTF(cause);
            conn.send(msg);
        } catch (SystemServiceConnectionClosedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Isolate getIsolateById(long targetIsolateId) {
        Isolate[] isolates = Isolate.getIsolates();
        int i;
        for (i = isolates.length; --i >= 0 && isolates[i].uniqueId() != targetIsolateId;) {
        }
        return i >= 0 ? isolates[i] : null;
    }

    private boolean checkIsolateAlive(Isolate isolate) {
        return isolate != null && !isolate.isTerminated();
    }
}
