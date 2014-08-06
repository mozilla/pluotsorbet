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

import com.sun.cldc.isolate.Isolate;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.configurator.Constants;

/**
 * Utilities for the MIDletProxy.  Does nothing is SVM mode.
 */
public class MIDletProxyUtils {

    /*
     * Set the MIDletProxy to run with the Maximum Isolate Priority
     *
     * @param mp MIDletProxy
     */
//    public static void maxPriority(MIDletProxy mp) {
//        Isolate isolate = getIsolateFromId(mp.getIsolateId());
//        if (isolate != null) {
//            isolate.setPriority(Isolate.MAX_PRIORITY);
//        }
//    }

    /**
     * Set the MIDletProxy to run with the Minimum Isolate Priority
     *
     * @param mp MIDletProxy
     */
    public static void minPriority(MIDletProxy mp) {
        Isolate isolate = getIsolateFromId(mp.getIsolateId());
        if (isolate != null) {
            isolate.setPriority(Isolate.MIN_PRIORITY);
        }
    }

    /**
     * Set the MIDletProxy to run with the Normal Isolate Priority
     *
     * @param mp MIDletProxy
     */
    public static void normalPriority(MIDletProxy mp) {
        Isolate isolate = getIsolateFromId(mp.getIsolateId());
        if (isolate != null) {
          isolate.setPriority(Isolate.NORM_PRIORITY);
        }
    }

    /**
     * Get the Isolate from a MIDletProxy's IsolateId
     *
     * @param id MIDletProxy's Isolate Id
     * @return MIDletProxy's Isolate
     */
    static Isolate getIsolateFromId(int id) {
        if (id > 1) {
            Isolate[] isolate = Isolate.getIsolates();
            for (int i = 0; i < isolate.length; i++) {
                if (isolate[i].id() == id) {
                    return isolate[i];
                }
            }
        }
        return null;
    }

    /**
     * Terminates an isolate correspondent to the proxy given, resets
     * proxy termination timer and invokes proper proxy list updates.
     * Waits for termination completion.
     * @param mp MIDlet proxy for the isolate to be terminated
     * @param mpl the MIDlet proxy list
     */
    static void terminateMIDletIsolate(MIDletProxy mp, MIDletProxyList mpl) {
        Isolate isolate = getIsolateFromId(mp.getIsolateId());
         if (isolate != null) {
            mp.setTimer(null);
            isolate.exit(0);
            // IMPL_NOTE: waiting for termination completion may be useless.
            isolate.waitForExit();
            mpl.removeIsolateProxies(mp.getIsolateId());
        }
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
        if (Constants.EXTENDED_MIDLET_ATTRIBUTES_ENABLED) {
            String prop;
            prop = MIDletSuiteUtils.getSuiteProperty(
                mp, MIDletSuite.LAUNCH_BG_PROP);
            if ("yes".equalsIgnoreCase(prop)) {
                mp.setExtendedAttribute(MIDletProxy.MIDLET_LAUNCH_BG);
            }
            prop = MIDletSuiteUtils.getSuiteProperty(
                mp, MIDletSuite.BACKGROUND_PAUSE_PROP);
            if ("yes".equalsIgnoreCase(prop)) {
                mp.setExtendedAttribute(MIDletProxy.MIDLET_BACKGROUND_PAUSE);
            }
            prop = MIDletSuiteUtils.getSuiteProperty(
                mp, MIDletSuite.NO_EXIT_PROP);
            if ("yes".equalsIgnoreCase(prop)) {
                mp.setExtendedAttribute(MIDletProxy.MIDLET_NO_EXIT);
            }
        }
    }
}
