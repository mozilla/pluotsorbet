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
import com.sun.midp.main.MIDletSuiteUtils;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

/**
 * Used by client to obtain connection to service.
 */
public abstract class SystemServiceRequestor {
    private static SystemServiceRequestor instance = null;

    /**
     * Establishes connection to service
     *
     * @param serviceID unique service ID
     * @return connection to service or null if service could not be found
     */
    abstract public SystemServiceConnection requestService(String serviceID);

    /**
     * Gets class instance.
     *
     * @return SystemServiceRequestor class instance
     */
    synchronized public static SystemServiceRequestor getInstance(
            SecurityToken token) {

        token.checkIfPermissionAllowed(Permissions.MIDP);

        if (instance == null) {
            if (!MIDletSuiteUtils.isAmsIsolate()) {
                Link receiveLink = SystemServiceLinkPortal.getToClientLink();
                Link sendLink = SystemServiceLinkPortal.getToServiceLink();
                SystemServiceConnectionLinks requestLinks = null;
                requestLinks = new SystemServiceConnectionLinks(
                        sendLink, receiveLink);

                instance = new SystemServiceRequestorRemote(requestLinks);
            } else {
                SystemServiceManager manager = null;
                manager = SystemServiceManager.getInstance(token);
                instance = new SystemServiceRequestorLocal(manager);
            }
        }

        return instance;
    }
}
