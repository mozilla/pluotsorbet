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

package com.sun.midp.midlet;

import com.sun.midp.security.SecurityToken;

/**
 * Represents a MIDlet suite.
 */
public interface MIDletSuite {
    /** Suite ID that is never used. */
    public final static int UNUSED_SUITE_ID = 0;

    /** Suite ID used for internal midlet suites. */
    public final static int INTERNAL_SUITE_ID = -1;

    /** Filename of Manifest inside the application archive. */
    public static final String JAR_MANIFEST       = "META-INF/MANIFEST.MF";

    /** MIDlet property for the size of the application data. */
    public static final String DATA_SIZE_PROP     = "MIDlet-Data-Size";

    /** MIDlet property for the size of the application archive. */
    public static final String JAR_SIZE_PROP      = "MIDlet-Jar-Size";

    /** MIDlet property for the application archive URL. */
    public static final String JAR_URL_PROP       = "MIDlet-Jar-URL";

    /** MIDlet property for the suite name. */
    public static final String SUITE_NAME_PROP    = "MIDlet-Name";

    /** MIDlet property for the suite vendor. */
    public static final String VENDOR_PROP        = "MIDlet-Vendor";

    /** MIDlet property for the suite version. */
    public static final String VERSION_PROP       = "MIDlet-Version";

    /** MIDlet property for the suite description. */
    public static final String DESC_PROP        = "MIDlet-Description";

    /** MIDlet property for the microedition configuration. */
    public static final String CONFIGURATION_PROP =
        "MicroEdition-Configuration";

    /** MIDlet property for the profile. */
    public static final String PROFILE_PROP       = "MicroEdition-Profile";

    /** MIDlet Runtime Execution Environment (MIDP.CLDC by default) */
    public static final String RUNTIME_EXEC_ENV_PROP =
        "Runtime-Execution-Environment";

    /** Default value for the Runtime-Execution-Environment property */
    public static final String RUNTIME_EXEC_ENV_DEFAULT = "MIDP.CLDC";

    /** MIDlet property for the required permissions. */
    public static final String PERMISSIONS_PROP     = "MIDlet-Permissions";

    /** MIDlet property for the optional permissions. */
    public static final String PERMISSIONS_OPT_PROP = "MIDlet-Permissions-Opt";
 
    /** MIDlet property for the maximum heap size allowed for the MIDlet. */
    public static final String HEAP_SIZE_PROP = "MIDlet-Heap-Size";

    /**
     * MIDlet property defines whether the MIDlet is paused while it's in
     * the background.
     */
    public static final String BACKGROUND_PAUSE_PROP = "MIDlet-Background-Pause";

    /** MIDlet property that deny an user to terminate the MIDlet. */
    public static final String NO_EXIT_PROP = "MIDlet-No-Exit";

    /** MIDlet property for launching the MIDlet directly in the background. */
    public static final String LAUNCH_BG_PROP = "MIDlet-Launch-Background";

    /** MIDlet property for launching the MIDlet during system start-up. */
    public static final String LAUNCH_POWER_ON_PROP = "MIDlet-Launch-Power-On";


    /**
     * Get a property of the suite. A property is an attribute from
     * either the application descriptor or JAR Manifest.
     *
     * @param key the name of the property
     * @return A string with the value of the property.
     *    <code>null</code> is returned if no value is available for
     *          the key.
     */
    public String getProperty(String key);

    /**
     * Gets push setting for interrupting other MIDlets.
     * Reuses the Permissions.
     *
     * @return push setting for interrupting MIDlets the value
     *        will be permission level from {@link Permissions}
     */
    public byte getPushInterruptSetting();

    /**
     * Gets push options for this suite.
     *
     * @return push options are defined in {@link PushRegistryImpl}
     */
    public int getPushOptions();

    /**
     * Gets list of permissions for this suite.
     *
     * @return array of permissions from {@link Permissions}
     */
    public byte[] getPermissions();

