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

package com.sun.midp.suspend;

import com.sun.midp.main.MIDletProxyList;
import com.sun.midp.main.Configuration;
import com.sun.midp.main.MIDletProxy;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Enumeration;

/**
 * Timer for terminating MIDlets that have not completed
 * their pase routines within suspend timeout.
 */
class SuspendTimer extends Timer {
    /**
     * The timeout within which MIDlets have chance to complete.
     */
    private static final long TIMEOUT =
            Configuration.getIntProperty("suspendAppTimeout", 2000);

    /**
     * The only instance of suspend timer.
     */
    private static SuspendTimer timer = new SuspendTimer();

    /**
     * Current timer task.
     */
    private static TimerTask task;

    /**
     * Constructs an instance.
     */
    private SuspendTimer() {}

    /**
     * Schedules standard MIDlets termination task to sandard timeout.
     * @param midletList the MIDlet proxy list
     */
    static synchronized void start(final MIDletProxyList midletList) {
        if (null == task) {
            task = new TimerTask() {
                public void run() {
                    SuspendSystem ss = SuspendSystem.getInstance();

                    // don't kill the midlets if the resume request is pending
                    if (ss.isResumePending()) {
                        Enumeration midlets = midletList.getMIDlets();
                        while(midlets.hasMoreElements()) {
                          ss.removeSuspendDependency(
                                  (MIDletProxy)midlets.nextElement());
                        }
                    } else {
                        midletList.terminatePauseAll();
                    }
                }
            };

            timer.schedule(task, TIMEOUT);
        }
    }

    /**
     * Cancels the timer.
     */
    static synchronized void stop() {
        task.cancel();
        task = null;
    }
}
