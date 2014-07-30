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

import java.util.Enumeration;

import javax.microedition.io.ConnectionNotFoundException;

import com.sun.midp.io.Util;

import com.sun.midp.main.*;

import com.sun.midp.midletsuite.MIDletSuiteStorage;

import com.sun.midp.midlet.MIDletStateHandler;
import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.security.SecurityToken;

import com.sun.midp.log.Logging;

/**
 * CLDC implementation of ConnectionRegistry.
 */
final class ConnectionRegistry
    implements Runnable, MIDletProxyListListener {

    /**
     * Push option to only launch this suite when not other applications
     * are running.
     */
    static final int PUSH_OPT_WHEN_ONLY_APP = 1;

    /** This class has a different security domain than the MIDlet suite. */
    private static SecurityToken classSecurityToken;

    /**
     * Flag to control when push launching is permitted.
     * This flag is set to false by the AMS when installing or removing
     * MIDlets, when an interruption could compromise the integrity of
     * the operation.
     */
    static boolean pushEnabled = true;

    /**
     * This flag is set to true by the AMS when running in MVM singal MIDlet
     * mode. In this mode the current MIDlet that is not the application
     * manager should be destroyed before the next MIDlet is started.
     */
    static boolean mvmSingleMidletMode;

    /** MIDlet proxy list reference. */
    private MIDletProxyList midletProxyList;

    /** Cached reference to the midlet suite storage instance. */
    private static MIDletSuiteStorage storage;

    /**
     * Start listening for push notifications. Will throw a security
     * exception if called by any thing other than the MIDletSuiteLoader.
     */
    static void startListening() {
        (new Thread(new ConnectionRegistry())).start();
    }

    /**
     * Keeps an object of this class from being created out of
     * the startListening method.
     * Will throw a security exception if called by any thing other
     * than the MIDletSuiteLoader.
     */
    private ConnectionRegistry() {
        /*
         * Will throw a security exception if called by any thing other
         * than the MIDletSuiteLoader.
         */
        midletProxyList =
            MIDletProxyList.getMIDletProxyList(classSecurityToken);
        midletProxyList.addListener(this);

        if (storage == null) {
            storage = MIDletSuiteStorage.getMIDletSuiteStorage(
                classSecurityToken);
        }
    }

    /**
     * Run the polling loop to check for inbound connections.
     */
    public void run() {
        int fd = -1;
        int ret = 0;
        while (true) {
            try {
                fd = poll0(System.currentTimeMillis());
                if (fd != -1) {
                    if (pushEnabled) {
                        byte[] registryEntry = new byte[512];
                        if ((ret = getMIDlet0(fd, registryEntry, 512)) == 0) {
                            String name = Util.toJavaString(registryEntry);
                            launchEntry(name);
                        }
                    } else {
                        checkInByHandle0(fd);
                    }
                }
            } catch (Exception e) {
                if (Logging.TRACE_ENABLED) {
                    Logging.trace(e, null);
                }
            }
        }
    }

    /**
     * Parse the registration entry and launch the associated
     * <code>MIDlet</code>.
     * @param name registration string for connection and
     * <code>MIDlet</code> to be launched
     */
    private void launchEntry(String name) {
        String conn;
        String midlet;
        String filter;
        String strSuiteId;
        int id;
        MIDletSuite next = null;

        /*
         * Parse the comma separated values  -
         *  " connection, midlet,  filter, id"
         *  "  midlet,  wakeup, midlet suite ID"
         */
        int comma1 = name.indexOf(',', 0);
        int comma2 = name.indexOf(',', comma1 + 1);
        int comma3 = name.indexOf(',', comma2 + 1);

        if (comma3 == -1) {
            /* Alarm was triggered */
            conn = null;
            midlet = name.substring(0, comma1).trim();
            strSuiteId = name.substring(comma2+1).trim();
        } else {
            conn = name.substring(0, comma1).trim();
            midlet = name.substring(comma1+1, comma2).trim();
            filter = name.substring(comma2+1, comma3).trim();
            strSuiteId = name.substring(comma3+1).trim();
        }

        try {
            /*
             * IMPL_NOTE: here it's assumed that when a suiteId is converted
             * to string the padding zeroes are placed _before_ the value,
             * for ex., suiteId 3 is converted into "00000003".
             * MIDletSuiteStorage.stringToSuiteId() API should be added later.
             */
            id = Integer.parseInt(strSuiteId);
        } catch (NumberFormatException nfe) {
            id = MIDletSuite.UNUSED_SUITE_ID;
        }

        try {
            /*
             * Check to see if the MIDlet is already started.
             */
            if (midletProxyList.isMidletInList(id, midlet)) {
                if (conn != null) {
                    checkInConnectionInternal(conn);
                }

                return;
            }

            next = storage.getMIDletSuite(id, false);
            if (next == null) {
                if (conn != null) {
                    checkInConnectionInternal(conn);
                }

                return;
            }

            if ((next.getPushOptions() & PUSH_OPT_WHEN_ONLY_APP) != 0 &&
                    !onlyAppManagerRunning()) {
                if (conn != null) {
                    checkInConnectionInternal(conn);
                }

                return;
            }

            if (!next.permissionToInterrupt(conn)) {
                // user does not want the interruption
                if (conn != null) {
                    checkInConnectionInternal(conn);
                }

                return;
            }

            if (MIDletSuiteUtils.execute(classSecurityToken, id, midlet,
                                          null)) {
                /* We are in SVM mode, destroy all running MIDlets. */
                MIDletStateHandler.getMidletStateHandler().destroySuite();
            } else if (mvmSingleMidletMode) {
                destroyAppMidlets();
            }
        } catch (Throwable e) {
            // Could not launch requested push entry
            if (conn != null) {
                checkInConnectionInternal(conn);
            }

            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, null);
            }
        } finally {
            if (next != null) {
                next.close();
            }
        }
    }

    /**
     * Check to see if only the application manager MIDlet is running.
     *
     * @return true if only the application manager is running
     */
    private boolean onlyAppManagerRunning() {
        Enumeration midlets = midletProxyList.getMIDlets();

        while (midlets.hasMoreElements()) {
            MIDletProxy midlet = (MIDletProxy)midlets.nextElement();

            if (midlet.getSuiteId() != MIDletSuite.INTERNAL_SUITE_ID ||
                    midlet.getClassName().indexOf("Manager") == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Destroy every MIDlet except the application manager midlet.
     * This should only be used in MVM Signal MIDlet Mode.
     */
    private void destroyAppMidlets() {
        Enumeration midlets = midletProxyList.getMIDlets();

        while (midlets.hasMoreElements()) {
            MIDletProxy midlet = (MIDletProxy)midlets.nextElement();

            if (midlet.getSuiteId() == MIDletSuite.INTERNAL_SUITE_ID &&
                    midlet.getClassName().indexOf("Manager") != -1) {
                continue;
            }

            midlet.destroyMidlet();
        }
    }

    /**
     * Called when a MIDlet is added to the list, not used by this class.
     *
     * @param midlet The proxy of the MIDlet being added
     */
    public void midletAdded(MIDletProxy midlet) {}

    /**
     * Called when the state of a MIDlet in the list is updated.
     *
     * @param midlet The proxy of the MIDlet that was updated
     * @param fieldId code for which field of the proxy was updated
     */
    public void midletUpdated(MIDletProxy midlet, int fieldId) {}

    /**
     * Called when a MIDlet is removed from the list, the connections
     * in "launch pending" state for this MIDlet will be checked in.
     *
     * @param midlet The proxy of the removed MIDlet
     */
    public void midletRemoved(MIDletProxy midlet) {
        checkInByMidlet0(midlet.getSuiteId(), midlet.getClassName());
    }

    /**
     * Called when error occurred while starting a MIDlet object. The
     * connections in "launch pending" state for this MIDlet will be checked
     * in.
     *
     * @param externalAppId ID assigned by the external application manager
     * @param suiteId Suite ID of the MIDlet
     * @param className Class name of the MIDlet
     * @param errorCode start error code
     * @param errorDetails start error code
     */
    public void midletStartError(int externalAppId, int suiteId,
                                 String className, int errorCode,
                                 String errorDetails) {
        checkInByMidlet0(suiteId, className);
    }

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken == null) {
            classSecurityToken = token;
        }
    }

    /**
     * Register a dynamic connection.
     *
     * @param midletSuite <code>MIDlet</code> suite to register connection for
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *               when new external data is available
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
     *
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *               name can not be found in the current
     *               <code>MIDlet</code> suite
     * @exception IOException if the connection is already
     *              registered or if there are insufficient resources
     *              to handle the registration request
     * @see #unregisterConnection
     */
    public static void registerConnection(MIDletSuite midletSuite,
            String connection, String midlet, String filter)
        throws ClassNotFoundException, IOException {

        checkRegistration(connection, midlet, filter);
        registerConnectionInternal(midletSuite,
                            connection, midlet, filter, true);
    }

    /**
     * Check the registration arguments.
     * @param connection preparsed connection to check
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *               when new external data is available
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
     * @exception  IllegalArgumentException if connection or filter is not
     *               valid
     * @exception ConnectionNotFoundException if PushRegistry doesn't support
     *               this kind of connections
     */
    static void checkRegistration(String connection, String midlet,
                                  String filter)
                                  throws ConnectionNotFoundException {
        ProtocolPush.getInstance(connection)
            .checkRegistration(connection, midlet, filter);
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
     *
     * @param midletSuite MIDlet suite for the suite registering,
     *                   the suite only has to implement isRegistered,
     *                   checkForPermission, and getID.
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *               when new external data is available
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
     * @param registerConnection if true, register a connection with a
     *         protocol,
     *         used by the installer when redo old connections during an
     *         aborted update
     *
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *               name can not be found in the current
     *               <code>MIDlet</code> suite
     * @exception IOException if the connection is already
     *              registered or if there are insufficient resources
     *              to handle the registration request
     *
     * @see #unregisterConnection
     */
    static void registerConnectionInternal(
            final MIDletSuite midletSuite,
            final String connection,
            final String midlet,
            final String filter,
            final boolean registerConnection)
        throws ClassNotFoundException, IOException {

        if (registerConnection) {
            /*
             * No need to register connection when bypassChecks: restoring
             * RFC: why add0 below?
             */
            ProtocolPush.getInstance(connection)
                .registerConnection(midletSuite, connection, midlet, filter);
        }

        String asciiRegistration = connection
                  + "," + midlet
                  + "," + filter
                  + "," + suiteIdToString(midletSuite);

        int ret = add0(asciiRegistration);
        if (ret == -1) {
            // in case of Bluetooth URL, unregistration within Bluetooth
            // PushRegistry was already performed by add0()
            throw new IOException("Connection already registered: " + connection);
        } else if (ret == -2) {
            throw new OutOfMemoryError("Connection registering");
        } else if (ret == -3) {
            throw new IllegalArgumentException("Connection not found");
        }
    }

    /**
     * Remove a dynamic connection registration.
     *
     * @param midletSuite <code>MIDlet</code> suite to unregister connection
     *             for
     * @param connection generic connection <em>protocol</em>,
     *             <em>host</em> and <em>port number</em>
     * @exception SecurityException if the connection was
     *            not registered by the current <code>MIDlet</code>
     *            suite
     * @return <code>true</code> if the unregistration was successful,
     *         <code>false</code> the  connection was not registered.
     * @see #registerConnection
     */
    public static boolean unregisterConnection(MIDletSuite midletSuite,
            String connection) {

        int ret =  del0(connection, suiteIdToString(midletSuite));
        if (ret == -2) {
            throw new SecurityException("wrong suite");
        }
        return ret != -1;
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
    static boolean checkInConnectionInternal(final String connection) {

        /* Verify that the connection requested is valid. */
        if (connection == null || connection.length() == 0) {
            throw new IllegalArgumentException("Connection missing");
        }

        byte[] asciiRegistration = Util.toCString(connection);

        return checkInByName0(asciiRegistration) != -1;
    }

    /**
     * Return a list of registered connections for the current
     * <code>MIDlet</code> suite.
     *
     * @param midletSuite <code>MIDlet</code> suite to list connections for
     * @param available if <code>true</code>, only return the list of
     *      connections with input available
     * @return array of connection strings, where each connection is
     *       represented by the generic connection <em>protocol</em>,
     *       <em>host</em> and <em>port number</em> identification
     */
    public static String [] listConnections(MIDletSuite midletSuite,
            boolean available) {

        return connectionsToArray(listConnections(midletSuite.getID(),
                               available));
    }

    /**
     * Return a list of registered connections for given
     * <code>MIDlet</code> suite. AMS permission is required.
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
    static String listConnections(int id, boolean available) {
        byte[] nativeID;
        String connections = null;
        byte[] connlist;

        nativeID = Util.toCString(suiteIdToString(id));
        connlist = new byte[512];

        if (list0(nativeID, available, connlist, 512) == 0) {
            connections = Util.toJavaString(connlist);
        }

        return connections;
    }

    /**
     * Converts connections as string into string array.
     *
     * @param connections Connections to convert
     * @return array of connections
     */
    private static String [] connectionsToArray(String connections) {
        if (connections == null) {
            return new String[0];
        }

        /* Count the commas in the returned string */
        int count = 0;
        int offset = 0;

        do {
            offset = connections.indexOf(',', offset + 1);
            count ++;
        } while (offset > 0);

        /* Now parse out the connections for easier access by caller. */
        String[] ret = new String[count];
        int start = 0;
        for (int i = 0; i < count; i++) {
            offset = connections.indexOf(',', start);
            if (offset > 0) {
                /* Up to the next comma */
                ret[i] = connections.substring(start, offset);
            } else {
                /* From the last comma to the end of the string. */
                ret[i] = connections.substring(start);
            }
            start = offset + 1;
        }

        return ret;
    }

    /**
     * Retrieve the registered <code>MIDlet</code> for a requested connection.
     *
     * @param midletSuite suite to fetch class name for
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
    public static String getMIDlet(MIDletSuite midletSuite, String connection) {

        String midlet = null;
        byte[] asciiConn = Util.toCString(connection);
        byte[] registryEntry = new byte[512];

        if (getEntry0(asciiConn, registryEntry, 512) == 0) {
            String name = Util.toJavaString(registryEntry);
            try {
                int comma1 = name.indexOf(',', 0);
                int comma2 = name.indexOf(',', comma1 + 1);

                midlet = name.substring(comma1+1, comma2).trim();
            } catch (Exception e) {
                if (Logging.TRACE_ENABLED) {
                    Logging.trace(e, null);
                }
            }
        }
        return  midlet;
    }

    /**
     * Retrieve the registered filter for a requested connection.
     *
     * @param midletSuite suite to fetch filter for
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
    public static String getFilter(MIDletSuite midletSuite, String connection) {

        String filter = null;
        byte[] asciiConn = Util.toCString(connection);
        byte[] registryEntry = new byte[512];

        if (getEntry0(asciiConn, registryEntry, 512) == 0) {
            String name = Util.toJavaString(registryEntry);
            try {
                int comma1 = name.indexOf(',', 0);
                int comma2 = name.indexOf(',', comma1 + 1);
                int comma3 = name.indexOf(',', comma2 + 1);

                filter = name.substring(comma2+1, comma3).trim();
            } catch (Exception e) {
                if (Logging.TRACE_ENABLED) {
                    Logging.trace(e, null);
                }
            }
        }
        return  filter;
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
     * @param midletSuite <code>MIDlet</code> suite to register alarm for
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
     */
    public static long registerAlarm(MIDletSuite midletSuite,
            String midlet, long time)
            throws ConnectionNotFoundException {

        byte[] asciiName = Util.toCString(midlet + ","
                  + time + ","
                  + suiteIdToString(midletSuite));
        return addAlarm0(asciiName, time);
    }

    /**
     * Loads application class given its name.
     *
     * @param className name of class to load
     * @return instance of class
     * @throws ClassNotFoundException if the class cannot be located
     */
    static Class loadApplicationClass(final String className)
            throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
      * Converts <code>MIDlet</code> suite ID into a string.
      *
      * @param suiteId <code>MIDlet</code> suite to convert ID of
      * @return string representation
      */
    private static String suiteIdToString(final int suiteId) {
        // assert storage != null; // Listener should be started before
        return storage.suiteIdToString(suiteId);
    }

    /**
      * Converts <code>MIDlet</code> suite ID into a string.
      *
      * @param midletSuite <code>MIDlet</code> suite to convert ID of
      * @return string representation
      */
    private static String suiteIdToString(final MIDletSuite midletSuite) {
        // assert midletSuite != null;
        return suiteIdToString(midletSuite.getID());
    }

    /**
     * Native connection registry add connection function.
     * @param connection string to register
     * @return 0 if successful, -1 if failed
     */
    private static native int add0(String connection);

    /**
     * Native function to test registered inbound connections
     * for new connection notification.
     * @param time current time to use for alarm checks
     * @return handle for the connection with inbound connection
     *         pending.
     */
    private native int poll0(long time);

    /**
     * Native connection registry lookup for MIDlet name from file
     * descriptor.
     * @param handle file descriptor of registered active connection
     * @param regentry registered entry
     * @param entrysz maximum string that will be accepted
     * @return 0 if successful, -1 if failed
     */
    private static native int getMIDlet0(int handle, byte[] regentry,
           int entrysz);

    /**
     * Native connection registry lookup registry entry from a
     * specific connection.
     * @param connection registered connection string
     * @param regentry registered entry
     * @param entrysz maximum string that will be accepted
     * @return 0 if successful, -1 if failed
     */
    private static native int getEntry0(byte[]connection, byte[] regentry,
           int entrysz);

    /**
     * Native connection registry add alarm function.
     * @param midlet string to register
     * @param time
     * @return 0 if unregistered, otherwise the time of the previous
     *         registered alarm
     */
    private static native long addAlarm0(byte[] midlet, long time);

    /**
     * Native connection registry del connection function.
     * @param connection string to register
     * @param storage current suite storage name
     * @return 0 if successful, -1 if failed
     */
    private static native int del0(String connection, String storage);

    /**
     * Native connection registry check in connection function.
     * @param connection string to register
     * @return 0 if successful, -1 if failed
     */
    private static native int checkInByName0(byte[] connection);

    /**
     * Native connection registry check in connection function.
     * @param handle native handle of the connection
     */
    private static native void checkInByHandle0(int handle);

    /**
     * Native connection registry method to check in connections that are in
     * launch pending state for a specific MIDlet.
     *
     * @param suiteId Suite ID of the MIDlet
     *        array
     * @param className Class name of the MIDlet as zero terminated ASCII
     *        byte array
     */
    private static native void checkInByMidlet0(int suiteId,
                                                String className);

    /**
     * Native connection registry list connection function.
     * @param midlet string to register
     * @param available if <code>true</code>, only return the list of
     *      connections with input available
     * @param connectionlist comma separated string of connections
     * @param listsz maximum string that will be accepted in connectionlist
     * @return 0 if successful, -1 if failed
     */
    private static native int list0(byte[] midlet, boolean available,
            byte[] connectionlist, int listsz);

    /**
     * Native connection registry delete a suite's connections function.
     * @param id suite's ID
     */
    static native void delAllForSuite0(int id);
}
