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
import com.sun.cldc.isolate.Isolate;
import com.sun.midp.main.MIDletSuiteUtils;
import com.sun.midp.security.SecurityToken;

public final class SystemServiceLinkPortal {
    
    private static SystemServiceManager manager;
    private static SystemServiceRequestHandler requestHandler;
    private static Link[] clientSideLinks;
    
    /**
     * Estalishes Service API runtime in AMS isolate for given client isolate.
     *
     * @param clientIsolate
     * @param token
     * @return
     * @throws IllegalStateException if invoked from non-AMS isolate
     */
    public static Link[] establishLinksFor(Isolate clientIsolate, SecurityToken token) {
        ensureInit(token);
        
        IsolateSystemServiceRequestHandler isolateRequestHandler =
                requestHandler.newIsolateRequestHandler(clientIsolate);
        SystemServiceConnectionLinks links = isolateRequestHandler.getSendReceiveLinks();
        Link[] linkArray = new Link[2];
        linkArray[0] = links.getSendLink();
        linkArray[1] = links.getReceiveLink();
        
        requestHandler.handleIsolateRequests(isolateRequestHandler);
        
        return linkArray;
    }
    
    private synchronized static void ensureInit(SecurityToken token) {
        if (!MIDletSuiteUtils.isAmsIsolate())
            throw new IllegalStateException("Attempt to estalish Service API runtime from non-AMS isolate");
        
        if (manager == null) {
            manager = SystemServiceManager.getInstance(token);
            
            requestHandler = new SystemServiceRequestHandler(manager);
        }
    }
    
    /**
     * Connects to Service API runtime from client isolate.
     * 
     * @param linkArray
     * @throws IllegalStateException if invoked from AMS isolate
     */
    public static void linksObtained(Link[] linkArray) {
        if (MIDletSuiteUtils.isAmsIsolate())
            throw new IllegalStateException("Attempt to connect to Service API runtime from AMS isolate");
        
        if (linkArray == null || linkArray.length != 2)
            throw new IllegalStateException("Invalid set of links provided");
        
        if (clientSideLinks != null)
            throw new IllegalStateException("Attempt to connect to Service API runtime twice");
        
        clientSideLinks = linkArray;
    }
    
    static Link getToClientLink() {
        if (clientSideLinks == null)
            throw new IllegalStateException("No links has been received for client isolate");
        
        return clientSideLinks[0];
    }
    
    static Link getToServiceLink() {
        if (clientSideLinks == null)
            throw new IllegalStateException("No links has been received for client isolate");
        
        return clientSideLinks[1];
    }

    private SystemServiceLinkPortal() {
    }
}
