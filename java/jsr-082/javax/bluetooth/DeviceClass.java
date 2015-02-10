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
 * This class is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED 
public class DeviceClass {

    // JAVADOC COMMENT ELIDED
    private int record;

    // JAVADOC COMMENT ELIDED
    private static final int MASK_MINOR = 0xFC;
    
    // JAVADOC COMMENT ELIDED
    private static final int MASK_MAJOR = 0x1F00;
    
    // JAVADOC COMMENT ELIDED
    private static final int MASK_SERVICE = 0xFFE000;
    
    // JAVADOC COMMENT ELIDED
    private static final int MASK_OVERFLOW = 0xFF000000;

    // JAVADOC COMMENT ELIDED 
    public DeviceClass(int record) {
        if ((record & MASK_OVERFLOW) != 0) {
            throw new IllegalArgumentException(
                    "The 'record' bits out of (0-23) range.");
        }
        this.record = record;
    }

    // JAVADOC COMMENT ELIDED 
    public int getServiceClasses() {
        return record & MASK_SERVICE;
    }

    // JAVADOC COMMENT ELIDED 
    public int getMajorDeviceClass() {
        return record & MASK_MAJOR;
    }

    // JAVADOC COMMENT ELIDED 
    public int getMinorDeviceClass() {
        return record & MASK_MINOR;
    }
} // end of class 'DeviceClass' definition
