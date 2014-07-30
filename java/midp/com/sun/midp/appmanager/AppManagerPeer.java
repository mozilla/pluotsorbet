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

import javax.microedition.lcdui.*;

import com.sun.midp.installer.*;
import com.sun.midp.main.*;
import com.sun.midp.midletsuite.*;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.io.j2me.push.PushRegistryInternal;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.configurator.Constants;

import com.sun.midp.payment.PAPICleanUp;

import java.io.*;
import javax.microedition.rms.*;
import java.util.*;

/**
 * The Graphical MIDlet selector Screen.
 * <p>
 * It displays a list (or grid to be exact) of currently installed
 * MIDlets/MIDlet suites (including the Installer MIDlet). Each MIDlet or
 * MIDlet suite is represented by an icon with a name under it.
 * An icon from a jad file for the MIDlet/MIDlet suite representation
 * is used if possible, otherwise a default icon is used.
 *
 * There is a a set of commands per MIDlet/MIDlet suite. Note that
 * the set of commands can change depending on the corresponding MIDlet state.
 * For MIDlets/MIDlet suites that are not running the following commands are
 * available:
 * <ul>
 * <li><b>Launch</b>: Launch the MIDlet or the MIDlet Selector
 *      if it is a suite.
 * <li><b>Remove</b>: Remove the MIDlet/MIDlet suite teh user selected
 *      (with confirmation). </li>
 * <li><b>Update</b>: Update the MIDlet/MIDlet suite the user selected.</li>
 * <li><b>Info</b>: Show the user general information
 *    of the selected MIDlet/MIdlet suite. </li>
 * <li><b>Settings</b>: Let the user change the manager's settings.
 * </ul>
 *
 * For MIDlets/MIDlet suites that are running the following commands are
 * available:
 * <ul>
 * <li><b>Bring to foreground</b>: Bring the running MIDlet to foreground
 * <li><b>End</b>: Terminate the running MIDlet
 * <li><b>Remove</b>: Remove the MIDlet/MIDlet suite teh user selected
 *      (with confirmation). </li>
 * <li><b>Update</b>: Update the MIDlet/MIDlet suite the user selected.</li>
 * <li><b>Info</b>: Show the user general information
 *    of the selected MIDlet/MIdlet suite. </li>
 * <li><b>Settings</b>: Let the user change the manager's settings.
 * </ul>
 *
 * Exactly one MIDlet from a MIDlet suite could be run at the same time.
 * Each MIDlet/MIDlet suite representation corresponds to an instance of
 * MidletCustomItem which in turn maintains a reference to a MIDletSuiteInfo
 * object (that contains info about this MIDlet/MIDlet suite).
 * When a MIDlet is launched or a MIDlet form a MIDlet suite is launched
 * the proxy instance in the corresponding MidletCustomItem is set to
 * a running MIDletProxy value. It is set back to null when MIDlet exits.
 *
 * Running midlets can be distinguished from non-running MIdlets/MIDlet suites
 * by the color of their name.
 */
class AppManagerPeer implements CommandListener {

    private AppManagerUI appManagerUI;

    /** Constant for the discovery application class name. */
    public static final String DISCOVERY_APP =
        "com.sun.midp.installer.DiscoveryApp";

    /** Constant for the certificate manager class name */
    public static final String CA_MANAGER =
        "com.sun.midp.appmanager.CaManager";

    /** Constant for the component manager class name */
    public static final String COMP_MANAGER =
        "com.sun.midp.appmanager.ComponentManager";

    /** Constant for the graphical installer class name. */
    public static final String INSTALLER =
        "com.sun.midp.installer.GraphicalInstaller";

    /** Constant for the ODT Agent class name. */
    public static final String ODT_AGENT =
        "com.sun.midp.odd.ODTAgentMIDlet";

    /** True if On Device Debug is enabled. */
    private static boolean oddEnabled = false;

    /** MIDlet Suite storage object. */
    private MIDletSuiteStorage midletSuiteStorage;

    /** Display for the Manager MIDlet. */
    Display display; // = null

    /** MIDlet to be removed after confirmation screen was accepted */
    private RunningMIDletSuiteInfo removeMsi;

   /** vector of existing RunningMIDletSuiteInfo */
    private Vector msiVector;

    /** UI used to display error messages. */
    private DisplayError displayError;   
    
    /** Display for the Manager MIDlet. */
    ApplicationManager manager;


    /**
     * There are several Application Manager
     * midlets from the same "internal" midlet suite
     * that should not be running in the background.
     * appManagerMidlet helps to destroy them
     * (see MidletCustomItem.showNotify).
     */
    private MIDletProxy appManagerMidlet;

    /** True, if the CA manager is included. */
    private boolean caManagerIncluded;

    /** True, if the CA manager is included. */
    private boolean compManagerIncluded;
    
    /** If there are folders */
    private boolean foldersOn;

