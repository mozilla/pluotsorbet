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

package com.sun.midp.suspend;

import com.sun.midp.main.*;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import java.util.Vector;

/**
 * Main system of the current isolate that contains all 
 * suspendable subsystems in current isolate.
 * There is a singleton instance in each isolate. The 
 * instance kept in the AMS isolate is a special one and 
 * belongs to <code>MIDPSystem</code> subtype.
 */
public class SuspendSystem extends AbstractSubsystem {
    /**
     * Listeners interested in suspend/resume operations.
     */
    private final Vector listeners = new Vector(1, 2);

    /**
     * Main subsystem that implements suspend actions for
     * whole MIDP system. It works in the AMS Isolate. There
     * is the only instance for all isolates.
     */
    private static class MIDPSystem extends SuspendSystem
            implements MIDletProxyListListener {
        /**
         * A flag to determine if at least one MIDlet has been
         * destroyed during last suspend processing.
         */
        private boolean midletKilled = false;

        /**
         * A flag to determine if at least one MIDlet has been
         * successfully paused during last suspend processing.
         */
        private boolean midletPaused = false;

        /**
         * Stored foreground MIDlet.
         */
        private MIDletProxy lastForeground;

        /**
         * The MIDlet proxy list.
         */
        MIDletProxyList mpl =
                MIDletProxyList.getMIDletProxyList(classSecurityToken);

        /**
         * Constructs the only instance.
         */
        private MIDPSystem() {
            state = ACTIVE;

            mpl.addListener(this);
            addListener(mpl);
        }

        /**
         * Initiates MIDPSystem suspend operations.
         */
        public synchronized void suspend() {
            if (ACTIVE == state) {
                SuspendTimer.start(mpl);
                lastForeground = mpl.getForegroundMIDlet();
                SuspendResumeUI.showSuspendAlert(classSecurityToken);
                super.suspend();
            }
        }

        /**
         * Performs MIDPSystem-specific suspend operations.
         */
        protected synchronized void suspendImpl() {
            SuspendTimer.stop();
        }

        /**
         * Performs MIDPSystem-specific resume operations.
         */
        protected synchronized void resumeImpl() {
            midletKilled = false;
            midletPaused = false;
            
            SuspendResumeUI.dismissSuspendAlert();
            alertIfAllMidletsKilled();

            if (null != lastForeground) {
                mpl.setForegroundMIDlet(lastForeground);
                lastForeground = null;
            }
        }

        /**
         * Shows proper alert if all user midlets were killed by a preceding
         * suspend operation, and the event is not reported yet.
         */
        private synchronized void alertIfAllMidletsKilled() {
            if (allMidletsKilled()) {
                SuspendResumeUI.showAllKilledAlert(classSecurityToken);
            }
        }

        /**
         * Notifies of system suspend.
         */
        protected void suspended() {
            super.suspended();
            suspended0(!midletPaused && midletKilled);
        }

        /**
         * Notifies native functionality that MIDP activities in java
         * have been suspended.
         * @param allMidletsKilled true to indicate that all user MIDlets
         *        were killed by suspend routines.
         */
        protected native void suspended0(boolean allMidletsKilled);

        /**
         * Determines if at least one of preceding suspension operations
         * killed all user MIDlets and  the condition has not been checked
         * since that time.
         * @return true if a suspension operation killed all user MIDlets
         *         and the condition has not been checked yet, false
         *         otherwise. This method returns true only once for one
         *         event.
         */
        protected native boolean allMidletsKilled();

        /**
         * Receives notifications on MIDlet updates and removes corresponding
         * MIDlet proxy from suspend dependencies if required.
         * @param midlet MIDletProxy that represents the MIDlet updated
         * @param reason kind of changes that took place, see
         */
        public void midletUpdated(MIDletProxy midlet, int reason) {
            boolean amsMidlet =
                (MIDletSuiteUtils.getAmsIsolateId() == midlet.getIsolateId());

            if (reason == MIDletProxyListListener.RESOURCES_SUSPENDED) {
                if (!amsMidlet) {
                    midletPaused = true;
                }
                removeSuspendDependency(midlet);
            } else if (reason == MIDletProxyListListener.MIDLET_STATE &&
                    amsMidlet &&
                    midlet.getMidletState() == MIDletProxy.MIDLET_ACTIVE) {
                /* An AMS midlet has been activated, checking if it is a
                 * result of abnormal midlet termination during suspend.
                 */
                alertIfAllMidletsKilled();
            }
        }

        /**
         * Receives MIDlet removal notification and removes corresponding
         * MIDlet proxy from suspend dependencies.
         * @param midlet MIDletProxy that represents the MIDlet removed
         */
        public synchronized void midletRemoved(MIDletProxy midlet) {
            midletKilled = true;
            removeSuspendDependency(midlet);
            if (midlet == lastForeground) {
                lastForeground = null; 
            }
        }

        /**
         * Not used. MIDletProxyListListener interface method.
         */
        public void midletAdded(MIDletProxy midlet) {}

        /**
         * Not used. MIDletProxyListListener interface method.
         */
        public void midletStartError(int externalAppId, int suiteId,
                                     String className, int errorCode,
                                     String errorDetails) {}
    }

    /**
     * The singleton instance.
     */
    private static SuspendSystem instance =
        MIDletSuiteUtils.getIsolateId() == MIDletSuiteUtils.getAmsIsolateId()?
                new MIDPSystem() : new SuspendSystem();

    /**
     * Retrieves the singleton instance.
     * @param token security token that identifies caller permissions for
     *        accessing this API
     * @return the singleton instance
     */
    public static SuspendSystem getInstance(SecurityToken token) {
        token.checkIfPermissionAllowed(Permissions.MIDP);
        return instance;
    }

    /**
     * Retrieves the singleton instance. The method is only available from
     * this restricted package.
     * @return the singleton instance
     */
    static SuspendSystem getInstance() {
        return instance;
    }

    /**
     * Constructs an instance.
     */
    private SuspendSystem() {}

    /**
     * Registers a listener interested in system suspend/resume operations.
     * IMPL_NOTE: method for removing listeners is not needed currently.
     *
     * @param listener the listener to be added
     */
    public void addListener(SuspendSystemListener listener) {
        synchronized (listeners) {
            listeners.addElement(listener);
        }
    }

    /**
     * Notifies listeners of system suspend.
     */
    protected void suspended() {
        synchronized (listeners) {
            for (int i = listeners.size() - 1; i >= 0; i-- ) {
                SuspendSystemListener listener =
                        (SuspendSystemListener)listeners.elementAt(i);
                listener.midpSuspended();
            }
        }
    }

    /**
     * Notifies listeners of system resume.
     */
    protected void resumed() {
        synchronized (listeners) {
            for (int i = listeners.size() - 1; i >= 0; i-- ) {
                SuspendSystemListener listener =
                        (SuspendSystemListener)listeners.elementAt(i);
                listener.midpResumed();
            }
        }
    }

    /**
     * Checks if the system was requested to be resumed.
     *
     * @return true if the resume request was received, false otherwise
     */
    native boolean isResumePending();
}
