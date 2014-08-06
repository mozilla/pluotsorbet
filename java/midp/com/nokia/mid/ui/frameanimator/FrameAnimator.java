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

    public FrameAnimator() {}

    native public boolean register(int x, int y, short maxFps, short maxPps, FrameAnimatorListener listener);
    native public void unregister();
    native public void drag(int x, int y);
    native public void kineticScroll(int speed, int direction, int friction, float angle);
    native public void limitedKineticScroll(int speed, int direction, int friction, float angle, int limitUp, int limitDown);
    native public void stop();
    native public boolean isRegistered();

    native public static int getNumRegisteredFrameAnimators();

}
