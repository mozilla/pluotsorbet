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

package com.sun.midp.services;

import com.sun.cldc.isolate.*;
import com.sun.midp.links.*;
import java.io.*;

final class IsolateSystemServiceRequestHandler 
    implements SystemServiceRequestListener {

    private Isolate serviceIsolate = null;
    private Isolate clientIsolate = null;

    private SystemServiceManager serviceManager = null;
    private SystemService requestedService = null;

    private SystemServiceConnectionLinks sendReceiveLinks = null;
    private SystemServiceRequestProtocolAMS serviceRequestProtocol = null;

    IsolateSystemServiceRequestHandler(SystemServiceManager serviceManager, 
            Isolate clientIsolate) {

        this.serviceIsolate = Isolate.currentIsolate();
        this.clientIsolate = clientIsolate;
        this.serviceManager = serviceManager;

        Link sendLink = Link.newLink(serviceIsolate, clientIsolate);
        Link receiveLink = Link.newLink(clientIsolate, serviceIsolate);      
        this.sendReceiveLinks = new SystemServiceConnectionLinks(
                sendLink, receiveLink);

        serviceRequestProtocol = new SystemServiceRequestProtocolAMS(this);
    }

    SystemServiceConnectionLinks getSendReceiveLinks() {
        return sendReceiveLinks;
    }

    void handleServiceRequest()
        throws ClosedLinkException, 
               InterruptedIOException, 
               IOException {

        serviceRequestProtocol.handleServiceRequest(sendReceiveLinks);
    }


    public SystemServiceConnectionLinks onServiceRequest(String serviceID) {
        requestedService = serviceManager.getService(serviceID);
        if (requestedService == null) {
            return null;
        }

        Link serviceToClient = Link.newLink(serviceIsolate, clientIsolate);
        Link clientToService = Link.newLink(clientIsolate, serviceIsolate);
        SystemServiceConnectionLinks connectionLinks = 
            new SystemServiceConnectionLinks(serviceToClient, clientToService);

        return connectionLinks;
    }

    public void onLinksPassedToClient(SystemServiceConnectionLinks 
            connectionLinks) {

        if (connectionLinks == null || requestedService == null) {
            throw new IllegalStateException();
        }

        SystemServiceConnection serviceConnection = 
            new SystemServiceConnectionImpl(connectionLinks);
            
        synchronized (requestedService) {
            requestedService.acceptConnection(serviceConnection);
        }

        requestedService = null;
    }
}
