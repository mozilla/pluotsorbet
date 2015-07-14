package com.nokia.mid.s40.bg;

import com.sun.cldc.isolate.Isolate;
import com.sun.midp.main.AmsUtil;
import com.sun.midp.midletsuite.MIDletSuiteStorage;

import com.sun.midp.events.Event;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.EventListener;

public class BGUtils implements EventListener {
    private static native int getFGMIDletNumber();
    private static native String getFGMIDletClass();

    public static void setBGMIDletResident(boolean param) {
        AmsUtil.executeWithArgs(MIDletSuiteStorage.getMIDletSuiteStorage(), 0, BGUtils.getFGMIDletNumber(),
                                BGUtils.getFGMIDletClass(), null, null, null, null, -1, -1, Isolate.MAX_PRIORITY,
                                null, false);
    }

    public static native boolean launchIEMIDlet(String midletSuiteVendor, String midletName, int midletNumber, String startupNoteText, String args);

    static {
        EventQueue.getEventQueue()
                  .registerEventListener(EventTypes.NATIVE_MIDLET_EXECUTE_REQUEST,
                                         BGUtils.getBGUtilsInstance());
    }
    private static BGUtils bgUtils = null;
    public static BGUtils getBGUtilsInstance() {
        if (bgUtils == null) {
            bgUtils = new BGUtils();
        }

        return bgUtils;
    }

    public boolean preprocess(Event event, Event waitingEvent) {
        return true;
    }

    public void process(Event event) {
        NativeEvent nativeEvent = (NativeEvent)event;

        switch (nativeEvent.getType()) {
            case EventTypes.NATIVE_MIDLET_EXECUTE_REQUEST:
              AmsUtil.executeWithArgs(MIDletSuiteStorage.getMIDletSuiteStorage(), 0, nativeEvent.intParam1,
                                      nativeEvent.stringParam1, null, null, null, null, -1, -1, Isolate.MAX_PRIORITY,
                                      null, false);
            break;
        }
    }
}
