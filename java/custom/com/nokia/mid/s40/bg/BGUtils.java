package com.nokia.mid.s40.bg;

import com.sun.cldc.isolate.Isolate;
import com.sun.midp.main.AmsUtil;
import com.sun.midp.midletsuite.MIDletSuiteStorage;

class WaitUserInteractionThread extends Thread {
    public WaitUserInteractionThread() {
        setPriority(Thread.MAX_PRIORITY);
    }
    public void run() {
        BGUtils.waitUserInteraction();
        BGUtils.startMIDlet();
    }
}

public class BGUtils {
    private static boolean launchMIDletCalled = false;

    private static native int getFGMIDletNumber();
    private static native String getFGMIDletClass();
    static native void waitUserInteraction();

    /* Start the FG MIDlet when the page becomes visible and if
       launchIEMIDlet hasn't been called (we want launchIEMIDlet
       to launch the MIDlet if possible) */
    public static void setBGMIDletResident(boolean param) {
      new WaitUserInteractionThread().start();
    }

    static void startMIDlet() {
      if (BGUtils.launchMIDletCalled) {
        return;
      }

      AmsUtil.executeWithArgs(MIDletSuiteStorage.getMIDletSuiteStorage(), 0, BGUtils.getFGMIDletNumber(),
                              BGUtils.getFGMIDletClass(), null, null, null, null, -1, -1, Isolate.MAX_PRIORITY,
                              null, false);
    }

    private static native void addSystemProperties(String args);

    public static boolean launchIEMIDlet(String midletSuiteVendor, String midletName, int midletNumber, String startupNoteText, String args) {
        BGUtils.launchMIDletCalled = true;

        BGUtils.waitUserInteraction();

        try {
            BGUtils.addSystemProperties(args);

            AmsUtil.executeWithArgs(MIDletSuiteStorage.getMIDletSuiteStorage(), 0, midletNumber,
                                    BGUtils.getFGMIDletClass(), null, null, null, null, -1, -1, Isolate.MAX_PRIORITY,
                                    null, false);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }

        return true;
    }
}
