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

import com.sun.midp.util.Properties;

import com.sun.midp.amsservices.ComponentInfo;

import com.sun.midp.security.Permissions;
import com.sun.j2me.security.AccessController;

/** Dummy implementation of Storage for Dynamically Loaded Components. */
public class DynamicComponentStorage {

    /** Holds an instance of DynamicComponentStorage. */
    private static DynamicComponentStorage componentStorage = null;

    /** "Not implemented" error message. */
    private static final String msgNotImplemented = "Not implemented.";

    /**
     * Private constructor to prevent direct instantiations.
     */
    private DynamicComponentStorage() {
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
    public static DynamicComponentStorage getComponentStorage()
            throws SecurityException {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        if (componentStorage == null) {
            componentStorage = new DynamicComponentStorage();
        }

        return componentStorage;
    }

    /**
     * Returns a unique identifier of a dynamic component.
     *
     * @return platform-specific id of the component
     */
    public int createComponentId() {
        throw new RuntimeException(msgNotImplemented);
    }

    /**
     * Gets the unique identifier of MIDlet suite's dynamic component.
     *
     * @param suiteId ID of the suite the component belongs to 
     * @param vendor name of the vendor that created the component, as
     *        given in a JAD file
     * @param name name of the component, as given in a JAD file
     *
     * @return ID of the midlet suite's component given by vendor and name
     *         or ComponentInfo.UNUSED_COMPONENT_ID if the component does
     *         not exist
     */
    public int getComponentId(int suiteId, String vendor, String name) {
        throw new RuntimeException(msgNotImplemented);
    }

    /**
     * Stores or updates a midlet suite's dynamic component.
     *
     * @param suiteStorage suite storage used to store the component
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
     * @param displayName name of the component to display to user
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
    public synchronized void storeComponent(
            MIDletSuiteStorage suiteStorage, InstallInfo installInfo,
                SuiteSettings suiteSettings, String displayName,
                    Properties jadProps, Properties jarProps)
                        throws IOException, MIDletSuiteLockedException {
        throw new RuntimeException(msgNotImplemented);
    }

    /**
     * Removes a dynamic component given its ID.
     * <p>
     * If the component is in use it must continue to be available
     * to the other components that are using it.
     *
     * @param id suite ID for the installed package
     *
     * @throws IllegalArgumentException if the component cannot be found
     * @throws MIDletSuiteLockedException is thrown, if the component is
     *                                    locked
     */
    public void removeComponent(int id)
            throws IllegalArgumentException, MIDletSuiteLockedException {
        throw new RuntimeException(msgNotImplemented);
    }

    /**
     * Removes all dynamic components belonging to the given suite.
     * <p>
     * If any component is in use, no components are removed, and
     * an exception is thrown.
     *
     * @param suiteId ID of the suite whose components must be removed
     *
     * @throws IllegalArgumentException if there is no suite with
     *                                  the specified ID
     * @throws MIDletSuiteLockedException is thrown, if any component is
     *                                    locked
     */
    public void removeAllComponents(int suiteId)
            throws IllegalArgumentException, MIDletSuiteLockedException {
        throw new RuntimeException(msgNotImplemented);
    }

    /**
     * Get the midlet suite component's class path including a path to the MONET
     * image of the specified component and a path to the suite's jar file.
     *
     * @param componentId unique ID of the dynamic component
     *
     * @return class path or null if the component does not exist
     */
    public synchronized String[] getComponentClassPath(int componentId) {
        return null;
    }

    /**
     * Returns a list of all components belonging to the given midlet suite.
     *
     * @param suiteId ID of a MIDlet suite
     *
     * @return an array of ComponentInfoImpl structures filled with the
     *         information about the installed components, or null
     *         if there are no components belonging to the given suite
     *
     * @exception IllegalArgumentException if the given suite id is invalid
     * @exception SecurityException if the caller does not have permission
     *                              to access this API
     */
    public synchronized ComponentInfo[] getListOfSuiteComponents(int suiteId)
            throws IllegalArgumentException, SecurityException {
        return null;
    }

    /**
     * Reads information about the installed midlet suite's components
     * from the storage.
     *
     * @param componentId unique ID of the component
     * @param ci ComponentInfo object to fill with the information about
     *           the midlet suite's component having the given ID
     *
     * @exception java.io.IOException if an the information cannot be read
     * @exception IllegalArgumentException if suiteId is invalid or ci is null
     */
    public void getComponentInfo(int componentId, ComponentInfo ci)
            throws IOException, IllegalArgumentException {
        throw new RuntimeException(msgNotImplemented);
    }

    /**
     * Get the class path for the specified dynamic component.
     *
     * @param componentId unique ID of the component
     *
     * @return class path or null if the component does not exist
     */
    public String getComponentJarPath(int componentId) {
        throw new RuntimeException(msgNotImplemented);
    }
}