    /** Command object for "Yes, enable on device debug" command. */
    private Command enableOddYesCmd = new Command(Resource.getString
                                            (ResourceConstants.YES),
                                            Command.OK, 1);

    /** Command object for "No, don't enable on device debug" command. */
    private Command enableOddNoCmd = new Command(Resource.getString
                                           (ResourceConstants.NO),
                                           Command.BACK, 1);

    /**
     * Creates and populates the Application Selector Screen.
     * @param manager - The application manager that invoked it
     * @param displayError - The UI used to display error messages
     * @param display - The display instance associated with the manager
     * @param first - true if this is the first time AppSelector is being
     *                shown
     * @param ms - MidletSuiteInfo that should be selected. For the internal
     *             suites midletToRun should be set, for the other suites
     *             suiteId is enough to find the corresponding item.
     */
    AppManagerPeer(ApplicationManager manager, Display display,
               DisplayError displayError, boolean first, MIDletSuiteInfo ms) {

        msiVector = new Vector();
        this.displayError = displayError;
        this.manager = manager;
        
        this.display = display;
        Vector folders = FolderManager.getFolders();
        foldersOn = (folders != null) ? folders.size() > 0 : false;
        try {
            caManagerIncluded = Class.forName(CA_MANAGER) != null;
        } catch (ClassNotFoundException e) {
            // keep caManagerIncluded false
        }
        try {
            compManagerIncluded = Class.forName(COMP_MANAGER) != null;
        } catch (ClassNotFoundException e) {
            // keep compManagerIncluded false
        }

        midletSuiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();

        RunningMIDletSuiteInfo currentItem = null;

        if (first) {
            appManagerUI = new AppManagerUIImpl(manager, this, display,
                                                displayError, foldersOn);

            // ensure that the suite database is not corrupted 
            int status = midletSuiteStorage.checkSuitesIntegrity(false, true);
            if (status == 1) {
                /*
                 * The suite database was corrupted and has been repaired,
                 * some suites may be lost. Inform the user about it.
                 */
                final String alertTitle = Resource.getString(
                        ResourceConstants.AMS_MGR_SUITE_DB_ALERT_TITLE);
                final String errMsg = Resource.getString(
                        ResourceConstants.AMS_MGR_SUITE_DB_REPAIRED);

                Alert a = new Alert(alertTitle, errMsg,
                                    null, AlertType.WARNING);
                a.setTimeout(Alert.FOREVER);
                display.setCurrent(a);
            } else if (status < 0) {
                /*
                 * An unrecoverable error has happened,
                 * display the error message.
                 */
                final String alertTitle = Resource.getString(
                        ResourceConstants.EXIT);
                final String errMsg = Resource.getString(
                        ResourceConstants.AMS_MGR_SUITE_DB_FATAL_ERROR);

                Alert a = new Alert(alertTitle, errMsg,
                                    null, AlertType.ERROR);
                a.setTimeout(Alert.FOREVER);
                display.setCurrent(a);

                throw new RuntimeException(errMsg);
            }

            // cleanup the storage from the previous execution
            midletSuiteStorage.removeTemporarySuites();
       } else {
            // if a MIDlet was just installed
            // getLastInstalledMidletItem() will return RunningMIDletSuiteInfo
            // corresponding to this suite, then we have to prompt
            // the user if he wantto launch a midlet from the suite.
            RunningMIDletSuiteInfo msi = getLastInstalledMidletItem();
            if (msi != null) {
                if (foldersOn) {
                    // move it to default folder
                    try {
                        midletSuiteStorage.moveSuiteToFolder(msi.suiteId,
                                FolderManager.getDefaultFolderId());
                        msi.folderId = FolderManager.getDefaultFolderId();
                    } catch (Throwable t) {
                        displayError.showErrorAlert(msi.displayName, t,
                                                    null, null);
                    }
                }
                appManagerUI = new AppManagerUIImpl(manager, this, display,
                        displayError, foldersOn, true);
                currentItem = msi;
            } else {
                appManagerUI = new AppManagerUIImpl(manager, this, display,
                        displayError, foldersOn, false);
            }
        }

        // appManagerUI will be populated with items during
        // updateContent() method execution.
        updateContent();
        if (null != currentItem) {
            appManagerUI.setCurrentItem(currentItem);
        } else {
            if (ms != null) {
                // Find item to select
                if (ms.isInternal()) {
                    for (int i = 0; i < msiVector.size(); i++) {
                        RunningMIDletSuiteInfo mi =
                                (RunningMIDletSuiteInfo)msiVector.elementAt(i);
                        if ((mi.isInternal()) &&
                                mi.midletToRun.equals(ms.midletToRun)) {
                            currentItem = mi;
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < msiVector.size(); i++) {
                        RunningMIDletSuiteInfo mi =
                                (RunningMIDletSuiteInfo)msiVector.elementAt(i);
                        if (mi.suiteId == ms.suiteId) {
                            currentItem = mi;
                            break;
                        }
                    }
                }
                appManagerUI.setCurrentItem(currentItem);
            } // ms != null
        }
    }


    /**
     * Returns True if On Device Debug is enabled.
     * @return
     */
    public boolean oddEnabled() {
        return oddEnabled;
    }

    /**
     * Returns true, if the CA manager is included.
     * @return
     */
    public boolean caManagerIncluded() {
        return caManagerIncluded;
    }

    /**
     * Returns true, if the CA manager is included.
     * @return
     */
    public boolean compManagerIncluded() {
        return compManagerIncluded;
    }

    /**
     * Shows requested application settings.
     * @param suiteId suite ID
     * @param nextScreen displayable that is to be shown
     *  after settings dialog is dismissed
     */
    public void showAppSettings(int suiteId,
                       Displayable nextScreen)
            throws MIDletSuiteLockedException, MIDletSuiteCorruptedException {
        AppSettings appSettings = new AppSettingsImpl(suiteId, display,
                                                  displayError, nextScreen);
    }

    /**
     * Shows ODT Agent midlet in the midlet list.
     */
    public void showODTAgent() {
        try {
            // check if the ODTAgent midlet is included into the build
            Class.forName(ODT_AGENT);
        } catch (ClassNotFoundException e) {
            // return if the agent is not included
            return;
        }

        // return if ODD is already enabled
        if (oddEnabled) {
            return;
        }

        // warn the user that he enables a potentially dangerous feature
        String title = Resource.getString(
            ResourceConstants.AMS_MGR_ENABLE_ON_DEVICE_DEBUG_TITLE, null);
        String msg = Resource.getString(
            ResourceConstants.AMS_MGR_ENABLE_ON_DEVICE_DEBUG, null);

        Alert alert = new Alert(title, msg, null, AlertType.WARNING);
        alert.addCommand(enableOddNoCmd);
        alert.addCommand(enableOddYesCmd);
        alert.setCommandListener(this);
        alert.setTimeout(Alert.FOREVER);

        display.setCurrent(alert);
    }


    /**
     * Respond to a command issued on any Screen.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == enableOddYesCmd) {
            // user has confirmed that he wants to enable on device debug
            oddEnabled = true;
            updateContent();
        } else if (c == enableOddNoCmd) {
            // user doesn't want to enable on device debug, do nothing
        }
        display.setCurrent(appManagerUI.getMainDisplayable());
    }

    /**
     * Called when a new midlet was launched.
     *
     * @param midlet proxy of a newly added MIDlet
     */
    void notifyMidletStarted(MIDletProxy midlet) {
        String midletClassName = midlet.getClassName();

        if (midletClassName.equals(manager.getClass().getName())) {
            return;
        }

        if (midlet.getSuiteId() == MIDletSuite.INTERNAL_SUITE_ID &&
                !midletClassName.equals(DISCOVERY_APP) &&
                !midletClassName.equals(INSTALLER) &&
                !midletClassName.equals(CA_MANAGER) &&
                !midletClassName.equals(COMP_MANAGER) &&
                !midletClassName.equals(ODT_AGENT)) {
            appManagerMidlet = midlet;
            appManagerUI.notifyInternalMidletStarted(midlet);
        } else {
            RunningMIDletSuiteInfo si;
            for (int i = 0; i < msiVector.size(); i++) {
                si = (RunningMIDletSuiteInfo)msiVector.elementAt(i);

                if (si.sameSuite(midlet)) {
                    si.addProxy(midlet);
                    appManagerUI.notifyMidletStarted(si, midletClassName);
                    return;
                }
            }
        }
    }

    /**
     * Called when state of a running midlet was changed.
     *
     * @param midlet proxy of a newly added MIDlet
     */
    void notifyMidletStateChanged(MIDletProxy midlet) {
        RunningMIDletSuiteInfo msi;

        for (int i = 0; i < msiVector.size(); i++) {
            msi = (RunningMIDletSuiteInfo)msiVector.elementAt(i);
            if (msi.hasProxy(midlet)) {
                appManagerUI.notifyMidletStateChanged(msi, midlet);
            }
        }
    }

    /**
     * Called when a running midlet exited.
     *
     * @param midlet proxy of a newly added MIDlet
     */
    void notifyMidletExited(MIDletProxy midlet) {
        String midletClassName = midlet.getClassName();

        if (midlet.getSuiteId() == MIDletSuite.INTERNAL_SUITE_ID &&
                !midletClassName.equals(DISCOVERY_APP) &&
                !midletClassName.equals(INSTALLER) &&
                !midletClassName.equals(CA_MANAGER) &&
                !midletClassName.equals(COMP_MANAGER) &&
                !midletClassName.equals(ODT_AGENT)) {
            appManagerMidlet = null;
            appManagerUI.notifyInternalMidletExited(midlet);
        } else {
            RunningMIDletSuiteInfo si;

            for (int i = 0; i < msiVector.size(); i++) {
                si = (RunningMIDletSuiteInfo)msiVector.elementAt(i);

                if (si.hasProxy(midlet)) {

                    si.removeProxy(midlet);

                    if (si.hasSingleMidlet() || 
                            (! si.hasRunningMidlet() && si.hasMainMidlet())) {
                        si.unlock();
                    }

                    appManagerUI.notifyMidletExited(si, midletClassName);
                    
                    if (si.numberOfRunningMidlets() == 0) {
                        manager.notifySuiteExited(si, null);
                    }

                    /*
                     * Remove the suite scheduled for removal after the midlet
                     * has exited. Note that in the case of two running midlets,
                     * we must wait till the last midlet exits.
                     */
                    if (removeMsi != null && removeMsi.sameSuite(midlet) &&
                            !removeMsi.hasRunningMidlet()) {
                        removeSuiteImpl(removeMsi);
                    }

                    /*
                     * When the Installer midlet quites
                     * (it is removed from the running apps list)
                     * this is a good time to see if any new MIDlet suites
                     * where added
                     * Also the CA manager could have disabled a MIDlet.
                     */
                    if (INSTALLER.equals(midletClassName)) {
                        updateContent();
                        /*
                        * After a MIDlet suite is successfully installed on the
                        * device, ask the user whether or not to launch
                        * a MIDlet from the suite.
                        */
                        RunningMIDletSuiteInfo msi = getLastInstalledMidletItem();
                        if (msi != null) {
                            appManagerUI.notifySuiteInstalled(msi);
                            return;
                        }
                    } else if (CA_MANAGER.equals(midletClassName) || COMP_MANAGER.equals(midletClassName)) {
                        updateContent();
                    }

                    return;
                }
            }
        }
    }
    
    /**
     * Called when a new MIDlet suite is installed externally.
     * 
     * @param suiteId ID of the newly installed MIDlet suite
     */         
    void notifySuiteInstalled(int suiteId) {
        updateContent();
        int size = msiVector.size();
        for (int i = 0; i < size; i++) {
            RunningMIDletSuiteInfo msi = (RunningMIDletSuiteInfo)msiVector.elementAt(i);
            if (msi.suiteId == suiteId) {
                appManagerUI.notifySuiteInstalledExt(msi);
                break;
            }
        }        
        
    }

    /**
     * Called when a MIDlet suite has been removed externally.
     * 
     * @param suiteId ID of the removed MIDlet suite          
     */         
    void notifySuiteRemoved(int suiteId) {
        int size = msiVector.size();
        for (int i = 0; i < size; i++) {
            RunningMIDletSuiteInfo msi = (RunningMIDletSuiteInfo)msiVector.elementAt(i);
            if (msi.suiteId == suiteId) {
                appManagerUI.notifySuiteRemovedExt(msi);
                msiVector.removeElementAt(i);
                appManagerUI.itemRemoved(msi);
                break;
            }
        }
        updateContent();
    }
    
    /**
     * Called when a suite exited (last running MIDlet in suite exited).
     * @param suiteInfo Suite which just exited
     */
    void notifySuiteExited(RunningMIDletSuiteInfo suiteInfo) {
        appManagerUI.notifySuiteExited(suiteInfo);
    }

    /**
     * Called when MIDlet selector exited.
     * @param suiteInfo Suite whose selector just exited
     */
    void notifyMIDletSelectorExited(RunningMIDletSuiteInfo suiteInfo) {
        appManagerUI.notifyMIDletSelectorExited(suiteInfo);
    }
    
    // ------------------------------------------------------------------

    /**
     * Read in and create a MIDletInfo for newly added MIDlet suite and
     * check enabled state of currently added MIDlet suites.
     */
    private void updateContent() {
        int[] suiteIds;
        RunningMIDletSuiteInfo msi = null;

        suiteIds = midletSuiteStorage.getListOfSuites();

        /*
         * IMPL_NOTE: Sorting MIDlet suite names is up to implementations
         * of the AppManagerUI interface. AppManagerPeer notifies the UI
         * via the append() function about added internal MIDlets and
         * user-added MIDlet suites, and this happens to be "internal
         * MIDlets first, then user-installed MIDlet suites in the order
         * of suite IDs", but the UI can sort the suite names on the
         * screen in any other way.  
         */
        
        // Add the Installer as the first installed midlet
        if (null == findInternalMidletRmsi(DISCOVERY_APP)) {

            msi = new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
                DISCOVERY_APP,
                Resource.getString(ResourceConstants.INSTALL_APPLICATION),
                true) {
                    public boolean sameSuite(MIDletProxy midlet) {
                        // there is one exception when 2 midlets belong to the
                        // same icon: Discovery app & Graphical installer.
                        // Graphical Installer can be launched by Discover app
                        // or when MIdlet update is needed.
                        // In such cases we simply need to set the proxy on
                        // corresponding icon (MidletCustomItem).
                        // Note that when Discovery app exits and
                        // Installer is launched
                        // notifyMidletExited() will not find corresponding
                        // icon in the list of MidletCustomItems.
                        // (that midlet exit will be ignored).

                        return sameSuiteId(midlet)
                            && (INSTALLER.equals(midlet.getClassName())
                                || DISCOVERY_APP.equals(midlet.getClassName()));
                    }
                };

            append(msi);
        }

        if (caManagerIncluded) {
            // Add the CA manager as the second installed midlet
            if (null == findInternalMidletRmsi(CA_MANAGER)) {

                msi = new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
                  CA_MANAGER,
                  Resource.getString(ResourceConstants.CA_MANAGER_APP), true);
                append(msi);
            }
        }

