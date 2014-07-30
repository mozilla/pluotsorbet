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

package com.sun.midp.appmanager;

/**
 * Dummy implementation to use when On Device Debugging is disabled.
 */
interface ODTControllerEventConsumer {
    /**
     * Processes MIDP_ENABLE_ODD_EVENT.
     */
    public void handleEnableODDEvent();

    /**
     * Processes MIDP_ODD_START_MIDLET_EVENT.
     *
     * @param suiteId ID of the midlet suite
     * @param className class name of the midlet to run
     * @param displayName display name of the midlet to run
     * @param isDebugMode true if the midlet must be started in debug mode,
     *                    false otherwise
     */
    public void handleODDStartMidletEvent(int suiteId, String className,
                                          String displayName,
                                          boolean isDebugMode);

    /**
     * Processes MIDP_ODD_EXIT_MIDLET_EVENT.
     *
     * @param suiteId ID of the midlet suite
     * @param className class name of the midlet to exit or <code>NULL</code>
     *      if all MIDlets from the suite should be exited
     */
    public void handleODDExitMidletEvent(int suiteId, String className);

    /**
     * Processes MIDP_ODD_SUITE_INSTALLED_EVENT. This event indicates that
     * a new MIDlet suite has been installed by ODT agent.
     * 
     * @param suiteId ID of the newly installed midlet suite          
     */
    public void handleODDSuiteInstalledEvent(int suiteId);

    /**
     * Processes MIDP_ODD_SUITE_REMOVED_EVENT. This event indicates that
     * an installed MIDlet suite has been removed by ODT agent.
     * 
     * @param suiteId ID of the removed midlet suite          
     */
    public void handleODDSuiteRemovedEvent(int suiteId);
}
