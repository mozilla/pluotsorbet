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

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import com.sun.midp.main.MIDletSuiteUtils;
import com.sun.midp.midletsuite.*;

public class MIDletSuiteLauncher extends MIDlet implements ApplicationManager {
    
    /** MIDlet Suite storage object. */
    private MIDletSuiteStorage midletSuiteStorage;

    /** UI to display error alerts. */
    private DisplayError displayError;

    /** Display for the Launcher MIDlet. */
    Display display;


    /**
     * Constructor.
     */
    public MIDletSuiteLauncher() {
        Display display = Display.getDisplay(this);
        displayError = new DisplayError(display);

        midletSuiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();
        
        try {
            String suiteID = getAppProperty("arg-0");
                int id = Integer.parseInt(suiteID);
            MIDletSuiteImpl midletSuite =
                    midletSuiteStorage.getMIDletSuite(id, false);

            if (midletSuite == null) {
                /*
                 * check if the suite is the index of the suite
                 * as returned by -Xjam:list
                 */
                try {
                    int suiteIndex = id - 1;
                    int[] suites = midletSuiteStorage.getListOfSuites();
                    id = suites[suiteIndex];
                    midletSuite = midletSuiteStorage.getMIDletSuite(id, false);
                } catch (Exception e) {
                    displayError.showErrorAlert("MIDlet Suite not found",
                                                        e, null,
                                                        "MIDlet Suite " +
                                                        suiteID + " not found");
                    return;
                }
            }

            RunningMIDletSuiteInfo suiteInfo =
                new RunningMIDletSuiteInfo(id, midletSuite, midletSuiteStorage);

            if (suiteInfo.hasSingleMidlet()) {
                launchSuite(suiteInfo, suiteInfo.midletToRun);
            } else {
                new MIDletSelector(suiteInfo, display, null, this);
            }
        } catch (Throwable t) {
             t.printStackTrace();
        }
    }

    protected void startApp() throws MIDletStateChangeException {
    }
    
    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
    }

    /** Discover and install a suite. */
    public void installSuite() {
    }

    /** Launch the CA manager. */
    public void launchCaManager() {
    }

    /**
     * Launch the component manager.
     */
    public void launchComponentManager() {
    }

    /** Launch ODT Agent. */
    public void launchODTAgent() {
    }

    /**
     * Launches a suite.
     *
     * @param suiteInfo information for suite to launch
     * @param midletToRun class name of the MIDlet to launch
     */
    public void launchSuite(RunningMIDletSuiteInfo suiteInfo,
                            String midletToRun) {
        try {
            // Create an instance of the MIDlet class
            // All other initialization happens in MIDlet constructor
            MIDletSuiteUtils.execute(suiteInfo.suiteId, midletToRun, null);
        } catch (Exception ex) {
            displayError.showErrorAlert(suiteInfo.displayName, ex, null, null);
        }
    }

    /**
     * Update a suite.
     *
     * @param suiteInfo information for suite to update
     */
    public void updateSuite(RunningMIDletSuiteInfo suiteInfo) {
    }

    /**
     * Shut downt the system
     */
    public void shutDown() {
        destroyApp(false);
        notifyDestroyed();
    }

    /**
     * Bring the midlet with the passed in midlet suite info to the 
     * foreground.
     * 
     * @param suiteInfo information for the midlet to be put to foreground
     * @param className the running MIDlet class name
     */
    public void moveToForeground(RunningMIDletSuiteInfo suiteInfo, String className) {
    }
    
    /**
     * Exit the midlet with the passed in midlet suite info.
     * 
     * @param suiteInfo information for the midlet to be terminated
     * @param className the running MIDlet class name
     */
    public void exitMidlet(RunningMIDletSuiteInfo suiteInfo, String className) {
    }

    /**
     * Handle exit of MIDlet suite (the only MIDlet in sute exited or MIDlet
     * selector exited).
     * @param suiteInfo Containing ID of exited suite
     * @param className the running MIDlet class name
     */
    public void notifySuiteExited(RunningMIDletSuiteInfo suiteInfo, String className) {
    }
    
    /**
     * Handle exit of MIDlet selector.
     * @param suiteInfo Containing ID of suite
     */
    public void notifyMIDletSelectorExited(RunningMIDletSuiteInfo suiteInfo) {
    }
    
}
