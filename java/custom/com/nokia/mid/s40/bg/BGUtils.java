package com.nokia.mid.s40.bg;

import com.sun.midp.main.MIDletSuiteUtils;
import com.sun.midp.midletsuite.MIDletSuiteStorage;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midletsuite.MIDletSuiteLockedException;
import com.sun.midp.security.SecurityToken;

public class BGUtils {
    private static boolean launchMIDletCalled = false;

    private static SecurityToken classSecurityToken;

    public static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken == null) {
            classSecurityToken = token;
        }
    }

    public static native int getFGMIDletNumber();
    public static native String getFGMIDletClass();
    public static native void waitUserInteraction();

    /* Start the FG MIDlet when the page becomes visible and if
       launchIEMIDlet hasn't been called (we want launchIEMIDlet
       to launch the MIDlet if possible) */
    public static void setBGMIDletResident(boolean param) {
      BGUtils.waitUserInteraction();

      if (BGUtils.launchMIDletCalled) {
        return;
      }

      int midletNumber = BGUtils.getFGMIDletNumber();
      String midletClass = BGUtils.getFGMIDletClass();

      MIDletSuiteUtils.execute(midletNumber, midletClass, null);
    }

    public static native void addSystemProperties(String args);

    public static boolean launchIEMIDlet(String midletSuiteVendor, String midletName, int midletNumber, String startupNoteText, String args) {
        System.out.println("midletSuiteVendor: " + midletSuiteVendor);
        System.out.println("midletName: " + midletName);
        System.out.println("midletNumber: " + midletNumber);
        System.out.println("startupNoteText: " + startupNoteText);
        System.out.println("args: " + args);

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
