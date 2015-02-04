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

import com.sun.midp.lcdui.ForegroundEventProducer;
import com.sun.midp.midlet.*;
import com.sun.midp.jsr.JSRInitializer;
import com.sun.midp.io.j2me.push.PushRegistryInternal;
import com.sun.midp.content.CHManager;
import com.sun.midp.configurator.Constants;
import com.sun.midp.i18n.*;
import com.sun.midp.log.*;
import com.sun.midp.security.SecurityToken;

/**
 * The first class loaded in VM by midp_run_midlet_with_args to initialize
 * internal security the internal AMS classes and start a MIDlet suite.
 * <p>
 * In SVM mode it handles all MIDlet suites (AMS and internal romized,
 * and application).
 * <p>
 * In MVM mode it only handles the first MIDlet suite isolate which is used
 * by the MIDP AMS and other internal MIDlets.
 */
public class MIDletSuiteLoader extends CldcMIDletSuiteLoader {

    /** Command state of the MIDlet suite loader */
    protected CommandState state;

    /** Disable startup error alerts, uninitialized by default */
    protected int disableAlerts = -1;

    /** MIDlet state event producer needed by AMS */
    protected MIDletEventProducer midletEventProducer;

    /** Foreground event producer needed by AMS. */
    protected static ForegroundEventProducer foregroundEventProducer;

    /** List of MIDlet proxies needed by AMS */
    protected MIDletProxyList midletProxyList;

    /**
     * Extends base class initialization with initializatons
     * specific for the AMS task
     */
    protected void init() {
        /*
         * WARNING: Don't add any calls before this !
         *
         * Register AMS task ID native global variable.
         * Since native functions rely on this value to distinguish
         * whether Java AMS is running, this MUST be called before any
         * other native functions from this Isolate. I.E. This call
         * must be the first thing this main make.
         */
        MIDletSuiteUtils.registerAmsIsolateId();
        super.init();

    }

    /** Creates environment objects needed to AMS task */
    protected void createSuiteEnvironment() {
        super.createSuiteEnvironment();

        midletEventProducer = new MIDletEventProducer(eventQueue);
        foregroundEventProducer = new ForegroundEventProducer(eventQueue);
        midletProxyList = new MIDletProxyList(eventQueue);
    }

    /**
     * Inits global systems common for all started MIDlet suite tasks.
     * The systems should be initialized only once in the AMS task.
     */
    protected void initGlobalSystems() {

        // Initialize AMS task resources needed for all tasks
        MIDletSuiteUtils.initAmsResources();

        // Initialize JSR subsystems
        JSRInitializer.init();

        // Start inbound connection watcher thread.
        PushRegistryInternal.startListening(internalSecurityToken);

        // Initialize the Content Handler Monitor of MIDlet exits
        CHManager.getManager(internalSecurityToken).init(
            midletProxyList, eventQueue);

        // Initialize Pipe service
        com.sun.midp.io.j2me.pipe.Protocol.registerService(internalSecurityToken);

        // Initialize Pipe service
        com.sun.midp.io.j2me.pipe.Protocol.registerService(internalSecurityToken);
    }

    /**
     * Inits created MIDlet suite environment objects and global
     * subsystems needed for all suites.
     * <p>
     * The method also loads MIDlet suite paramaters and arguments
     * from the natively saved <code>CommandState</code> instance.
     */
    protected void initSuiteEnvironment() {
        super.initSuiteEnvironment();

        AmsUtil.initClass(
            midletProxyList, midletControllerEventProducer);

        MIDletProxy.initClass(foregroundEventProducer, midletEventProducer);
        MIDletProxyList.initClass(midletProxyList);

        // Listen for start MIDlet requests from the other isolates
        ExecuteMIDletEventListener.startListening(internalSecurityToken,
						  eventQueue);

        // Init gloabal systems common for all isolates
        initGlobalSystems();
    }

    /**
     * The AMS MIDlet started in the suite loader could request for
     * shutdown, so we need to check it, wait for other MIDlets destroying
     * and update <code>CommandState</code> with appropriate status.
     */
    protected void checkForShutdown() {
        if (MIDletProxyList.shutdownInProgress()) {

            // The MIDlet was shutdown by either the OS or the
            // push system. Set the command state to signal this
            // to the native AMS code.
            state.status = CommandState.SHUTDOWN;
            midletProxyList.waitForShutdownToComplete();
        } else {
            state.status = CommandState.OK;
        }
    }

    /** Overrides suite close logic for the AMS task */
    protected void closeSuite() {
        /*
         * The midletSuite is not closed because the other
         * active threads may be depending on it.
         * For example, Display uses isTrusted to update
         * screen icons.
         * A native finalizer will take care of unlocking
         * the native locks.
         */
    }

