/*
 * Copyright © 2013 Nokia Corporation. All rights reserved. Nokia and Nokia
 * Connecting People are registered trademarks of Nokia Corporation. Oracle and
 * Java are trademarks or registered trademarks of Oracle and/or its affiliates.
 * Other product and company names mentioned herein may be trademarks or trade
 * names of their respective owners. See LICENSE.TXT for license information.
 */

package com.nokia.example;

import com.nokia.mid.ui.KeyboardVisibilityListener;
import java.io.IOException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.nokia.mid.ui.TextEditor;
import com.nokia.mid.ui.TextEditorListener;
import com.nokia.mid.ui.VirtualKeyboard;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Abstract base class for MIDlet screens.
 *
 * Screen class handles painting of background and processing of TextEditor
 * events.
 */
public abstract class Screen extends Canvas implements KeyboardVisibilityListener {

    protected BlogWriter parent = null;
    protected Display parentDisplay;
    protected Image background = null;
    protected boolean keyboardOpen = false;
    protected boolean isPortraitOrientation = true;
    protected int lastWidth = -1;
    private Timer timer;

    public Screen(Display display) {
        super();
        this.setFullScreenMode(true);
        this.parentDisplay = display;

        updateOrientation();

        try {
            // Create background image
            this.background = Image.createImage("midlets/blogwriter/images/Background.png");
        } catch (IOException e) {
            this.parentDisplay.setCurrent(
                    new Alert("Cannot create graphics."), this);
        }
        VirtualKeyboard.setVisibilityListener(this);
    }

    protected void showNotify() {
        super.showNotify();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                // Update TextEditor state
                repaint();
            }
        }, 0, 50);
    }

    protected void hideNotify() {
        super.hideNotify();
        timer.cancel();
    }

    protected abstract void layoutControls();

    protected abstract void enableControls();

    protected abstract void handleEditorTraversal();

    public abstract void removeItems();

    protected void paint(Graphics graphics) {
        int imagePosition = 0;
        // Draw background image
        if (this.background != null) {
            graphics.drawImage(
                    this.background,
                    0, imagePosition,
                    Graphics.TOP | Graphics.LEFT);
        }
    }

    public void sizeChanged(int w, int h) {
        // If width of the screen didn't change, do nothing.
        // This occurs when virtual keyboard is opened - available height is
        // reduced, but width stays the same.
        if (this.lastWidth == getWidth()) {
            return;
        }

        this.updateOrientation();

        // Initialize last screen width if sizeChanged was called
        // before the value is set in constructor.
        if (this.lastWidth == -1) {
            this.lastWidth = this.getWidth();
        }

        if (!this.keyboardOpen) {
            // Do layout only when keyboard is not opened or opening.
            // Keyboard change layout is handled in inputAction().
            this.layoutControls();
        }
    }

    public void inputAction(TextEditor textEditor, int action) {
        if ((action & (TextEditorListener.ACTION_TRAVERSE_NEXT | TextEditorListener.ACTION_TRAVERSE_PREVIOUS)) != 0) {
            this.handleEditorTraversal();
        }

        if ((action & (TextEditorListener.ACTION_CONTENT_CHANGE | TextEditorListener.ACTION_CARET_MOVE)) != 0) {
            // When content changes, some controls might get enabled.
            this.enableControls();
        }

        final int keyboardOpenCode = 4096; //com.nokia.mid.ui.S60TextEditor.ACTION_VIRTUAL_KEYBOARD_OPEN
        if (((action & keyboardOpenCode) != 0)) {
            
            this.keyboardOpen = true;
            // Re-layout only when it is really needed.
            this.enableControls();
            this.updateOrientation();
            this.layoutControls();
        }
        final int keyboardCloseCode = 8192; //com.nokia.mid.ui.S60TextEditor.ACTION_VIRTUAL_KEYBOARD_CLOSE
        if (((action & keyboardCloseCode) != 0)) {
            // Re-layout only when it is needed.
            if (this.lastWidth == this.getWidth()) {
                // When the keyboard is closed and width of the Canvas didn't change,
                // we need to re-layout since the keyboard was actually closed.
                this.keyboardOpen = false;
                this.enableControls();
                this.updateOrientation();
                this.layoutControls();
            } else {
                // Ignore re-layout in this case, since it is orientation change, and
                // keyboard is re-opened and laid out
                this.lastWidth = this.getWidth();
            }
        }
    }

    public void setParent(BlogWriter parent) {
        this.parent = parent;
    }

    private void updateOrientation() {
        if (!this.keyboardOpen) {
            this.isPortraitOrientation = this.getHeight() >= this.getWidth();
        } else {
            // When the keyboard is opened, it is better to change portrait detection.
            // This way we use screen space better for single line text editors.
            this.isPortraitOrientation = ((float) this.getWidth() / (float) this.getHeight()) < 1.4;
        }
    }
}