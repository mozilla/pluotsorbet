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
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import java.util.Hashtable;
import java.util.Vector;

/*
 * This class represents the module which is used by
 * DiscoveryAgent#selectService method implementation.
 *
 */
final class SelectServiceHandler implements DiscoveryListener {

    /* Set to false in RR version - then the javac skip the code. */
    private static final boolean DEBUG = false;

    private Vector btDevs;
    private Hashtable btDevsHash;
    private Object btDevsLock = new Object();
    private boolean selectDevDisStarted;
    private boolean selectDevDisStopped;
    private DiscoveryAgentImpl agent;

    /*
     * Constructs <code>SelectServiceHandler</code> for
     * <code>DiscoveryAgentImpl</code> given.
     *
     * @param agent the discovery agent to create instance for.
     */
    SelectServiceHandler(DiscoveryAgentImpl agent) {
        this.agent = agent;
    }

    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {

        // if this bloototh device was found in preknown or
        // cached devices skips it now.
        if (btDevsHash.put(btDevice, btDevice) == null) {
            btDevs.addElement(btDevice);
        }
    }

    public void inquiryCompleted(int discType) {
        synchronized (btDevsLock) {
            selectDevDisStopped = true;
            btDevsLock.notify();
        }
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        throw new RuntimeException("unexpected call");
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        throw new RuntimeException("unexpected call");
    }

    String selectService(UUID uuid, int security, boolean master)
            throws BluetoothStateException {
        if (DEBUG) {
            System.out.println("selectService:");
            System.out.println("\tuuid=" + uuid);
        }
        Vector disDevsVector = null;
        Hashtable disDevsHash = new Hashtable();

        if (uuid == null) {
            throw new NullPointerException("uuid is null");
        }

        // check in CACHED and PREKNOWN devices
        String url = selectFromDevicesList(agent.retrieveDevices(
                DiscoveryAgent.PREKNOWN), uuid, security, master, disDevsHash);

        if (url != null) {
            return url;
        }
        url = selectFromDevicesList(agent.retrieveDevices(
                DiscoveryAgent.CACHED), uuid, security, master, disDevsHash);

        if (url != null) {
            return url;
        }

        // start own device discovery now
        synchronized (btDevsLock) {
            if (selectDevDisStarted) {
                throw new BluetoothStateException(
                        "The previous device discovery is running...");
            }
            selectDevDisStarted = true;
            btDevs = new Vector();
            btDevsHash = disDevsHash;
        }

        try {
            agent.startInquiry(DiscoveryAgent.GIAC, this);
        } catch (BluetoothStateException btse) {
            synchronized (btDevsLock) {
                selectDevDisStarted = false;
                btDevs = null;
                btDevsHash = null;
            }
            throw btse;
        }

        synchronized (btDevsLock) {
            if (!selectDevDisStopped) {
                try {
                    btDevsLock.wait();
                } catch (InterruptedException ie) {
                    // ignore (breake waiting)
                }
                disDevsVector = btDevs;
                btDevs = null;
                btDevsHash = null;
                selectDevDisStarted = false;
                selectDevDisStopped = false;
            }
        }

        for (int i = 0; i < disDevsVector.size(); i++) {
            RemoteDevice btDev = (RemoteDevice) disDevsVector.elementAt(i);
            url = selectService(btDev, uuid, security, master);

            if (url != null) {
                if (DEBUG) {
                    System.out.println("\turl=" + url);
                }
                return url;
            }
        }
        if (DEBUG) {
            System.out.println("\turl=null");
        }
        return null;
    }

    private String selectFromDevicesList(RemoteDevice[] devs, UUID uuid,
            int security, boolean master, Hashtable disDevsHash) {
        if (devs == null) {
            return null;
        }

        for (int i = 0; i < devs.length; i++) {
            if (disDevsHash.put(devs[i], devs[i]) != null) {
                continue;
            }
            String url = selectService(devs[i], uuid, security, master);

            if (url != null) {
                if (DEBUG) {
                    System.out.println("\turl=" + url);
                }
                return url;
            }
        }
        return null;
    }

    private String selectService(RemoteDevice btDev, UUID uuid, int security,
            boolean master) {
        UUID[] uuidSet = new UUID[] {uuid};
        ServiceSelector selector = new ServiceSelector(null, uuidSet, btDev);
        ServiceRecord serRec = selector.getServiceRecord();

        if (serRec == null) {
            return null;
        } else {
            return serRec.getConnectionURL(security, master);
        }
    }
} // end of class 'SelectServiceHandler' definition