    /**
     * Replace or add a property to the suite for this run only.
     *
     * @param token token with the AMS permission set to allowed,
     *        can be null to use the suite's permission
     * @param key the name of the property
     * @param value the value of the property
     *
     * @exception SecurityException if the caller's token does not have
     *            internal AMS permission
     */
    public void setTempProperty(SecurityToken token, String key, String value);

    /**
     * Get the name of a MIDlet to display to the user.
     *
     * @param className classname of a MIDlet in the suite
     *
     * @return name to display to the user
     */
    public String getMIDletName(String className);

    /**
     * Check to see the suite has the ALLOW level for specific permission.
     * This is used for by internal APIs that only provide access to
     * trusted system applications.
     * <p>
     * @deprecated To maintain compatiblity
     * with future security models like Java SE and CDC, APIs should use
     * <code>com.sun.j2me.security.AccessController.checkPermission</code>
     * instead of this method.
     *
     * @param permission permission name from JCP spec or OEM spec
     *
     * @exception SecurityException if the suite is not
     *            allowed to perform the specified action.
     */
    public void checkIfPermissionAllowed(String permission);

    /**
     * Check for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     * <p>
     * @deprecated To maintain compatiblity
     * with future security models like Java SE and CDC, APIs should use
     * <code>com.sun.j2me.security.AccessController.checkPermission</code>
     * instead of this method.
     *
     * @param permission permission name from JCP spec or OEM spec
     * @param resource string to insert into the question, can be null if
     *        no %2 in the question
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public void checkForPermission(String permission, String resource)
        throws InterruptedException;

    /**
     * Checks for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     * <p>
     * @deprecated To maintain compatiblity
     * with future security models like Java SE and CDC, APIs should use
     * <code>com.sun.j2me.security.AccessController.checkPermission</code>
     * instead of this method.
     *
     * @param permission permission name from JCP spec or OEM spec
     * @param resource string to insert into the question, can be null if
     *        no %2 in the question
     * @param extraValue string to insert into the question,
     *        can be null if no %3 in the question
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public void checkForPermission(String permission, String resource,
        String extraValue) throws InterruptedException;

    /**
     * Get the status of the specified permission.
     * If no API on the device defines the specific permission
     * requested then it must be reported as denied.
     * If the status of the permission is not known because it might
     * require a user interaction then it should be reported as unknown.
     *
     * @param permission to check if denied, allowed, or unknown.
     * @return 0 if the permission is denied; 1 if the permission is allowed;
     *  -1 if the status is unknown
     */
    public int checkPermission(String permission);

    /**
     * Gets the unique ID of the suite.
     *
     * @return suite ID
     */
    public int getID();

    /**
     * Ask the user want to interrupt the current MIDlet with
     * a new MIDlet that has received network data.
     *
     * @param connection connection to place in the permission question or
     *        null for alarm
     *
     * @return true if the use wants interrupt the current MIDlet, else false
     */
    public boolean permissionToInterrupt(String connection);

    /**
     * Indicates if the named MIDlet is registered in the suite
     * with MIDlet-&lt;n&gt; record in the manifest or
     * application descriptor.
     * @param midletClassName class name of the MIDlet to be checked
     *
     * @return true if the MIDlet is registered
     */
    public boolean isRegistered(String midletClassName);

    /**
     * Indicates if this suite is trusted.
     * (not to be confused with a domain named "trusted",
     * this is used for extra checks beyond permission checking)
     *
     * @return true if the suite is trusted false if not
     */
     public boolean isTrusted();

    /**
     * Check whether the suite classes are preverified and
     * the suite content hasn't been changed since installation
     *
     * @return true if no more verification needed, false otherwise
     */
    public boolean isVerified();

    /**
     * Determine if the a MIDlet from this suite can be run. Note that
     * disable suites can still have their settings changed and their
     * install info displayed.
     *
     * @return true if suite is enabled, false otherwise
     */
    public boolean isEnabled();

    /**
     * Close the opened MIDletSuite
     */
    public void close();
}
