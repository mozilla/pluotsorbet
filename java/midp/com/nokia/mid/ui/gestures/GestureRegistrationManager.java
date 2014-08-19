package com.nokia.mid.ui.gestures;

class ListenerRegistration {
    private Object container;
    private GestureListener listener;

    public ListenerRegistration(Object container, GestureListener listener) {
        this.container = container;
        this.listener = listener;
    }
}

class ZoneRegistration {
  public Object container;
  public GestureInteractiveZone zone;
  public GestureListener listener;
}

public class GestureRegistrationManager {
    public static final int MAX_SUPPORTED_GESTURE_INTERACTIVE_ZONES = 99;

    //private static ArrayList zoneRegistrations = new ArrayList();
    //private static ArrayList listenerRegistrations = new ArrayList();

    private static GestureListener privListener = null;
    private static GestureInteractiveZone privGestureInteractiveZone = null;
    private static Object privContainer = null;

    public static void setListener(Object container, GestureListener listener) throws IllegalArgumentException {
        System.out.println("REGISTER LISTENER");
        //listenerRegistrations.add(new ListenerRegistration(container, listener));
        privListener = listener;
        privContainer = container;
    }

    public static void callListener(GestureEvent event) {
        privListener.gestureAction(privContainer, privGestureInteractiveZone, event);
    }

    public static boolean register(Object container, GestureInteractiveZone gestureInteractiveZone) throws IllegalArgumentException {
        System.out.println("REGISTER");
        privGestureInteractiveZone = gestureInteractiveZone;
        return true;
    }

    native public static void unregister(java.lang.Object container, GestureInteractiveZone gestureInteractiveZone)
        throws java.lang.IllegalArgumentException;

    native public static void unregisterAll(java.lang.Object container)
        throws java.lang.IllegalArgumentException;

    native public static GestureInteractiveZone getInteractiveZone(Object container);
}
