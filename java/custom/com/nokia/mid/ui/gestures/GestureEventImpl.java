package com.nokia.mid.ui.gestures;

public class GestureEventImpl implements GestureEvent
{
    private final int type;
    private final int dragDistanceX;
    private final int dragDistanceY;
    private final int startX;
    private final int startY;
    private final float flickDirection;
    private final int flickSpeed;
    private final int flickSpeedX;
    private final int flickSpeedY;
    private final int pinchDistanceStarting;
    private final int pinchDistanceCurrent;
    private final int pinchDistanceChange;
    private final int pinchCenterX;
    private final int pinchCenterY;
    private final int pinchCenterChangeX;
    private final int pinchCenterChangeY;

    public GestureEventImpl(int type, int dragDistanceX, int dragDistanceY, int startX, int startY,
                            float flickDirection, int flickSpeed, int flickSpeedX, int flickSpeedY,
                            int pinchDistanceStarting, int pinchDistanceCurrent, int pinchDistanceChange,
                            int pinchCenterX, int pinchCenterY, int pinchCenterChangeX, int pinchCenterChangeY) {
        this.type = type;
        this.dragDistanceX = dragDistanceX;
        this.dragDistanceY = dragDistanceY;
        this.startX = startX;
        this.startY = startY;
        this.flickDirection = flickDirection;
        this.flickSpeed = flickSpeed;
        this.flickSpeedX = flickSpeedX;
        this.flickSpeedY = flickSpeedY;
        this.pinchDistanceStarting = pinchDistanceStarting;
        this.pinchDistanceCurrent = pinchDistanceCurrent;
        this.pinchDistanceChange = pinchDistanceChange;
        this.pinchCenterX = pinchCenterX;
        this.pinchCenterY = pinchCenterY;
        this.pinchCenterChangeX = pinchCenterChangeX;
        this.pinchCenterChangeY = pinchCenterChangeY;
    }

    public int getType() {
        return type;
    }

    public int getDragDistanceX() {
        return dragDistanceX;
    }

    public int getDragDistanceY() {
        return dragDistanceY;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public float getFlickDirection() {
        return flickDirection;
    }

    public int getFlickSpeed() {
        return flickSpeed;
    }

    public int getFlickSpeedX() {
        return flickSpeedX;
    }

    public int getFlickSpeedY() {
        return flickSpeedY;
    }

    public int getPinchDistanceStarting() {
        return pinchDistanceStarting;
    }

    public int getPinchDistanceCurrent() {
        return pinchDistanceCurrent;
    }

    public int getPinchDistanceChange() {
        return pinchDistanceChange;
    }

    public int getPinchCenterX() {
        return pinchCenterX;
    }

    public int getPinchCenterY() {
        return pinchCenterY;
    }

    public int getPinchCenterChangeX() {
        return pinchCenterChangeX;
    }

    public int getPinchCenterChangeY() {
        return pinchCenterChangeY;
    }
}
