package com.nokia.mid.ui.frameanimator;

public interface FrameAnimatorListener {

    void animate(FrameAnimator animator, int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame);

}
