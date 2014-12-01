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
import com.sun.jsr082.bluetooth.LocalDeviceImpl;
import javax.microedition.io.Connection;

/*
 * This class is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED
public class LocalDevice {

    /* Keeps this singleton object. */
    private static LocalDevice localDevice;

    static {
        try {
            localDevice = getLocalDevice();
        } catch (BluetoothStateException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /* Keeps the reference to implementation object. */
    private static LocalDeviceImpl localDeviceImpl;

    /*
     * Keeps the discovery agen reference -
     * because the DiscoveryAgent.<init> is package private,
     * so it can't be created ffrom the implemetation.
     */
    private DiscoveryAgent discoveryAgent;

    /*
     * The default constructor is hidden so that no one can create a new
     * instance of the LocalDevice.  To get the LocalDevice
     * object for this device, use the <code>getLocalDevice()</code>
     * static method in this class.
     *
     * @see #getLocalDevice
     */
    private LocalDevice() {}

    // JAVADOC COMMENT ELIDED
    public static LocalDevice getLocalDevice() throws BluetoothStateException {

        /*
         * The method is not declared as synchronized to keep
         * its signature unchanged.
         */
        synchronized (LocalDevice.class) {
            if (localDevice == null) {
                try {
                    // create a shared impl object and 'this'
                    localDevice = new LocalDevice();

                    /*
                     * create a DiscoveryAgent from here.
                     * This should be done one time only
                     * regardless whether or not the system is
                     * initialized for the first time.
                     *
                     * We suppose the getLocalDevice() may be called
                     * for the next time if the first try failed.
                     */
                    if (localDevice.discoveryAgent == null) {
                        localDevice.discoveryAgent = new DiscoveryAgent();
                    }

                    /*
                     * Constructing LocaldeviceImpl causes initialization
                     * of device properties and attributes.
                     */
                    localDeviceImpl = LocalDeviceImpl.getInstance();
                } catch (BluetoothStateException bse) {
                    localDevice = null;
                    throw bse;
                } catch (Throwable e) {
                    localDevice = null;
                    throw new BluetoothStateException(e.toString());
                }
            }
        }
        return localDevice;
    }

    // JAVADOC COMMENT ELIDED
    public DiscoveryAgent getDiscoveryAgent() {

        /*
         * This is an only exception for the "API/IMPL wrapper"
         * scheme, i.e. the DiscoveryAgent object is stored
         * locally in this class.
         */
        return discoveryAgent;
    }

    // JAVADOC COMMENT ELIDED
    public String getFriendlyName() {
        return localDeviceImpl.getFriendlyName();
    }

    // JAVADOC COMMENT ELIDED
    public DeviceClass getDeviceClass() {
        return localDeviceImpl.getDeviceClass();
    }

    // JAVADOC COMMENT ELIDED
    public static String getProperty(String property) {
        return localDevice != null ? localDeviceImpl.getProperty(property) :
                null;
    }

    // JAVADOC COMMENT ELIDED
    public int getDiscoverable() {
        return localDeviceImpl.getDiscoverable();
    }

    // JAVADOC COMMENT ELIDED
    public String getBluetoothAddress() {
        return localDeviceImpl.getBluetoothAddress();
    }

    // JAVADOC COMMENT ELIDED
    public boolean setDiscoverable(int mode) throws BluetoothStateException {
        return localDeviceImpl.setDiscoverable(mode);
    }

    // JAVADOC COMMENT ELIDED
    public ServiceRecord getRecord(Connection notifier) {
        return localDeviceImpl.getRecord(notifier);
    }

    // JAVADOC COMMENT ELIDED
    public void updateRecord(ServiceRecord srvRecord)
            throws ServiceRegistrationException {
        localDeviceImpl.updateRecord(srvRecord);
    }

    // JAVADOC COMMENT ELIDED
    public static boolean isPowerOn() {
        return localDeviceImpl.isPowerOn();
    }

} // end of class 'LocalDevice' definition
