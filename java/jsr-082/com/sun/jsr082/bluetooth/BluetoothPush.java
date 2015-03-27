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
import javax.bluetooth.DiscoveryAgent;
import com.sun.jsr082.bluetooth.btl2cap.L2CAPNotifierImpl;
import com.sun.jsr082.bluetooth.btspp.BTSPPNotifierImpl;

/*
 * Bluetooth connect-anytime services support class.
 */
public class BluetoothPush {

    /* 'authenticated' AllowedSender parameter. */
    private static final String AUTHENTICATED = ";AUTHENTICATED";

    /* 'authorized' AllowedSender parameter. */
    private static final String AUTHORIZED = ";AUTHORIZED";

    /* 'blacklist' AllowedSender parameter. */
    private static final String BLACKLIST = ";BLACKLIST=";

    /* Service record serializer instance. */
    private static ServiceRecordSerializer srs = new ServiceRecordSerializer();

    /*
     * Checks if the specified URL is valid.
     *
     * @param url URL to verify
     * @throws IllegalArgumentException if the URL is malformed
     */
    public static void verifyUrl(String url) {
        BluetoothUrl btUrl = new BluetoothUrl(url);
        if ((btUrl.encrypt || btUrl.authorize) && !btUrl.authenticate) {
            throw new IllegalArgumentException(
                    "'authenticate=true' parameter is required.");
        }
    }

    /*
     * Checks if the specified AllowedSender field is valid.
     *
     * @param filter filter to verify
     * @throws IllegalArgumentException if the filter is malformed
     */
    public static void verifyFilter(String filter) {
        if (filter.length() == 0) {
            throw new IllegalArgumentException();
        }
        filter = filter.toUpperCase();
        int i = 0;
        while (i < filter.length() && i < 12) {
            char c = filter.charAt(i++);
            if (c == '*' || c == '?') {
                continue;
            }
            if (c == ';') {
                i--;
                if (i == 0) {
                    throw new IllegalArgumentException(
                            "Invalid Bluetooth address.");
                }
                break;
            }
            if ((c < '0' || c > '9') && (c < 'A' || c > 'F')) {
                throw new IllegalArgumentException(
                        "Invalid Bluetooth address.");
            }
        }
        filter = filter.substring(i);
        if (filter.length() == 0) {
            return;
        }
        if (filter.startsWith(AUTHENTICATED)) {
            filter = filter.substring(AUTHENTICATED.length());
        } else if (filter.startsWith(AUTHORIZED)) {
            filter = filter.substring(AUTHORIZED.length());
        }
        if (filter.length() == 0) {
            return;
        }
        if (!filter.startsWith(BLACKLIST)) {
            throw new IllegalArgumentException("Invalid parameter.");
        }
        filter = filter.substring(BLACKLIST.length());
        if (filter.length() == 0) {
            throw new IllegalArgumentException("Invalid blacklist.");
        }
        int count = 0;
        while (true) {
            if (++count > 1024) {
                throw new IllegalArgumentException("Blacklist too long.");
            }
            i = 0;
            while (i < filter.length() && i < 12) {
                char c = filter.charAt(i++);
                if (c == '*' || c == '?') {
                    continue;
                }
                if (c == ';') {
                    i--;
                    break;
                }
                if ((c < '0' || c > '9') && (c < 'A' || c > 'F')) {
                    throw new IllegalArgumentException(
                            "Invalid blacklist address.");
                }
            }
            filter = filter.substring(i);
            if (filter.length() == 0) {
                return;
            }
            if (filter.charAt(0) != ';' || filter.length() == 1) {
                throw new IllegalArgumentException("Invalid blacklist.");
            }
            filter = filter.substring(1);
        }
    }

    /*
     * Registers URL within Bluetooth push subsytem.
     *
     * @param url URL to register
     * @throws IOException if an I/O error occurs
     */
    public static void registerUrl(String url) throws IOException {
        ServiceRecordImpl record = null;
        String protocol = url.substring(0, url.indexOf(':')).toUpperCase();
        if (protocol.equals("BTL2CAP")) {
            record = L2CAPNotifierImpl.createServiceRecord(url);
        } else if (protocol.equals("BTSPP")) {
            record = BTSPPNotifierImpl.createServiceRecord(url);
        } else if (protocol.equals("BTGOEP")) {
            record = BTSPPNotifierImpl.createServiceRecord(url);
        } else {
            throw new RuntimeException("Unsupported Bluetooth protocol.");
        }
        record.setHandle(0);
        if (!BCC.getInstance().isBluetoothEnabled() &&
                !BCC.getInstance().enableBluetooth()) {
            throw new IOException("Bluetooth radio is not enabled.");
        }
        if (!registerUrl(url, srs.serialize(record))) {
            throw new IOException("Error registering Bluetooth URL.");
        }
        if (BCC.getInstance().getAccessCode() != DiscoveryAgent.GIAC) {
            BCC.getInstance().setAccessCode(DiscoveryAgent.GIAC);
        }
        // get the emulation services up and running
        SDDB.getInstance();
    }

    /*
     * Retrieves service record associated with a service maintained by
     * Bluetooth push subsytem.
     *
     * @param notifier Bluetooth notifier to be used with the service record
     * @param url URL used during push entry registration
     * @return service record instance
     */
    public static ServiceRecordImpl getServiceRecord(BluetoothNotifier notifier,
            String url) {
        return SDDB.getInstance().getServiceRecord(getRecordHandle(url),
                notifier);
    }

    /*
     * Registers URL within Bluetooth push subsystem.
     *
     * @param url URL to register
     * @param data serialized service record created from the URL
     * @return true on success, false on failure
     */
    private native static boolean registerUrl(String url, byte[] data);

    /*
     * Retrieves service record handle for a service maintained by
     * Bluetooth push subsytem.
     *
     * @param url URL used during push entry registration
     * @return service record handle
     */
    private native static int getRecordHandle(String url);

}
