package com.nokia.mid.ui;

import com.nokia.mid.ui.CanvasItem;
import com.nokia.mid.ui.TextEditorListener;
import javax.microedition.lcdui.Font;

public class TextEditor extends CanvasItem {
    // This flag is a hint to the implementation that during text editing, the smiley key should be disabled on the virtual keyboard.
    public static final int DISABLE_SMILEY_MODE = 4194304;

    // Creates a new TextEditor object with the given initial contents, maximum size in characters, constraints and editor size in pixels.
    public static TextEditor createTextEditor(String text, int maxSize, int constraints, int width, int height) {
        throw new RuntimeException("TextEditor::createTextEditor(int,int,int,int) not implemented");
    }

    // Creates a new empty TextEditor with the given maximum size in characters, constraints and editor size as number of visible rows.
    public static TextEditor createTextEditor(int maxSize, int constraints, int width, int rows) {
        throw new RuntimeException("TextEditor::createTextEditor(int,int,int,int) not implemented");
    }

    // Sets this TextEditor focused or removes keyboard focus.
    public void setFocus(boolean focused) {
        throw new RuntimeException("TextEditor::setFocus(boolean) not implemented");
    }

    // Returns the focus state of TextEditor.
    public boolean hasFocus() {
        throw new RuntimeException("TextEditor::hasFocus() not implemented");
    }

    // Set the parent object of this TextEditor.
    public void setParent(Object theParent) {
        throw new RuntimeException("TextEditor::setParent(Object) not implemented");
    }

    // Sets the size of this TextEditor in pixels.
    public void setSize(int width, int height) {
        throw new RuntimeException("TextEditor::setSize(int,int) not implemented");
    }

    // Sets the rendering position of this TextEditor.
    public void setPosition(int x, int y) {
        throw new RuntimeException("TextEditor::setPosition(int,int) not implemented");
    }

    // Sets the visibility value of TextEditor.
    public void setVisible(boolean visible) {
        throw new RuntimeException("TextEditor::setVisible(boolean) not implemented");
    }

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
    public int getLineMarginHeight() {
        throw new RuntimeException("TextEditor::getLineMarginHeight() not implemented");
    }

    // Gets the whole content height in this TextEditor in pixels.
    public int getContentHeight() {
        throw new RuntimeException("TextEditor::getContentHeight() not implemented");
    }

    // Sets the index of the caret.
    public void setCaret(int index) {
        throw new RuntimeException("TextEditor::setCaret(int) not implemented");
    }

    // Gets the current position of the caret in the editor.
    public int getCaretPosition() {
        throw new RuntimeException("TextEditor::getCaretPosition() not implemented");
    }

    // Gets the topmost pixel position of the topmost visible line in the editor.
    public int getVisibleContentPosition() {
        throw new RuntimeException("TextEditor::getVisibleContentPosition() not implemented");
    }

    // Gets the font being used in rendering the text content in this TextEditor.
    public Font getFont() {
        throw new RuntimeException("TextEditor::getFont() not implemented");
    }

    // Sets the application preferred font for rendering the text content in this TextEditor.
    public void setFont(Font font) {
        throw new RuntimeException("TextEditor::setFont(Font) not implemented");
    }

    // Gets the background color and alpha of this TextEditor.
    public int getBackgroundColor() {
        throw new RuntimeException("TextEditor::getBackgroundColor() not implemented");
    }

    // Gets the foreground color and alpha of this TextEditor.
    public int getForegroundColor() {
        throw new RuntimeException("TextEditor::getForegroundColor() not implemented");
    }

    // Sets the background color and alpha of this TextEditor to the specified values.
    public void setBackgroundColor(int color) {
        throw new RuntimeException("TextEditor::setBackgroundColor(int) not implemented");
    }

    // Sets the foreground color and alpha of this TextEditor to the specified values.
    public void setForegroundColor(int color) {
        throw new RuntimeException("TextEditor::setForegroundColor(int) not implemented");
    }

    // Sets the highlight background color.
    public void setHighlightBackgroundColor(int color) {
        throw new RuntimeException("TextEditor::setHighlightBackgroundColor(int) not implemented");
    }

    // Sets the highlight foreground color.
    public void setHighlightForegroundColor(int color) {
        throw new RuntimeException("TextEditor::setHighlightForegroundColor(int) not implemented");
    }

    // Sets the content of the TextEditor as a string.
    public void setContent(String content) {
        throw new RuntimeException("TextEditor::setContent(String) not implemented");
    }

    // Gets the string content in the TextEditor.
    public String getContent() {
        throw new RuntimeException("TextEditor::getContent() not implemented");
    }

    // Inserts a string into the content of the TextEditor.
    public void insert(String text, int position) {
        throw new RuntimeException("TextEditor::insert(String,int) not implemented");
    }

    // Deletes characters from the TextEditor.
    public void delete(int offset, int length) {
        throw new RuntimeException("TextEditor::delete(int,int) not implemented");
    }

    // Returns the maximum size (number of characters) that can be stored in this TextEditor.
    public int getMaxSize() {
        throw new RuntimeException("TextEditor::getMazSize() not implemented");
    }

    // Sets the maximum size (number of characters) that can be contained in this TextEditor.
    public int setMaxSize(int maxSize) {
        throw new RuntimeException("TextEditor::setMaxSize(int) not implemented");
    }

    // Gets the number of characters that are currently stored in this TextEditor.
    public int size() {
        throw new RuntimeException("TextEditor::size() not implemented");
    }

    // Sets the input constraints of this TextEditor.
    public void setConstraints(int constraints) {
        throw new RuntimeException("TextEditor::setConstraints(int) not implemented");
    }

    // Gets the current input constraints of this TextEditor.
    public int getConstraints() {
        throw new RuntimeException("TextEditor::getConstraints() not implemented");
    }

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
        throw new RuntimeException("TextEditor::setTextEditorListener(TextEditorListener) not implemented");
    }

    // Returns the multiline state of the TextEditor.
    public boolean isMultiline() {
        throw new RuntimeException("TextEditor::isMultiline() not implemented");
    }

    // Sets the editor to be either multi-line (true) or single-line (false).
    public void setMultiline(boolean aMultiline) {
        throw new RuntimeException("TextEditor::setMultiline(boolean) not implemented");
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

