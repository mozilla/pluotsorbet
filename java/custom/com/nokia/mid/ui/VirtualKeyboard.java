package com.nokia.mid.ui;

import com.nokia.mid.ui.CustomKeyboardControl;
import com.nokia.mid.ui.KeyboardVisibilityListener;

class VKVisibilityNotificationRunnable implements Runnable {
    private Object listenerLock = new Object();

    public void run() {
        while(true) {
            boolean isShow = sleepUntilVKVisibilityChange();
            synchronized(listenerLock) {
                if (null == listener) {
                    continue;
                }
                // NB: A malicious or poorly-written listener can block
                //     in one of `showNotify` or `hideNotify`, causing
                //     us to stop sending visibility notifications and
                //     causing the next call to `setListener` to also
                //     block. We could alleviate this by creating a
                //     new Thread on which we will make the call to
                //     `showNotify` or `hideNotify`, then waiting for
                //     the thread to notify us but timing out after
                //     some reasonable amount of time. This is a lot
                //     of thread creation and will only help poorly-
                //     written programs so is probably overkill.
                if (isShow) {
                    listener.showNotify(VirtualKeyboard.SYSTEM_KEYBOARD);
                } else {
                    listener.hideNotify(VirtualKeyboard.SYSTEM_KEYBOARD);
                }
            }
        }
    }

    public void setListener(KeyboardVisibilityListener listener) {
        synchronized(listenerLock) {
            this.listener = listener;
        }
    }

    private KeyboardVisibilityListener listener;
    private native boolean sleepUntilVKVisibilityChange();
}

public class VirtualKeyboard {
    public static final int CUSTOM_KEYBOARD = 1;
    public static final int SYSTEM_KEYBOARD = 2;
    public static final int VKB_TYPE_ITUT = 1;
    public static final int VKB_TYPE_GAME = 3;
    public static final int VKB_MODE_DEFAULT = 0;
    public static final int VKB_MODE_NUMERIC = 1;
    public static final int VKB_MODE_ALPHA_LOWER_CASE = 2;
    public static final int VKB_MODE_ALPHA_UPPER_CASE = 3;
    public static final int VKB_MODE_ALPHA_UPPER_CASE_LOCKED = 4;

    public static CustomKeyboardControl getCustomKeyboardControl() {
        throw new RuntimeException("VirtualKeyboard::getCustomKeyboardControl() not implemented");
    }

    public static void hideOpenKeypadCommand(boolean bl) {
        System.out.println("VirtualKeyboard::hideOpenKeypadCommand(boolean) not implemented");
    }

    public native static boolean isVisible();

    public native static int getXPosition();

    public native static int getYPosition();

    public native static int getWidth();

    public native static int getHeight();

    public static void setVisibilityListener(KeyboardVisibilityListener listener) {
        if (null == visibilityNotifier) {
            visibilityNotifier = new VKVisibilityNotificationRunnable();
            listenerThread = new Thread(visibilityNotifier);
            listenerThread.start();
        }
        visibilityNotifier.setListener(listener);
    }

    public static void suppressSizeChanged(boolean bl) {
        throw new RuntimeException("VirtualKeyboard::suppressSizeChanged(boolean) not implemented");
    }

    private static VKVisibilityNotificationRunnable visibilityNotifier = null;
    private static Thread listenerThread = null;
}

