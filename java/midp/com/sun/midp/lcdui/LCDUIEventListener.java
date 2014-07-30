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

import com.sun.midp.events.*;
import javax.microedition.lcdui.CustomItem;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * This is the event listener for LCDUI events.
 */
public class LCDUIEventListener implements EventListener {

// ------- Private Data Members ------- //

    /** The DisplayManager object handling the display events. */
    private ItemEventConsumer itemEventConsumer;

    /** Cached reference to the MIDP event queue. */
    private EventQueue eventQueue;

// -------- Constructor --------- //

    /**
     * The constructor for the default event handler for LCDUI.
     *
     * @param  token security token that controls instance creation.
     * @param theEventQueue the event queue
     * @param  theItemEventConsumer handler for item events
     */
    public LCDUIEventListener(
        SecurityToken token,
        EventQueue theEventQueue,
        ItemEventConsumer theItemEventConsumer) {
            
        token.checkIfPermissionAllowed(Permissions.MIDP);
        
        eventQueue = theEventQueue;
        itemEventConsumer = theItemEventConsumer;

        /*
         * All events handled by this object are of [ackage private 
         * LCDUIEvent class, so only this object knows how to 
         * decode their fields. 
         */
        eventQueue.registerEventListener(EventTypes.SCREEN_CHANGE_EVENT, this);
        eventQueue.registerEventListener(EventTypes.SCREEN_REPAINT_EVENT, this);
        eventQueue.registerEventListener(EventTypes.INVALIDATE_EVENT, this);
        eventQueue.registerEventListener(EventTypes.ITEM_EVENT, this);
        eventQueue.registerEventListener(EventTypes.CALL_SERIALLY_EVENT, this);
    }

// ------- Public Methods -------- //

    /**
     * Preprocess an event that is being posted to the event queue.
     * 
     * @param newEvent event being posted
     *
     * @param waitingEvent previous event of this type waiting in the
     *     queue to be processed
     * 
     * @return true to allow the post to continue, false to not post the
     *     event to the queue
     */
    public boolean preprocess(Event newEvent, Event waitingEvent) {
        switch (newEvent.getType()) {
        case EventTypes.INVALIDATE_EVENT:
        case EventTypes.CALL_SERIALLY_EVENT:
            return preprocessAllowOnlyOneEvent(newEvent, waitingEvent);
        case EventTypes.SCREEN_CHANGE_EVENT:
            return preprocessScreenChangeEvent
                        (newEvent, waitingEvent);
        // case EventTypes.ITEM_EVENT: 
        default:
            return true;
        }
    }

    /**
     * Process an event.
     *
     * @param event event to process
     */
    public void process(Event event) {
        LCDUIEvent lcduiEvent = (LCDUIEvent)event;
        
        switch (event.getType()) {
        case EventTypes.SCREEN_CHANGE_EVENT:
            /*
             * Target DisplayEventConsumer is obtained directly 
             * from event field.
             * Assumed that target consumer is not null.
             */
            lcduiEvent.display.handleScreenChangeEvent(lcduiEvent.nextScreen);
            return;

        case EventTypes.SCREEN_REPAINT_EVENT:
            /*
             * Target DisplayEventConsumer is obtained directly
             * from event field.
             * Assumed that target consumer is not null.
             */
            lcduiEvent.display.handleScreenRepaintEvent();
            return;

        case EventTypes.INVALIDATE_EVENT:
            /*
             * Target DisplayEventConsumer is obtained directly 
             * from event field.
             * Assumed that target consumer is not null.
             */
            lcduiEvent.display.handleInvalidateEvent();
            return;

        case EventTypes.ITEM_EVENT:
            /*
             * Item Events are processed by ItemEventConsumer.
             */
            itemEvent(lcduiEvent);
            return;
            
        case EventTypes.CALL_SERIALLY_EVENT:
            /*
             * Target DisplayEventConsumer is obtained directly 
             * from event field.
             * Assumed that target consumer is not null.
             */
            lcduiEvent.display.handleCallSeriallyEvent();
            return;
            
        default:
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                               "unknown system event (" +
                               event.getType() + ")");
            }
        }
    }

// ------- Private Event Handling Routines -------- //

    /**
     * Preprocess an event to only allow one in the queue
     * at once.
     *
     * @param genericEvent1 LCDUI event
     * @param genericEvent2 waiting LCDUI event
     *
     * @return true if the event should be put in the queue, false if
     * the event should not be put in the queue because it has been merged
     * with the event currently waiting in the queue.
     */
    private boolean preprocessAllowOnlyOneEvent(
        Event genericEvent1, 
        Event genericEvent2) {
        if (genericEvent2 == null) {
            // There is no other event, queue this event
            return true;
        }

        return false;
    }

    /**
     * Preprocess a screen change event to only allow one with the same
     * screen in the queue at once.
     *
     * @param genericEvent1 LCDUI event
     * @param genericEvent2 waiting LCDUI event
     *
     * @return true if the event should be put in the queue, false if
     * the event should not be put in the queue because it is duplicate
     * of the event currently waiting in the queue.
     */
    private boolean preprocessScreenChangeEvent(Event genericEvent1,
            Event genericEvent2) {
        LCDUIEvent newEvent;
        LCDUIEvent waitingEvent;

        if (genericEvent2 == null) {
            // There is no other event, queue this event
            return true;
        }

        newEvent = (LCDUIEvent)genericEvent1;
        waitingEvent = (LCDUIEvent)genericEvent2;

        if (newEvent.nextScreen != waitingEvent.nextScreen) {
             // The events are different, queue this event
             return true;
        }

        // The events are the same event , do not queue this event
        return false;
    }

    /**
     * Processes event of ITEM_EVENT class
     *
     * @param lcduiEvent LCDUI event to be processed
     */
    private void itemEvent(LCDUIEvent lcduiEvent) {
        switch (lcduiEvent.minorCode) {
        case LCDUIEvent.ITEM_STATE_CHANGED:
            itemEventConsumer.handleItemStateChangeEvent(
                lcduiEvent.changedItem);
            break;
        case LCDUIEvent.ITEM_SIZE_REFRESH:
            itemEventConsumer.handleItemSizeRefreshEvent(
                (CustomItem)lcduiEvent.changedItem);
            break;
        default:
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                               "ITEM_EVENT invalid minor code (" +
                               lcduiEvent.minorCode + ")");
            }
            break;
        }
        return;
    }
}
