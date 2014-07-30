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

import com.sun.midp.midlet.*;
import com.sun.midp.security.*;
import com.sun.midp.events.EventQueue;
import com.sun.midp.lcdui.*;
import com.sun.midp.configurator.Constants;
import com.sun.midp.log.*;
import com.sun.midp.installer.InternalMIDletSuiteImpl;

/**
 * The class presents abstract MIDlet suite loader with routines to prepare
 * runtime environment for a suite execution. The class provides generic
 * scenario to load, start and terminate a MIDlet suite in the common way
 * for both SVM and MVM modes. All the differences of SVM & MVM modes are
 * designed as virtual or abstract methods.
 */
abstract class AbstractMIDletSuiteLoader
	implements MIDletSuiteExceptionListener {

    /** The ID of the MIDlte suite task Isolate */
    protected int isolateId;

    /** The ID of the AMS task Isolate */
    protected int amsIsolateId;

    /** Suite ID of the MIDlet suite */
    protected int suiteId;

    /** External application ID that can be provided by native AMS */
    protected int externalAppId;

    /** Name of the class to start MIDlet suite execution */
    protected String midletClassName;

    /** Display name of a MIDlet suite */
    protected String midletDisplayName;

    /** The arguments to start MIDlet suite with */
    protected String args[];

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted implements ImplicitlyTrustedClass {}

    /** This class has a different security domain than the MIDlet suite */
    protected static SecurityToken internalSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /** Event queue instance created for this MIDlet suite execution */
    protected EventQueue eventQueue;

    /**
     * MIDlet suite instance created and properly initialized for
     * a MIDlet suite invocation.
     */
    protected MIDletSuite midletSuite;

    /** Foreground Controller adapter. */
    protected ForegroundController foregroundController;

    /** Stores array of active displays for a MIDlet suite isolate. */
    protected DisplayContainer displayContainer;

    /**
     * Provides interface to lcdui environment.
     */
    protected LCDUIEnvironment lcduiEnvironment;

    /** Starts and controls MIDlets through the lifecycle states. */
    protected MIDletStateHandler midletStateHandler;

    /**
     * Reports an error detected during MIDlet suite invocation.
     * @param errorCode the error code to report
     */
    protected abstract void reportError(int errorCode, String details);
    
    /**
     * Reports an error detected during MIDlet suite invocation.
     * @param errorCode the error code to report
     */
    protected void reportError(int errorCode) {
        reportError(errorCode, null);
    }

    /**
     * Allocates resources for a suite execution according to
     * global resource policy.
     *
     * @return true in the case resources were successfully allocated,
     *   false otherwise
     */
    protected boolean allocateReservedResources() {
        return true;
    }

    /**
     * Sets MIDlet suite arguments as temporary suite properties.
     * Subclasses can override the method to export any other needed
     * suite properties.
     */
    protected void setSuiteProperties() {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    midletSuite.setTempProperty(
                        internalSecurityToken,
                        "arg-" + i, args[i]);
                }
            }
        }
    }

    /** Core initialization of a MIDlet suite loader */
    protected void init() {
        // Init security tokens for core subsystems
        SecurityInitializer.initSystem();

        eventQueue = EventQueue.getEventQueue(
            internalSecurityToken);
    }

    /** Final actions to finish a MIDlet suite loader */
    protected void done() {
        eventQueue.shutdown();
    }

    /**
     * Creates all needed objects of a MIDlet suite environment, but
     * only initialization that is done, will be to pass other created objects,
     * and the current and AMS isolate IDs. It is mostly event-related
     * objects, however subclasses can extend the environment with more
     * specific parts
     */
    protected void createSuiteEnvironment() {

	if (lcduiEnvironment == null) {
	    throw new
		RuntimeException("Suite environment not complete.");
	}

        displayContainer = lcduiEnvironment.getDisplayContainer();
    }

    /**
     * Does all initialization for already created objects of a MIDlet suite
     * environment. Subclasses can also extend the initialization with
     * various global system initializations needed for all suites.
     * The MIDlet suite has been created at this point, so it can be
     * used to initialize any per suite data.
     */
    protected void initSuiteEnvironment() {
        lcduiEnvironment.setTrustedState(midletSuite.isTrusted());
    }

    /**
     * Handles exception occurred during MIDlet suite execution.
     * @param t exception instance
     */
    public abstract void handleException(Throwable t);

    /** Restricts suite access to internal API */
    protected void restrictAPIAccess() {
        // IMPL_NOTE: No restrictions by default
    }

    /**
     * Start MIDlet suite in the prepared environment
     * @throws Exception can be thrown during execution
     */
    protected void startSuite() throws Exception {
        if (suiteId == -1 && midletClassName.equals("internal")) {
            // no class name, need to look for it in the JAD file
            midletClassName = 
                ((InternalMIDletSuiteImpl)midletSuite).getMIDletClassName();
        }

        midletStateHandler.startSuite(
            this, midletSuite, externalAppId, midletClassName);
    }

    /** Closes suite and unlock native suite locks */
    protected void closeSuite() {
        if (midletSuite != null) {
            /* When possible, don't wait for the finalizer
             * to unlock the suite. */
            midletSuite.close();
        }

        if (lcduiEnvironment != null) {
            lcduiEnvironment.shutDown();
        }
    }

    /**
     * Checks whether an executed MIDlet suite has requested
     * for a system shutdown. User MIDlets most probably have
     * no right for it, however Java AMS MIDlet could do it.
     */
    protected void checkForShutdown() {
        // IMPL_NOTE: No checks for shutdown by default
    }

    /**
     * Explicitly requests suite loader exit after MIDlet
     * suite execution is finished and created environment is done.
     */
    protected abstract void exitLoader();

    /**
     * Creates MIDlet suite instance by suite ID, the 
     *
     * @return MIDlet suite to load
     *
     * @throws Exception in the case MIDlet suite can not be
     *   created because of a security reasons or some problems
     *   related to suite storage
     */
    protected abstract MIDletSuite createMIDletSuite() throws Exception;

    /**
     * Inits MIDlet suite runtime environment and start a MIDlet
     * suite with it
     */
    protected void runMIDletSuite() {
        // WARNING: Don't add any calls before this!
        //
        // The core init of a MIDlet suite task should be able
        // to perform the very first initializations of the environment
        init();

        try {
            /*
             * Prepare MIDlet suite environment, classes that only need
             * the isolate ID can be created here.
             */
            createSuiteEnvironment();

            /* Check to see that all of the core object are created. */
            if (foregroundController == null ||
                displayContainer == null ||
                midletStateHandler == null) {

                throw new
                    RuntimeException("Suite environment not complete.");
	    }

            // Regard resource policy for the suite task
            if (!allocateReservedResources()) {
                reportError(Constants.MIDLET_RESOURCE_LIMIT);
                return;
            }          

            // Create suite instance ready for start
            midletSuite = createMIDletSuite();

            if (midletSuite == null) {
                reportError(Constants.MIDLET_SUITE_NOT_FOUND);
                return;
            }

            if (!midletSuite.isEnabled()) {
                reportError(Constants.MIDLET_SUITE_DISABLED);
                return;
            }

            /*
             * Now that we have the suite and reserved its resources
             * we can initialize any classes that need MIDlet Suite
             * information.
             */
            initSuiteEnvironment();

            // Export suite arguments as properties, so well
            // set any other properties to control a suite
            setSuiteProperties();

            // Restrict suite access to internal API
            restrictAPIAccess();

            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                    "MIDlet suite task starting a suite");
            }

            // Blocking call to start MIDlet suite
            // in the prepared environment
            startSuite();

            // Check for shutdown possibly requested from the suite
            checkForShutdown();

            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_CORE,
                    "MIDlet suite loader exiting");
            }

        } catch (Throwable t) {
           handleException(t);

        } finally {
            closeSuite();
            done();
            exitLoader();
        }
    }
}
