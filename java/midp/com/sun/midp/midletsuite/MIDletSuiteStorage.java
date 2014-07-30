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

import java.io.IOException;

import com.sun.j2me.security.AccessController;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.content.CHManager;

import com.sun.midp.jarutil.JarReader;
import com.sun.midp.util.Properties;

import com.sun.midp.configurator.Constants;

import com.sun.midp.rms.RecordStoreImpl;

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

            int status = loadSuitesIcons0();
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                if (status != 0) {
                    Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                        "Can't load the cached icons, error code: " + status);
                }
            }
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
        if (!suiteExists(id)) {
            return null;
        }

        MIDletSuiteImpl.lockMIDletSuite(id, update);

        /*
         * save on startup time, get the properties at first getProperty call
         * and fill permissions on getPermission
         */
        return new MIDletSuiteImpl(id);
    }

    /**
     * Reads the basic information about the midlet suite from the storage.
     *
     * @param id unique ID of the suite
     *
     * @exception IOException if an the information cannot be read
     * @exception IllegalArgumentException if suiteId is invalid
     *
     * @return MIDletSuiteInfo object with the suite's attributes
     */
    public synchronized MIDletSuiteInfo getMIDletSuiteInfo(int id)
            throws IOException, IllegalArgumentException {

        MIDletSuiteInfo msi = new MIDletSuiteInfo(id);
        getMIDletSuiteInfoImpl0(id, msi);

        return msi;
    }

    /**
     * Retrieves an icon for the given midlet suite.
     *
     * @param suiteId unique identifier of the suite
     * @param iconName the name of the icon to retrieve
     *
     * @return image of the icon as a byte array
     */
    public synchronized byte[] getMIDletSuiteIcon(int suiteId,
                                                  String iconName) {
        byte[] iconBytes = null;

        if (iconName == null) {
            return null;
        }

        try {
            iconBytes = getMIDletSuiteIcon0(suiteId, iconName);

            if (iconBytes == null) {
                /* Search for icon in the image cache */
                iconBytes = loadCachedIcon0(suiteId, iconName);
            }

            if (iconBytes == null) {
                /* Search for icon in the suite JAR */
                iconBytes = JarReader.readJarEntry(
                    getMidletSuiteJarPath(suiteId), iconName);
            }
        } catch (Exception e) {
            iconBytes = null;
        }

        return iconBytes;
    }

    /**
     * Get the midlet suite's class path including a path to the MONET
     * image of the specified suite and a path to the suite's jar file.
     *
     * @param id unique ID of the suite
     *
     * @return class path or null if the suite does not exist
     */
    public synchronized String[] getMidletSuiteClassPath(int id) {
        String jarFile = getMidletSuiteJarPath(id);

        if (Constants.MONET_ENABLED && id != MIDletSuite.INTERNAL_SUITE_ID) {
            String bunFile = getMidletSuiteAppImagePath(id);
            return new String[] {bunFile, jarFile};
        }

        return new String[] {jarFile};
    }

    /**
     * Loads the cached icons from the permanent storage into memory.
     *
     * @return status code (0 if no errors) 
     */
    public static native int loadSuitesIcons0();

    /**
     * Retrieves the cached icon from the icon cache.
     *
     * @param suiteId unique identifier of the suite
     * @param iconName the name of the icon to retrieve
     *
     * @return cached image data if available, otherwise null
     */
    private static native byte[] getMIDletSuiteIcon0(int suiteId,
                                                     String iconName);

    /**
     * Loads suite icon data from image cache.
     *
     * @param suiteId the ID of suite the icon belongs to
     * @param iconName the name of the icon to be loaded
     *
     * @return cached image data if available, otherwise null
     */
    private static native byte[] loadCachedIcon0(int suiteId, String iconName);

    /**
     * Reads the basic information about the midlet suite from the storage.
     *
     * @param id unique ID of the suite
     * @param msi object to fill
     *
     * @exception IOException if an the information cannot be read
     * @exception IllegalArgumentException if suiteId is invalid
     */
    public native void getMIDletSuiteInfoImpl0(int id,
        MIDletSuiteInfo msi) throws IOException, IllegalArgumentException;

    /**
     * Get the path for the MONET image of the specified suite.
     *
     * @param id unique ID of the suite
     *
     * @return image path or null if the suite does not exist
     */
    public synchronized native String getMidletSuiteAppImagePath(int id);

    /**
     * Get the class path for a suite.
     *
     * @param id unique ID of the suite
     *
     * @return class path or null if the suite does not exist
     */
    public synchronized native String getMidletSuiteJarPath(int id);

    /**
     * Get the storage id for a suite.
     *
     * @param id unique ID of the suite
     *
     * @return storage id or null if the suite does not exist
     */
    public native static int getMidletSuiteStorageId(int id);

    /**
     * Get the folder id for a suite.
     *
     * @param id unique ID of the suite
     *
     * @return folder id or -1 if the suite does not exist
     */
    public native static int getMidletSuiteFolderId(int id);

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

    // -------------- Installer related functionality ---------------

    /**
     * Get the installation information of a suite.
     *
     * @param midletSuite Suite object
     *
     * @return installation information
     *
     * @exception IOException if an the information cannot be read
     */
    InstallInfo getInstallInfo(MIDletSuiteImpl midletSuite)
            throws IOException {
        return midletSuite.getInstallInfo();
    }

    /**
     * Tells if a suite exists.
     *
     * @param id ID of a suite
     *
     * @return true if a suite of the given storage name
     *          already exists on the system
     *
     * @exception MIDletSuiteCorruptedException is thrown if the
     * MIDletSuite is corrupted
     */
    public native boolean suiteExists(int id)
        throws MIDletSuiteCorruptedException;

    /**
     * Returns a unique identifier of MIDlet suite.
     *
     * @return the platform-specific storage name of the application
     *          given by vendorName and appName
     */
    public native int createSuiteID();

    /**
     * Stores or updates a midlet suite.
     *
     * @param installInfo structure containing the following information:<br>
     * <pre>
     *     id - unique ID of the suite;
     *     jadUrl - where the JAD came from, can be null;
     *     jarUrl - where the JAR came from;
     *     jarFilename - name of the downloaded MIDlet suite jar file;
     *     suiteName - name of the suite;
     *     suiteVendor - vendor of the suite;
     *     authPath - authPath if signed, the authorization path starting
     *                with the most trusted authority;
     *     domain - security domain of the suite;
     *     trusted - true if suite is trusted;
     *     verifyHash - may contain hash value of the suite with
     *                  preverified classes or may be NULL;
     * </pre>
     *
     * @param suiteSettings structure containing the following information:<br>
     * <pre>
     *     permissions - permissions for the suite;
     *     pushInterruptSetting - defines if this MIDlet suite interrupt
     *                            other suites;
     *     pushOptions - user options for push interrupts;
     *     suiteId - unique ID of the suite, must be equal to the one given
     *               in installInfo;
     *     boolean enabled - if true, MIDlet from this suite can be run;
     * </pre>
     *
     * @param msi structure containing the following information:<br>
     * <pre>
     *     suiteId - unique ID of the suite, must be equal to the value given
     *               in installInfo and suiteSettings parameters;
     *     storageId - ID of the storage where the MIDlet should be installed;
     *     numberOfMidlets - number of midlets in the suite;
     *     displayName - the suite's name to display to the user;
     *     midletToRunClassName - the midlet's class name if the suite contains
     *                            only one midlet, ignored otherwise;
     *     iconName - name of the icon for this suite.
     * </pre>
     *
     * @param jadProps properties defined in the application descriptor
     *
     * @param jarProps properties of the manifest
     *
     * @exception IOException is thrown, if an I/O error occurs during
     * storing the suite
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     */
    public synchronized void storeSuite(InstallInfo installInfo,
        SuiteSettings suiteSettings, MIDletSuiteInfo msi,
            Properties jadProps, Properties jarProps)
                throws IOException, MIDletSuiteLockedException {
        /*
         * Convert the property args to String arrays to save
         * creating the native KNI code to access the object.
         */
        String[] strJadProperties = getPropertiesStrings(jadProps);
        String[] strJarProperties = getPropertiesStrings(jarProps);

        nativeStoreSuite(installInfo, suiteSettings, msi, null,
            strJadProperties, strJarProperties);
    }

    /**
     * Stores hash value of the suite with preverified classes
     *
     * @param id unique ID of the suite
     * @param verifyHash hash value of the suite with preverified classes
     */
    public native void storeSuiteVerifyHash(int id, byte[] verifyHash);

    /**
     * Disables a suite given its suite ID.
     * <p>
     * The method does not stop the suite if is in use. However any future
     * attepts to run a MIDlet from this suite while disabled should fail.
     *
     * @param id suite ID for the installed package
     *
     * @exception IllegalArgumentException if the suite cannot be found
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked for updating
     */
    public native void disable(int id) throws MIDletSuiteLockedException;

    /**
     * Enables a suite given its suite ID.
     * <p>
     * The method does update an suites that are currently loaded for
     * settings or of application management purposes.
     *
     * @param id suite ID for the installed package
     *
     * @exception IllegalArgumentException if the suite cannot be found
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked for updating
     */
    public native void enable(int id) throws MIDletSuiteLockedException;

    /**
     * Removes a software package given its suite ID.
     * The content handler manager is called to remove any registrations,
     * if any.
     * <p>
     * If the component is in use it must continue to be available
     * to the other components that are using it.  The resources it
     * consumes must not be released until it is not in use.
     *
     * @param id suite ID for the installed package
     *
     * @exception IllegalArgumentException if the suite cannot be found
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     */
    public void remove(int id) throws MIDletSuiteLockedException {
        remove0(id);

        /*
         * If no exception occurs,
         * remove the content handler registrations, if any.
         */
        CHManager.getManager(classSecurityToken).uninstall(id);
    }

    /**
     * Moves a software package with given suite ID to the specified storage.
     *
     * @param suiteId suite ID for the installed package
     * @param newStorageId new storage ID
     *
     * @exception IllegalArgumentException if the suite cannot be found or
     * invalid storage ID specified 
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     * @exception IOException if an I/O error occurred
     * @exception OutOfMemoryError if out of memory
     */
    public native void changeStorage(int suiteId, int newStorageId)
            throws IllegalArgumentException, MIDletSuiteLockedException,
                   IOException, OutOfMemoryError;

    /**
     * Native method void moveSuiteToFolder(...) of
     * com.sun.midp.midletsuite.MIDletSuiteStorage.
     * <p>
     * Moves a software package with given suite ID to the specified folder.
     *
     * @param suiteId suite ID for the installed package
     * @param newFolderId folder ID
     *
     * @exception IllegalArgumentException if the suite cannot be found or
     *                                     invalid folder ID specified
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     *                                       locked
     * @exception IOException if an I/O error occurred
     * @exception OutOfMemoryError if out of memory
     */
    public native void moveSuiteToFolder(int suiteId, int newFolderId)
            throws IllegalArgumentException, MIDletSuiteLockedException,
                   IOException, OutOfMemoryError;

    /**
     * Removes all suites with the temporary flag set to true.
     */
    public synchronized void removeTemporarySuites() {
        final int[] suiteIds = getListOfSuites();
        
        for (int i = 0; i < suiteIds.length; ++i) {
            try {
                final MIDletSuiteInfo suiteInfo = 
                        getMIDletSuiteInfo(suiteIds[i]);
                if (suiteInfo.temporary) {
                    remove(suiteIds[i]);
                }
            } catch (final IOException e) {
                // skip this suite
            } catch (final MIDletSuiteLockedException e) {
                // skip this suite
            }
        }
    }
    
    /**
     * Implementation for storeSuite() and storeSuiteComponent().
     * Stores or updates a midlet suite or a dynamic component.
     *
     * @param installInfo structure containing the following information:<br>
     * <pre>
     *     id - unique ID of the suite;
     *     jadUrl - where the JAD came from, can be null;
     *     jarUrl - where the JAR came from;
     *     jarFilename - name of the downloaded MIDlet suite jar file;
     *     suiteName - name of the suite;
     *     suiteVendor - vendor of the suite;
     *     suiteVersion - version of the suite;
     *     authPath - authPath if signed, the authorization path starting
     *                with the most trusted authority;
     *     domain - security domain of the suite;
     *     trusted - true if suite is trusted;
     *     verifyHash - may contain hash value of the suite with
     *                  preverified classes or may be NULL;
     * </pre>
     *
     * @param suiteSettings structure containing the following information:<br>
     * <pre>
     *     permissions - permissions for the suite;
     *     pushInterruptSetting - defines if this MIDlet suite interrupt
     *                            other suites;
     *     pushOptions - user options for push interrupts;
     *     suiteId - unique ID of the suite, must be equal to the one given
     *               in installInfo;
     *     boolean enabled - if true, MIDlet from this suite can be run;
     * </pre>
     *
     * @param msi structure containing the following information:<br>
     * <pre>
     *     suiteId - unique ID of the suite, must be equal to the value given
     *               in installInfo and suiteSettings parameters;
     *     storageId - ID of the storage where the MIDlet should be installed;
     *     numberOfMidlets - number of midlets in the suite;
     *     displayName - the suite's name to display to the user;
     *     midletToRunClassName - the midlet's class name if the suite contains
     *                            only one midlet, ignored otherwise;
     *     iconName - name of the icon for this suite.
     * </pre>
     * msi is null if a dynamic component rather than a suite is being saved
     *
     * @param ci structure containing the following information:<br>
     * <pre>
     *     componentId - unique ID of the component being saved
     *     suiteId - unique ID of the suite that the component belongs to,
     *               must be equal to the value given in installInfo and
     *               suiteSettings parameters;
     *     trusted - true if component is trusted, must be equal to the
     *               value given in installInfo;
     *     displayName - the suite's name to display to the user.
     * </pre>
     * ci is null if a suite rather than a dynamic component is being saved
     *
     * @param jadProps properties the JAD as an array of strings in
     *        key/value pair order, can be null if jadUrl is null
     *
     * @param jarProps properties of the manifest as an array of strings
     *        in key/value pair order
     *
     * @exception IOException is thrown, if an I/O error occurs during
     * storing the suite
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     */
    native void nativeStoreSuite(InstallInfo installInfo,
        SuiteSettings suiteSettings, MIDletSuiteInfo msi, Object ci,
            String[] jadProps, String[] jarProps)
                throws IOException, MIDletSuiteLockedException;

    /**
     * Native remove of a software package given its suite ID.
     * <p>
     * If the component is in use it must continue to be available
     * to the other components that are using it.  The resources it
     * consumes must not be released until it is not in use.
     *
     * @param id suite ID for the installed package
     *
     * @exception IllegalArgumentException if the suite cannot be found
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     */
    private native void remove0(int id) throws MIDletSuiteLockedException;

    /**
     * Fill plain array with properties key/value String pairs.
     * It's needed to simplify properties using in a native code.
     *
     * @param props properties to get Strings from
     *
     * @return array of Strings filled with property key/value pairs
     */
    static String[] getPropertiesStrings(Properties props) {
        if (props != null) {
            int size = props.size();
            String[] res = new String[size * 2];
            for (int i = 0, j = 0; i < size; i++) {
                res[j++] = props.getKeyAt(i);
                res[j++] = props.getValueAt(i);
            }
            return res;
        } else return null;
    }

    // ------------ Graphical App Manager ------------------


    /**
     * Saves any of the settings (security or others) that the user may have
     * changed.
     *
     * @param id ID of the suite
     * @param pushInterruptSetting push interrupt setting
     * @param pushOptions push options
     * @param permissions security permissions for the suite
     *
     * @exception IOException if an error happens while writing
     */
    public void saveSuiteSettings(int id,
            byte pushInterruptSetting, int pushOptions, byte[] permissions)
                throws IOException {
        SuiteSettings settings = new SuiteSettings(id);
        settings.setPushInterruptSetting(pushInterruptSetting);
        settings.setPushOptions(pushOptions);
        settings.setPermissions(permissions);
        settings.save();
    }

    /**
     * Gets the amount of storage on the device that this suite is using.
     * This includes the JAD, JAR, management data, and RMS.
     *
     * @param id ID of a MIDlet suite
     *
     * @return number of bytes of storage the suite is using
     */
    public native int getStorageUsed(int id);

    /**
     * List all installed software packages by storage name.
     *
     * @return an array of ints of the storage names for the
     *     installed packages
     * @exception SecurityException if the caller does not have permission
     *     to see what software is installed
     */
    public synchronized int[] getListOfSuites() {
        int n = getNumberOfSuites();
        if (n < 0) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                    "Error in getNumberOfSuites(): returned " + n);
            }
            n = 0;
        }

        int[] array = new int[n];

        if (n > 0) {
            getSuiteList(array);
        }

        return array;
    }

    /**
     * Returns an array of the names of record stores owned by the
     * MIDlet suite.
     *
     * The order of RecordStore names returned is implementation
     * dependent.
     *
     * @param suiteId ID of the MIDlet suite that owns the record store
     *
     * @return array of the names of record stores owned by the
     * MIDlet suite or null if the MIDlet suite does not have
     * any record stores
     */
    public String[] listRecordStores(int suiteId) {
        return RecordStoreImpl.listRecordStores(classSecurityToken, suiteId);
    }

    /**
     * Get the number of installed of MIDlet suites.
     *
     * @return the number of installed suites or -1 in case of error
     */
    private native int getNumberOfSuites();

    /**
     * Retrieves the list of MIDlet suites and store them into a Vector
     * object. Each element in the Vector is the storage name
     * of an installed application.
     *
     * @param suites an empty array of suite IDs to fill, call
     *     getNumberOfSuites to know how big to make the array
     */
    private native void getSuiteList(int[] suites);
    
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

    /**
     * Checks the integrity of the suite storage database and of the
     * installed suites.
     *
     * @param fullCheck 0 to check just an integrity of the database,
     *                    other value for full check
     * @param delCorruptedSuites != 0 to delete the corrupted suites,
     *                           0 - to keep them (for re-installation).
     *
     * @return 0 if no errors,
     *         1 if the suite database was corrupted but has been successfully
     *           repaired,
     *         a negative value if the database is corrupted and could not
     *         be repaired
     */
    public native int checkSuitesIntegrity(boolean fullCheck,
                                           boolean delCorruptedSuites);
}
