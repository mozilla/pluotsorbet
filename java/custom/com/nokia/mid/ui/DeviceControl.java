package com.nokia.mid.ui;

public class DeviceControl {
    public static final int KEYMAT_ALPHANUMERIC = 2;
    public static final int KEYMAT_DEFAULT = 0;
    public static final int KEYMAT_NUMERIC = 1;
    public static final int KEYMAT_OFF = 3;

    public static void setLights(int num, int level) {
        System.out.println("DeviceControl::setLights(int,int) not implemented");
    }

    public static void flashLights(long duration) {
        throw new RuntimeException("DeviceControl::flashLights(long) not implemented");
    }

    public static native void startVibra(int freq, long duration);

    public static native void stopVibra();

    public static int getUserInactivityTime() {
        throw new RuntimeException("DeviceControl::getUserInactivityTime() not implemented");
    }

    public static void resetUserInactivityTime() {
        throw new RuntimeException("DeviceControl::resetUserInactivityTime() not implemented");
    }
}
