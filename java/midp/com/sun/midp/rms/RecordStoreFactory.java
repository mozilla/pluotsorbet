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

import com.sun.j2me.security.AccessController;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * A utility class for checking and removing record stores.
 * <p>
 * Method requires com.sun.midp.ams permission.
 */
public class RecordStoreFactory {

    /** Private constructor to prevent any instances from being created. */
    private RecordStoreFactory() {}

    /**
     * Remove all the Record Stores for a suite.
     * Called by the installer when updating a suite
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * @param token security token with MIDP AMS permission
     * @param id ID of the suite
     */
    public static void removeRecordStoresForSuite(SecurityToken token,
            int id) {

	if (token == null) {
            AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);
        } else {
            token.checkIfPermissionAllowed(Permissions.AMS);
        }

        RecordStoreFile.removeRecordStores(id);
    }

    /**
     * Returns true if the suite has created at least one record store.
     * Called by the installer when updating a suite.
     *
     * @param id ID of the suite
     *
     * @return true if the suite has at least one record store
     */
    public static boolean suiteHasRmsData(int id) {
        String fileNameBase = RmsEnvironment.getSecureFilenameBase(id);

        if (fileNameBase == null) {
            return false;
        }

        return suiteHasRmsData0(fileNameBase);
    }

    /**
     * Returns true if the suite has created at least one record store.
     * Called by the installer when updating a suite.
     *
     * @param fileNameBase filename base of the suite
     *
     * @return true if the suite has at least one record store
     */
    private static native boolean suiteHasRmsData0(String fileNameBase);
}
