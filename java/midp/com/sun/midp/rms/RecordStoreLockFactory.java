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

package com.sun.midp.rms;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

/**
 * Factory that creates AbstractRecordStoreLock implementation 
 * instances.
 */
final class RecordStoreLockFactory {
    /**
     * Gets lock instance for locking record store corresponding 
     * to the specified suite id and record store name.
     *
     * @param token security token
     * @param suiteId ID of the MIDlet suite that owns the record store
     * @param storeName record store name
     * @return lock for record store corresponding to the specified 
     * suite id and record store name
     */
    static final synchronized AbstractRecordStoreLock getLockInstance(
            SecurityToken token, int suiteId, String storeName) {

        token.checkIfPermissionAllowed(Permissions.MIDP);
        String base = RmsEnvironment.getSecureFilenameBase(suiteId);
        String lockName = base + "#" + storeName;

        return new RecordStoreLock(token, lockName);
    }

    /**
     * Private constructor toprevent creating class instances.
     */
    private void RecordStoreLockFactory() {
    }
}
