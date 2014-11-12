package com.nokia.mid.ui;

public interface KeyboardVisibilityListener {
    // `keyboardCategory` will always be either
    // `VirtualKeyboard.CUSTOM_KEYBOARD` or `VirtualKeyboard.SYSTEM_KEYBOARD`
    public void showNotify(int keyboardCategory);
    public void hideNotify(int keyboardCategory);
}

