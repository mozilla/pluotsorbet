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

    public FrameAnimator() {
        init();
    }

    private native void init();

    public native boolean register(int x, int y, short maxFps, short maxPps, FrameAnimatorListener listener)
        throws IllegalStateException, NullPointerException, IllegalArgumentException;
    public native void unregister() throws IllegalStateException;
    public native void drag(int x, int y);
    public native void kineticScroll(int speed, int direction, int friction, float angle);
    public native void limitedKineticScroll(int speed, int direction, int friction, float angle, int limitUp, int limitDown);
    public native void stop();
    public native boolean isRegistered();
    public native static int getNumRegisteredFrameAnimators();
}
