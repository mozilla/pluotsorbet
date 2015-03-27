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

import javax.bluetooth.*;
import java.util.Vector;

/*
 * Bluetooth Control Center.
 *
 * This is a singleton which is instantiated in <code>LocalDeviceImpl</code>.
 * BCC is supposed to communicate with native application which is a central
 * authority for local Bluetooth device settings.
 *
 * To simplify porting efforts, all methods of this class work with Bluetooth
 * addresses (presented as Java strings), instead of using RemoteDeviceImpl
 * objects. Conversion between the two is performed elsewhere.
 */
public abstract class BCC {

    /* Keeps the only instance of this class. */
    private static BCC instance = null;

    /*
     * Protects the constructor to prevent unauthorized instantiation.
     */
    protected BCC() {
    }

    /*
     * Retrieves instance of this class.
     *
     * @return instance of this class
     */
    public synchronized static BCC getInstance() {
        if (instance == null) {
            instance = new NativeBCC();
        }
        return instance;
    }

    /*
     * Enables Bluetooth radio and the Bluetooth protocol stack for use.
     *
     * @return true if the operation succeeded, false otherwise
     */
    public abstract boolean enableBluetooth();

    /*
     * Queries the power state of the Bluetooth device.
     *
     * @return <code>true</code> is the Bluetooth device is on,
     *         <code>false</code> otherwise.
     */
    public abstract boolean isBluetoothEnabled();

    /*
     * Returns local Bluetooth address.
     *
     * @return local Bluetooth address.
     */
    public abstract String getBluetoothAddress();

    /*
     * Returns user-friendly name for the local device.
     *
     * @return user-friendly name for the local device, or
     *         null if the name could not be retrieved
     */
    public abstract String getFriendlyName();

    /*
     * Retrieves the user-friendly name for specified remote device.
     *
     * @param address Bluetooth address of a remote device
     * @return name of the remote device, or
     *         null if the name could not be retrieved
     */
    public abstract String getFriendlyName(String address);

    /*
     * Determines if the local device is in connectable mode.
     *
     * @return true if the device is connectable, false otherwise
     */
    public abstract boolean isConnectable();

    // JAVADOC COMMENT ELIDED
    public abstract DeviceClass getDeviceClass();

    // JAVADOC COMMENT ELIDED
    public abstract boolean setServiceClasses(int classes);

    // JAVADOC COMMENT ELIDED
    public abstract int getAccessCode();

    // JAVADOC COMMENT ELIDED
    public abstract boolean setAccessCode(int accessCode);

    /*
     * Checks if the local device has a bond with a remote device.
     *
     * @param address Bluetooth address of a remote device
     * @return true if the two devices were paired, false otherwise
     */
    public abstract boolean isPaired(String address);

    /*
     * Checks if a remote device was authenticated.
     *
     * @param address Bluetooth address of a remote device
     * @return true if the device was authenticated, false otherwise
     */
    public abstract boolean isAuthenticated(String address);

    /*
     * Checks if a remote device is trusted (authorized for all services).
     *
     * @param address Bluetooth address of a remote device
     * @return true if the device is trusted, false otherwise
     */
    public abstract boolean isTrusted(String address);

    /*
     * Checks if connections to a remote device are encrypted.
     *
     * @param address Bluetooth address of the remote device
     * @return true if connections to the device are encrypted, false otherwise
     */
    public abstract boolean isEncrypted(String address);

    /*
     * Retrieves PIN code to use for pairing with a remote device. If the
     * PIN code is not known, PIN entry dialog is displayed.
     *
     * @param address the Bluetooth address of the remote device
     * @return string containing the PIN code
     */
    public abstract String getPasskey(String address);

    /*
     * Initiates pairing with a remote device.
     *
     * @param address the Bluetooth address of the device with which to pair
     * @param pin an array containing the PIN code
     * @return true if the device was authenticated, false otherwise
     */
    public abstract boolean bond(String address, String pin);

    /*
     * Authenticates remote device.
     *
     * @param address Bluetooth address of a remote device
     * @return true if the device was authenticated, false otherwise
     */
    public abstract boolean authenticate(String address);

    /*
     * Authorizes a Bluetooth connection.
     *
     * @param address the Bluetooth address of the remote device
     * @param handle handle for the service record of the srvice the remote
     *         device is trying to access
     * @return true if authorization succeeded, false otherwise
     */
    public abstract boolean authorize(String address, int handle);

    /*
     * Enables or disables encryption of data exchanges.
     *
     * @param address the Bluetooth address of the remote device
     * @param enable specifies whether the encryption should be enabled
     * @return true if the encryption has been changed, false otherwise
     */
    public abstract boolean encrypt(String address, boolean enable);

    /*
     * Returns the list of preknown devices.
     *
     * @return vector containing preknown devices;
     *         <code>null</code> if there is no preknown devices .
     */
    public abstract Vector getPreknownDevices();

    /*
     * Checks if there is a connection to the remote device.
     *
     * @param address the Bluetooth address of the remote device
     * @return true if connection is established with the remote device
     */
    public abstract boolean isConnected(String address);

    /*
     * Registers a new connection to a remote device.
     * For the real mode makes nothing currently.
     *
     * @param address the Bluetooth address of the remote device
     */
    public void addConnection(String address) {}

    /*
     * Unregisters an existing connection to a remote device.
     * For the real mode makes nothing currently.
     *
     * @param address the Bluetooth address of the remote device
     */
    public void removeConnection(String address) {}

}
