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

import com.sun.midp.links.Link;

/**
 * Server pipe connection endpoint. Used by Dispatcher to keep track of
 * open pipe connections and match server/client pipes.
 */
class ServerEndpoint extends Endpoint {

    private static final boolean DEBUG = false;
    private String serverName;
    private String serverVersion;
    private int serverVersionAsInt;
    private long targetIsolateId;
    private Link acceptLink;

    ServerEndpoint(UserListener connectionListener, long endpointId, String serverName, 
            String serverVersion, long targetIsolateId) {
        super(connectionListener, endpointId);
        
        this.connectionListener = connectionListener;
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.serverVersionAsInt = PipeServiceProtocol.parseVersion(serverVersion);
        this.targetIsolateId = targetIsolateId;
    }

    void close() {
        if (acceptLink != null) {
            acceptLink.close();
        }
    }

    boolean suitableForClient(String name, int version) {
        if (DEBUG)
            PipeServiceProtocol.debugPrintS(" considering " + name + ' ' + version);

        return name.equals(serverName) && version <= serverVersionAsInt;
    }

    long getTargetIsolateId() {
        return targetIsolateId;
    }

    String getServerVersion() {
        return serverVersion;
    }
    
    void setAcceptLink(Link acceptLink) {
        this.acceptLink = acceptLink;
    }

    Link getAcceptLink() {
        return acceptLink;
    }
    
    
}
