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

package com.sun.midp.security;

import javax.microedition.io.*;

import javax.microedition.lcdui.*;

import com.sun.midp.lcdui.*;

import com.sun.midp.midlet.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.events.EventQueue;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * Contains methods to handle with the various security state information of a
 * a MIDlet suite.
 */
public final class SecurityHandler {

    /** The security token for this class. */
    private static SecurityToken classSecurityToken;

    /** The standard security exception message. */
    public static final String STD_EX_MSG = "Application not authorized " +
                                            "to access the restricted API";
    /**
     * Creates a security domain with a list of permitted actions or no list
     * to indicate all actions. The caller must be have permission for
     * <code>Permissions.MIDP</code> or be the first caller of
     * the method for this instance of the VM.
     *
     * @param ApiPermissions for the token
     * @param domain name of the security domain
     *
     * @exception SecurityException if caller is not permitted to call this
     *            method
     */
    public SecurityHandler(byte[] ApiPermissions, String domain) {
    	// No-op since we don't cache any permissions in Java
    	// Query native security manager every time
    }

    /**
     * Creates a security domain with a list of permitted actions or no list
     * to indicate all actions. The caller must be have permission for
     * <code>Permissions.MIDP</code> or be the first caller of
     * the method for this instance of the VM.
     *
     * @param securityToken security token of the caller
     * @param ApiPermissions for the token, can be null
     * @param domain name of the security domain
     *
     * @exception SecurityException if caller is not permitted to call this
     *            method
     */
    public SecurityHandler(SecurityToken securityToken,
            byte[] ApiPermissions, String domain) {
    	// Do not cache anything
    	// Ask native security manager everytime
    }

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
    public int checkPermission(String permission) {
        int status = 0;
        int permId;

        try {
            permId = Permissions.getId(permission);
        	MIDletSuite current =
		MIDletStateHandler.getMidletStateHandler().getMIDletSuite();

        	if (current != null) {
        		// query native security mgr for status
                status = checkPermissionStatus0(current.getID(), permId);
        	}
        } catch (SecurityException exc) {
            // intentionally ignored
        }
        
        return status;
    }

    /**
     * Check for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     * <p>
     * The title, and question strings will be translated,
     * if a string resource is available.
     * Since the strings can have substitution token in them, if there is a
     * "%" it must changed to "%%". If a string has a %1, the app parameter
     * will be substituted for it. If a string has a "%2, the resource
     * parameter will be substituted for it. If a string has a %3, the
     * extraValue parameter will be substituted for it.
     *
     * @param permission ID of the permission to check for,
     *      the ID must be from
     *      {@link com.sun.midp.security.Permissions}
     * @param title Resource constant for the title of the dialog
     * @param question Resource constant for the question to ask user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     *
     * @return true if the permission was allow and was not allowed
     *    before
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public boolean checkForPermission(String permission, String title, String question,
        String oneshotQuestion, String app, String resource, String extraValue)
        throws InterruptedException {

        return checkForPermission(permission, title, question,
            oneshotQuestion, app, resource, extraValue,
            SecurityToken.STD_EX_MSG);
    }


    /**
     * Check for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     * <p>
     * The title, question, and answer strings will be translated,
     * if a string resource is available.
     * Since the strings can have substitution token in them, if there is a
     * "%" it must changed to "%%". If a string has a %1, the app parameter
     * will be substituted for it. If a string has a "%2, the resource
     * parameter will be substituted for it. If a string has a %3, the
     * extraValue parameter will be substituted for it.
     *
     * @param permission ID of the permission to check for,
     *      the ID must be from
     *      {@link com.sun.midp.security.Permissions}
     * @param title Resource constant for the title of the dialog
     * @param question Resource constant for the question to ask user
     * @param oneShotQuestion Resource constant for the oneshot question to
     *                        ask the user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     * @param exceptionMsg message if a security exception is thrown
     *
     * @return <code>true</code> if the permission was allowed and was
     * not allowed before; <code>false</code>, if permission is granted..
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public boolean checkForPermission(String permission, String title, String question,
        String oneShotQuestion, String app, String resource, String extraValue,
        String exceptionMsg) throws InterruptedException {
    	
    	MIDletSuite current =
            MIDletStateHandler.getMidletStateHandler().getMIDletSuite();

        if (current != null) {
            // can throw SecurityException
            int permId = Permissions.getId(permission);
            if (checkPermission0(current.getID(), permId)) {
                return false;
            }
        }
        throw new SecurityException(STD_EX_MSG);
    }

    /**
     * Ask the user yes/no permission question.
     *
     * @param token security token with the permission to preempt the
     *        foreground display
     * @param title Resource constant for the title of the dialog
     * @param question Resource constant for the question to ask user
     * @param oneShotQuestion Resource constant for the oneshot question to
     *                        ask the user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     *
     * @return true if the user says yes else false
     *
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public static boolean askUserForPermission(SecurityToken token,
            boolean trusted, String title, String question, String app,
            String resource, String extraValue) throws InterruptedException {
    	// Allow Push interrupt since the decision is already made
	// at native Push level
        return true;
    }

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken != null) {
            return;
        }

        classSecurityToken = token;
    }
    
    /**
     * Query native security manager for permission.
     * This call may block if user needs to be asked.
     *
     * @param suiteId the MIDlet suite the permission should be checked against
     * @param permission the permission id
     * 
     * @return true if permission is granted. Otherwise, false.
     */
    private native boolean checkPermission0(int suiteId, int permission);
    
    /**
     * Get the status of the specified permission.
     * This is to implement public API MIDlet.checkPermission()
     * and will not block calling thread.
     * 
     * If no API on the device defines the specific permission
     * requested then it must be reported as denied.
     * If the status of the permission is not known because it might
     * require a user interaction then it should be reported as unknown.
     *
     * @param suiteId the MIDlet suite the permission should be checked against
     * @param permission to check if denied, allowed, or unknown.
     * @return 0 if the permission is denied; 1 if the permission is allowed;
     *  -1 if the status is unknown
     */
    private native int checkPermissionStatus0(int suiteId,
					      int permission);
}
