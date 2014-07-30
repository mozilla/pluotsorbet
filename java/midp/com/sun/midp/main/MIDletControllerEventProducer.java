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
import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventQueue;


/**
 * This class provides methods to send events of types
 * handled by MIDletControllerEventConsumer I/F implementors.
 * This class completely hide event construction & sending in its methods.
 *
 * This class is intended to be used by MIDletStateHandler & MIDletPeer
 * classes in Allication Isolate.
 * So in some of its sendXXXEvent()methods we can change int IDs to
 * MIDletPeer references.
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
public class MIDletControllerEventProducer {

    /** Cached reference to the MIDP event queue. */
    protected EventQueue eventQueue;
    /** Cached reference to AMS isolate ID. */
    protected int amsIsolateId;
    /** Cached reference to current isolate ID. */
    protected int currentIsolateId;

    /** Preallocate start error event to work in case of out of memory */
    final NativeEvent startErrorEvent;
    /** Preallocate MIDlet created event to work in case of out of memory */
    final NativeEvent midletCreatedEvent;
    /** Preallocate MIDlet active event to work in case of out of memory */
    final NativeEvent midletActiveEvent;
    /** Preallocate MIDlet paused event to work in case of out of memory */
    final NativeEvent midletPausedEvent;
    /** Preallocate MIDlet destroyed event to work in case of out of memory */
    final NativeEvent midletDestroyedEvent;
    /**
     * Preallocate MIDlet resources paused event to work in case of out
     * of memory
     */
    final NativeEvent midletRsPausedEvent;

    /**
     * Construct a new MIDletControllerEventProducer.
     *
     * @param  theEventQueue An event queue where new events will be posted.
     * @param  theAmsIsolateId AMS Isolate Id
     * @param  theCurrentIsolateId Current Isolate Id
     */
    public MIDletControllerEventProducer(
        EventQueue theEventQueue,
        int theAmsIsolateId,
        int theCurrentIsolateId) {

        eventQueue = theEventQueue;
        amsIsolateId = theAmsIsolateId;
        currentIsolateId = theCurrentIsolateId;

        /* Cache all of the notification events. */
        startErrorEvent =
            new NativeEvent(EventTypes.MIDLET_START_ERROR_EVENT);
        midletCreatedEvent =
            new NativeEvent(EventTypes.MIDLET_CREATED_NOTIFICATION);
        midletActiveEvent =
            new NativeEvent(EventTypes.MIDLET_ACTIVE_NOTIFICATION);
        midletPausedEvent =
            new NativeEvent(EventTypes.MIDLET_PAUSED_NOTIFICATION);
        midletDestroyedEvent =
            new NativeEvent(EventTypes.MIDLET_DESTROYED_NOTIFICATION);
        midletRsPausedEvent =
            new NativeEvent(EventTypes.MIDLET_RS_PAUSED_NOTIFICATION);
    }

    /*
     * MIDlet Startup Events:
     *
     * MIDLET_START_ERROR
     * MIDLET_CREATED_NOTIFICATION
     */
    /**
     * Notifies AMS that MIDlet creation failed
     * NEW: earlier it has been explicitely generated by
     * void static AppIsolateMIDletSuiteLoader.main(...)
     *
     * @param midletExternalAppId ID of given by an external application
     *                            manager
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     * @param errorCode start error code
     */
    public void sendMIDletStartErrorEvent(
        int midletSuiteId,
        String midletClassName,
        int midletExternalAppId,
        int errorCode,
        String errorDetails) {

        synchronized (startErrorEvent) {
            // use pre-created event to work in case of handling out of memory
            startErrorEvent.intParam1 = midletSuiteId;
            startErrorEvent.intParam2 = midletExternalAppId;
            startErrorEvent.intParam3 = errorCode;

            startErrorEvent.stringParam1 = midletClassName;
            startErrorEvent.stringParam2 = errorDetails; 

            eventQueue.sendNativeEventToIsolate(startErrorEvent, amsIsolateId);
        }
    }

    /**
     * Called to send a MIDlet created notification to the AMS isolate.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     * @param midletExternalAppId ID of given by an external application
     *                            manager
     * @param midletDisplayName name to show the user
     */
    public void sendMIDletCreateNotifyEvent(
        int midletSuiteId,
        String midletClassName,
        int midletExternalAppId,
        String midletDisplayName) {

        synchronized (midletCreatedEvent) {
            midletCreatedEvent.intParam1 = midletSuiteId;
            midletCreatedEvent.intParam2 = currentIsolateId;
            midletCreatedEvent.intParam3 = midletExternalAppId;

            midletCreatedEvent.stringParam1 = midletClassName;
            midletCreatedEvent.stringParam2 = midletDisplayName;

            eventQueue.sendNativeEventToIsolate(midletCreatedEvent,
                                                amsIsolateId);
        }
    }

    /*
     * MIDlet State Management (Lifecycle) Events:
     *
     * MIDLET_ACTIVE_NOTIFICATION
     * MIDLET_PAUSE_NOTIFICATION
     * MIDLET_DESTROY_NOTIFICATION
     *
     * MIDLET_DESTROY_REQUEST
     *
     * ACTIVATE_ALL - produced by native code
     * PAUSE_ALL -produced by native code
     * SHUTDOWN/DESTROY_ALL - produced by native code
     *
     * FATAL_ERROR_NOTIFICATION - produced by native code
     *
     */
    /**
     * Called to send a MIDlet active notification to the AMS isolate.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void sendMIDletActiveNotifyEvent(int midletSuiteId,
                                            String midletClassName) {
        sendEvent(midletActiveEvent, midletSuiteId, midletClassName);
    }
    /**
     * Called to send a MIDlet paused notification to the AMS isolate.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void sendMIDletPauseNotifyEvent(int midletSuiteId,
                                           String midletClassName) {
        sendEvent(midletPausedEvent, midletSuiteId, midletClassName);
    }
    /**
     * Called to send a MIDlet destroyed notification to the AMS isolate.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void sendMIDletDestroyNotifyEvent(int midletSuiteId,
                                             String midletClassName) {
        sendEvent(midletDestroyedEvent, midletSuiteId, midletClassName);
    }
    /**
     * Called to send a MIDlet resume request to the AMS isolate.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void sendMIDletResumeRequest(int midletSuiteId,
                                        String midletClassName) {
        sendEvent(new NativeEvent(EventTypes.MIDLET_RESUME_REQUEST),
                  midletSuiteId, midletClassName);
    }
    /**
     * Sends notification for MIDlet resources pause to the AMS isolate.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void sendMIDletRsPauseNotifyEvent(int midletSuiteId,
                                             String midletClassName) {
        sendEvent(midletRsPausedEvent, midletSuiteId, midletClassName);
    }

    /**
     * Called by the display to request the central AMS to destroy the owning
     * MIDlet.
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendMIDletDestroyRequestEvent(int midletDisplayId) {
        sendEvent(new NativeEvent(EventTypes.MIDLET_DESTROY_REQUEST_EVENT),
                midletDisplayId);
    }

    /*
     * Foreground MIDlet Management Events:
     *
     * SELECT_FOREGROUND - produced by native code
     * FOREGROUND_TRANSFER
     * SET_FOREGROUND_BY_NAME_REQUEST
     *
     */
    /**
     * Called to send a foreground MIDlet transfer event to the AMS isolate.
     * Former: NEW method, originally sent from CHAPI
     *
     * @param originMIDletSuiteId ID of MIDlet from which
     *        to take forefround ownership away,
     * @param originMIDletClassName Name of MIDlet from which
     *        to take forefround ownership away
     * @param targetMIDletSuiteId ID of MIDlet
     *        to give forefround ownership to,
     * @param targetMIDletClassName Name of MIDlet
     *        to give forefround ownership to
     */
    public void sendMIDletForegroundTransferEvent(
        int originMIDletSuiteId,
        String originMIDletClassName,
        int targetMIDletSuiteId,
        String targetMIDletClassName) {
        NativeEvent event =
            new NativeEvent(EventTypes.FOREGROUND_TRANSFER_EVENT);

        event.intParam1 = originMIDletSuiteId;
        event.intParam2 = targetMIDletSuiteId;

        event.stringParam1 = originMIDletClassName;
        event.stringParam2 = targetMIDletClassName;

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }
    /**
     * Called to send a request to AMS isolate for a MIDlet be in
     * the foreground.
     *
     * @param suiteId MIDlet's suite ID
     * @param className MIDlet's class name
     */
    public void sendSetForegroundByNameRequestEvent(int suiteId,
            String className) {

        NativeEvent event =
            new NativeEvent(EventTypes.SET_FOREGROUND_BY_NAME_REQUEST);

        event.intParam1 = suiteId;
        event.stringParam1 = className;

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }


    /**
     * Called to send a Display created notification to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     * @param midletClassName Class name of the MIDlet that owns the display
     */
    public void sendDisplayCreateNotifyEvent(int midletDisplayId,
                                             String midletClassName) {
        NativeEvent event =
            new NativeEvent(EventTypes.DISPLAY_CREATED_NOTIFICATION);

        event.intParam1 = currentIsolateId;
        event.intParam2 = midletDisplayId;

        event.stringParam1 = midletClassName;

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }

    /*
     * Foreground Display Management Events:
     *
     * FOREGROUND_REQUEST
     * BACKGROUND_REQUEST
     *
     */
    /**
     * Called to send a foreground request event to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     * @param isAlert true if the current displayable is an Alert
     */
    public void sendDisplayForegroundRequestEvent(int midletDisplayId,
            boolean isAlert) {
        NativeEvent event =
            new NativeEvent(EventTypes.FOREGROUND_REQUEST_EVENT);

        if (isAlert) {
            event.intParam2 = 1;
        } else {
            event.intParam2 = 0;
        }

        sendEvent(event, midletDisplayId);
    }
    /**
     * Called to send a background request event to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendDisplayBackgroundRequestEvent(int midletDisplayId) {
        sendEvent(new NativeEvent(EventTypes.BACKGROUND_REQUEST_EVENT),
                midletDisplayId);
    }

    /*
     * Display Preemption Management Events:
     *
     * PREEMPT
     *
     */
    /**
     * Called to start preempting and end preempting.
     * Probably: will need more parameters, ex. MIDlet ID
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendDisplayPreemptStartEvent(int midletDisplayId) {
        NativeEvent event =
            new NativeEvent(EventTypes.PREEMPT_EVENT);

        event.intParam2 = -1; /* start = true */

        sendEvent(event, midletDisplayId);
    }
    /**
     * Called to start preempting and end preempting.
     * Probably: will need more parameters, ex. MIDlet ID
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendDisplayPreemptStopEvent(int midletDisplayId) {
        NativeEvent event =
            new NativeEvent(EventTypes.PREEMPT_EVENT);

        event.intParam2 = 0; /* start = false */

        sendEvent(event, midletDisplayId);
    }

    /**
     * Sends standard MIDlet controller event setting two integer parameters
     * for display ID and isolate ID. It is synchronized by the event to be 
     * sent to avoid inconsistent parameters setting.
     *
     * @param event event to be sent
     * @param midletDisplayId ID of the sending Display
     */
    private void sendEvent(NativeEvent event, int midletDisplayId) {
        synchronized (event) {
            event.intParam1 = currentIsolateId;
            event.intParam4 = midletDisplayId;
            eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
        }
    }

    /**
     * Sends standard MIDlet controller event setting two parameters
     * for suite ID and class name. It is synchronized by the event to be 
     * sent to avoid inconsistent parameters setting.
     *
     * @param event event to be sent
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    private void sendEvent(NativeEvent event, int midletSuiteId,
                           String midletClassName) {
        synchronized (event) {
            event.intParam1 = midletSuiteId;
            event.stringParam1 = midletClassName;
            eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
        }
    }
}
