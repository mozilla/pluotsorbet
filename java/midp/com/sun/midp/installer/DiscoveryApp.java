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

import java.util.*;

import javax.microedition.io.*;

import javax.microedition.lcdui.*;

import javax.microedition.midlet.*;

import javax.microedition.rms.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.midlet.*;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import javax.microedition.lcdui.List;

import com.sun.midp.io.FileUrl;

/**
 * The Graphical MIDlet suite Discovery Application.
 * <p>
 * Let the user install a suite from a list of suites
 * obtained using an HTML URL given by the user. This list is derived by
 * extracting the links with hrefs that are in quotes and end with ".jad" from
 * the HTML page. An href in an extracted link is assumed to be an absolute
 * URL for a MIDP application descriptor. The selected URL is then passed to
 * graphical Installer.
 */
public class DiscoveryApp extends MIDlet implements CommandListener {

    /** Display for this MIDlet. */
    private Display display;
    /** Contains the default URL for the install list. */
    private String defaultInstallListUrl = "http://";
    /** Contains the URL the user typed in. */
    private TextBox urlTextBox;
    /** Displays the progress of the install. */
    private Form progressForm;
    /** Gauge for progress form index. */
    private int progressGaugeIndex;
    /** URL for progress form index. */
    private int progressUrlIndex;
    /** Keeps track of when the display last changed, in milliseconds. */
    private long lastDisplayChange;
    /** Displays a list of suites to install to the user. */
    private List installListBox;
    /** Contains a list of suites to install. */
    private Vector installList;

    /** Command object for URL screen to go and discover available suites. */
    private Command discoverCmd =
        new Command(Resource.getString(ResourceConstants.GOTO),
                    Command.SCREEN, 1);
    /** Command object for "Install" command in the suite list form . */
    private Command installCmd = new Command(
        Resource.getString(ResourceConstants.INSTALL), Command.ITEM, 1);
    /** Command object for "Back" command in the suite list form. */
    private Command backCmd = new Command(Resource.getString
                                          (ResourceConstants.BACK),
                                          Command.BACK, 1);
    /** Command object for URL screen to save the URL for suites. */
    private Command saveCmd =
        new Command(Resource.getString(ResourceConstants.SAVE),
                    Command.SCREEN, 3);

    /** Command object for "Back" command in the URL form. */
    private Command endCmd = new Command(Resource.getString
                                         (ResourceConstants.BACK),
                                         Command.BACK, 1);

    /** Command object for begin writing path to file in external devices. */
    private Command fileStorage = new Command(Resource.getString
            (ResourceConstants.AMS_DISC_APP_INSTALL_FROM_FILE),Command.SCREEN,2);
    
    /** Command object for begin writing URL. */
    private Command httpStorage = new Command(Resource.getString
            (ResourceConstants.AMS_DISC_APP_INSTALL_FROM_HTTP),Command.SCREEN,2);
    
    /** Command object for begin installing file from external devices. */
    private Command installFromFileStorage = new Command(Resource.getString
            (ResourceConstants.AMS_DISC_APP_START_FILE_INSTALL),Command.SCREEN,1);
    
         
    /** Type of last installation. */
    private int lastTypeOfInstall;
    /** Current type of installation. */
    private int typeOfInstall;
    
    /*
     * Create and initialize a new discovery application MIDlet.
     * The saved URL is retrieved and the list of MIDlets are retrieved.
     */
    public DiscoveryApp() {
        String storageName;

        display = Display.getDisplay(this);
        
        typeOfInstall = InstallerResource.HTTP_INSTALL;
        lastTypeOfInstall = InstallerResource.HTTP_INSTALL;
        
        GraphicalInstaller.initSettings();
        restoreSettings();         
        // get the URL of a list of suites to install
        getUrl();
        
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
       
    }

