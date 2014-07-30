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

package com.sun.midp.io.j2me.push;

import java.io.IOException;
import java.io.InterruptedIOException;

import com.sun.j2me.security.AccessControlContext;
import com.sun.j2me.security.AccessController;
import com.sun.j2me.security.InterruptedSecurityException;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

/**
 * Internal push functionality for CLDC stack.
 */
public final class PushRegistryInternal {
    /**
     * Push option to only launch this suite when not other applications
     * are running.
     */
    public static final int PUSH_OPT_WHEN_ONLY_APP =
            ConnectionRegistry.PUSH_OPT_WHEN_ONLY_APP;

    /**
     * Hides the default constructor.
     */
    private PushRegistryInternal() { }

    /**
      * Validates that the method is invoked allowed party.
      * <p>
      * Method requires com.sun.midp.ams permission.
      */
    private static void checkInvocationAllowed() {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);
    }

    /**
     * Start listening for push notifications. Will throw a security
     * exception if token does not have the com.sun.midp.ams
     * permission allowed.
     *
     * @param token security token of the calling class
     */
    public static void startListening(SecurityToken token) {
        token.checkIfPermissionAllowed(Permissions.AMS);
        ConnectionRegistry.startListening();
    }

    /**
     * Register a dynamic connection with the
     * application management software. Once registered,
     * the dynamic connection acts just like a
     * connection preallocated from the descriptor file.
     * The internal implementation includes the storage name
     * that uniquely identifies the <code>MIDlet</code>.
     * This method bypasses the class loader specific checks
     * needed by the <code>Installer</code>.
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * @param context Access control context the suite
     * @param midletSuite MIDlet suite for the suite registering,
     *                   the suite only has to implement isRegistered,
     *                   and getID.
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *               when new external data is available
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
     * @param bypassChecks if true, bypass the permission checks,
     *         used by the installer when redo old connections during an
     *         aborted update
     *
     * @exception  IllegalArgumentException if the connection string is not
     *               valid
     * @exception ConnectionNotFoundException if the runtime system does not
     *              support push delivery for the requested
     *              connection protocol
     * @exception IOException if the connection is already
     *              registered or if there are insufficient resources
     *              to handle the registration request
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *               name can not be found in the current
     *               <code>MIDlet</code> suite
     * @exception SecurityException if the <code>MIDlet</code> does not
     *              have permission to register a connection
     *
     * @see #unregisterConnection
     */
    public static void registerConnectionInternal(AccessControlContext context,
                                                  MIDletSuite midletSuite,
                                                  String connection,
                                                  String midlet,
                                                  String filter,
                                                  boolean bypassChecks)
        throws ClassNotFoundException, IOException {

        checkInvocationAllowed();

        if (filter == null) {
            throw new IllegalArgumentException("filter is null");
        }

        if (!bypassChecks) {
            try {
                context.checkPermission(PushRegistryImpl.PUSH_PERMISSION_NAME);
            } catch (InterruptedSecurityException ise) {
                throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
            }

            PushRegistryImpl.checkMidletRegistered(midletSuite, midlet);

            ConnectionRegistry.checkRegistration(connection, midlet, filter);
        }

        ConnectionRegistry.registerConnectionInternal(
                midletSuite,
                connection, midlet, filter,
                !bypassChecks);
    }

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public static void initSecurityToken(SecurityToken token) {
        token.checkIfPermissionAllowed(Permissions.AMS);
        ConnectionRegistry.initSecurityToken(token);
    }

    /**
     * Return a list of registered connections for given
     * <code>MIDlet</code> suite.
     *
     * @param id identifies the specific <code>MIDlet</code>
     *               suite to be launched
     * @param available if <code>true</code>, only return the list of
     *      connections with input available
     *
     * @return array of connection strings, where each connection is
     *       represented by the generic connection <em>protocol</em>,
     *       <em>host</em> and <em>port number</em> identification
     */
    public static String listConnections(int id, boolean available) {
        checkInvocationAllowed();
        return ConnectionRegistry.listConnections(id, available);
    }

    /**
     * Unregister all the connections for a <code>MIDlet</code> suite.
     *
     * @param id identifies the specific <code>MIDlet</code>
     *               suite
     */
    public static void unregisterConnections(int id) {
        checkInvocationAllowed();
        ConnectionRegistry.delAllForSuite0(id);
    }

    /**
     * Sets the flag which enables push launches to take place.
     *
     * @param enable set to <code>true</code> to enable launching
     *  of MIDlets based on alarms and connection notification
     *  events, otherwise set to <code>false</code> to disable
     *  launches
     */
    public static void enablePushLaunch(boolean enable) {
        checkInvocationAllowed();
        ConnectionRegistry.pushEnabled = enable;
    }

    /**
     * Check in a push connection into AMS so the owning MIDlet can get
     * launched next time data is pushed. This method is used when a MIDlet
     * will not be able to get the connection and close (check in) the
     * connection for some reason. (normally because the user denied a
     * permission)
     * <p>
     * For datagram connections this function will discard the cached message.
     * <p>
     * For server socket connections this function will close the
     * accepted connection.
     *
     * @param token security token of the calling class
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *              and <em>port number</em>
     *              (optional parameters may be included
     *              separated with semi-colons (;))
     * @exception IllegalArgumentException if the connection string is not
     *              valid
     * @exception SecurityException if the <code>MIDlet</code> does not
     *              have permission to clear a connection
     * @return <code>true</code> if the check in was successful,
     *         <code>false</code> the connection was not registered.
     * @see #unregisterConnection
     */
    public static boolean checkInConnectionInternal(SecurityToken token,
                                                    String connection) {
        if (token == null) {
            throw new SecurityException("This API is for internal use only");
        }
        token.checkIfPermissionAllowed(Permissions.AMS);
        return ConnectionRegistry.checkInConnectionInternal(connection);
    }

    /**
     * Sets the flag which indicates that the AMS is operating in MVM
     * single MIDlet mode.
     */
    public static void setMvmSingleMidletMode() {
        checkInvocationAllowed();
        ConnectionRegistry.mvmSingleMidletMode = true;
    }
}
