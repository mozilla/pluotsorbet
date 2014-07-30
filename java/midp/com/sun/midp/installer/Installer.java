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

import java.util.Vector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;

import javax.microedition.io.ConnectionNotFoundException;

import com.sun.j2me.security.*;

import com.sun.midp.security.*;

import com.sun.midp.main.MIDletSuiteVerifier;
import com.sun.midp.main.MIDletAppImageGenerator;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.midletsuite.*;

import com.sun.midp.jarutil.JarReader;

import com.sun.midp.io.HttpUrl;

import com.sun.midp.io.Util;

import com.sun.midp.io.j2me.push.PushRegistryInternal;
import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.io.j2me.storage.File;

import com.sun.midp.rms.RecordStoreFactory;

import com.sun.midp.content.CHManager;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.midp.configurator.Constants;
import com.sun.midp.jsr075.FileConnectionCleanup;

/**
 * An Installer manages MIDlet suites and libraries
 * present in a Java application environment.  An MIDlet suite
 * distributed as a descriptor and JAR pair.
 * The descriptor identifies the configuration and contains security
 * information and the manifest of the JAR describes the contents.
 * The implementation of an Installer is
 * specific to the platform and provides access to
 * procedures that make an MIDlet suite visible to users.
 * <P>
 * Each installed package is uniquely identified by a storage name
 * constructed from the combination
 * of the values of the <code>MIDlet-Name</code> and
 * <code>MIDlet-Vendor</code> attributes.
 * The syntax and content of the strings used to identify
 * installed packages are implementation dependent.
 * Only packages installed or upgraded using this API appear
 * in the list of known packages.
 *
 */
public abstract class Installer {
    /** Status code to signal connection to the JAD server was successful. */
    public static final int DOWNLOADING_JAD = 1;

    /** Status code to signal that another 1K of the JAD has been download. */
    public static final int DOWNLOADED_1K_OF_JAD = 2;

    /** Status code to signal connection to the JAR server was successful. */
    public static final int DOWNLOADING_JAR = 3;

    /** Status code to signal that another 1K of the JAR has been download. */
    public static final int DOWNLOADED_1K_OF_JAR = 4;

    /**
     * Status code to signal that download is done and the suite is being
     * verified.
     */
    public static final int VERIFYING_SUITE = 5;

    /**
     * Status code to signal that application image is being generating.
     */
    public static final int GENERATING_APP_IMAGE = 6;

    /**
     * Status code to signal that suite classes are being verified.
     */
    public static final int VERIFYING_SUITE_CLASSES = 7;

    /**
     * Status code for local writing of the verified MIDlet suite.
     * Stopping the install at this point has no effect, so there user
     * should not be given a chance to stop the install.
     */
    public static final int STORING_SUITE = 8;

    /** Status code for corrupted suite */
    public static final int CORRUPTED_SUITE = 9;

    /** System property containing the supported microedition profiles */
    protected static final String MICROEDITION_PROFILES =
        "microedition.profiles";

    /** System property containing the microedition configuration */
    protected static final String MICROEDITION_CONFIG =
        "microedition.configuration";

    /** System property containing the microedition locale */
    protected static final String MICROEDITION_LOCALE = "microedition.locale";

    /** Media-Type for valid application descriptor files. */
    public static final String JAD_MT = "text/vnd.sun.j2me.app-descriptor";

    /** Media-Type for valid Jar file. */
    public static final String JAR_MT_1 = "application/java";

    /** Media-Type for valid Jar file. */
    public static final String JAR_MT_2 = "application/java-archive";

    /**
     * Filename to save the JAR of the suite temporarily. This is used
     * to avoid overwriting an existing JAR prior to verification.
     */
    protected static final String TMP_FILENAME = "installer.tmp";

    /** Midlet suite signature verifier. */
    protected Verifier verifier;

    /** Holds the install state. */
    protected InstallStateImpl state;

    /** Holds the access control context. */
    protected AccessControlContext accessControlContext;

    /** An alias for more state.installInfo to get more compact record. */
    protected InstallInfo info;
    
    /** An alias for more state.suiteSettings to get more compact record. */
    protected SuiteSettings settings;

    /** Holds the CLDC configuration string. */
    protected String cldcConfig;

    /** Holds the device's Runtime Execution Environment string. */
    protected final String cldcRuntimeEnv = "MIDP.CLDC";

    /** Holds the MIDP supported profiles. */
    private Vector supportedProfiles;

    /** Use this to be the security domain for unsigned suites. */
    protected String unsignedSecurityDomain =
        Permissions.getUnsignedDomain();

    /**
     * Include this permissions into the list of permissions
     * given in MIDlet-Permissions jad attribute for unsigned
     * suites.
     */
    protected String additionalPermissions;

    /**
     * Constructor of the Installer.
     */
    Installer() {
        state = getInstallState();
        verifier = new VerifierImpl(state);
        // This setting has no effect until OCSP is enabled at the build-time.
        verifier.enableOCSPCheck(true);

        // Aliases for more compact record.
        info = state.installInfo;
        settings = state.suiteSettings;
    }

    /**
     * Creates an instance of InstallState of the appropriate type
     * depending on the installer type. Should be overloaded in the
     * inherited classes.
     *
     * @return an instance of class containing the installation state
     */
    protected InstallStateImpl getInstallState() {
        if (state == null) {
            state = new InstallStateImpl();
            // IMPL_NOTE: "info" and "settings" aliases must be updated
            // after calling getInstallState().
        }

        return state;
    }

    /**
     * Creates an AccessControlContext for the suite being installed.
     *
     * @return an instance of class containing the installation state
     */
    protected AccessControlContext getAccessControlContext() {
        if (accessControlContext == null) {
            accessControlContext = new AccessControl(getInstallState());
        }

        return accessControlContext;
    }
    
    /**
     * Installs a software package from the given URL. The URL is assumed
     * refer to an application descriptor.
     * <p>
     * If the component to be installed is the same as an existing
     * component (by comparing the <code>MIDlet-Name</code>,
     * <code>MIDlet-Vendor</code> attributes)
     * then this install is an upgrade if the version number is greater
     * than the current version.  If so, the new version replaces in its
     * entirety the current version.
     * <p>
     * It is implementation dependent when the upgraded component is
     * made available for use.
     * <p>
     * The implementation of install must be robust in the presence
     * of failures such as running out of memory.  If this method
     * throws an exception then the package must <em>not</em> be installed
     * and any previous version of the component must be left intact
     * and operational.
     * <p>
     * To receive status updates and installer warnings, provide an install
     * listener. If no listener is provided all warnings will be thrown
     * as exceptions.
     *
     * @param location the URL from which the application descriptor can be
     *        updated
     * @param storageId ID of the storage where the suite should be saved
     * @param force if <code>true</code> the MIDlet suite components to be
     *              installed will overwrite any existing components without
     *              any version comparison
     * @param removeRMS if <code>true</code> and existing RMS data will be
     *              removed when overwriting an existing suite
     * @param installListener object to receive status updates and install
     *     warnings, can be null
     *
     * @return the unique ID of the installed package.
     *
     * @exception ConnectionNotFoundException if JAD URL is invalid
     * @exception IOException is thrown if any error prevents the installation
     *   of the MIDlet suite, including being unable to access the application
     *   descriptor or JAR
     * @exception InvalidJadException if the downloaded application descriptor
     *   is invalid
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     * @exception SecurityException if the caller does not have permission
     *   to install software
     * @exception IllegalArgumentException is thrown, if the location of the
     * descriptor file is not specified
     */
    public int installJad(String location, int storageId, boolean force,
        boolean removeRMS, InstallListener installListener)
            throws IOException, InvalidJadException,
                   MIDletSuiteLockedException, SecurityException {
            
        info.jadUrl = location;
        state.force = force;
        state.removeRMS = removeRMS;
        state.nextStep = 1;
        state.listener = installListener;
        state.chmanager = CHManager.getManager(null);
        state.storageId = storageId;
        
        return performInstall();
    }

    /**
     * Installs a software package from the given URL. The URL is assumed
     * refer to a JAR.
     * <p>
     * If the component to be installed is the same as an existing
     * component (by comparing the <code>MIDlet-Name</code>,
     * <code>MIDlet-Vendor</code> attributes)
     * then this install is an upgrade if the version number is greater
     * than the current version.  If so, the new version replaces in its
     * entirety the current version.
     * <p>
     * It is implementation dependent when the upgraded component is
     * made available for use.
     * <p>
     * The implementation of install must be robust in the presence
     * of failures such as running out of memory.  If this method
     * throws an exception then the package must <em>not</em> be installed
     * and any previous version of the component must be left intact
     * and operational.
     * <p>
     * To receive status updates and installer warnings, provide an install
     * listener. If no listener is provided all warnings will be thrown
     * as exceptions.
     *
     * @param location the URL from which the JAR can be updated
     * @param name the name of the suite to be updated
     * @param storageId ID of the storage where the suite should be saved
     * @param force if <code>true</code> the MIDlet suite components to be
     *              installed will overwrite any existing components without
     *              any version comparison
     * @param removeRMS if <code>true</code> and existing RMS data will be
     *              removed when overwriting an existing suite
     * @param installListener object to receive status updates and install
     *     warnings, can be null
     *
     * @return the unique ID of the installed package.
     *
     * @exception IOException is thrown if any error prevents the installation
     *   of the MIDlet suite, including being unable to access the JAR
     * @exception InvalidJadException if the downloaded JAR is invalid
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     * @exception SecurityException if the caller does not have permission
     *   to install software
     * @exception IllegalArgumentException is thrown, if the location of the
     * JAR specified
     */
    public int installJar(String location, String name, int storageId,
            boolean force, boolean removeRMS, InstallListener installListener)
                throws IOException, InvalidJadException,
                    MIDletSuiteLockedException {

        if (location == null || location.length() == 0) {
            throw
                new IllegalArgumentException("Must specify URL of .jar file");
        }
      
        info.jadUrl = null;
        info.jarUrl = location;
        info.suiteName = name;
        state.force = force;
        state.removeRMS = removeRMS;
        state.file = new File();
        state.nextStep = 5;
        state.listener = installListener;
        state.chmanager = CHManager.getManager(null);
        state.storageId = storageId;

        return performInstall();
    }

    /**
     * Enables or disables certificate revocation checking using OCSP.
     *
     * @param enable true to enable OCSP checking, false - to disable it
     */
    public void enableOCSPCheck(boolean enable) {
        // This setting has no effect until OCSP is enabled at the build-time.
        verifier.enableOCSPCheck(enable);
    }

    /**
     * Returns true if OCSP certificate revocation checking is enabled,
     * false if it is disabled.
     *
     * @return true if OCSP checking is enabled, false otherwise
     */
    public boolean isOCSPCheckEnabled() {
        return verifier.isOCSPCheckEnabled();
    }

