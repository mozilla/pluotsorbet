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

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.midlet.*;

import com.sun.midp.installer.*;

import com.sun.midp.midletsuite.*;

import com.sun.midp.main.*;

import com.sun.midp.configurator.Constants;
import com.sun.midp.events.EventQueue;

/**
 * This is an implementation of the ApplicationManager interface
 * for the single VM mode of the VM.
 *
 * In this mode the VM is shut down each time a MIDlet exits.
 *
 * Application manager controls midlet life cycle:
 *    - installs, updates and removes midlets/midlet suites
 *    - launches and terminates midlets
 *    - displays info about a midlet/midlet suite
 *    - shuts down the AMS system
 */
public class Manager extends MIDlet implements ApplicationManager,
        ODTControllerEventConsumer {

    /** Constant for the discovery application class name. */
    private static final String DISCOVERY_APP =
        "com.sun.midp.installer.DiscoveryApp";

    /** Constant for the graphical installer class name. */
    private static final String INSTALLER =
        "com.sun.midp.installer.GraphicalInstaller";

    /** Constant for the CA manager class name. */
    private static final String CA_MANAGER =
        "com.sun.midp.appmanager.CaManager";

    /** Constant for the component manager class name. */
    private static final String COMP_MANAGER =
        "com.sun.midp.appmanager.ComponentManager";

    /** Constant for the ODT Agent class name. */
    private static final String ODT_AGENT =
        "com.sun.midp.odd.ODTAgentMIDlet";

    /** True until constructed for the first time. */
    private static boolean first = true;

    /** MIDlet Suite storage object. */
    private MIDletSuiteStorage midletSuiteStorage;

    /** UI to display error alerts. */
    private DisplayError displayError;

    /** Application Selector Screen. */
    private AppManagerPeer appManager;

    /**
     * Create and initialize a new Manager MIDlet.
     */
    public Manager() {
        midletSuiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();

        EventQueue eq = EventQueue.getEventQueue();
        new ODTControllerEventListener(eq, this);

        GraphicalInstaller.initSettings();

        first = (getAppProperty("logo-displayed") == null);

        Display display = Display.getDisplay(this);
        displayError = new DisplayError(display);

        // Get arguments to create appManager
        String suiteIdStr = getAppProperty("arg-0");
        int suiteId = MIDletSuite.UNUSED_SUITE_ID;
        try {
            suiteId = Integer.parseInt(suiteIdStr);
        } catch (NumberFormatException e) {
            suiteId = MIDletSuite.UNUSED_SUITE_ID;
        }

        if (suiteId != MIDletSuite.UNUSED_SUITE_ID) {
            MIDletSuiteInfo sui = new MIDletSuiteInfo(suiteId);
            if (suiteId == MIDletSuite.INTERNAL_SUITE_ID) {
                // For internal suites midlet class name should be specified
                sui.midletToRun = getAppProperty("arg-1");
            }
            // AppManagerUI will be set to be current at the end of its constructor
            appManager = new AppManagerPeer(this, display, displayError, first, sui);
        } else {
            // AppManagerUI will be set to be current at the end of its constructor
            appManager = new AppManagerPeer(this, display, displayError, first, null);
        }

        if (first) {
            first = false;
        }
    }


    /**
     * Start puts up a List of the MIDlets found in the descriptor file.
     */
    public void startApp() {
    }

    /**
     * Pause; there are no resources that need to be released.
     */
    public void pauseApp() {
    }

    /**
     * Destroy cleans up.
     *
     * @param unconditional is ignored; this object always
     * destroys itself when requested.
     */
    public void destroyApp(boolean unconditional) {
        GraphicalInstaller.saveSettings(null, MIDletSuite.UNUSED_SUITE_ID);

        appManager.cleanUp();

        if (MIDletSuiteUtils.getNextMIDletSuiteToRun() !=
                MIDletSuite.UNUSED_SUITE_ID) {
            /*
             * A MIDlet was pushed.
             * So make sure this MIDlet is run after the pushed one.
             *
             * Note this call only is
             * needed now because suites are not run concurrently and must
             * be queued to be run after this MIDlet is destroyed.
             */
            updateLastSuiteToRun();
        }
    }

    /**
     * Processes MIDP_ENABLE_ODD_EVENT
     */
    public void handleEnableODDEvent() {
        appManager.showODTAgent();
    }

    /**
     * Processes MIDP_ODD_START_MIDLET_EVENT
     *
     * @param suiteId ID of the midlet suite
     * @param className class name of the midlet to run
     * @param displayName display name of the midlet to run
     * @param isDebugMode true if the midlet must be started in debug mode,
     *                    false otherwise
     */
    public void handleODDStartMidletEvent(int suiteId, String className,
                                          String displayName,
                                          boolean isDebugMode) {
        /*
         * Not used in SVM: midlet is started directly instead of sending
         * a message to AMS.
         */
    }

    /**
     * Processes MIDP_ODD_EXIT_MIDLET_EVENT.
     *
     * @param suiteId ID of the midlet suite
     * @param className class name of the midlet to exit or <code>NULL</code>
     *      if all MIDlets from the suite should be exited
     */
    public void handleODDExitMidletEvent(int suiteId, String className) {
        /*
         * Not used in SVM: ODT agent doesn't send messages to AMS in SVM mode.
         */
    }
    
    /**
     * Processes MIDP_ODD_SUITE_INSTALLED_EVENT. This event indicates that
     * a new MIDlet suite has been installed by ODT agent.
     * 
     * @param suiteId ID of the newly installed MIDlet suite          
     */
    public void handleODDSuiteInstalledEvent(int suiteId) {
        /*
         * Not used in SVM: ODT agent doesn't send messages to AMS in SVM mode.
         */
    }

    /**
     * Processes MIDP_ODD_SUITE_REMOVED_EVENT. This event indicates that
     * an installed MIDlet suite has been removed by ODT agent.
     * 
     * @param suiteId ID of the removed MIDlet suite          
     */
    public void handleODDSuiteRemovedEvent(int suiteId) {
        /*
         * Not used in SVM: ODT agent doesn't send messages to AMS in SVM mode.
         */
    }

    // ===================================================================
    // ---- Implementation of the ApplicationManager interface ------------

    /**
     * Discover and install a suite.
     */
    public void installSuite() {
        try {
            MIDletStateHandler.getMidletStateHandler().startMIDlet(
                DISCOVERY_APP,
                Resource.getString(ResourceConstants.INSTALL_APPLICATION));

            yieldForNextMidlet();
        } catch (Exception ex) {
            displayError.showErrorAlert(Resource.getString
				    (ResourceConstants.INSTALL_APPLICATION),
					ex, null, null);
        }
    }

    /** Launch the CA manager. */
    public void launchCaManager() {
        try {
            MIDletStateHandler.getMidletStateHandler().startMIDlet(
                CA_MANAGER,
                Resource.getString(ResourceConstants.CA_MANAGER_APP));

            yieldForNextMidlet();
        } catch (Exception ex) {
            displayError.showErrorAlert(Resource.getString(
                ResourceConstants.CA_MANAGER_APP), ex, null, null);
        }
    }

    /**
     * Launch the component manager.
     */
    public void launchComponentManager() {
        try {
            MIDletSuiteUtils.execute(MIDletSuite.INTERNAL_SUITE_ID,
                COMP_MANAGER,
                Resource.getString(ResourceConstants.COMP_MANAGER_APP));
        } catch (Exception ex) {
            displayError.showErrorAlert(Resource.getString(
                ResourceConstants.COMP_MANAGER_APP), ex, null, null);
        }
    }

    /**
     * Launch ODT Agent.
     */
    public void launchODTAgent() {
        try {
            MIDletStateHandler.getMidletStateHandler().startMIDlet(
                ODT_AGENT,
                Resource.getString(ResourceConstants.ODT_AGENT_MIDLET));

            yieldForNextMidlet();
        } catch (Exception ex) {
            displayError.showErrorAlert(Resource.getString(
                ResourceConstants.ODT_AGENT_MIDLET), ex, null, null);
        }
    }

    /**
     * Launches a suite.
     *
     * @param suiteInfo information for suite to launch
     * @param midletToRun class name of the MIDlet to launch
     */
    public void launchSuite(RunningMIDletSuiteInfo suiteInfo,
                            String midletToRun) {

        if (Constants.MEASURE_STARTUP) {
            System.err.println("Application Startup Time: Begin at "
                        +System.currentTimeMillis());
        }

        try {
            // Create an instance of the MIDlet class
            // All other initialization happens in MIDlet constructor
            MIDletSuiteUtils.execute(suiteInfo.suiteId, midletToRun,
                                     suiteInfo.displayName);

            /*
             * Give the new MIDlet the screen by destroy our self,
             * because we are running in a limited VM and must
             * restart the VM let the select suite run.
             */
            yieldForNextMidlet();
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
        MIDletStateHandler midletStateHandler =
            MIDletStateHandler.getMidletStateHandler();
        MIDletSuite midletSuite = midletStateHandler.getMIDletSuite();

        /*
         * Setting arg 0 to "U" signals that arg 1 is a suite ID for updating.
         */
        midletSuite.setTempProperty(null, "arg-0", "U");
        midletSuite.setTempProperty(null, "arg-1",
            String.valueOf(suiteInfo.suiteId));
        try {
            midletStateHandler.startMIDlet(INSTALLER, null);
            yieldForNextMidlet();

        } catch (Exception ex) {
            displayError.showErrorAlert(suiteInfo.displayName, ex, null, null);
        }
    }

    /**
     * Shut down the system
     */
    public void shutDown() {
        MIDletProxyList.getMIDletProxyList().shutdown();
    }

    /**
     * Bring the midlet with the passed in midlet suite info to the
     * foreground.
     *
     * @param suiteInfo information for the midlet to be put to foreground
     * @param className the running MIDlet class name
     */
    public void moveToForeground(RunningMIDletSuiteInfo suiteInfo, String className) {}


    /**
     * Exit the midlet with the passed in midlet suite info.
     *
     * @param suiteInfo information for the midlet to be terminated
     * @param className the running MIDlet class name
     */
    public void exitMidlet(RunningMIDletSuiteInfo suiteInfo, String className) {}

    /**
     * Handle exit of MIDlet suite (last running MIDlet in sute exited).
     * @param suiteInfo Containing ID of exited suite
     * @param className the running MIDlet class name
     */
    public void notifySuiteExited(RunningMIDletSuiteInfo suiteInfo, String className) {}
    
    /**
     * Handle exit of MIDlet selector.
     * @param suiteInfo Containing ID of suite
     */
    public void notifyMIDletSelectorExited(RunningMIDletSuiteInfo suiteInfo) {}

    // ==============================================================
    // ----------------- PRIVATE methods ---------------------------

    /**
     * Yield the VM so the next MIDlet can run. To yield set this MIDlet as
     * last MIDlet run after the next MIDlet suite is done and then destroy
     * this MIDlet. Note: if the system is changed to allow multiple suites
     * to run concurrently, this method will not be needed.
     */
    private void yieldForNextMidlet() {
        // We want this MIDlet to run after the next MIDlet is run.
        updateLastSuiteToRun();
        destroyApp(false);
        notifyDestroyed();
    }

    /**
     * Set this MIDlet to run after the next MIDlet is run.
     */
    private void updateLastSuiteToRun() {
        MIDletSuiteInfo msi = appManager.getSelectedMIDletSuiteInfo();
        if (msi == null) {
            MIDletSuiteUtils.setLastSuiteToRun(MIDletStateHandler.
                    getMidletStateHandler().getMIDletSuite().getID(),
                    getClass().getName(), null, null);
        } else {
            String midletToRun = null;
            if (msi.isInternal()) {
                midletToRun = msi.midletToRun;
            }
            MIDletSuiteUtils.setLastSuiteToRun(MIDletStateHandler.
                    getMidletStateHandler().getMIDletSuite().getID(),
                    getClass().getName(), String.valueOf(msi.suiteId), midletToRun);
        }
    }
}
