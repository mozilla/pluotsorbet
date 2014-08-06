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

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * Wrapper for the Isolate API that may be present or not depending
 * on a build-time option.
 */
class IsolateUtil {
    /**
     * Sets the specified VM profile for the given isolate.
     *
     * @param isolate an isolate object for which the new profile must be set
     * @param profileName name of the profile to set
     */
    static void setProfile(Isolate isolate, String profileName) {
// #ifdef ENABLE_VM_PROFILES
        //isolate.setProfile(profileName);
// #else
        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
            Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                "VM profiles are disabled in the VM, " +
                "but Isolate.setProfile() was called!");
        }
// #endif
    }
}
