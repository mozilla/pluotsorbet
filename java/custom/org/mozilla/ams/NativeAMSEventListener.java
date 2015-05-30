package org.mozilla.ams;

import com.sun.midp.events.Event;
import com.sun.midp.events.EventListener;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;

/**
 * NativeAMSEventListener
 */
class NativeAMSEventListener implements EventListener {
    private NativeAMSEventConsumer consumer;

    NativeAMSEventListener(
        EventQueue eventQueue,
        NativeAMSEventConsumer aConsumer) {

        consumer = aConsumer;

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
     * Preprocess
     *
     * @param event event to preprocess
     *
     * @param waitingEvent previous event of same type if one is waiting
     *
     * @return false to block event from being posted
     */
    public boolean preprocess(Event event, Event waitingEvent) {
        return true;
    }

    /**
     * Process
     *
     * @param event event to process
     */
    public void process(Event event) {
        try {
            NativeEvent nativeEvent = (NativeEvent)event;

            switch (nativeEvent.getType()) {

                case EventTypes.NATIVE_MIDLET_EXECUTE_REQUEST:
                    consumer.handleNativeMIDletExecuteRequest(
                        nativeEvent.intParam1, // suiteId
                        nativeEvent.stringParam1, // className
                        nativeEvent.stringParam2, // displayName
                        nativeEvent.stringParam3, // arg0
                        nativeEvent.stringParam4, // arg1
                        nativeEvent.stringParam5); // arg2
                    return;

                case EventTypes.NATIVE_MIDLET_DESTROY_REQUEST:
                    consumer.handleNativeMIDletDestroyRequest(
                        nativeEvent.intParam1, // suiteId
                        nativeEvent.stringParam1); // midlet class name
                    return;

                    /*
                     * TODO: We can add these as we need them
                     *
                case EventTypes.NATIVE_MIDLET_RESUME_REQUEST:
                    consumer.handleNativeMIDletResumeRequest();
                    return;

                case EventTypes.NATIVE_MIDLET_PAUSE_REQUEST:
                    consumer.handleNativeMIDletPauseRequest();
                    return;


                case EventTypes.NATIVE_MIDLET_GETINFO_REQUEST:
                    consumer.handleNativeMIDletGetInfoRequest();
                    return;
                    */

            default:
                System.out.println("Unknown native AMS event: " + nativeEvent.getType());
                return;
            }
        } catch (Throwable t) {
            System.out.println("Exception while processing native AMS event");
        }
    }
}
