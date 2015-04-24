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

    /** MIDlet property for the suite name. */
    public static final String SUITE_NAME_PROP    = "MIDlet-Name";

    /**
     * MIDlet property defines whether the MIDlet is paused while it's in
     * the background.
     */
    public static final String BACKGROUND_PAUSE_PROP = "MIDlet-Background-Pause";

    /** MIDlet property that deny an user to terminate the MIDlet. */
    public static final String NO_EXIT_PROP = "MIDlet-No-Exit";

    /** MIDlet property for launching the MIDlet directly in the background. */
    public static final String LAUNCH_BG_PROP = "MIDlet-Launch-Background";


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
     * Gets push options for this suite.
     *
     * @return push options are defined in {@link PushRegistryImpl}
     */
    public int getPushOptions();

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
     * Close the opened MIDletSuite
     */
    public void close();
}
