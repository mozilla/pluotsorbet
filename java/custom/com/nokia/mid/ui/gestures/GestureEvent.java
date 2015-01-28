package com.nokia.mid.ui.gestures;

public interface GestureEvent {
    int getType();
    int getDragDistanceX();
    int getDragDistanceY();
    int getStartX();
    int getStartY();
    float getFlickDirection();
    int getFlickSpeed();
    int getFlickSpeedX();
    int getFlickSpeedY();
    int getPinchDistanceStarting();
    int getPinchDistanceCurrent();
    int getPinchDistanceChange();
    int getPinchCenterX();
    int getPinchCenterY();
    int getPinchCenterChangeX();
    int getPinchCenterChangeY();
}
