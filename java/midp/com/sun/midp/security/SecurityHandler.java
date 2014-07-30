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

import com.sun.j2me.security.AccessController;

import com.sun.midp.lcdui.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.events.EventQueue;

import com.sun.midp.io.j2me.storage.*;
import com.sun.midp.configurator.Constants;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * Contains methods to handle with the various security state information of a
 * a MIDlet suite.
 */
public final class SecurityHandler {
    /** Session level interaction has not occured. */
    private final static byte NOT_ASKED = 0;

    /** User granted permission for this session. */
    private final static byte GRANTED = 1;

    /** User denied permission for this session. */
    private final static byte DENIED = -1;

    /** The security token for this class. */
    private static SecurityToken classSecurityToken;

    /** The standard security exception message. */
    public static final String STD_EX_MSG = "Application not authorized " +
                                            "to access the restricted API";

    /** Permission list. */
    private byte permissions[];

    /** A flag for the session value of each permission. */
    private byte sessionValues[];

    /** Maximum permission level list. */
    private byte maxPermissionLevels[];

    /** True, if trusted. */
    private boolean trusted;

    /**
     * Creates a security domain with a list of permitted actions or no list
     * to indicate all actions. The caller must be have permission for
     * <code>Permissions.MIDP</code> or be the first caller of
     * the method for this instance of the VM.
     *
     * @param apiPermissions for the token
     * @param domain name of the security domain
     *
     * @exception SecurityException if caller is not permitted to call this
     *            method
     */
    public SecurityHandler(byte[] apiPermissions, String domain) {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);
        init(apiPermissions, domain);
    }

    /**
     * Creates a security domain with a list of permitted actions or no list
     * to indicate all actions. The caller must be have permission for
     * <code>Permissions.MIDP</code> or be the first caller of
     * the method for this instance of the VM.
     *
     * @param securityToken security token of the caller
     * @param apiPermissions for the token, can be null
     * @param domain name of the security domain
     *
     * @exception SecurityException if caller is not permitted to call this
     *            method
     */
    public SecurityHandler(SecurityToken securityToken,
            byte[] apiPermissions, String domain) {
        securityToken.checkIfPermissionAllowed(Permissions.AMS);
        init(apiPermissions, domain);
    }

    /**
     * Creates a security domain with a list of permitted actions or no list
     * to indicate all actions. The caller must be have permission for
     * <code>Permissions.MIDP</code> or be the first caller of
     * the method for this instance of the VM.
     *
     * @param apiPermissions for the token
     * @param domain name of the security domain
     *
     * @exception SecurityException if caller is not permitted to call this
     *            method
     */
    private void init(byte[] apiPermissions, String domain) {
        maxPermissionLevels =
            (Permissions.forDomain(domain))[Permissions.MAX_LEVELS];

        permissions = apiPermissions;

        sessionValues = new byte[permissions.length];

        trusted = Permissions.isTrusted(domain);
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
        int i;

        synchronized (this) {
            try {
                i = Permissions.getId(permission);
            } catch (SecurityException e) {
                return 0;  //not found, report denied
            }

            switch (permissions[i]) {
            case Permissions.ALLOW:
            case Permissions.BLANKET_GRANTED:
                // report allowed
                return 1;

            case Permissions.SESSION:
                if (sessionValues[i] == GRANTED) {
                    // report allowed
                    return 1;
                }

                if (sessionValues[i] == DENIED) {
                    // report denied
                    return 0;
                }

                // fall through
            case Permissions.BLANKET:
            case Permissions.ONESHOT:
                // report unknown
                return -1;

            default:
                // Permissions.NEVER
                break;
            }

            // report denied
            return 0;
        }
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
     * @param question Resource constant for the question to ask the user
     * @param oneshotQuestion Resource constant for the oneshot question to
     *                        ask the user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     *
     * @return <code>true</code> if the permission interaction has permanently
     * changed and the new state should be saved, this will only happen
     * if the permission granted
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
            oneshotQuestion, app, resource, extraValue, STD_EX_MSG);
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
     * @return <code>true</code> if the permission interaction has permanently
     * changed and the new state should be saved, this will only happen
     * if the permission granted
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public boolean checkForPermission(String permissionStr, String title, String question,
        String oneShotQuestion, String app, String resource, String extraValue,
        String exceptionMsg) throws InterruptedException {

        if (permissions == null) {
            /* totally trusted, all permissions allowed */
            return false;
        }

        synchronized (this) {
			int permission;
			try {
				permission = Permissions.getId(permissionStr);
			} catch (SecurityException e) {
				throw new SecurityException(exceptionMsg);
			}
            if (permission >= 0 && permission < permissions.length) {
                switch (permissions[permission]) {
                case Permissions.ALLOW:
                case Permissions.BLANKET_GRANTED:
                    return false;

                case Permissions.BLANKET:
                    /* This level means the question has not been asked yet. */
                    if (askUserForPermission(classSecurityToken, trusted,
                            title, question, app, resource, extraValue)) {

                        Permissions.setPermissionGroup(permissions,
                            permission, Permissions.BLANKET_GRANTED);

                        return true;
                    }

                    Permissions.setPermissionGroup(permissions,
                        permission, Permissions.BLANKET_DENIED);
                    break;

                case Permissions.SESSION:
                    if (sessionValues[permission] == GRANTED) {
                        return false;
                    }

                    if (sessionValues[permission] == DENIED) {
                        break;
                    }

                    if (askUserForPermission(classSecurityToken, trusted,
                            title, question, app, resource, extraValue)) {
                        /*
                         * Save the fact that the question has already
                         * been asked this session.
                         */
                        Permissions.setPermissionGroup(sessionValues,
                            permission, GRANTED);

                        return false;
                    }

                    /*
                     * Save the fact that the question has already
                     * been asked this session.
                     */
                    Permissions.setPermissionGroup(sessionValues,
                        permission, DENIED);
                    break;

                case Permissions.ONESHOT:
                    if (askUserForPermission(classSecurityToken, trusted,
                            title, oneShotQuestion, app, resource,
                            extraValue)) {
                        return false;
                    }

                    break;

                default:
                    // Permissions.NEVER
                    break;
                } // switch
            } // if

            throw new SecurityException(exceptionMsg);
        } // synchronized
    }

    /**
     * Ask the user yes/no permission question.
     *
     * @param token security token with the permission to preempt the
     *        foreground display
     * @param trusted true to display the trusted icon, false to display the
     *                untrusted icon
     * @param title Resource constant for the title of the dialog
     * @param question Resource constant for the question to ask user
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

        PermissionDialog dialog =
            new PermissionDialog(token, trusted, title, question, app,
                                 resource, extraValue);

        return dialog.waitForAnswer();
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
}

/** Implements security permission dialog. */
class PermissionDialog implements CommandListener {
    /** Caches the display manager reference. */
    private DisplayEventHandler displayEventHandler;