    /**
     * Respond to a command issued on any Screen.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == discoverCmd) {
            // user wants to discover the suites that can be installed
            discoverSuitesToInstall(urlTextBox.getString());            
        } else if (s == installListBox &&
                  (c == List.SELECT_COMMAND || c == installCmd)) {
            installSuite(createSuiteDownloadInfo());            
        } else if (c == backCmd) {
            display.setCurrent(urlTextBox);
        } else if (c == saveCmd) {
            saveURLSetting();
        } else if (c == endCmd || c == Alert.DISMISS_COMMAND) {
            // goto back to the manager midlet
            notifyDestroyed();
        }
        // want to install from external storage
        else if (c == fileStorage) {          
          urlTextBox.setTitle(Resource.getString
                  (ResourceConstants.AMS_DISC_APP_STORAGE_INSTALL));
          setupCommands(InstallerResource.FILE_INSTALL);
          restoreSettings();
          urlTextBox.setString(defaultInstallListUrl);
        }
        // want to install from Web
        else if (c == httpStorage) {         
         urlTextBox.setTitle(Resource.getString
                  (ResourceConstants.AMS_DISC_APP_WEBSITE_INSTALL));
         setupCommands(InstallerResource.HTTP_INSTALL);
         restoreSettings();        
         urlTextBox.setString(defaultInstallListUrl);
        }
        else if (c == installFromFileStorage) {
         installSuite(createSuiteDownloadInfo());        
        }    
    }

    /**
     * Get the settings the Manager saved for the user.
     */
    private void restoreSettings() {
        ByteArrayInputStream bas;
        DataInputStream dis;
        byte[] data;
        RecordStore settings = null;

        /**
         * ams.url = "" or null when running OTA from command line /
         *           OTA provisioning
         * ams.url = <some url> when running OTA from KToolbar */
        String amsUrl = System.getProperty("ams.url");
        if (amsUrl != null && !amsUrl.equals("")) {
            defaultInstallListUrl = amsUrl.trim();
            return;
        }

        try {
            settings = RecordStore.openRecordStore(
                       GraphicalInstaller.SETTINGS_STORE, false);
           
            // recognize the last type of installation            
            data = settings.getRecord(
                    GraphicalInstaller.LAST_INSTALLATION_SOURCE_RECORD_ID);
            if (data != null) {
                bas = new ByteArrayInputStream(data);
                dis = new DataInputStream(bas);
                lastTypeOfInstall = dis.readInt();
                if(urlTextBox == null)
                   typeOfInstall=lastTypeOfInstall; 
                
            }
            // if this method invoked from constructor or
            // if user switch to web source installation or
            // if switch to storage install and than switch
            // back without installation
            if ((lastTypeOfInstall == InstallerResource.HTTP_INSTALL && urlTextBox==null) ||
               (lastTypeOfInstall == InstallerResource.FILE_INSTALL &&
                                           typeOfInstall == InstallerResource.HTTP_INSTALL) ||
               (lastTypeOfInstall == InstallerResource.HTTP_INSTALL &&
                                           typeOfInstall == InstallerResource.HTTP_INSTALL)) { 
                data = settings.getRecord(GraphicalInstaller.URL_RECORD_ID);                
                defaultInstallListUrl="http://";
            }
            // if this method invoked from constructor or
            // if user switch to storage source installation or
            // if switch to web source install and than switch back without installation
            else if ((lastTypeOfInstall == InstallerResource.FILE_INSTALL && urlTextBox==null) ||
                    (lastTypeOfInstall == InstallerResource.HTTP_INSTALL &&
                                             typeOfInstall==InstallerResource.FILE_INSTALL)      ||
                    (lastTypeOfInstall == InstallerResource.FILE_INSTALL &&
                                             typeOfInstall==InstallerResource.FILE_INSTALL)) {
                     data = settings.getRecord(
                             GraphicalInstaller.FILE_PATH_RECORD_ID);              
                     defaultInstallListUrl="";
            }
                                        
            if (data != null) {
                bas = new ByteArrayInputStream(data);
                dis = new DataInputStream(bas);
                defaultInstallListUrl = dis.readUTF();                
            }
           
        } catch (RecordStoreException e) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                               "restoreSettings threw a RecordStoreException");
            }
        } catch (IOException e) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                               "restoreSettings threw an IOException");
            }
        } finally {
            if (settings != null) {
                try {
                    settings.closeRecordStore();
                } catch (RecordStoreException e) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "closeRecordStore threw a RecordStoreException");
                    }
                }
            }
        }
    }

    /**
     * Save the URL setting the user entered in to the urlTextBox.
     */
    private void saveURLSetting() {
        String temp;
        Exception ex;

        temp = urlTextBox.getString();
        if (typeOfInstall == InstallerResource.FILE_INSTALL) {
            temp = InstallerResource.DEFAULT_FILE_SCHEMA+temp;
        }
        ex = GraphicalInstaller.saveSettings(temp,
                MIDletSuite.INTERNAL_SUITE_ID);
        if (ex != null) {
            displayException(Resource.getString
                             (ResourceConstants.EXCEPTION), ex.toString());
            return;
        }

        defaultInstallListUrl = temp;

        displaySuccessMessage(Resource.getString
                              (ResourceConstants.AMS_MGR_SAVED));
    }

    /**
     * Alert the user that an action was successful.
     *
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

        lastDisplayChange = System.currentTimeMillis();
        display.setCurrent(successAlert);
    }

    /**
     * Let the user select a suite to install. The suites that are listed
     * are the links on a web page that end with .jad.
     *
     * @param url where to get the list of suites to install.
     */
    private void discoverSuitesToInstall(String url) {
        new Thread(new BackgroundInstallListGetter(this, url)).start();
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
                typeOfInstall,InstallerResource.TYPE_OF_SOURCE) + ": ", url);
        }

        progressUrlIndex = progressForm.append(urlItem);

        display.setCurrent(progressForm);
        lastDisplayChange = System.currentTimeMillis();

        return progressForm;
    }

    /**
     * Create or get SuiteDownloadInfo object for specific
     * type of installation
     * 
     * @return suite info
     */
    private SuiteDownloadInfo createSuiteDownloadInfo() {
        SuiteDownloadInfo suite;
        
        if (typeOfInstall == InstallerResource.HTTP_INSTALL) {
            int selectedSuite = installListBox.getSelectedIndex();
            suite = (SuiteDownloadInfo)installList.elementAt(selectedSuite);         
        } else {
            String filenamepath = urlTextBox.getString().startsWith(
                    InstallerResource.DEFAULT_FILE_SCHEMA) ?
                    urlTextBox.getString() :
                    InstallerResource.DEFAULT_FILE_SCHEMA+
                    urlTextBox.getString();                
            suite = new SuiteDownloadInfo(filenamepath,urlTextBox.getString());            
        }
        
        return suite;
    }
    /**
     * Install a suite.
     *
     * @param suite suite we want to install
     */
    private void installSuite(SuiteDownloadInfo suite) {
        MIDletStateHandler midletStateHandler =
        MIDletStateHandler.getMidletStateHandler();
        MIDletSuite midletSuite = midletStateHandler.getMIDletSuite();        
        String displayName;

        midletSuite.setTempProperty(null, "arg-0", "I");
        midletSuite.setTempProperty(null, "arg-1", suite.url);
        midletSuite.setTempProperty(null, "arg-2", suite.label);

        displayName =
            Resource.getString(ResourceConstants.INSTALL_APPLICATION);
        try {
            midletStateHandler.startMIDlet(
                "com.sun.midp.installer.GraphicalInstaller", displayName);
            /*
             * Give the create MIDlet notification 1 second to get to
             * AMS.
             */
            Thread.sleep(1000);
            notifyDestroyed();
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(displayName);
            sb.append("\n");
            sb.append(Resource.getString(ResourceConstants.ERROR));
            sb.append(": ");
            sb.append(ex.toString());

            Alert a = new Alert(Resource.getString
                                (ResourceConstants.AMS_CANNOT_START),
                                sb.toString(), null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            display.setCurrent(a, urlTextBox);
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
        while (System.currentTimeMillis() - lastDisplayChange <
               GraphicalInstaller.ALERT_TIMEOUT);

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
                typeOfInstall,InstallerResource.TYPE_OF_SOURCE) + ": ", url);
            
            progressForm.set(progressUrlIndex, urlItem);
        }

        lastDisplayChange = System.currentTimeMillis();
    }

    /**
     * Ask the user for the URL.
     */
    private void getUrl() {
        try {
            if (urlTextBox == null) {                
                urlTextBox = new TextBox(Resource.getString
                                         (ResourceConstants.
                                          AMS_DISC_APP_WEBSITE_INSTALL),
                                         defaultInstallListUrl, 1024,
                                         TextField.ANY);
                
                urlTextBox.addCommand(endCmd);
                urlTextBox.addCommand(saveCmd);
                
                if (lastTypeOfInstall == InstallerResource.HTTP_INSTALL) {
                   setupCommands(InstallerResource.HTTP_INSTALL);
                }
                else {
                    setupCommands(InstallerResource.FILE_INSTALL);
                    urlTextBox.setTitle(Resource.getString(
                            ResourceConstants.AMS_DISC_APP_STORAGE_INSTALL));
                }
                               
                urlTextBox.setCommandListener(this);                
            }

            display.setCurrent(urlTextBox);
        } catch (Exception ex) {
            displayException(Resource.getString(ResourceConstants.EXCEPTION),
                             ex.toString());
        }
    }

    /**
     * Display an exception to the user, with a done command.
     *
     * @param title exception form's title
     * @param message exception message
     */
    private void displayException(String title, String message) {
        Alert a = new Alert(title, message, null, AlertType.ERROR);

        a.setTimeout(Alert.FOREVER);
        a.setCommandListener(this);

        display.setCurrent(a);
    }

    /** A class to get the install list in a background thread. */
    private class BackgroundInstallListGetter implements Runnable {
        /** Parent application. */
        private DiscoveryApp parent;
        /** URL of the list. */
        private String url;

        /**
         * Construct a BackgroundInstallListGetter.
         *
         * @param theParent parent of this object
         * @param theUrl where to get the list of suites to install.
         */
        private BackgroundInstallListGetter(DiscoveryApp theParent,
                                            String theUrl) {
            parent = theParent;
            url = theUrl;
        }

        /**
         * Get the list of suites for the user to install.
         * The suites that are listed
         * are the links on a web page that end with .jad.
         */
        public void run() {
            StreamConnection conn = null;
            InputStreamReader in = null;
            String errorMessage;
            long startTime;

            startTime = System.currentTimeMillis();

            try {
                    parent.displayProgressForm(
                        InstallerResource.getString(
                        typeOfInstall,InstallerResource.PREPARE_INSTALLATION_LIST_LABEL),
                        "", url, 0,
                        InstallerResource.getString(
                        typeOfInstall,InstallerResource.CONNECTING_GAUGE_LABEL));
                       
                
                conn = (StreamConnection)Connector.open(url, Connector.READ);
                in = new InputStreamReader(conn.openInputStream());
                try {
                        
                     parent.updateProgressForm("", 0,
                        InstallerResource.getString(typeOfInstall,
                        InstallerResource.TRANSFER_DATA_LABEL));
                        
                    parent.installList =
                        SuiteDownloadInfo.getDownloadInfoFromPage(in);

                    if (parent.installList.size() > 0) {
                        parent.installListBox =
                            new List(Resource.getString
                                     (ResourceConstants.
                                      AMS_DISC_APP_SELECT_INSTALL),
                                     Choice.IMPLICIT);

                        // Add each suite
                        for (int i = 0; i < parent.installList.size(); i++) {
                            SuiteDownloadInfo suite =
                                (SuiteDownloadInfo)installList.elementAt(i);
                            parent.installListBox.append(suite.label,
                                                         (Image)null);
                        }

                        parent.installListBox.addCommand(parent.backCmd);
                        parent.installListBox.addCommand(parent.installCmd);
                        parent.installListBox.setCommandListener(parent);

                        /*
                         * We need to prevent "flashing" on fast development
                         * platforms.
                         */
                        while (System.currentTimeMillis() -
                            parent.lastDisplayChange <
                            GraphicalInstaller.ALERT_TIMEOUT);

                        parent.display.setCurrent(parent.installListBox);
                        return;
                    }

                    errorMessage = Resource.getString
                        (ResourceConstants.AMS_DISC_APP_CHECK_URL_MSG);
                } catch (IllegalArgumentException ex) {
                    errorMessage = Resource.getString
                        (ResourceConstants.AMS_DISC_APP_URL_FORMAT_MSG);
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                }
            } catch (Exception ex) {
                errorMessage = Resource.getString
                    (ResourceConstants.AMS_DISC_APP_CONN_FAILED_MSG);
            } finally {
                if (parent.progressForm != null) {
                    // end the background thread of progress gauge.
                    Gauge progressGauge = (Gauge)parent.progressForm.get(
                                          parent.progressGaugeIndex);
                    progressGauge.setValue(Gauge.CONTINUOUS_IDLE);
                }

                try {
                    conn.close();
                    in.close();
                } catch (Exception e) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                                      "close threw an Exception");
                    }
                }
            }

            Alert a = new Alert(Resource.getString(ResourceConstants.ERROR),
                                errorMessage, null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            parent.display.setCurrent(a, parent.urlTextBox);
        }
    }
    
    /**
     *  Turning Commands for urlTextBox.
     * @param type what type of installation
     */
    private void setupCommands(int type) {
        if (type == InstallerResource.HTTP_INSTALL) {
            urlTextBox.removeCommand(httpStorage);
            urlTextBox.removeCommand(installFromFileStorage);
            urlTextBox.addCommand(fileStorage);
            urlTextBox.addCommand(discoverCmd);            
            typeOfInstall = InstallerResource.HTTP_INSTALL;
        } else {
            urlTextBox.removeCommand(fileStorage);        
            urlTextBox.removeCommand(discoverCmd);
            urlTextBox.addCommand(httpStorage);
            urlTextBox.addCommand(installFromFileStorage);            
            typeOfInstall = InstallerResource.FILE_INSTALL;
        }       
    }      
}
