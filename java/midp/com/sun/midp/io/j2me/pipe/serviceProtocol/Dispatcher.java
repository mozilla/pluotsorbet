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

import com.sun.midp.security.SecurityToken;
import com.sun.midp.services.SystemService;
import com.sun.midp.services.SystemServiceConnection;

/**
 * Dispatcher is running on AMS side as SystemService providing registration and
 * deregistration of client-side Pipe service proxies. Each alive connection is given
 * an instance of UserListener class which processes requests from client MIDlet.
 */
class Dispatcher implements SystemService {

    private static final boolean DEBUG = false;
    private ServerEndpoint servers = null;
    private ClientEndpoint clients = null;

    Dispatcher(SecurityToken token) {
    }

    public String getServiceID() {
        return PipeServiceProtocol.SERVICE_ID;
    }

    public void start() {
    }

    public void stop() {
    }

    public void acceptConnection(SystemServiceConnection connection) {
        if (DEBUG)
            PipeServiceProtocol.debugPrintS(" Dispatcher.acceptConnection " + connection);
        UserListener listener = new UserListener(connection, this);
        connection.setConnectionListener(listener);
    }

    synchronized void addServerEndpoint(ServerEndpoint point) {
        point.next = servers;
        servers = point;
    }

    synchronized void removeServerEndpoint(ServerEndpoint point) {
        if (servers == point) {
            servers = (ServerEndpoint) point.next;
        } else if (servers != null) {
            removeEndpointFrom(point, servers);
        }

    }

    synchronized void addClientEndpoint(ClientEndpoint point) {
        point.next = clients;
        clients = point;
    }

    synchronized void removeClientEndpoint(ClientEndpoint point) {
        if (clients == point) {
            clients = (ClientEndpoint) point.next;
        } else if (clients != null) {
            removeEndpointFrom(point, clients);
        }

    }

    private synchronized void removeEndpointFrom(Endpoint point, Endpoint list) {
        for (Endpoint cur = list; cur != null; cur = cur.next) {
            if (cur.next == point) {
                cur.next = point.next;
                break;
            }
        }
    }

    synchronized void removeAllEndpoints(UserListener listener) {
        if (servers != null) {
        }
    }

    synchronized ServerEndpoint getServerEndpoint(String serverName, String serverVersion) {
        int version = PipeServiceProtocol.parseVersion(serverVersion);

        if (DEBUG)
            PipeServiceProtocol.debugPrintS(" searching for endpoint for pipe server " + serverName + 
                    ' ' + serverVersion + '(' + version + ')');
        for (ServerEndpoint point = servers; point != null; point = (ServerEndpoint) point.next) {
            if (point.suitableForClient(serverName, version))
                return point;
        }

        return null;
    }

    synchronized Endpoint getEndpoint(long endpointId) {
        if (DEBUG)
            PipeServiceProtocol.debugPrintS(" searching for endpoint id " + endpointId);

        Endpoint point;
        for (point = servers; point != null && point.getId() != endpointId; point = point.next) {
            //
        }
        if (point == null) {
            for (point = clients; point != null && point.getId() != endpointId; point = point.next) {
                //
            }
        }

        return point;
    }
}
