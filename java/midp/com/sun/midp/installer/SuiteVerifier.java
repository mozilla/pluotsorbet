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

import com.sun.cldchi.jvm.JVM;
import com.sun.midp.midletsuite.MIDletSuiteStorage;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.main.MIDletSuiteVerifier;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;
import java.io.IOException;

/**
 * The Graphical MIDlet suite classes verifier application.
 * <p>
 */
public class SuiteVerifier extends MIDlet implements CommandListener {

    /** Display for this MIDlet */
    private Display display;
    /** Command object for "Back" command in the suite verification form */
    private Command backCommand;
    /** Displays the progress of the verification */
    private Form progressForm;
    /** Gauge for progress form index. */
    private int progressGaugeIndex;

    /** Path to the JAR package to be verified */
    private String jarPath;
    /** Suite ID of the suite whose classes are verified */
    private int suiteId;
    /** Suite storage */
    private MIDletSuiteStorage suiteStorage;

    /**
     * Create and initialize a new discovery application MIDlet.
     * The saved URL is retrieved and the list of MIDlets are retrieved.
     */
    public SuiteVerifier() {
        display = Display.getDisplay(this);

        String strSuiteId = getAppProperty("arg-0");
        try {
            suiteId = Integer.parseInt(strSuiteId);
        } catch (RuntimeException ex) {
            suiteId = MIDletSuite.UNUSED_SUITE_ID;
        }

        suiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();
        jarPath = suiteStorage.getMidletSuiteJarPath(suiteId);

        if (jarPath == null) {
            exit(false);
            return;
        }
        createProgressForm();
        new Thread(new BackgroundVerifier(this, jarPath)).start();
    }

    /**
     * Exit the SuiteVerifier with the status supplied.
     * It will perform any remaining cleanup and call notifyDestroyed.
     * @param status <code>true</code> if the install was a success,
     *  <code>false</code> otherwise.
     */
    void exit(boolean status) {
        /* TBD: Handle the status */
        notifyDestroyed();
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
        // Provide a listener to disable the advance-to-next-displayable
        // feature of Alert.
        successAlert.setCommandListener(
            new CommandListener() {
                public void commandAction(Command c, Displayable d) { }
            }
        );
        display.setCurrent(successAlert);
    }

    /**
     * Create main form for suite classes verification
     * @return Created form instance
     */
    private Form createProgressForm() {

        progressForm = new Form(null);
        progressForm.setTitle(Resource.getString(
            ResourceConstants.AMS_CLASS_VERIFIER_TITLE));
        Gauge progressGauge = new Gauge(
            Resource.getString(
            ResourceConstants.AMS_CLASS_VERIFIER_GAUGE_LABEL),
            false, Gauge.INDEFINITE,
            Gauge.CONTINUOUS_RUNNING);

        progressGaugeIndex = progressForm.append(progressGauge);
        backCommand = new Command(Resource.getString(
            ResourceConstants.BACK), Command.BACK, 1);
        progressForm.addCommand(backCommand);
        progressForm.setCommandListener(this);
        display.setCurrent(progressForm);
        return progressForm;
    }

    /**
     * Catch command events that occurred on
     * <code>Displayable d</code>.
      * @param command
     * @param displayable
     */
    public void commandAction(Command command, Displayable displayable) {
        if(command == backCommand) {
            destroyApp(false);
            notifyDestroyed();
        } else if (command == Alert.DISMISS_COMMAND) {
            // goto back to the manager midlet
            exit(false);
        }
    }

    /**
     * Display a warning to the user, with a done command.
     *
     * @param title warning form's title
     * @param message warning message
     */
    private void displayWarning(String title, String message) {
        Alert a = new Alert(title, message, null, AlertType.WARNING);
        a.setTimeout(Alert.FOREVER);
        a.setCommandListener(this);
        display.setCurrent(a);
    }

    /**
     * Store hash value of the suite JAR after all classes
     * in the package are successfully verified
     */
    private void storeSuiteHash() {
        try {
            // Evaluate suite hash value and store it
            byte []verifyHash = MIDletSuiteVerifier.getJarHash(jarPath);
            suiteStorage.storeSuiteVerifyHash(suiteId, verifyHash);
        } catch (IOException ioe) {
            displayWarning(
                Resource.getString(
                ResourceConstants.AMS_CLASS_VERIFIER_WARNING),
                Resource.getString(
                ResourceConstants.AMS_CLASS_VERIFIER_CANT_STORE_HASH));
        }
    }

    /** A class to verify all classes of a JAR in a background thread. */
    private class BackgroundVerifier implements Runnable {
        /** Size of the verification portion */
        final static int CHUNK_SIZE = 10240;

        /** Parent application. */
        private SuiteVerifier parent;
        /** JAR path. */
        private String jarPath;

        /**
         * Construct a BackgroundVerifier.
         *
         * @param theParent parent of this object
         * @param theJarPath path of the JAR to be verified.
         */
        private BackgroundVerifier(SuiteVerifier theParent,
                                   String theJarPath) {
            parent = theParent;
            jarPath = theJarPath;
        }

        /** Verify in background all classes within a JAR */
        public void run() {
            int status = JVM.verifyJar(jarPath, CHUNK_SIZE);

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                "JAR classes verification: " + jarPath + ", " + status);
            }

            if (parent.progressForm != null) {
                // End the background thread of progress gauge.
                Gauge progressGauge = (Gauge)parent.progressForm.get(
                    parent.progressGaugeIndex);
                progressGauge.setValue(Gauge.CONTINUOUS_IDLE);
            }

            if (status != JVM.STATUS_VERIFY_SUCCEEDED) {
                parent.displayWarning(
                    Resource.getString(
                    ResourceConstants.AMS_CLASS_VERIFIER_WARNING),
                    Resource.getString(
                    ResourceConstants.AMS_CLASS_VERIFIER_FAILURE));
            } else {
                parent.storeSuiteHash();
                parent.displaySuccessMessage(
                    Resource.getString(
                    ResourceConstants.AMS_CLASS_VERIFIER_SUCCESS));
            }

            parent.exit(true);
        }
    }
}
