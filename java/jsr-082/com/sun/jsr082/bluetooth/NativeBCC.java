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
import com.sun.jsr082.bluetooth.BluetoothStack;
import java.util.Vector;

/*
 * Native-based Bluetooth Control Center. For many operations, this
 * implementation relies on BluetoothStack class.
 */
public class NativeBCC extends BCC {

    /*
     * Delimiter character used to separate entries in packed strings returned
     * by <code>getPreknown()</code>.
     */
    private final char ADDR_DELIMETER = ':';


    /*
     * Constructs the only instance of this class.
     */
    protected NativeBCC() {
        initialize();
    }

    /*
     * Allocates native resources.
     */
    private native void initialize();

    /*
     * Releases native resources.
     */
    protected native void finalize();

    /*
     * Enables Bluetooth radio and the Bluetooth protocol stack for use.
     *
     * @return true if the operation succeeded, false otherwise
     */
    public boolean enableBluetooth() {
        if (BluetoothStack.getInstance().isEnabled()) {
            return true;
        }
        if (!confirmEnable()) {
            return false;
        }
        return BluetoothStack.getInstance().enable();
    }

    /*
     * Queries the power state of the Bluetooth device.
     *
     * @return <code>true</code> is the Bluetooth device is on,
     *         <code>false</code> otherwise.
     */
    public boolean isBluetoothEnabled() {
        return BluetoothStack.getInstance().isEnabled();
    }

    /*
     * Asks user whether Bluetooth radio is allowed to be turned on.
     *
     * @return true if user has allowed to enable Bluetooth, false otherwise
     */
    public native boolean confirmEnable();

    /*
     * Returns local Bluetooth address.
     *
     * @return local Bluetooth address.
     */
    public String getBluetoothAddress() {
        return BluetoothStack.getInstance().getLocalAddress();
    }

    /*
     * Determines if the local device is in connectable mode.
     *
     * @return true if the device is connectable, false otherwise
     */
    public native boolean isConnectable();

    /*
     * Returns user-friendly name for the local device.
     *
     * @return user-friendly name for the local device, or
     *         null if the name could not be retrieved
     * @see LocalDevice#getFriendlyName
     */
    public String getFriendlyName() {
        return BluetoothStack.getInstance().getLocalName();
    }

    /*
     * Retrieves the user-friendly name for specified remote device.
     *
     * @param address Bluetooth address of a remote device
     * @return name of the remote device, or
     *         null if the name could not be retrieved
     * @see RemoteDevice#getFriendlyName
     */
    public String getFriendlyName(String address) {
        return BluetoothStack.getInstance().askFriendlyNameSync(address);
    }

    // JAVADOC COMMENT ELIDED
    public DeviceClass getDeviceClass() {
        return new DeviceClass(BluetoothStack.getInstance().getDeviceClass());
    }

    // JAVADOC COMMENT ELIDED
    public boolean setServiceClasses(int classes) {
        return BluetoothStack.getInstance().setServiceClasses(classes);
    }

    // JAVADOC COMMENT ELIDED
    public int getAccessCode() {
        return BluetoothStack.getInstance().getAccessCode();
    }

    // JAVADOC COMMENT ELIDED
    public boolean setAccessCode(int accessCode) {
        return BluetoothStack.getInstance().setAccessCode(accessCode);
    }

    /*
     * Checks if the local device has a bond with a remote device.
     *
     * @param address Bluetooth address of a remote device
     * @return true if the two devices were paired, false otherwise
     */
    public native boolean isPaired(String address);

    /*
     * Checks if a remote device was authenticated.
     *
     * @param address Bluetooth address of a remote device
     * @return true if the device was authenticated, false otherwise
     */
    public native boolean isAuthenticated(String address);

    /*
     * Checks if a remote device is trusted (authorized for all services).
     *
     * @param address Bluetooth address of a remote device
     * @return true if the device is trusted, false otherwise
     */
    public native boolean isTrusted(String address);

    /*
     * Checks if connections to a remote device are encrypted.
     *
     * @param address Bluetooth address of the remote device
     * @return true if connections to the device are encrypted, false otherwise
     */
    public native boolean isEncrypted(String address);

    /*
     * Retrieves PIN code to use for pairing with a remote device. If the
     * PIN code is not known, PIN entry dialog is displayed.
     *
     * @param address the Bluetooth address of the remote device
     * @return string containing the PIN code
     */
    public native String getPasskey(String address);

    /*
     * Initiates pairing with a remote device.
     *
     * @param address the Bluetooth address of the device with which to pair
     * @param pin an array containing the PIN code
     * @return true if the device was authenticated, false otherwise
     */
    public native boolean bond(String address, String pin);

    /*
     * Authenticates remote device.
     *
     * @param address Bluetooth address of a remote device
     * @return true if the device was authenticated, false otherwise
     */
    public boolean authenticate(String address) {
        if (isAuthenticated(address)) {
            return true;
        }
        if (!isPaired(address)) {
            String pin = getPasskey(address);
            if (pin == null || !bond(address, pin)) {
                return false;
            }
        }

        return BluetoothStack.getInstance().authenticateSync(address);
    }

    /*
     * Authorizes a Bluetooth connection.
     *
     * @param address Bluetooth address of a remote device
     * @param handle handle for the service record of the srvice the remote
     *         device is trying to access
     * @return true if authorization succeeded, false otherwise
     */
    public native boolean authorize(String address, int handle);

    /*
     * Enables or disables encryption of data exchanges.
     *
     * @param address the Bluetooth address of the remote device
     * @param enable indicated whether the encryption needs to be enabled
     * @return true if the encryption has been changed, false otherwise
     */
    public boolean encrypt(String address, boolean enable) {
        if (setEncryption(address, enable)) {
            return BluetoothStack.getInstance().encryptSync(address, enable);
        }
        return false;
    }

    /*
     * Returns list of preknown devices in a packed string.
     *
     * @return vector containing preknown devices
     */
    public Vector getPreknownDevices() {
        return listDevices(getPreknown());
    }

    /*
     * Returns list of preknown devices in a packed string.
     *
     * @return packed string containing preknown devices
     */
    private native String getPreknown();

    /*
     * Extracts Bluetooth addresses from a packed string.
     * In the packed string, each device entry is a Bluetooth device address
     * followed by <code>ADDR_DELIMETER</code> delimiter.
     *
     * @param packed string containing Bluetooth addresses
     * @return Vector containing Bluetooth addresses
     */
    private Vector listDevices(String packed) {
        if (packed == null || packed.trim().length() == 0) {
            return null;
        }

        Vector addrs = new Vector();
        int index = 0;
        while (index < packed.length()) {
            int end = packed.indexOf(ADDR_DELIMETER, index);
            if (end == -1) {
                end = packed.length();
            }
            addrs.addElement(packed.substring(index, end));
            index = end + 1;
        }
        return addrs;
    }

    /*
     * Checks if there is a connection to the remote device.
     *
     * @param address the Bluetooth address of the remote device
     * @return true if connection is established with the remote device
     */
    public native boolean isConnected(String address);

    /*
     * Increases or decreases encryption request counter for a remote device.
     *
     * @param address the Bluetooth address of the remote device
     * @param enable indicated whether the encryption needs to be enabled
     * @return true if the encryption needs to been changed, false otherwise
     */
    public native boolean setEncryption(String address, boolean enable);

}
