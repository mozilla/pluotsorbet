package com.nokia.mid.ui.gestures;

public interface GestureListener {
    void gestureAction(Object container,
                       GestureInteractiveZone gestureInteractiveZone,
                       GestureEvent gestureEvent);
}
