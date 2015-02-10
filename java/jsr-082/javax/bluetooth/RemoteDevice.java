/*
 *
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*
 * (c) Copyright 2001, 2002 Motorola, Inc.  ALL RIGHTS RESERVED.
 */
package javax.bluetooth;

import java.io.IOException;
import javax.microedition.io.Connection;
import com.sun.jsr082.bluetooth.BluetoothConnection;
import com.sun.jsr082.bluetooth.BCC;

/*
 * This class is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED
public class RemoteDevice {
    // JAVADOC COMMENT ELIDED
    private long l_address;

    // JAVADOC COMMENT ELIDED
    private String s_address;

    // JAVADOC COMMENT ELIDED
    private String friendlyName;

    // JAVADOC COMMENT ELIDED
    protected RemoteDevice(String address) {
        if (address == null) {
            throw new NullPointerException("null address");
        }
        final String errorMsg = "Malformed address: " + address;

        if (address.length() != 12) {
            throw new IllegalArgumentException(errorMsg);
        }

        if (address.startsWith("-")) {
            throw new IllegalArgumentException(errorMsg);
        }

        try {
            l_address = Long.parseLong(address, 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMsg);
        }

        // should be upper case only
        address = address.toUpperCase();

        try {
            String lAddr = LocalDevice.getLocalDevice().getBluetoothAddress();

            if (address.equals(lAddr)) {
                throw new IllegalArgumentException(
                        "Can't use the local address.");
            }
        } catch (BluetoothStateException e) {
            throw new RuntimeException("Can't initialize bluetooth support");
        }
        s_address = address;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isTrustedDevice() {
        return BCC.getInstance().isTrusted(getBluetoothAddress());
    }

    // JAVADOC COMMENT ELIDED
    public String getFriendlyName(boolean alwaysAsk) throws IOException {
        // contact the remote device if name is not known or alwaysAsk is true
        if (friendlyName == null || alwaysAsk) {
            friendlyName = BCC.getInstance().getFriendlyName(
                    getBluetoothAddress());
        }
        return friendlyName;
    }

    // JAVADOC COMMENT ELIDED
    public final String getBluetoothAddress() {
        return s_address;
    }

    // JAVADOC COMMENT ELIDED
    public boolean equals(Object obj) {
        return obj instanceof RemoteDevice &&
                l_address == ((RemoteDevice) obj).l_address;
    }

    // JAVADOC COMMENT ELIDED
    public int hashCode() {
        return (int) ((l_address >>> 24) ^ (l_address & 0xffffffL));
    }

    // JAVADOC COMMENT ELIDED
    public static RemoteDevice getRemoteDevice(Connection conn)
            throws IOException {
        return BluetoothConnection.getConnection(conn).getRemoteDevice();
    }

    // JAVADOC COMMENT ELIDED
    public boolean authenticate() throws IOException {
        if (!BCC.getInstance().isConnected(getBluetoothAddress())) {
            throw new IOException("There are no open connections between the " +
                    "local device and this RemoteDevice.");
        }

        return BCC.getInstance().authenticate(getBluetoothAddress());
    }

    // JAVADOC COMMENT ELIDED
    public boolean authorize(Connection conn) throws IOException {
        BluetoothConnection btconn = BluetoothConnection.getConnection(conn);

        if (!equals(btconn.getRemoteDevice())) {
            throw new IllegalArgumentException("The specified connection " +
                    "is not a connection to this RemoteDevice.");
        }
        if (!btconn.isServerSide()) {
            throw new IllegalArgumentException("The local device is client " +
                    "rather than the server for the specified connection.");
        }

        return authenticate() && (isTrustedDevice() || btconn.isAuthorized() ||
                btconn.authorize());
    }

    // JAVADOC COMMENT ELIDED
    public boolean encrypt(Connection conn, boolean on) throws IOException {
        BluetoothConnection btconn = BluetoothConnection.getConnection(conn);
        if (!equals(btconn.getRemoteDevice())) {
            throw new IllegalArgumentException("The specified connection " +
                    "is not a connection to this RemoteDevice.");
        }
        if (on && !authenticate()) {
            return false;
        }
        return btconn.encrypt(on);
    }

    // JAVADOC COMMENT ELIDED
    public boolean isAuthenticated() {
        return BCC.getInstance().isAuthenticated(getBluetoothAddress());
    }

    // JAVADOC COMMENT ELIDED
    public boolean isAuthorized(Connection conn) throws IOException {
        BluetoothConnection btconn = BluetoothConnection.getConnection(conn);
        RemoteDevice device;

        try {
            device = btconn.getRemoteDevice();
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (!equals(device)) {
            throw new IllegalArgumentException("The specified connection " +
                    "is not a connection to this RemoteDevice.");
        }

        return btconn.isServerSide() && btconn.isAuthorized();
    }

    // JAVADOC COMMENT ELIDED
    public boolean isEncrypted() {
        return BCC.getInstance().isEncrypted(getBluetoothAddress());
    }
} // end of class 'RemoteDevice' definition
