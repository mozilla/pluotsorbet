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
import com.sun.jsr082.bluetooth.DiscoveryAgentImpl;

/*
 * This class is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED
public class DiscoveryAgent {

    // JAVADOC COMMENT ELIDED
    public static final int NOT_DISCOVERABLE = 0;

    // JAVADOC COMMENT ELIDED
    public static final int GIAC = 0x9E8B33;

    // JAVADOC COMMENT ELIDED
    public static final int LIAC = 0x9E8B00;

    // JAVADOC COMMENT ELIDED
    public static final int CACHED = 0x00;

    // JAVADOC COMMENT ELIDED
    public static final int PREKNOWN = 0x01;

    // JAVADOC COMMENT ELIDED
    private DiscoveryAgentImpl discoveryAgentImpl;

    // JAVADOC COMMENT ELIDED
    DiscoveryAgent() {
        discoveryAgentImpl = DiscoveryAgentImpl.getInstance();
    }

    // JAVADOC COMMENT ELIDED
    public RemoteDevice[] retrieveDevices(int option) {
        return discoveryAgentImpl.retrieveDevices(option);
    }

    // JAVADOC COMMENT ELIDED
    public boolean startInquiry(int accessCode, DiscoveryListener listener)
            throws BluetoothStateException {
        return discoveryAgentImpl.startInquiry(accessCode, listener);
    }

    // JAVADOC COMMENT ELIDED
    public boolean cancelInquiry(DiscoveryListener listener) {
        return discoveryAgentImpl.cancelInquiry(listener);
    }

    // JAVADOC COMMENT ELIDED
    public int searchServices(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev,
            DiscoveryListener discListener) throws BluetoothStateException {
        return discoveryAgentImpl.searchServices(attrSet, uuidSet, btDev,
                discListener);
    }

    // JAVADOC COMMENT ELIDED
    public boolean cancelServiceSearch(int transID) {
        return discoveryAgentImpl.cancelServiceSearch(transID);
    }

    // JAVADOC COMMENT ELIDED
    public String selectService(UUID uuid, int security, boolean master)
            throws BluetoothStateException {
        return discoveryAgentImpl.selectService(uuid, security, master);
    }
} // end of class 'DiscoveryAgent' definition
