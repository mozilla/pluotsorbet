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

package com.sun.midp.io.j2me.datagram;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Datagram;
import javax.microedition.io.UDPDatagramConnection;
import javax.microedition.io.Connector;
import javax.microedition.io.Connection;

import com.sun.cldc.io.ConnectionBaseInterface;

import com.sun.j2me.security.AccessController;
import com.sun.j2me.security.InterruptedSecurityException;

import com.sun.midp.io.NetworkConnectionBase;
import com.sun.midp.io.HttpUrl;
import com.sun.midp.io.Util;

import com.sun.midp.io.j2me.push.PushRegistryInternal;

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.MIDletStateHandler;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.security.SecurityInitializer;

/**
 * This is the default "datagram://" protocol for J2ME that maps onto UDP.
 */
public class Protocol implements UDPDatagramConnection,
    ConnectionBaseInterface {

    /** UDP client permission name. */
    private static final String SERVER_PERMISSION_NAME =
        "javax.microedition.io.Connector.datagramreceiver";

    /** UDP server permission name. */
    private static final String CLIENT_PERMISSION_NAME =
        "javax.microedition.io.Connector.datagram";

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /** Initialize the native network. */
    static {
        NetworkConnectionBase.initializeNativeNetwork();
    }

    /** Lock object for reading from the datagram socket */
    private final Object readerLock = new Object();

    /** Lock object for writing to the datagram socket */
    private final Object writerLock = new Object();

    /**
     * Handle to native datagram socket object. This is set and get only by
     * native code.
     */
    private int nativeHandle = -1;

    /** Machine name from the URL connection string. */
    private String host;

    /** Port number from the URL connection string. */
    private int port;

    /** Open flag to indicate if the connection is currently open. */
    private boolean open;

    /** This needs to know that if the owner is trusted. */
    private boolean ownerTrusted;

    /**
     * Check for the required permission and open a connection to a target.
     * This method can be used with permissions greater than
     * the current MIDlet suite.
     *
     * @param checkSecurity    true if security check is required
     * @param name             URL for the connection, without the
     *                         without the protocol part
     * @param mode             I/O access mode, see {@link Connector}
     * @return                 this Connection object
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the connection cannot
     *                                        be found.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    private Connection openPrimCommon(boolean checkSecurity, String name,
            int mode) throws IOException {

        MIDletStateHandler stateHandler =
            MIDletStateHandler.getMidletStateHandler();
        MIDletSuite midletSuite = stateHandler.getMIDletSuite();

        int incomingPort = 0;
        // parse name into host and port
        HttpUrl url = new HttpUrl("datagram", name);

        /*
         * Since we reused the <code>HttpUrl</code> parser, we must
         * make sure that there was nothing past the authority in the
         * URL.
         */
        if (url.path != null || url.query != null || url.fragment != null) {
            throw new IllegalArgumentException("Malformed address " + name);

        }

        host = url.host;
        port = url.port;

        if (name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException(
                      "Protocol must start with slash slash");
        }

        /*
         * If 'host' == null then we are a server endpoint at
         * port 'port'.
         *
         * If 'host' != null we are a client endpoint at a port
         * decided by the system and the default address for
         * datagrams to be send is 'host':'port'
         *
         * If 'host' and 'port' are omitted in
         * the name, then
         * the application is requesting a system assigned port
         * number.
         */
        if (host == null) {
            if (checkSecurity) {
                try {
                    // When asking permission use Internet protocol name.
                    AccessController.checkPermission(SERVER_PERMISSION_NAME,
                        "UDP:" + name);
                } catch (SecurityException se) {
                    // Give back the connection to AMS
                    PushRegistryInternal.checkInConnectionInternal(
                        classSecurityToken, "datagram:" + name);

                    if (se instanceof InterruptedSecurityException) {
                        throw new InterruptedIOException(
                        "Interrupted while trying to ask the user permission");
                    }

                    throw se;
                }
            }

            if (port > 0) {
                incomingPort = port;
            }
        } else {
            if (port < 0) {
                throw new IllegalArgumentException("Missing port number");
            }

            if (checkSecurity) {
                try {
                    // When asking permission use Internet protocol name.
                    AccessController.checkPermission(CLIENT_PERMISSION_NAME,
                                                   "UDP:" + name);
                } catch (InterruptedSecurityException ise) {
                    throw new InterruptedIOException(
                        "Interrupted while trying to ask the user permission");
                }
            }
        }

        if (checkSecurity) {
            try {
                AccessController.checkPermission(
                    AccessController.TRUSTED_APP_PERMISSION_NAME);
                ownerTrusted = true;
            } catch (SecurityException se) {
                ownerTrusted = false;
            } 
        }

        /* Check the mode parameter. (See NetworkConnectionAdapter). */
        switch (mode) {
        case Connector.READ:
        case Connector.WRITE:
        case Connector.READ_WRITE:
            break;

        default:
            throw new IllegalArgumentException("Illegal mode");
        }

        open0(incomingPort, midletSuite.getID());
        open = true;

        return this;
    }

    /**
     * Open a connection to a target. This function would be used while
     * opening a dagaram connection from WMA. WMA version of datagram disables
     * the security check while opening a datagram connection
     *
     * <p>
     * The name string for this protocol should be:
     * "//[address:][port]"
     *
     * @param token  a security token with the javax.microedition.io.datagram
     *                permission set to the "allowed" level
     * @param name       the target of the connection
     * @param mode       a flag that is <code>true</code> if the caller
     *                   intends to write to the connection, ignored
     * @param timeouts   a flag to indicate that the called
     *                   wants timeout exceptions, ignored
     * @return this connection
     * @exception IllegalArgumentException if a parameter is invalid
     * @throws IOException if an I/O operation failed
     * @exception SecurityException if a caller is not authorized for UDP
     */
    public Connection openPrim(SecurityToken token, String name,
                               int mode, boolean timeouts) throws IOException {
        token.checkIfPermissionAllowed(CLIENT_PERMISSION_NAME);
        return openPrimCommon(false, name, mode);
    }

    /**
     * Open a connection to a target.
     * <p>
     * The name string for this protocol should be:
     * "//[address:][port]"
     *
     * @param name       the target of the connection
     * @param mode       a flag that is <code>true</code> if the caller
     *                   intends to write to the connection, ignored
     * @param timeouts   a flag to indicate that the called
     *                   wants timeout exceptions, ignored
     * @return this connection
     * @exception IllegalArgumentException if a parameter is invalid
     * @throws IOException if an I/O operation failed
     * @exception SecurityException if a caller is not authorized for UDP
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
            throws IOException {

        // IMPL NOTE remove usage of nativeHandle from Java
        if (nativeHandle != -1) {
            // This method should be called only once.
            throw new RuntimeException("Illegal state for operation");
        }

        return openPrimCommon(true, name, mode);
    }

    /**
     * Ensure that the connection is open.
     * @exception  IOException  if the connection was closed
     */
    void ensureOpen() throws IOException {
        if (!open) {
            throw new IOException("Connection closed");
        }
    }

    /**
     * Get the maximum length a datagram can be.
     *
     * @return  the length
     * @exception  IOException  if the connection was closed
     */
    public int getMaximumLength() throws IOException {
        ensureOpen();
        return getMaximumLength0();
    }

    /**
     * Get the nominal length of a datagram.
     *
     * @return    address      the length
     * @exception  IOException  if the connection was closed
     */
    public int getNominalLength() throws IOException {
        ensureOpen();
        return getNominalLength0();
    }

    /**
     * Send a datagram.
     *
     * @param     dgram        a datagram
     * @exception IOException  if an I/O error occurs
     */
    public void send(Datagram dgram) throws IOException {
        /*
         * Synchronization should be done for a writer lock and not for
         * the DatagramObject. For BSD based platforms, send on UDP sockets
         * are always synchronous(they never return IO_WOULDBLOCK). But having
         * a writer lock even for UDP send, would ensure that no more than one
         * thread is performing write for the same socket(handle) at the same
         * time.
         */
        synchronized (writerLock) {
            int length;
            int ipNumber;
            int locPort;

            ensureOpen();

            length = dgram.getLength();

            // allow zero length datagrams to be sent
            if (length < 0) {
                throw new IOException("Bad datagram length");
            }

            if (dgram instanceof DatagramObject) {
                DatagramObject dh = (DatagramObject)dgram;

                ipNumber = dh.ipNumber;
                if (ipNumber == 0) {
                    throw new IOException(
                        "No address in datagram");
                }

                locPort = dh.port;
            } else {
                // address is a datagram url
                String addr;
                HttpUrl url;
                String locHost;

                addr = dgram.getAddress();
                if (addr == null) {
                    throw new IOException(
                         "No address in datagram");
                }

                url = new HttpUrl(addr);
                locHost = url.host;
                locPort = url.port;

                if (locHost == null) {
                    throw new IOException("Missing host");
                }

                if (locPort == -1) {
                    throw new IOException("Missing port");
                }

                ipNumber = getIpNumber(locHost);

                if (ipNumber == -1) {
                    throw new IOException("Invalid host");
                }
            }

            /*
             * JTWI security check, untrusted MIDlets cannot open
             * WAP gateway ports 9200-9203.
             */
            if (!ownerTrusted && (locPort >= 9200 && locPort <= 9203)) {
                throw new SecurityException(
                    "Target port denied to untrusted applications");
            }

            while (true) {
                int res;

                try {
                    res = send0(ipNumber, locPort,
                        dgram.getData(), dgram.getOffset(), length);
                } finally {
                    if (!open) {
                        throw new InterruptedIOException("Socket closed");
                    }
                }

                if (res == dgram.getLength()) {
                    break;
                }

                if (res != 0) {
                    throw new IOException("Failed to send datagram");
                }
            }
        }
    }

    /**
     * Receive a datagram.
     *
     * @param     dgram        a datagram
     * @exception IOException  if an I/O error occurs
     */
    public void receive(Datagram dgram)
        throws IOException {

        /*
         * Synchronization should be done for a reader lock and not for
         * the DatagramObject. Reader lock would ensure that no
         * more than one thread is performing read for the same socket(handle)
         * at the same time.
         */
        synchronized (readerLock) {
            int length;
            long res;
            int count;
            int ipNumber;
            String locHost;
            int locPort;
            String addr;

            ensureOpen();

            length = dgram.getLength();

            if (length <= 0) {
                throw new IOException("Bad datagram length");
            }

            while (true) {
                try {
                    res = receive0(dgram.getData(),
                        dgram.getOffset(), length);
                } finally {
                    if (!open) {
                        throw new InterruptedIOException("Socket closed");
                    }
                }

                // check res, not count so we can receive zero length datagrams
                if (res != 0) {
                    break;
                }
            }

            count = ((int)res) & 0xffff;

            /*
             * There should be another field for bytes received so
             * the datagram can be reused without an extra effort, but
             * to be consistent with J2SE DatagramSocket we shrink the buffer
             * length.
             */
            dgram.setLength(count);

            ipNumber = (int)((res >> 32));
            locHost = addrToString(ipNumber).trim();
            locPort = (int)((res >> 16)) & 0xffff;
            addr = "datagram://" + locHost + ":" + locPort;

            if (dgram instanceof DatagramObject) {
                // save this data for sending back a message
                DatagramObject dh = (DatagramObject)dgram;
                dh.address = addr;
                dh.ipNumber = ipNumber;
                dh.port = locPort;
            } else {
                dgram.setAddress("datagram://" + locHost + ":" + locPort);
            }
        }
    }

    /**
     * Close the connection to the target.
     *
     * @exception IOException  if an I/O error occurs
     */
    public void close() throws IOException {
        if (open) {
            open = false;
	    close0();
        }
    }

    /**
     * Get a new datagram object.
     *
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @return                 a new datagram
     * @exception IOException  if an I/O error occurs
     * @exception IllegalArgumentException if the length is negative
     *                                     or larger than the buffer
    */
    public Datagram newDatagram(int size) throws IOException {

        Datagram dgram;

        ensureOpen();

        if (size < 0) {
            throw new IllegalArgumentException("Size is negative");
        }

	byte[] buf = new byte[size];

        dgram = new DatagramObject(buf, size);

        if (host != null) {
	    try {
		dgram.setAddress("datagram://" + host + ":" + port);
	    } catch (IllegalArgumentException iae) {
		// Intercept a bad address, here.
		// It'll be caught on send if used.
	    }
        }

        return dgram;
    }

    /**
     * Get a new datagram object.
     *
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @param     addr         the address to which the datagram must go
     * @return                 a new datagram
     * @exception IOException  if an I/O error occurs
     * @exception IllegalArgumentException if the length is negative or
     *                         larger than the buffer, or if the address
     *                         parameter is invalid
     */
    public Datagram newDatagram(int size, String addr) throws IOException {
        Datagram dgram = createDatagram(true, null, size);
        dgram.setAddress(addr); // override the address
        return dgram;
    }

    /**
     * Get a new datagram object.
     *
     * @param  buf             the buffer to be used in the datagram
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @return                 a new datagram
     * @exception IOException  if an I/O error occurs
     * @exception IllegalArgumentException if the length is negative or
     *                         larger than the buffer, or if the address
     *                         or buffer parameters is invalid
     */
    public Datagram newDatagram(byte[] buf, int size) throws IOException {
        return createDatagram(false, buf, size);
    }

    /**
     * Get a new datagram object.
     *
     * @param  buf             the buffer to be used in the datagram
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @param     addr         the address to which the datagram must go
     * @exception IOException  if an I/O error occurs
     * @return                 a new datagram
     */
    public Datagram newDatagram(byte[] buf, int size, String addr)
        throws IOException {

        Datagram dgram = createDatagram(false, buf, size);
        dgram.setAddress(addr); // override the address
        return dgram;
    }

    /**
     * Create a new datagram object with error checking.
     * If there is a <code>host</code> associated with the connection,
     * set the address of the datagram.
     *
     * @param  createBuffer    if true the buffer is created
     * @param  buf             the buffer to be used in the datagram
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @return                 a new datagram
     * @exception IOException  if an I/O error occurs, or the connection
     *                         was closed
     * @exception IllegalArgumentException if the length is negative or
     *                         larger than the buffer, or if the address
     *                         or buffer parameters is invalid
     */
    private Datagram createDatagram(boolean createBuffer, byte[] buf, int size)
        throws IOException {

        Datagram dgram;

        ensureOpen();

        if (size < 0) {
            throw new IllegalArgumentException("Size is negative");
        }

        if (createBuffer) {
            buf = new byte[size];
        } else if (buf == null) {
            throw new IllegalArgumentException("Buffer is invalid");
        } else if (size > buf.length) {
            throw new
                IllegalArgumentException("Size bigger than the buffer");
        }

        dgram = new DatagramObject(buf, size);

        if (host != null) {
            dgram.setAddress("datagram://" + host + ":" + port);
        }

        return dgram;
    }

    /**
     * Gets the local address to which the socket is bound.
     *
     * <P>The host address(IP number) that can be used to connect to this
     * end of the socket connection from an external system.
     * Since IP addresses may be dynamically assigned a remote application
     * will need to be robust in the face of IP number reassignment.</P>
     * <P> The local hostname (if available) can be accessed from
     * <code>System.getProperty("microedition.hostname")</code>
     * </P>
     *
     * @return the local address to which the socket is bound.
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public String getLocalAddress() throws IOException {
        ensureOpen();
	return getHost0();
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
	return getPort0();
    }

    /**
     * Opens a datagram connection on the given port.
     *
     * @param inpPort port to listen on, or 0 to have one selected
     * @param suiteId the ID of the current midlet suite
     *
     * @exception IOException  if some other kind of I/O error occurs
     *  or if reserved by another suite
     */
    private native void open0(int inpPort, int suiteId)
        throws IOException;

    /**
     * Sends a datagram.
     *
     * @param ipNumber raw IPv4 address of the remote host
     * @param inpPort UDP port of the remote host
     * @param buf the data buffer to send
     * @param off the offset into the data buffer
     * @param len the length of the data in the buffer
     * @return number of bytes sent
     * @exception IOException  if an I/O error occurs
     */
    private native int send0(int ipNumber, int inpPort,
                             byte[] buf, int off, int len)
        throws IOException;

    /**
     * Receives a datagram.
     *
     * @param buf the data buffer
     * @param off the offset into the data buffer
     * @param len the length of the data in the buffer
     * @return The upper 32 bits contain the raw IPv4 address of the
     *         host the datagram was received from. The next 16 bits
     *         contain the port. The last 16 bits contain the number
     *         of bytes received.
     * @exception IOException  if an I/O error occurs
     */
    private native long receive0(byte[] buf, int off, int len)
        throws IOException;

    /**
     * Closes the datagram connection.
     *
     * @exception IOException  if an I/O error occurs
     */
    private native void close0()
        throws IOException;

    /**
     * Get a string representation for the given raw IPv4 address.
     *
     * @param ipn raw IPv4 address
     * @return dotted-quad
     */
    static native String addrToString(int ipn);

    /**
     * Get a raw IPv4 address for the given hostname.
     *
     * @param sHost the hostname to lookup
     * @return raw IPv4 address or -1 if there was an error
     */
    public static native int getIpNumber(String sHost);

    /**
     * Get the maximum length of a datagram.
     *
     * @return the maximum length
     *
     * @exception IOException  if an I/O error occurs
     */
    native int getMaximumLength0()
        throws IOException;

    /**
     * Gets the nominal length of a datagram.
     *
     * @return the nominal length
     *
     * @exception IOException  if an I/O error occurs
     */
    native int getNominalLength0()
        throws IOException;

    /**
     * Native finalizer.
     */
    private native void finalize();

    /**
     * Gets the local IP number.
     *
     * @return the local IP address as a dotted-quad <tt>String</tt>
     */
    private static native String getHost0();

    /**
     * Gets the local port number of this datagram connection.
     *
     * @return the port number
     */
    private native int getPort0();
}
