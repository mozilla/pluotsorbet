/*
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

package com.sun.midp.main;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import javax.microedition.io.Connector;

import com.sun.j2me.security.AccessController;

import com.sun.midp.midlet.*;
import com.sun.midp.lcdui.*;
import com.sun.midp.midletsuite.*;
import com.sun.midp.configurator.Constants;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.installer.InternalMIDletSuiteImpl;
import com.sun.midp.installer.JadProperties;
import com.sun.midp.installer.ManifestProperties;
import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.jarutil.JarReader;
import com.sun.midp.log.*;
import com.sun.midp.publickeystore.WebPublicKeyStore;
import com.sun.midp.util.Properties;
import com.sun.midp.rms.RmsEnvironment;
import com.sun.midp.rms.RecordStoreRegistry;


/**
 * The class presents abstract MIDlet suite loader with routines to prepare
 * runtime environment for CLDC a suite execution.
 */
abstract class CldcMIDletSuiteLoader extends AbstractMIDletSuiteLoader {
    /** Event producer to send MIDlet state events to the AMS isolate. */
    protected MIDletControllerEventProducer midletControllerEventProducer;

    /** Listener for MIDlet related events (state changes, etc). */
    protected MIDletEventListener midletEventListener;

    /**
     * Provides interface for display foreground notification,
     * functionality that can not be publicly added to a javax package.
     */
    protected ForegroundEventListener foregroundEventListener;

    /** Core initialization of a MIDlet suite loader */
    protected void init() {
        isolateId = MIDletSuiteUtils.getIsolateId();
        amsIsolateId = MIDletSuiteUtils.getAmsIsolateId();

        // Hint VM of startup beginning: system init phase
        MIDletSuiteUtils.vmBeginStartUp(isolateId);

        WebPublicKeyStore.initKeystoreLocation(internalSecurityToken,
            Configuration.getProperty("com.sun.midp.publickeystore.WebPublicKeyStore"));

        super.init();
    }

    /**
     * Creates all needed objects of a MIDlet suite environment, but
     * only initialization that is done, will be to pass other created objects,
     * and the current and AMS isolate IDs. It is mostly event-related
     * objects, however subclasses can extend the environment with more
     * specific parts
     */
    protected void createSuiteEnvironment() {
        midletControllerEventProducer =
            new MIDletControllerEventProducer(
                eventQueue,
                amsIsolateId,
                isolateId);

        foregroundController = new CldcForegroundController(
            midletControllerEventProducer);

	lcduiEnvironment = new LCDUIEnvironment(internalSecurityToken, 
						eventQueue, isolateId, 
						foregroundController);

        // creates display container, needs foregroundController
        super.createSuiteEnvironment();

        foregroundEventListener = new ForegroundEventListener(
            eventQueue,
            displayContainer);

        midletStateHandler =
            MIDletStateHandler.getMidletStateHandler();

        MIDletStateListener midletStateListener =
            new CldcMIDletStateListener(internalSecurityToken,
                                        displayContainer,
                                        midletControllerEventProducer);

        midletStateHandler.initMIDletStateHandler(
            internalSecurityToken,
            midletStateListener,
            new CldcMIDletLoader(internalSecurityToken),
            new CldcPlatformRequest(internalSecurityToken));

        midletEventListener = new MIDletEventListener(
            internalSecurityToken,
            midletStateHandler,
            eventQueue);
        
        MidletSuiteContainer msc = 
                new MidletSuiteContainer(MIDletSuiteStorage.getMIDletSuiteStorage(internalSecurityToken));
        RmsEnvironment.init(internalSecurityToken, msc);
    }

    /** Final actions to finish a MIDlet suite loader */
    protected void done() {
        RecordStoreRegistry.shutdown(
            internalSecurityToken);
        super.done();
    }

    /**
     * Does all initialization for already created objects of a MIDlet suite
     * environment. Subclasses can also extend the initialization with
     * various global system initializations needed for all suites.
     * The MIDlet suite has been created at this point, so it can be
     * used to initialize any per suite data.
     */
    protected void initSuiteEnvironment() {
        super.initSuiteEnvironment();

        /* Set up permission checking for this suite. */
        AccessController.setAccessControlContext(
            new CldcAccessControlContext(midletSuite));
    }

    /**
     * Starts MIDlet suite in the prepared environment
     * Overrides super method to hint VM of system startup
     * phase is ended 
     *
     * @throws Exception can be thrown during execution
     */
    protected void startSuite() throws Exception {
        // Hint VM of startup finish: system init phase 
        MIDletSuiteUtils.vmEndStartUp(isolateId);
        super.startSuite();
    }

    /**
     * Creates MIDlet suite instance by suite ID
     *
     * @return MIDlet suite to load
     *
     * @throws Exception in the case MIDlet suite can not be
     *   created because of a security reasons or some problems
     *   related to suite storage
     */
    protected MIDletSuite createMIDletSuite() throws Exception {
        MIDletSuiteStorage storage;
        MIDletSuite suite = null;

        if (suiteId == MIDletSuite.INTERNAL_SUITE_ID) {
            Properties props = null;

            // This is support the CLDC WTK
            if (args != null && args.length > 0 && args[0] != null) {
                
                if (args[0].toLowerCase().endsWith(".jad")) {
                    /*
                     * Check if the arg 0 ends with .jad,
                     * which means it is a path to the JAD file.
                     */
                    props = getJadProps(args[0]);
                } else if (args[0].toLowerCase().endsWith(".jar")) {
                    /*
                     * Check if the arg 0 ends with .jar,
                     * which means it is a path to the JAR file.
                     */
                    props = getJarProps(args[0]);
                }
            }

            if (props == null) {
                suite = InternalMIDletSuiteImpl.create(midletDisplayName,
                                                       suiteId);
            } else {
                suite = InternalMIDletSuiteImpl.create(midletDisplayName,
                                                       suiteId, props);
            }
        } else {
            storage = MIDletSuiteStorage.
                getMIDletSuiteStorage(internalSecurityToken);

            suite = storage.getMIDletSuite(suiteId, false);
            Logging.initLogSettings(suiteId);
        }

        return suite;
    }

