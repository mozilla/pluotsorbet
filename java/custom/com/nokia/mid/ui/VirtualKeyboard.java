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

    public static CustomKeyboardControl getCustomKeyboardControl() {
        throw new RuntimeException("CustomKeyboardControl::getCustomKeyboardControl() not implemented");
    }

    public static void hideOpenKeypadCommand(boolean bl) {
        System.out.println("CustomKeyboardControl::hideOpenKeypadCommand(boolean) not implemented");
    }

    public static boolean isVisible() {
        throw new RuntimeException("CustomKeyboardControl::isVisible() not implemented");
    }

    public static int getXPosition() {
        throw new RuntimeException("CustomKeyboardControl::getXPosition() not implemented");
    }

    public static int getYPosition() {
        throw new RuntimeException("CustomKeyboardControl::getYPosition() not implemented");
    }

    public static int getWidth() {
        throw new RuntimeException("CustomKeyboardControl::getWidth() not implemented");
    }

    public static int getHeight() {
        throw new RuntimeException("CustomKeyboardControl::getHeight() not implemented");
    }

    public static void setVisibilityListener(KeyboardVisibilityListener keyboardVisibilityListener) {
        System.out.println("CustomKeyboardControl::setVisibilityListener(KeyboardVisibilityListener) not implemented");
    }

    public static void suppressSizeChanged(boolean bl) {
        throw new RuntimeException("CustomKeyboardControl::suppressSizeChanged(boolean) not implemented");
    }
}