    /**
     * Performs an install.
     *
     * @return the unique name of the installed package
     *
     * @exception IOException is thrown, if an I/O error occurs during
     * descriptor or jar file download
     * @exception InvalidJadException is thrown, if the descriptor file is not
     * properly formatted or does not contain the required
     * information
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     * @exception IllegalArgumentException is thrown, if the
     * descriptor file is not specified
     */
    protected int performInstall()
            throws IOException, InvalidJadException,
                   MIDletSuiteLockedException {

        state.midletSuiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();

        /* Disable push interruptions during install. */
        PushRegistryInternal.enablePushLaunch(false);

        try {
            state.startTime = System.currentTimeMillis();

            while (state.nextStep < 9) {
                /*
                 * clear the previous warning, so we can tell if another has
                 * happened
                 */
                state.exception = null;

                if (state.stopInstallation) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.USER_CANCELLED_MSG);
                    throw new IOException("stopped");
                }

                switch (state.nextStep) {
                case 1:
                    installStep1();
                    break;

                case 2:
                    installStep2();
                    break;

                case 3:
                    installStep3();
                    break;

                case 4:
                    installStep4();
                    break;

                case 5:
                    installStep5();
                    break;

                case 6:
                    installStep6();
                    break;

                case 7:
                    installStep7();
                    break;

                case 8:
                    installStep8();
                    break;

                default:
                    // for safety/completeness.
                    Logging.report(Logging.CRITICAL, LogChannels.LC_AMS,
                        "Installer: Unknown step: " + state.nextStep);
                    break;
                }

                if (state.exception != null) {
                    if (state.listener == null) {
                        throw state.exception;
                    }

                    if (!state.listener.warnUser(state)) {
                        state.stopInstallation = true;
                        postInstallMsgBackToProvider(
                            OtaNotifier.USER_CANCELLED_MSG);
                        throw state.exception;
                    }
                }
            }
        } finally {
            if (state.previousSuite != null) {
                state.previousSuite.close();
            }
            if (info.jarFilename != null) {
                if (state.file.exists(info.jarFilename)) {
                    try {
                        state.file.delete(info.jarFilename);
                    } catch (Exception e) {
                        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                            Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                            "delete file  threw an Exception");
                        }
                    }
                }
            }

            PushRegistryInternal.enablePushLaunch(true);
        }

        return info.id;
    }

    /**
     * Downloads the JAD, save it in the install state.
     * Parse the JAD, make sure it has
     * the required properties, and save them in the install state.
     *
     * @exception IOException is thrown, if an I/O error occurs during
     * descriptor or jar file download
     * @exception InvalidJadException is thrown, if the descriptor file is not
     * properly formatted or does not contain the required attributes
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     * @exception IllegalArgumentException is thrown, if the
     * descriptor file is not specified
     */
    private void installStep1()
        throws IOException, InvalidJadException, MIDletSuiteLockedException {        
        
        if (info.jadUrl == null || info.jadUrl.length() == 0) {
            throw
                new IllegalArgumentException("Must specify URL of .jad file");
        }
         
        try {          
            state.jad = downloadJAD();             
        } catch (OutOfMemoryError e) {
            try {
                postInstallMsgBackToProvider(
                    OtaNotifier.INSUFFICIENT_MEM_MSG);
            } catch (Throwable t) {                
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                    "Throwable during posting install message");
                }
            }

            throw new
                InvalidJadException(InvalidJadException.TOO_MANY_PROPS);
        }

        if (state.exception != null) {
            return;
        }

        state.jadProps = new JadProperties();
        try {
            state.jadProps.load(new ByteArrayInputStream(state.jad),
                                state.jadEncoding);
        } catch (OutOfMemoryError e) {
            state.jad = null;
            try {
                postInstallMsgBackToProvider(
                    OtaNotifier.INSUFFICIENT_MEM_MSG);
            } catch (Throwable t) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                    "Throwable during posting install message");
                }
            }

            throw new
                InvalidJadException(InvalidJadException.TOO_MANY_PROPS);
        } catch (InvalidJadException ije) {
            state.jad = null;           
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw ije;
        } catch(java.io.UnsupportedEncodingException uee) {
            state.jad = null;
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw new InvalidJadException(
                InvalidJadException.UNSUPPORTED_CHAR_ENCODING,
                    state.jadEncoding);
        }
        
        checkJadAttributes();
        assignNewId();
        checkPreviousVersion();
        state.nextStep++;
    }

    /**
     * If the JAD belongs to an installed suite, check the URL against the
     * installed one.
     */
    private void installStep2() {          
        
        state.nextStep++;      
        if (state.isPreviousVersion) {
            checkForDifferentDomains(info.jadUrl);
        }
    }

    /**
     * Makes sure the suite can fit in storage.
     *
     * @exception IOException is thrown, if an I/O error occurs during
     * descriptor or jar file download
     * @exception InvalidJadException is thrown, if the descriptor file is not
     * properly formatted or does not contain the required
     */
    private void installStep3()
            throws IOException, InvalidJadException {       
        String sizeString;
        int dataSize;
        int suiteSize;

        sizeString = state.jadProps.getProperty(MIDletSuite.JAR_SIZE_PROP);
        if (sizeString == null) {
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw new
                InvalidJadException(InvalidJadException.MISSING_JAR_SIZE);
        }

        try {
            info.expectedJarSize = Integer.parseInt(sizeString);
        } catch (NumberFormatException e) {
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw new
                InvalidJadException(InvalidJadException.INVALID_VALUE);
        }

        sizeString = state.jadProps.getProperty(MIDletSuite.DATA_SIZE_PROP);
        if (sizeString == null) {
            dataSize = 0;
        } else {
            try {
                dataSize = Integer.parseInt(sizeString);
            } catch (NumberFormatException e) {
                postInstallMsgBackToProvider(
                    OtaNotifier.INVALID_JAD_MSG);
                throw new
                    InvalidJadException(InvalidJadException.INVALID_VALUE);
            }
        }

        /*
         * A suite is a jad + jar + manifest + url + data size.
         * lets say the manifest is the same size as the jad
         * since we do know at this point. the size is in bytes,
         * UTF-8 chars can be upto 3 bytes
         */
        suiteSize = info.expectedJarSize + (state.jad.length * 2) +
                    (info.jadUrl.length() * 3) + dataSize;
        state.jad = null;

        state.file = new File();

        if (suiteSize > state.file.getBytesAvailableForFiles(state.storageId)) {
            postInstallMsgBackToProvider(
                OtaNotifier.INSUFFICIENT_MEM_MSG);

            // the size reported to the user should be in K and rounded up
            throw new
                InvalidJadException(InvalidJadException.INSUFFICIENT_STORAGE,
                    Integer.toString((suiteSize + 1023)/ 1024));
        }

        info.jarUrl = state.jadProps.getProperty(MIDletSuite.JAR_URL_PROP);
        
        if (info.jarUrl == null || info.jarUrl.length() == 0) {
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw new
                InvalidJadException(InvalidJadException.MISSING_JAR_URL);
        }

        state.nextStep++;
    }

    /**
     * Confirm installation with the user.
     *
     * @exception IOException is thrown, if the user cancels installation
     */
    private void installStep4()
            throws IOException {
        
        synchronized (state) {
            /* One more check to see if user has already canceled */
            if (state.stopInstallation) {
                postInstallMsgBackToProvider(
                    OtaNotifier.USER_CANCELLED_MSG);
                throw new IOException("stopped");
            }
            /*
             * Not canceled, so ignore cancel requests for now because below we
             * are going to ask anyway if user wants to install suite
             */
            state.ignoreCancel = true;
        }

        if (state.listener != null &&
            !state.listener.confirmJarDownload(state)) {
            state.stopInstallation = true;
            postInstallMsgBackToProvider(
                OtaNotifier.USER_CANCELLED_MSG);
            throw new IOException("stopped");
        }

        synchronized (state) {
            /* Allow cancel requests again */
            state.ignoreCancel = false;
        }
        state.nextStep++;
    }

    /**
     * Downloads the JAR, make sure it is the correct size, make sure
     * the required attributes match the JAD's. Then store the
     * application.
     *
     * @exception IOException is thrown, if an I/O error occurs during
     * descriptor or jar file download
     * @exception InvalidJadException is thrown, if the descriptor file is not
     * properly formatted or does not contain the required
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * already installed and is locked
     */
    private void installStep5()
            throws IOException, InvalidJadException, MIDletSuiteLockedException {
        int bytesDownloaded;
        MIDletInfo midletInfo;
        String midlet;
        
        // Send out delete notifications that have been queued, first
        OtaNotifier.postQueuedDeleteMsgsBackToProvider(state.proxyUsername,
            state.proxyPassword);

        // Save jar file to temp name; we need to do this to read the
        // manifest entry, but, don't want to overwrite an existing
        // application in case there are problems with the manifest
        state.storageRoot = File.getStorageRoot(state.storageId);
        info.jarFilename = state.storageRoot + TMP_FILENAME;

        bytesDownloaded = downloadJAR(info.jarFilename);

        if (state.exception != null) {
            return;
        }

        try {
            state.storage = new RandomAccessStream();

            state.installInfo.authPath =
                verifier.verifyJar(state.storage, info.jarFilename);

            if (state.listener != null) {
                state.listener.updateStatus(VERIFYING_SUITE, state);
            }

            // Create JAR Properties (From .jar file's MANIFEST)
            try {
                state.manifest = JarReader.readJarEntry(info.jarFilename,
                    MIDletSuite.JAR_MANIFEST);
                if (state.manifest == null) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.INVALID_JAR_MSG);
                    throw new
                        InvalidJadException(InvalidJadException.CORRUPT_JAR,
                                            MIDletSuite.JAR_MANIFEST);
                }
            } catch (OutOfMemoryError e) {
                try {
                    postInstallMsgBackToProvider(
                        OtaNotifier.INSUFFICIENT_MEM_MSG);
                } catch (Throwable t) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "Throwable during posting the install message");
                    }
                }

                throw new
                    InvalidJadException(InvalidJadException.TOO_MANY_PROPS);
            } catch (IOException ioe) {
                postInstallMsgBackToProvider(
                    OtaNotifier.INVALID_JAR_MSG);
                throw new
                    InvalidJadException(InvalidJadException.CORRUPT_JAR,
                                        MIDletSuite.JAR_MANIFEST);
            }

            state.jarProps = new ManifestProperties();

            try {
                state.jarProps.load(new ByteArrayInputStream(state.manifest));
                state.manifest = null;
            } catch (OutOfMemoryError e) {
                state.manifest = null;
                try {
                    postInstallMsgBackToProvider(
                        OtaNotifier.INSUFFICIENT_MEM_MSG);
                } catch (Throwable t) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "Throwable while posting install message ");
                    }
                }

                throw new
                    InvalidJadException(InvalidJadException.TOO_MANY_PROPS);
            } catch (InvalidJadException ije) {
                state.manifest = null;

                try {
                    postInstallMsgBackToProvider(
                        OtaNotifier.INVALID_JAR_MSG);
                } catch (Throwable t) {
                    // ignore
                }

                throw ije;
            }

            for (int i = 1; ; i++) {
                String key = "MIDlet-" + i;
                midlet = state.getAppProperty(key);
                if (midlet == null) {
                    break;
                }

                /*
                 * Verify the MIDlet class is present in the JAR
                 * An exception thrown if not.
                 * Do the proper install notify on an exception
                 */
                try {
                    midletInfo = new MIDletInfo(midlet);

                    verifyMIDlet(midletInfo.classname);
                } catch (InvalidJadException ije) {
                    if (ije.getReason() == InvalidJadException.INVALID_VALUE) {
                        // The MIDlet-n attribute may present in Manifest only
                        if (state.jadProps != null &&
                                state.jadProps.getProperty(key) != null) {
                            postInstallMsgBackToProvider(
                                OtaNotifier.INVALID_JAD_MSG);
                        } else {
                            postInstallMsgBackToProvider(
                                OtaNotifier.INVALID_JAR_MSG);
                        }
                    } else {
                        postInstallMsgBackToProvider(
                            OtaNotifier.INVALID_JAR_MSG);
                    }
                    throw ije;
                }
            }

            // Move on to the next step after a warning
            state.nextStep++;

            // Check Manifest entries against .jad file
            if (info.jadUrl != null) {
                if (bytesDownloaded != info.expectedJarSize) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.JAR_SIZE_MISMATCH_MSG);
                    throw new  InvalidJadException(
                        InvalidJadException.JAR_SIZE_MISMATCH);
                }

                if (!info.suiteName.equals(state.jarProps.getProperty(
                            MIDletSuite.SUITE_NAME_PROP))) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.ATTRIBUTE_MISMATCH_MSG);
                    throw new InvalidJadException(
                        InvalidJadException.SUITE_NAME_MISMATCH);
                }

                if (!info.suiteVersion.equals(
                        state.jarProps.getProperty(MIDletSuite.VERSION_PROP))) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.ATTRIBUTE_MISMATCH_MSG);
                    throw new InvalidJadException(
                         InvalidJadException.VERSION_MISMATCH);
                }

                if (!info.suiteVendor.equals(
                        state.jarProps.getProperty(MIDletSuite.VENDOR_PROP))) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.ATTRIBUTE_MISMATCH_MSG);
                    throw new InvalidJadException(
                         InvalidJadException.VENDOR_MISMATCH);
                }
            } else {
                info.expectedJarSize = bytesDownloaded;

                checkJarAttributes();
                assignNewId();

                // if already installed, check the domain of the JAR URL
                checkPreviousVersion();
            }
        } catch (Exception e) {
            state.file.delete(info.jarFilename);

            if (e instanceof IOException) {
                throw (IOException)e;
            }

            if (e instanceof MIDletSuiteLockedException) {
                throw (MIDletSuiteLockedException)e;
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }

            /* handle not RuntimeException-derived exceptions */
            throw new RuntimeException("Exception in installStep5(): " +
                                        e.getMessage());
        }
    }

    /**
     * If the JAR belongs to an installed suite if there was
     * no JAD, check the URL against the installed one.
     */
    private void installStep6() {
        state.nextStep++;
      
        if (info.jadUrl == null && state.isPreviousVersion) {
            checkForDifferentDomains(info.jarUrl);                     
        }
    }

    /**
     * If some additional (i.e. that are not listed in jad) permissions must
     * be allowed, add them to the value of MIDlet-Permissions attribute.
     */
    private void applyExtraPermissions() {
        if (additionalPermissions != null) {
            String newPermissions = state.jadProps.getProperty(
                MIDletSuite.PERMISSIONS_PROP);

            if (newPermissions != null && newPermissions.length() > 0) {
                newPermissions += ",";
            }

            if ("all".equals(additionalPermissions)) {
                int i;
                byte[] domainPermissions = Permissions.forDomain(
                    info.domain)[Permissions.MAX_LEVELS];

                newPermissions = "";

                for (i = 0; i < Permissions.NUMBER_OF_PERMISSIONS - 1; i++) {
                    if (domainPermissions[i] != Permissions.NEVER) {
                        newPermissions += Permissions.getName(i) + ",";
                    }
                }

                // the same for the last permission, but without ","
                if (domainPermissions[i] != Permissions.NEVER) {
                    newPermissions += Permissions.getName(i);
                }
            } else {
                if (newPermissions == null) {
                    newPermissions = additionalPermissions; 
                } else {
                    newPermissions += additionalPermissions;
                }
            }

            state.jadProps.setProperty(MIDletSuite.PERMISSIONS_PROP,
                        newPermissions);

            /*
             * If the Midlet-Permissions attribute presents in there
             * manifest, it must be the same as in jad because the suite
             * is trusted.
             */
            String jarPermissions = state.jarProps.getProperty(
                MIDletSuite.PERMISSIONS_PROP);

            if (jarPermissions != null) {
                state.jarProps.setProperty(MIDletSuite.PERMISSIONS_PROP,
                                           newPermissions);
            }
        }
    }

    /**
     * Checks the permissions and store the suite.
     *
     * @exception IOException is thrown, if an I/O error occurs during
     * storing the suite
     * @exception InvalidJadException is thrown, if the there is
     * permission problem
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     */
    private void installStep7() throws IOException,
            InvalidJadException, MIDletSuiteLockedException {

        try {
            if (info.authPath != null) {
                // suite was signed
                info.domain = verifier.getSecurityDomainName(info.authPath[0]);
                if (state.listener != null &&
                    !state.listener.confirmAuthPath(state)) {
                    state.stopInstallation = true;
                    postInstallMsgBackToProvider(
                        OtaNotifier.USER_CANCELLED_MSG);
                    throw new IOException("stopped");
                }
            } else {
                info.domain = unsignedSecurityDomain;
            }

            info.trusted = Permissions.isTrusted(info.domain);

            // Do not overwrite trusted suites with untrusted ones
            if (!info.trusted && state.isPreviousVersion &&
                    state.previousSuite.isTrusted()) {

                postInstallMsgBackToProvider(
                    OtaNotifier.AUTHORIZATION_FAILURE_MSG);

                /*
                 * state.previousInstallInfo.authPath can be null in the case
                 * if the previously installed suite was not signed but its
                 * domain was set to some trusted one by AutoTester using
                 * setUnsignedSecurityDomain().
                 */
                throw new InvalidJadException(
                    InvalidJadException.TRUSTED_OVERWRITE_FAILURE,
                        state.previousInstallInfo.authPath != null ?
                            state.previousInstallInfo.authPath[0] : "");
            }

            /*
             * The unidentified suites do not get checked for requested
             * permissions.
             */
            if (!Permissions.isTrusted(info.domain)) {

                settings.setPermissions((Permissions.forDomain(
                    info.domain)) [Permissions.CUR_LEVELS]);

                /*
                 * To keep public key management simple, there is only one
                 * trusted keystore. So it is possible that the CA for
                 * the suite is untrusted. This may be done on purpose for
                 * testing. This is OK, but do not confuse the user by saying
                 * the untrusted suite is authorized, so set the CA name to
                 * null.
                 */
                info.authPath = null;
            } else {
                /*
                 * For identified suites, make sure an properties duplicated in
                 * both the manifest and JAD are the same.
                 */
                if (info.jadUrl != null) {
                    checkForJadManifestMismatches();

                    /*
                     * Check that if MIDlet-Permissions[-Opt] presents in jad
                     * then it also presents in the manifest (their equality
                     * was already checked by checkForJadManifestMismatches()).
                     */
                    String[] keys = {
                        MIDletSuite.PERMISSIONS_PROP,
                        MIDletSuite.PERMISSIONS_OPT_PROP
                    };

                    for (int i = 0; i < keys.length; i++) {
                        if (state.jadProps.getProperty(keys[i]) != null) {
                            if (state.jarProps.getProperty(keys[i]) == null) {
                                postInstallMsgBackToProvider(
                                    OtaNotifier.ATTRIBUTE_MISMATCH_MSG);
                                throw new InvalidJadException(
                                    InvalidJadException.ATTRIBUTE_MISMATCH,
                                        keys[i]);
                            }
                        }
                    }

                    /*
                     * Check that if extended properties are present in jad
                     * then they aare lso present in the manifest (their equality
                     * was already checked by checkForJadManifestMismatches()).
                     */
                    String[] extKeys = {
                        MIDletSuite.HEAP_SIZE_PROP,
                        MIDletSuite.BACKGROUND_PAUSE_PROP,
                        MIDletSuite.NO_EXIT_PROP,
                        MIDletSuite.LAUNCH_BG_PROP,
                        MIDletSuite.LAUNCH_POWER_ON_PROP
                    };

                    int midletNum = state.getNumberOfMIDlets();
                    for (int i = 0; i < extKeys.length; i++) {
                        for (int j = 1; j <= midletNum; j++) {
                            String extKey = extKeys[i] + "-" + j;
                            if (state.jadProps.getProperty(extKey) != null) {
                                if (state.jarProps.getProperty(extKey) == null) {
                                    postInstallMsgBackToProvider(
                                        OtaNotifier.ATTRIBUTE_MISMATCH_MSG);
                                    throw new InvalidJadException(
                                        InvalidJadException.ATTRIBUTE_MISMATCH,
                                            extKey);
                                }
                            }
                        }
                    }

                }

                /*
                 * This is needed by the AutoTester: sometimes it is required
                 * to allow some permissions even if they are not listed in jad.
                 */
                applyExtraPermissions();

                settings.setPermissions(getInitialPermissions(info.domain));
            }

            if (state.isPreviousVersion) {
                applyCurrentUserLevelPermissions(
                    state.previousSuite.getPermissions(),
                    (Permissions.forDomain(info.domain))
                        [Permissions.MAX_LEVELS],
                    settings.getPermissions());

                if (state.removeRMS) {
                    // override suite comparisons, just remove RMS
                    RecordStoreFactory.removeRecordStoresForSuite(null,
                        info.id);
                } else {
                    processPreviousRMS();
                }
            }

            state.securityHandler = new SecurityHandler(
                settings.getPermissions(), info.domain);

            checkRuntimeEnv();
            checkConfiguration();
            matchProfile();

            try {
                state.chmanager.preInstall(this,
                       (InstallState)state,
                       (MIDletSuite)state,
                       (info.authPath == null ?
                           null : info.authPath[0]));
            } catch (InvalidJadException jex) {
                // Post the correct install notify msg back to the server
                String msg = OtaNotifier.INVALID_CONTENT_HANDLER;
                if (jex.getReason() ==
                    InvalidJadException.CONTENT_HANDLER_CONFLICT) {
                    msg = OtaNotifier.CONTENT_HANDLER_CONFLICT;
                }

                postInstallMsgBackToProvider(msg);
                throw jex;
            } catch (SecurityException se) {
                postInstallMsgBackToProvider(
                    OtaNotifier.AUTHORIZATION_FAILURE_MSG);

                // since our state object put the permission in message
                throw new InvalidJadException(
                    InvalidJadException.AUTHORIZATION_FAILURE,
                    se.getMessage());
            }

            // make sure at least 1 second has passed
            try {
                long waitTime = 1000 -
                    (System.currentTimeMillis() - state.startTime);

                if (waitTime > 0 && waitTime <= 1000) {
                    Thread.sleep(waitTime);
                }
            } catch (InterruptedException ie) {
                // ignore
            }

            synchronized (state) {
                // this is the point of no return, one last check
                if (state.stopInstallation) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.USER_CANCELLED_MSG);
                    throw new IOException("stopped");
                }

                state.ignoreCancel = true;
            }

            if (state.listener != null) {
                state.listener.updateStatus(STORING_SUITE, state);
            }

            registerPushConnections();

            /** Do the Content Handler registration updates now */
            state.chmanager.install();

            /*
             * Store suite will remove the suite including push connections,
             * if there an error, but may not remove the temp jar file.
             */
            storeUnit();
        } catch (Throwable e) {
            state.file.delete(info.jarFilename);
            
            if (e instanceof IOException) {
                throw (IOException)e;
            }

            if (e instanceof MIDletSuiteLockedException) {
                throw (MIDletSuiteLockedException)e;
            }

            if (e instanceof OutOfMemoryError) {
                try {
                    postInstallMsgBackToProvider(
                        OtaNotifier.INSUFFICIENT_MEM_MSG);
                } catch (Throwable t) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "Throwable during posting install message");
                    }
                }

                throw new
                    InvalidJadException(InvalidJadException.TOO_MANY_PROPS);
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }

            /* handle not RuntimeException-derived exceptions */
            throw new RuntimeException("Exception in installStep7(): " +
                                        e.getMessage());
        }

        state.nextStep++;

        try {
            postInstallMsgBackToProvider(OtaNotifier.SUCCESS_MSG);
        } catch (Throwable t) {
            /*
             * The suite is successfully installed, but the post of the
             * status message failed. Do not let this failure prevent
             * the suite from being used.
             */
        }
    }

    /**
     * Installation time optimizations are to be done in this step.
     * MONET optimization, VERIFY ONCE optimization, etc.
     * This step is done after obligatory installation part,
     * so the suite is downloaded, checked and stored by this moment.
     * 
     * @exception IOException is thrown, if an I/O error occurs during
     * MONET image creation or classes verification
     */
    private void installStep8() throws IOException {

        // In case of installation from JAR the suite id as well as other
        // properties are read from .jar file's MANIFEST rather then from
        // JAD file. Only after that application image can be generated.
        if (Constants.MONET_ENABLED) {
            if (state.listener != null) {
                state.listener.updateStatus(GENERATING_APP_IMAGE, state);
            }
            MIDletAppImageGenerator.createAppImage(
                info.id, state.midletSuiteStorage);
        }
        // Verification caching should be done if MONET disabled only
        else if (Constants.VERIFY_ONCE) {
            if (state.listener != null) {
                state.listener.updateStatus(VERIFYING_SUITE_CLASSES, state);
            }
            // Preverify all suite classes
            // in the case of success store hash value of the suite
            try {
                info.verifyHash =
                    MIDletSuiteVerifier.verifySuiteClasses(info.id,
                        state.midletSuiteStorage);
                if (info.verifyHash != null) {
                    state.midletSuiteStorage.storeSuiteVerifyHash(
                        info.id, info.verifyHash);
                }
            } catch (Throwable t) {
                // Notify installation listener of verifcation error
                state.exception = new InvalidJadException(
                    InvalidJadException.JAR_CLASSES_VERIFICATION_FAILED);
                if (state.listener != null) {
                    state.listener.updateStatus(
                        VERIFYING_SUITE_CLASSES, state);
                }

                // Clean exception since this step is optional and its
                // problems shouldn't cause whole installation failure
                state.exception = null;
            }
        }

        state.nextStep++;
    }

    /**
     * Verify that a class is present in the JAR file.
     * If the classname is invalid or is not found an
     * InvalidJadException is thrown.
     * @param classname the name of the class to verify
     * @exception InvalidJadException is thrown if the name is null or empty
     * or if the file is not found
     */
    public void verifyMIDlet(String classname) throws InvalidJadException {
        if (classname == null ||
            classname.length() == 0) {
            throw new
                InvalidJadException(InvalidJadException.INVALID_VALUE);
        }

        String file = classname.replace('.', '/').concat(".class");

        try {
            /* Attempt to read the MIDlet from the JAR file. */
            if (JarReader.readJarEntry(info.jarFilename, file) != null) {
                return;                // File found, normal return
            }
            // Fall into throwing the exception
        } catch (IOException ioe) {
            // Fall into throwing the exception
        }
        // Throw the InvalidJadException
        throw new InvalidJadException(InvalidJadException.CORRUPT_JAR, file);
    }

    /**
     * Downloads an application descriptor file from the given URL.
     *
     * @return a byte array representation of the file or null if not found
     *
     * @exception IOException is thrown if any error prevents the download
     *   of the JAD
     */
    protected abstract byte[] downloadJAD() throws IOException;

    /**
     * Downloads an application archive file from the given URL into the
     * given file. Automatically handle re-tries.
     *
     * @param filename name of the file to write. This file resides
     *          in the storage area of the given application
     *
     * @return size of the JAR
     *
     * @exception IOException is thrown if any error prevents the download
     *   of the JAR
     */
    protected abstract int downloadJAR(String filename) throws IOException;

    /**
     * Checks that all necessary attributes are present in JAD and are valid.
     *
     * May be overloaded by subclasses that require presence of
     * different attributes in JAD during the installation.
     *
     * @throws InvalidJadException if any mandatory attribute is missing in
     *                             the JAD file or its value is invalid
     */
    protected void checkJadAttributes() throws InvalidJadException {
        info.suiteName = state.jadProps.getProperty(
            MIDletSuite.SUITE_NAME_PROP);
        if (info.suiteName == null || info.suiteName.length() == 0) {
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw new
                InvalidJadException(InvalidJadException.MISSING_SUITE_NAME);
        }

        info.suiteVendor = state.jadProps.getProperty(MIDletSuite.VENDOR_PROP);
        if (info.suiteVendor == null || info.suiteVendor.length() == 0) {
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw new
                InvalidJadException(InvalidJadException.MISSING_VENDOR);
        }

        info.suiteVersion = state.jadProps.getProperty(
            MIDletSuite.VERSION_PROP);
        if (info.suiteVersion == null || info.suiteVersion.length() == 0) {
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw new
                InvalidJadException(InvalidJadException.MISSING_VERSION);
        }

        try {
            checkVersionFormat(info.suiteVersion);
        } catch (NumberFormatException nfe) {
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw new InvalidJadException(
                  InvalidJadException.INVALID_VERSION);
        }
    }

    /**
     * Checks that all necessary attributes are present in the manifest
     * in the JAR file and are valid.
     *
     * May be overloaded by subclasses that require presence of
     * different attributes in manifest during the installation.
     *
     * @throws InvalidJadException if any mandatory attribute is missing in
     *                             the manifest or its value is invalid
     */
    protected void checkJarAttributes() throws InvalidJadException {
        /*
         * Check MIDlet-* attributes only if this is
         * not a dynamic component.
         */
        info.suiteName = state.jarProps.getProperty(
            MIDletSuite.SUITE_NAME_PROP);
        if (info.suiteName == null ||
                info.suiteName.length() == 0) {
            postInstallMsgBackToProvider(
                OtaNotifier.INVALID_JAR_MSG);
            throw new InvalidJadException(
                 InvalidJadException.MISSING_SUITE_NAME);
        }

        info.suiteVendor = state.jarProps.getProperty(
            MIDletSuite.VENDOR_PROP);
        if (info.suiteVendor == null ||
                info.suiteVendor.length() == 0) {
            postInstallMsgBackToProvider(
                OtaNotifier.INVALID_JAR_MSG);
            throw new InvalidJadException(
                 InvalidJadException.MISSING_VENDOR);
        }

        info.suiteVersion = state.jarProps.getProperty(
            MIDletSuite.VERSION_PROP);
        if (info.suiteVersion == null ||
                info.suiteVersion.length() == 0) {
            postInstallMsgBackToProvider(
                OtaNotifier.INVALID_JAR_MSG);
            throw new InvalidJadException(
                 InvalidJadException.MISSING_VERSION);
        }

        try {
            checkVersionFormat(info.suiteVersion);
        } catch (NumberFormatException nfe) {
            postInstallMsgBackToProvider(
                OtaNotifier.INVALID_JAR_MSG);
            throw new InvalidJadException(
                 InvalidJadException.INVALID_VERSION);
        }
    }

    /**
     * Assigns a new ID to the midlet suite being installed.
     * May be overloaded by subclasses that use different storages.
     */
    protected void assignNewId() {
        info.id = state.midletSuiteStorage.createSuiteID();
    }

    /**
     * Stores the midlet suite being installed in the midlet suite storage.
     *
     * @throws IOException if an I/O error occured when storing the suite
     * @throws MIDletSuiteLockedException if the suite is locked
     */
    protected void storeUnit() throws IOException, MIDletSuiteLockedException {
        MIDletInfo midletInfo = state.getMidletInfo();
        String midletClassNameToRun = null, iconName;

        iconName = state.getAppProperty("MIDlet-Icon");
        if (iconName != null) {
            iconName = iconName.trim();
        }

        if (midletInfo != null) {
            midletClassNameToRun = midletInfo.classname;
            if (iconName == null) {
                // If an icon for the suite is not specified,
                // use the first midlet's icon.
                iconName = midletInfo.icon;
            }
        }

        MIDletSuiteInfo msi = new MIDletSuiteInfo(info.id);

        msi.displayName = state.getDisplayName();
        msi.midletToRun = midletClassNameToRun;
        msi.numberOfMidlets = state.getNumberOfMIDlets();
        /* default is to enable a newly installed suite */
        msi.enabled = true;
        msi.trusted = info.trusted;
        msi.preinstalled = false;
        msi.iconName = iconName;
        msi.storageId = state.storageId;

        state.midletSuiteStorage.storeSuite(
                info, settings, msi, state.jadProps, state.jarProps);
    }

    /**
     * If the JAD belongs to an installed suite, check the URL against the
     * installed one. Set the state.exception if the user needs to be warned.
     *
     * @param url JAD or JAR URL of the suite being installed
     */
    protected void checkForDifferentDomains(String url) {
        String previousUrl = state.previousInstallInfo.getDownloadUrl();
        // perform a domain check not a straight compare
        if (info.authPath == null && previousUrl != null) {
            HttpUrl old = new HttpUrl(previousUrl);
            HttpUrl current = new HttpUrl(url);

            if ((current.domain != null && old.domain == null) ||
                (current.domain == null && old.domain != null) ||
                (current.domain != null && old.domain != null &&
                 !current.domain.regionMatches(true, 0, old.domain, 0,
                                           old.domain.length()))) {
                /*
                 * The jad is at new location, could be bad,
                 * let the user decide
                 */
                state.exception = new InvalidJadException(
                    InvalidJadException.JAD_MOVED, previousUrl);
                return;
            }
        }
    }

    /**
     * See if there is an installed version of the suite being installed and
     * if so, make an necessary checks. Will set state fields, including
     * the exception field for warning the user.
     *
     * @exception InvalidJadException if the new version is formated
     * incorrectly
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     */
    protected void checkPreviousVersion()
        throws InvalidJadException, MIDletSuiteLockedException {

        int id;
        MIDletSuiteImpl midletSuite;
        String installedVersion;
        int cmpResult;

        state.isPreviousVersion = false;
        state.previousSuite = null;
        state.previousInstallInfo = null;
        
        // Check if app already exists
        id = MIDletSuiteStorage.getSuiteID(info.suiteVendor,
                                           info.suiteName);
        if (id == MIDletSuite.UNUSED_SUITE_ID) {
            // there is no previous version
            return;
        }

        try {
            midletSuite =
              state.midletSuiteStorage.getMIDletSuite(id, true);

            if (midletSuite == null) {
                // there is no previous version
                return;
            }
            checkVersionFormat(info.suiteVersion);

            state.isPreviousVersion = true;

            // This is now an update, use the old ID
            info.id = id;

            state.previousSuite = midletSuite;
            state.previousInstallInfo = midletSuite.getInstallInfo();

            if (state.force) {
                // do not ask questions, force an overwrite
                return;
            }

            // If it does, check version information
            installedVersion = midletSuite.getProperty(
                MIDletSuite.VERSION_PROP);
            cmpResult = vercmp(info.suiteVersion,
                               installedVersion);
            if (cmpResult < 0) {
                // older version, warn user
                state.exception = new InvalidJadException(
                                  InvalidJadException.OLD_VERSION,
                                  installedVersion);
                return;
            }

            if (cmpResult == 0) {
                // already installed, warn user
                state.exception = new InvalidJadException(
                                  InvalidJadException.ALREADY_INSTALLED,
                                  installedVersion);
                return;
            }

            // new version, warn user
            state.exception = new InvalidJadException(
                                  InvalidJadException.NEW_VERSION,
                                  installedVersion);
        } catch (MIDletSuiteCorruptedException mce) {
            if (state.listener != null) {
                state.listener.updateStatus(CORRUPTED_SUITE, state);
            }
        } catch (NumberFormatException nfe) {
            postInstallMsgBackToProvider(
                OtaNotifier.INVALID_JAD_MSG);
            throw new
                InvalidJadException(InvalidJadException.INVALID_VERSION);
        } catch (MIDletSuiteLockedException msle) {
            // this was an attempt to update a locked suite, set the correct ID
            info.id = id;
            throw msle;
        }
    }

    /**
     * Posts a status message back to the provider's URL in JAD.
     *
     * @param message status message to post
     */
    protected void postInstallMsgBackToProvider(String message) {       
        OtaNotifier.postInstallMsgBackToProvider(message, state,
            state.proxyUsername, state.proxyPassword);
    }

    /**
     * Function that actually does the work of transferring file data.
     * <p>
     * Updates the listener every 1 K bytes.
     * <p>
     * If the amount of data to be read is larger than <code>maxDLSize</code>
     * we will break the input into chunks no larger than
     * <code>chunkSize</code>. This prevents the VM from running out of
     * memory when processing large files.
     *
     * @param in the input stream to read from
     * @param out the output stream to write to
     * @param chunkSize size of piece to read from the input buffer
     *
     * @return number of bytes written to the output stream
     *
     * @exception IOException if any exceptions occur during transfer
     * of data
     */
    protected int transferData(InputStream in, OutputStream out, int chunkSize)
            throws IOException {
        byte[] buffer = new byte[chunkSize];
        int bytesRead;
        int totalBytesWritten = 0;

        if (state.listener != null) {
            state.listener.updateStatus(state.beginTransferDataStatus, state);
        }

        try {
            for (int nextUpdate = totalBytesWritten + 1024; ; ) {
                bytesRead = in.read(buffer);

                if (state.listener != null && (bytesRead == -1 ||
                        totalBytesWritten + bytesRead >= nextUpdate)) {

                    synchronized (state) {
                        if (state.stopInstallation) {
                            throw new IOException("stopped");
                        }

                        state.listener.updateStatus(state.transferStatus,
                                                    state);
                    }

                    nextUpdate = totalBytesWritten + 1024;
                }

                if (bytesRead == -1) {
                    return totalBytesWritten;
                }

                out.write(buffer, 0, bytesRead);
                totalBytesWritten += bytesRead;
            }
        } catch (IOException ioe) {
            if (state.stopInstallation) {
                postInstallMsgBackToProvider(
                    OtaNotifier.USER_CANCELLED_MSG);
                throw new IOException("stopped");
            } else {
                throw ioe;
            }
        }
    }

    /**
     * Retrieves a scheme component of the given URL.
     *
     * @param url url to parse
     * @param defaultScheme if the url has no scheme component, this one
     *                      will be returned; may be null
     *
     * @return scheme component of the given URL
     */
    public static String getUrlScheme(String url, String defaultScheme) {
        if (url == null) {
            return null;
        }

        /* this will parse any kind of URL, not only Http */
        HttpUrl parsedUrl = new HttpUrl(url);

        if (parsedUrl.scheme == null) {
            return defaultScheme;
        }

        return parsedUrl.scheme;
    }

    /**
     * Retrieves a path component of the given URL.
     *
     * @param url url to parse
     *
     * @return path component of the given URL
     */
    public static String getUrlPath(String url) {
        if (url == null) {
            return null;
        }

        /* this will parse any kind of URL, not only Http */
        HttpUrl parsedUrl = new HttpUrl(url);
        String path = parsedUrl.path;

        /*
           IMPL_NOTE: In current implementation of HttpUrl
               the absolute path always begins with '/' which
               would make getUrlPath() produce the win32
               paths in the form "/C:/path/to/file" that is
               rejected by the filesystem.
               The initial '/' in 'path' is currently the only
               flag which allows to distinguish between absolute
               and relative url.
               Probably there should be a special flag in HttpUrl
               to distinguish between absolute and relative urls.
               Moreover it seems necessary to have platform-dependent
               conversion procedure from url path to filesystem path.
        */

        if (path != null) {
            if (path.charAt(0) == '/') {
                path = path.substring (1, path.length ());
            }
        }

        return path;
    }

    /**
     * Compares two URLs for equality in sense that they have the same
     * scheme, host and path.
     *
     * @param url1 the first URL for comparision
     * @param url2 the second URL for comparision
     *
     * @return true if the scheme, host and path of the first given url
     *              is identical to the scheme, host and path of the second
     *              given url; false otherwise
     */
    protected abstract boolean isSameUrl(String url1, String url2);

    /**
     * The method checks if it's necessary to clean up file connection
     * data upon suite.
     *
     * It detects whether JSR-75 is included, if so, invokes dedicated JSR's
     * class.
     *
     * @param suiteId ID of suite to check data presence for
     * @return true if JSR data exists for the suite, false otherwise
     */
    private static boolean FileConnectionHasPrivateData(int suiteId) {
        FileConnectionCleanup fcc;

        try {
            Class fccClass =
                Class.forName("com.sun.midp.jsr075.FileConnectionCleanupImpl");
            fcc = (FileConnectionCleanup)(fccClass.newInstance());
        } catch (ClassNotFoundException cnfe){
            return false;
        } catch (IllegalAccessException iae) {
            return false;
        } catch (InstantiationException ie) {
            return false;
        }

        return fcc.suiteHasPrivateData(suiteId);
    }

    /**
     * If this is an update, make sure the RMS data is handle correctly
     * according to the OTA spec.
     * <p>
     * From the OTA spec:
     * <blockquote>
     * The RMS record stores of a MIDlet suite being updated MUST be
     * managed as follows:</p>
     * <ul>
     * <li>
     *   If the cryptographic signer of the new MIDlet suite and the
     *   original MIDlet suite are identical, then the RMS record
     *   stores MUST be retained and made available to the new MIDlet
     *   suite.</li>
     * <li>
     *   If the scheme, host, and path of the URL that the new
     *   Application Descriptor is downloaded from is identical to the
     *   scheme, host, and path of the URL the original Application
     *   Descriptor was downloaded from, then the RMS MUST be retained
     *   and made available to the new MIDlet suite.</li>
     * <li>
     *   If the scheme, host, and path of the URL that the new MIDlet
     *   suite is downloaded from is identical to the scheme, host, and
     *   path of the URL the original MIDlet suite was downloaded from,
     *   then the RMS MUST be retained and made available to the new
     *   MIDlet suite.</li>
     * <li>
     *   If the above statements are false, then the device MUST ask
     *   the user whether the data from the original MIDlet suite
     *   should be retained and made available to the new MIDlet
     *   suite.</li>
     * </ul>
     * </blockquote>
     *
     * @exception IOException if the install is stopped
     */
    protected void processPreviousRMS() throws IOException {
        if (!RecordStoreFactory.suiteHasRmsData(info.id) &&
                !FileConnectionHasPrivateData(info.id)) {
            return;
        }

        if (state.previousInstallInfo.authPath != null &&
            info.authPath != null &&
            info.authPath[0].equals(
                state.previousInstallInfo.authPath[0])) {
            // signers the same
            return;
        }

        if (isSameUrl(info.jadUrl, state.previousInstallInfo.getJadUrl()) ||
            isSameUrl(info.jarUrl, state.previousInstallInfo.getJarUrl())) {
            return;
        }

        // ask the user, if no listener assume no for user's answer
        if (state.listener != null) {
            if (state.listener.keepRMS(state)) {
                // user wants to keep the data
                return;
            }
        }

        // this is a good place to check for a stop installing call
        if (state.stopInstallation) {
            postInstallMsgBackToProvider(
                OtaNotifier.USER_CANCELLED_MSG);
            throw new IOException("stopped");
        }

        RecordStoreFactory.removeRecordStoresForSuite(null, info.id);
    }

    /**
     * Stops the installation. If installer is not installing then this
     * method has no effect. This will cause the install method to
     * throw an IOException if the install is not writing the suite
     * to storage which is the point of no return.
     *
     * @return true if the install will stop, false if it is too late
     */
    public boolean stopInstalling() {
        if (state == null) {
            return false;
        }

        synchronized (state) {
            if (state.ignoreCancel) {
                return false;
            }

            state.stopInstallation = true;
        }

        return true;
    }

    /**
     * Tells if the installation was stopped by another thread.
     * @return true if the installation was stopped by another thread
     */
    public boolean wasStopped() {
        if (state == null) {
            return false;
        }

        return state.stopInstallation;
    }

    /**
     * Builds the initial API permission for suite currently being installed.
     *
     * @param domain security domain name for the CA of the suite
     *
     * @return current level of permissions
     *
     * @exception InvalidJadException if a permission attribute is not
     *     formatted properly or a required permission is denied
     */
    protected byte[] getInitialPermissions(String domain)
            throws InvalidJadException {
        byte[][] domainPermissions = Permissions.forDomain(domain);
        byte[] permissions = Permissions.getEmptySet();

        // only the current level of each permission has to be adjusted
        getRequestedPermissions(MIDletSuite.PERMISSIONS_PROP,
                                domainPermissions[Permissions.CUR_LEVELS],
                                permissions, true);

        getRequestedPermissions(MIDletSuite.PERMISSIONS_OPT_PROP,
                                domainPermissions[Permissions.CUR_LEVELS],
                                permissions, false);

        return permissions;
    }

    /**
     * Gets the permissions for a domain that are requested the manifest.
     *
     * @param propName name of the property in the manifest
     * @param domainPermissions array of the starting levels for permissions
     *        of a domain
     * @param permissions array to put the permissions from the domain in
     *        when found in the manifest property
     * @param required if set to true the manifest permissions are required
     *
     * @exception InvalidJadException if a permission attribute is not
     *     formatted properly or a required permission is denied
     */
    private void getRequestedPermissions(String propName,
            byte[] domainPermissions, byte[] permissions, boolean required)
            throws InvalidJadException {

        String reqPermissionLine;
        Vector reqPermissions;
        String permission;
        boolean found;
        int i;

        reqPermissionLine = state.getAppProperty(propName);
        if (reqPermissionLine == null || reqPermissionLine.length() == 0) {
            // Zero properties are allowed.
            return;
        }

        reqPermissions = Util.getCommaSeparatedValues(reqPermissionLine);
        if (reqPermissions.size() == 0) {
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAD_MSG);
            throw new InvalidJadException(InvalidJadException.INVALID_VALUE);
        }

        for (int j = 0; j < reqPermissions.size(); j++) {
            permission = (String)reqPermissions.elementAt(j);

            if (permission.length() == 0) {
                postInstallMsgBackToProvider(
                    OtaNotifier.INVALID_JAD_MSG);
                throw new
                    InvalidJadException(InvalidJadException.INVALID_VALUE);
            }

            found = false;
            for (i = 0; i < Permissions.NUMBER_OF_PERMISSIONS; i++) {
                if (Permissions.getName(i).equals(permission)) {
                    if (domainPermissions[i] != Permissions.NEVER) {
                        found = true;
                    }

                    break;
                }
            }

            if (!found) {
                if (required) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.AUTHORIZATION_FAILURE_MSG);
                    throw new InvalidJadException(
                        InvalidJadException.AUTHORIZATION_FAILURE, permission);
                }

                continue;
            }

            permissions[i] = domainPermissions[i];
        }
    }

    /**
     * Apply the previous user level permission of the currently installed
     * version of a suite to the next version of the suite in a secure way.
     *
     * @param current array permissions for the current version
     * @param domainPermissions array of the starting levels for permissions
     *        of the new domain
     * @param next array permissions for the next version
     */
    private void applyCurrentUserLevelPermissions(byte[] current,
            byte[] domainPermissions, byte[] next) {

        for (int i = 0; i < current.length && i < next.length; i++) {
            switch (current[i]) {
            case Permissions.ALLOW:
            case Permissions.NEVER:
                // not a user level permission
                continue;
            }

            switch (domainPermissions[i]) {
            case Permissions.ALLOW:
            case Permissions.NEVER:
                // not a user level permission
                continue;

            case Permissions.ONESHOT:
                if (current[i] == Permissions.SESSION) {
                    // do not apply
                    continue;
                }
                // fall through; per-session permissions may be permitted.

            case Permissions.SESSION:
                if (current[i] == Permissions.BLANKET ||
                    current[i] == Permissions.BLANKET_GRANTED) {
                    // do not apply
                    continue;
                }
                // fall through to store the permission for the next version.

            default:
                next[i] = current[i];
                continue;
            }
        }
    }

    /**
     * Checks to see if the JAD has a signature, but does not verify the
     * signature. This is a place holder the the Secure Installer and
     * just returns false.
     *
     * @return true if the JAD has a signature
     */
    public boolean isJadSigned() {
        return verifier.isJadSigned();
    }

    /**
     * Checks if the calling suite has com.sun.midp.midletsuite.midp
     * permission.
     *
     * @exception SecurityException if suite does not have the required
     *            permission
     */
    private void checkAmsPermission() throws SecurityException {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);
    }

    /**
     * Sets security domain for unsigned suites. The default is untrusted.
     * Can only be called by JAM for testing.
     *
     * @param domain name of a security domain
     * if they are absent from the jad file; "all" to allow all permissions
     */
    public void setUnsignedSecurityDomain(String domain) {
        checkAmsPermission();
        unsignedSecurityDomain = domain;
    }

    /**
     * Sets the permissions that must be allowed not depending on their
     * presence in the application descriptor file.
     * Can only be called by JAM for testing.
     *
     * @param extraPermissions list of permissions that must be allowed even
     * if they are absent from the jad file; "all" to allow all permissions
     */
    public void setExtraPermissions(String extraPermissions) {
        checkAmsPermission();
        additionalPermissions = extraPermissions;
    }

    /**
     * Checks to see that if any properties that are both in the JAD and
     * JAR manifest are not equal and throw a exception and notify the
     * server when a mismatch is found. Only used for trusted suites.
     * @exception InvalidJadException  if the properties do not match
     */
    protected void checkForJadManifestMismatches()
            throws InvalidJadException {

        for (int i = 0; i < state.jarProps.size(); i++) {
            String key = state.jarProps.getKeyAt(i);
            String value = state.jarProps.getValueAt(i);
            String dup = state.jadProps.getProperty(key);

            if (dup == null) {
                continue;
            }

            if (!dup.equals(value)) {
                postInstallMsgBackToProvider(
                    OtaNotifier.ATTRIBUTE_MISMATCH_MSG);
                throw new InvalidJadException(
                    InvalidJadException.ATTRIBUTE_MISMATCH, key);
            }
        }
    }

    /**
     * Compares two version strings. The return values are very similar to
     * that of strcmp() in 'C'. If the first version is less than the second
     * version, a negative number will be returned. If the first version is
     * greater than the second version, a positive number will be returned.
     * If the two versions are equal, zero is returned.
     * <p>
     * Versions must be in the form <em>xxx.yyy.zzz</em>, where:
     * <pre>
     *     <em>xxx</em> is the major version
     *     <em>yyy</em> is the minor version
     *     <em>zzz</em> is the micro version
     * </pre>
     * It is acceptable to omit the micro and possibly the minor versions.
     * If these are not included in the version string, the period immediately
     * preceding the number must also be removed. So, the versions
     * <em>xxx.yyy</em> or <em>xxx</em> are also valid.
     * <p>
     * Version numbers do not have to be three digits wide. However, you may
     * pad versions with leading zeros if desired.
     * <p>
     * If a version number is omitted, its value is assumed to be zero. All
     * tests will be based on this assumption.
     * <p>
     * For example:
     * <pre>
     *    1.04  >  1.
     *    1.04  <  1.4.1
     *    1.04  =  1.4.0
     * </pre>
     * <p>
     *
     * @param ver1 the first version to compare
     * @param ver2 the second version to compare
     *
     * @return  1 if <code>ver1</code> is greater than <code>ver2</code>
     *          0 if <code>ver1</code> is equal to <code>ver2</code>
     *         -1 if <code>ver1</code> is less than <code>ver2</code>
     *
     * @exception NumberFormatException if either <code>ver1</code> or
     * <code>ver2</code> contain characters that are not numbers or periods
     */
    protected static int vercmp(String ver1, String ver2)
            throws NumberFormatException {
        String strVal1;
        String strVal2;
        int    intVal1;
        int    intVal2;
        int    idx1 = 0;
        int    idx2 = 0;
        int    newidx;

        if ((ver1 == null) && (ver2 == null)) {
            return 0;
        }

        if (ver1 == null) {
            return -1;
        }

        if (ver2 == null) {
            return 1;
        }

        for (int i = 0; i < 3; i++) {
            strVal1 = "0"; // Default value
            strVal2 = "0"; // Default value
            if (idx1 >= 0) {
                newidx = ver1.indexOf('.', idx1);
                if (newidx < 0) {
                    strVal1 = ver1.substring(idx1);
                } else {
                    strVal1 = ver1.substring(idx1, newidx);
                    newidx++; // Idx of '.'; need to go to next char
                }

                idx1 = newidx;
            }

            if (idx2 >= 0) {
                newidx = ver2.indexOf('.', idx2);
                if (newidx < 0) {
                    strVal2 = ver2.substring(idx2);
                } else {
                    strVal2 = ver2.substring(idx2, newidx);
                    newidx++;
                }

                idx2 = newidx;
            }

            intVal1 = Integer.parseInt(strVal1); // May throw NFE
            intVal2 = Integer.parseInt(strVal2); // May throw NFE

            if (intVal1 > intVal2) {
                return 1;
            }

            if (intVal1 < intVal2) {
                return -1;
            }
        }

        return 0;
    }

    /**
     * Checks the format of a version string.
     * <p>
     * Versions must be in the form <em>xxx.yyy.zzz</em>, where:
     * <pre>
     *     <em>xxx</em> is the major version
     *     <em>yyy</em> is the minor version
     *     <em>zzz</em> is the micro version
     * </pre>
     * It is acceptable to omit the micro and possibly the minor versions.
     * If these are not included in the version string, the period immediately
     * preceding the number must also be removed. So, the versions
     * <em>xxx.yyy</em> or <em>xxx</em> are also valid.
     * <p>
     * Version numbers do not have to be three digits wide. However, you may
     * pad versions with leading zeros if desired.
     *
     * @param ver the version to check
     *
     * @exception NumberFormatException if <code>ver</code>
     *     contains any characters that are not numbers or periods
     */
    protected static void checkVersionFormat(String ver)
            throws NumberFormatException {
        int length;
        int start = 0;
        int end;

        length = ver.length();
        for (int i = 0; ; i++) {
            // check for more than 3 parts or a trailing '.'
            if (i == 3 || start == length) {
                throw new NumberFormatException();
            }

            end = ver.indexOf('.', start);
            if (end == -1) {
                end = length;
            }

            // throws NFE if the substring is not all digits
            Integer.parseInt(ver.substring(start, end));

            if (end == length) {
                // we are done
                return;
            }

            // next time around start after the index of '.'
            start = end + 1;
        }
    }

    /**
     * Checks to make sure the runtime environment required by
     * the application is supported.
     * Send a message back to the server if the check fails and
     * throw an exception.
     *
     * @exception InvalidJadException if the check fails
     */
    private void checkRuntimeEnv() throws InvalidJadException {
        String execEnv;

        execEnv = state.getAppProperty(MIDletSuite.RUNTIME_EXEC_ENV_PROP);
        if (execEnv == null || execEnv.length() == 0) {
            execEnv = MIDletSuite.RUNTIME_EXEC_ENV_DEFAULT;
        }

        // need to call trim to remove trailing spaces
        execEnv = execEnv.trim();

        if (execEnv.equals(cldcRuntimeEnv)) {
            // success, done
            return;
        }

        postInstallMsgBackToProvider(OtaNotifier.INCOMPATIBLE_MSG);
        throw new InvalidJadException(InvalidJadException.DEVICE_INCOMPATIBLE);
    }

    /**
     * Match the name of the configuration or profile, and return
     * true if the first name has a greater or equal version than the
     * second. The names of the format "XXX-Y.Y" (e.g. CLDC-1.0, MIDP-2.0)
     * as used in the system properties (microedition.configuration &
     * microedition.profiles).
     *
     * This is used for checking both configuration and profiles.
     *
     * @param name1 name of configuration or profile
     * @param name2 name of configuration or profile
     * @return  true is name1 matches name2 and is greater or equal in
     *          version number. false otherwise
     */
    private static boolean matchVersion(String name1, String name2) {
        int dash1 = name1.indexOf('-');

        if (dash1 < 0) {
            return false;
        }

        int dash2 = name2.indexOf('-');

        if (dash2 < 0) {
            return false;
        }

        String base1 = name1.substring(0, dash1);
        String base2 = name2.substring(0, dash2);

        if (!base1.equals(base2)) {
            return false;
        }

        String ver1 = name1.substring(dash1 + 1, name1.length());
        String ver2 = name2.substring(dash2 + 1, name2.length());

        return (vercmp(ver1, ver2) >= 0);
    }

    /**
     * Checks to make sure the configration need by the application
     * is supported.
     * Send a message back to the server if the check fails and
     * throw an exception.
     *
     * @exception InvalidJadException if the check fails
     */
    private void checkConfiguration() throws InvalidJadException {
        String config;

        config = state.getAppProperty(MIDletSuite.CONFIGURATION_PROP);
        if (config == null || config.length() == 0) {
            postInstallMsgBackToProvider(
                OtaNotifier.INVALID_JAR_MSG);
            throw new InvalidJadException(
                InvalidJadException.MISSING_CONFIGURATION);
        }

        if (cldcConfig == null) {
            // need to call trim to remove trailing spaces
            cldcConfig =
                System.getProperty(MICROEDITION_CONFIG).trim();
        }

        if (matchVersion(cldcConfig, config)) {
            // success, done
            return;
        }

        postInstallMsgBackToProvider(OtaNotifier.INCOMPATIBLE_MSG);
        throw new InvalidJadException(InvalidJadException.DEVICE_INCOMPATIBLE);
    }

    /**
     * Tries to match one of the supported profiles with a profile
     * listed in string of profiles separated by a space.
     * Send a message back to the server if a match is not found and
     * throw an exception.
     *
     * @exception InvalidJadException if there is no match
     */
    private void matchProfile() throws InvalidJadException {
        String profiles = state.getAppProperty(MIDletSuite.PROFILE_PROP);

        if (profiles == null || profiles.length() == 0) {
            postInstallMsgBackToProvider(OtaNotifier.INVALID_JAR_MSG);
            throw new
                InvalidJadException(InvalidJadException.MISSING_PROFILE);
        }

        // build the list of supported profiles if needed
        if (supportedProfiles == null) {
            int start;
            int nextSpace = -1;
            String meProfiles =
                System.getProperty(MICROEDITION_PROFILES);
            if (meProfiles == null || meProfiles.length() == 0) {
                throw new RuntimeException(
                    "system property microedition.profiles not set");
            }
            supportedProfiles = new Vector();
            // need to call trim to remove trailing spaces
            meProfiles = meProfiles.trim();

            for (; ; ) {
                start = nextSpace + 1;
                nextSpace = meProfiles.indexOf(' ', start);

                // consecutive spaces, keep searching
                if (nextSpace == start) {
                    continue;
                }

                if ((nextSpace < 0)) {
                    supportedProfiles.addElement(
                        meProfiles.substring(start, meProfiles.length()));
                    break;
                }

                supportedProfiles.addElement(
                    meProfiles.substring(start, nextSpace));

            }
        }

        /*
         * for each profiles listed in MicroEdition-Profile, we need to
         * find a matching profile in microedition.profiles.
         */
        int current = 0;
        int nextSeparatorIndex = 0;
        String requestedProfile;
        boolean supported = false;

        // convert tab to space so that the parsing later is simplified
        StringBuffer tmp = new StringBuffer(profiles);
        boolean modified = false;
        while ((nextSeparatorIndex = profiles.indexOf('\t', current)) != -1) {
            tmp.setCharAt(nextSeparatorIndex, ' ');
            current++;
            modified = true;
        }

        if (modified) {
            profiles = tmp.toString();
        }

        // reset the indices
        current = nextSeparatorIndex = 0;
        do {
            // get the next requested profiles
            nextSeparatorIndex = profiles.indexOf(' ', current);

            if (nextSeparatorIndex == current) {
                // consecutive spaces, keep searching
                current++;
                continue;
            }

            if (nextSeparatorIndex == -1) {
                // last (or the only one) value in the list
                requestedProfile =
                   profiles.substring(current, profiles.length());
            } else {
                requestedProfile =
                    profiles.substring(current, nextSeparatorIndex);
                current = nextSeparatorIndex + 1;
            }

            /*
             * try to match each requested profiles against the supported
             * ones.
             */
            supported = false;
            for (int i = 0; i < supportedProfiles.size(); i++) {
                String supportedProfile =
                    (String)supportedProfiles.elementAt(i);
                if (matchVersion(supportedProfile, requestedProfile)) {
                     supported = true;
                     break;
                }
            }

            // short circuit the test if there is one mismatch
            if (!supported) {
                break;
            }
        } while (nextSeparatorIndex != -1);

        // matched all requested profiles against supported ones
        if (supported) {
            return;
        }

        postInstallMsgBackToProvider(OtaNotifier.INCOMPATIBLE_MSG);
        throw new InvalidJadException(InvalidJadException.DEVICE_INCOMPATIBLE);
    }

    /**
     * Registers the push connections for the application.
     * Send a message back to the server if a connection cannot be
     * registered and throw an exception.
     *
     * @exception InvalidJadException if a connection cannot be registered
     */
    private void registerPushConnections()
            throws InvalidJadException {
        byte[] curLevels = settings.getPermissions();

        if (state.isPreviousVersion) {
            PushRegistryInternal.unregisterConnections(info.id);
        }

        for (int i = 1; ; i++) {
            String pushProp;

            pushProp = state.getAppProperty("MIDlet-Push-" + i);
            if (pushProp == null) {
                break;
            }

            /*
             * Parse the comma separated values  -
             *  " connection, midlet, role, filter"
             */
            int comma1 = pushProp.indexOf(',', 0);
            int comma2 = pushProp.indexOf(',', comma1 + 1);

            String conn = pushProp.substring(0, comma1).trim();
            String midlet = pushProp.substring(comma1+1, comma2).trim();
            String filter = pushProp.substring(comma2+1).trim();

            /* Register the new push connection string. */
            try {
                PushRegistryInternal.registerConnectionInternal(
                    getAccessControlContext(),
                    state, conn, midlet, filter, false);
            } catch (Exception e) {
                /* If already registered, abort the installation. */
                PushRegistryInternal.unregisterConnections(info.id);

                if (state.isPreviousVersion) {
                    // put back the old ones, removed above
                    redoPreviousPushConnections();
                }

                if (e instanceof SecurityException) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.AUTHORIZATION_FAILURE_MSG);

                    // since our state object put the permission in message
                    throw new InvalidJadException(
                        InvalidJadException.AUTHORIZATION_FAILURE,
                        e.getMessage());
                }

                postInstallMsgBackToProvider(
                    OtaNotifier.PUSH_REG_FAILURE_MSG);

                if (e instanceof IllegalArgumentException) {
                    throw new InvalidJadException(
                        InvalidJadException.PUSH_FORMAT_FAILURE, pushProp);
                }

                if (e instanceof ConnectionNotFoundException) {
                    throw new InvalidJadException(
                        InvalidJadException.PUSH_PROTO_FAILURE, pushProp);
                }

                if (e instanceof IOException) {
                    throw new InvalidJadException(
                        InvalidJadException.PUSH_DUP_FAILURE, pushProp);
                }

                if (e instanceof ClassNotFoundException) {
                    throw new InvalidJadException(
                        InvalidJadException.PUSH_CLASS_FAILURE, pushProp);
                }

                // error in the implementation code
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }

                /* handle not RuntimeException-derived exceptions */
                throw new RuntimeException("Exception in " +
                        "registerPushConnections(): " + e.getMessage());
            }
        }

        if (state.isPreviousVersion) {
            // preserve the push options when updating
            settings.setPushOptions(state.previousSuite.getPushOptions());

            // use the old setting
            settings.setPushInterruptSetting(
                (byte)state.previousSuite.getPushInterruptSetting());

            // The old suite may have not had push connections
            if (settings.getPushInterruptSetting() != Permissions.NEVER) {
                return;
            }
        }
		int PUSH_ID = Permissions.getId("javax.microedition.io.PushRegistry");
        if (curLevels[PUSH_ID] == Permissions.NEVER) {
            settings.setPushInterruptSetting(Permissions.NEVER);
        } else if (curLevels[PUSH_ID] == Permissions.ALLOW) {
            // Start the default at session for usability when denying.
            settings.setPushInterruptSetting(Permissions.SESSION);
        } else {
            settings.setPushInterruptSetting(curLevels[PUSH_ID]);
        }
    }

    /**
     * Registers the push connections for previous version after
     * and aborted update.
     */
    private void redoPreviousPushConnections() {
        for (int i = 1; ; i++) {
            String pushProp;

            pushProp = state.previousSuite.getProperty("MIDlet-Push-" + i);
            if (pushProp == null) {
                break;
            }

            /*
             * Parse the comma separated values  -
             *  " connection, midlet, role, filter"
             */
            int comma1 = pushProp.indexOf(',', 0);
            int comma2 = pushProp.indexOf(',', comma1 + 1);

            String conn = pushProp.substring(0, comma1).trim();
            String midlet = pushProp.substring(comma1+1, comma2).trim();
            String filter = pushProp.substring(comma2+1).trim();

            /* Register the new push connection string. */
            try {
                PushRegistryInternal.registerConnectionInternal(
                    getAccessControlContext(),
                    state, conn, midlet, filter, true);
            } catch (IOException e) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "registerConnectionInternal  threw an IOException");
                }
            } catch (ClassNotFoundException e) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "registerConnectionInternal threw a " +
                        "ClassNotFoundException");
                }
            }
        }
    }
}

