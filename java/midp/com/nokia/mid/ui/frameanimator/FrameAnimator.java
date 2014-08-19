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

    public FrameAnimator() {
        System.out.println("warning: FrameAnimator() not implemented");
    }

    public boolean register(int x, int y, short maxFps, short maxPps, FrameAnimatorListener listener) {
        System.out.println("warning: FrameAnimator.register(IISSL...FrameAnimatorListener;)Z not implemented (" +
                           x + ", " + y + ", " + maxFps + ", " + maxPps + ", " + listener + ")");
        return true;
    }

    public void unregister() {
        throw new RuntimeException("FrameAnimator.unregister()V not implemented");
    }

    public void drag(int x, int y) {
        throw new RuntimeException("FrameAnimator.drag(II)V not implemented (" + x + ", " + y + ")");
    }

    public void kineticScroll(int speed, int direction, int friction, float angle) {
        throw new RuntimeException("FrameAnimator.kineticScroll(IIIF)V not implemented (" +
                                   speed + ", " + direction + ", " + friction + ", " + angle + ")");
    }

    public void limitedKineticScroll(int speed, int direction, int friction, float angle, int limitUp, int limitDown) {
        throw new RuntimeException("FrameAnimator.limitedKineticScroll(IIIFII)V not implemented (" +
                                   speed + ", " + direction + ", " + friction + ", " + angle + ", " + limitUp + ", " +
                                   limitDown + ")");
    }

    public void stop() {
        throw new RuntimeException("FrameAnimator.stop()V not implemented");
    }

    public boolean isRegistered() {
        System.out.println("warning: FrameAnimator.isRegistered()Z not implemented");
        return false;
    }

    public static int getNumRegisteredFrameAnimators() {
        throw new RuntimeException("FrameAnimator.getNumRegisteredFrameAnimators()I not implemented");
    }

}
