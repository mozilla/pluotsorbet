package com.nokia.mid.s40.bg;

import com.sun.midp.main.MIDletSuiteUtils;
import com.sun.midp.midletsuite.MIDletSuiteStorage;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midletsuite.MIDletSuiteLockedException;
import com.sun.midp.security.SecurityToken;

public class BGUtils {
    private static SecurityToken classSecurityToken;

    public static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken == null) {
            classSecurityToken = token;
        }
    }

    public static void setBGMIDletResident(boolean param) {
        System.out.println("warning: BGUtils.setBGMIDletResident(Z)V not implemented (" + param + ")");
    }

    public static boolean launchIEMIDlet(String midletSuiteVendor, String midletName, int midletNumber, String startupNoteText, String args) {
        System.out.println("midletSuiteVendor: " + midletSuiteVendor);
        System.out.println("midletName: " + midletName);
        System.out.println("midletNumber: " + midletNumber);
        System.out.println("startupNoteText: " + startupNoteText);
        System.out.println("args: " + args);

        try {
            MIDletSuiteStorage storage = MIDletSuiteStorage.getMIDletSuiteStorage(classSecurityToken);
            MIDletSuite next = storage.getMIDletSuite(midletNumber, false);
            MIDletSuiteUtils.execute(midletNumber, MIDLET, null);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }

        return true;
    }
}
