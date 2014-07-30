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

import javax.microedition.lcdui.Display;

import javax.microedition.midlet.*;

import com.sun.midp.content.CHManager;

import com.sun.midp.installer.OtaNotifier;

import com.sun.midp.lcdui.DisplayContainer;

import com.sun.midp.midlet.*;

import com.sun.midp.suspend.SuspendSystem;

import com.sun.midp.security.SecurityToken;

/**
 * The class implements the MIDlet state listener for the CLDC VM.
 */
class CldcMIDletStateListener implements MIDletStateListener {
    /** This class has a different security domain than the application. */
    private SecurityToken classSecurityToken;

    /** Stores array of active displays for a MIDlet suite isolate. */
    protected DisplayContainer displayContainer;

    /** Cached reference to the MIDletControllerEventProducer. */
    private MIDletControllerEventProducer midletControllerEventProducer;

    /** Indicates if the VM is in MIDlet start mode. */
    private boolean vmInMidletStartMode;

    /** Indicate if a MIDlet has been active before. */
    private boolean previouslyActive;

    /**
     * Initializes this object.
     *
     * @param token security token for this class.
     * @param theDisplayContainer display container
     * @param theMIDletControllerEventProducer event producer
     */
    CldcMIDletStateListener(SecurityToken token,
            DisplayContainer theDisplayContainer,
            MIDletControllerEventProducer theMIDletControllerEventProducer) {
        classSecurityToken = token;
        displayContainer = theDisplayContainer;
        midletControllerEventProducer = theMIDletControllerEventProducer;
    }

    /**
     * Called before a MIDlet is created.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet to be created
     */
    public void midletPreStart(MIDletSuite suite, String className) {
        /*
         * Send a hint to VM about begining of a MIDlet startup phase within
         * current isolate to allow VM to adjust internal parameters for
         * better performance
         */
        MIDletSuiteUtils.vmBeginStartUp(
            classSecurityToken, MIDletSuiteUtils.getIsolateId());
        vmInMidletStartMode = true;

        // Do ContentHandler initialization for this MIDlet
        CHManager.getManager(classSecurityToken).
            midletInit(suite.getID(), className);
    }

    /**
     * Called after a MIDlet is successfully created.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     * @param externalAppId ID of given by an external application manager
     */
    public void midletCreated(MIDletSuite suite, String className,
                              int externalAppId) {
        
        midletControllerEventProducer.sendMIDletCreateNotifyEvent(
            suite.getID(), className, externalAppId,
                suite.getMIDletName(className));

        OtaNotifier.retryInstallNotification(classSecurityToken, suite);

        if (vmInMidletStartMode) {
            /*
             * Send a hint to VM about end of a MIDlet startup phase within
             * current isolate to allow VM to restore its internal parameters
             * changed for startup time for better performance
             */
            MIDletSuiteUtils.vmEndStartUp(
               classSecurityToken, MIDletSuiteUtils.getIsolateId());
            vmInMidletStartMode = false;
        }
    }

    /**
     * Called before a MIDlet is activated.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     */
    public void preActivated(MIDletSuite suite, String className) {
        SuspendSystem.getInstance(classSecurityToken).resume();
    }

    /**
     * Called after a MIDlet is successfully activated. This is after
     * the startApp method is called.
     *
     * @param suite reference to the loaded suite
     * @param midlet reference to the MIDlet
     */
    public void midletActivated(MIDletSuite suite, MIDlet midlet) {
        String className = midlet.getClass().getName();

        /*
         * JAMS UE feature: If a MIDlet has not set a current displayable
         * in its display by the time it has returned from startApp,
         * display the headless alert. The headless alert has been
         * set as the initial displayable but for the display but the
         * foreground has not been requested, to avoid displaying the
         * alert for MIDlet that do set a current displayable.
         */
        if (!previouslyActive) {
            previouslyActive = true;

            if (Display.getDisplay(midlet).getCurrent() == null) {
                displayContainer.requestForegroundForDisplay(className);
            }
        }

        midletControllerEventProducer.sendMIDletActiveNotifyEvent(
            suite.getID(), className);
    }

    /**
     * Called after a MIDlet is successfully paused.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     */
    public void midletPaused(MIDletSuite suite, String className) {
        midletControllerEventProducer.sendMIDletPauseNotifyEvent(
            suite.getID(), className);

        /*
         * IMPL_NOTE: it is now implied that MIDlet is always
         * requested to be paused together with all the
         * suspendable resources.
         *
         * This code is not suitable when mulitple MIDlet are running in this
         * isolate.
         */
        SuspendSystem.getInstance(classSecurityToken).suspend();
        midletControllerEventProducer.sendMIDletRsPauseNotifyEvent(
            suite.getID(), className);
    }

    /**
     * Called after a MIDlet pauses itself. In this case pauseApp has
     * not been called.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     */
    public void midletPausedItself(MIDletSuite suite, String className) {
        midletControllerEventProducer.sendMIDletPauseNotifyEvent(
            suite.getID(), className);
    }

    /**
     * Called when a MIDlet calls MIDlet resume request.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     */
    public void resumeRequest(MIDletSuite suite, String className) {
        midletControllerEventProducer.sendMIDletResumeRequest(
            suite.getID(), className);
    }

    /**
     * Called after a MIDlet is successfully destroyed.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     * @param midlet reference to the MIDlet, null if the MIDlet's constructor
     *               was not successful
     */
    public void midletDestroyed(MIDletSuite suite, String className,
                                MIDlet midlet) {
        if (midlet != null) {
            displayContainer.removeDisplaysByOwner(midlet);
        }

        midletControllerEventProducer.sendMIDletDestroyNotifyEvent(
            suite.getID(), className);
    }
}
