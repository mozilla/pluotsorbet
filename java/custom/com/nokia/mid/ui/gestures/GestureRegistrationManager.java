package com.nokia.mid.ui.gestures;

import java.util.Hashtable;
import java.util.Vector;

class ZoneRegistration {
    public Object container;
    public GestureInteractiveZone zone;

    public ZoneRegistration(Object container, GestureInteractiveZone zone) {
        this.container = container;
        this.zone = zone;
    }
}

public class GestureRegistrationManager {
    public static final int MAX_SUPPORTED_GESTURE_INTERACTIVE_ZONES = 99;

    private static Vector zoneRegistrations = new Vector();
    private static Hashtable listenerRegistrations = new Hashtable();

    public static void setListener(Object container, GestureListener listener) throws IllegalArgumentException {
        listenerRegistrations.put(container, listener);
    }

    public static void callListener(GestureEvent event) {
        for (int i = 0; i < zoneRegistrations.size(); i++) {
            ZoneRegistration zoneReg = (ZoneRegistration)zoneRegistrations.elementAt(i);

            GestureInteractiveZone zone = zoneReg.zone;

            if (!zone.contains(event.getStartX(), event.getStartY())) {
                continue;
            }

            if (!zone.supports(event.getType())) {
                continue;
            }

            GestureListener listener = (GestureListener)listenerRegistrations.get(zoneReg.container);
            listener.gestureAction(zoneReg.container, zone, event);
        }
    }

    public static boolean register(Object container, GestureInteractiveZone gestureInteractiveZone) throws IllegalArgumentException {
        zoneRegistrations.addElement(new ZoneRegistration(container, gestureInteractiveZone));
        return true;
    }

    native public static void unregister(Object container, GestureInteractiveZone gestureInteractiveZone)
        throws IllegalArgumentException;

    native public static void unregisterAll(Object container)
        throws IllegalArgumentException;

    native public static GestureInteractiveZone getInteractiveZone(Object container);
}
