package com.nokia.mid.ui.gestures;

public class GestureRegistrationManager
{
    public static final int MAX_SUPPORTED_GESTURE_INTERACTIVE_ZONES = 99;

    native public static void setListener(java.lang.Object container, GestureListener listener)
        throws java.lang.IllegalArgumentException;

    native public static boolean register(java.lang.Object container, GestureInteractiveZone gestureInteractiveZone)
        throws java.lang.IllegalArgumentException;

    native public static void unregister(java.lang.Object container, GestureInteractiveZone gestureInteractiveZone)
        throws java.lang.IllegalArgumentException;

    native public static void unregisterAll(java.lang.Object container)
        throws java.lang.IllegalArgumentException;

    native public static GestureInteractiveZone getInteractiveZone(java.lang.Object container);
}
