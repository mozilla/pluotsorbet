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

package com.sun.midp.main;

import com.sun.cldc.isolate.*;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.midletsuite.MIDletSuiteStorage;

import com.sun.midp.configurator.Constants;

import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.security.SecurityInitializer;
import com.sun.midp.security.SecurityToken;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import java.util.Vector;

/**
 * Implements utilities that are different for SVM and MVM modes.
 * Utilities to start a MIDlet in a suite. If not the called from the
 * AMS Isolate the request is forwarded to the AMS Isolate.
 * See {@link #executeWithArgs}.
 * <p>
 * In the AMS Isolate, a check is make that the MIDlet is
 * not already running and is not in the process of starting.
 * If the MIDlet is already running or is starting the request
 * returns immediately. See {@link #startMidletCommon}
 * <p>
 */
public class AmsUtil {
    /** Cached reference to the MIDletExecuteEventProducer. */
    private static MIDletExecuteEventProducer midletExecuteEventProducer;

    /** Cached reference to the MIDletControllerEventProducer. */
    private static MIDletControllerEventProducer midletControllerEventProducer;

    /** Cached reference to the MIDletProxyList. */
    private static MIDletProxyList midletProxyList;
    
    /** Own trusted class to be able to request SecurityToken for priviledged operations */
    private static class SecurityTrusted implements ImplicitlyTrustedClass {}
    /** The instance of SecurityToken for priviledged operations */
    private static SecurityToken trustedToken;
    
    /**
     * Initializes AmsUtil class. shall only be called from
     * MIDletSuiteLoader's main() in MVM AMS isolate
     * or in SVM main isolate.
     * No need in security checks since it is package private method.
     *
     * @param theMIDletProxyList MIDletController's container
     * @param theMidletControllerEventProducer utility to send events
     */
    static void initClass(MIDletProxyList theMIDletProxyList,
            MIDletControllerEventProducer theMidletControllerEventProducer) {

        midletProxyList = theMIDletProxyList;
        midletControllerEventProducer = theMidletControllerEventProducer;

        IsolateMonitor.initClass(theMIDletProxyList);
        StartMIDletMonitor.initClass(theMIDletProxyList);
    }

    /**
     * Initializes AmsUtil class. shall only be called from
     * AppIsolateMIDletSuiteLoader's main() in
     * non-AMS isolates.
     * No need in security checks since it is package private method.
     *
     * @param theMIDletExecuteEventProducer event producer
     *        to be used by this class to send events
     */
    static void initClassInAppIsolate(
            MIDletExecuteEventProducer theMIDletExecuteEventProducer) {
        midletExecuteEventProducer = theMIDletExecuteEventProducer;
    }

    /**
     * Starts a MIDlet in a new Isolate.
     *
     * @param midletSuiteStorage reference to a MIDletStorage object
     * @param externalAppId ID of MIDlet to invoke, given by an external
     *                      application manager
     * @param id ID of an installed suite
     * @param midlet class name of MIDlet to invoke
     * @param displayName name to display to the user
     * @param arg0 if not null, this parameter will be available to the
     *             MIDlet as application property arg-0
     * @param arg1 if not null, this parameter will be available to the
     *             MIDlet as application property arg-1
     * @param arg2 if not null, this parameter will be available to the
     *             MIDlet as application property arg-2
     * @param memoryReserved the minimum amount of memory guaranteed to be
     *             available to the isolate at any time; &lt; 0 if not used
     * @param memoryTotal the total amount of memory that the isolate can
                   reserve; &lt; 0 if not used
     * @param priority priority to set for the new isolate;
     *                 &lt;= 0 if not used
     * @param profileName name of the profile to set for the new isolate;
     *                    null if not used
     * @param isDebugMode true if the new midlet must be started in debug
     *                    mode, false otherwise
     *
     * @return false to signal that the MIDlet suite does not have to exit
     * before the MIDlet is run
     */
    static boolean executeWithArgs(MIDletSuiteStorage midletSuiteStorage,
            int externalAppId, int id, String midlet,
            String displayName, String arg0, String arg1, String arg2,
            int memoryReserved, int memoryTotal, int priority,
            String profileName, boolean isDebugMode) {

        if (id == MIDletSuite.UNUSED_SUITE_ID) {
            // this was just a cancel request meant for SVM mode
            return false;
        }

        if (midlet == null) {
            throw new IllegalArgumentException("MIDlet class cannot be null");
        }

        if (!MIDletSuiteUtils.isAmsIsolate()) {
            /*
             * This is not the AMS isolate so send the request to the
             * AMS isolate.
             */
            midletExecuteEventProducer.sendMIDletExecuteEvent(
                externalAppId, id,
                midlet, displayName,
                arg0, arg1, arg2,
                memoryReserved, memoryTotal, priority,
                profileName, isDebugMode
            );
            return false;
        }

        // Don't start the MIDlet if it is already running.
        if (midletProxyList.isMidletInList(id, midlet)) {
            return false;
        }

        try {
            startMidletCommon(midletSuiteStorage, externalAppId, id, midlet,
                              displayName, arg0, arg1, arg2,
                              memoryReserved, memoryTotal, priority,
                              profileName, isDebugMode);
        } catch (Throwable t) {
            /*
             * This method does not throw exceptions for start errors,
             * (just like the SVM case), for errors, MVM callers rely on
             * start error events.
             */
        }

        return false;
    }

