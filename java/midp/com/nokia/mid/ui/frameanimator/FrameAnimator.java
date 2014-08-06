package com.nokia.mid.ui.frameanimator;

public class FrameAnimator {

    public static final int FRAME_ANIMATOR_MAX_CONCURRENT = 5;
    public static final int FRAME_ANIMATOR_VERTICAL = 0;
    public static final int FRAME_ANIMATOR_HORIZONTAL = 1;
    public static final int FRAME_ANIMATOR_FREE_ANGLE = 2;
    public static final int FRAME_ANIMATOR_FRICTION_LOW = 0;
    public static final int FRAME_ANIMATOR_FRICTION_MEDIUM = 1;
    public static final int FRAME_ANIMATOR_FRICTION_HIGH = 2;

    protected int actionType;
    protected int actionID;

    protected static int _numRegistered;

    public FrameAnimator() {}

    public boolean register(int x, int y, short maxFps, short maxPps, FrameAnimatorListener listener) {
        _numRegistered++;
        return true;
    }

    public void unregister() {
        _numRegistered--;
    }

    public void drag(int x, int y) {
        //
    }

    public void kineticScroll(int speed, int direction, int friction, float angle) {
        //
    }

    public void limitedKineticScroll(int speed, int direction, int friction, float angle, int limitUp, int limitDown) {
        //
    }

    public void stop() {
        //
    }

    public boolean isRegistered() {
        return true;
    }

    public static int getNumRegisteredFrameAnimators() {
        return _numRegistered;
    }

}