/**
 * Implements the permission checking interface using the permission of
 * the suite being installed.
 */
class AccessControl extends AccessControlContextAdapter {

    /** Rreference to the MIDlet suite. */
    private MIDletSuite suite;

    /**
     * Initializes the AccessControl object.
     *
     * @param theSuite reference to the MIDlet suite
     */
    AccessControl(MIDletSuite theSuite) {
        suite = theSuite;
    }
    
    /**
     * Checks for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     *
     * @param name name of the permission to check for
     * @param resource string to insert into the question, can be null if
     *        no %2 in the question
     * @param extraValue string to insert into the question,
     *        can be null if no %3 in the question
     *
     * @exception SecurityException if the specified permission
     * is not permitted, based on the current security policy
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public void checkPermissionImpl(String name, String resource,
            String extraValue) throws SecurityException, InterruptedException {

        int permissionId;

        if (AccessController.TRUSTED_APP_PERMISSION_NAME.equals(name)) {
            // This is really just a trusted suite check.
            if (suite.isTrusted()) {
                return;
            }

            throw new SecurityException("suite not trusted");
        }

        permissionId = Permissions.getId(name);

        if (permissionId == Permissions.AMS ||
                permissionId == Permissions.MIDP) {
            // These permission checks cannot block
            suite.checkIfPermissionAllowed(name);
        } else {
            suite.checkForPermission(name, resource, extraValue);
        }
    }
}

/*
 * Holds the state of an installation, so it can restarted after it has
 * been stopped.
 */
class InstallStateImpl implements InstallState, MIDletSuite {
    /** Contains the data obtained during the installation process */
    protected InstallInfo installInfo;

