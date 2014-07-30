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

import com.sun.midp.midletsuite.*;

/**
 * This is a common interface for Application managers.
 * Application manager controls midlet life cycle:
 *    - installs and updates midlet suites
 *    - launches, moves to foreground and exits midlets
 *    - shuts down the AMS system
 * It is used by the AppSelector.
 */
interface ApplicationManager {

    /** Discover and install a suite. */
    void installSuite();

    /** Launch the CA manager. */
    void launchCaManager();

    /** Launch the component manager. */
    void launchComponentManager();
    
    /** Launch ODT Agent. */
    void launchODTAgent();

    /**
     * Launches a suite.
     *
     * @param suiteInfo information for suite to launch
     * @param midletToRun class name of the MIDlet to launch
     */
    void launchSuite(RunningMIDletSuiteInfo suiteInfo, String midletToRun);

    /**
     * Update a suite.
     *
     * @param suiteInfo information for suite to update
     */
    void updateSuite(RunningMIDletSuiteInfo suiteInfo);

    /**
     * Shut downt the system
     */
    void shutDown();

    /**
     * Bring the midlet with the passed in midlet suite info to the 
     * foreground.
     * 
     * @param suiteInfo information for the midlet to be put to foreground
     * @param className the running MIDlet class name
     */
    void moveToForeground(RunningMIDletSuiteInfo suiteInfo, String className);
    
    /**
     * Exit the midlet with the passed in midlet suite info.
     * 
     * @param suiteInfo information for the midlet to be terminated
     * @param className the running MIDlet class name
     */
    void exitMidlet(RunningMIDletSuiteInfo suiteInfo, String className);

    /**
     * Handle exit of MIDlet suite (last running MIDlet in sute exited).
     * @param suiteInfo Containing ID of exited suite
     * @param className the running MIDlet class name
     */
    void notifySuiteExited(RunningMIDletSuiteInfo suiteInfo, String className);

    /**
     * Handle exit of MIDlet selector.
     * @param suiteInfo Containing ID of suite
     */
    void notifyMIDletSelectorExited(RunningMIDletSuiteInfo suiteInfo);
}
