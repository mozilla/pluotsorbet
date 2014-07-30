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

package com.sun.midp.installer;

import java.io.*;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

import com.sun.j2me.security.AccessController;
import com.sun.midp.security.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.configurator.Constants;

import com.sun.midp.main.TrustedMIDletIcon;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.midletsuite.*;

import com.sun.midp.content.CHManager;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.midp.util.ResourceHandler;

import com.sun.midp.events.Event;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventListener;
import com.sun.midp.ams.VMUtils;

/**
 * The Graphical MIDlet suite installer.
 * <p>
 * The graphical installer is implements the installer requirements of the
 * MIDP OTA specification.</p>
 * <p>
 * If the Content Handler API (CHAPI) is present the GraphicalInstaller will
 * dequeue a single Invocation and install from the URL contained
 * in the request. If there is no Invocation present then the arguments below
 * will be used.
 * <p>
 * The MIDlet uses certain application properties as arguments: </p>
 * <ol>
 *   <li>arg-0: "U" for update, "I" for install, "FI" for forced install,
 *              "FU" for forced update, "PR" for install or update
 *              initiated by platformRequest</li>
 *   <li>arg-1: Suite ID for updating, URL for installing
 *   <li>arg-2: For installing a name to put in the title bar when installing
 * </ol>
 * @see CHManager
 */
public class GraphicalInstaller extends MIDlet implements CommandListener {

    /** Standard timeout for alerts. */
    public static final int ALERT_TIMEOUT = 1250;
     /** settings database */
    public static final String SETTINGS_STORE = "settings";
    /** record id of selected midlet */
    public static final int URL_RECORD_ID = 1;
    /** record id of filepath in external storage */
    public static final int FILE_PATH_RECORD_ID = 3;
    /** record is of the last installed midlet */
    public static final int SELECTED_MIDLET_RECORD_ID = 2;
    /** type of last installation: from web or storage source */
    public static final int LAST_INSTALLATION_SOURCE_RECORD_ID = 4;
   
    /** The installer that is being used to install or update a suite. */
    private Installer installer;
    /** Display for this MIDlet. */
    private Display display;
    /** Form obtain a password and a username. */
    private Form passwordForm;
    /** Contains the username for installing. */
    private TextField usernameField;
    /** Contains the password for installing. */
    private TextField passwordField;
    /** Background installer that holds state for the current install. */
    private BackgroundInstaller backgroundInstaller;
    /** Displays the progress of the install. */
    private Form progressForm;
    /** Gauge for progress form index. */
    private int progressGaugeIndex;
    /** URL for progress form index. */
    private int progressUrlIndex;
    /** Keeps track of when the display last changed, in milliseconds. */
    private long lastDisplayChange;
    /** What to display to the user when the current action is cancelled. */
    private String cancelledMessage;
    /** What to display to the user when the current action is finishing. */
    private String finishingMessage;
    /** Displays a list of storages to install to. */
    private List storageListBox;    
    /** ID of the storage where the new midlet suite will be installed. */
    private int storageId = Constants.INTERNAL_STORAGE_ID;

    /** Content handler specific install functions. */
    CHManager chmanager;

    /** Command object for "Stop" command for progress form. */
    private Command stopCmd = new Command(Resource.getString
                                          (ResourceConstants.STOP),
                                          Command.STOP, 1);

    /** Command object for "Cancel" command for the confirm form. */
    private Command cancelCmd =
        new Command(Resource.getString(ResourceConstants.CANCEL),
                    Command.CANCEL, 1);
    /** Command object for "Install" command for the confirm download form. */
    private Command continueCmd =
        new Command(Resource.getString(ResourceConstants.INSTALL),
                    Command.OK, 1);
    /** Command object for "Next" command for storage select list. */
    private Command storeSelectCmd =
            new Command(Resource.getString(ResourceConstants.NEXT),
                        Command.OK, 1);
    /** Command object for "Next" command for password form. */
    private Command nextCmd =
        new Command(Resource.getString(ResourceConstants.NEXT),
                    Command.OK, 1);
    /** Command object for "continue" command for warning form. */
    private Command okCmd =
        new Command(Resource.getString(ResourceConstants.CONTINUE),
                    Command.OK, 1);
    /** Command object for "Yes" command for keep RMS form. */
    private Command keepRMSCmd =
        new Command(Resource.getString(ResourceConstants.YES),
                    Command.OK, 1);
    /** Command object for "No" command for keep RMS form. */
    private Command removeRMSCmd =
        new Command(Resource.getString(ResourceConstants.NO),
                    Command.CANCEL, 1);

    /** Suite name */
    private String label;
    /** Url to install from */
    private String url;
    /** true if update should be forced without user confirmation */
    private boolean forceUpdate = false;
    /** true if user confirmation should be presented */
    private boolean noConfirmation = false;
    /** true if runnning midlets from the suite being updated must be killed */
    private boolean killRunningMIDletIfUpdate = false;
            
    /**
     * Gets an image from the internal storage.
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * IMPL_NOTE: this method should be moved somewhere.
     *
     * @param imageName image file name without a path and extension
     * @return Image loaded from storage, or null if not found
     */
    public static Image getImageFromInternalStorage(String imageName) {
        byte[] imageBytes =
                ResourceHandler.getSystemImageResource(null, imageName);

        if (imageBytes != null) {
            return Image.createImage(imageBytes, 0, imageBytes.length);
        }

        return null;
    }