    /** Contains the data obtained during the installation process */
    protected SuiteSettings suiteSettings;

    /** ID of the storage where the new midlet suite will be installed. */
    protected int storageId;

    /** Receives warnings and status. */
    protected InstallListener listener;

    /** When the install started, in milliseconds. */
    protected long startTime;

    /** What to do next. */
    protected int nextStep;

    /** Signals the installation to stop. */
    protected boolean stopInstallation;

    /**
     * Signals that installation is at a point where cancel
     * requests are ignored
     */
    protected boolean ignoreCancel;

    /** exception that stopped the installation. */
    protected InvalidJadException exception;

    /**
     * Option to force an overwrite of existing components without
     * any version comparison.
     */
    protected boolean force;

    /**
     * Option to force the RMS data of the suite to be overwritten to
     * be removed without comparison to the new suite.
     */
    protected boolean removeRMS;

    /** Raw JAD. */
    protected byte[] jad;

    /** character encoding of the JAD. */
    protected String jadEncoding;

    /** Parsed JAD. */
    protected JadProperties jadProps;

    /** Parsed manifest. */
    protected ManifestProperties jarProps;

    /** Cached File object. */
    protected File file;

    /** User name for authentication. */
    protected String username;

    /** Password for authentication. */
    protected String password;

    /** User name for proxyAuthentication. */
    protected String proxyUsername;

