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
package com.sun.jsr082.bluetooth;

import java.io.IOException;
import com.sun.jsr082.obex.ObexPacketStream;
import com.sun.jsr082.obex.ObexTransport;

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.BluetoothConnectionException;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;

/*
 * Base class for all bluetooth connections.
 */
abstract public class BluetoothConnection {

    /* Keeps requested connection details. */
    protected BluetoothUrl url;

    /* Keeps open mode. */
    protected int mode;

    /* true if this connection was authorized, false otherwise. */
    private boolean authorized;

    /* true if this connection has requested encryption and is encrypted. */
    private boolean encrypted;

    /* Remote device for this connection. */
    private RemoteDevice remoteDevice;

    /*
     * Retrieves <code>BluetoothConnection</code> from given one.
     * Connection given is supposed to be either Bluetooth connection
     * or a connection that uses Bluetooth as transport. All involved
     * connections supposed to be open.
     *
     * @param conn the connection to extract Bluetooth connection from
     * @return proper <code>BluetoothConnection</code> instance
     * @throws IllegalArgumentException if connection is neither an instance
     *         of BluetoothConnection, nor uses one as transport.
     * @throws IOException if connection given or transport is closed or
     *         transport is invalid.
     */
    public static BluetoothConnection getConnection(Connection conn)
            throws IOException {
        if (conn == null) {
            throw new NullPointerException("Null connection specified.");
        }

        if (conn instanceof ObexPacketStream) {
            conn = ((ObexPacketStream)conn).getTransport();
        }

        if (!(conn instanceof BluetoothConnection)) {
            throw new IllegalArgumentException("The specified connection " +
                    "is not a Bluetooth connection.");
        }

        BluetoothConnection btConn = (BluetoothConnection)conn;
        btConn.checkOpen();
        return btConn;
    }

    /*
     * Creates a new instance of this class.
     *
     * @param url connection url
     * @param mode I/O access mode server side otherwise it's false
     */
    protected BluetoothConnection(BluetoothUrl url, int mode) {
        // IMPL_NOTE: find proper place; the intent here is to start EmulationPolling
        // and SDPServer prior to create a user's notifier
        SDDB.getInstance();

        this.url = url;
        this.mode = mode;
    }

    /*
     * Returns remote device for this connection.
     *
     * @return <code>RemoteDevice</code> object for this connection
     * @throws IOException if this connection is closed
     */
    public RemoteDevice getRemoteDevice() throws IOException {
        checkOpen();
        return remoteDevice;
    }

    /*
     * Returns Bluetooth address of the remote device for this connection.
     *
     * @return Bluetooth address of the remote device
     */
    public abstract String getRemoteDeviceAddress();

    /*
     * Retrieves reference to the remote device for this connection.
     */
    protected void setRemoteDevice() {
        remoteDevice = DiscoveryAgentImpl.getInstance().
                getRemoteDevice(getRemoteDeviceAddress());
        BCC.getInstance().addConnection(getRemoteDeviceAddress());
    }

    /*
     * Removes reference to the remote device.
     */
    protected void resetRemoteDevice() {
        if (encrypted) {
            encrypt(false);
        }
        remoteDevice = null;
        BCC.getInstance().removeConnection(getRemoteDeviceAddress());
    }

    /*
     * Determines if this connection is closed.
     *
     * @return true if this connection is closed, false otherwise
     */
    public boolean isClosed() {
        return remoteDevice == null;
    }

    /*
     * Determines whether this connection represents the server side,
     * i.e. this connection was created by a notifier in acceptAndOpen().
     *
     * @return true if this connection is a server-side connection,
     *         false otherwise
     */
    public boolean isServerSide() {
        return url.isServer;
    }

    /*
     * Returns the authorization state of this connection.
     *
     * @return true if this connection has been authorized, false otherwise
     */
    public boolean isAuthorized() {
        return authorized;
    }

    /*
     * Authorizes this connection. It is assumed that the remote device has
     * previously been authenticated. This connection must represent the server
     * side, i.e. isServer() should return true.
     *
     * @return true if the operation succeeded, false otherwise
     */
    public boolean authorize() {
        authorized = BCC.getInstance().authorize(
                remoteDevice.getBluetoothAddress(), getServiceRecordHandle());
        return authorized;
    }

    /*
     * Changes encryption for this connection.
     *
     * @param enable specifies whether encription should be turned on or off
     * @return true if encryption has been set as required, false otherwise
     */
    public boolean encrypt(boolean enable) {
        if (enable == encrypted) {
            return true;
        }
        BCC.getInstance().encrypt(remoteDevice.getBluetoothAddress(), enable);
        if (remoteDevice.isEncrypted()) {
            if (enable) {
                encrypted = true;
                return true;
            }
            encrypted = false;
            return false;
        } else {
            if (enable) {
                return false;
            }
            encrypted = false;
            return true;
        }
    }

    /*
     * Returns handle for the service record of the service this connection
     * is attached to. Valid for server-side (incoming) connections only.
     *
     * @return service record handle, or 0 if the handle is not available
     */
    protected int getServiceRecordHandle() {
        return 0;
    }

    /*
     * Checks if this connection is open.
     *
     * @throws IOException if this connection is closed
     */
    protected void checkOpen() throws IOException {
        if (isClosed()) {
            throw new IOException("Connection is closed.");
        }
    }

    /*
     * Performs security checks, such as authentication, authorization, and
     * encryption setup.
     *
     * @throws BluetoothConnectionException when failed
     */
    protected void checkSecurity()
            throws BluetoothConnectionException, IOException {
        if (url.authenticate) {
            if (!remoteDevice.authenticate()) {
                throw new BluetoothConnectionException(
                        BluetoothConnectionException.SECURITY_BLOCK,
                        "Authentication failed.");
            }
        }
        if (url.authorize) {
            if (!remoteDevice.authorize((Connection)this)) {
                throw new BluetoothConnectionException(
                        BluetoothConnectionException.SECURITY_BLOCK,
                        "Authorization failed.");
            }
        }
        if (url.encrypt) {
            if (!remoteDevice.encrypt((Connection)this, true)) {
                throw new BluetoothConnectionException(
                        BluetoothConnectionException.SECURITY_BLOCK,
                        "Encryption failed.");
            }
        }
    }

    /*
     * Checks read access.
     *
     * @throws IOException if open mode does not permit read access
     */
    protected void checkReadMode() throws IOException {
        if ((mode & Connector.READ) == 0) {
            throw new IOException("Invalid mode: " + mode);
        }
    }

    /*
     * Checks write access.
     *
     * @throws IOException if open mode does not permit write access
     */
    protected void checkWriteMode() throws IOException {
        if ((mode & Connector.WRITE) == 0) {
            throw new IOException("Invalid mode: " + mode);
        }
    }

}
