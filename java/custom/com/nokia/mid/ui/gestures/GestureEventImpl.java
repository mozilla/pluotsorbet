package com.nokia.mid.ui.gestures;

public class GestureEventImpl implements GestureEvent {
    public GestureEventImpl(int type, int dragDistanceX, int dragDistanceY, int startX, int startY,
                            float flickDirection, int flickSpeed, int flickSpeedX, int flickSpeedY,
                            int pinchDistanceStarting, int pinchDistanceCurrent, int pinchDistanceChange,
                            int pinchCenterX, int pinchCenterY, int pinchCenterChangeX, int pinchCenterChangeY) {
        // Overridden in midp/gestures.js
    }

    native public int getType();
    native public int getDragDistanceX();
    native public int getDragDistanceY();
    native public int getStartX();
    native public int getStartY();
    native public float getFlickDirection();
    native public int getFlickSpeed();
    native public int getFlickSpeedX();
    native public int getFlickSpeedY();
    native public int getPinchDistanceStarting();
    native public int getPinchDistanceCurrent();
    native public int getPinchDistanceChange();
    native public int getPinchCenterX();
    native public int getPinchCenterY();
    native public int getPinchCenterChangeX();
    native public int getPinchCenterChangeY();
}