    /**
     * Translate an InvalidJadException into a message for the user.
     *
     * @param exception exception to translate
     * @param name name of the MIDlet suite to insert into the message
     * @param vendor vendor of the MIDlet suite to insert into the message,
     *        can be null
     * @param version version of the MIDlet suite to insert into the message,
     *        can be null
     * @param jadUrl URL of a JAD, can be null
     *
     * @return message to display to the user
     */
    private static String translateJadException(
            InvalidJadException exception, String name, String vendor,
            String version, String jadUrl) {
        String[] values = {name, vendor, version, jadUrl,
                           exception.getExtraData()};
        int key;

        switch (exception.getReason()) {
        case InvalidJadException.OLD_VERSION:
            key =ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_OLD_VERSION;
            break;

        case InvalidJadException.ALREADY_INSTALLED:
            key =
                ResourceConstants.
                  AMS_GRA_INTLR_INVALIDJADEXCEPTION_ALREADY_INSTALLED;
            break;

        case InvalidJadException.NEW_VERSION:
            key = ResourceConstants.
                      AMS_GRA_INTLR_INVALIDJADEXCEPTION_NEW_VERSION;
            break;

        case InvalidJadException.JAD_SERVER_NOT_FOUND:
        case InvalidJadException.JAD_NOT_FOUND:
        case InvalidJadException.INVALID_JAD_URL:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_INVALID_JAD_URL;
            break;

        case InvalidJadException.INVALID_JAD_TYPE:
            key = ResourceConstants.
                 AMS_GRA_INTLR_INVALIDJADEXCEPTION_INVALID_JAD_TYPE;
            break;

        case InvalidJadException.MISSING_PROVIDER_CERT:
        case InvalidJadException.MISSING_SUITE_NAME:
        case InvalidJadException.MISSING_VENDOR:
        case InvalidJadException.MISSING_VERSION:
        case InvalidJadException.MISSING_JAR_URL:
        case InvalidJadException.MISSING_JAR_SIZE:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_MISSING_JAD_INFO;
            break;

        case InvalidJadException.MISSING_CONFIGURATION:
        case InvalidJadException.MISSING_PROFILE:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_MISSING_JAR_INFO;
            break;

        case InvalidJadException.INVALID_KEY:
        case InvalidJadException.INVALID_VALUE:
        case InvalidJadException.INVALID_VERSION:
        case InvalidJadException.PUSH_FORMAT_FAILURE:
        case InvalidJadException.PUSH_CLASS_FAILURE:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_INVALID_FORMAT;
            break;

        case InvalidJadException.DEVICE_INCOMPATIBLE:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_DEVICE_INCOMPATIBLE;
            break;

        case InvalidJadException.JAD_MOVED:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_JAD_MOVED;
            break;

        case InvalidJadException.INSUFFICIENT_STORAGE:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_INSUFFICIENT_STORAGE;
            break;

        case InvalidJadException.JAR_SERVER_NOT_FOUND:
        case InvalidJadException.JAR_NOT_FOUND:
        case InvalidJadException.INVALID_JAR_URL:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_JAR_NOT_FOUND;
            break;

        case InvalidJadException.INVALID_JAR_TYPE:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_INVALID_JAR_TYPE;
            break;

        case InvalidJadException.SUITE_NAME_MISMATCH:
        case InvalidJadException.VERSION_MISMATCH:
        case InvalidJadException.VENDOR_MISMATCH:
        case InvalidJadException.JAR_SIZE_MISMATCH:
        case InvalidJadException.ATTRIBUTE_MISMATCH:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_ATTRIBUTE_MISMATCH;
            break;

        case InvalidJadException.CORRUPT_JAR:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_CORRUPT_JAR;
            break;

        case InvalidJadException.CANNOT_AUTH:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_CANNOT_AUTH;
            break;

        case InvalidJadException.CORRUPT_PROVIDER_CERT:
        case InvalidJadException.INVALID_PROVIDER_CERT:
        case InvalidJadException.CORRUPT_SIGNATURE:
        case InvalidJadException.INVALID_SIGNATURE:
        case InvalidJadException.UNSUPPORTED_CERT:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_INVALID_SIGNATURE;
            break;

        case InvalidJadException.UNKNOWN_CA:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_UNKNOWN_CA;
            break;

        case InvalidJadException.EXPIRED_PROVIDER_CERT:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_EXPIRED_PROVIDER_CERT;
            break;

        case InvalidJadException.EXPIRED_CA_KEY:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_EXPIRED_CA_KEY;
            break;

        case InvalidJadException.AUTHORIZATION_FAILURE:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_AUTHORIZATION_FAILURE;
            break;

        case InvalidJadException.CA_DISABLED:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_CA_DISABLED;
            break;

        case InvalidJadException.PUSH_DUP_FAILURE:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_PUSH_DUP_FAILURE;
            break;

        case InvalidJadException.PUSH_PROTO_FAILURE:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_PUSH_PROTO_FAILURE;
            break;

        case InvalidJadException.TRUSTED_OVERWRITE_FAILURE:
            if (exception.getExtraData() != null) {
                key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_TRUSTED_OVERWRITE_FAILURE;
            } else {
                key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_TRUSTED_OVERWRITE_FAILURE_2;
            }

            break;

        case InvalidJadException.TOO_MANY_PROPS:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_APP_TOO_BIG;
            break;

        case InvalidJadException.INVALID_CONTENT_HANDLER:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_INVALID_CONTENT_HANDLER;
            break;

        case InvalidJadException.CONTENT_HANDLER_CONFLICT:
            key = ResourceConstants.
            AMS_GRA_INTLR_INVALIDJADEXCEPTION_CONTENT_HANDLER_CONFLICT;
            break;

        case InvalidJadException.JAR_CLASSES_VERIFICATION_FAILED:
            // This constant is shared between graphical installer
            // and standalone class verifier MIDlet used for SVM mode
            key = ResourceConstants.AMS_CLASS_VERIFIER_FAILURE;
            break;

        case InvalidJadException.UNSUPPORTED_CHAR_ENCODING:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_UNSUPPORTED_CHAR_ENCODING;
            break;

        case InvalidJadException.REVOKED_CERT:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_REVOKED_PROVIDER_CERT;
            break;

        case InvalidJadException.UNKNOWN_CERT_STATUS:
            key = ResourceConstants.
                AMS_GRA_INTLR_INVALIDJADEXCEPTION_UNKNOWN_PROVIDER_CERT_STATUS;
            break;

        default:
            return exception.getMessage();
        }

        return Resource.getString(key, values);
    }

    /**
     * Create and initialize a new graphical installer MIDlet.
     * <p>
     * If a ContentHandler request to install a suite is found,
     * then that URL will be installed.  In this case the command
     * arguments are ignored.
     * <p>
     * The Display is retrieved and the list of MIDlet will be retrieved or
     * update a currently installed suite.
     */
    public GraphicalInstaller() {
        
        String arg0;
           
        display = Display.getDisplay(this);
        GraphicalInstaller.initSettings();
        
        // Establish Content handler installer context
        chmanager = CHManager.getManager(null);
        
         // Get the URL, if any, provided from the invocation mechanism.
        url = chmanager.getInstallURL(this);
        
        if (url != null) {
            label = Resource.getString(ResourceConstants.APPLICATION);
            forceUpdate = false;
            noConfirmation = false;
        } else {
            arg0 = getAppProperty("arg-0");
            if (arg0 == null) {
                // goto back to the discovery midlet
                exit(false);
                return;
            }
              
           if ("U".equals(arg0)) {
                String strSuiteID = getAppProperty("arg-1");
                int suiteId = MIDletSuite.UNUSED_SUITE_ID;

                if (strSuiteID != null) {
                    try {
                        suiteId = Integer.parseInt(strSuiteID);
                    } catch (NumberFormatException nfe) {
                        // Intentionally ignored
                    }
                }

                if (suiteId == MIDletSuite.UNUSED_SUITE_ID) {
                    // goto back to the discovery midlet
                    exit(false);
                    return;
                }

                // IMPL_NOTE: "installer" instance is not created yet but
                // it will be in the updateSuite method itself. 
                updateSuite(suiteId);
                return;
            } else if("FI".equals(arg0)) {
                // force installation without user confirmation
                noConfirmation = true;
                // force installation without user confirmation and force update
                forceUpdate = false;
            } else if("FU".equals(arg0)) {
                // force installation without user confirmation */
                noConfirmation = true;
                // force installation without user confirmation and force update
                forceUpdate = true;
            } else if("PR".equals(arg0)) {
                /*
                 * This URL was dispatched by a Platform Request, so if an
                 * update is requested, we have to kill any running midlet
                 * from the suite that is going to be updated.
                 */
                killRunningMIDletIfUpdate = true;
            }

            url = getAppProperty("arg-1");
            if (url == null) {
                // goto back to the discovery midlet
                exit(false);
                return;
            }
                       
            label = getAppProperty("arg-2");
            if (label == null || label.length() == 0) {
                label = Resource.getString(ResourceConstants.APPLICATION);
            }
        }
        
        installer = InstallerResource.getInstaller(url);
        
        cancelledMessage =
            Resource.getString(ResourceConstants.AMS_GRA_INTLR_INST_CAN);

        storageListBox = new List(Resource.getString(
            ResourceConstants.AMS_GRA_INTLR_SELECT_STORAGE), Choice.IMPLICIT);
        storageListBox.append(Resource.getString(
                ResourceConstants.AMS_INTERNAL_STORAGE_NAME), null);

        String storagePrefix = Resource.getString(
            ResourceConstants.AMS_EXTRENAL_STORAGE_NAME);

        int validStorageCnt = Constants.MAX_STORAGE_NUM;
        for (int i = 1; i < Constants.MAX_STORAGE_NUM; i++) {
            /*
             * IMPL_NOTE: here we should check if storage is accessible and
             * update validStorageCnt accordingly
             */
            storageListBox.append(storagePrefix + i, null);
        }

        /*
         * if VERIFY_ONCE is enabled then MONET is disabled
         * so we don't have to check MONET_ENABLED.
         */
        if (Constants.VERIFY_ONCE && (validStorageCnt > 1)) {
            // select storage
            storageListBox.addCommand(cancelCmd);
            storageListBox.addCommand(storeSelectCmd);
            storageListBox.setSelectCommand(storeSelectCmd);
            storageListBox.setCommandListener(this);
            display.setCurrent(storageListBox);
        } else {
            // use default storage
            installSuite(label, url, storageId, forceUpdate, noConfirmation);
        }
    }

