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

package com.sun.midp.main;

/**
 * Structure containing the run time information about the midlet.
 */
class RuntimeInfo {
    /**
     * The minimum amount of memory guaranteed to be available to the isolate
     * at any time. Used to pass a parameter to midlet_create_start(),
     * < 0 if not used.
     */
    int memoryReserved;

    /**
     * The total amount of memory that the isolate can reserve.
     * Used to pass a parameter to midlet_create_start(), < 0 if not used.
     */
    int memoryTotal;

    /**
     * The approximate amount of object heap memory currently
     * used by the isolate.
     */
    int usedMemory;

    /**
     * Priority of the isolate (< 0 if not set).
     */
    int priority;

    /**
     * Name of the VM profile that should be used for the new isolate.
     * Used (1) to pass a parameter to midlet_create_start();
     * (2) to get a profile's name of the given isolate in run time.
     */
    String profileName;

    /** Constructor */
    RuntimeInfo() {
    }

    /**
     * Returns the string form of this object.
     *
     * @return displayable string representation of this object
     */
    public String toString() {
        return "Runtime Information:" +
            "\n  memoryReserved: " + memoryReserved +
            "\n  memoryTotal: " + memoryTotal +
            "\n  usedMemory: " + usedMemory +
            "\n  priority: " + priority +
            "\n  profileName: " +  profileName;
    }
}
