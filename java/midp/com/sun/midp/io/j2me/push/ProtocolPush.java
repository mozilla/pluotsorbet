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
import com.sun.midp.midlet.MIDletSuite;
import java.io.IOException;
import javax.microedition.io.ConnectionNotFoundException;
import com.sun.midp.main.Configuration;
import com.sun.midp.io.HttpUrl;

/**
 * JSR's implementations will provide push behaviour.
 */
public abstract class ProtocolPush {

    /**
     * Number of delimiter characters in IP v4 address
     */
    protected static final int IP4_DELIMITER_COUNT = 3;

    /**
     * Get instance of this class.
     * @return class instance
     */
    protected abstract ProtocolPush getInstance();

    /**
     * Get instance of class depends on protocol.
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @return class instance
     * @exception  IllegalArgumentException if the connection string is not
     *               valid
     * @exception  ConnectionNotFoundException if the protocol is not
     *               supported or invalid
     */
    public static ProtocolPush getInstance(String connection) 
        throws IllegalArgumentException, ConnectionNotFoundException {

        /* Verify that the connection requested is valid. */
        if (connection == null || connection.length() == 0) {
            throw new IllegalArgumentException("Connection is empty");
        }

        int index = connection.indexOf(':');
        if (index == -1) {
            throw new IllegalArgumentException("Protocol field is omitted");
        }

        String className = Configuration.getProperty
            (connection.substring(0, index).toLowerCase());

        if (className == null || className.length() == 0) {
            throw new ConnectionNotFoundException("Protocol is invalid " +
                "or not supported");
        }

        try {
            ProtocolPush cl = (ProtocolPush)Class.forName(className).newInstance();
            return cl.getInstance();
        } catch (ClassNotFoundException exc) {
            throw new ConnectionNotFoundException("Protocol is not supported");
        } catch (ClassCastException exc) {
            throw new RuntimeException(
                    "System error loading class " + className + ": " + exc);
        } catch (IllegalAccessException exc) {
            throw new RuntimeException(
                    "System error loading class " + className + ": " + exc);
        } catch (InstantiationException exc) {
            throw new RuntimeException(
                    "System error loading class " + className + ": " + exc);
        }
    }

    /**
     * Called when registration is checked.
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *               when new external data is available
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
     * @exception  IllegalArgumentException if the connection string is not
     *               valid
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *               name can not be found in the current
     *               <code>MIDlet</code> suite
     */
    public abstract void checkRegistration(String connection, String midlet,
                                  String filter);

    /**
     * Called when registration is established.
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
     * @exception  IllegalArgumentException if the connection string is not
     *               valid
     * @exception IOException if the connection is already
     *              registered or if there are insufficient resources
     *              to handle the registration request
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *               name can not be found in the current
     *               <code>MIDlet</code> suite
     */
    public abstract void registerConnection(MIDletSuite midletSuite, String connection, 
        String midlet, String filter) 
        throws IllegalArgumentException, IOException, ClassNotFoundException;

    /**
     * Check if host is not present.
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @param checkPort check if the port is not omitted
     * @exception  IllegalArgumentException if the connection contains no port
     *               value
     * @exception ConnectionNotFoundException if connection contains any host
     *               name
     */
    protected void checkIsNotHost(String connection, boolean checkPort) 
        throws IllegalArgumentException, ConnectionNotFoundException {
        HttpUrl url = new HttpUrl(connection);
        // Server connections do not have a host
        if (url.host != null) {
            throw new ConnectionNotFoundException(
                "Connection not supported");
        }

        if (checkPort && url.port == -1) {
            throw new IllegalArgumentException("Port missing");
        }
    }

    /**
     * Check IP filter is valid.
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
     * @exception  IllegalArgumentException if the connection contains no port
     *               value
     */
    protected void checkIIPFilter(String filter) 
        throws IllegalArgumentException {
        int len = filter.length();
        int dotCount = 0;
        boolean dotUnexpected = true;
        boolean failed = false;

        /* IP address characters only for other connections. */
        /* Check for special case - single * char. This is valid filter. */
        if (!"*".equals(filter)) {
            /* All other filters shall be in IPv4 format. */
            for (int i = 0; i < len && !failed; i++) {
                char c = filter.charAt(i);

                if (c == '.') {
                    if (dotUnexpected || i == len-1) {
                        failed = true;
                    } else {
                        dotCount++;
                        if (dotCount > IP4_DELIMITER_COUNT) {
                            failed = true;
                        }
                        dotUnexpected = true;
                    }
                } else
                    if (c != '?' && c != '*' && !('0' <= c && c <= '9')) {
                        /* The only acceptable characters are [*?0-9] */
                        failed = true;
                    } else {
                        dotUnexpected = false;
                    }
            }

            if (failed || dotCount < IP4_DELIMITER_COUNT) {
                throw new IllegalArgumentException("IP Filter \"" + filter + "\" is invalid");
            }
        }
    }
}
