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

import javax.microedition.io.ConnectionNotFoundException;

import com.sun.j2me.security.AccessController;
import com.sun.j2me.security.InterruptedSecurityException;

import com.sun.midp.midlet.MIDletStateHandler;
import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.security.Permissions;

/**
 * PushRegistry implementation that checks parameters.
 */
public final class PushRegistryImpl {
    /** Name of the permission permission. */
    static final String PUSH_PERMISSION_NAME =
        "javax.microedition.io.PushRegistry";

    /**
     * Hides the default constructor.
     */
    private PushRegistryImpl() { }

    /**
     * Gets a midlet suite object.
     *
     * @return A reference to <code>MIDletSuite</code> object
     */
    private static MIDletSuite getMIDletSuite() {
        return MIDletStateHandler
                .getMidletStateHandler()
                .getMIDletSuite();
    }

    /**
     * Checks that the midlet is registered for the given suite.
     *
     * @param midletSuite host <code>MIDlet suite</code> (MUST be not
     *  <code>null</code>
     *
     * @param midlet Name of <code>MIDlet</code> class to check
     *
     * @throws ClassNotFoundException if <code>MIDlet</code> is not registered
     */
    static void checkMidletRegistered(
            final MIDletSuite midletSuite,
            final String midlet)
            throws ClassNotFoundException {
        // assert midletSuite != null;
        // RFC: the check below might be unnecessary
        if (midlet == null || midlet.length() == 0) {
            throw new ClassNotFoundException("MIDlet missing");
        }

        if (!midletSuite.isRegistered(midlet)) {
            throw new ClassNotFoundException("No MIDlet-<n> registration");
        }
    }

    /**
     * Checks validity of midlet class name.
     *
     * @param midletSuite host <code>MIDlet suite</code> (MUST be not
     *  <code>null</code>
     *
     * @param midlet Name of <code>MIDlet</code> class to check
     *
     * @throws ClassNotFoundException if <code>MIDlet</code> is invalid
     */
    private static void checkMidlet(
            final MIDletSuite midletSuite,
            final String midlet)
            throws ClassNotFoundException {
        checkMidletRegistered(midletSuite, midlet);

        /*
         * IMPL_NOTE: strings in MIDlet-<n> attributes (see the check
         *  above) are not verified to be the names of classes that
         *  subclass javax.microedition.midlet.MIDlet, therefore we
         *  need this check
         */
        final Class midletCls = ConnectionRegistry.loadApplicationClass(midlet);
	final boolean isMIDlet = Class.forName("javax.microedition.midlet.MIDlet").isAssignableFrom(midletCls);

        if (!isMIDlet) {
            throw new ClassNotFoundException("Not a MIDlet");
        }
    }

    /**
     * Register a dynamic connection with the
     * application management software. Once registered,
     * the dynamic connection acts just like a
     * connection preallocated from the descriptor file.
     * The internal implementation includes the storage name
     * that uniquely identifies the <code>MIDlet</code>.
     *
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *               when new external data is available
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
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
     * @exception IllegalStateException if the MIDlet suite context is
     *              <code>null</code>.
     * @see #unregisterConnection
     */
    public static void registerConnection(
            final String connection,
            final String midlet,
            final String filter)
            throws ClassNotFoundException,
                ConnectionNotFoundException, IOException {
        // Quick check of connection to be spec complaint
        if (connection == null) {
            throw new IllegalArgumentException("connection is null");
        }

        // Quick check of filter to be spec complaint
        if (filter == null) {
            throw new IllegalArgumentException("filter is null");
        }

        final MIDletSuite midletSuite = getMIDletSuite();

        /* This method should only be called by running MIDlets. */
        if (midletSuite == null) {
            throw new IllegalStateException("Not in a MIDlet context");
        }

        /*
         * Validate parameters and Push connection availability
         */
        checkMidlet(midletSuite, midlet);
        /*
         * Check permissions.
         */
        try {
            AccessController.
                checkPermission(PUSH_PERMISSION_NAME);
        } catch (InterruptedSecurityException ise) {
            throw new InterruptedIOException(
                "Interrupted while trying to ask the user permission");
        }

        ConnectionRegistry.registerConnection(
                midletSuite, connection, midlet, filter);
    }

