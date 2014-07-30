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

package com.sun.midp.lcdui;

import javax.microedition.lcdui.*;
import com.sun.midp.events.EventQueue;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

/**
 * Display a preempting alert and wait for the user to acknowledge it.
 */
public class SystemAlert extends Alert
    implements CommandListener, Runnable {

    /** Preempt token for displaying errors. */
    private Object preemptToken;

    /** The display event handler for displaying errors. */
    private DisplayEventHandler displayEventHandler;

    /** Explicit command lstener for this alert, null if not set. */
    private CommandListener explicitListener;

    /** Synchronization lock for setting explicit command listener. */
    private final Object listenerLock = new Object();

    /** Flag to identify if the alert is being displayed currently. */
    private boolean shown = false;

    /**
     * Construct an <code>SystemAlert</code>.
     *
     * @param displayEventHandler The display event handler for error display
     * @param title The title of the <tt>Alert</tt>
     * @param text The text of the <tt>Alert</tt>
     * @param image An <tt>Image</tt> to display on the <tt>Alert</tt>
     * @param type The <tt>Alert</tt> type
     */
    public SystemAlert(DisplayEventHandler displayEventHandler,
                       String title, String text,
                       Image image, AlertType type) {

        super(title, text, image, type);

        setTimeout(Alert.FOREVER);

        super.setCommandListener(this);

        this.displayEventHandler = displayEventHandler;

    }

    /**
     * Construct an <code>SystemAlert</code>.
     *
     * @param securityToken The <tt>SecurityToken</tt> 
     * @param title The title of the <tt>Alert</tt>
     * @param text The text of the <tt>Alert</tt>
     * @param image An <tt>Image</tt> to display on the <tt>Alert</tt>
     * @param type The <tt>Alert</tt> type
     */
    public SystemAlert(SecurityToken securityToken,
                       String title, String text,
                       Image image, AlertType type) {

        super(title, text, image, type);

	securityToken.checkIfPermissionAllowed(Permissions.MIDP);
        this.displayEventHandler = 
	    DisplayEventHandlerFactory.getDisplayEventHandler(securityToken);

        setTimeout(Alert.FOREVER);

        super.setCommandListener(this);
    }

    /** Waits for the user to acknowledge the alert. */
    public synchronized void waitForUser() {
        if (!shown) {
            return;
        }

        if (EventQueue.isDispatchThread()) {
            // Developer programming error
            throw new RuntimeException(
                "Blocking call performed in the event thread");
        }

        try {
            wait();
        } catch (Throwable t) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                              "Throwable while SystemAlert.waitForUser");
            }
        }
    }

    /** Dismiss the alert */
    public synchronized void dismiss() {
        if (shown) {
            notify(); // wait up waitForUser() thread
            displayEventHandler.donePreempting(preemptToken);
            preemptToken = null;
            shown = false;
        }
    }

    /**
     * Respond to a command issued on this alert.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        synchronized (listenerLock) {
            if (null != explicitListener) {
                explicitListener.commandAction(c, s);
            } else {
                dismiss();
            }
        }

    }

    /**
     * Assigns explicit command listener to this alert. If an non-null
     * explcit listener its commandAction() method is called to process
     * a command, otherwise default dismiss() action is used.
     *
     * @param cl expilict command listener, null to remove any explicit
     *          listener
     */
    public void setCommandListener(CommandListener cl) {
        synchronized (listenerLock) {
            explicitListener = cl;
        }
    }

    /**
     * Displays this alert. Since alert displaying may be blocking, it is
     * not allowed in the event dispatching thread. Nothing is done when
     * the method is called from the dispatching thread, to produce a
     * system alert from ituse runInNewThread(). Nothing is done if the
     * alert is being displayed currently.
     */
    public synchronized void run() {
        shown = true;

        if (preemptToken != null) {
            return;
        }

        try {
            preemptToken =
                displayEventHandler.preemptDisplay(this, true);
        } catch (Throwable e) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                              "Throwable while preempting Display");
            }
        }
    }

    /**
     * Launches a new thread and displays this alert from it. Use this method
     * to avoid blocking a thread that produces the alert. Makes nothing if
     * the alert is being displayed currently.
     */
    public synchronized void runInNewThread() {
        shown = true;
        new Thread(this).start();
    }
}
