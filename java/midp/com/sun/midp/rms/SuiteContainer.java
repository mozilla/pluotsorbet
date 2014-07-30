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

package com.sun.midp.rms;

/**
 * Implemented byt he class running the suite so that it can be decoupled
 * from the RMS library
 */
public interface SuiteContainer {
    /**
     * Get the suite ID of applicaiton on the current call stack.
     */
    int getCallersSuiteId();

    /**
     * Get the suite of identified by vendor and suite name.
     */
    int getSuiteId(String vendorName, String suiteName);

    /**
     * Get secure filename base to build a an RMS file name that will be
     * deleted with the suite.
     */
    String getSecureFilenameBase(int suiteId);
    
    /**
     * Get the storage area ID for a suite.
     * This is only CLDC RescordStore implementations, any other implementations can just
     * return 0.
     */
    int getStorageAreaId(int suiteId);
}
