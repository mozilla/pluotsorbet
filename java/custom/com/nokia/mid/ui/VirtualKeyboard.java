package com.nokia.mid.ui;

import com.nokia.mid.ui.CustomKeyboardControl;
import com.nokia.mid.ui.KeyboardVisibilityListener;

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

    public native static CustomKeyboardControl getCustomKeyboardControl();

    public native static void hideOpenKeypadCommand(boolean bl);

    public native static boolean isVisible();

    public native static int getXPosition();

    public native static int getYPosition();

    public native static int getWidth();

    public native static int getHeight();

    public native static void suppressSizeChanged(boolean bl);

    public native static void setVisibilityListener(KeyboardVisibilityListener listener);
}
