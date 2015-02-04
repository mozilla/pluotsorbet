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

import com.sun.cldc.isolate.Isolate;
import com.sun.midp.io.j2me.pipe.Protocol;
import com.sun.midp.links.Link;
import com.sun.midp.links.LinkPortal;
import com.sun.midp.security.Permissions;
import com.sun.midp.services.SystemServiceLinkPortal;

/**
 * The first class loaded in an application Isolate by the MIDP AMS to
 * initialize internal security and start a MIDlet suite.
 */
public class AppIsolateMIDletSuiteLoader extends CldcMIDletSuiteLoader {

    /** Guards against multiple use in an Isolate. */
    protected static boolean inUse;

    /** Cached reference to the current Isolate */
    protected Isolate currentIsolate;

    /** Event producer to send events for other MIDlets execution */
    protected MIDletExecuteEventProducer midletExecuteEventProducer;

    /**
     * Creates class instance and gets suite parameters
     * from array with arguments
     *
     * @param args the arguments passed to main class of the Isolate
     */
    private AppIsolateMIDletSuiteLoader(String args[]) {
        this.suiteId = Integer.parseInt(args[0]);
        this.midletClassName = args[1];
        this.midletDisplayName = args[2];
        this.args = new String[] {args[3], args[4], args[5]};
        this.externalAppId = Integer.parseInt(args[6]);
    }

    /** Inits suite loader instance */
    protected void init() {
        currentIsolate = Isolate.currentIsolate();
        super.init();
    }

    /**
     * Extends base class implementation with
     * creation of additional event producers
     */
    protected void createSuiteEnvironment() {
        super.createSuiteEnvironment();

        // Create event producer to execute other MIDlets
        // from non-AMS tasks
        midletExecuteEventProducer =
            new MIDletExecuteEventProducer(
                internalSecurityToken,
                eventQueue,
                amsIsolateId);
    }

    /**
     * Extends base class implementation with MVM specific
     * initializtion of the <code>AmsUtil</code> class
     */
    protected void initSuiteEnvironment() {
        super.initSuiteEnvironment();

        AmsUtil.initClassInAppIsolate(
            midletExecuteEventProducer);

        com.sun.midp.io.j2me.pipe.Protocol.initUserContext();
    }

    /** Restricts suite access to internal API */
    protected void restrictAPIAccess() {
        if (midletSuite.checkPermission(
            Permissions.getName(Permissions.AMS)) != 1) {

            // Permission is not allowed.
            //
            // Shutdown access to Isolate references before a MIDlet is
            // loaded. This will not effect the reference already obtained.
            currentIsolate.setAPIAccess(false);
        }
    }

    /**
     * Posts suite task error to event system.
     *
     * @param errorCode the error code to report
     * @param details text with error details
     */
    protected void reportError(int errorCode, String details) {
        midletControllerEventProducer.sendMIDletStartErrorEvent(
            suiteId, midletClassName, externalAppId,
            errorCode, details);
    }

    /** Exits suite loader Isolate with proper exit code. */
    protected void exitLoader() {
        currentIsolate.exit(0);
    }

    /**
     * Called for isolates other than the initial one.
     * Initializes internal security, and starts the MIDlet.
     *
     * @param args arg[0] the suite ID, arg[1] the class name of the MIDlet,
     *             arg[2] the name of the MIDlet to display,
     *             arg[3] optional MIDlet arg 0, arg[4] optional MIDlet arg 1,
     *             arg[5] optional MIDlet arg 2
     */
    public static void main(String args[]) {
        try {
            /* This class shouldn't be used more than once. */
            if (inUse) {
                throw new IllegalStateException();
            }
            
            inUse = true;
            new AppIsolateMIDletSuiteLoader(args).runMIDletSuite();
        } catch (Throwable t) {
            handleFatalError(t);
        }
    }

    /**
     * Native cleanup code, called when this isolate is done,
     * even if killed.
     */
    private native void finalize();

    /**
     * Handles a fatal error
     *
     * @param t the Throwable that caused the fatal error
     */
    private static native void handleFatalError(Throwable t);
}
