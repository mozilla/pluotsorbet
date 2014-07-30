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
import com.sun.midp.lcdui.DisplayEventHandlerFactory;
import com.sun.midp.lcdui.DisplayEventHandler;
import com.sun.midp.lcdui.SystemAlert;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Command;
import java.util.Timer;
import java.util.TimerTask;

/**
 * User interface utilities for suspend/resume subsystem.
 */
class SuspendResumeUI {
    /**
     * Alert notifying user of system suspension. It is null if the alert
     * is not being shown currently.
     */
    private static SystemAlert suspendAlert;

    /**
     * Shows alert notifying user of system suspend.
     * @param token security token for accessing restricted API
     */
    static synchronized void showSuspendAlert(final SecurityToken token) {
        if (null == suspendAlert && AlertTimer.shouldShow()) {
            String title = Resource.getString(
                ResourceConstants.SR_SUSPEND_ALERT_TITLE, null);
            String message = Resource.getString(
                ResourceConstants.SR_SUSPEND_ALERT_MSG, null);

            AlertTimer.start();

            CommandListener ignoring = new CommandListener() {
                public void commandAction(Command c, Displayable d) {}
            };

            suspendAlert = new SystemAlert(getDisp(token), title,
                message, null, AlertType.WARNING);
            suspendAlert.setCommandListener(ignoring);

            suspendAlert.runInNewThread();
        }
    }

    /**
     * Dismisses alert notifying user of system suspend.
     */
    static synchronized void dismissSuspendAlert() {
        if (null != suspendAlert) {
            suspendAlert.dismiss();
            suspendAlert = null;
        }
    }

    /**
     * Shows alert notifying user that all MIDlets were killed during
     * preceding system suspension.
     * @param token security token for accessing restricted API
     */
    static void showAllKilledAlert(SecurityToken token) {
        String title = Resource.getString(
            ResourceConstants.SR_ALL_KILLED_ALERT_TITLE, null);
        String message = Resource.getString(
            ResourceConstants.SR_ALL_KILLED_ALERT_MSG, null);

        SystemAlert alert = new SystemAlert(getDisp(token), title,
                message, null, AlertType.WARNING);
        alert.runInNewThread();
    }

    /**
     * Retrieves a display handler that guarantees exposition of a
     * system alert.
     * @param token security token for accessing restricted API
     * @return DisplayEventHandler instance for alert exposition
     */
    private static DisplayEventHandler getDisp(SecurityToken token) {
        /* IMPL_NOTE: Due to current implementation of display preempting,
         * alert is shown immediately only if launched from the isolate
         * that has foreground. Moving AMS isolate to foreground to
         * get display preemption.
         */
        MIDletProxyList proxyList = MIDletProxyList.getMIDletProxyList(token);
        proxyList.setForegroundMIDlet(proxyList.findAmsProxy());

        return DisplayEventHandlerFactory.getDisplayEventHandler(token);
    }
}

/**
 * Alert timer that guarantees exposition of suspend alert.
 */
class AlertTimer extends Timer implements SuspendDependency {
    /** The timeout. The alert is not shown if this timeout is 0. */
    private static final long TIMEOUT =
            Configuration.getIntProperty("suspendAlertTime", 0);

    /**
     * Determines whether the suspend alert should be shown at all,
     * zero timeout means that it should not.
     * @return true if timeout is configured for showing the alert,
     *         false if the alert should not be shown.
     */
    static boolean shouldShow() {
        return 0 != TIMEOUT;
    }

    /**
     * Starts the timer that prevents system from suspension. When timeout
     * expires, the timer will not prevent from suspension any more.
     */
    static void start() {
        if (0 == TIMEOUT) {
            return;
        }

        final AlertTimer timer = new AlertTimer();
        SuspendSystem.getInstance().addSuspendDependency(timer);

        TimerTask removeDep = new TimerTask() {
            public void run() {
                SuspendSystem.getInstance().removeSuspendDependency(timer);
            }
        };

        timer.schedule(removeDep, TIMEOUT);
    }
}