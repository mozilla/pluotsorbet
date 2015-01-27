package com.nokia.mid.s40.bg;

import com.sun.midp.main.MIDletSuiteUtils;

class WaitUserInteractionThread extends Thread {
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

    public static void startMIDlet() {
      if (BGUtils.launchMIDletCalled) {
        return;
      }

      int midletNumber = BGUtils.getFGMIDletNumber();
      String midletClass = BGUtils.getFGMIDletClass();

      MIDletSuiteUtils.execute(midletNumber, midletClass, null);
    }

    private static native void addSystemProperties(String args);

    public static boolean launchIEMIDlet(String midletSuiteVendor, String midletName, int midletNumber, String startupNoteText, String args) {
        BGUtils.launchMIDletCalled = true;

        BGUtils.waitUserInteraction();

        try {
            BGUtils.addSystemProperties(args);

            MIDletSuiteUtils.execute(midletNumber, BGUtils.getFGMIDletClass(), null);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }

        return true;
    }
}
