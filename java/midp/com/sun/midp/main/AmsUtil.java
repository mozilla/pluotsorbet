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

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.MIDletStateHandler;
import com.sun.midp.midletsuite.MIDletSuiteStorage;

/** Implements utilities that are different for SVM and MVM modes. */
public class AmsUtil {
    /** Cached reference to the MIDletProxyList. */
    private static MIDletProxyList midletProxyList;

    /**
     * Initializes AmsUtil class. shall only be called from
     * MIDletSuiteLoader's main() in MVM AMS isolate
     * or in SVM main isolate.
     * No need in security checks since it is package private method.
     *
     * @param theMIDletProxyList MIDletController's container
     * @param theMidletControllerEventProducer utility to send events
     */
    static void initClass(MIDletProxyList theMIDletProxyList,
            MIDletControllerEventProducer theMidletControllerEventProducer) {

        midletProxyList = theMIDletProxyList;
    }

    /**
     * Queues the execution of the named Application suite to run.
     * The current application suite should terminate itself normally
     * to make resources available to the new application suite. Only
     * one package and set of MIDlets can be queued in this manner.
     * If multiple calls to execute are made, the package and MIDlets
     * specified during the <em>last</em> invokation will be executed
     * when the current application is terminated.
     *
     * @param midletSuiteStorage reference to a MIDletStorage object
     * @param externalAppId ID of MIDlet to invoke, given by an external
     *                      application manager (MVM only)
     * @param id ID of an installed suite
     * @param midlet class name of MIDlet to invoke
     * @param displayName name to display to the user
     * @param arg0 if not null, this parameter will be available to the
     *             MIDlet as application property arg-0
     * @param arg1 if not null, this parameter will be available to the
     *             MIDlet as application property arg-1
     * @param arg2 if not null, this parameter will be available to the
     *             MIDlet as application property arg-2
     * @param memoryReserved the minimum amount of memory guaranteed to be
     *             available to the isolate at any time; &lt; 0 if not used
     * @param memoryTotal the total amount of memory that the isolate can
                   reserve; &lt; 0 if not used
     * @param priority priority to set for the new isolate;
     *                 &lt;= 0 if not used
     * @param profileName name of the profile to set for the new isolate;
     *                    null if not used
     * @param isDebugMode true if the new midlet must be started in debug
     *                    mode, false otherwise
     *
     * @return true to signal that the MIDlet suite MUST first exit before the
     * MIDlet is run
     */
    static boolean executeWithArgs(MIDletSuiteStorage midletSuiteStorage,
            int externalAppId, int id, String midlet,
            String displayName, String arg0, String arg1, String arg2,
            int memoryReserved, int memoryTotal, int priority,
            String profileName, boolean isDebugMode) {

        if (id != MIDletSuite.UNUSED_SUITE_ID) {

            // The MIDlet running already shoudln't be started again.
            // Each started MIDlet has matching MIDletProxy instance
            // created on MIDLET_CREATED_NOTIFICATION event. In SVM mode
            // the event system is not used for MIDlet execution, so
            // MIDletProxy can not exist yet for a MIDlet just started.
            // Instead of MIDletProxyList browsing the MIDletStateHandler
            // is checked for the running MIDlet.

            if (MIDletStateHandler.getMidletStateHandler().isRunning(midlet)) {
                // No need to exit, MIDlet already loaded
                return false;
            }
        }

        MIDletSuiteUtils.nextMidletSuiteToRun = id;
        MIDletSuiteUtils.nextMidletToRun = midlet;
        MIDletSuiteUtils.arg0ForNextMidlet = arg0;
        MIDletSuiteUtils.arg1ForNextMidlet = arg1;
        MIDletSuiteUtils.arg2ForNextMidlet = arg2;
        MIDletSuiteUtils.memoryReserved = memoryReserved;
        MIDletSuiteUtils.memoryTotal = memoryTotal;
        MIDletSuiteUtils.priority    = priority;
        MIDletSuiteUtils.profileName = profileName;
        MIDletSuiteUtils.isDebugMode = isDebugMode;
        
        return true;
    }

    /**
     * Does nothing in SVM mode
     *
     * @param id Isolate Id
     */
    static void terminateIsolate(int id) {
    }
}


