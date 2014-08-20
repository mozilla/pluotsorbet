package com.nokia.mid.ui;

import com.nokia.mid.ui.TextEditor;

public interface TextEditorListener {
    public static final int ACTION_CONTENT_CHANGE = 1;
    public static final int ACTION_OPTIONS_CHANGE = 2;
    public static final int ACTION_CARET_MOVE = 4;
    public static final int ACTION_TRAVERSE_PREVIOUS = 8;
    public static final int ACTION_TRAVERSE_NEXT = 16;
    public static final int ACTION_PAINT_REQUEST = 32;
    public static final int ACTION_DIRECTION_CHANGE = 64;
    public static final int ACTION_INPUT_MODE_CHANGE = 128;
    public static final int ACTION_LANGUAGE_CHANGE = 256;
    public static final int ACTION_TRAVERSE_OUT_SCROLL_UP = 512;
    public static final int ACTION_TRAVERSE_OUT_SCROLL_DOWN = 1024;
    public static final int ACTION_SCROLLBAR_CHANGED = 2048;

    // This method is called by the platform to notify the client about events in a TextEditor.
    public void inputAction(TextEditor textEditor, int actions);
}

