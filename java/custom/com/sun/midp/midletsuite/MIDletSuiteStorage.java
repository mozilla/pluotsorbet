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

package com.sun.midp.midletsuite;

import com.sun.j2me.security.AccessController;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * This class manages the persistent data for MIDlet suites.
 * <P>
 * Each installed package is uniquely identified by a unique ID.
 * Only suites installed or updated using this API appear
 * in the list of known suites.
 */
public class MIDletSuiteStorage {

    /** This class has a different security domain than the MIDlet suite. */
    private static SecurityToken classSecurityToken;

    /** This is the master storage object to synchronize all accesses */
    private static MIDletSuiteStorage masterStorage;

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken != null) {
            return;
        }

        classSecurityToken = token;
        MIDletSuiteImpl.initSecurityToken(classSecurityToken);
    }

    /**
     * Returns a reference to the singleton MIDlet suite storage object.
     * <p>
     * Method requires the com.sun.midp.ams permission.
     *
     * @return the storage reference
     *
     * @exception SecurityException if the caller does not have permission
     *   to install software
     */
    public static MIDletSuiteStorage getMIDletSuiteStorage()
            throws SecurityException {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        return getMasterStorage();
    }

    /**
     * Returns a reference to the singleton MIDlet suite storage object.
     *
     * @param securityToken security token of the calling class
     *
     * @return the storage reference
     *
     * @exception SecurityException if the caller does not have permission
     *   to manage midlets
     */
    public static MIDletSuiteStorage getMIDletSuiteStorage(
           SecurityToken securityToken) throws SecurityException {
        securityToken.checkIfPermissionAllowed(Permissions.AMS);

        return getMasterStorage();
    }

    /**
     * Java interface for midp_suiteid2pcsl_string().
     *
     * @param suiteId unique ID of the suite
     *
     * @return string representation of the given suiteId
     */
    public static native String suiteIdToString(int suiteId);

    /**
     * Returns a reference to the singleton storage object.
     *
     * @return the storage reference
     */
    private static MIDletSuiteStorage getMasterStorage() {
        if (masterStorage == null) {
            masterStorage = new MIDletSuiteStorage();
        }

        return masterStorage;
    }

    /**
     * Private constructor to prevent outside instantiation.
     */
    private MIDletSuiteStorage() {
    }

    /**
     * Gets the MIDlet Suite from storage, and selects one midlet to be run.
     *
     * @param id the unique ID of the suite given
     *        by the installer when it was downloaded
     * @param update true is this MIDletSuite need to be updated
     *
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked; MIDletSuiteCorruptedException is thrown if the MIDletSuite is
     * corrupted
     * @exception MIDletSuiteCorruptedException if the suite is corrupted
     *
     * @return MIDlet Suite reference or null if the suite doesn't exist
     */
    public synchronized MIDletSuiteImpl getMIDletSuite(int id,
            boolean update)
            throws MIDletSuiteLockedException, MIDletSuiteCorruptedException {
        MIDletSuiteImpl.lockMIDletSuite(id, update);

        /*
         * save on startup time, get the properties at first getProperty call
         * and fill permissions on getPermission
         */
        return new MIDletSuiteImpl(id);
    }

    /**
     * Get the storage id for a suite.
     *
     * @param id unique ID of the suite
     *
     * @return storage id or null if the suite does not exist
     */
    public native static int getMidletSuiteStorageId(int id);

    /**
     * Gets the unique identifier of MIDlet suite.
     *
     * @param vendor name of the vendor that created the application, as
     *        given in a JAD file
     * @param name name of the suite, as given in a JAD file
     *
     * @return suite ID of the midlet suite given by vendor and name
     *         or MIDletSuite.UNUSED_SUITE_ID if the suite does not exist
     */
    public static native int getSuiteID(String vendor, String name);
    
    /**
     * Gets a secure filename base (including path separator if needed)
     * for the suite. File build with the base will be automatically deleted
     * when the suite is removed.
     *
     * @param suiteId unique ID of the suite
     *
     * @return secure filename base for the suite having the given ID
     */
    public native String getSecureFilenameBase(int suiteId);
}
