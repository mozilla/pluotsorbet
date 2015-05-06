package com.nokia.mid.s40.bg;

import com.sun.cldc.isolate.Isolate;
import com.sun.midp.main.AmsUtil;
import com.sun.midp.midletsuite.MIDletSuiteStorage;

public class BGUtils {
    private static native int getFGMIDletNumber();
    private static native String getFGMIDletClass();
    public static native void maybeWaitUserInteraction(String midletClassName);

    public static void setBGMIDletResident(boolean param) {
        AmsUtil.executeWithArgs(MIDletSuiteStorage.getMIDletSuiteStorage(), 0, BGUtils.getFGMIDletNumber(),
                                BGUtils.getFGMIDletClass(), null, null, null, null, -1, -1, Isolate.MAX_PRIORITY,
                                null, false);
    }

    private static native void addSystemProperties(String args);

    public static boolean launchIEMIDlet(String midletSuiteVendor, String midletName, int midletNumber, String startupNoteText, String args) {
        BGUtils.addSystemProperties(args);
        return true;
    }
}
