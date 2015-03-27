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

/*
 * This interface is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED 
public interface DiscoveryListener {

    // JAVADOC COMMENT ELIDED 
    public static final int INQUIRY_COMPLETED = 0x00;

    // JAVADOC COMMENT ELIDED 
    public static final int INQUIRY_TERMINATED = 0x05;

    // JAVADOC COMMENT ELIDED 
    public static final int INQUIRY_ERROR = 0x07;

    // JAVADOC COMMENT ELIDED 
    public static final int SERVICE_SEARCH_COMPLETED = 0x01;

    // JAVADOC COMMENT ELIDED 
    public static final int SERVICE_SEARCH_TERMINATED = 0x02;

    // JAVADOC COMMENT ELIDED 
    public static final int SERVICE_SEARCH_ERROR = 0x03;

    // JAVADOC COMMENT ELIDED 
    public static final int SERVICE_SEARCH_NO_RECORDS = 0x04;

    // JAVADOC COMMENT ELIDED 
    public static final int SERVICE_SEARCH_DEVICE_NOT_REACHABLE = 0x06;

    // JAVADOC COMMENT ELIDED 
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod);

    // JAVADOC COMMENT ELIDED 
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord);

    // JAVADOC COMMENT ELIDED 
    public void serviceSearchCompleted(int transID, int respCode);

    // JAVADOC COMMENT ELIDED 
    public void inquiryCompleted(int discType);
} // end of class 'DiscoveryListener' definition
