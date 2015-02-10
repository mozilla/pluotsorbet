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
import java.io.IOException;

/*
 * This class is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED 
public class BluetoothConnectionException extends IOException {

    // JAVADOC COMMENT ELIDED 
    public static final int UNKNOWN_PSM = 0x0001;

    // JAVADOC COMMENT ELIDED 
    public static final int SECURITY_BLOCK = 0x0002;

    // JAVADOC COMMENT ELIDED 
    public static final int NO_RESOURCES = 0x0003;

    // JAVADOC COMMENT ELIDED 
    public static final int FAILED_NOINFO = 0x0004;

    // JAVADOC COMMENT ELIDED 
    public static final int TIMEOUT = 0x0005;

    // JAVADOC COMMENT ELIDED 
    public static final int UNACCEPTABLE_PARAMS = 0x0006;

    /* Contains the error code specified in constructor. */
    private int status;
    
    // JAVADOC COMMENT ELIDED 
    public BluetoothConnectionException(int error) {
        this(error, null);
    }

    // JAVADOC COMMENT ELIDED 
    public BluetoothConnectionException(int error, String msg) {
        super(msg);

        switch (error) {
        case UNKNOWN_PSM: /* falls through */
        case SECURITY_BLOCK: /* falls through */
        case NO_RESOURCES: /* falls through */
        case FAILED_NOINFO: /* falls through */
        case TIMEOUT: /* falls through */
        case UNACCEPTABLE_PARAMS:
            status = error;
            break;
        default:
            throw new IllegalArgumentException("Invalid error code: " + error);
        }
    }

    // JAVADOC COMMENT ELIDED 
    public int getStatus() {
        return status;
    }
} // end of class 'BluetoothConnectionException' definition
