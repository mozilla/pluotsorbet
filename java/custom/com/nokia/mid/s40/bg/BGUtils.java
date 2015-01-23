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

    public static native String getFGMIDlet(int midletNumber);
    public static native void waitUserInteraction();
    public static native void addSystemProperties(String args);
    public static native void startMIDlet(int midletNumber, String midletClass, String thirdArg);

    public static boolean launchIEMIDlet(String midletSuiteVendor, String midletName, int midletNumber, String startupNoteText, String args) {
        System.out.println("midletSuiteVendor: " + midletSuiteVendor);
        System.out.println("midletName: " + midletName);
        System.out.println("midletNumber: " + midletNumber);
        System.out.println("startupNoteText: " + startupNoteText);
        System.out.println("args: " + args);

        BGUtils.waitUserInteraction();

        try {
            BGUtils.addSystemProperties(args);

            MIDletSuiteUtils.execute(midletNumber, BGUtils.getFGMIDlet(midletNumber), null);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }

        return true;
    }
}