    /** Permission Alert. */
    private Alert alert;

    /** Command object for "Yes" command. */
    private Command yesCmd =
        new Command(Resource.getString(ResourceConstants.YES),
                    Command.OK, 1);
    /** Command object for "No" command. */
    private Command noCmd =
        new Command(Resource.getString(ResourceConstants.NO),
                    Command.BACK, 1);
    /** Holds the preempt token so the form can end. */
    private Object preemptToken;

    /** Holds the answer to the security question. */
    private boolean answer;

    /**
     * Construct permission dialog.
     * <p>
     * The title, question, and answer strings will be translated,
     * if a string resource is available.
     * Since the strings can have substitution token in them, if there is a
     * "%" it must changed to "%%". If a string has a %1, the app parameter
     * will be substituted for it. If a string has a "%2, the resource
     * parameter will be substituted for it. If a string has a %3, the
     * extraValue parameter will be substituted for it.
     *
     * @param token security token with the permission to preempt the
     *        foreground display
     * @param trusted true to display the trusted icon, false to display the
     *                untrusted icon
     * @param title Resource constant for the title of the dialog
     * @param question Resource constant for the question to ask user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     *
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    PermissionDialog(SecurityToken token, boolean trusted, String title,
            String question, String app, String resource, String extraValue)
            throws InterruptedException {
        String[] substitutions = {app, resource, extraValue};
        String iconFilename;
        RandomAccessStream stream;
        byte[] rawPng;
        Image icon;
        String configRoot = File.getConfigRoot(Constants.INTERNAL_STORAGE_ID);

        alert = new Alert(Resource.getString(title, substitutions));

        displayEventHandler =
            DisplayEventHandlerFactory.getDisplayEventHandler(token);

        if (trusted) {
            iconFilename = configRoot + "trusted_icon.png";
        } else {
            iconFilename = configRoot + "untrusted_icon.png";
        }

        stream = new RandomAccessStream(token);
        try {
            stream.connect(iconFilename, Connector.READ);
            rawPng = new byte[stream.getSizeOf()];
            stream.readBytes(rawPng, 0, rawPng.length);
            stream.disconnect();
            icon = Image.createImage(rawPng, 0, rawPng.length);
            alert.setImage(icon);
        } catch (java.io.IOException noImage) {
        }

        alert.setString(Resource.getString(question, substitutions));
        alert.addCommand(noCmd);
        alert.addCommand(yesCmd);
        alert.setCommandListener(this);
        preemptToken = displayEventHandler.preemptDisplay(alert, true);
    }

    /**
     * Waits for the user's answer.
     *
     * @return user's answer
     */
    boolean waitForAnswer() {
        synchronized (this) {
            if (preemptToken == null) {
                return false;
            }

            if (EventQueue.isDispatchThread()) {
                // Developer programming error
                throw new RuntimeException(
                "Blocking call performed in the event thread");
            }

            try {
                wait();
            } catch (Throwable t) {
                return false;
            }

            return answer;
        }
    }

    /**
     * Sets the user's answer and notifies waitForAnswer and
     * ends the form.
     *
     * @param theAnswer user's answer
     */
    private void setAnswer(boolean theAnswer) {
        synchronized (this) {
            answer = theAnswer;

            /*
             * Since this may be the only display, clear the alert,
             * so the user will not be confused by alert text still
             * displaying.
             *
             * The case should happen when running TCK test MIDlets in
             * SVM mode.
             */
            alert.setTitle(null);
            alert.setString(null);
            alert.setImage(null);
            alert.addCommand(new Command("", 1, 1));
            alert.removeCommand(noCmd);
            alert.removeCommand(yesCmd);

            displayEventHandler.donePreempting(preemptToken);

            notify();
        }

    }

    /**
     * Respond to a command issued on security question form.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == yesCmd) {
            setAnswer(true);
            return;
        }

        setAnswer(false);
    }
}