        if (compManagerIncluded) {
            // Add the component manager as the next installed midlet
            if (null == findInternalMidletRmsi(COMP_MANAGER)) {

                msi = new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
                  COMP_MANAGER,
                  Resource.getString(ResourceConstants.COMP_MANAGER_APP), true);
                append(msi);
            }
        }

        if (oddEnabled) {
            // Add the ODT Agent midlet as the next installed midlet
            if (null == findInternalMidletRmsi(ODT_AGENT)) {

                msi = new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
                  ODT_AGENT,
                  Resource.getString(ResourceConstants.ODT_AGENT_MIDLET), true);
                append(msi);
            }
        }

        // Add the rest of the installed midlets
        for (int lowest, i = 0; i < suiteIds.length; i++) {
            lowest = i;

            for (int k = i + 1; k < suiteIds.length; k++) {
                if (suiteIds[k] < suiteIds[lowest]) {
                    lowest = k;
                }
            }
            try {

                RunningMIDletSuiteInfo updatedMsi =
                    new RunningMIDletSuiteInfo(
                            midletSuiteStorage.getMIDletSuiteInfo(suiteIds[lowest]),
                            midletSuiteStorage);

                msi = findUserInstalledSuiteRmsi(suiteIds[lowest]);
                if (null == msi) {
                    // newly added
                    append(updatedMsi);
                    launchSuite(updatedMsi);
                } else {
                    MIDletSuiteInfo oldMsi = new MIDletSuiteInfo(msi.suiteId);
                    oldMsi.copyFieldsFrom(msi);

                    appManagerUI.notifyMIDletSuiteStateChanged(msi, updatedMsi);

                    msi.copyFieldsFrom(updatedMsi);

                    // Update all information about the suite;
                    // if the suite's icon was changed, reload it.
                    if (oldMsi.enabled != updatedMsi.enabled) {
                        // MIDlet suite being enabled
                        appManagerUI.notifyMIDletSuiteEnabled(msi);
                        // running MIDlets will continue to run
                        // even when disabled
                    }

                    if ((updatedMsi.iconName != null &&
                            !updatedMsi.iconName.equals(oldMsi.iconName)) ||
                        (updatedMsi.iconName == null &&
                            updatedMsi.numberOfMidlets != oldMsi.numberOfMidlets)
                    ) {
                        msi.icon = null;
                        msi.loadIcon(midletSuiteStorage);
                        appManagerUI.notifyMIDletSuiteIconChaged(updatedMsi);
                    }

                }

            } catch (Exception e) {
                // move on to the next suite
            }

            suiteIds[lowest] = suiteIds[i];
        }
    }

    /**
     * Appends an item to the list
     *
     * @param suiteInfo the midlet suite info
     *                  of the recently started midlet
     */
    private void append(RunningMIDletSuiteInfo suiteInfo) {
        msiVector.addElement(suiteInfo);
        appManagerUI.itemAppended(suiteInfo);
    }

    /**
     * If extended MIDlet attributes support enabled, launch all MIDlets from
     * the suite that have MIDlet-Launch-Power-On attribute set to true.
     * <p/>
     * This method is used during JVM start-up to run all such MIDlets from all
     * installed suites. The method is also utilzied for newly installed
     * MIDlets that have the attribute to launch them right after installation
     * process is completed.
     *
     * @param suiteInfo describes the suite
     */
    private void launchSuite(RunningMIDletSuiteInfo suiteInfo) {
        if (Constants.EXTENDED_MIDLET_ATTRIBUTES_ENABLED) {
            // IMPL_NOTE: for better performance open suite only one time
            MIDletSuite suite = MIDletSuiteUtils.getSuite(suiteInfo.suiteId);

            if (suite != null) {
                /**
                 * Calling to MIDletSuiteUtils.getSuite locks the
                 * suite this makes impossible to launch a MIDlet
                 * from it. So we have to iterate over all MIDlets
                 * in the suite and remember which of them must be
                 * started upon JVM start-up.
                 */
                int midletsNum = suiteInfo.numberOfMidlets;
                Vector midletsToStart = new Vector(midletsNum);
                try {
                    for (int m = 1; m <= midletsNum; m++) {
                        String prop = MIDletSuiteUtils.getSuiteProperty(
                            suite, m, MIDletSuite.LAUNCH_POWER_ON_PROP);

                        if ("yes".equalsIgnoreCase(prop)) {
                            String className =
                                MIDletSuiteUtils.getMIDletClassName(suite, m);
                            midletsToStart.addElement(className);
                        }
                    }
                } finally {
                    suite.close();
                }

                for (Enumeration e = midletsToStart.elements();
                        e.hasMoreElements() ;) {
                    manager.launchSuite(suiteInfo, (String)e.nextElement());
                }
            }
        }
    }

    /**
     * Removes a suite from the App Selector Screen
     *
     * @param suiteInfo the midlet suite info of a recently removed MIDlet
     */
    public void removeSuite(RunningMIDletSuiteInfo suiteInfo) {
        if (suiteInfo == null) {
            // Invalid parameter, should not happen.
            return;
        }

        if (suiteInfo.hasRunningMidlet()) {
            /*
             * Schedule the midlet suite for removal after all running midlets
             * from it are destroyed.
             * The removing routine is called from notifyMidletExited(). 
             */
            removeMsi = suiteInfo;

            // destroy all running midlets from the suite being removed
            MIDletProxy[] runningMidlets = suiteInfo.getProxies();
            if (runningMidlets != null) {
                for (int i = 0; i < runningMidlets.length; i++) {
                    runningMidlets[i].destroyMidlet();
                }
            }
        } else {
            /*
             * There are no running midlets belonging to the suite
             * being removed, so remove the suite immediately.
             */
            removeSuiteImpl(suiteInfo);
        }
    }

    /**
     * Removes a suite from the App Selector Screen
     *
     * @param suiteInfo the midlet suite info of a recently removed MIDlet
     */
    private void removeSuiteImpl(RunningMIDletSuiteInfo suiteInfo) {
        if (suiteInfo == null) {
            // Invalid parameter, should not happen.
            return;
        }

        // the last item in AppSelector is time
        for (int i = 0; i < msiVector.size(); i++) {
            RunningMIDletSuiteInfo msi =
                    (RunningMIDletSuiteInfo)msiVector.elementAt(i);
            if (msi == suiteInfo) {
                PAPICleanUp.removeMissedTransaction(suiteInfo.suiteId);

                try {
                    midletSuiteStorage.remove(suiteInfo.suiteId);
                } catch (Throwable t) {
                    if (t instanceof MIDletSuiteLockedException) {
                        String[] val = new String[1];
                        val[0] = suiteInfo.displayName;
                        displayError.showErrorAlert(suiteInfo.displayName,
                            null,
                            Resource.getString(ResourceConstants.ERROR),
                            Resource.getString(
                                ResourceConstants.AMS_MGR_REMOVE_LOCKED_SUITE,
                                    val),
                            appManagerUI.getMainDisplayable());
                    } else {
                        displayError.showErrorAlert(suiteInfo.displayName,
                            t,
                            Resource.getString(ResourceConstants.ERROR),
                            null, appManagerUI.getMainDisplayable());
                    }

                    return;
                }


                try {
                    PushRegistryInternal.unregisterConnections(
                        suiteInfo.suiteId);
                } catch (Throwable t) {
                    // Intentionally ignored: suite has been removed already,
                    // we can't do anything meaningful at this point.
                }


                msiVector.removeElementAt(i);
                appManagerUI.itemRemoved(msi);
                removeMsi = null;
                break;
            }
        }

        display.setCurrent(appManagerUI.getMainDisplayable());
    }

    /**
     * Appends a names of all the MIDlets in a suite to a Form, one per line.
     *
     * @param midletSuite information of a suite of MIDlets
     *
     * @return array of strings to be appended to a Form
     */
    public String[] getMIDletsNames(MIDletSuiteImpl midletSuite) {
        int numberOfMidlets;
        MIDletInfo midletInfo;
        numberOfMidlets = midletSuite.getNumberOfMIDlets();
        String[] ret = new String[numberOfMidlets];

        for (int i = 1; i <= numberOfMidlets; i++) {
            midletInfo = new MIDletInfo(
                             midletSuite.getProperty("MIDlet-" + i));
            ret[i-1] = new String(midletInfo.name);
        }
        
        return ret;
    }

    /**
     * Retrieves the MIDlet suite Display name.
     *
     * @param midletSuite the MIDletSuiteImpl object instance
     *
     * @exception Exception if problem occurs while getting the suite info
     */
    public String getMidletSuiteDisplayName(MIDletSuiteImpl midletSuite)
        throws Exception {

        int numberOfMidlets = midletSuite.getNumberOfMIDlets();

        if (numberOfMidlets == 1) {
            String value = midletSuite.getProperty("MIDlet-1");
            MIDletInfo temp = new MIDletInfo(value);
            return temp.name;
        } else {
            return midletSuite.getProperty(MIDletSuiteImpl.SUITE_NAME_PROP);
        }
    }


    /**
     * Open the settings database and retreive an id of the midlet suite
     * that was installed last.
     *
     * @return ID of the midlet suite that was installed last or
     * MIDletSuite.UNUSED_SUITE_ID.
     */
    private int getLastInstalledMIDlet() {
        ByteArrayInputStream bas;
        DataInputStream dis;
        byte[] data;
        RecordStore settings = null;
        int ret = MIDletSuite.UNUSED_SUITE_ID;

        try {
            settings = RecordStore.
                       openRecordStore(GraphicalInstaller.SETTINGS_STORE,
                                       false);

            /** we should be guaranteed that this is always the case! */
            if (settings.getNumRecords() > 0) {

                data = settings.getRecord(
                           GraphicalInstaller.SELECTED_MIDLET_RECORD_ID);

                if (data != null) {
                    bas = new ByteArrayInputStream(data);
                    dis = new DataInputStream(bas);
                    ret = dis.readInt();
                }
            }

        } catch (RecordStoreException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        } finally {
            if (settings != null) {
                try {
                    settings.closeRecordStore();
                } catch (RecordStoreException e) {
                    // ignore
                }
            }
        }

        return ret;
    }

    /**
     * Finds a RunningMIDletSuiteInfo corresponding to the last installed
     * midlet suite.
     * @return the midlet suite info item if it was found, null otherwise
     */
    public RunningMIDletSuiteInfo getLastInstalledMidletItem() {
        int installedMidlet = getLastInstalledMIDlet();

        if (installedMidlet != MIDletSuite.UNUSED_SUITE_ID &&
                installedMidlet != MIDletSuite.INTERNAL_SUITE_ID) {
            for (int i = 0; i < msiVector.size(); i++) {
                RunningMIDletSuiteInfo si =
                    (RunningMIDletSuiteInfo)msiVector.elementAt(i);
                if (si.suiteId == installedMidlet) {
                    return si;
                }
            }
        }

        return null;
    }

    /**
     * Launches the midlet suite described by the given MIDletSuiteInfo.
     * @param msi a structure with information about the midlet suite
     * that must be launched
     */
    public void launchMidlet(RunningMIDletSuiteInfo msi) {
        if (msi.hasSingleMidlet()) {
            manager.launchSuite(msi, msi.midletToRun);
        } else {
            throw new IllegalArgumentException(
                    "Suite contains more that one MIDlet.");
        }
    }

    /**
     * Find a running midlet suite info object for a user-installed suite
     * with the specified suite id. Since all internal MIDlets share
     * the same suite id (MIDletSuite.INTERNAL_SUITE_ID), this function
     * is not meaningful for internal MIDlets.
     * @param suiteId Suite ID to search info for
     * @return the matching info, null if not found.
     */
    private RunningMIDletSuiteInfo findUserInstalledSuiteRmsi(int suiteId) {
        if (suiteId == MIDletSuite.INTERNAL_SUITE_ID) {
            return null;
        }
        int size = msiVector.size();
        for (int i = 0; i < size; i++) {
            RunningMIDletSuiteInfo info = 
                    (RunningMIDletSuiteInfo) msiVector.elementAt(i);
            if (info.suiteId == suiteId) {
                return info;
            }
        }
        return null;
    }
    
    /**
     * Find a running midlet suite info object for an internal MIDlet
     * whose class name is specified as the parameter.
     * @param midletToRun the class name of the main MIDlet
     * @return the matching info, null if not found.
     */
    private RunningMIDletSuiteInfo findInternalMidletRmsi(String midletToRun) {
        int size = msiVector.size();
        for (int i = 0; i < size; i++) {
            RunningMIDletSuiteInfo info =
                    (RunningMIDletSuiteInfo) msiVector.elementAt(i);
            if (info.isInternal() && info.midletToRun.equals(midletToRun)) {
                return info;
            }
        }
        return null;
    }

    
    /**
     * Launches the midlet suite determined by the suite ID.
     * @param suiteId ID of the suite to launch
     * @param midletClassname MIDlet to run, may be null, then will be launched
     *        the single MIDlet or MIDlet selector
     * @param isDebugMode whether the suite should be launched in debug mode
     * @param lock whether the running suite should be locked
     */
    void launchSuite(int suiteId, String midletClassname, boolean isDebugMode,
            boolean lock) {

        RunningMIDletSuiteInfo msi = findUserInstalledSuiteRmsi(suiteId);
        if (msi == null) {
            if (midletClassname == null) {
                /* unknown suite, classname not specified: cannot say what to 
                   launch */
                throw new IllegalArgumentException(
                        "Suite id " + suiteId + " not found.");
            }

            /* The only thing used from the msi for launching is suiteId, so 
             * we can create the info ourselves. This is needed when suite
             * is installed without notification (e.g. by autotester) */
            msi = new RunningMIDletSuiteInfo(suiteId);
        } else if (msi.hasSingleMidlet()) {
            midletClassname = msi.midletToRun;
        }
        
        msi.isDebugMode = isDebugMode;

        if (lock) {
            msi.lock();
        }
        
        msi.setMainMidlet(midletClassname != null);
        if (midletClassname != null) {
            manager.launchSuite(msi, midletClassname);
        } else {
            showMidletSelector(msi);
        }
    }

    /**
     * Exits a MIDlet from the specified suite or all runing MIDlets of that
     * suite.
     * 
     * @param suiteId the id of the suite to exit
     * @param midletClassname the class name of the MIDlet to exit or 
     *      <code>null</code> if all running MIDlets of the specified suite 
     *      should be exited
     */
    void exitSuite(final int suiteId, final String midletClassname) {
        final RunningMIDletSuiteInfo msi = findUserInstalledSuiteRmsi(suiteId);
        if (msi == null) {
            // already exited?
            return;
        }
        
        if (midletClassname != null) {
            // destroy a single MIDlet from the suite
            final MIDletProxy proxy = msi.getProxyFor(midletClassname);
            if (proxy != null) {
                proxy.destroyMidlet();
            }
            return;
        }

        final MIDletProxy[] proxies = msi.getProxies();
        for (int i = 0; i < proxies.length; ++i) {
            proxies[i].destroyMidlet();
        }
    }
    
    /**
     * Checks if the installer is currently running.
     *
     * @return true if the installer or discovery application is running,
     *         false otherwise
     */
    private boolean isInstallerRunning() {
        RunningMIDletSuiteInfo msi;

        for (int i = 0; i < msiVector.size(); i++) {
            msi = (RunningMIDletSuiteInfo)msiVector.elementAt(i);
            if (msi.isInternal() &&
                msi.hasRunningMidlet() &&
                    (DISCOVERY_APP.equals(msi.midletToRun) ||
                     INSTALLER.equals(msi.midletToRun))) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param si
     */
    public void updateSuite(RunningMIDletSuiteInfo si) {
        if (!isInstallerRunning()) {
            manager.updateSuite(si);
            display.setCurrent(appManagerUI.getMainDisplayable());
        } else {
            String alertMessage = Resource.getString(
                ResourceConstants.AMS_MGR_INSTALLER_IS_RUNNING);

            displayError.showErrorAlert(null, null,
                Resource.getString(ResourceConstants.ERROR),
                alertMessage);
        }
    }

    /**
     * Should be called by AppManageUI when icon for the Installer
     * is to be shown.
     */
    public void ensureNoInternalMIDletsRunning() {

        // When icon for the Installer
        // is shown we want to make sure
        // that there are no running midlets from the "internal" suite.
        // The only 2 midlets that can run in bg from
        // "internal" suite are the DiscoveryApp and the Installer.
        // Icon for the Installer will be shown each time
        // the AppSelector is made current since it is the top
        // most icon and we reset the traversal to start from the top
        if (appManagerMidlet != null) {
            appManagerMidlet.destroyMidlet();
        }
    }

    /**
     * Called when a midlet could not be launched.
     *
     * @param suiteId suite ID of the MIDlet
     * @param className class name of the MIDlet
     * @param errorCode error code
     * @param errorDetails error code details
     */
    public void notifyMidletStartError(int suiteId, String className, int errorCode,
                                String errorDetails) {
        appManagerUI.notifyMidletStartError(suiteId, className, errorCode, errorDetails);
    }

    /**
     * Called when midlet switcher is needed.
     *
     * @param onlyFromLaunchedList true if midlet should
     *        be selected from the list of already launched midlets,
     *        if false then possibility to launch midlet is needed.
     */
    public void showMidletSwitcher(boolean onlyFromLaunchedList) {
        appManagerUI.showMidletSwitcher(onlyFromLaunchedList);
    }

    /**
     * Called when midlet selector is needed. Should show a list of
     * midlets present in the given suite and allow to select one.
     *
     * @param msiToRun a suite from which a midlet must be selected
     */
    public void showMidletSelector(RunningMIDletSuiteInfo msiToRun) {
        appManagerUI.showMidletSelector(msiToRun);
    }

    /**
     * Called by Manager when destroyApp happens to clean up data.
     */
    public void cleanUp() {
        appManagerUI.cleanUp();
    }

    /**
     * Called to determine MidletSuiteInfo of the last selected Item.
     *
     * @return last selected MidletSuiteInfo
     */
    public RunningMIDletSuiteInfo getSelectedMIDletSuiteInfo() {
        return appManagerUI.getSelectedMIDletSuiteInfo();
    }

    
}


