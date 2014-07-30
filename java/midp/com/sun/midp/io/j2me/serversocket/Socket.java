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

package com.sun.midp.io.j2me.serversocket;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.StreamConnection;
import javax.microedition.io.ServerSocketConnection;

import com.sun.j2me.security.AccessController;
import com.sun.j2me.security.InterruptedSecurityException;

import com.sun.midp.io.j2me.socket.ServerSocket;

import com.sun.midp.io.Util;

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.MIDletStateHandler;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * Implements server sockets for J2ME devices.
 */
public class Socket implements ServerSocketConnection, ServerSocket {

    /** TCP server permission name. */
    private static final String SERVER_PERMISSION_NAME =
        "javax.microedition.io.Connector.serversocket";

    /**
     * Handle to native server socket object. This is set and get only by
     * native code.
     */
    private int nativeHandle = -1;

    /** Flag to indicate connection is currently open. */
    boolean connectionOpen = false;

    /**
     * Privileged security token, initialized if privileged <code>open()</code>
     * has been used, <code>null</code> otherwize.
     */
    private SecurityToken privilegedSecurityToken = null;

    /**
     * Opens a port to listen on. This method relies on
     * the MIDlet suite permissions before granting
     * rights to use the inbound connection..
     *
     * @param port TCP to listen on. If less than or equal to zero, a
     *             port will be assigned automatically.
     *
     * @exception IOException  if some other kind of I/O error occurs
     * @exception SecurityException if the current MIDlet suite does not have
     *            permission to open a server socket
     */
    public void open(int port) throws IOException {
	open(port, null);
    }

    /**
     * Opens a port with privileged security token.
     *
     * @param port TCP to listen on. If less than or equal to zero, a
     *             port will be assigned automatically.
     * @param token the security token
     *
     * @exception IOException  if some other kind of I/O error occurs
     * @exception SecurityException if the token is invalid
     */
    public void open(int port, SecurityToken token) throws IOException {
        int suiteId = MIDletSuite.UNUSED_SUITE_ID;
        try {
            MIDletStateHandler midletStateHandler;
            MIDletSuite midletSuite;

	    if (token != null) {
		token.checkIfPermissionAllowed(SERVER_PERMISSION_NAME);
		privilegedSecurityToken = token;
	    } else {
                AccessController.checkPermission(SERVER_PERMISSION_NAME,
						"TCP Server" + port);
	    }

            midletStateHandler =
                MIDletStateHandler.getMidletStateHandler();
            midletSuite = midletStateHandler.getMIDletSuite();

            if (midletSuite != null) {
                suiteId = midletSuite.getID();
            }
        } catch (SecurityException e) {
            close();

            if (e instanceof InterruptedSecurityException) {
                throw new InterruptedIOException(
		    "Interrupted while trying to ask the user permission");
            }

            throw e;
        }

        openCommon(port, suiteId);
    }

    /**
     * Common implementation of opening a port.
     *
     * @param port TCP port to listen on
     * @param suiteId id of the currently running midlet, or null if
     *                there is no currently running midlet
     * @exception IOException if an I/O error occurs
     */
    private void openCommon(int port, int suiteId) throws IOException {
        open0(port > 0 ? port : 0, suiteId);
        connectionOpen = true;
    }

    /**
     * Checks if the connection is open.
     *
     * @exception  IOException  is thrown, if the stream is not open
     */
    void ensureOpen() throws IOException {
        if (!connectionOpen) {
            throw new IOException("Connection closed");
        }
    }

    /**
     * Returns a connection that represents a server side
     * socket connection.
     * <p>
     *
     * @return     a socket to communicate with a client.
     *
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream
     */
    synchronized public StreamConnection acceptAndOpen()
        throws IOException {

        com.sun.midp.io.j2me.socket.Protocol con;

        ensureOpen();

        con = new com.sun.midp.io.j2me.socket.Protocol();

        accept0(con);

        con.open(privilegedSecurityToken);
        return con;
    }

    /**
     * Gets the local address to which the socket is bound.
     *
     * <P>The host address(IP number) that can be used to connect to this
     * end of the socket connection from an external system.
     * Since IP addresses may be dynamically assigned, a remote application
     * will need to be robust in the face of IP number reassignment.</P>
     * <P> The local hostname (if available) can be accessed from
     * <code> System.getProperty("microedition.hostname")</code>
     * </P>
     *
     * @return the local address to which the socket is bound
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public String getLocalAddress() throws IOException {
        ensureOpen();
        return getLocalAddress0();
    }

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public int getLocalPort() throws IOException {
        ensureOpen();
        return getLocalPort0();
    }

    /**
     * Closes the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection
     */
    public void close() throws IOException {
        connectionOpen = false;
        privilegedSecurityToken = null;
        close0();
    }

    /**
     * Opens a server socket connection on the given port.  If successful,
     * stores a handle directly into the nativeHandle field.  If unsuccessful,
     * throws an exception.
     *
     * @param port       TCP port to listen for connections on
     * @param suiteId    ID of current midlet suite, or null if there
     *                   is no current suite
     *
     * @exception IOException  if some other kind of I/O error occurs
     * or if reserved by another suite
     */
    private native void open0(int port, int suiteId)
        throws IOException;

    /**
     * Closes the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection
     */
    private native void close0() throws IOException;

    /**
     * Waits for an incoming TCP connection on server socket. The method
     * blocks current thread till a connection is made.
     * <p>
     * The 'con' parameter must be a freshly created client socket connection
     * object.  When an incoming connection is accepted, the socket handle for
     * the newly accepted connection is stored into this object directly from
     * native code. This technique ensures that the acceptance of a new
     * connection and the storing of the native handle are performed
     * atomically.
     *
     * @param con the client socket connection object
     *
     * @exception IOException if an I/O error has occurred
     */
    private native void accept0(
        com.sun.midp.io.j2me.socket.Protocol con) throws IOException;

    /**
     * Native finalizer
     */
    private native void finalize();

    /**
     * Gets the local IP number.
     *
     * @return     the IP address as a dotted-quad <tt>String</tt>
     */
    private native String getLocalAddress0();

    /**
     * Gets the local port to which this socket connection is bound.
     *
     * @return the local port number for this socket connection
     */
    private native int getLocalPort0();
}



