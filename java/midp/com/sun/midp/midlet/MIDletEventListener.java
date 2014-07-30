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

package com.sun.midp.midlet;

import com.sun.midp.events.Event;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventListener;
import com.sun.midp.events.NativeEvent;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * Listener for MIDlet related events (state changes, etc).
 *
 */
public class MIDletEventListener implements EventListener {
    /** Cached reference to the MIDP event queue. */
    private EventQueue eventQueue;

    /** The controller of MIDlets. */
    private MIDletStateHandler midletStateHandler;

    /** This class has a different security domain than the application. */
    private static SecurityToken classSecurityToken;

    /**
     * The constructor for the default event handler for LCDUI.
     *
     * @param  token security token that has AMS permission to
     *         use restricted MIDlet state handler methods
     * @param theMIDletStateHandler the midlet state handler
     * @param theEventQueue the event queue
     */
    public MIDletEventListener(
        SecurityToken token,
        MIDletStateHandler theMIDletStateHandler,
        EventQueue theEventQueue) {
            
        token.checkIfPermissionAllowed(Permissions.AMS);

        classSecurityToken = token;

        midletStateHandler = theMIDletStateHandler;

        eventQueue = theEventQueue;

        /*
         * All events handled by this object are of NativeEventClass
         * and are instance specific events assosiated with some display Id.
         * So this listener is able to find an appropriate DisplayEventConsumer
         * associated with the displayId field of NativeEvent and 
         * to call methods of found consumer.
         */
        eventQueue.registerEventListener(EventTypes.ACTIVATE_MIDLET_EVENT, 
					this);
        eventQueue.registerEventListener(EventTypes.PAUSE_MIDLET_EVENT, this);
        eventQueue.registerEventListener(EventTypes.DESTROY_MIDLET_EVENT,
                                         this);
    }

    /**
     * Preprocess an event that is being posted to the event queue.
     * 
     * @param event event being posted
     *
     * @param waitingEvent previous event of this type waiting in the
     *     queue to be processed
     * 
     * @return true to allow the post to continue, false to not post the
     *     event to the queue
     */
    public boolean preprocess(Event event, Event waitingEvent) {
        return true;
    }

    /**
     * Process an event.
     *
     * @param event event to process
     */
    public void process(Event event) {
        NativeEvent nativeEvent = (NativeEvent)event;

        MIDletEventConsumer midletEventConsumer =
            midletStateHandler.getMIDletEventConsumer(classSecurityToken,
                nativeEvent.stringParam1);

        if (midletEventConsumer != null) {
            switch (event.getType()) {
                
            case EventTypes.ACTIVATE_MIDLET_EVENT:
                midletEventConsumer.handleMIDletActivateEvent();
                return;

            case EventTypes.PAUSE_MIDLET_EVENT:
                midletEventConsumer.handleMIDletPauseEvent();
                return;

            case EventTypes.DESTROY_MIDLET_EVENT:
                midletEventConsumer.handleMIDletDestroyEvent();
                return;

            default:
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                                   "unknown system event (" +
                                   event.getType() + ")");
                }
            }
        }
    }
}
