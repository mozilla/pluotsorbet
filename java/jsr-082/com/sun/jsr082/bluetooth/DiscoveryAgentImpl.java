/*
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

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/*
 * The <code>DiscoveryAgentImpl</code> class is a DiscoveryAgent
 * API class implementation which does not extend this API class.
 */
public final class DiscoveryAgentImpl {
    /* Calls inquiry completion callback in a separate thread. */
    class Completed implements Runnable {
        /* type of completion. */
        private int discType;
        /* listener to be called. */
        private DiscoveryListener listener;
        /* Constructs an instance and starts the the thread. */
        Completed(DiscoveryListener listener, int discType) {
            this.listener = listener;
            this.discType = discType;
            new Thread(this).start();
        }
        /* Implements Runnable. */
        public void run() {
        	if (listener != null) {
        		listener.inquiryCompleted(discType);
        	}
        }
    }

    /* Set to false in RR version - then the javac skip the code. */
    private static final boolean DEBUG = false;

    /*
     * maximum number of allowed UUIDS in search uuids sequence
     */
    private static final int MAX_ALLOWED_UUIDS = 12;

    /*
     * Keeps an instance to the object of this class to be
     * accessible from the implementation.
     */
    private static DiscoveryAgentImpl instance;

    /* Keeps the <code>RemoteDeviceImpl</code> references of known devices. */
    private Hashtable knownDevices = new Hashtable();

    /* Keeps the <code>RemoteDeviceImpl</code> references of cached devices. */
    private Hashtable cachedDevices = new Hashtable();

    /*
     * Keeps the listener of the device discovery inquire.
     * Also, it is used as flag that a device is in inquire mode.
     */
    private DiscoveryListener d_listener;
    
    /* Keeps the lock object for device discovery synchronization. */
    private Object d_lock = new Object();

    /* Keeps the reference to module responsible for selecting services. */
    private SelectServiceHandler selectServiceHandler =
            new SelectServiceHandler(this);

    /* Constructs the single instance. */
    private DiscoveryAgentImpl() {}

    public RemoteDevice[] retrieveDevices(int option) {
        switch (option) {
            case DiscoveryAgent.CACHED:
                // IMPL_NOTE: use native cache keeping addresses of found devices
                // to share the cache between multiple isolates
                return getCachedDevices();
            case DiscoveryAgent.PREKNOWN:
                Vector pk = BCC.getInstance().getPreknownDevices();
                if (pk == null || pk.size() == 0) {
                    return null;
                }
                RemoteDevice[] res = new RemoteDevice[pk.size()];
                for (int i = 0; i < pk.size(); i++) {
                    String addr = (String)pk.elementAt(i);
                    res[i] = getRemoteDevice(addr);
                }
                return res;
            default:
                throw new IllegalArgumentException("Invalid option value: "
                        + option);
        }
    }


    /*
     * Adds address of remote device found during inquiry request to internal
     * inquiry cache.
     *
     * The method does nothing if the RemoteDevice is already in the cache.
     */
    public void addCachedDevice(String addr) {
        RemoteDevice rd = getRemoteDevice(addr);
        synchronized (cachedDevices) {
            cachedDevices.put(addr, rd);
        }
    }

    // JAVADOC COMMENT ELIDED
    private RemoteDevice[] getCachedDevices() {
        synchronized (cachedDevices) {
            int len = cachedDevices.size();
            if (len == 0) {
                return null;
            }
            RemoteDevice[] res = new RemoteDevice[len];
            Enumeration e = cachedDevices.elements();
            for (int i = 0; e.hasMoreElements(); i++) {
                res[i] = (RemoteDevice)e.nextElement();
            }
            return res;
        }
    }

    public boolean startInquiry(int accessCode, DiscoveryListener listener)
            throws BluetoothStateException {

        if (accessCode != DiscoveryAgent.GIAC &&
                accessCode != DiscoveryAgent.LIAC &&
                (accessCode < 0x9E8B00 || accessCode > 0x9E8B3F)) {
            throw new IllegalArgumentException("Access code is out of range: "
                    + accessCode);
        }

        if (listener == null) {
            throw new NullPointerException("null listener");
        }

        /* IMPL_NOTE see
        // kvem/classes/com/sun/kvem/jsr082/impl/bluetooth/
        //         BTDeviceDiscoverer.java
        // heck what access codes should be supported.
        // Return false if access code is not supported. 
         */

        synchronized (d_lock) {
            if (d_listener != null) {
                throw new BluetoothStateException(
                        "The previous device discovery is running...");
            }
            d_listener = listener;

            /* process the inquiry in the device specific way */
            return startInquiry(accessCode);
        }
    }