    /**
     * Gets the properties of a JAD.
     *
     * @param filePath full path of the JAD
     *
     * @return JAD properties
     */
    private Properties getJadProps(String filePath) {
        JadProperties jadProps = null;
        try {
            /* Open JAD file and extract properties */
            RandomAccessStream storage =
                new RandomAccessStream(internalSecurityToken);
            storage.connect(filePath, Connector.READ);
            try {
                int size = storage.getSizeOf();
                byte[] buffer = new byte[size];
                DataInputStream dis = storage.openDataInputStream();
                try {
                    dis.readFully(buffer);
                    InputStream is = new ByteArrayInputStream(buffer);
                    jadProps = new JadProperties();
                    jadProps.load(is, null);
                    buffer = null;
                    is = null;
                } finally {
                    dis.close();
                }
            } finally {
                storage.disconnect();
            }
        } catch (Throwable t){
            t.printStackTrace();
        }

        return jadProps;
    }

    /**
     * Gets the properties of a JAR.
     *
     * @param filePath full path of the JAR
     *
     * @return JAR properties
     */
    private Properties getJarProps(String filePath) {
        String jarPath = null;
        String subPath;
        ManifestProperties jarProps = null;
        int index = filePath.indexOf(';');

        /* parse classpath for a jar file */
        while(index != -1) {
            /* parse classpath token by token asuming delimited is ';' */
            subPath = filePath.substring(0, index);
            if (subPath.toLowerCase().indexOf(".jar") != -1) {
                jarPath = subPath;
                break;
            } else {
                // get rid of the first token
                filePath = filePath.substring(index+1, filePath.length());
                index = filePath.indexOf(';'); // look for the next token
            }
        }

        if ((jarPath == null) &&
            (filePath.toLowerCase().indexOf(".jar") != -1)) {
            jarPath = filePath;
        }

        try {
            byte[] manifest =
                JarReader.readJarEntry(jarPath, MIDletSuite.JAR_MANIFEST);
            jarProps = new ManifestProperties();
            jarProps.load(new ByteArrayInputStream(manifest));
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return jarProps;
    }

    /**
     * Gets error code by exception type
     *
     * @param t exception instance
     * @return error code
     */
    static protected int getErrorCode(Throwable t) {
        if (t instanceof ClassNotFoundException) {
            return Constants.MIDLET_CLASS_NOT_FOUND;
        } else if (t instanceof InstantiationException) {
            return Constants.MIDLET_INSTANTIATION_EXCEPTION;
        } else if (t instanceof IllegalAccessException) {
            return Constants.MIDLET_ILLEGAL_ACCESS_EXCEPTION;
        } else if (t instanceof OutOfMemoryError) {
            return Constants.MIDLET_OUT_OF_MEM_ERROR;
        } else if (t instanceof MIDletSuiteLockedException) {
            return Constants.MIDLET_INSTALLER_RUNNING;
        } else {
            return Constants.MIDLET_CONSTRUCTOR_FAILED;
        }
    }

    /**
     * Gets AMS error message ID by generic error code
     *
     * @param errorCode generic error code
     * @return AMS error ID
     */
    static protected int getErrorMessageId(int errorCode) {
        switch (errorCode) {
            case Constants.MIDLET_SUITE_DISABLED:
                return ResourceConstants.
                    AMS_MIDLETSUITELDR_MIDLETSUITE_DISABLED;
            case Constants.MIDLET_SUITE_NOT_FOUND:
                return ResourceConstants.
                    AMS_MIDLETSUITELDR_MIDLETSUITE_NOTFOUND;
            case Constants.MIDLET_CLASS_NOT_FOUND:
                return ResourceConstants.
                    AMS_MIDLETSUITELDR_CANT_LAUNCH_MISSING_CLASS;
            case Constants.MIDLET_INSTANTIATION_EXCEPTION:
                return ResourceConstants.
                    AMS_MIDLETSUITELDR_CANT_LAUNCH_ILL_OPERATION;
            case Constants.MIDLET_ILLEGAL_ACCESS_EXCEPTION:
                return ResourceConstants.
                    AMS_MIDLETSUITELDR_CANT_LAUNCH_ILL_OPERATION;
            case Constants.MIDLET_OUT_OF_MEM_ERROR:
                return ResourceConstants.
                    AMS_MIDLETSUITELDR_QUIT_OUT_OF_MEMORY;
            default:
                return ResourceConstants.
                    AMS_MIDLETSUITELDR_UNEXPECTEDLY_QUIT;
        }
    }
    
    /**
     * Handles exception occurred during MIDlet suite execution.
     * @param t exception instance
     */
    public void handleException(Throwable t) {
        t.printStackTrace();
        int errorCode = getErrorCode(t);

        reportError(errorCode, t.getMessage());
    }
}
