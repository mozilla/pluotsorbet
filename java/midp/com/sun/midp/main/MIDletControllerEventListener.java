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

import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.Event;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventListener;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.configurator.Constants;

/**
 * Provides initial event processing for MIDletProxyList.
 * This class is a listener that receives events and redirects them to
 * an appropriate MIDletControllerEventConsumer I/F implementor
 * (typically it is MIDletProxyList).
 */
class MIDletControllerEventListener implements EventListener {

    /** Consumer that will process events */
    private MIDletControllerEventConsumer midletControllerEventConsumer;

    /**
     * Default package private constructor.
     * Shall be called by MIDletProxyList.
     * This object shall be instantiated in MIDletProxyList's constructor.
     *
     * @param  eventQueue reference to the event queue
     * @param  theMIDletControllerEventConsumer comsumer that will process
     *         events received by this listener
     */
    MIDletControllerEventListener(
        EventQueue eventQueue,
        MIDletControllerEventConsumer theMIDletControllerEventConsumer) {

        midletControllerEventConsumer = theMIDletControllerEventConsumer;

        eventQueue.registerEventListener(
            EventTypes.MIDLET_CREATED_NOTIFICATION, this);
        eventQueue.registerEventListener(
            EventTypes.MIDLET_ACTIVE_NOTIFICATION, this);
        eventQueue.registerEventListener(
            EventTypes.MIDLET_PAUSED_NOTIFICATION, this);
        eventQueue.registerEventListener(
            EventTypes.MIDLET_DESTROYED_NOTIFICATION, this);
        eventQueue.registerEventListener(
            EventTypes.MIDLET_RS_PAUSED_NOTIFICATION, this);
        eventQueue.registerEventListener(
            EventTypes.MIDLET_RESUME_REQUEST, this);
        eventQueue.registerEventListener(
            EventTypes.DISPLAY_CREATED_NOTIFICATION, this);
        eventQueue.registerEventListener(
            EventTypes.FOREGROUND_REQUEST_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.BACKGROUND_REQUEST_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.SHUTDOWN_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.SELECT_FOREGROUND_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.FOREGROUND_TRANSFER_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.SET_FOREGROUND_BY_NAME_REQUEST, this);
        eventQueue.registerEventListener(
            EventTypes.PREEMPT_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.MIDLET_START_ERROR_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.MIDLET_DESTROY_REQUEST_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.ACTIVATE_ALL_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.PAUSE_ALL_EVENT, this);
        eventQueue.registerEventListener(
            EventTypes.FATAL_ERROR_NOTIFICATION, this);
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
     * Processes events.
     * <p>
     * Upon receiving a MIDlet created event a new MIDlet proxy will be
     * added to the list and the midletAdded method of all listeners will be
     * called with the new proxy.
     * <p>
     * Upon receiving a MIDlet destroyed event the MIDlet proxy corresponding
     * to the destroyed MIDlet will be removed from the list and the
     * midletRemoved method of all listeners
     * will be called with the removed proxy.
     * <p>
     * Upon receiving a MIDlet paused event the MIDlet proxy corresponding
     * to the paused MIDlet will have its midletState field set to
     * PAUSED and the midletUpdated method of all listeners
     * will be called with the updated proxy.
     * <p>
     * Upon receiving a foreground request event the MIDlet proxy corresponding
     * to the MIDlet requesting to be moved to the foreground will have its
     * wantsForeground field set to true and the midletUpdated method of all
     * listeners will be called with the updated proxy.
     * <p>
     * Upon receiving a background request event the MIDlet proxy corresponding
     * to the MIDlet requesting to be moved to the background will have its
     * wantsForeground field set to false and the midletUpdated method of all
     * listeners will be called with the updated proxy.
     *
     * @param event event to process
     */
    public void process(Event event) {
        try {
            NativeEvent nativeEvent = (NativeEvent)event;

            switch (nativeEvent.getType()) {

            case EventTypes.SHUTDOWN_EVENT:
                midletControllerEventConsumer.handleDestroyAllEvent();
                return;

            case EventTypes.SELECT_FOREGROUND_EVENT:

                if (Constants.MEASURE_STARTUP) {

                    // IMPL_NOTE: Usually MIDlet is explicitly switched to
                    // background on a native event, e.g. HOME key pressing.
                    // The native event handling is correct place to count
                    // the time of background switching from. However, native
                    // events handling is platform dependent, and in this way
                    // we have to instrument lot of platform code. That's why
                    // we selected this place as the the first shared place
                    // to start the measuring from. The measured time is
                    // less than the time experienced by user by the time to
                    // map native event to MIDP SELECT_FOREGROUND_EVENT

                    System.err.println("Switch To Background Time: Begin at " +
                        System.currentTimeMillis());
                }

                midletControllerEventConsumer.
                    handleMIDletForegroundSelectEvent(nativeEvent.intParam1);
                return;

            case EventTypes.FOREGROUND_TRANSFER_EVENT:
                midletControllerEventConsumer.
                    handleMIDletForegroundTransferEvent(
                        nativeEvent.intParam1,
                        nativeEvent.stringParam1,
                        nativeEvent.intParam2,
                        nativeEvent.stringParam2);
                return;

            case EventTypes.SET_FOREGROUND_BY_NAME_REQUEST:
                midletControllerEventConsumer.
                    handleSetForegroundByNameRequestEvent(
                        nativeEvent.intParam1,
                        nativeEvent.stringParam1);
                return;

            case EventTypes.PREEMPT_EVENT:
                if (nativeEvent.intParam2 != 0) {
                    midletControllerEventConsumer.
                        handleDisplayPreemptStartEvent(
                            nativeEvent.intParam1,
                            nativeEvent.intParam4);
                } else {
                    midletControllerEventConsumer.
                        handleDisplayPreemptStopEvent(
                            nativeEvent.intParam1,
                            nativeEvent.intParam4);
                }

                return;

            case EventTypes.MIDLET_CREATED_NOTIFICATION:
                midletControllerEventConsumer.handleMIDletCreateNotifyEvent(
                    nativeEvent.intParam1,
                    nativeEvent.stringParam1,
                    nativeEvent.intParam2,
                    nativeEvent.intParam3,
                    nativeEvent.stringParam2);
                return;

            case EventTypes.MIDLET_ACTIVE_NOTIFICATION:
                midletControllerEventConsumer.handleMIDletActiveNotifyEvent(
                    nativeEvent.intParam1,
                    nativeEvent.stringParam1);
                return;

            case EventTypes.MIDLET_PAUSED_NOTIFICATION:
                midletControllerEventConsumer.handleMIDletPauseNotifyEvent(
                    nativeEvent.intParam1,
                    nativeEvent.stringParam1);
                return;

            case EventTypes.MIDLET_DESTROYED_NOTIFICATION:
                midletControllerEventConsumer.handleMIDletDestroyNotifyEvent(
                    nativeEvent.intParam1,
                    nativeEvent.stringParam1);
                return;

            case EventTypes.MIDLET_RESUME_REQUEST:
                midletControllerEventConsumer.handleMIDletResumeRequestEvent(
                    nativeEvent.intParam1,
                    nativeEvent.stringParam1);
                return;

            case EventTypes.MIDLET_RS_PAUSED_NOTIFICATION:
                midletControllerEventConsumer.handleMIDletRsPauseNotifyEvent(
                    nativeEvent.intParam1,
                    nativeEvent.stringParam1);
                return;

            case EventTypes.DISPLAY_CREATED_NOTIFICATION:
                midletControllerEventConsumer.handleDisplayCreateNotifyEvent(
                    nativeEvent.intParam1,
                    nativeEvent.intParam2,
                    nativeEvent.stringParam1);
                return;

            case EventTypes.FOREGROUND_REQUEST_EVENT:
                midletControllerEventConsumer.
                    handleDisplayForegroundRequestEvent(
                        nativeEvent.intParam1,
                        nativeEvent.intParam4,
                        (nativeEvent.intParam2 != 0));
                return;

            case EventTypes.BACKGROUND_REQUEST_EVENT:
                midletControllerEventConsumer.
                    handleDisplayBackgroundRequestEvent(
                        nativeEvent.intParam1,
                        nativeEvent.intParam4);
                return;

            case EventTypes.MIDLET_START_ERROR_EVENT:
                midletControllerEventConsumer.handleMIDletStartErrorEvent(
                    nativeEvent.intParam1,
                    nativeEvent.stringParam1,
                    nativeEvent.intParam2,
                    nativeEvent.intParam3,
                    nativeEvent.stringParam2);
                return;

            case EventTypes.MIDLET_DESTROY_REQUEST_EVENT:
                midletControllerEventConsumer.handleMIDletDestroyRequestEvent(
                    nativeEvent.intParam1,
                    nativeEvent.intParam4);
                return;

            case EventTypes.ACTIVATE_ALL_EVENT:
                midletControllerEventConsumer.handleActivateAllEvent();
                return;

            case EventTypes.PAUSE_ALL_EVENT:
                midletControllerEventConsumer.handlePauseAllEvent();
                return;

            case EventTypes.FATAL_ERROR_NOTIFICATION:
                midletControllerEventConsumer.handleFatalErrorNotifyEvent(
                    nativeEvent.intParam1,
                    nativeEvent.intParam4);
                return;

            default:
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                       "unknown event (" +
                       event.getType() + ")");
                }
                return;
            }
        } catch (Throwable t) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(t, "Error occurred processing MIDlet event " +
                              event.getType());
            }
        }
    }
}
