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

import com.sun.midp.configurator.Constants;

import com.sun.midp.events.Event;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventListener;
import com.sun.midp.events.NativeEvent;

import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.lcdui.DisplayContainer;
import com.sun.midp.lcdui.DisplayDeviceContainer;
import com.sun.midp.lcdui.DisplayEventConsumer;
import com.sun.midp.lcdui.DisplayAccess;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.nokia.mid.ui.gestures.GestureEvent;
import com.nokia.mid.ui.gestures.GestureEventImpl;

/**
 * Listener for LCDUI events (user inputs, etc).
 */
public class DisplayEventListener implements EventListener {

    /** Active displays. */
    private DisplayContainer displayContainer;
    
     /** Display devices. */
    private DisplayDeviceContainer displayDeviceContainer;
 
   /** Cached reference to the MIDP event queue. */
    private EventQueue eventQueue;

    /**
     * The constructor for the default event handler for LCDUI.
     *
     * @param theEventQueue the event queue
     * @param theDisplayContainer container for display objects
     */
    public DisplayEventListener(
        EventQueue theEventQueue, 
        DisplayContainer theDisplayContainer,
        DisplayDeviceContainer theDisplayDeviceContainer) {

        eventQueue = theEventQueue;
        displayContainer = theDisplayContainer;
        displayDeviceContainer = theDisplayDeviceContainer;

        /*
         * All events handled by this object are of NativeEventClass
         * and are instance specific events assosiated with some display Id.
         * So this listener is able to find an appropriate DisplayEventConsumer
         * associated with the displayId field of NativeEvent and 
         * to call methods of found consumer.
         */
        eventQueue.registerEventListener(EventTypes.KEY_EVENT, this);
        eventQueue.registerEventListener(EventTypes.PEN_EVENT, this);
        eventQueue.registerEventListener(EventTypes.COMMAND_EVENT, this);
        eventQueue.registerEventListener(EventTypes.PEER_CHANGED_EVENT, this);
        eventQueue.registerEventListener(EventTypes.ROTATION_EVENT,this);
        eventQueue.registerEventListener(EventTypes.DISPLAY_DEVICE_STATE_CHANGED_EVENT,this);
	eventQueue.registerEventListener(EventTypes.DISPLAY_CLAMSHELL_STATE_CHANGED_EVENT,this);	
        eventQueue.registerEventListener(EventTypes.VIRTUAL_KEYBOARD_EVENT,this);
        eventQueue.registerEventListener(EventTypes.CHANGE_LOCALE_EVENT,this);
        eventQueue.registerEventListener(EventTypes.GESTURE_EVENT, this);
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
        NativeEvent nativeEvent = (NativeEvent) event;

        if (event.getType() == EventTypes.CHANGE_LOCALE_EVENT) {
            DisplayEventConsumer[] consumers =
                    displayContainer.getAllDisplayEventConsumers();
            for (int i = 0; i < consumers.length; i++) {
                if (consumers[i] != null) {
                    consumers[i].handleChangeLocaleEvent();
                }
            }
            return;
        }
        /*
        * Find DisplayEventConsumer instance by nativeEvent.intParam4
        * and (if not null) call DisplayEventConsumer methods ...
        */

	if (event.getType() ==  EventTypes.DISPLAY_DEVICE_STATE_CHANGED_EVENT) {
	    displayDeviceContainer.getDisplayDeviceById(nativeEvent.intParam1).setState(nativeEvent.intParam2);
	    DisplayAccess das[] =  displayContainer.findDisplaysByHardwareId(nativeEvent.intParam1);
	    for (int i = das.length; --i >= 0;) {
		    das[i].getDisplayEventConsumer().handleDisplayDeviceStateChangedEvent(nativeEvent.intParam2);
	    }
	    return;
	}

        DisplayEventConsumer dc =
                displayContainer.findDisplayEventConsumer(nativeEvent.intParam4);

        if (dc != null) {
            switch (event.getType()) {
                case EventTypes.KEY_EVENT:
                    if (nativeEvent.intParam1 == EventConstants.IME) {
                        dc.handleInputMethodEvent(nativeEvent.stringParam1);
                    }
                    if (Logging.REPORT_LEVEL < Constants.LOG_DISABLED &&
                            nativeEvent.intParam2 == EventConstants.DEBUG_TRACE1) {
                        // This is a special VM hook to print all stacks
                        if (nativeEvent.intParam1 == EventConstants.PRESSED) {
                            System.getProperty("__debug.only.pss");
                        }
                    } else {
                        dc.handleKeyEvent(
                                nativeEvent.intParam1,
                                nativeEvent.intParam2);
                    }
                    return;

                case EventTypes.PEN_EVENT:
                    dc.handlePointerEvent(
                            nativeEvent.intParam1,
                            nativeEvent.intParam2,
                            nativeEvent.intParam3);
                    return;

                case EventTypes.COMMAND_EVENT:
                    dc.handleCommandEvent(
                            /* nativeEvent.intParamX - will contain screenId */
                            nativeEvent.intParam1);
                    return;

                case EventTypes.PEER_CHANGED_EVENT:
                    dc.handlePeerStateChangeEvent(
                            nativeEvent.intParam1,
                            nativeEvent.intParam2,
                            nativeEvent.intParam3,
                            nativeEvent.intParam5);

                    return;

                case EventTypes.ROTATION_EVENT:
                    dc.handleRotationEvent();
                    return;

	    case EventTypes.DISPLAY_CLAMSHELL_STATE_CHANGED_EVENT:
		    dc.handleClamshellEvent();
		return;
		    

                case EventTypes.VIRTUAL_KEYBOARD_EVENT:
                    dc.handleVirtualKeyboardEvent();
                    return;

                case EventTypes.GESTURE_EVENT:
                    System.out.println("GESTUREEVENT");
                    GestureEvent gestureEvent = new GestureEventImpl(nativeEvent.intParam1,
                                                              nativeEvent.intParam2,
                                                              nativeEvent.intParam3,
                                                              nativeEvent.intParam5,
                                                              nativeEvent.intParam6,
                                                              nativeEvent.floatParam1,
                                                              nativeEvent.intParam7,
                                                              nativeEvent.intParam8,
                                                              nativeEvent.intParam9,
                                                              nativeEvent.intParam10,
                                                              nativeEvent.intParam11,
                                                              nativeEvent.intParam12,
                                                              nativeEvent.intParam13,
                                                              nativeEvent.intParam14,
                                                              nativeEvent.intParam15,
                                                              nativeEvent.intParam16);
                    dc.handleGestureEvent(gestureEvent);

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
