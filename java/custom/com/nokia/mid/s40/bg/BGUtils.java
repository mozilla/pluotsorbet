package com.nokia.mid.s40.bg;

public class BGUtils {
    private static native int getFGMIDletNumber();
    private static native String getFGMIDletClass();

    public static native void setBGMIDletResident(boolean param);
    public static native boolean launchIEMIDlet(String midletSuiteVendor, String midletName, int midletNumber, String startupNoteText, String args);
}
