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

import com.sun.midp.links.*;
import com.sun.cldc.isolate.*;
import java.io.*;

final class SystemServiceRequestorLocal extends SystemServiceRequestor {
    private SystemServiceManager serviceManager;

    SystemServiceRequestorLocal(SystemServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }
    
    public SystemServiceConnection requestService(String serviceID) {
        synchronized (this) {
            return doRequestService(serviceID);
        }
    }

    private SystemServiceConnection doRequestService(String serviceID) {
        SystemService service = serviceManager.getService(serviceID);
        if (service == null) {
            return null;
        }

        Isolate is = Isolate.currentIsolate();
        Link serviceToClient = Link.newLink(is, is);
        Link clientToService = Link.newLink(is, is);

        SystemServiceConnectionLinks linksService = 
            new SystemServiceConnectionLinks(serviceToClient, clientToService);

        SystemServiceConnectionLinks linksClient = 
            new SystemServiceConnectionLinks(clientToService, serviceToClient);

        SystemServiceConnection conService = 
            new SystemServiceConnectionImpl(linksService);

        SystemServiceConnection conClient = 
            new SystemServiceConnectionImpl(linksClient);

        service.acceptConnection(conService);

        return conClient;
    }
}