    /**
     * Starts a MIDlet in a new Isolate. Called from the AMS MIDlet suite.
     *
     * @param id ID of an installed suite
     * @param midlet class name of MIDlet to invoke
     * @param displayName name to display to the user
     * @param arg0 if not null, this parameter will be available to the
     *             MIDlet as application property arg-0
     * @param arg1 if not null, this parameter will be available to the
     *             MIDlet as application property arg-1
     * @param arg2 if not null, this parameter will be available to the
     *             MIDlet as application property arg-2
     *
     * @return Isolate that the MIDlet suite was started in
     */
    public static Isolate startMidletInNewIsolate(int id, String midlet,
            String displayName, String arg0, String arg1, String arg2) {
        // Note: getMIDletSuiteStorage performs an AMS permission check
        return startMidletCommon(MIDletSuiteStorage.getMIDletSuiteStorage(),
            0, id, midlet, displayName, arg0, arg1, arg2,
            -1, -1, Isolate.MIN_PRIORITY - 1, null, false);
    }

    /**
     * Starts a MIDlet in a new Isolate.
     * Check that the MIDlet is is not realy running and is not already
     * being started. If so, return immediately.
     *
     * @param midletSuiteStorage midletSuiteStorage for obtaining a
     *      classpath
     * @param externalAppId ID of MIDlet to invoke, given by an external
     *                      application manager
     * @param id ID of an installed suite
     * @param midlet class name of MIDlet to invoke
     * @param displayName name to display to the user
     * @param arg0 if not null, this parameter will be available to the
     *             MIDlet as application property arg-0
     * @param arg1 if not null, this parameter will be available to the
     *             MIDlet as application property arg-1
     * @param arg2 if not null, this parameter will be available to the
     *             MIDlet as application property arg-2
     * @param memoryReserved the minimum amount of memory guaranteed to be
     *             available to the isolate at any time; &lt; 0 if not used
     * @param memoryTotal the total amount of memory that the isolate can
                   reserve; &lt; 0 if not used
     * @param priority priority to set for the new isolate;
     *                 &lt;= 0 if not used
     * @param profileName name of the profile to set for the new isolate;
     *                    null if not used
     * @param isDebugMode true if the new isolate must be started in debug
     *                    mode, false otherwise
     *
     * @return Isolate that the MIDlet suite was started in;
     *             <code>null</code> if the MIDlet is already running
     */
    private static Isolate startMidletCommon(MIDletSuiteStorage
            midletSuiteStorage, int externalAppId, int id, String midlet,
            String displayName, String arg0, String arg1, String arg2,
            int memoryReserved, int memoryTotal,
            int priority, String profileName,
            boolean isDebugMode) {
        Isolate isolate;
        String[] args = {Integer.toString(id), midlet, displayName, arg0,
                         arg1, arg2, Integer.toString(externalAppId),
                         isDebugMode ? "1" : "0"};
        String[] classpath = midletSuiteStorage.getMidletSuiteClassPath(id);

        if (classpath[0] == null) {
            /*
             * Avoid a null pointer exception, rommized midlets don't need
             * a classpath.
             */
            classpath[0] = "";
        }

        String isolateClassPath = System.getProperty("classpathext");
        String[] classpathext = null;
        if (isolateClassPath != null) {
            classpathext = new String[] {isolateClassPath};
        }

        try {
            StartMIDletMonitor app = StartMIDletMonitor.okToStart(id, midlet);
            if (app == null) {
                // Isolate is already running; don't start it again
                return null;
            }

            isolate =
                new Isolate("com.sun.midp.main.AppIsolateMIDletSuiteLoader",
                    args, classpath, null);
            app.setIsolate(isolate);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Can't create Isolate****");
        }

        try {
            if (priority >= Isolate.MIN_PRIORITY) {
                isolate.setPriority(priority);
            }
            
            isolate.setAPIAccess(true);
            isolate.start();
        } catch (Throwable t) {
            int errorCode;
            String msg;

            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                t.printStackTrace();
            }

            if (t instanceof IsolateStartupException) {
                /*
                 * An error occured in the
                 * initialization or configuration of the new isolate
                 * before any application code is invoked, or if this
                 * Isolate was already started or is terminated.
                 */
                errorCode = Constants.MIDLET_ISOLATE_CONSTRUCTOR_FAILED;
                msg = "Can't start Application.";
            } else if (t instanceof IsolateResourceError) {
                /* The system has exceeded the maximum Isolate count. */
                errorCode = Constants.MIDLET_ISOLATE_RESOURCE_LIMIT;
                msg = "No more concurrent applications allowed.";
            } else if (t instanceof IllegalArgumentException) {
                /* Requested profile doesn't exist. */
                errorCode = Constants.MIDLET_ISOLATE_CONSTRUCTOR_FAILED;
                msg = "Invalid profile name: " + profileName;
            } else if (t instanceof OutOfMemoryError) {
                /* The reserved memory cannot be allocated */
                errorCode = Constants.MIDLET_OUT_OF_MEM_ERROR;
                msg = "Not enough memory to run the application.";
            } else {
                errorCode = Constants.MIDLET_ISOLATE_CONSTRUCTOR_FAILED;
                msg = t.toString();
            }

            midletControllerEventProducer.sendMIDletStartErrorEvent(
                id,
                midlet,
                externalAppId,
                errorCode,
                msg);

            throw new RuntimeException(msg);
        }

