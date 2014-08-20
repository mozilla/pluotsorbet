/*
 * Copyright © 2013 Nokia Corporation. All rights reserved. Nokia and Nokia
 * Connecting People are registered trademarks of Nokia Corporation. Oracle and
 * Java are trademarks or registered trademarks of Oracle and/or its affiliates.
 * Other product and company names mentioned herein may be trademarks or trade
 * names of their respective owners. See LICENSE.TXT for license information.
 */

package com.nokia.example;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.TextField;
import com.nokia.mid.ui.TextEditorListener;

/**
 * EditScreen of the application.
 *
 * EditScreen contains a single line editor for the blog post title and multi
 * line editor for blog post contents. There are buttons Publish and Exit. The
 * Publish button is disabled by default, it is enabled when some content is
 * entered into the post content TextEditor.
 *
 * This class implements view logic of the post editing screen (it creates and
 * lays out controls, handles their enabling and traversal).
 *
 * Support for TextEditors is implemented in base class.
 */
public class EditScreen extends Screen implements TextEditorListener {

    private CanvasTextBox titleTextBox;
    private CanvasTextBox contentTextBox;
    private Button exitButton;
    private Button publishButton;
    private final int maximumTextLength = 32768;

    EditScreen(Display display) {
        super(display);

        this.titleTextBox = new CanvasTextBox(this, "Title", TextField.ANY,
                this.maximumTextLength);
        this.titleTextBox.setTextEditorListener(this);
        if (!BlogWriter.isFullTouch() && !BlogWriter.isAshaPlatform()) {
            this.titleTextBox.setFocused(true);
        }
        this.contentTextBox = new CanvasTextBox(this, "Post", TextField.ANY,
                this.maximumTextLength, true);
        this.contentTextBox.setTextEditorListener(this);

        try {
            this.exitButton = new Button(this, "Exit", new Runnable() {

                public void run() {
                    // When Exit is tapped, close the application
                    removeItems();
                    Display.getDisplay(parent).setCurrent(null);
                    parent.notifyDestroyed();
                }
            });

            this.publishButton = new Button(this, "Publish", new Runnable() {

                public void run() {
                    // Show alert when the Publish button is tapped
                    parentDisplay.setCurrent(new Alert("Publish",
                            "Post published successfully.", null,
                            AlertType.INFO));
                }
            });
            this.publishButton.setEnabled(false);
        } catch (Exception ex) {
            this.parentDisplay.setCurrent(new Alert("Cannot create controls."),
                    this);
        }

        // Set default position of items and buttons
        this.layoutControls();

        this.titleTextBox.setVisible(true);
        this.contentTextBox.setVisible(true);
    }

    public void pointerPressed(int x, int y) {
        // Only very basic pointer handling here

        boolean wasContentTextBoxFocused = this.contentTextBox.isFocused();

        // TextBoxes set/reset their focus based on pointer event
        // The change in event delivery order is needed to keep the virtual keyboard
        // open on focus change (first new focus must be given before previous
        // focus is lost).
        if (this.titleTextBox.isFocused()) {
            this.contentTextBox.handlePointerPressed(x, y);
            this.titleTextBox.handlePointerPressed(x, y);

        } else {
            this.contentTextBox.handlePointerPressed(x, y);
            this.titleTextBox.handlePointerPressed(x, y);
        }

        if (this.keyboardOpen) {
            // When the keyboard is opened, and content Textbox gains focus,
            // we want to change layout
            if (!wasContentTextBoxFocused && this.contentTextBox.isFocused()) {
                this.layoutControls();
            }
        }

        // Let buttons handle pointerPressed events
        this.publishButton.handlePointerEvent(x, y);
        this.exitButton.handlePointerEvent(x, y);
    }

    public void pointerReleased(int x, int y) {
        this.contentTextBox.handlePointerReleased(x, y);
        this.titleTextBox.handlePointerReleased(x, y);
        // Buttons need also pointerReleased events
        this.publishButton.handlePointerEvent(x, y);
        this.exitButton.handlePointerEvent(x, y);
    }

    protected void handleEditorTraversal() {
        if (this.titleTextBox.isFocused() && this.contentTextBox.isEnabled()) {
            this.titleTextBox.setFocused(false);
            this.contentTextBox.setFocused(true);
        } else if (contentTextBox.isFocused()) {
            this.contentTextBox.setFocused(false);
            this.titleTextBox.setFocused(true);
        }
    }
    
    /**
     * 
     */
    
    private void handleKeyboardState() {
        int margin = 5;
        int controlPadding = 10;
        int controlY = margin;
        int textBoxX = margin;
        if (this.keyboardOpen && contentTextBox.isFocused()) {
            this.titleTextBox.setVisible(false);
            this.contentTextBox.setPosition(textBoxX, controlY);
        } else {
            this.titleTextBox.setVisible(true);
            this.titleTextBox.setPosition(textBoxX, controlY);
            controlY += this.titleTextBox.getHeight() + controlPadding;
            this.contentTextBox.setPosition(textBoxX, controlY);
        }

    }