    /**
     * Remove a dynamic connection registration.
     *
     * @param connection generic connection <em>protocol</em>,
     *             <em>host</em> and <em>port number</em>
     * @exception SecurityException if the connection was
     *            not registered by the current <code>MIDlet</code>
     *            suite
     * @return <code>true</code> if the unregistration was successful,
     *         <code>false</code> the  connection was not registered.
     * @see #registerConnection
     */
    public static boolean unregisterConnection(final String connection) {

        /* Verify the connection string before using it. */
        if (connection == null || connection.length() == 0) {
            return false;
        }

        final MIDletSuite midletSuite = getMIDletSuite();
        if (midletSuite == null) {
            return false;
        }

        return ConnectionRegistry.unregisterConnection(midletSuite, connection);
    }

    /**
     * Return a list of registered connections for the current
     * <code>MIDlet</code> suite.
     *
     * @param available if <code>true</code>, only return the list of
     *      connections with input available
     * @return array of connection strings, where each connection is
     *       represented by the generic connection <em>protocol</em>,
     *       <em>host</em> and <em>port number</em> identification
     */
    public static String [] listConnections(final boolean available) {
        final MIDletSuite midletSuite = getMIDletSuite();
        if (midletSuite == null) {
            return null;
        }

        return ConnectionRegistry.listConnections(midletSuite, available);
    }

    /**
     * Retrieve the registered <code>MIDlet</code> for a requested connection.
     *
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *              and <em>port number</em>
     *              (optional parameters may be included
     *              separated with semi-colons (;))
     * @return  class name of the <code>MIDlet</code> to be launched,
     *              when new external data is available, or
     *              <code>null</code> if the connection was not
     *              registered
     * @see #registerConnection
     */
    public static String getMIDlet(final String connection) {

        /* Verify that the connection requested is valid. */
        if (connection == null || connection.length() == 0) {
            return null;
        }

        final MIDletSuite midletSuite = getMIDletSuite();
        if (midletSuite == null) {
            throw new IllegalStateException("Not in a MIDlet context");
        }

        return ConnectionRegistry.getMIDlet(midletSuite, connection);
    }

    /**
     * Retrieve the registered filter for a requested connection.
     *
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *              and <em>port number</em>
     *              (optional parameters may be included
     *              separated with semi-colons (;))
     * @return a filter string indicating which senders
     *              are allowed to cause the MIDlet to be launched or
     *              <code>null</code> if the connection was not
     *              registered
     * @see #registerConnection
     */
    public static String getFilter(final String connection) {

        /* Verify that the connection requested is valid. */
        if (connection == null || connection.length() == 0) {
            return null;
        }

        final MIDletSuite midletSuite = getMIDletSuite();
        if (midletSuite == null) {
            throw new IllegalStateException("Not in a MIDlet context");
        }

        return ConnectionRegistry.getFilter(midletSuite, connection);
    }

    /**
     * Register a time to launch the specified application. The
     * <code>PushRegistry</code> supports one outstanding wake up
     * time per <code>MIDlet</code> in the current suite. An application
     * is expected to use a <code>TimerTask</code> for notification
     * of time based events while the application is running.
     * <P>If a wakeup time is already registered, the previous value will
     * be returned, otherwise a zero is returned the first time the
     * alarm is registered. </P>
     *
     * @param midlet  class name of the <code>MIDlet</code> within the
     *                current running <code>MIDlet</code> suite
     *                to be launched,
     *                when the alarm time has been reached
     * @param time time at which the <code>MIDlet</code> is to be executed
     *        in the format returned by <code>Date.getTime()</code>
     * @return the time at which the most recent execution of this
     *        <code>MIDlet</code> was scheduled to occur,
     *        in the format returned by <code>Date.getTime()</code>
     * @exception ConnectionNotFoundException if the runtime system does not
     *              support alarm based application launch
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *              name can not be found in the current
     *              <code>MIDlet</code> suite
     * @see Date#getTime()
     * @see Timer
     * @see TimerTask
     */
    public static long registerAlarm(
            final String midlet,
            final long time)
            throws ClassNotFoundException, ConnectionNotFoundException {

        final MIDletSuite midletSuite = getMIDletSuite();
        if (midletSuite == null) {
            throw new IllegalStateException("Not in a MIDlet context");
        }

        checkMidlet(midletSuite, midlet);

        AccessController.checkPermission(PUSH_PERMISSION_NAME);

        return ConnectionRegistry.registerAlarm(midletSuite, midlet, time);
    }
}
