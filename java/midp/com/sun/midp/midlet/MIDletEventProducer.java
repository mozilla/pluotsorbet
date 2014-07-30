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

import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventQueue;


/**
 * This class provides methods to send events of types 
 * handled by MIDletEventConsumer I/F implementors. 
 * This class completely hide event construction & sending in its methods.
 *
 * The primary user of this class are MIDletProxyList & MIDletProxy 
 * in AMS isolate. Threrefore ve can use MIdletProxy object reference 
 * as sendXXXEvent() method parameter instead of currently used int IDs.
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
public class MIDletEventProducer {
    
    /** Cached reference to the MIDP event queue. */
    protected EventQueue eventQueue;
    
    /**
     * Construct a new MIDletEventProducer.
     *
     * @param  theEventQueue An event queue where new events will be posted.
     */
    public MIDletEventProducer(EventQueue theEventQueue) {
        eventQueue = theEventQueue;
    }

    /*
     * MIDlet State Management (Lifecycle) Events
     *
     * ACTIVATE_MIDLET_EVENT
     * PAUSE_MIDLET_EVENT
     * DESTROY_MIDLET_EVENT
     *
     */

    /**
     * Pause a MIDlet.
     * Probably: need some form of MIDlet ID instead of displayId.
     *           use MIDletProxy instead of 2 int ID parameters.
     *
     * @param isolateId ID of the target isolate (where to send event)
     * @param midletClassName class name of the MIDlet
     */
    public void sendMIDletActivateEvent(int isolateId,
                                        String midletClassName) {
        sendMIDletEvent(EventTypes.ACTIVATE_MIDLET_EVENT, isolateId,
                        midletClassName);
    }
    /**
     * Activate a MIDlet.
     * Former: void MIDletProxy.pauseMidlet();
     *
     * @param isolateId ID of the target isolate (where to send event)
     * @param midletClassName class name of the MIDlet
     */
    public void sendMIDletPauseEvent(int isolateId, String midletClassName) {
        sendMIDletEvent(EventTypes.PAUSE_MIDLET_EVENT, isolateId,
                        midletClassName);
    }

    /**
     * Destroy a MIDlet.
     * Former: void MIDletProxy.destroyMidlet();
     *
     * @param isolateId ID of the target isolate (where to send event)
     * @param midletClassName class name of the MIDlet
     */
    public void sendMIDletDestroyEvent(int isolateId, String midletClassName) {
        sendMIDletEvent(EventTypes.DESTROY_MIDLET_EVENT, isolateId,
                        midletClassName);
    }

    /**
     * Sends an event with the only integer parameter for display ID
     * to a MIDlet .
     *
     * @param eventType type of event to be sent
     * @param midletIsolateId ID of the target isolate
     * @param midletClassName class name of the MIDlet
     */
    private void sendMIDletEvent(int eventType, int midletIsolateId,
                                 String midletClassName) {
        NativeEvent event = new NativeEvent(eventType);

        event.stringParam1 = midletClassName;

        eventQueue.sendNativeEventToIsolate(event, midletIsolateId);
    }
}
