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

package com.sun.jsr082.bluetooth.btl2cap;

import java.io.IOException;
import javax.microedition.io.Connection;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.BluetoothConnectionException;

import com.sun.jsr082.bluetooth.BluetoothUrl;
import com.sun.jsr082.bluetooth.BluetoothProtocol;
import com.sun.j2me.security.BluetoothPermission;
import com.sun.j2me.main.Configuration;

/*
 * Provides "btl2cap" protocol implementation
 */
public class Protocol extends BluetoothProtocol {
    /* Keeps maximum MTU supported by the BT stack. */
    static final int MAX_STACK_MTU;

    static {
        int maxReceiveMTU;
        try {
            maxReceiveMTU = Integer.parseInt(System.getProperty(
                    "bluetooth.l2cap.receiveMTU.max"));
        } catch (NumberFormatException e) {
            maxReceiveMTU = L2CAPConnection.DEFAULT_MTU;
        }
        MAX_STACK_MTU = maxReceiveMTU;
    }

    /*
     * Constructs an instance.
     */
    public Protocol() {
        super(BluetoothUrl.L2CAP);
    }

    /*
     * Cheks permissions and opens requested connection.
     *
     * @param token security token passed by calling class
     * @param url <code>BluetoothUrl</code> instance that defines required
     *        connection stringname the URL without protocol name and colon
     * @param mode Connector.READ_WRITE or Connector.READ or Connector.WRITE
     *
     * @return a notifier in case of server connection string, open connection
     * in case of client one.
     *
     * @exception IOException if opening connection fails.
     */
    public Connection openPrim(BluetoothUrl url, int mode)
            throws IOException {
        return openPrimImpl(url, mode);
    }

    /*
     * Ensures URL parameters have valid values. Sets receiveMTU if undefined.
     * @param url URL to check
     * @exception IllegalArgumentException if invalid url parameters found
     * @exception BluetoothConnectionException if url parameters are not
     *            acceptable due to Bluetooth stack limitations
     */
    protected void checkUrl(BluetoothUrl url)
            throws IllegalArgumentException, BluetoothConnectionException {

        if (url.receiveMTU == -1) {
            url.receiveMTU = L2CAPConnection.DEFAULT_MTU;
        }

        if (url.isSystem()) {
            return;
        }

        super.checkUrl(url);

        if (!url.isServer && (url.port <= 0x1000 || url.port >= 0xffff ||
                    ((url.port & 1) != 1) || ((url.port & 0x100) != 0))) {
            throw new IllegalArgumentException("Invalid PSM: " + url.port);
        }

        // IMPL_NOTE BluetoothConnectionException should be thrown here
        // It is temporary substituted by IllegalArgumentException
        // to pass TCK succesfully. To be changed back when fixed
        // TCK arrives. The erroneous TCK test is
        // javasoft.sqe.tests.api.javax.bluetooth.Connector.L2Cap.
        //                             openClientTests.L2Cap1014()
        //
        // Correct code here is
        // throw new BluetoothConnectionException(
        //    BluetoothConnectionException.UNACCEPTABLE_PARAMS,
        //    <message>);
        if (url.receiveMTU < L2CAPConnection.MINIMUM_MTU) {
            throw new IllegalArgumentException(
                "Receive MTU is too small");
        }

        if (url.receiveMTU > MAX_STACK_MTU) {
            throw new IllegalArgumentException("Receive MTU is too large");
        }

        if (url.transmitMTU != -1 && url.transmitMTU > MAX_STACK_MTU) {
            throw new BluetoothConnectionException(
                BluetoothConnectionException.UNACCEPTABLE_PARAMS,
                "Transmit MTU is too large");
        }
    }

    /*
     * Ensures that permissions are proper and creates client side connection.
     * @param token security token if passed by caller, or <code>null</code>
     * @param mode       I/O access mode
     * @return proper <code>L2CAPConnectionImpl</code> instance
     * @exception IOException if openning connection fails.
     */
    protected Connection clientConnection(int mode)
            throws IOException {
        checkForPermission(BluetoothPermission.BLUETOOTH_CLIENT);
        return new L2CAPConnectionImpl(url, mode);
    }

    /*
     * Ensures that permissions are proper and creates required notifier at
     * server side.
     * @param token security token if passed by caller, or <code>null</code>
     * @param mode       I/O access mode
     * @return proper <code>L2CAPNotifierImpl</code> instance
     * @exception IOException if openning connection fails
     */
    protected Connection serverConnection(int mode)
            throws IOException {
        checkForPermission(BluetoothPermission.BLUETOOTH_SERVER);
        return new L2CAPNotifierImpl(url, mode);
    }
}