    /**
     * Start.
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
        if (installer != null) {
            installer.stopInstalling();
        }

        /* The backgroundInstaller could be waiting for the user. */
        cancelBackgroundInstall();
    }

    /**
     * Exit the GraphicalInstaller with the status supplied.
     * It will perform any remaining cleanup and call notifyDestroyed.
     * @param success <code>true</code> if the install was a success,
     *  <code>false</code> otherwise.
     */
    void exit(boolean success) {
        chmanager.installDone(success);

        notifyDestroyed();
    }

    /**
     * Respond to a command issued on any Screen.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == nextCmd) {
            // the user has entered a username and password
            resumeInstallWithPassword();
        } else if (c == storeSelectCmd) {
            storageId = this.storageListBox.getSelectedIndex();
            installSuite(label, url, storageId, forceUpdate, noConfirmation);
        } else if (c == okCmd) {
            resumeInstallAfterWarning();
        } else if (c == continueCmd) {
            startJarDownload();
        } else if (c == keepRMSCmd) {
            setKeepRMSAnswer(true);
        } else if (c == removeRMSCmd) {
            setKeepRMSAnswer(false);
        } else if (c == stopCmd) {
            if (installer != null) {
                /*
                 * BackgroundInstaller may be displaying
                 * the "Finishing" message
                 *
                 * also we need to prevent the BackgroundInstaller from
                 * re-displaying the list before the cancelled message is
                 * displayed
                 */
                synchronized (this) {
                    if (installer.stopInstalling()) {
                        displayCancelledMessage(cancelledMessage);
                    }
                }
            } else {
                // goto back to the manager midlet
                exit(false);
            }
        } else if (c == cancelCmd) {
            displayCancelledMessage(cancelledMessage);
            cancelBackgroundInstall();
        } else if (c == Alert.DISMISS_COMMAND) {
            // goto back to the manager midlet
            exit(false);
        }
    }

     /**
     * Initialize the settings database if it doesn't exist. This may create
     * two entries. The first will be for the download url, the second will
     * be for storing the storagename of the currently selected midlet
     * <p>
     * Method requires com.sun.midp.ams permission.
     */
    public static void initSettings() {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        try {
            RecordStore settings = RecordStore.
                                   openRecordStore(SETTINGS_STORE, true);

            try {
                if (settings.getNumRecords() == 0) {
                    // space for a URL
                    settings.addRecord(null, 0, 0);
                    
                    // space for current MIDlet Suite name
                    settings.addRecord(null, 0, 0);
                    
                    // space for storage filepath
                    settings.addRecord(null, 0, 0);
                    
                    // space for last installation type
                    settings.addRecord(null, 0, 0);
                }
            } finally {
                settings.closeRecordStore();
            }

        } catch (Exception e) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                               "initSettings  throw an Exception");
            }
        }
    }

    /**
     * Save the settings the user entered.
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * @param url the url to save
     * @param curMidlet suiteId of the currently selected midlet
     * @return the Exception that may have been thrown, or null
     */
    public static Exception saveSettings(String url, int curMidlet) {
        Exception ret = null;

        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        try {
            ByteArrayOutputStream bas;
            DataOutputStream dos;
            byte[] data;
            RecordStore settings;

            bas = new ByteArrayOutputStream();
            dos = new DataOutputStream(bas);
            settings = RecordStore.openRecordStore(SETTINGS_STORE, false);

            if (url != null) {
                
                if (url.startsWith(InstallerResource.DEFAULT_FILE_SCHEMA)) {
                    
                    url = url.substring(
                        InstallerResource.DEFAULT_FILE_SCHEMA.length(),
                        url.length());
                    
                    dos.writeUTF(url);
                    data = bas.toByteArray();                    
                    settings.setRecord(FILE_PATH_RECORD_ID, data, 0,
                        data.length);
                    // saves last type of install
                    bas.reset();
                    dos.writeInt(InstallerResource.FILE_INSTALL);
                                        
                   } else {
                    dos.writeUTF(url);
                    data = bas.toByteArray();                 
                    settings.setRecord(URL_RECORD_ID, data, 0, data.length);
                    // saves last type of install
                    bas.reset();
                    dos.writeInt(InstallerResource.HTTP_INSTALL);
                }
                // write last type of installation
                // to record storage
                data = bas.toByteArray();                    
                settings.setRecord(LAST_INSTALLATION_SOURCE_RECORD_ID,
                        data, 0, data.length);
            }
                        
            // Save the current midlet even if its id is
            // MIDletSuite.UNUSED_SUITE_ID. Otherwise in SVM mode
            // the last installed midlet will be always highlighted
            // because its id is recorded in this RMS record.
            bas.reset();

            dos.writeInt(curMidlet);
            data = bas.toByteArray();
            settings.setRecord(SELECTED_MIDLET_RECORD_ID,
                               data, 0, data.length);

            settings.closeRecordStore();
            dos.close();            
        } catch (Exception e) {
            ret = e;
        }

        return ret;
    }
               
    /**
     * Update a suite.
     *
     * @param id ID of the suite to update
     */
    private void updateSuite(int id) {
        MIDletSuiteImpl midletSuite = null;
        try {
            // Any runtime error will get caught by the installer
            midletSuite =
                MIDletSuiteStorage.getMIDletSuiteStorage()
                    .getMIDletSuite(id, false);

            String name;
            if (midletSuite.getNumberOfMIDlets() == 1) {
                MIDletInfo midletInfo =
                    new MIDletInfo(midletSuite.getProperty("MIDlet-1"));
                name = midletInfo.name;
            } else {
                name = midletSuite.getProperty(MIDletSuite.SUITE_NAME_PROP);
            }

            cancelledMessage =
                Resource.getString(ResourceConstants.AMS_GRA_INTLR_UPD_CAN);
            finishingMessage =
                Resource.getString(ResourceConstants.AMS_GRA_INTLR_FIN_UPD);

            InstallInfo installInfo = midletSuite.getInstallInfo();
            String url = installInfo.getDownloadUrl();

            // Create an installer instance corresponding to the URL
            installer = InstallerResource.getInstaller(url);

            installSuiteCommon(Resource.getString
                               (ResourceConstants.AMS_GRA_INTLR_UPDATING),
                               name,
                               url,
                               MIDletSuiteStorage.getMidletSuiteStorageId(id),
                               name + Resource.getString
                               (ResourceConstants.AMS_GRA_INTLR_SUCC_UPDATED),
                               true, false);
        } catch (MIDletSuiteLockedException e) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                               "updateSuite threw MIDletSuiteLockedException");
            }
        } catch (MIDletSuiteCorruptedException e) {
            String msg = Resource.getString
                         (ResourceConstants.AMS_MIDLETSUITE_ID_CORRUPT_MSG)
                         + id;
            Alert a = new Alert(Resource.getString(ResourceConstants.ERROR),
                            msg, null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            a.setCommandListener(this);
            display.setCurrent(a, new Form(""));
            // this Form is never displayed
        } finally {
            if (midletSuite != null) {
                midletSuite.close();
            }
        }
    }

    /**
     * Install a suite from URL.
     *
     * @param label label of the URL link
     * @param url HTTP/S URL of the suite to update
     * @param storageId id of the storage
     * @param forceUpdate no user confirmation for update 
     * @param noConfirmation no user confirmation
     */
    private void installSuite(String label, String url, int storageId,
                              boolean forceUpdate, boolean noConfirmation) {
        cancelledMessage =
            Resource.getString(ResourceConstants.AMS_GRA_INTLR_INST_CAN);
        finishingMessage =
            Resource.getString(ResourceConstants.AMS_GRA_INTLR_FIN_INST);
        installSuiteCommon(Resource.getString
                           (ResourceConstants.AMS_GRA_INTLR_INSTALLING),
                           label, url, storageId,
                           label + Resource.getString
                           (ResourceConstants.AMS_GRA_INTLR_SUCC_INSTALLED),
                           forceUpdate, noConfirmation);
    }

    /**
     * Common helper method to install or update a suite.
     *
     * @param action action to put in the form's title
     * @param name name to in the form's title
     * @param url URL of a JAD
     * @param storageId id of the storage
     * @param successMessage message to display to user upon success
     * @param updateFlag if true the current suite is being updated
     * @param noConfirmation no user confirmation
     */
    private void installSuiteCommon(String action, String name, String url,
            int storageId, String successMessage, boolean updateFlag,
            boolean noConfirmation) {
        try {
            createProgressForm(action, name, url, 0,
                InstallerResource.getString(installer,
                    InstallerResource.CONNECTING_GAUGE_LABEL));

            backgroundInstaller = new BackgroundInstaller(this, url, name,
                storageId, successMessage, updateFlag, noConfirmation);

            new Thread(backgroundInstaller).start();
            
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(name);
            sb.append("\n");
            sb.append(Resource.getString(ResourceConstants.ERROR));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString
                             (ResourceConstants.AMS_CANT_ACCESS),
                             sb.toString());
        }
    }

    /**
     * Create and display the progress form to the user with the stop action.
     *
     * @param action action to put in the form's title
     * @param name name to in the form's title
     * @param url URL of a JAD
     * @param size 0 if unknown, else size of object to download in K bytes
     * @param gaugeLabel label for progress gauge
     */
    private void createProgressForm(String action, String name,
                                    String url, int size, String gaugeLabel) {
        Form installForm;

        // display the JAR progress form
        installForm = displayProgressForm(action, name, url, size,
                                            gaugeLabel);
        installForm.addCommand(stopCmd);
        installForm.setCommandListener(this);
    }

    /**
     * Display the connecting form to the user, let call set actions.
     *
     * @param action action to put in the form's title
     * @param name name to in the form's title
     * @param url URL of a JAD
     * @param size 0 if unknown, else size of object to download in K bytes
     * @param gaugeLabel label for progress gauge
     *
     * @return displayed form
     */
    private Form displayProgressForm(String action, String name,
            String url, int size, String gaugeLabel) {
        Gauge progressGauge;
        StringItem urlItem;

        progressForm = new Form(null);

        progressForm.setTitle(action + " " + name);

        if (size <= 0) {
            progressGauge = new Gauge(gaugeLabel,
                                      false, Gauge.INDEFINITE,
                                      Gauge.CONTINUOUS_RUNNING);
        } else {
            progressGauge = new Gauge(gaugeLabel,
                                      false, size, 0);
        }

        progressGaugeIndex = progressForm.append(progressGauge);

        if (url == null) {
            urlItem = new StringItem("", "");
        } else {
            
            urlItem =
               new StringItem(InstallerResource.getString(
                   installer,InstallerResource.TYPE_OF_SOURCE) + ": ", url);
        }

        progressUrlIndex = progressForm.append(urlItem);

        display.setCurrent(progressForm);
        lastDisplayChange = System.currentTimeMillis();

        return progressForm;
    }

    /** Cancel an install (if there is one) waiting for user input. */
    private void cancelBackgroundInstall() {
        if (backgroundInstaller != null) {
            backgroundInstaller.continueInstall = false;

            synchronized (backgroundInstaller) {
                backgroundInstaller.notify();
            }
        }
    }

    /**
     * Update the status form.
     *
     * @param status current status of the install.
     * @param state current state of the install.
     */
    private void updateStatus(int status, InstallState state) {
        if (status == Installer.DOWNLOADING_JAD) {
                        
            updateProgressForm("", 0,
                InstallerResource.getString(installer,
                InstallerResource.LOAD_JAD_GAUGE_LABEL)); 
            
            return;
        }

        if (status == Installer.DOWNLOADING_JAR) {
            
            updateProgressForm(state.getJarUrl(), state.getJarSize(),
                InstallerResource.getString(
                installer,InstallerResource.LOAD_JAR_GAUGE_LABEL));
            
            return;
        }

        if (status == Installer.DOWNLOADED_1K_OF_JAR &&
                state.getJarSize() > 0) {
            Gauge progressGauge = (Gauge)progressForm.get(progressGaugeIndex);
            progressGauge.setValue(progressGauge.getValue() + 1);
            return;
        }

        if (Constants.MONET_ENABLED) {
            if (status == Installer.GENERATING_APP_IMAGE) {
                updateProgressForm(null, 0,
                    Resource.getString
                      (ResourceConstants.
                      AMS_GRA_INTLR_GENERATING_APP_IMAGE_GAUGE_LABEL));
                return;
            }
        }

        if (Constants.VERIFY_ONCE) {
            if (status == Installer.VERIFYING_SUITE_CLASSES) {
                if (state.getLastException() != null) {
                    displayWarning(
                        Resource.getString(
                        ResourceConstants.AMS_GRA_INTLR_INSTALL_WARNING),
                        Resource.getString(
                        ResourceConstants.AMS_CLASS_VERIFIER_FAILURE));
                } else {
                    updateProgressForm(null, 0,
                        Resource.getString(
                        ResourceConstants.AMS_CLASS_VERIFIER_GAUGE_LABEL));
                    return;
                }
            }
        }

        if (status == Installer.VERIFYING_SUITE) {
            updateProgressForm(null, 0,
                 Resource.getString
                   (ResourceConstants.
                   AMS_GRA_INTLR_VERIFYING_SUITE_GAUGE_LABEL));
            return;
        }

        if (status == Installer.STORING_SUITE) {
            updateProgressForm(null, 0, finishingMessage);
            return;
        }

        if (status == Installer.CORRUPTED_SUITE) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                               "Suite is corrupted");
            }
            return;
        }
    }

    /**
     * Prevent screen flash on a fast systems.
     */
    void preventScreenFlash() {
        long waitTime = ALERT_TIMEOUT -
            (System.currentTimeMillis() - lastDisplayChange);

        if (waitTime <= 0) {
            return;
        }

        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ie) {
            // ignore
        }
    }

    /**
     * Update URL and gauge of the progress form.
     *
     * @param url new URL, null to remove, "" to not change
     * @param size 0 if unknown, else size of object to download in K bytes
     * @param gaugeLabel label for progress gauge
     */
    private void updateProgressForm(String url, int size, String gaugeLabel) {
        Gauge oldProgressGauge;
        Gauge progressGauge;
        StringItem urlItem;

        // We need to prevent "flashing" on fast development platforms.
        preventScreenFlash();

        if (size <= 0) {
            progressGauge = new Gauge(gaugeLabel,
                                      false, Gauge.INDEFINITE,
                                      Gauge.CONTINUOUS_RUNNING);
        } else {
            progressGauge = new Gauge(gaugeLabel,
                                      false, size, 0);
        }
        oldProgressGauge = (Gauge)progressForm.get(progressGaugeIndex);
        progressForm.set(progressGaugeIndex, progressGauge);

        // this ends the background thread of gauge.
        oldProgressGauge.setValue(Gauge.CONTINUOUS_IDLE);

        if (url == null) {
            urlItem = new StringItem("", "");
            progressForm.set(progressUrlIndex, urlItem);
        } else if (url.length() != 0) {
            
            urlItem =
                new StringItem(InstallerResource.getString(
                installer,InstallerResource.TYPE_OF_SOURCE) + ": ", url);
            
            progressForm.set(progressUrlIndex, urlItem);
        }

        lastDisplayChange = System.currentTimeMillis();
    }

    /**
     * Give the user a chance to act on warning during an installation.
     *
     * @param name name of the MIDlet suite to insert into the message
     * @param vendor vendor of the MIDlet suite to insert into the message,
     *        can be null
     * @param version version of the MIDlet suite to insert into the message,
     *        can be null
     * @param jadUrl URL of a JAD, can be null
     * @param e last exception from the installer
     */
    private void warnUser(String name, String vendor, String version,
                          String jadUrl, InvalidJadException e) {
        Form warningForm;

        warningForm = new Form(null);
        warningForm.setTitle(Resource.getString(ResourceConstants.WARNING));
        warningForm.append(translateJadException(e, name, vendor, version,
                                                 jadUrl));
        warningForm.addCommand(cancelCmd);
        warningForm.addCommand(okCmd);
        warningForm.setCommandListener(this);
        display.setCurrent(warningForm);
    }

    /**
     * Resume the install after a the user overrides a warning.
     */
    private void resumeInstallAfterWarning() {
        // redisplay the progress form
        display.setCurrent(progressForm);

        backgroundInstaller.continueInstall = true;
        synchronized (backgroundInstaller) {
            backgroundInstaller.notify();
        }
    }

    /**
     * Ask for a username and password.
     */
    private void getUsernameAndPassword() {
        getUsernameAndPasswordCommon("");
    }

    /**
     * Ask for proxy username and password.
     */
    private void getProxyUsernameAndPassword() {
        getUsernameAndPasswordCommon(
              Resource.getString(
              ResourceConstants.AMS_GRA_INTLR_PASSWORD_FORM_FIREWALL_TITLE));
    }

    /**
     * Ask a username and password.
     *
     * @param title title of the password form
     */
    private void getUsernameAndPasswordCommon(String title) {
        if (passwordForm == null) {
            passwordForm = new Form(null);

            usernameField = new TextField(
                            Resource.getString(ResourceConstants.
                                               AMS_GRA_INTLR_ENTER_ID),
                            null, 40,
                            TextField.ANY);
            passwordForm.append(usernameField);

            passwordField = new TextField(
                            Resource.getString(ResourceConstants.
                                               AMS_GRA_INTLR_PASSWORD),
                            null, 40,
                            TextField.PASSWORD);
            passwordForm.append(passwordField);
            passwordForm.addCommand(cancelCmd);
            passwordForm.addCommand(nextCmd);
            passwordForm.setCommandListener(this);
        }

        passwordForm.setTitle(title);
        passwordField.setString("");
        display.setCurrent(passwordForm);
    }

    /**
     * Resume the install of the suite with a password and username.
     */
    private void resumeInstallWithPassword() {
        String username;
        String password;


        username = usernameField.getString();
        password = passwordField.getString();
        if (username == null || username.length() == 0) {
            Alert a = new Alert(Resource.getString(ResourceConstants.ERROR),
                             Resource.getString(ResourceConstants.
                                                AMS_GRA_INTLR_ID_NOT_ENTERED),
                             null, AlertType.ERROR);
            a.setTimeout(ALERT_TIMEOUT);
            display.setCurrent(a, passwordForm);
            return;
        }

        if (password == null || password.length() == 0) {
            Alert a = new Alert(Resource.getString(ResourceConstants.ERROR),
                                Resource.getString(ResourceConstants.
                                            AMS_GRA_INTLR_PWD_NOT_ENTERED),
                                null, AlertType.ERROR);
            a.setTimeout(ALERT_TIMEOUT);
            display.setCurrent(a, passwordForm);
            return;
        }

        // redisplay the progress form
        display.setCurrent(progressForm);

        if (backgroundInstaller.proxyAuth) {
            backgroundInstaller.installState.setProxyUsername(username);
            backgroundInstaller.installState.setProxyPassword(password);
        } else {
            backgroundInstaller.installState.setUsername(username);
            backgroundInstaller.installState.setPassword(password);
        }

        backgroundInstaller.continueInstall = true;
        synchronized (backgroundInstaller) {
            backgroundInstaller.notify();
        }
    }

    /**
     * Confirm the JAR download with the user.
     *
     * @param state current state of the install.
     */
    private void displayDownloadConfirmation(InstallState state) {
        Form infoForm;
        StringItem item;
        String name;
        String desc;
        StringBuffer label = new StringBuffer(40);
        StringBuffer value = new StringBuffer(40);
        String[] values = new String[1];

        name = state.getSuiteName();

        try {
            infoForm = new Form(null);

            infoForm.setTitle(Resource.getString
                              (ResourceConstants.AMS_CONFIRMATION));

            values[0] = name;
            item = new StringItem(null, Resource.getString
                      (ResourceConstants.AMS_GRA_INTLR_WANT_INSTALL,
                       values));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            if (!installer.isJadSigned()) {
                // The MIDlet suite is not signed, therefore will be untrusted
                item = new StringItem(
                          Resource.getString(ResourceConstants.WARNING) + ":",
                          Resource.getString
                          (ResourceConstants.AMS_GRA_INTLR_UNTRUSTED_WARN));
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                infoForm.append(item);
            }

            // round up the size to a Kilobyte
            label.append(Resource.getString(ResourceConstants.AMS_SIZE));
            label.append(": ");
            value.setLength(0);
            value.append(state.getJarSize());
            value.append(" K");
            item = new StringItem(label.toString(), value.toString());
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            label.setLength(0);
            label.append(Resource.getString(ResourceConstants.AMS_VERSION));
            label.append(": ");
            value.setLength(0);
            item = new StringItem(label.toString(),
                       state.getAppProperty(MIDletSuite.VERSION_PROP));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            label.setLength(0);
            label.append(Resource.getString(ResourceConstants.AMS_VENDOR));
            label.append(": ");
            item = new StringItem(label.toString(),
                      state.getAppProperty(MIDletSuite.VENDOR_PROP));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            desc = state.getAppProperty(MIDletSuite.DESC_PROP);
            if (desc != null) {
                label.setLength(0);
                label.append(Resource.getString
                             (ResourceConstants.AMS_DESCRIPTION));
                label.append(": ");
                item = new StringItem(label.toString(), desc);
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                infoForm.append(item);
            }

            label.setLength(0);            
            label.append(InstallerResource.getString(
                    installer,InstallerResource.TYPE_OF_SOURCE));
            label.append(": ");
            infoForm.append(new StringItem(label.toString(),
                                           state.getJarUrl()));

            infoForm.addCommand(continueCmd);
            infoForm.addCommand(cancelCmd);
            infoForm.setCommandListener(this);

            // We need to prevent "flashing" on fast development platforms.
            preventScreenFlash();

            display.setCurrent(infoForm);
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(name);
            sb.append("\n");
            sb.append(Resource.getString(ResourceConstants.EXCEPTION));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString
                             (ResourceConstants.AMS_CANT_ACCESS),
                             sb.toString());
        }
    }

    /**
     * Ask the user during an update if they want to keep the old RMS data.
     *
     * @param state current state of the install.
     */
    private void displayKeepRMSForm(InstallState state) {
        Form infoForm;
        String name;
        StringBuffer value = new StringBuffer(40);
        String[] values = new String[1];

        name = state.getAppProperty(MIDletSuite.SUITE_NAME_PROP);

        try {
            infoForm = new Form(null);

            infoForm.setTitle(Resource.getString
                              (ResourceConstants.AMS_CONFIRMATION));

            values[0] = name;
            value.append(Resource.getString
                         (ResourceConstants.AMS_GRA_INTLR_NEW_OLD_VERSION,
                          values));
            infoForm.append(value.toString());

            infoForm.addCommand(keepRMSCmd);
            infoForm.addCommand(removeRMSCmd);
            infoForm.setCommandListener(this);

            // We need to prevent "flashing" on fast development platforms.
            preventScreenFlash();

            display.setCurrent(infoForm);
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(name);
            sb.append("\n");
            sb.append(Resource.getString(ResourceConstants.EXCEPTION));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString
                             (ResourceConstants.AMS_CANT_ACCESS),
                             sb.toString());
        }
    }

    /**
     * Confirm the authorization path with the user.
     *
     * @param state current state of the install.
     */
    private void displayAuthPathConfirmation(InstallState state) {
        Form infoForm;
        String name;
        String values[] = new String[1];
        StringItem item;
        String authPath[];
        String temp;
        StringBuffer label = new StringBuffer(40);

        name = state.getAppProperty(MIDletSuite.SUITE_NAME_PROP);

        try {
            infoForm = new Form(Resource.getString(
                          ResourceConstants.AMS_AUTHORIZATION_INFO));

            infoForm.append(new ImageItem(null, TrustedMIDletIcon.getIcon(),
                ImageItem.LAYOUT_NEWLINE_BEFORE |
                ImageItem.LAYOUT_CENTER |
                ImageItem.LAYOUT_NEWLINE_AFTER, null));

            values[0] = name;
            label.setLength(0);
            label.append(Resource.getString(
                ResourceConstants.AMS_GRA_INTLR_TRUSTED, values));
            label.append(": ");

            authPath = state.getAuthPath();
            temp = label.toString();
            for (int i = 0; i < authPath.length; i++) {
                item = new StringItem(temp, authPath[i]);
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                infoForm.append(item);
                temp = " -> ";
            }

            infoForm.addCommand(continueCmd);
            infoForm.addCommand(cancelCmd);
            infoForm.setCommandListener(this);

            // We need to prevent "flashing" on fast development platforms.
            preventScreenFlash();

            display.setCurrent(infoForm);
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(Resource.getString(ResourceConstants.EXCEPTION));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString
                             (ResourceConstants.AMS_CANT_ACCESS),
                             sb.toString());
        }
    }

    /**
     * Confirm redirection with the user.
     *
     * @param state current state of the install.
     * @param newLocation new url of the resource to install.
     */
    private void displayRedirectConfirmation(InstallState state,
                                             String newLocation) {
        Form infoForm;
        StringBuffer value = new StringBuffer(40);
        String[] values = new String[1];

        try {
            infoForm = new Form(null);

            infoForm.setTitle(Resource.getString(
                                  ResourceConstants.AMS_CONFIRMATION));

            values[0] = newLocation;
            value.append(Resource.getString(
                             ResourceConstants.AMS_GRA_INTLR_CONFIRM_REDIRECT,
                                 values));
            infoForm.append(value.toString());

            infoForm.addCommand(continueCmd);
            infoForm.addCommand(cancelCmd);
            infoForm.setCommandListener(this);

            // We need to prevent "flashing" on fast development platforms.
            preventScreenFlash();

            display.setCurrent(infoForm);
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(Resource.getString(ResourceConstants.EXCEPTION));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString(
                                 ResourceConstants.AMS_CANT_ACCESS),
                                     sb.toString());
        }
    }

    /**
     * Resume the install to start the JAR download.
     */
    private void startJarDownload() {
                
        updateProgressForm(backgroundInstaller.url, 0,
            InstallerResource.getString(installer,
                InstallerResource.CONNECTING_GAUGE_LABEL));

        // redisplay the progress form
        display.setCurrent(progressForm);

        backgroundInstaller.continueInstall = true;
        synchronized (backgroundInstaller) {
            backgroundInstaller.notify();
        }
    }

    /** Confirm the JAR only download with the user. */
    private void displayJarOnlyDownloadConfirmation() {
        Form infoForm;
        StringItem item;
        StringBuffer label = new StringBuffer(40);
        StringBuffer value = new StringBuffer(40);
        String[] values = new String[1];

        try {
            infoForm = new Form(null);

            infoForm.setTitle(Resource.getString
                              (ResourceConstants.AMS_CONFIRMATION));

            values[0] = backgroundInstaller.name;
            item = new StringItem(null, Resource.getString(
                                  ResourceConstants.AMS_GRA_INTLR_WANT_INSTALL,
                                  values));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);
            
            label.append(InstallerResource.getString(
                    installer,InstallerResource.TYPE_OF_SOURCE));
            label.append(": ");
            item = new StringItem(label.toString(), backgroundInstaller.url);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            value.append(" \n");
            value.append(Resource.getString
                         (ResourceConstants.AMS_GRA_INTLR_NO_INFO));
            infoForm.append(new StringItem(null, value.toString()));

            infoForm.addCommand(continueCmd);
            infoForm.addCommand(cancelCmd);
            infoForm.setCommandListener(this);

            // We need to prevent "flashing" on fast development platforms.
            preventScreenFlash();

            display.setCurrent(infoForm);
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(backgroundInstaller.name);
            sb.append("\n");
            sb.append(Resource.getString(ResourceConstants.EXCEPTION));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString
                             (ResourceConstants.AMS_CANT_ACCESS),
                             sb.toString());
        }
    }

    /**
     * Tell the background installer to keep the RMS data.
     *
     * @param keepRMS set to true to mean the user answered yes
     */
    private void setKeepRMSAnswer(boolean keepRMS) {
        // redisplay the progress form
        display.setCurrent(progressForm);

        // We need to prevent "flashing" on fast development platforms.
        preventScreenFlash();

        backgroundInstaller.continueInstall = keepRMS;
        synchronized (backgroundInstaller) {
            backgroundInstaller.notify();
        }
    }

    /**
     * Alert the user that an action was successful.
     *
     * @param successMessage message to display to user
     */
    private void displaySuccessMessage(String successMessage) {
        Image icon;
        Alert successAlert;

        icon = getImageFromInternalStorage("_dukeok8");

        successAlert = new Alert(null, successMessage, icon, null);

        successAlert.setTimeout(Alert.FOREVER);

        // Provide a listener to disable the advance-to-next-displayable
        // feature of Alert.
        successAlert.setCommandListener(
            new CommandListener() {
                public void commandAction(Command c, Displayable d) { }
            }
        );

        // We need to prevent "flashing" on fast development platforms.
        preventScreenFlash();

        lastDisplayChange = System.currentTimeMillis();
        display.setCurrent(successAlert, new Form(""));
        // this Form is never displayed
    }

    /**
     * Alert the user that an action was canceled.
     *
     * @param message message to display to user
     */
    private void displayCancelledMessage(String message) {
        Image icon;
        Alert cancelAlert;

        icon = getImageFromInternalStorage("_ack8");

        cancelAlert = new Alert(null, message, icon, null);

        cancelAlert.setTimeout(Alert.FOREVER);
        cancelAlert.setCommandListener(this);

        // We need to prevent "flashing" on fast development platforms.
        preventScreenFlash();

        lastDisplayChange = System.currentTimeMillis();
        display.setCurrent(cancelAlert);
    }

    /**
     * Display an alert to the user, with a done command.
     *
     * @param title alert's title
     * @param message alert message
     * @param type severity of the alert message
     */
    private void displayAlert(
        String title, String message, AlertType type) {

        Alert a = new Alert(title, message, null, type);

        a.setTimeout(Alert.FOREVER);
        a.setCommandListener(this);

        display.setCurrent(a, new Form(""));
        // this Form is never displayed
    }

    /**
     * Display an warning to the user, with a done command.
     *
     * @param title warnings form's title
     * @param message warning message
     */
    private void displayWarning(String title, String message) {
        displayAlert(title, message, AlertType.WARNING);
    }

    /**
     * Display an exception to the user, with a done command.
     *
     * @param title exception form's title
     * @param message exception message
     */
    private void displayException(String title, String message) {
        displayAlert(title, message, AlertType.ERROR);
    }


    /** A class to install a suite in a background thread. */
    private class BackgroundInstaller implements Runnable, InstallListener {
        /** Parent installer. */
        private GraphicalInstaller parent;
        /** URL to install from. */
        private String url;
        /** Name of MIDlet suite. */
        private String name;
        /** ID of the storage where the new midlet suite will be installed. */
        private int storageId;

        /**
         * Message for the user after the current install completes
         * successfully.
         */
        private String successMessage;
        /** Flag to update the current suite. */
        private boolean update;
        /** Flag for user confiramtion. */
        private boolean noConfirmation;
        /** State of the install. */
        InstallState installState;
        /** Signals that the user wants the install to continue. */
        boolean continueInstall;
        /** Signals that the suite only has JAR, no JAD. */
        boolean jarOnly;
        /** Signals that a proxyAuth is needed. */
        boolean proxyAuth;

        /**
         * Construct a BackgroundInstaller.
         *
         * @param theParent parent installer of this object
         * @param theJadUrl where to get the JAD.
         * @param theName name of the MIDlet suite
         * @param theStorageId id of the storage to install to
         * @param theSuccessMessage message to display to user upon success
         * @param updateFlag if true the current suite should be
         *                      overwritten without asking the user.
         * @param noConfirmationFlag if true the current suite should be
         *                      installed without asking the user.
         */
        private BackgroundInstaller(GraphicalInstaller theParent,
                String theJadUrl, String theName, int theStorageId,
                String theSuccessMessage, boolean updateFlag,
                boolean noConfirmationFlag) {
            parent = theParent;
            url = theJadUrl;
            name = theName;
            storageId = theStorageId;
            successMessage = theSuccessMessage;
            update = updateFlag;
            noConfirmation = noConfirmationFlag;
        }

        /**
         * Run the installer.
         */
        public void run() {
            // ID of the suite that was just installed
            int lastInstalledMIDletId = MIDletSuite.UNUSED_SUITE_ID;

            try {
                // a flag indicating that an attempt of installation must
                // be repeated, but now using the jar URL instead of jad
                boolean tryAgain;
                // title of the window displaying an error message
                String title;
                // an error message to display
                String msg;
               
                // repeat while(tryAgain)
                do {
                    tryAgain = false;
                    msg = null;

                    try {
                        if (jarOnly) {                            
                            lastInstalledMIDletId =
                                parent.installer.installJar(url, name,
                                    storageId, update, false, this);
                        } else {
                            lastInstalledMIDletId =
                                parent.installer.installJad(url, storageId,
                                    update, false, this);
                        }

                        // Let the manager know what suite was installed
                        GraphicalInstaller.saveSettings(null,
                                                        lastInstalledMIDletId);

                        parent.displaySuccessMessage(successMessage);

                        /*
                         * We need to prevent "flashing" on fast development
                         * platforms.
                         */
                        parent.preventScreenFlash();

                        parent.exit(true);
                    } catch (InvalidJadException ije) {
                        int reason = ije.getReason();
                                                 
                        if (reason == InvalidJadException.INVALID_JAD_TYPE) {
                            // media type of JAD was wrong, it could be a JAR
                            String mediaType = ije.getExtraData();
                                                      
                            if (Installer.JAR_MT_1.equals(mediaType) ||
                                    Installer.JAR_MT_2.equals(mediaType)) {
                                // re-run as a JAR only install                                
                                if (noConfirmation || confirmJarOnlyDownload()) {
                                    jarOnly = true;
                                    installState = null;
                                    tryAgain = true;
                                    continue;
                                }

                                displayListAfterCancelMessage();
                                break;
                            }
                        } else if
                            (reason == InvalidJadException.ALREADY_INSTALLED ||
                             reason == InvalidJadException.OLD_VERSION ||
                             reason == InvalidJadException.NEW_VERSION) {
                            // user has canceled the update operation,
                            // don't display an error message 
                            break;
                        }

                        msg = GraphicalInstaller.translateJadException(
                            ije, name, null, null, url);
                    } catch (MIDletSuiteLockedException msle) {
                        if (!killRunningMIDletIfUpdate) {
                            String[] values = new String[1];
                            values[0] = name;

                            if (!update) {
                                msg = Resource.getString(
                                    ResourceConstants.AMS_DISC_APP_LOCKED,
                                        values);
                            } else {
                                msg = Resource.getString(
                                    ResourceConstants.AMS_GRA_INTLR_LOCKED,
                                        values);
                            }
                        } else {
                             /*
                              * Kill running midlets from the suite being
                              * installed or updated.
                              */
                             NativeEvent event = new NativeEvent(
                                     EventTypes.MIDP_KILL_MIDLETS_EVENT);
                             event.intParam1 = parent.installer.info.getID();
                             event.intParam2 =
                                     VMUtils.getIsolateId();

                             final EventQueue eq = EventQueue.getEventQueue();
                             final Object waitUntilKilled = new Object();
                             final boolean[] midletKilled = new boolean[] {false};

                             final class KilledNotificationListener
                                     implements EventListener {

                                 KilledNotificationListener() { }

                                 void startListen() {
                                     eq.registerEventListener(
                                         EventTypes.MIDP_MIDLETS_KILLED_EVENT,
                                             this);
                                 }

                                 void endListen() {
                                     eq.remove(
                                         EventTypes.MIDP_MIDLETS_KILLED_EVENT);
                                 }

                                 public boolean preprocess(Event event,
                                                           Event waitingEvent) {
                                     return true;
                                 }

                                 public void process(Event event) {
                                     if (event.getType() ==
                                         EventTypes.MIDP_MIDLETS_KILLED_EVENT
                                     ) {
                                         synchronized (waitUntilKilled) {
                                             midletKilled[0] = true;
                                             waitUntilKilled.notify();
                                         }
                                     }
                                 }
                             }

                             final KilledNotificationListener listener =
                                     new KilledNotificationListener();
                             listener.startListen();

                             // ask AMS to kill the midlet
                             eq.sendNativeEventToIsolate(event,
                                 VMUtils.getAmsIsolateId());

                             synchronized (waitUntilKilled) {
                                 do {
                                     try {
                                         waitUntilKilled.wait();
                                     } catch (InterruptedException ie) {
                                         // ignore
                                     }
                                 } while (!midletKilled[0]);
                             }

                             listener.endListen();

                             tryAgain = true;
                        }
                    } catch (IOException ioe) {
                        if (parent.installer != null &&
                                parent.installer.wasStopped()) {
                            displayListAfterCancelMessage();
                            break;
                        } else {
                            String urlToShow = url;
                            // if installer break installation in step number 5
                            // or over, than we will show user correct path to
                            // jar and not path to jad
                            if (parent.installer.state.nextStep >= 5)
                                urlToShow = installer.state.installInfo.jarUrl;
                            
                            msg = InstallerResource.getString(
                                parent.installer,
                                InstallerResource.IO_EXCEPTION_MESSAGE)
                                    + ":" + urlToShow;
                             }
                    } catch (Throwable ex) {
                        if (Logging.TRACE_ENABLED) {
                            Logging.trace(ex, "Exception caught " +
                                "while installing");
                        }

                        msg = ex.getClass().getName() + ": " + ex.getMessage();
                    }

                } while (tryAgain);

                // display an error message, if any
                if (msg != null) {
                    title = Resource.getString(
                        ResourceConstants.AMS_GRA_INTLR_INSTALL_ERROR);
                    // go back to the app list
                    parent.displayException(title, msg);
                }
            } finally {
                if (lastInstalledMIDletId == MIDletSuite.UNUSED_SUITE_ID) {
                    // Reset an ID of the last successfully installed midlet
                    // because an error has occured.
                    GraphicalInstaller.saveSettings(null,
                        MIDletSuite.UNUSED_SUITE_ID);
                }

                if (parent.progressForm != null) {
                    // end the background thread of progress gauge.
                    Gauge progressGauge = (Gauge)parent.progressForm.get(
                                          parent.progressGaugeIndex);
                    progressGauge.setValue(Gauge.CONTINUOUS_IDLE);
                }
            }
        }

        /**
         * Called with the current state of the install so the user can be
         * asked to override the warning. Calls the parent to display the
         * warning to the user and then waits on the state object for
         * user's response.
         *
         * @param state current state of the install.
         *
         * @return true if the user wants to continue,
         *         false to stop the install
         */
        public boolean warnUser(InstallState state) {
            installState = state;
  
            InvalidJadException e = installState.getLastException();

            int reason = e.getReason();
            if (noConfirmation) {
                if (update) {
                    /* no confirmation is needed */
                    return true;
                } else {
                    /* confirmation is needed only for update */
                    if((reason != InvalidJadException.OLD_VERSION) &&
                       (reason != InvalidJadException.ALREADY_INSTALLED) &&
                       (reason != InvalidJadException.NEW_VERSION)) {
                        /* no confirmation is needed since it's not an update */
                        return true;
                    }
                }
            }

            switch (e.getReason()) {
            case InvalidJadException.UNAUTHORIZED:
                proxyAuth = false;
                parent.getUsernameAndPassword();
                break;

            case InvalidJadException.PROXY_AUTH:
                proxyAuth = true;
                parent.getProxyUsernameAndPassword();
                break;

            case InvalidJadException.OLD_VERSION:
            case InvalidJadException.ALREADY_INSTALLED:
            case InvalidJadException.NEW_VERSION:
                // this is now an update
                update = true;

                // fall through
            default:
                parent.warnUser(name,
                    state.getAppProperty(MIDletSuite.VENDOR_PROP),
                    state.getAppProperty(MIDletSuite.VERSION_PROP),
                    url, e);
            }

            return waitForUser();
        }

        /**
         * Called with the current state of the install so the user can be
         * asked to confirm the jar download.
         * If false is returned, the an I/O exception thrown and
         * {@link Installer#wasStopped()} will return true if called.
         *
         * @param state current state of the install.
         *
         * @return true if the user wants to continue, false to stop the
         *         install
         */
        public boolean confirmJarDownload(InstallState state) {
            if (update || noConfirmation) {
                // this an update, no need to confirm.
                return true;
            }

            installState = state;

            url = state.getJarUrl();

            parent.displayDownloadConfirmation(state);
            return waitForUser();
        }

        /**
         * Called with the current state of the install so the user can be
         * asked to confirm if the RMS data should be kept for new version of
         * an updated suite.
         *
         * @param state current state of the install.
         *
         * @return true if the user wants to keep the RMS data for the next
         * suite
         */
        public boolean keepRMS(InstallState state) {
            installState = state;

            parent.displayKeepRMSForm(state);
            return waitForUser();
        }


        /**
         * Called with the current state of the install so the user can be
         * asked to confirm the authentication path.
         * If false is returned, the an I/O exception thrown and
         * {@link Installer#wasStopped()} will return true if called.
         *
         * @param state current state of the install.
         *
         * @return true if the user wants to continue, false to stop the
         *         install
         */
        public boolean confirmAuthPath(InstallState state) {
            parent.displayAuthPathConfirmation(state);
            return waitForUser();
        }

        /**
         * Called with the current state of the install and the URL where the
         * request is attempted to be redirected so the user can be asked
         * to confirm if he really wants to install from the new location.
         * If false is returned, the an I/O exception thrown and
         * {@link Installer#wasStopped()} will return true if called.
         *
         * @param state       current state of the install.
         * @param newLocation new url of the resource to install.
         * 
         * @return true if the user wants to continue, false to stop the install
         */
        public boolean confirmRedirect(InstallState state, String newLocation) {
            parent.displayRedirectConfirmation(state, newLocation);
            return waitForUser();
        }

        /**
         * Called with the current state of the install so the user can be
         * asked to confirm the jar only download.
         *
         * @return true if the user wants to continue, false to stop the
         *         install
         */
        private boolean confirmJarOnlyDownload() {
            if (update) {
                // this an update, no need to confirm.
                return true;
            }

            parent.displayJarOnlyDownloadConfirmation();
            return waitForUser();
        }

        /**
         * Wait for the user to respond to current dialog.
         *
         * @return true if the user wants to continue, false to stop the
         *         install
         */
        private boolean waitForUser() {
            boolean temp;

            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ie) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                                      "wait threw an InterruptedException");
                    }
                }
            }

            installState = null;

            temp = continueInstall;
            continueInstall = false;

            return temp;
        }

        /**
         * Called with the current status of the install.
         * Changes the status alert box text based on the status.
         *
         * @param status current status of the install.
         * @param state current state of the install.
         */
        public void updateStatus(int status, InstallState state) {
            parent.updateStatus(status, state);
        }

        /**
         * Wait for the cancel message to be displayed to prevent flashing
         * and then display the list of suites.
         */
        private void displayListAfterCancelMessage() {
            // wait for the parent to display "cancelled"
            synchronized (parent) {
                /*
                 * We need to prevent "flashing" on fast
                 * development platforms.
                 */
                parent.preventScreenFlash();

                // go back to app list
                parent.exit(false);
            }
        }
    }
}
