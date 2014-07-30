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

import com.sun.midp.events.EventTypes;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.NativeEvent;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.CustomItem;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * This class provides methods to send events of types 
 * handled by MDisplayEventConsumer I/F implementors. 
 * This class completely hide event construction & sending in its methods.
 * 
 * Generic comments for all XXXEventProducers:
 *
 * For each supported event type there is a separate sendXXXEvent() method, 
 * that gets all needed parameters to construct an event of an approprate class.
 * The method also performs event sending itself.
 *
 * If a given event type merges a set of logically different subtypes, 
 * this class shall provide separate methods for these subtypes.
 *
 * It is assumed that only one object instance of this class
 * is initialized with the system event that is created at (isolate) startup. 
 *
 * This class only operates on the event queue given to it during
 * construction, the class does not obtain any restricted object itself,
 * so it does not need protection.
 *
 * All MIDP stack subsystems that need to send events of supported types, 
 * must get a reference to an already created istance of this class. 
 * Typically, this instance should be passed as a constructor parameter.
 *
 * Class is NOT final to allow debug/profile/test/automation subsystems
 * to change, substitute, complement default "event sending" functionality :
 * Ex. 
 * class LogXXXEventProducer 
 *      extends XXXEventProducer {
 *  ...
 *  void sendXXXEvent(parameters) {
 *      LOG("Event of type XXX is about to be sent ...")
 *      super.sendXXXEvent(parameters);
 *      LOG("Event of type XXX has been sent successfully !")
 *  }
 *  ...
 * }
 */
public class DisplayEventProducer {
    
    /** Cached reference to the MIDP event queue. */
    private EventQueue eventQueue;
    
    /**
     * Construct a new DisplayEventProducer.
     *
     * @param  theEventQueue An event queue where new events will be posted.
     */
    public DisplayEventProducer(
        EventQueue theEventQueue) {
            
        eventQueue = theEventQueue;
    }

    /*
     * LOCAL USER INPUT EVENTS - produced by Native code
     *
     * KEY/non-IME
     * KEY/IME
     * PEN
     * CMD
     * PEER_CHANGED
     * 
     */
    /*
     * NO PRODUCER METHODS for these event types
     */

    /*
     * LOCAL DISPLAY MANAGEMENT EVENTS
     *
     * SCREEN_CHANGE
     * INVALIDATE
     */

    /**
     * Called to schedule a screen change to the given Displayable
     * as soon as possible
     *
     * @param parent parent Display of the Displayable
     * @param d The Displayable to change to
     */
    public void sendScreenChangeEvent(
            DisplayEventConsumer parent, Displayable d) {
        eventQueue.post(LCDUIEvent.createScreenChangeEvent(parent, d));
    }

    /**
     * Called to schedule an invalidation of a Form.
     *
     * @param d The Display
     */
    public void sendInvalidateEvent(DisplayEventConsumer d) {
        eventQueue.post(LCDUIEvent
    			.createBasicEvent(d, EventTypes.INVALIDATE_EVENT));
    }

    /*
     * LOCAL CALLBACK MANAGEMEMT EVENTS
     *
     * CALL_SERIALLLY
     */

    /**
     * Called to schedule a serial callback of a Runnable object passed
     * into Display's callSerially() method.
     *
     * @param d The Display
     */
    public void sendCallSeriallyEvent(DisplayEventConsumer d) {
        eventQueue.post(
            LCDUIEvent.createBasicEvent(d, EventTypes.CALL_SERIALLY_EVENT));
    }

    /*
     * TBD: add FOREGROUND_NOTIFY (or move to a separate Producer) ...
     */

    /*
     * ITEM EVENTS - not associated with a particular Display
     *
     * ITEM_CHANGED/STATE_CHANGE
     * ITEM_CHANGED/SIZE_REFRESH
     * ITEM_CHANGED/MAKE_VISIBLE
     */
    
    /**
     * Schedules a call to an ItemStateListener.
     *
     * @param src the Item which has changed, this parameter is need only
     * by <code>Form</code> however, this means that events cannot be merged.
     * If <code>Form</code> was to scan its items
     * for invalid ones in callItemStateChanged, only one of these events
     * would need to be in the queue at once.
     */
    public void sendItemStateChangeEvent(Item src) {
        eventQueue.post(
            LCDUIEvent.createItemEvent(src, LCDUIEvent.ITEM_STATE_CHANGED));
    }

    /**
     * Schedules a call requesting a CustomItem to refresh its sizes.
     *
     * @param src the CustomItem requested to be refreshed
     */
    public void sendItemSizeRefreshEvent(CustomItem src) {
        eventQueue.post(
            LCDUIEvent.createItemEvent(src, LCDUIEvent.ITEM_SIZE_REFRESH));
    }

    /**
     * Schedules a call to repaint entire screen content.
     *
     * @param d The Display
     */
    public void sendScreenRepaintEvent(DisplayEventConsumer d) {
        eventQueue.post(
            LCDUIEvent.createScreenRepaintEvent(d));
    }
}

