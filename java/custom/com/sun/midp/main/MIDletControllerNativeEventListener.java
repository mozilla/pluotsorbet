// TODO: License info?

package com.sun.midp.main;

import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.Event;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventListener;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * TODO: Class description
 */
class MIDletControllerNativeEventListener implements EventListener {

    /** Consumer that will process events */
    private MIDletControllerNativeEventConsumer midletControllerNativeEventConsumer;

    /**
     * Default package private constructor.
     * Shall be called by MIDletProxyList.
     * This object shall be instantiated in MIDletProxyList's constructor.
     *
     * @param  eventQueue reference to the event queue
     * @param  theMIDletNativeControllerEventConsumer consumer that will process
     *         events received by this listener
     */
    MIDletControllerNativeEventListener(
        EventQueue eventQueue,
        MIDletControllerNativeEventConsumer theMIDletControllerNativeEventConsumer) {

        midletControllerNativeEventConsumer = theMIDletControllerNativeEventConsumer;

        eventQueue.registerEventListener(
            EventTypes.NATIVE_MIDLET_EXECUTE_REQUEST, this);
        eventQueue.registerEventListener(
            EventTypes.NATIVE_MIDLET_DESTROY_REQUEST, this);

        /*
         * TODO: We can add these as we want them
         *
        eventQueue.registerEventListener(
            EventTypes.NATIVE_MIDLET_RESUME_REQUEST, this);
        eventQueue.registerEventListener(
            EventTypes.NATIVE_MIDLET_PAUSE_REQUEST, this);
        eventQueue.registerEventListener(
            EventTypes.NATIVE_MIDLET_GETINFO_REQUEST, this);
        */
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
     *
     * TODO: Description
     *
     * @param event event to process
     */
    public void process(Event event) {
        try {
            NativeEvent nativeEvent = (NativeEvent)event;

            switch (nativeEvent.getType()) {

                case EventTypes.NATIVE_MIDLET_EXECUTE_REQUEST:
                    midletControllerNativeEventConsumer.handleNativeMidletExecuteEvent(
                        nativeEvent.intParam1, // externalAppId
                        nativeEvent.intParam2, // suiteId
                        nativeEvent.stringParam1, // className
                        nativeEvent.stringParam2, // displayName
                        nativeEvent.stringParam3, // arg0
                        nativeEvent.stringParam4, // arg1
                        nativeEvent.stringParam5, // arg2
                        nativeEvent.intParam3, // memoryReserved
                        nativeEvent.intParam4, // memoryTotal
                        nativeEvent.intParam5, // priority
                        nativeEvent.stringParam6 // profile
                    );
                    return;

                case EventTypes.NATIVE_MIDLET_DESTROY_REQUEST:
                    midletControllerNativeEventConsumer.handleNativeMidletDestroyEvent(
                        nativeEvent.intParam1, // suiteId
                        nativeEvent.stringParam1 // midlet class name
                    );
                    return;

                    /*
                     * TODO: We can add these as we need them
                     *
                case EventTypes.NATIVE_MIDLET_RESUME_REQUEST:
                    midletControllerNativeEventConsumer.handleNativeMidletResumeEvent();
                    return;

                case EventTypes.NATIVE_MIDLET_PAUSE_REQUEST:
                    midletControllerNativeEventConsumer.handleNativeMidletPauseEvent();
                    return;


                case EventTypes.NATIVE_MIDLET_GETINFO_REQUEST:
                    midletControllerNativeEventConsumer.handleNativeMidletGetInfoEvent();
                    return;
                    */

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
                Logging.trace(t, "Error occurred processing native MIDlet event " +
                              event.getType());
            }
        }
    }
}
