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

import java.util.*;
import java.lang.*;

final class SystemServiceManagerImpl extends SystemServiceManager {

    /** Registered service entries */
    private Hashtable serviceEntries = null;
    
    SystemServiceManagerImpl() {
        serviceEntries = new Hashtable();
    }

    class SystemServiceEntry {
        boolean isStarted = false;
        SystemService service = null;

        SystemServiceEntry(SystemService service) {
            this.isStarted = false;
            this.service = service;
        }
    }

    synchronized public void registerService(SystemService service) {
        addService(service);
    }

    synchronized public SystemService getService(String serviceID) {
        SystemServiceEntry entry = 
            (SystemServiceEntry)serviceEntries.get(serviceID);

        if (entry == null) {
            return null;
        }

        if (!entry.isStarted) {
            entry.service.start();
            entry.isStarted = true;
        }

        return entry.service;
    }

    public void shutdown() {
        removeAllServices();
    }

    private void addService(SystemService service) {
        removeService(service.getServiceID());

        SystemServiceEntry entry = new SystemServiceEntry(service);
        serviceEntries.put(service.getServiceID(), entry);
    }

    private void removeService(String serviceID) {
        SystemServiceEntry entry = 
            (SystemServiceEntry)serviceEntries.get(serviceID);

        if (entry == null) {
            return;
        }

        if (entry.isStarted) {
            entry.service.stop();
            entry.isStarted = false;
        }

        serviceEntries.remove(serviceID);
    }

    private void removeAllServices() {
        Enumeration e = serviceEntries.keys();
        for(; e.hasMoreElements(); ) {
            String serviceID = (String)e.nextElement();
            removeService(serviceID);
        }
    }
}
