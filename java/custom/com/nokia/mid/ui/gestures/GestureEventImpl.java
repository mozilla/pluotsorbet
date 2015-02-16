package com.nokia.mid.ui.gestures;

import com.sun.midp.events.NativeEvent;

public class GestureEventImpl implements GestureEvent {
    protected NativeEvent nativeEvent;

    public int getType() {
        return nativeEvent.intParam1;
    }

    public int getDragDistanceX() {
        return nativeEvent.intParam2;
    }

    public int getDragDistanceY() {
        return nativeEvent.intParam3;
    }

    public int getStartX() {
        return nativeEvent.intParam5;
    }

    public int getStartY() {
        return nativeEvent.intParam6;
    }

    public float getFlickDirection() {
        return nativeEvent.floatParam1;
    }

    public int getFlickSpeed() {
        return nativeEvent.intParam7;
    }

    public int getFlickSpeedX() {
        return nativeEvent.intParam8;
    }

    public int getFlickSpeedY() {
        return nativeEvent.intParam9;
    }

    public int getPinchDistanceStarting() {
        return nativeEvent.intParam10;
    }

    public int getPinchDistanceCurrent() {
        return nativeEvent.intParam11;
    }

    public int getPinchDistanceChange() {
        return nativeEvent.intParam12;
    }

    public int getPinchCenterX() {
        return nativeEvent.intParam13;
    }

    public int getPinchCenterY() {
        return nativeEvent.intParam14;
    }

    public int getPinchCenterChangeX() {
        return nativeEvent.intParam15;
    }

    public int getPinchCenterChangeY() {
        return nativeEvent.intParam16;
    }
}
