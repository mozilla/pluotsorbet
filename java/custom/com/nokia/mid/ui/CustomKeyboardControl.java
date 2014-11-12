package com.nokia.mid.ui;

public interface CustomKeyboardControl {
    public static final int KEYBOARD_LANDSCAPE = 1;
    public static final int KEYBOARD_LANDSCAPE_180 = 4;
    public static final int KEYBOARD_PORTRAIT = 2;
    public static final int KEYBOARD_PORTRAIT_180 = 8;

    // `mode` can be one of the following:
    // `VirtualKeyboard.VKB_MODE_DEFAULT`,
    // `VirtualKeyboard.VKB_MODE_NUMERIC`,
    // `VirtualKeyboard.VKB_MODE_ALPHA_LOWER_CASE`,
    // `VirtualKeyboard.VKB_MODE_ALPHA_UPPER_CASE`,
    // or `VirtualKeyboard.VKB_MODE_ALPHA_UPPER_CASE_LOCKED`
    //
    // `type` can be one of the following:
    // `VirtualKeyboard.VKB_TYPE_ITUT`,
    // or `VirtualKeyboard.VKB_TYPE_GAME`
    public void launch(int type, int mode);
    public void launch(int type);
    public void launch();

    public void dismiss();

    public void setKeyboardType(int type);

    public void setKeyboardMode(int mode);

    public int getKeyboardMode();

    public int getKeyboardType();

    /**
     * getSupportedOrientations
     *
     * @param vkbType keyboard type for which supported orientation is queried.
     *                must be `VirtualKeyboard.VKB_TYPE_ITUT` or `VirtualKeyboard.VKB_TYPE_GAME`
     * @return supported orientations
     */
    public int getSupportedOrientations(int vkbType);
}

