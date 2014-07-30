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

package com.sun.midp.midlet;

/**
 * This class is here for the for JSR code that
 * has not been updated to use the <code>MIDletStateHandler</code>
 * for security checks.
 *
 * The functionality of the Scheduler was moved to the
 * {@link MIDletStateHandler}
 */

public class Scheduler {
    /** The manager of all MIDlets. */
    private static Scheduler scheduler;

    /** The event handler of all MIDlets in an Isolate. */
    private static MIDletStateHandler midletStateHandler;

    /**
     * Construct a new Scheduler object.
     */
    private Scheduler() {
    }

    /**
     * Get the Scheduler for performing security checks.
     *
     * @return the MIDlet management software scheduler
     */
    public static synchronized Scheduler getScheduler() {
        /*
         * If not scheduler has been created, create one now.
         */
        if (scheduler == null) {
            /* This is the default scheduler class */
            midletStateHandler = MIDletStateHandler.getMidletStateHandler();
            scheduler = new Scheduler();
        }

        return scheduler;
    }


    /**
     * Provides objects with a mechanism to retrieve
     * <code>MIDletSuite</code> being scheduled.
     *
     * @return MIDletSuite being scheduled
     */
    public MIDletSuite getMIDletSuite() {
        return midletStateHandler.getMIDletSuite();
    }
}
