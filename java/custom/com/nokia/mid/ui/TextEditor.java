package com.nokia.mid.ui;

import javax.microedition.lcdui.Font;
import java.util.Hashtable;

class TextEditorThread extends Thread {
    // We need a thread to be able to wake from js when there is an async js keyboard event.
    native TextEditor getNextDirtyEditor();

    TextEditorThread() {
        setPriority(Thread.MAX_PRIORITY);
    }

    public void run() {
        while (true) {
            TextEditor dirty = getNextDirtyEditor();
            dirty.myListener.inputAction(dirty, TextEditorListener.ACTION_CONTENT_CHANGE);
        }
    }
}

public class TextEditor extends CanvasItem {
    // This flag is a hint to the implementation that during text editing, the smiley key should be disabled on the virtual keyboard.
    public static final int DISABLE_SMILEY_MODE = 4194304; // 0x400000

    protected TextEditorListener myListener;

    private boolean multiline = false;
    private static TextEditorThread textEditorThread;
    private Font font = Font.getDefaultFont();

    protected TextEditor(String text, int maxSize, int constraints, int width, int height) {
        init(text, maxSize, constraints, width, height);

        if (textEditorThread == null) {
            textEditorThread = new TextEditorThread();
            textEditorThread.start();
        }
    }

    // Initialize the native representation.
    native private void init(String text, int maxSize, int constraints, int width, int height);

    // Creates a new TextEditor object with the given initial contents, maximum size in characters, constraints and editor size in pixels.
    public static TextEditor createTextEditor(String text, int maxSize, int constraints, int width, int height) {
        return new TextEditor(text, maxSize, constraints, width, height);
    }

    // Creates a new empty TextEditor with the given maximum size in characters, constraints and editor size as number of visible rows.
    public static TextEditor createTextEditor(int maxSize, int constraints, int width, int rows) {
        return createTextEditor("", maxSize, constraints, width, Font.getDefaultFont().getHeight() * rows);
    }

    // Sets this TextEditor focused or removes keyboard focus.
    native public void setFocus(boolean focused);

    // Returns the focus state of TextEditor.
    native public boolean hasFocus();

    // Sets the Z-position, or the elevation, of the item.
    public void setZPosition(int z) {
        throw new RuntimeException("TextEditor::setZPosition(int) not implemented");
    }

    // Specifies whether or not the editor will receive touch-events.
    public void setTouchEnabled(boolean enabled) {
        throw new RuntimeException("TextEditor::setTouchEnabled(boolean) not implemented");
    }

    // Gets the current touch-enabled state.
    public boolean isTouchEnabled() {
        throw new RuntimeException("TextEditor::isTouchEnabled() not implemented");
    }

    // Returns the Z-position, or the elevation, of the item. The Z-position decides the stacking order of neighboring items.
    public int getZPosition() {
        throw new RuntimeException("TextEditor::getZPosition() not implemented");
    }

    // Gets the line margin height in this TextEditor in pixels.
    // This is in addition to the normal font height (i.e. Font.getHeight()),
    // which already includes leading (margin below the text).  So we set this
    // to zero, although this will be inaccurate if the native implementation
    // adds a line margin.
    native public int getLineMarginHeight();

    native public int getContentHeight();

    // Sets the index of the caret.
    native public void setCaret(int index);

    // Gets the current position of the caret in the editor.
    native public int getCaretPosition();

    // Gets the topmost pixel position of the topmost visible line in the editor.
    native public int getVisibleContentPosition();

    // Gets the font being used in rendering the text content in this TextEditor.
    public Font getFont() {
        return this.font;
    }

    // Sets the application preferred font for rendering the text content in this TextEditor.
    native public void setFont(Font font);

    // Gets the background color and alpha of this TextEditor.
    native public int getBackgroundColor();

    // Gets the foreground color and alpha of this TextEditor.
    native public int getForegroundColor();

    // Sets the background color and alpha of this TextEditor to the specified values.
    native public void setBackgroundColor(int color);

    // Sets the foreground color and alpha of this TextEditor to the specified values.
    native public void setForegroundColor(int color);

    // Sets the highlight background color.
    public void setHighlightBackgroundColor(int color) {
        throw new RuntimeException("TextEditor::setHighlightBackgroundColor(int) not implemented");
    }