    /**
     * Extends base class implementation with additional actions for main
     * task shutdown. Update and save <code>CommandState</code> instance
     * for VM cycling mechanism.
     */
    protected void done() {
        super.done();

        state.suiteId = MIDletSuite.UNUSED_SUITE_ID;
        state.midletClassName = null;

        if (state.status != CommandState.SHUTDOWN) {
            if (MIDletSuiteUtils.lastMidletSuiteToRun !=
                    MIDletSuite.UNUSED_SUITE_ID) {

                state.lastSuiteId = MIDletSuiteUtils.lastMidletSuiteToRun;
                state.lastMidletClassName = MIDletSuiteUtils.lastMidletToRun;
                state.lastArg0 = MIDletSuiteUtils.arg0ForLastMidlet;
                state.lastArg1 = MIDletSuiteUtils.arg1ForLastMidlet;
            }

            // Check to see if we need to run a selected suite next
            if (MIDletSuiteUtils.nextMidletSuiteToRun !=
                    MIDletSuite.UNUSED_SUITE_ID) {

                state.suiteId = MIDletSuiteUtils.nextMidletSuiteToRun;
                state.midletClassName = MIDletSuiteUtils.nextMidletToRun;

                state.arg0 = MIDletSuiteUtils.arg0ForNextMidlet;
                state.arg1 = MIDletSuiteUtils.arg1ForNextMidlet;
                state.arg2 = MIDletSuiteUtils.arg2ForNextMidlet;

                state.runtimeInfo.memoryReserved =
                    MIDletSuiteUtils.memoryReserved;
                state.runtimeInfo.memoryTotal = MIDletSuiteUtils.memoryTotal;
                state.runtimeInfo.priority    = MIDletSuiteUtils.priority;
                state.runtimeInfo.profileName = MIDletSuiteUtils.profileName;
                state.isDebugMode = MIDletSuiteUtils.isDebugMode;

            } else if (state.lastSuiteId !=
                    MIDletSuite.UNUSED_SUITE_ID) {

                state.suiteId = state.lastSuiteId;
                state.midletClassName = state.lastMidletClassName;
                state.arg0 = state.lastArg0;
                state.arg1 = state.lastArg1;

                /* Avoid an endless loop. */
                state.lastSuiteId = MIDletSuite.UNUSED_SUITE_ID;
                state.lastMidletClassName = null;
                state.lastArg0 = null;
                state.lastArg1 = null;

                /*
                 * This could an bad JAD from an auto test suite,
                 * so make sure the status to OK, the native
                 * code will run the last suite.
                 */
                state.status = CommandState.OK;
            }
        }

        state.save();

        // Finalize JSR subsystems
        JSRInitializer.cleanup();
    }

    /** Gracefully terminates VM with proper return code */
    protected void exitLoader() {
        /* Return specific non-zero number so the native AMS code can
         * know that this is graceful exit and not VM abort. */
        CommandState.exitInternal(CommandState.MAIN_EXIT);
    }

    /**
     * Displays an exception message to user
     * @param securityToken security token for displaying System Alert.
     * @param exceptionMsg the message text
     */
    protected void displayException(SecurityToken securityToken,
				    String exceptionMsg) {
        MIDletSuiteUtils.displayException(internalSecurityToken,
					  exceptionMsg);
    }

    /**
     * Updates CommandState status and displays proper
     * exception message to user
     *
     * @param errorCode generic error code
     * @param details text with error details
     */
    protected void reportError(int errorCode, String details) {
        state.status = errorCode;

        // Initialize display alerts state on first error handling
        if (disableAlerts < 0) {
            disableAlerts = Configuration.getIntProperty(
                "DisableStartupErrorAlert", 0);
        }

        int msgId = getErrorMessageId(errorCode);
        String errorMsg = Resource.getString(msgId);
        if (details != null) {
            errorMsg += "\n\n" + details;
        }

        if (disableAlerts == 0) {
            displayException(internalSecurityToken, errorMsg);
        }

        // Error message is always obtained for logging
        if (Logging.REPORT_LEVEL <= Logging.ERROR) {
            Logging.report(Logging.ERROR, LogChannels.LC_CORE, errorMsg);
        }
    }

    /**
     * Called at the initial start of the VM.
     * Initializes internal security and any other AMS classes related
     * classes before starting the MIDlet.
     *
     * @param args not used,
     *             a {@link CommandState} object is obtained and
     *             used for arguments
     */
    public static void main(String args[]) {
        try {
            MIDletSuiteLoader loader = new MIDletSuiteLoader();

            loader.runMIDletSuite();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Creates class instance and gets suite parameters
     * from the persistent {@link CommandState} object.
     */
    private MIDletSuiteLoader() {
        // Restore command state transfered to MIDlet suite loader
        state = CommandState.getCommandState();

        // Init internal state from the restored command state
        externalAppId = 0;
        midletDisplayName = null;
        args = new String[] {
               state.arg0, state.arg1, state.arg2};
        suiteId = state.suiteId;
        midletClassName = state.midletClassName;

        // Release command state argument references
        state.arg0 = null;
        state.arg1 = null;
        state.arg2 = null;
    }
}