    /**
     * This method lays out controls on the screen.
     *
     * Layout for portrait and landscape is basically the same (TextEditors use
     * as much of the screen as possible), but there are two layouts for the
     * situation when virtual keyboard is opened. When the title TextEditor is
     * focused, both title and post content editors are visible. When the user
     * selects content editor, it uses all the available screen space.
     */
    protected void layoutControls() {
        if (this.publishButton == null || this.exitButton == null) {
            // SizeChanged of the Canvas can be called before constructor
            // finishes. It this is that case, we are not ready for layout yet.
            return;
        }

        int margin = 5;
        int controlPadding = 10;
        int controlY = margin;

        // Move controls out of the screen to improve appearance during
        // repainting
        this.publishButton.setPosition(0, this.getHeight());
        this.exitButton.setPosition(0, this.getHeight());
        this.titleTextBox.setPosition(0, this.getHeight());
        this.contentTextBox.setPosition(0, this.getHeight());

        int textBoxX = margin;
        // Let text boxes to use full width of the screen
        int textEditorWidth = this.getWidth() - 2 * margin;

        // Different layouts is used when keyboard is opened
        if (!this.keyboardOpen) {
            // No keyboard layout
            this.titleTextBox.setPosition(textBoxX, controlY);
            this.titleTextBox.setSize(textEditorWidth, this.titleTextBox.getHeight() + ((BlogWriter.isAshaPlatform()) ? (4 * controlPadding) : 0) );
            controlY += this.titleTextBox.getHeight() + controlPadding;
            this.contentTextBox.setPosition(textBoxX, controlY);
            this.contentTextBox.setSize(textEditorWidth, this.getHeight()
                    - controlY - this.publishButton.getHeight() - 2
                    * controlPadding);
        } else {
            if (this.titleTextBox.isFocused()) {
                // When title TextBox is focused, show both title and post text
                // boxes
                this.titleTextBox.setSize(textEditorWidth, this.titleTextBox.getHeight());
                this.titleTextBox.setPosition(textBoxX, controlY);
                controlY += this.titleTextBox.getHeight() + controlPadding;
                this.contentTextBox.setSize(textEditorWidth, this.getHeight()
                        - controlY - controlPadding);
                this.contentTextBox.setPosition(textBoxX, controlY);
            } else if (this.contentTextBox.isFocused()) {
                // Move the  title TextEditor out of screen to make more space for
                // post content when it is focused
                this.titleTextBox.setPosition(textBoxX, -this.titleTextBox.getHeight());

                this.contentTextBox.setSize(textEditorWidth, this.getHeight()
                        - controlY - controlPadding);
                this.contentTextBox.setPosition(textBoxX, controlY);
            }
        }

        // Calculate width of the Button based on available area
        int buttonWidth = (2 * this.getWidth()) / 5;
        publishButton.setSize(buttonWidth, publishButton.getHeight());
        exitButton.setSize(buttonWidth, exitButton.getHeight());
        // Buttons have maximum and minimum width. Get it to compute position
        // properly.
        buttonWidth = publishButton.getWidth();

        // Place Buttons in one row under text boxes
        int buttonX = (this.getWidth() / 2 - buttonWidth) / 2;
        controlY += this.contentTextBox.getHeight() + controlPadding;
        this.publishButton.setPosition(buttonX, controlY);
        this.exitButton.setPosition(buttonX + this.getWidth() / 2, controlY);

        // Update last width used for layout. Used for orientation change
        // detection.
        this.lastWidth = this.getWidth();
    }

    public void removeItems() {
        this.titleTextBox.dispose();
        this.contentTextBox.dispose();
        this.publishButton.dispose();
        this.exitButton.dispose();
    }

    protected void enableControls() {
        boolean enabled = !this.keyboardOpen;
        boolean publishEnabled = enabled;
        if (enabled) {
            if (contentTextBox.isEmpty()) {
                // No content - disable Publish button
                publishEnabled = false;
            }
        }
                
        this.publishButton.setEnabled(publishEnabled);
        this.exitButton.setEnabled(true);
    }

    public void displayOrientationChanged(int arg0) {
        // TODO Auto-generated method stub
    }
    
    public void showNotify(int keyboardCategory) {
        if (BlogWriter.isFullTouch() || BlogWriter.isAshaPlatform()) {
                this.keyboardOpen = true;
                this.handleKeyboardState();
		        this.enableControls();
        }
    }

    public void hideNotify(int keyboardCategory) {
          if (BlogWriter.isFullTouch() || BlogWriter.isAshaPlatform()) {
                this.keyboardOpen = false;
                this.handleKeyboardState();
                this.enableControls();
        }
    }
}
