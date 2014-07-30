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

import com.sun.midp.events.EventTypes;
import com.sun.midp.events.Event;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventListener;
import com.sun.midp.events.EventQueue;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.lcdui.DisplayEventHandler;

import com.sun.midp.security.SecurityToken;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * Handles execute MIDlet events.
 */
class ExecuteMIDletEventListener implements EventListener, Runnable {
    /** An internal security token. */
    private static SecurityToken classSecurityToken;

    /** External app ID of an installed suite to execute. */
    private int externalAppId;

    /** ID of an installed suite to execute. */
    private int id;

    /** MIDlet class name of MIDlet to invoke. */
    private String midlet;

    /** displayName name to display to the user. */
    private String displayName;

    /** arg0 if not null, is application property arg-0. */
    private String arg0;

    /** arg1 if not null, is application property arg-1. */
    private String arg1;

    /** arg2 if not null, is application property arg-2. */
    private String arg2;


    /**
     * Start listening for execute MIDlet events.
     *
     * @param token security token for initilaization
     * @param eventQueue event queue to work with
     *
     */
    static void startListening(SecurityToken token,
        EventQueue eventQueue) {
        classSecurityToken = token;
        eventQueue.registerEventListener(EventTypes.EXECUTE_MIDLET_EVENT,
                                  new ExecuteMIDletEventListener());
    }

    /**
     * Initialize an ExecuteMIDletEventListener to with execute arguments.
     */
    private ExecuteMIDletEventListener() {
    }

    /**
     * Initialize an ExecuteMIDletEventListener with arguments to execute.
     *
     *
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
     */
    private ExecuteMIDletEventListener(int externalAppId, int id,
            String midlet, String displayName, String arg0, String arg1,
            String arg2) {
        this.externalAppId = externalAppId;
        this.id = id;
        this.midlet = midlet;
        this.displayName = displayName;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    /**
     * Preprocess an event that is being posted to the event queue.
     * This implementation of the method always return true.
     *
     * @param event event being posted
     *
     * @param waitingEvent previous event of this type waiting in the
     *     queue to be processed
     *
     * @return true to allow the post to continue
     */
    public boolean preprocess(Event event, Event waitingEvent) {
        return true;
    }

    /**
     * Process an Execute event to start a new MIDlet.
     * If the MIDlet is already in the ProxyList it will <strong>not</strong>
     * start it again.
     * If the MIDlet/Isolate has decided to exit but has
     * not yet sent the remove event the execute will be unreliable.
     *
     * @param genericEvent event to process
     */
    public void process(Event genericEvent) {
        // Verify that the requested MIDlet is not already running
        // (is not in the MIDletProxyList)
        if (MIDletProxyList.getMIDletProxyList().isMidletInList(id, midlet)) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                                   "MIDlet already running; execute ignored");
                }
            return;
        }

        try {
            // The execute MIDlet method may block
            NativeEvent event = (NativeEvent)genericEvent;
            ExecuteMIDletEventListener runnable =
            new ExecuteMIDletEventListener(event.intParam1,
                               event.intParam2,
                               event.stringParam1,
                               event.stringParam2,
                               event.stringParam3,
                               event.stringParam4,
                               event.stringParam5);
            (new Thread(runnable)).start();
        } catch (Throwable t) {
            Logging.trace(t, "Error creating a new Execute thread");
        }
    }

    /**
     * Processes an execute MIDlet event outside of the event thread.
     */
    public void run() {
        try {
            MIDletSuiteUtils.executeWithArgs(classSecurityToken,
                externalAppId, id, midlet, displayName,
                    arg0, arg1, arg2, false);
        } catch (Throwable t) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(t,
                    "Exception calling MIDletSuiteLoader.execute");
            }

            MIDletSuiteUtils.displayException(classSecurityToken,
                 Resource.getString(
                 ResourceConstants.AMS_MIDLETSUITELDR_CANT_EXE_NEXT_MIDLET) +
                             "\n\n" + t.getMessage());
        }
    }
}