    /** Password for proxy authentication. */
    protected String proxyPassword;

    /** Status to signal the beginning of the data transfer. */
    protected int beginTransferDataStatus;

    /** Status for the data transfer method to give to the listener. */
    protected int transferStatus;

    /** Security Handler. */
    protected SecurityHandler securityHandler;

    /** Holds the unzipped JAR manifest to be saved. */
    protected byte[] manifest;

    /** Cache of storage object. */
    protected RandomAccessStream storage;

    /** Cache of MIDlet suite storage object. */
    protected MIDletSuiteStorage midletSuiteStorage;

    /** The root of all MIDP persistent system data. */
    protected String storageRoot;

    /** Signals that previous version exists. */
    protected boolean isPreviousVersion;

    /** Previous MIDlet suite info. */
    protected MIDletSuiteImpl previousSuite;

    /** Previous MIDlet suite install info. */
    protected InstallInfo previousInstallInfo;

    /** The ContentHandler installer state. */
    protected CHManager chmanager;

    /** Constructor. */
    public InstallStateImpl() {
        installInfo   = new InstallInfo(UNUSED_SUITE_ID);
        suiteSettings = new SuiteSettings(UNUSED_SUITE_ID);
    }

    /**
     * Gets the last recoverable exception that stopped the install.
     * Non-recoverable exceptions are thrown and not saved in the state.
     *
     * @return last exception that stopped the install
     */
    public InvalidJadException getLastException() {
        return exception;
    }

