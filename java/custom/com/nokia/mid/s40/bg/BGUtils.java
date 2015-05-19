package com.nokia.mid.s40.bg;

import com.sun.cldc.isolate.Isolate;
import com.sun.midp.main.AmsUtil;
import com.sun.midp.midletsuite.MIDletSuiteStorage;

public class BGUtils {
    private static native void addSystemProperties(String args);

    public static native void setBGMIDletResident(boolean param);

    public static boolean launchIEMIDlet(String midletSuiteVendor, String midletName, int midletNumber, String startupNoteText, String args) {
      System.out.println("launchIEMIDlet(" + midletNumber + ", " + midletName + ", " + args + ")");
      try {
          BGUtils.addSystemProperties(args);
          AmsUtil.executeWithArgs(MIDletSuiteStorage.getMIDletSuiteStorage(),
                                  0, // external app id
                                  midletNumber, // suite id
                                  midletName, // class name
                                  null, // display name
                                  null, // arg0
                                  null, // arg1
                                  null, // arg2
                                  -1, // memoryReserved
                                  -1, // memoryTotal
                                  Isolate.MAX_PRIORITY,
                                  null, // profile name
                                  false); // `true` for debug mode
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }

        return true;
    }
}
