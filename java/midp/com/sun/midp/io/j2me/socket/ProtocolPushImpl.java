/*
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

package com.sun.midp.io.j2me.socket;
import com.sun.midp.io.j2me.push.ProtocolPush;
import com.sun.midp.midlet.MIDletSuite;
import java.io.IOException;
import java.io.InterruptedIOException;
import javax.microedition.io.ConnectionNotFoundException;
import com.sun.midp.security.Permissions;

/**
 * Implementation of push behaviour.
 */
public class ProtocolPushImpl extends ProtocolPush {

    /** Instance */
    private ProtocolPushImpl pushInstance;

    /**
     * Get instance of this class.
     * @return class instance
     */
    protected ProtocolPush getInstance() {
        if (pushInstance == null) {
            pushInstance = new ProtocolPushImpl();
        }
        return (ProtocolPush)pushInstance;
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
    public void checkRegistration(String connection, String midlet,
                                  String filter) {
        checkIIPFilter(filter);
    }

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
    public void registerConnection(MIDletSuite midletSuite, String connection, 
        String midlet, String filter) 
        throws IllegalArgumentException, IOException, ClassNotFoundException {

        checkIsNotHost(connection, true);

        /*
         * Attempt to open the connection to perform security check
         * int the context of the current MIDlet suite.
         */
        try {
            Class.forName(
                "com.sun.midp.io.j2me.serversocket.Socket");
        } catch (ClassNotFoundException e) {
            throw new ConnectionNotFoundException(
                "Connection not supported");
        }

        try {
            midletSuite.checkForPermission("javax.microedition.io.Connector.serversocket",
                                            connection);
        } catch (InterruptedException ie) {
            throw new InterruptedIOException(
                "Interrupted while trying to ask the user permission");
        }
    }
}