    /**
     * Gets the unique ID that the installed suite was stored with.
     *
     * @return storage name that can be used to load the suite
     */
    public int getID() {
        return installInfo.id;
    }

    /**
     * Sets the username to be used for HTTP authentication.
     *
     * @param theUsername 8 bit username, cannot contain a ":"
     */
    public void setUsername(String theUsername) {
        username = theUsername;
    }

    /**
     * Sets the password to be used for HTTP authentication.
     *
     * @param thePassword 8 bit password
     */
    public void setPassword(String thePassword) {
        password = thePassword;
    }

    /**
     * Sets the username to be used for HTTP proxy authentication.
     *
     * @param theUsername 8 bit username, cannot contain a ":"
     */
    public void setProxyUsername(String theUsername) {
        proxyUsername = theUsername;
    }

    /**
     * Sets the password to be used for HTTP proxy authentication.
     *
     * @param thePassword 8 bit password
     */
    public void setProxyPassword(String thePassword) {
        proxyPassword = thePassword;
    }

    /**
     * Gets a property of the application to be installed.
     * First from the JAD, then if not found, the JAR manifest.
     *
     * @param key key of the property
     *
     * @return value of the property or null if not found
     */
    public String getAppProperty(String key) {
        String value;

        if (jadProps != null) {
            value = jadProps.getProperty(key);
            if (value != null) {
                return value;
            }
        }

        if (jarProps != null) {
            value = jarProps.getProperty(key);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    /**
     * Gets the URL of the JAR.
     *
     * @return URL of the JAR
     */
    public String getJarUrl() {
        return installInfo.jarUrl;
    }

    /**
     * Gets the label for the downloaded JAR.
     *
     * @return suite name
     */
    public String getSuiteName() {
        return installInfo.suiteName;
    }

    /**
     * Gets the expected size of the JAR.
     *
     * @return size of the JAR in K bytes rounded up
     */
    public int getJarSize() {
        return (installInfo.expectedJarSize + 1023) / 1024;
    }

    /**
     * Gets the authorization path of this suite. The path starts with
     * the most trusted CA that authorized this suite.
     *
     * @return array of CA names or null if the suite was not signed
     */
    public String[] getAuthPath() {
        /*
         * The auth path returned is no a copy because this object is
         * only available to callers with the AMS permission, which
         * have permission to build auth paths for new suites.
         */
        return installInfo.getAuthPath();
    }

    /**
     * Checks for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     *
     * @param permission ID of the permission to check for,
     *      the ID must be from
     *      {@link com.sun.midp.security.Permissions}
     * @param resource string to insert into the question, can be null if
     *        no %2 in the question
     *
     * @exception SecurityException if the permission is not
     *            allowed
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public void checkForPermission(String permission, String resource)
            throws InterruptedException {
        checkForPermission(permission, resource, null);
    }

    /**
     * Checks for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     *
     * @param permissionStr name of the permission to check for,
     *      the name must be from
     *      {@link com.sun.midp.security.Permissions}
     * @param resource string to insert into the question, can be null if
     *        no %2 in the question
     * @param extraValue string to insert into the question,
     *        can be null if no %3 in the question
     *
     * @exception SecurityException if the permission is not
     *            allowed
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public void checkForPermission(String permissionStr, String resource,
            String extraValue) throws InterruptedException {

		int permission = Permissions.getId(permissionStr);
        securityHandler.checkForPermission(permissionStr,
            Permissions.getTitle(permission),
            Permissions.getQuestion(permission),
            Permissions.getOneshotQuestion(permission),
            installInfo.suiteName, resource, extraValue,
            permissionStr);
    }

    /**
     * Indicates if the named MIDlet is registered in the suite
     * with MIDlet-&lt;n&gt; record in the manifest or
     * application descriptor.
     * @param midletName class name of the MIDlet to be checked
     *
     * @return true if the MIDlet is registered
     */
    public boolean isRegistered(String midletName) {
        String midlet;
        MIDletInfo midletInfo;

        for (int i = 1; ; i++) {
            midlet = getAppProperty("MIDlet-" + i);
            if (midlet == null) {
                return false; // We went past the last MIDlet
            }

            /* Check if the names match. */
            midletInfo = new MIDletInfo(midlet);
            if (midletInfo.classname.equals(midletName)) {
                return true;
            }
        }
    }

    /**
     * Counts the number of MIDlets from its properties.
     * IMPL_NOTE: refactor to avoid duplication with MIDletSuiteImpl.
     *
     * @return number of midlet in the suite
     */
    public int getNumberOfMIDlets() {
        int i;

        for (i = 1; getProperty("MIDlet-" + i) != null; i++);

        return (i-1);
    }

    /**
     * Returns the suite's name to display to the user.
     *
     * @return suite's name that will be displayed to the user
     */
    public String getDisplayName() {
        String displayName = installInfo.displayName;

        if (displayName == null || "".equals(displayName)) {
            displayName = getAppProperty(MIDletSuite.SUITE_NAME_PROP);

            if (displayName == null) {
                displayName = String.valueOf(installInfo.id);
            }
        }

        return displayName;
    }


    /**
     * Returns the information about the first midlet in the suite.
     *
     * @return MIDletInfo structure describing the first midlet
     * or null if it is not available
     */
    public MIDletInfo getMidletInfo() {
        String midlet;

        midlet = getAppProperty("MIDlet-1");
        if (midlet == null) {
            return null;
        }

        return new MIDletInfo(midlet);
    }

    /**
     * Indicates if this suite is trusted.
     * (not to be confused with a domain named "trusted",
     * this is used to determine if a trusted symbol should be displayed
     * to the user and not used for permissions)
     *
     * @return true if the suite is trusted false if not
     */
    public boolean isTrusted() {
        return installInfo.trusted;
    }


    /**
     * Check if the suite classes were successfully verified
     * during the suite installation.
     *
     * @return true if the suite classes are verified, false otherwise
     */
    public boolean isVerified() {
        return installInfo.verifyHash != null;
    }

    /**
     * Gets a property of the suite. A property is an attribute from
     * either the application descriptor or JAR Manifest.
     *
     * @param key the name of the property
     * @return A string with the value of the property.
     *    <code>null</code> is returned if no value
     *          is available for the key.
     */
    public String getProperty(String key) {
        return getAppProperty(key);
    }

    /**
     * Gets push setting for interrupting other MIDlets.
     * Reuses the Permissions.
     *
     * @return push setting for interrupting MIDlets the value
     *        will be permission level from {@link Permissions}
     */
    public byte getPushInterruptSetting() {
        return suiteSettings.getPushInterruptSetting();
    }

    /**
     * Gets push options for this suite.
     *
     * @return push options are defined in {@link PushRegistryImpl}
     */
    public int getPushOptions() {
        return suiteSettings.getPushOptions();
    }

    /**
     * Gets list of permissions for this suite.
     *
     * @return array of permissions from {@link Permissions}
     */
    public byte[] getPermissions() {
        return suiteSettings.getPermissions();
    }

    /**
     * Replace or add a property to the suite for this run only.
     *
     * @param token token with the AMS permission set to allowed
     * @param key the name of the property
     * @param value the value of the property
     *
     * @exception SecurityException if the caller's token does not have
     *            internal AMS permission
     */
    public void setTempProperty(SecurityToken token, String key,
                                String value) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Get the name of a MIDlet.
     *
     * @param classname classname of a MIDlet in the suite
     *
     * @return name of a MIDlet to show the user
     */
    public String getMIDletName(String classname) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Checks to see the suite has the ALLOW level for specific permission.
     * This is used for by internal APIs that only provide access to
     * trusted system applications.
     * <p>
     * Only trust this method if the object has been obtained from the
     * MIDletStateHandler of the suite.
     *
     * @param permission permission ID from
     *      {@link com.sun.midp.security.Permissions}
     *
     * @exception SecurityException if the suite is not
     *            allowed to perform the specified action
     */
    public void checkIfPermissionAllowed(String permission) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Gets the status of the specified permission.
     * If no API on the device defines the specific permission
     * requested then it must be reported as denied.
     * If the status of the permission is not known because it might
     * require a user interaction then it should be reported as unknown.
     *
     * @param permission to check if denied, allowed, or unknown
     * @return 0 if the permission is denied; 1 if the permission is
     *    allowed; -1 if the status is unknown
     */
    public int checkPermission(String permission) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Saves any the settings (security or others) that the user may have
     * changed. Normally called by the scheduler after
     * the last running MIDlet in the suite is destroyed.
     * However it could be call during a suspend of the VM so
     * that persistent settings of the suite can be preserved.
     */
    public void saveSettings() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Asks the user want to interrupt the current MIDlet with
     * a new MIDlet that has received network data.
     *
     * @param connection connection to place in the permission question or
     *        null for alarm
     *
     * @return true if the use wants interrupt the current MIDlet,
     * else false
     */
    public boolean permissionToInterrupt(String connection) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Determine if the a MIDlet from this suite can be run. Note that
     * disable suites can still have their settings changed and their
     * install info displayed.
     *
     * @return true if suite is enabled, false otherwise
     */
    public boolean isEnabled() {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Close the opened MIDletSuite
     */
    public void close() {
    }
}
