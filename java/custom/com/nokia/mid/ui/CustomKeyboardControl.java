package com.nokia.mid.ui;

public interface CustomKeyboardControl {
    public static final int KEYBOARD_LANDSCAPE = 1;
    public static final int KEYBOARD_LANDSCAPE_180 = 4;
    public static final int KEYBOARD_PORTRAIT = 2;
    public static final int KEYBOARD_PORTRAIT_180 = 8;

    public void launch(int var1, int var2);

    public void launch(int var1);

    public void launch();

    public void dismiss();

    public void setKeyboardType(int var1);

    public void setKeyboardMode(int var1);

    public int getKeyboardMode();

    public int getKeyboardType();

    public int getSupportedOrientations(int var1);
}

