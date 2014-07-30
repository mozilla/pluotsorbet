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
 * Utilities for the MIDletProxy.
 */
public class MIDletProxyUtils {

    /**
     * Set the MIDletProxy to run with the Maximum Isolate Priority
     *
     * @param mp MIDletProxy
     */
    public static void maxPriority(MIDletProxy mp) {
    }

    /**
     * Set the MIDletProxy to run with the Minimum Isolate Priority
     *
     * @param mp MIDletProxy
     */
    public static void minPriority(MIDletProxy mp) {
    }

    /**
     * Set the MIDletProxy to run with the Normal Isolate Priority
     *
     * @param mp MIDletProxy
     */
    public static void normalPriority(MIDletProxy mp) {
    }

    /**
     * Removes current MIDlet form the proxy list and reqests VM to stop.
     * This results in terminating current applicaton and further re-launching
     * VM with an AMS MIDlet (if the latter is scheduled in the main MIDP
     * running loop).
     *
     * @param mp the only MIDlet proxy presenting in the system
     * @param mpl the MIDlet proxy list
     */
    static void terminateMIDletIsolate(MIDletProxy mp, MIDletProxyList mpl) {
        requestVMStop();
    }

    /**
     * Loads extended MIDlet attributes accessed during MIDlet execution
     * (not the ones that used during MIDlet's start up only) then saves
     * them to MIDletProxy instance to reach better performance.
     *
     * @param mp MIDletProxy for running MIDlet to load and cache
     *           extended attributes for
     */
    public static void setupExtendedAttributes(MIDletProxy mp) {
    }

    /**
     * Requests VM to stop.
     */
    private static native void requestVMStop();
}