    private boolean startInquiry(int accessCode) throws BluetoothStateException {
        return BluetoothStack.getEnabledInstance().startInquiry(
            accessCode, d_listener);
    }

    public boolean cancelInquiry(DiscoveryListener listener) {
        if (listener == null) {
            throw new NullPointerException("null listener");
        }
        synchronized (d_lock) {

            /* no inquiry was started */
            if (d_listener == null) {
                return false;
            }

            /* not valid listener */
            if (d_listener != listener) {
                return false;
            }

            /* process the inquiry in the device specific way */
            cancelInquiry();
        }

        inquiryCompleted(DiscoveryListener.INQUIRY_TERMINATED);
        return true;
    }

    /*
     * Cancels inquiry in device specific way.
     */
    private void cancelInquiry() {
        BluetoothStack.getInstance().cancelInquiry(d_listener);
    }

    /*
     * Porting interface: this method is used by the device specific
     * implementation to notify this class, that the current inquire
     * has been completed.
     *
     * @param discType type of completion:
     * <code>DiscoveryListener.INQUIRY_COMPLETED</code>, or
     * <code>DiscoveryListener.INQUIRY_TERMINATED</code>, or
     * <code>DiscoveryListener.INQUIRY_ERROR</code>
     */
    public void inquiryCompleted(int discType) {
        DiscoveryListener listener;
        synchronized (d_lock) {
            listener = d_listener;
            d_listener = null;
        }

        new Completed(listener, discType);
    }

    /*
     * Porting interface: this method is used by the device specific
     * implementation to create the RemoteDevice object by address.
     *
     * Also, this method puts the new remote devices into cache of
     * known devices.
     *
     * @param addr address of remote device to be created
     *
     * @return new <code>RemoteDeviceImpl</code>instance if device with address
     * given is unknown, the known one otherwise.
     */
    public RemoteDeviceImpl getRemoteDevice(String addr) {
        synchronized (knownDevices) {
            addr = addr.toUpperCase();
            RemoteDeviceImpl rd = (RemoteDeviceImpl) knownDevices.get(addr);

            if (rd == null) {
                rd = new RemoteDeviceImpl(addr);
                knownDevices.put(addr, rd);
            }
            return rd;
        }
    }

    public int searchServices(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev,
            DiscoveryListener discListener) throws BluetoothStateException {

        if (DEBUG) {
            System.out.println("searchServices: ");
            System.out.println("\tattrSet=" + attrSet);

            if (attrSet != null) {
                for (int i = 0; i < attrSet.length; i++) {
                    System.out.println("\tattrSet[" + i + "]=0x" + attrSet[i]);
                }
            }
            System.out.println("\tuuidSet=" + uuidSet);

            if (uuidSet != null) {
                for (int i = 0; i < uuidSet.length; i++) {
                    System.out.println("\tuuidSet[" + i + "]=" + uuidSet[i]);
                }
            }
            System.out.println("\tadderess=" + btDev.getBluetoothAddress());
        }
        if (uuidSet == null) {
            throw new NullPointerException("UUID set is null");
        }

        if (uuidSet.length == 0 || uuidSet.length > MAX_ALLOWED_UUIDS ) {
            throw new IllegalArgumentException("Invalid UUID set length");
        }

        if (btDev == null) {
            throw new NullPointerException("null instance of RemoteDevice");
        }
        
        /* the 'transID' is assigned by service discoverer */
        int transID = ServiceDiscovererFactory.getServiceDiscoverer().
                searchService(
                        ServiceSearcherBase.extendByStandardAttrs(attrSet), 
                        ServiceSearcherBase.removeDuplicatedUuids(uuidSet), 
                        btDev, discListener);

        if (DEBUG) {
            System.out.println("\ttransID=" + transID);
        }
        return transID;
    }

    public boolean cancelServiceSearch(int transID) {
        if (DEBUG) {
            System.out.println("cancelServiceSearch: transID=" + transID);
        }
        return ServiceDiscovererFactory.getServiceDiscoverer().cancel(transID);
    }

    public String selectService(UUID uuid, int security, boolean master)
            throws BluetoothStateException {

        // use the separated class to light this one
        return selectServiceHandler.selectService(uuid, security, master);
//        return ServiceDiscovererFactory.getServiceDiscoverer().
//                                 selectService(uuid, security, master, this);
    }

    /*
     * Returns the instance of this singleton constructing it if needed.
     * @return the only instance of <code>DiscoveryAgentImpl</code>.
     */
    public static synchronized DiscoveryAgentImpl getInstance() {
        if (instance == null) {
            instance = new DiscoveryAgentImpl();
        }

        return instance;
    }
}
