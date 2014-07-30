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

package com.sun.midp.suspend;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import java.io.IOException;

/**
 * Processes suspend/resume requests for networking resources.
 */
public class NetworkSubsystem extends AbstractSubsystem {
    /** The only instance of the subsytem. */
    private static NetworkSubsystem instance = new NetworkSubsystem();

    /**
     * Constructs an instance in registers it in the suspend system.
     */
    private NetworkSubsystem() {
        state = ACTIVE;
        SuspendSystem.getInstance(classSecurityToken).registerSubsystem(this);
    }

    /**
     * Returns te only instance.
     * @return the singleton instance
     * @param token security token that identifies caller access rights
     */
    public static NetworkSubsystem getInstance(SecurityToken token) {
        token.checkIfPermissionAllowed(Permissions.MIDP);
        return instance;
    }

    /**
     * Checks that networking subsystem is in either active or resuming
     * state and throws <code>IOException</code> if it is not.
     * @throws IOException if the networking subsystem state is neither
     *         <code>ACTIVE</code> nor <code>RESUMING</code>.
     */
    public synchronized void ensureActive() throws IOException {
        if (state != ACTIVE && state != RESUMING) {
            throw new IOException("Networking is not active");
        }
    }
}