    // Sets the highlight foreground color.
    public void setHighlightForegroundColor(int color) {
        throw new RuntimeException("TextEditor::setHighlightForegroundColor(int) not implemented");
    }

    // Sets the content of the TextEditor as a string.
    native public void setContent(String content);

    // Gets the string content in the TextEditor.
    native public String getContent();

    // Inserts a string into the content of the TextEditor.
    native public void insert(String text, int position);

    // Deletes characters from the TextEditor.
    native public void delete(int offset, int length);

    // Returns the maximum size (number of characters) that can be stored in this TextEditor.
    native public int getMaxSize();

    // Sets the maximum size (number of characters) that can be contained in this TextEditor.
    native public int setMaxSize(int maxSize);

    // Gets the number of characters that are currently stored in this TextEditor.
    native public int size();

    // Sets the input constraints of this TextEditor.
    native public void setConstraints(int constraints);

    // Gets the current input constraints of this TextEditor.
    native public int getConstraints();

    // Sets a hint to the implementation as to the input mode that should be used when the user initiates editing of this TextEditor.
    public void setInitialInputMode(String characterSubset) {
        throw new RuntimeException("TextEditor::setInitialInputMode(String) not implemented");
    }

    // Returns the initial input mode set to the editor, or null if no initial input mode has been set.
    public String getInitialInputMode() {
        throw new RuntimeException("TextEditor::getInitialInputMode() not implemented");
    }

    // Sets a selection on a range of text in the TextEditor content.
    public void setSelection(int index, int length) {
        throw new RuntimeException("TextEditor::setSelection(int,int) not implemented");
    }

    // Gets the currently selected content in the TextEditor.
    public String getSelection() {
        throw new RuntimeException("TextEditor::getSelection() not implemented");
    }

    // Sets a listener for content changes in this TextEditor, replacing any previous TextEditorListener.
    public void setTextEditorListener(TextEditorListener listener) {
        myListener = listener;
    }

    // Returns the multiline state of the TextEditor.
    public boolean isMultiline() {
        return multiline;
    }

    // Sets the editor to be either multi-line (true) or single-line (false).
    public void setMultiline(boolean aMultiline) {
        // XXX If the caller is disabling multiline, then we may need to make
        // the native widget pass "enter" keystrokes to the parent object.
        multiline = aMultiline;
    }

    // If the default indicator location is not used then sets the drawing location for input indicators relative to the TextEditor's parent.
    public void setIndicatorLocation(int x, int y) {
        throw new RuntimeException("TextEditor::setIndicatorLocation(int,int) not implemented");
    }

    // Resets the implementation provided input indicators to their default position.
    public void setDefaultIndicators() {
        throw new RuntimeException("TextEditor::setDefaultIndicators() not implemented");
    }

    // By default indicators visibility is set to true and they are made visible when the associated TextEditor is focused.
    public void setIndicatorVisibility(boolean visible) {
        throw new RuntimeException("TextEditor::setIndicatorVisibility(boolean) not implemented");
    }

    // Gets the size of the area needed for drawing the input indicators.
    public int[] getIndicatorSize() {
        throw new RuntimeException("TextEditor::getIndicatorSize() not implemented");
    }

    // Disables one or multiple touch input modes from use.
    public void setDisabledTouchInputModes(int touchInputModes) {
        throw new RuntimeException("TextEditor::setDisabledTouchInputModes(int) not implemented");
    }

    // By default all supported touch input modes are available.
    public int getDisabledTouchInputModes() {
        throw new RuntimeException("TextEditor::getDisabledTouchInputModes() not implemented");
    }

    // Set the preferred touch input mode overriding the device default preferred mode.
    public void setPreferredTouchMode(int touchInputModes) {
        throw new RuntimeException("TextEditor::setPreferredTouchMode(int) not implemented");
    }

    // Gets the preferred touch input mode.
    public int getPreferredTouchMode() {
        throw new RuntimeException("TextEditor::getPreferredTouchMode() not implemented");
    }

    // Sets the caret in the Editor at x, y location.
    public void setCaretXY(int x, int y) {
        throw new RuntimeException("TextEditor::setCaretXY(int,int) not implemented");
    }
}
