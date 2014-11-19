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

import com.sun.jsr082.obex.SessionNotifierImpl;
import javax.microedition.io.Connection;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.ServiceRegistrationException;

/*
 * The <code>LocalDeviceImpl</code> class is a LocalDevice
 * API class implementation. This is a singleton class.
 */
public final class LocalDeviceImpl {

    /* Set to false in RR version - then the javac skip the code. */
    private static final boolean DEBUG = false;

    /* Keeps this singleton object. */
    private static LocalDeviceImpl instance;

    /* Keeps bluetooth address of this device. */
    private String bluetoothAddress;

    /* Timeout canceller of limited discoverable mode. */
    private CancelerOfLIAC cancelerOfLIAC = new CancelerOfLIAC();

    /*
     * Device should not be in LIAC for more than 1 minute,
     * then return to the previous mode.
     */
    private class CancelerOfLIAC implements Runnable {
        /* One minute. */
        private long MINUTE = 60000;
        /* Specifies the delay for timeout checks. */
        private int RETRY_DELAY = 100; // ms
        /* Saved access code to get back to at timeout. */
        private int savedCode;
        /* Keeps canceller start time to check if timeout expired. */
        private long startTime = -1;
        /* Flaggs if LIAC mode has been cancelled from outside. */
        private boolean isCanceledFromOutside = true;

        /*
         * Starts timeout killer if new discoverable mode is LIAC.
         *
         * @param oldCode the previous value of discoverable mode.
         * @param newCode discoverable mode that has been set just.
         */
        synchronized void notifyNewAccessCode(int oldCode, int newCode) {
            if (newCode == oldCode) {
                return;
            }
            savedCode = oldCode;

            if (newCode == DiscoveryAgent.LIAC) {
                // the currentCode was not LIAC - start a killer
                startTime = System.currentTimeMillis();
                new Thread(this).start();
            } else {
                /*
                 * startTime != -1 if the killer is running, but
                 * this method may be called by the killer itself -
                 * then there is no need to stop it.
                 */
                boolean stopKiller = startTime != -1 && isCanceledFromOutside;
                startTime = -1;
                isCanceledFromOutside = false;

                if (stopKiller) {
                    try {
                        wait();
                    } catch (InterruptedException e) {}
                }
            }
        }

        /*
         * Implements of <code>run()</code> of <code>Runnable</code> interface.
         */
        public void run() {
            while (true) {
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException e) {} // ignore

                synchronized (this) {
                    // the access code was changed by application
                    if (startTime == -1) {
                        notify();
                        return;
                    }
                    // minute is running yet
                    if (System.currentTimeMillis() - startTime < MINUTE) {
                        continue;
                    }
                    // minute is over - change the mode back
                    isCanceledFromOutside = false;
                    boolean res = false;

                    try {
                        res = setDiscoverable(savedCode);
                    } catch (BluetoothStateException e) {}

                    if (!res) {
                        // not now - h-m-m, ok, try later then
                        isCanceledFromOutside = true;
                        continue;
                    }
                    return;
                }
            }
        }
    } // end of class 'CancelerOfLIAC' definition

    /*
     * Constructs the only instance of <code>LocalDeviceImpl</code>.
     */
    private LocalDeviceImpl() {
    }

    /*
     * Retrieves singleton instance.
     *
     * @return the only instance of <code>LocalDeviceImpl</code>
     * @throws BluetoothStateException if an error occured.
     */
    public static synchronized LocalDeviceImpl getInstance()
            throws BluetoothStateException {
        if (instance == null) {
            instance = new LocalDeviceImpl();
        }
        return instance;
    }

    // JAVADOC COMMENT ELIDED
    public String getFriendlyName() {
        return BCC.getInstance().getFriendlyName();
    }

    // JAVADOC COMMENT ELIDED
    public DeviceClass getDeviceClass() {
        return BCC.getInstance().getDeviceClass();
    }

    // JAVADOC COMMENT ELIDED
    public String getProperty(String property) {
        return System.getProperty(property);
    }

    // JAVADOC COMMENT ELIDED
    public int getDiscoverable() {
        return BCC.getInstance().getAccessCode();
    }

    // JAVADOC COMMENT ELIDED
    public String getBluetoothAddress() {
        return BCC.getInstance().getBluetoothAddress();
    }

    // JAVADOC COMMENT ELIDED
    public boolean setDiscoverable(int accessCode)
            throws BluetoothStateException {
        // Check if the specified mode has a valid value
        if (accessCode != DiscoveryAgent.GIAC &&
                accessCode != DiscoveryAgent.LIAC &&
                accessCode != DiscoveryAgent.NOT_DISCOVERABLE &&
                (accessCode < 0x9E8B00 || accessCode > 0x9E8B3F)) {
            throw new IllegalArgumentException("Access code is out of range: "
                    + "0x" + Integer.toHexString(accessCode).toUpperCase());
        }
        synchronized (cancelerOfLIAC) {
            /*
             * Accroding to the spec, the device should only be limited
             * discoverable (DiscoveryAgent.LIAC) for 1 minute -
             * then back to the PREVIOUS discoverable mode.
             */
            int oldAccessCode = BCC.getInstance().getAccessCode();
            if (BCC.getInstance().setAccessCode(accessCode)) {
                cancelerOfLIAC.notifyNewAccessCode(oldAccessCode, accessCode);
                if (accessCode != DiscoveryAgent.NOT_DISCOVERABLE) {
                    // Start SDDB if discoverable mode was set successfully
                    // IMPL_NOTE: Do we really need this step?
                    SDDB.getInstance();
                }
                return true;
            }
        }
        return false;
    }

    // JAVADOC COMMENT ELIDED
    public ServiceRecord getRecord(Connection notifier) {
        if (notifier == null) {
            throw new NullPointerException("Null notifier specified.");
        }
        if (!(notifier instanceof BluetoothNotifier)) {
            if (!(notifier instanceof SessionNotifierImpl)) {
                throw new IllegalArgumentException("Invalid notifier class.");
            }
            Connection transport =
                ((SessionNotifierImpl)notifier).getTransport();
            if (!(transport instanceof BluetoothNotifier)) {
                throw new IllegalArgumentException("Invalid notifier class.");
            }
            return ((BluetoothNotifier)transport).getServiceRecord();
        }
        return ((BluetoothNotifier)notifier).getServiceRecord();
    }

    // JAVADOC COMMENT ELIDED
    public void updateRecord(ServiceRecord srvRecord)
            throws ServiceRegistrationException {
        if (DEBUG) {
            System.out.println("LocalDeviceImpl.updateRecord");
        }
        if (srvRecord == null) {
            throw new NullPointerException("Null record specified.");
        }
        if (!(srvRecord instanceof ServiceRecordImpl)) {
            throw new IllegalArgumentException("Invalid service record class.");
        }
        ServiceRecordImpl record = (ServiceRecordImpl)srvRecord;
        BluetoothNotifier notifier = record.getNotifier();
        if (notifier == null) {
            throw new IllegalArgumentException(
                    "Service record is not from local SDDB.");
        }
        notifier.updateServiceRecord(record);
    }

    /*
     * Checks if Bluetooth device is turned on.
     *
     * @return <code>true</code> is the Bluetooth device is on,
     *         <code>false</code> otherwise.
     */
    public boolean isPowerOn() {
        return BCC.getInstance().isBluetoothEnabled();
    }

}