        return isolate;
    }

    /*
     * Check whether MIDlet-Heap-Size attribute is defined for the
     * MIDlet. It specifies maximum heap memory available for the MIDlet.
     *
     * The value is in bytes and must be a positive integer. The
     * only abbreviation in the value definition supported is K that
     * stands for kilobytes.
     *
     * If the amount declared exceeds the maximum heap limit allowed
     * for a single MIDlet, the attribute is ignored and the default
     * heap limit is used.
     *
     * Heap size is a total heap memory available for the isolate
     * where the MIDlet is launched. The size includes memory
     * occupied by the implementation for internal system purposes.
     * Thus the real heap size available for the MIDlet may be less.
     *
     * @param suiteId ID of an installed suite
     * @param midletName class name of MIDlet to invoke
     *
     * @return heap size limit if it's explicitly defined for the MIDlet,
     *         or -1 if it's not defined or invalid
     */
    static private int getMidletHeapSize(int suiteId, String midletName) {
        int heapSize = -1;

        if (Constants.EXTENDED_MIDLET_ATTRIBUTES_ENABLED) {
            String heapSizeProp = MIDletSuiteUtils.getSuiteProperty(
                suiteId, midletName, MIDletSuite.HEAP_SIZE_PROP);

            if (heapSizeProp != null) {
                boolean sizeInKilos = false;

                int propLen = heapSizeProp.length();
                if (propLen > 0) {
                    char lastChar = heapSizeProp.charAt(propLen - 1);
                    if ((lastChar == 'K') || (lastChar == 'k')) {
                        heapSizeProp = heapSizeProp.substring(0, propLen - 1);
                        sizeInKilos = true;
                    }
                }

                try {
                    heapSize = Integer.parseInt(heapSizeProp);
                    heapSize = sizeInKilos ? heapSize * 1024 : heapSize;
                } catch (NumberFormatException e) {
                    // ignore the attribute if the value is not valid
                }
            }
        }

        return heapSize;
    }

    /**
     * Terminates an isolate.
     *
     * @param id Isolate Id
     */
    static void terminateIsolate(int id) {
        Isolate isolate = MIDletProxyUtils.getIsolateFromId(id);
        if (isolate != null) {
            isolate.exit(0);
        }
    }
    
    /**
     * Obtains trusted instance of SecurityToken
     *
     * @return instance of SecurityToken
     */
    private static SecurityToken getTrustedToken() {
        if (trustedToken == null) {
            trustedToken = SecurityInitializer.requestToken(new SecurityTrusted());
        }
        
        return trustedToken;
    }
}
