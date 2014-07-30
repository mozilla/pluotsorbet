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

import com.sun.midp.configurator.Constants;

import com.sun.midp.installer.*;
import com.sun.midp.main.*;
import com.sun.midp.midletsuite.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.midp.payment.PAPICleanUp;
import com.sun.midp.jsr075.FileConnectionCleanup;

import com.sun.midp.midlet.MIDletSuite;

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
class AppManagerUIImpl extends Form
    implements AppManagerUI, ItemCommandListener, CommandListener {

    /** Command object for "Exit" command for splash screen. */
    private Command exitCmd =
        new Command(Resource.getString(ResourceConstants.EXIT),
                    Command.BACK, 1);

    /** Command object for "Launch" install app. */
    private Command launchInstallCmd =
        new Command(Resource.getString(ResourceConstants.LAUNCH),
                    Command.ITEM, 1);

    /** Command object for "Launch" CA manager app. */
    private Command launchCaManagerCmd =
        new Command(Resource.getString(ResourceConstants.LAUNCH),
                    Command.ITEM, 1);

    /** Command object for "Launch" CA manager app. */
    private Command launchCompManagerCmd =
        new Command(Resource.getString(ResourceConstants.LAUNCH),
                    Command.ITEM, 1);

    /** Command object for "Launch" ODT Agent app. */
    private Command launchODTAgentCmd =
        new Command(Resource.getString(ResourceConstants.LAUNCH),
                    Command.ITEM, 1);

    /** Command object for "Launch". */
    private Command openCmd =
        new Command(Resource.getString(ResourceConstants.OPEN),
                    Command.ITEM, 1);
    /** Command object for "Info". */
    private Command infoCmd =
        new Command(Resource.getString(ResourceConstants.INFO),
                    Command.ITEM, 2);
    /** Command object for "Remove". */
    private Command removeCmd =
        new Command(Resource.getString(ResourceConstants.REMOVE),
                    Command.ITEM, 3);
    /** Command object for "Update". */
    private Command updateCmd =
        new Command(Resource.getString(ResourceConstants.UPDATE),
                    Command.ITEM, 4);
    /** Command object for "Application settings". */
    private Command appSettingsCmd =
        new Command(Resource.
                    getString(ResourceConstants.APPLICATION_SETTINGS),
                    Command.ITEM, 5);
    /** Command object for moving to internal storage. */
    private Command moveToInternalStorageCmd =
        new Command(Resource.
                    getString(ResourceConstants.AMS_MOVE_TO_INTERNAL_STORAGE),
                    Command.ITEM, 6);
    /** Command object for "view components". */
    private Command viewCompCmd =
        new Command(Resource.getString(ResourceConstants.VIEW_COMP),
                    Command.ITEM, 7);


    /** Command object for "Cancel" command for the remove form. */
    private Command cancelCmd =
        new Command(Resource.getString(ResourceConstants.CANCEL),
                    Command.CANCEL, 1);
    /** Command object for "Remove" command for the remove form. */
    private Command removeOkCmd =
        new Command(Resource.getString(ResourceConstants.REMOVE),
                    Command.SCREEN, 1);

    /** Command object for "Back" command for back to the AppSelector. */
    Command backCmd =
        new Command(Resource.getString(ResourceConstants.BACK),
                    Command.BACK, 1);


    /** Command object for "Bring to foreground". */
    private Command fgCmd = new Command(Resource.getString
                                        (ResourceConstants.FOREGROUND),
                                        Command.ITEM, 1);

    /** Command object for "End" midlet. */
    private Command endCmd = new Command(Resource.getString
                                         (ResourceConstants.END),
                                         Command.ITEM, 1);

    /** Command object for "Yes" command. */
    private Command runYesCmd = new Command(Resource.getString
                                            (ResourceConstants.YES),
                                            Command.OK, 1);

    /** Command object for "No" command. */
    private Command runNoCmd = new Command(Resource.getString
                                           (ResourceConstants.NO),
                                           Command.BACK, 1);

    /** Command object for "Select" command in folder list. */
    private Command selectFolderCmd = new Command(Resource.getString
                                           (ResourceConstants.SELECT),
                                           Command.OK, 1);

    /** Command object for "Select" command in folder list. */
    private Command selectItemFolderCmd = new Command(Resource.getString
                                           (ResourceConstants.SELECT),
                                           Command.OK, 1);

    /** Command object for "Change folder" command. */
    private Command changeFolderCmd = new Command(Resource.getString
                                           (ResourceConstants.AMS_CHANGE_FOLDER),
                                           Command.ITEM, 1);

    /** Display for the Manager MIDlet. */
    ApplicationManager manager;

    /** task manager */
    AppManagerPeer appManager;

    /** MIDlet Suite storage object. */
    private MIDletSuiteStorage midletSuiteStorage;

    /** Display for the Manager MIDlet. */
    Display display; // = null

    /** Keeps track of when the display last changed, in milliseconds. */
    private long lastDisplayChange;

    /** MIDlet to be removed after confirmation screen was accepted */
    private RunningMIDletSuiteInfo removeMsi;

    /** last Item that was selected */
    private RunningMIDletSuiteInfo lastSelectedMsi;

   /** vector of existing MidletCustomItems */
    private Vector mciVector;

    /** UI used to display error messages. */
    private DisplayError displayError;

    private MIDletSwitcher midletSwitcher;

    /** List of available folders */
    private FolderList folderList;

    /** currently selected folder' id */
    private int currentFolderId;

    /** If there are folders */
    private boolean foldersOn;

    /** custom item for which command to change folder was activated */
    private AppManagerMIDletCustomItem mciToChangeFolder;

    /** running MIDlet selectors */
    private Vector midletSelectors;
    
    /** suite id of midlet exiting from AMS menu */
    int exitingMidletSuiteId;
    /** class name of midlet exiting from AMS menu */
    String exitingMidletClassName;
    

    private void init(ApplicationManager manager, AppManagerPeer appManager,
                 Display display, DisplayError displayError, boolean foldersOn) {
        mciToChangeFolder = null;

        this.foldersOn = foldersOn;
        if (foldersOn) {
            folderList = new FolderList();
            folderList.addCommand(exitCmd);
            folderList.addCommand(selectFolderCmd);
            folderList.setSelectCommand(selectFolderCmd);
            folderList.setCommandListener(this);
        }

        mciVector = new Vector();

        this.manager = manager;
        this.appManager = appManager;
        this.display = display;
        this.displayError = displayError;

        midletSwitcher = new MIDletSwitcher(this, manager, display);

        midletSuiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();

        midletSelectors = new Vector();
        exitingMidletSuiteId = 0;
        exitingMidletClassName = null;

        setTitle(Resource.getString(ResourceConstants.AMS_MGR_TITLE));
        
        if (foldersOn) {
            addCommand(backCmd);
        } else {
            addCommand(exitCmd);
        }

        setCommandListener(this);
        
    }

    /**
     * Creates the Application Selector Screen.
     * Called if this is the first time AppSelector is being shown.
     *
     * @param manager - The application manager that invoked it
     * @param appManager - The app manager
     * @param display - The display instance associated with the manager
     * @param displayError - The UI used to display error messages
     * @param foldersOn - if folders are used
     */
    AppManagerUIImpl(ApplicationManager manager, AppManagerPeer appManager,
                 Display display, DisplayError displayError, boolean foldersOn) {
        super(null);

        init(manager, appManager, display, displayError, foldersOn);

        if (foldersOn) {
            display.setCurrent(new SplashScreen(display, folderList));
        } else {
            display.setCurrent(new SplashScreen(display, this));
        }
    }


    /**
     * Creates the Application Selector Screen.
     * @param manager - The application manager that invoked it
     * @param appManager - The app manager
     * @param display - The display instance associated with the manager
     * @param displayError - The UI used to display error messages
     * @param foldersOn - if folders are used
     * @param askUserIfLaunchMidlet - If true, it is expected that dialog be shown asking
     *             user if last installed midlet should be launched.
     */
    AppManagerUIImpl(ApplicationManager manager, AppManagerPeer appManager,
                 Display display, DisplayError displayError, boolean foldersOn,
                 boolean askUserIfLaunchMidlet) {
        super(null);
        init(manager, appManager, display, displayError, foldersOn);
        if (askUserIfLaunchMidlet) {
            askUserIfLaunchMidlet();
        }  else {
            display.setCurrent(this);
        }
   }

    /**
     * Requests that the ui element, associated with the specified midlet
     * suite, be visible and active.
     *
     * @param msi corresponding suite info
     */
    public void setCurrentItem(RunningMIDletSuiteInfo msi) {
        for (int i = 0; i < mciVector.size(); i++) {
            AppManagerMIDletCustomItem mci =
                (AppManagerMIDletCustomItem)mciVector.elementAt(i);
            if (mci.msi == msi) {
                selectItem(mci);
                break;
            }
        }

    }

    /**
     * Selects specified item. If necessary changes folder to items folder. 
     */
    private void selectItem(AppManagerMIDletCustomItem mi) {
        if (foldersOn) {
            if (currentFolderId != mi.msi.folderId) {
                setFolder(mi.msi.folderId);
            }
        }
        display.setCurrentItem(mi);
    }

    /**
     * Changes current folder and refreshes items on the
     * form accordingly.
     *
     * @param fid new current folder id
     */
    private void setFolder(int fid) {
        if (foldersOn) {
            currentFolderId = fid;
            deleteAll();
            for (int i = 0; i < mciVector.size(); i++) {
                AppManagerMIDletCustomItem mci =
                    (AppManagerMIDletCustomItem)mciVector.elementAt(i);
                if (currentFolderId == mci.msi.folderId) {
                    append(mci);
                }
            }
        } else {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                               "Folders are not supported");
            }
        }
    }
    
    /**
     * Called when midlet switcher is needed.
     *
     * @param onlyFromLaunchedList true if midlet should
     *        be selected from the list of already launched midlets,
     *        if false then possibility to launch midlet is needed.
     */
    public void showMidletSwitcher(boolean onlyFromLaunchedList) {
        if (onlyFromLaunchedList && midletSwitcher.hasItems()) {
            display.setCurrent(midletSwitcher);
        } else {
            display.setCurrent(this);
        }
    }

    /**
     * Called when midlet selector is needed. Should show a list of
     * midlets present in the given suite and allow to select one.
     *
     * @param msiToRun a suite from which a midlet must be selected
     */
    public void showMidletSelector(RunningMIDletSuiteInfo msiToRun) {
        if (msiToRun != null) {
            try {
                MIDletSelector selector = getMidletSelector(msiToRun.suiteId);
                if (selector != null) {
                    selector.show();
                    return;
                }
                
                midletSelectors.addElement(new MIDletSelector(msiToRun, display, this, manager));
            } catch (Exception e) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                                   "showMidletSelector(): " + e.getMessage());
                }

                displayError.showErrorAlert(msiToRun.displayName,
                                            e, null, null);
            }
        } else {
            display.setCurrent(this);
        }
    }
    
    /**
     * Called to determine MidletSuiteInfo of the last selected Item.
     *
     * @return last selected MidletSuiteInfo
     */
    public RunningMIDletSuiteInfo getSelectedMIDletSuiteInfo() {
        return lastSelectedMsi;
    }

    /**
     * Respond to a command issued on any Screen.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == exitCmd) {
            if ((s == this) || s == folderList) {
                manager.shutDown();
            }
            return;
        }

        // for the rest of the commands
        // we will have to request AppSelector to be displayed
        if (c == removeOkCmd) {

            // suite to remove was set in confirmRemove()
            try {
                appManager.removeSuite(removeMsi);
            } catch (Throwable t) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                                   "Throwable in removeSuitee");
                }
            }
            return;

        } else if (c == cancelCmd) {

            // null out removeMsi in remove confirmation screen
            removeMsi = null;

        } else if (c == runYesCmd) {

            // user decided run the midlet suite after installation
            RunningMIDletSuiteInfo msiToRun = appManager.getLastInstalledMidletItem();
            if (msiToRun != null) {
                display.setCurrentItem(findItem(msiToRun));
                if (msiToRun.hasSingleMidlet()) {
                    appManager.launchMidlet(msiToRun);
                    display.setCurrent(this);
                } else {
                    appManager.showMidletSelector(msiToRun);
                }
                return;
            }

        } else if (c == runNoCmd) {

            /*
             * user decided not to run the newly installed midlet suite
             *
             * if a MIDlet was just installed
             * we should make the corresponding item active
             */
            RunningMIDletSuiteInfo isi = appManager.getLastInstalledMidletItem();
            if (isi != null) {
                setCurrentItem(isi);
                return;
            }

        } else if (c == selectFolderCmd) {
            if (foldersOn) {
                Folder f = folderList.getSelectedFolder();
                setFolder(f.getId());
            }
        } else if (c == selectItemFolderCmd) {
            if (foldersOn && (null != mciToChangeFolder)) {
                Folder f = folderList.getSelectedFolder();
                int folderId = f.getId();

                if (mciToChangeFolder.msi.folderId != folderId) {
                    try {
                        midletSuiteStorage.moveSuiteToFolder(
                                mciToChangeFolder.msi.suiteId, folderId);
                        mciToChangeFolder.msi.folderId = folderId;
                        if (currentFolderId != folderId) {
                            /*
                             * IMPL_NOTE: optimization possible - we can remove only
                             *  one item and avoid full refresh
                             */
                            setFolder(currentFolderId);
                        }
                    } catch (Throwable t) {
                        displayError.showErrorAlert(
                                mciToChangeFolder.msi.displayName,
                                    t, null, null);
                    }
                    mciToChangeFolder = null;
                }
            }
            display.setCurrent(this);
        } else if (c == backCmd) {
            if ((display.getCurrent() == this) && foldersOn) {
                folderList.removeCommand(selectItemFolderCmd);
                folderList.addCommand(exitCmd);
                folderList.addCommand(selectFolderCmd);
                folderList.setSelectCommand(selectFolderCmd);
                folderList.setSelectedFolder(currentFolderId);
                display.setCurrent(folderList);
                return;
            }
        } else {
            return;
        }

        // for back we just need to display AppSelector
        display.setCurrent(this);
    }
    
    /**
     * Launches a midlet suite.
     * @param msi Structure identifying the suite to launch
     */
    private void enterSuite(RunningMIDletSuiteInfo msi) {
        if (msi.hasSingleMidlet()) {
            appManager.launchMidlet(msi);
            display.setCurrent(this);
        } else {
            appManager.showMidletSelector(msi);
        }        
    }
    
    /**
     * Respond to a command issued on an Item in AppSelector
     *
     * @param c command activated by the user
     * @param item the Item the command was on.
     */
    public void commandAction(Command c, Item item) {
        RunningMIDletSuiteInfo msi = ((AppManagerMIDletCustomItem)item).msi;
        if (msi == null) {
            return;
        }

        if (c == launchInstallCmd) {

            manager.installSuite();

        } else if (c == launchCaManagerCmd) {

            manager.launchCaManager();

        } else if (c == launchCompManagerCmd) {

            manager.launchComponentManager();

        } else if (c == launchODTAgentCmd) {

            manager.launchODTAgent();

        } if (c == openCmd) {

            enterSuite(msi);
           
        } else if (c == infoCmd) {

            try {
                AppInfo appInfo = new AppInfo(msi.suiteId);
                appInfo.addCommand(backCmd);
                appInfo.setCommandListener(this);
                display.setCurrent(appInfo);
            } catch (Throwable t) {
                displayError.showErrorAlert(msi.displayName, t, null, null);
            }

        } else if (c == viewCompCmd) {

            // Installation of new components is a MIDlet's prerogative.
            // Therefore, we specify the read-only mode (3rd arg).
            ComponentManagerLauncher.componentView(msi.suiteId, display, false);

        } else if (c == removeCmd) {

            showConfirmRemoveDialog(msi);

        } else if (c == updateCmd) {

            appManager.updateSuite(msi);

        } else if (c == appSettingsCmd) {

            try {
                appManager.showAppSettings(msi.suiteId, this);

            } catch (Throwable t) {
                displayError.showErrorAlert(msi.displayName, t, null, null);
            }
        } else if (c == moveToInternalStorageCmd) {
            try {
                midletSuiteStorage.changeStorage(msi.suiteId,
                        Constants.INTERNAL_STORAGE_ID);
                
                /*
                 * According to MIDP Spec security requirements we don't allow
                 *  to copy non DRM-protected MIDlet suite to external storage.
                 */
                
                item.removeCommand(moveToInternalStorageCmd);
                msi.storageId = Constants.INTERNAL_STORAGE_ID;
                displaySuccessMessage(Resource.getString(
                    ResourceConstants.APPLICATION) + Resource.getString(
                        ResourceConstants.AMS_MGR_SUCC_SUITE_STORAGE_CHANGED));
            } catch (Throwable t) {
                displayError.showErrorAlert(msi.displayName, t, null, null);
            }

        } else if (c == fgCmd) {
            // The "Foreground" command will be shown only if there is only one running MIDlet
            manager.moveToForeground(msi, null);
            display.setCurrent(this);

        } else if (c == endCmd) {
            // The "End" command will be shown only if there is only one running MIDlet
            MIDletProxy theOnlyProxy = msi.getFirstProxy();
            if (theOnlyProxy != null) {
                exitingMidletSuiteId = theOnlyProxy.getSuiteId();
                exitingMidletClassName = theOnlyProxy.getClassName();
                manager.exitMidlet(msi, exitingMidletClassName);
            }
            display.setCurrent(this);
        } else if (c == changeFolderCmd) {
            if (foldersOn) {
                folderList.removeCommand(exitCmd);
                folderList.removeCommand(selectFolderCmd);
                folderList.addCommand(selectItemFolderCmd);
                folderList.setSelectCommand(selectItemFolderCmd);
                mciToChangeFolder = (AppManagerMIDletCustomItem)item;
                folderList.setSelectedFolder(currentFolderId);
                display.setCurrent(folderList);
            }
        }
    }

    /**
     *  Called when a new internal midlet was launched
     * 
     * @param midlet proxy of a newly launched MIDlet
     */
    public void notifyInternalMidletStarted(MIDletProxy midlet) {
        // nothing to do    
    }

    /**
     * Called when a new midlet was launched.
     *
     * @param si corresponding midlet suite info
     * @param className MIDlet class name
     */
    public void notifyMidletStarted(RunningMIDletSuiteInfo si, String className) {
        AppManagerMIDletCustomItem ci = findItem(si);
        if (null != ci) {
            setupDefaultCommand(ci);
            setupRunStateDependentCommands(ci);

            // add item to midlet switcher
            midletSwitcher.append(ci.msi, className);
        }
    }

    /**
     * This function encapsulates the logic of showing the "End" and
     * "To Foreground" commands.
     *
     * @param ci midlet custom item
     */
    private void setupRunStateDependentCommands(AppManagerMIDletCustomItem ci) {
        // IMPL_NOTE: we decide to have the "Open" command default for all cases,
        // just to make the life easier for both the programmer and the user.
        RunningMIDletSuiteInfo si = ci.msi;
        if (si.numberOfRunningMidlets() == 1) {
            if (Constants.EXTENDED_MIDLET_ATTRIBUTES_ENABLED) {
                MIDletProxy proxy = si.getFirstProxy();

                if (proxy != null) {
                    /*
                     * Add "Bring to foreground" command only if the MIDLet has
                     * no MIDlet-Launch-Background attribute.
                     */
                    if (!proxy.getExtendedAttribute(
                            MIDletProxy.MIDLET_LAUNCH_BG)) {
                        ci.addCommand(fgCmd);
                    }

                    /*
                     * Check whether MIDlet-No-Exit attribute is defined for
                     * the MIDlet.
                     *
                     * If definded and the value is "yes" then the application
                     * is not allowed to be exited other than calling
                     * destroyApp().
                     *
                     * Add "End" command only if an user may termniate the
                     * MIDlet.
                     */
                    if (!proxy.getExtendedAttribute(
                            MIDletProxy.MIDLET_NO_EXIT)) {
                        ci.addCommand(endCmd);
                    }
                }
            } else {
                ci.addCommand(fgCmd);
                ci.addCommand(endCmd);
            }
        } else {
            ci.removeCommand(fgCmd);
            ci.removeCommand(endCmd);
        }
    }

    /**
     * This function encapsulates the logic of choosing the default command
     * (open or launch, whatever it means), depending on the midlet type
     * and enabled state.
     * @param mci the form item whose set of menu commands will be modified 
     */
    private void setupDefaultCommand(AppManagerMIDletCustomItem mci) {
        RunningMIDletSuiteInfo si = mci.msi;
        boolean running = si.hasRunningMidlet();

        // setDefaultCommand will add default command first
        if (si.isInternal()) {
            // midlets from the internal suite are never disabled
            if (!running) {
                if (AppManagerPeer.DISCOVERY_APP.equals(mci.msi.midletToRun)) {
                    mci.setDefaultCommand(launchInstallCmd);
                } else if (appManager.caManagerIncluded() &&
                           AppManagerPeer.CA_MANAGER.equals(mci.msi.midletToRun)) {
                    mci.setDefaultCommand(launchCaManagerCmd);
                } else if (appManager.compManagerIncluded() &&
                           AppManagerPeer.COMP_MANAGER.equals(mci.msi.midletToRun)) {
                    mci.setDefaultCommand(launchCompManagerCmd);
                } else if (appManager.oddEnabled() &&
                           AppManagerPeer.ODT_AGENT.equals(mci.msi.midletToRun)) {
                    mci.setDefaultCommand(launchODTAgentCmd);
                } else {
                    // This should never happen: all possible kinds of
                    // internal applications must be listed above
                    mci.setDefaultCommand(infoCmd);
                }
            } else {
                mci.setDefaultCommand(fgCmd);
                mci.removeCommand(launchInstallCmd);
                if (appManager.caManagerIncluded()) {
                    mci.removeCommand(launchCaManagerCmd);
                }
                if (appManager.compManagerIncluded()) {
                    mci.removeCommand(launchCompManagerCmd);
                }
                if (appManager.oddEnabled()) {
                    mci.removeCommand(launchODTAgentCmd);
                }
            }
        } else { // not internal suite
            // running MIDlets will continue to run
            // even when disabled
            if (si.enabled || running) {
                if (running && si.hasSingleMidlet()) {
                    mci.setDefaultCommand(fgCmd);
                } else {
                    mci.setDefaultCommand(openCmd);
                }
            } else {
                mci.setDefaultCommand(infoCmd);
                mci.removeCommand(openCmd);
            }
        }
    }

    /**
     * Called when state of a running midlet has changed.
     *
     * @param si corresponding midlet suite info
     * @param midlet specifies which midlet has changed its state
     */
    public void notifyMidletStateChanged(RunningMIDletSuiteInfo si, MIDletProxy midlet) {
        AppManagerMIDletCustomItem mci;
        for (int i = 0; i < mciVector.size(); i++) {
            mci = (AppManagerMIDletCustomItem)mciVector.elementAt(i);
            if (mci.msi == si) {
                mci.update();
            }
        }
    }

    /**
     * Called when a running internal midlet exited.
     * @param midlet proxy of the midlet that has exited
     */
    public void notifyInternalMidletExited(MIDletProxy midlet) {
        // nothing to do
    }

    /**
     * Called when a running midlet exited.
     * @param si corresponding midlet suite info
     * @param midletClassName Class name of the exited midlet
     */
    public void notifyMidletExited(RunningMIDletSuiteInfo si, String midletClassName) {
        AppManagerMIDletCustomItem mci = findItem(si);
        if (mci == null) {
            // Midlet quitted; display the application Selector
            display.setCurrent(this);
        } else {
            // we get here when mci.msi.proxy already is null

            setupDefaultCommand(mci);
            setupRunStateDependentCommands(mci);

            midletSwitcher.remove(mci.msi, midletClassName);
            mci.update();

            /* find appropriate MIDlet selector */
            MIDletSelector selector = getMidletSelector(si.suiteId);
            if (selector != null) {

                /* notify the selector that MIDlet was exited */
                selector.notifyMidletExited(midletClassName);

                if (exitingMidletSuiteId == si.suiteId &&
                        exitingMidletClassName.equals(midletClassName)) {
                    exitingMidletSuiteId = 0;
                    exitingMidletClassName = null;
                    selector.exitIfNoMidletRuns();
                }
            }
        }
    }

    /**
     * Removes MIDlet selector from the list of active selectors.
     * @param suiteInfo suite whose selector should be removed
     */
    private void removeMIDletSelector(RunningMIDletSuiteInfo suiteInfo) {
        MIDletSelector selector = getMidletSelector(suiteInfo.suiteId);
        if (selector != null) {
            midletSelectors.removeElement(selector);
        }
    }
    
    /**
     * Called when a suite exited (the only MIDlet in suite exited or the
     * MIDlet selector exited).
     * Removes appropriate MIDlet selector from list of active selectors.
     * @param suiteInfo Suite which just exited
     */
    public void notifySuiteExited(RunningMIDletSuiteInfo suiteInfo) {
        if (!suiteInfo.isLocked()) {
            removeMIDletSelector(suiteInfo);
        }
    }

    /**
     * Called when MIDlet selector exited.
     * Removes appropriate MIDlet selector from list of active selectors.
     * @param suiteInfo Containing ID of suite
     */
    public void notifyMIDletSelectorExited(RunningMIDletSuiteInfo suiteInfo) {
        if (!suiteInfo.isLocked()) {
            removeMIDletSelector(suiteInfo);
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
        Alert a;
        String errorMsg;

        switch (errorCode) {
        case Constants.MIDLET_SUITE_NOT_FOUND:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_MIDLETSUITE_NOTFOUND);
            break;

        case Constants.MIDLET_CLASS_NOT_FOUND:
            errorMsg = Resource.getString(
              ResourceConstants.AMS_MIDLETSUITELDR_CANT_LAUNCH_MISSING_CLASS);
            break;

        case Constants.MIDLET_INSTANTIATION_EXCEPTION:
            errorMsg = Resource.getString(
              ResourceConstants.AMS_MIDLETSUITELDR_CANT_LAUNCH_ILL_OPERATION);
            break;

        case Constants.MIDLET_ILLEGAL_ACCESS_EXCEPTION:
            errorMsg = Resource.getString(
              ResourceConstants.AMS_MIDLETSUITELDR_CANT_LAUNCH_ILL_OPERATION);
            break;

        case Constants.MIDLET_OUT_OF_MEM_ERROR:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_QUIT_OUT_OF_MEMORY);
            break;

        case Constants.MIDLET_RESOURCE_LIMIT:
        case Constants.MIDLET_ISOLATE_RESOURCE_LIMIT:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_RESOURCE_LIMIT_ERROR);
            break;

        case Constants.MIDLET_ISOLATE_CONSTRUCTOR_FAILED:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_CANT_EXE_NEXT_MIDLET);
            break;

        case Constants.MIDLET_SUITE_DISABLED:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_MIDLETSUITE_DISABLED);
            break;

        case Constants.MIDLET_INSTALLER_RUNNING:
            String[] values = new String[1];
            values[0] = className;
            errorMsg = Resource.getString(
                           ResourceConstants.AMS_MGR_UPDATE_IS_RUNNING,
                           values);
            break;

        default:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_UNEXPECTEDLY_QUIT);
        }

        if (errorDetails != null) {
             errorMsg += "\n\n" + errorDetails;
        }

        displayError.showErrorAlert(null, null,
                                    Resource.getString
                                    (ResourceConstants.EXCEPTION),
                                    errorMsg);
    }
    
    /**
     * Called by ApplicationManager after a MIDlet suite
     * is successfully installed on the device,
     * to ask the user whether or not to launch
     * a MIDlet from the suite.
     * @param si corresponding suite info
     */
    public void notifySuiteInstalled(RunningMIDletSuiteInfo si) {
        if (Constants.EXTENDED_MIDLET_ATTRIBUTES_ENABLED) {
            boolean userMidletExists = true;
            boolean sysFgMidletExists = false;

            MIDletSuite suite = MIDletSuiteUtils.getSuite(si.suiteId);
            if (suite != null) {
                userMidletExists = false;
                int midletsNum = si.numberOfMidlets;
                try {
                    for (int m = 1; m <= midletsNum; m++) {
                        String pwrProp = MIDletSuiteUtils.getSuiteProperty(
                            suite, m, MIDletSuite.LAUNCH_POWER_ON_PROP);

                        if ("yes".equalsIgnoreCase(pwrProp)) {
                            String bgProp = MIDletSuiteUtils.getSuiteProperty(
                                suite, m, MIDletSuite.LAUNCH_BG_PROP);

                            if (!"yes".equalsIgnoreCase(bgProp)) {
                                sysFgMidletExists = true;
                                break;
                            }
                        } else {
                            userMidletExists = true;
                        }
                    }
                } finally {
                    suite.close();
                }
            }

            if (userMidletExists && !sysFgMidletExists) {
                askUserIfLaunchMidlet();
            }
        } else {
            askUserIfLaunchMidlet();
        }
    }

    /**
     * Called when a new MIDlet suite is installed externally.
     * @param si corresponding suite info
     */
    public void notifySuiteInstalledExt(RunningMIDletSuiteInfo si) {
        int size = mciVector.size();
        for (int i = 0; i < size; i++) {
            AppManagerMIDletCustomItem mci =
                (AppManagerMIDletCustomItem)mciVector.elementAt(i);
            if (mci.msi == si) {
                display.setCurrentItem(mci);
                break;
            }
        }        
        
    }

    /**
     * Called when a MIDlet suite has been removed externally.
     * @param si corresponding suite info
     */
    public void notifySuiteRemovedExt(RunningMIDletSuiteInfo si) {
        // do nothing
    }

    /**
     * The AppManagerPeer manages list of available MIDlet suites
     * and informs AppManagerUI regarding changes in list through
     * itemRemoved callback when item is removed from the list.
     *
     * @param suiteInfo the midlet suite info
     */
    public void itemRemoved(RunningMIDletSuiteInfo suiteInfo) {
        AppManagerMIDletCustomItem mci = null;
        int i = 0;
        for (; i < mciVector.size(); i++) {
            mci = (AppManagerMIDletCustomItem)mciVector.elementAt(i);
            if (mci.msi == suiteInfo) {
                break;
            }
        }

        RunningMIDletSuiteInfo msi = mci.msi;
        if (msi == suiteInfo) {
            mciVector.removeElement(mci);
            if (foldersOn) {
                if (mci.msi.folderId == currentFolderId) {
                    for (int j = 0; j < size(); j++) {
                        if (get(j).equals(mci)) {
                            delete(j);
                            break;
                        }
                    }
                }
            } else {
                delete(i);
            }
        }
    }

    /**
     * Called when MIDlet suite being enabled
     * @param msi corresponding suite info
     */
    public void notifyMIDletSuiteEnabled(RunningMIDletSuiteInfo msi) {
        setupDefaultCommand(findItem(msi));
    }


    /**
     * Called when state of the midlet changes.
     *
     * @param si corresponding suite info
     * @param newSi new suite info
     */
    public void notifyMIDletSuiteStateChanged(RunningMIDletSuiteInfo si,
                                             RunningMIDletSuiteInfo newSi) {
        midletSwitcher.update(si, newSi);
    }

    /**
     * Called when MIDlet suite icon hase changed
     * @param si corresponding suite info
     */
    public void notifyMIDletSuiteIconChaged(RunningMIDletSuiteInfo si) {
        AppManagerMIDletCustomItem mci = findItem(si);
        mci.icon = si.icon;
    }

    /**
     * Finds MidletCustomItem for specified midlet
     * @param si corresponding suite info
     * @return the custom item that points to the suite info, or null if not found
     */
    private AppManagerMIDletCustomItem findItem(RunningMIDletSuiteInfo si) {
        for (int i = 0; i < mciVector.size(); i++) {
            AppManagerMIDletCustomItem mci =
                (AppManagerMIDletCustomItem)mciVector.elementAt(i);
            if (mci.msi == si) {
                return mci;
            }
        }
        return null;
    }
    
    /**
     * Gets MIDlet selector for the given suite Id
     * @return MIDlet selector or null if it is not running
     */
    private MIDletSelector getMidletSelector(int suiteId) {
        int size = midletSelectors.size();
        for (int i = 0; i < size; i++) {
            MIDletSelector selector = 
                    (MIDletSelector) midletSelectors.elementAt(i);
            if (selector.getSuiteInfo().suiteId == suiteId) {
                return selector;
            }
        }
        return null;
    }

    /**
     * The AppManagerPeer manages list of available MIDlet suites
     * and informs AppManagerUI regarding changes in list through
     * itemAppended callback when new item is appended to the list.
     *
     * The order in which the MIDlets are shown is up to the UI
     * and need not be the order of itemAppended invocations.
     *
     * @param suiteInfo the midlet suite info
     */
    public void itemAppended(RunningMIDletSuiteInfo suiteInfo) {
        AppManagerMIDletCustomItem ci =
            new AppManagerMIDletCustomItem(suiteInfo);

        setupDefaultCommand(ci);
        if (!suiteInfo.isInternal()) {
            ci.addCommand(infoCmd);
            ci.addCommand(removeCmd);
            ci.addCommand(updateCmd);
            ci.addCommand(appSettingsCmd);
            if (suiteInfo.storageId != Constants.INTERNAL_STORAGE_ID) {
                ci.addCommand(moveToInternalStorageCmd);
            }
            if (foldersOn) {
                ci.addCommand(changeFolderCmd);
            }
            if (appManager.compManagerIncluded()) {
                ci.addCommand(viewCompCmd);
            }
        }

        ci.setItemCommandListener(this);
        ci.setOwner(this);

        mciVector.addElement(ci);

        if (foldersOn) {
            /* check if suiteInfo corresponds to current folder */
            if (currentFolderId == suiteInfo.folderId) {
                append(ci);
            }
        }  else {
            append(ci);
        }
    }

    /**
     * Alert the user that an action was successful.
     * @param successMessage message to display to user
     */
    private void displaySuccessMessage(String successMessage) {
        Image icon;
        Alert successAlert;

        icon = GraphicalInstaller.getImageFromInternalStorage("_dukeok8");

        successAlert = new Alert(null, successMessage, icon, null);

        successAlert.setTimeout(GraphicalInstaller.ALERT_TIMEOUT);

        // We need to prevent "flashing" on fast development platforms.
        while (System.currentTimeMillis() - lastDisplayChange <
               GraphicalInstaller.ALERT_TIMEOUT);

        display.setCurrent(successAlert, this);
        lastDisplayChange = System.currentTimeMillis();
    }

    /**
     * Confirm the removal of a suite.
     *
     * @param suiteInfo information for suite to remove
     */
    private void showConfirmRemoveDialog(RunningMIDletSuiteInfo suiteInfo) {
        Form confirmForm;
        StringBuffer temp = new StringBuffer(40);
        Item item;
        String extraConfirmMsg;
        String[] values = new String[1];
        MIDletSuiteImpl midletSuite = null;

        try {
            midletSuite = midletSuiteStorage.getMIDletSuite(suiteInfo.suiteId,
                                                            false);
            confirmForm = new Form(null);

            confirmForm.setTitle(Resource.getString
                                 (ResourceConstants.AMS_CONFIRMATION));

            if (suiteInfo.hasSingleMidlet()) {
                values[0] = suiteInfo.displayName;
            } else {
                values[0] =
                    midletSuite.getProperty(MIDletSuiteImpl.SUITE_NAME_PROP);
            }

            item = new StringItem(null, Resource.getString(
                       ResourceConstants.AMS_MGR_REMOVE_QUE,
                       values));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            confirmForm.append(item);

            extraConfirmMsg =
                PAPICleanUp.checkMissedTransactions(suiteInfo.suiteId);
            if (extraConfirmMsg != null) {
                temp.setLength(0);
                temp.append(" \n");
                temp.append(extraConfirmMsg);
                item = new StringItem(null, temp.toString());
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
            }

            extraConfirmMsg = midletSuite.getProperty("MIDlet-Delete-Confirm");
            if (extraConfirmMsg != null) {
                temp.setLength(0);
                temp.append(" \n");
                temp.append(extraConfirmMsg);
                item = new StringItem(null, temp.toString());
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
            }

            if (!suiteInfo.hasSingleMidlet()) {
                temp.setLength(0);
                temp.append(Resource.getString
                            (ResourceConstants.AMS_MGR_SUITE_CONTAINS));
                temp.append(": ");
                item = new StringItem(temp.toString(), "");
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
                appendMIDletsToForm(midletSuite, confirmForm);
            }

            String[] recordStores =
                midletSuiteStorage.listRecordStores(suiteInfo.suiteId);
            if (recordStores != null) {
                temp.setLength(0);
                temp.append(Resource.getString
                            (ResourceConstants.AMS_MGR_SUITE_RECORD_STORES));
                temp.append(": ");
                item = new StringItem(temp.toString(), "");
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
                appendRecordStoresToForm(recordStores, confirmForm);
            }

            // IMPL_NOTE: uncomment this code once 
            // FileConnectionCleanupImpl.suiteHasPrivateData has sensible
            // (not stubbed) implementation.
            /*
            boolean privateDataExists =
                Installer.FileConnectionHasPrivateData(suiteInfo.suiteId);
            if (privateDataExists) {
                temp.setLength(0);
                temp.append(" \n");
                temp.append(Resource.getString
                            (ResourceConstants.AMS_MGR_SUITE_PRIVATE_DATA));
                item = new StringItem(null, temp.toString());
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
            }
            */

            temp.setLength(0);
            temp.append(" \n");
            temp.append(Resource.getString
                        (ResourceConstants.AMS_MGR_REM_REINSTALL, values));
            item = new StringItem("", temp.toString());
            confirmForm.append(item);
        } catch (Throwable t) {
            displayError.showErrorAlert(suiteInfo.displayName, t,
                                   Resource.getString
                                   (ResourceConstants.AMS_CANT_ACCESS),
                                   null);
            return;
        } finally {
            if (midletSuite != null) {
                midletSuite.close();
            }
        }

        confirmForm.addCommand(cancelCmd);
        confirmForm.addCommand(removeOkCmd);
        confirmForm.setCommandListener(this);
        removeMsi = suiteInfo;
        display.setCurrent(confirmForm);
    }

    /**
     * Appends a names of all the MIDlets in a suite to a Form, one per line.
     *
     * @param midletSuite information of a suite of MIDlets
     * @param form form to append to
     */
    private void appendMIDletsToForm(MIDletSuiteImpl midletSuite, Form form) {
        StringItem item;

        String names[] = appManager.getMIDletsNames(midletSuite);
        for (int i = 0; i < names.length; i++) {
            item = new StringItem(null, names[i]);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            form.append(item);
        }
    }

    /**
     * Appends names of the record stores owned by the midlet suite
     * to a Form, one per line.
     *
     * @param recordStores list of the record store names
     * @param form form to append to
     */
    private void appendRecordStoresToForm(String[] recordStores, Form form) {
        StringItem item;

        for (int i = 0; i < recordStores.length; i++) {
            item = new StringItem(null, recordStores[i]);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            form.append(item);
        }
    }

    /**
     * Prompts the user to specify whether to launch a midlet from
     * the midlet suite that was just installed.
     */
    private void askUserIfLaunchMidlet() {
        // Ask the user if he wants to run a midlet from
        // the newly installed midlet suite
        String title = Resource.getString(
            ResourceConstants.AMS_MGR_RUN_THE_NEW_SUITE_TITLE, null);
        String msg = Resource.getString(
            ResourceConstants.AMS_MGR_RUN_THE_NEW_SUITE, null);

        Alert alert = new Alert(title, msg, null, AlertType.CONFIRMATION);
        alert.addCommand(runNoCmd);
        alert.addCommand(runYesCmd);
        alert.setCommandListener(this);
        alert.setTimeout(Alert.FOREVER);

        display.setCurrent(alert);
    }

    /**
     * Returns the main displayable of AppManagerUI.
     * @return main screen
     */
    public Displayable getMainDisplayable() {
        return this;
    }

    /** Called by Manager when destroyApp happens to clean up data */
    public void cleanUp() {
        AppManagerMIDletCustomItem ci =
            findItem(lastSelectedMsi);
        ci.cleanUp();
    }

    /**
     * The inner class to represent MIDlet and MIDlet suite items
     * in the Application Selector screen
     */
    class AppManagerMIDletCustomItem extends MIDletCustomItem {

        /** The MIDletSuiteInfo associated with the item */
        RunningMIDletSuiteInfo msi;

        /**
         * Constructs a midlet representation for the App Selector Screen.
         * @param msi The MIDletSuiteInfo for which representation has to be created
         */
        AppManagerMIDletCustomItem(RunningMIDletSuiteInfo msi) {
            super(msi.displayName, msi.icon);
            this.msi = msi;
        }

        /**
         * Returns running state of the MIDlet or MIDlte suite.
         * @return true is MIDlet is running or associated MIDlet suite has running
         *   MIDlets, false otherwise
         */
        boolean isRunning() {
            return msi.hasRunningMidlet();
        }

        /**
         * Returns locked state of the MIDlet or MIDlet suite.
         * Locked states limits operations available for this item.
         * @return true if MIDlet or MIDlet suite is locked, false otherwise
         */
        boolean isLocked() {
            return msi.isLocked();
        }

        /**
         * True if alert is waiting for the foreground in at
         * least of one of the MIDlets from this suite.
         * @return true if there is a waiting alert
         */
        boolean isAnyAlertWaiting() {
            return msi.isAnyAlertWaiting();
        }

        /**
         * True if this MIDlet or MIDlet suite item is enabled
         * @return true if the item is enabled
         */
        boolean isEnabled() {
            return msi.enabled;
        }

        /** Called when MidletCustomItem is shown */
        public void showNotify() {
            // Unfortunately there is no Form.showNotify  method where this could
            // have been done.
            //
            // When icon for the Installer is shown we want to make sure that there
            // are no running midlets from the "internal" suite. The only 2 midlets
            // that can run in bg from "internal" suite are the DiscoveryApp and
            // the Installer. Icon for the Installer will be shown each time the
            // AppSelector is made current since it is the top most icon and we
            // reset the traversal to start from the top
            if (msi.isInternal()) {
                appManager.ensureNoInternalMIDletsRunning();
            }
        }

        /**
         * Overrides #MIDletCustomItem.traverse() to remember
         * last selected item of the form 
         */
        protected boolean traverse(
                int dir, int viewportWidth, int viewportHeight, int visRect_inout[]) {
            lastSelectedMsi = this.msi;
            return super.traverse(
                dir, viewportWidth, viewportHeight, visRect_inout);
        }
    }
}
