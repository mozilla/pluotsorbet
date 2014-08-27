/*
 * Copyright © 2013 Nokia Corporation. All rights reserved. Nokia and Nokia
 * Connecting People are registered trademarks of Nokia Corporation. Oracle and
 * Java are trademarks or registered trademarks of Oracle and/or its affiliates.
 * Other product and company names mentioned herein may be trademarks or trade
 * names of their respective owners. See LICENSE.TXT for license information.
 */

package com.nokia.example;

import com.nokia.mid.ui.TextEditorListener;
import java.io.IOException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;


/**
 * LoginScreen of the application.
 *
 * Login screen contains two single line text editors (user name and password)
 * and two buttons (login and exit). Password text editor is disabled by
 * default, it is enabled when a username is entered. Login button is disabled
 * by default, it is enabled when both username and password are entered.
 *
 * This class implements view logic of the login screen (it creates and lays out
 * controls, handles their enabling and traversal).
 *
 * Support for TextEditors is implemented in the base class.
 */
public class LoginScreen extends Screen implements TextEditorListener {

    public CanvasTextBox userNameTextBox;
    CanvasTextBox passwordTextBox;
    private Button exitButton;
    private Button loginButton;
    private Image logoImage;
    private final int maxLoginLength = 64;

    /*
     * Constructor
     */
    public LoginScreen(Display display) {
        super(display);

        try {
            this.userNameTextBox = new CanvasTextBox(
                    this, "Login", TextField.ANY, this.maxLoginLength);
            this.userNameTextBox.setTextEditorListener(this);
            this.passwordTextBox = new CanvasTextBox(
                    this, "Password", TextField.PASSWORD, this.maxLoginLength);
            this.passwordTextBox.setTextEditorListener(this);
        } catch (IllegalStateException e1) {
            // TODO Auto-generated catch block
            this.parentDisplay.setCurrent(
                    new Alert("e1 " + e1),
                    this);
            //e1.printStackTrace();
        }

        //Disable Password box. It will be enabled once username is not empty.
        this.passwordTextBox.setEnabled(false);

        try {
            this.exitButton = new Button(
                    this, "Exit",
                    new Runnable() {
                        public void run() {
                            // When Exit is tapped, close the application
                            removeItems();
                            Display.getDisplay(parent).setCurrent(null);
                            parent.notifyDestroyed();
                        }
                    });
            this.loginButton = new Button(
                    this, "Log in",
                    new Runnable() {
                        public void run() {
                            // When Login is tapped, create a new screen and set it current
                            if(BlogWriter.isAshaPlatform())
                                removeItems();
                            EditScreen editScreen = new EditScreen(parentDisplay);
                            editScreen.setParent(parent);
                            parentDisplay.setCurrent(editScreen);
                        }
                    });
            // Disable Login button - it is enabled once username and password are entered
            this.loginButton.setEnabled(false);
        } catch (Exception ex) {
            this.parentDisplay.setCurrent(
                    new Alert("Cannot create controls."),
                    this);
        }

        // Create the logo image
        try {
            this.logoImage = Image.createImage("midlets/blogwriter/images/LogoImage.png");
        } catch (IOException e) {
            this.parentDisplay.setCurrent(
                    new Alert("Cannot create graphics."), this);
        }

        // Set the default position of items and buttons
        this.layoutControls();

        this.userNameTextBox.setVisible(true);
        if (!BlogWriter.isFullTouch() && !BlogWriter.isAshaPlatform()) {
            this.userNameTextBox.setFocused(true);
        }
        this.passwordTextBox.setVisible(true);
    }

    public void create() {
    }

    protected void paint(Graphics graphics) {
        super.paint(graphics);
        // Draw logo
        if (this.logoImage != null) {
            graphics.drawImage(this.logoImage, getWidth() / 2, 0, Graphics.TOP | Graphics.HCENTER);
        }
    }

    public void pointerPressed(int x, int y) {
        // Only very basic pointer handling here
        // TextBoxes set/reset their focus based on pointer event
        // The change in event delivery order is needed to keep virtual keyboard
        // open on focus change (first new focus must be given before previous
        // focus is lost).
        
        if (this.userNameTextBox.isFocused()) {
            this.passwordTextBox.handlePointerPressed(x, y);
            this.userNameTextBox.handlePointerPressed(x, y);
        } else {
            this.userNameTextBox.handlePointerPressed(x, y);
            this.passwordTextBox.handlePointerPressed(x, y);
        }

        // Let Buttons handle pointerPressed events
        this.loginButton.handlePointerEvent(x, y);
        this.exitButton.handlePointerEvent(x, y);
    }

    public void pointerReleased(int x, int y) {
        this.userNameTextBox.handlePointerReleased(x, y);
        this.passwordTextBox.handlePointerReleased(x, y);
        // Buttons need also pointerReleased events
        this.loginButton.handlePointerEvent(x, y);
        this.exitButton.handlePointerEvent(x, y);
    }

    public void removeItems() {
        this.userNameTextBox.dispose();
        this.passwordTextBox.dispose();
        this.loginButton.dispose();
        this.exitButton.dispose();
    }

    /**
     * 
     */
    private void handleKeyboardState() {
        int margin = 10;
        // Different layout for Full Touch devices
        int controlPadding = 5;
        int textEditorWidth = this.getWidth() - 2 * margin;
        int controlY = this.logoImage.getHeight() + controlPadding;
        int textBoxX = (this.getWidth() - textEditorWidth) / 2;
        if (this.keyboardOpen && passwordTextBox.isFocused()) {
            this.userNameTextBox.setVisible(false);
            this.passwordTextBox.setPosition(textBoxX, controlY);
        } else {
            this.userNameTextBox.setVisible(true);
            this.userNameTextBox.setPosition(textBoxX, controlY);
            controlY += userNameTextBox.getHeight() + controlPadding;
            this.passwordTextBox.setPosition(textBoxX, controlY);
        }

    }
    /**
     * This method lays out controls on the screen.
     *
     * There are different layouts for portrait and landscape. In portrait
     * TextEditors are in one column, in landscape they are in one row.
     */
    protected void layoutControls() {
        if (this.loginButton == null || this.exitButton == null) {
            // SizeChanged of the Canvas can be called before constructor
            // finishes. It this is that case, we are not ready for layout yet.
            return;
        }

        int controlY = 0;
        int margin = 10;
        int controlPadding = 10;
        int textEditorWidth = 0;
        // Move controls out of the screen to improve appearance during layout
        // this.loginButton.setPosition(0, this.getHeight());
        //this.exitButton.setPosition(0, this.getHeight());
        //this.userNameTextBox.setPosition(0, this.getHeight());
        //this.passwordTextBox.setPosition(0, this.getHeight());

        // Different layouts for portrait and landscape are used
        if (BlogWriter.isFullTouch() || BlogWriter.isAshaPlatform()) {
            // Different layout for Full Touch devices
            controlPadding = 5;
            // In portrait place text boxes in one column
            textEditorWidth = this.getWidth() - 2 * margin;
            // Set text boxes width
            this.userNameTextBox.setSize(
                    textEditorWidth, this.userNameTextBox.getHeight() + ((BlogWriter.isAshaPlatform()) ? (8 * controlPadding): 0 ));
            this.passwordTextBox.setSize(
                    textEditorWidth, this.passwordTextBox.getHeight() + ((BlogWriter.isAshaPlatform()) ? (8 * controlPadding): 0 ));

            controlY = this.logoImage.getHeight() + controlPadding;
            int textBoxX = (this.getWidth() - textEditorWidth) / 2;
            this.userNameTextBox.setPosition(textBoxX, controlY);
            controlY += userNameTextBox.getHeight() + controlPadding;
            this.passwordTextBox.setPosition(textBoxX, controlY);
            controlPadding = 5;
        } else if (this.isPortraitOrientation) {
            // In portrait place text boxes in one column
            textEditorWidth = this.getWidth() - 2 * margin;
            // Set text boxes width
            this.userNameTextBox.setSize(
                    textEditorWidth, this.userNameTextBox.getHeight());
            this.passwordTextBox.setSize(
                    textEditorWidth, this.passwordTextBox.getHeight());

            int controlsHeight = userNameTextBox.getHeight()
                    + passwordTextBox.getHeight() + loginButton.getHeight()
                    + 3 * controlPadding;

            // Different layout is used when keyboard is opened
            if (!this.keyboardOpen || this.lastWidth != this.getWidth()) {
                controlY = this.getHeight() - controlsHeight;
            } else {
                controlY = this.getHeight() - userNameTextBox.getHeight()
                        - passwordTextBox.getHeight() - 2 * controlPadding;
            }

            int textBoxX = (this.getWidth() - textEditorWidth) / 2;
            this.userNameTextBox.setPosition(textBoxX, controlY);
            controlY += userNameTextBox.getHeight() + controlPadding;
            this.passwordTextBox.setPosition(textBoxX, controlY);
        } else {
            // In landscape place text boxes in one row
            textEditorWidth = (2 * this.getWidth()) / 5 - 2 * margin;
            // Set text boxes width
            this.userNameTextBox.setSize(
                    textEditorWidth, this.userNameTextBox.getHeight());
            this.passwordTextBox.setSize(
                    textEditorWidth, this.passwordTextBox.getHeight());

            int controlsHeight = userNameTextBox.getHeight()
                    + loginButton.getHeight() + 3 * controlPadding;

            // Different layout is used when keyboard is opened
            if (!this.keyboardOpen || this.lastWidth != this.getWidth()) {
                controlY = this.getHeight() - controlsHeight;
            } else {
                controlY = this.getHeight() - userNameTextBox.getHeight()
                        - controlPadding;
            }

            int textBoxX = (this.getWidth() / 2 - textEditorWidth) / 2;
            this.userNameTextBox.setPosition(textBoxX, controlY);
            this.passwordTextBox.setPosition(textBoxX + this.getWidth() / 2, controlY);
        }

        // Calculate width of the Button based on available area
        int buttonWidth = (2 * this.getWidth()) / 5;
        loginButton.setSize(buttonWidth, loginButton.getHeight());
        exitButton.setSize(buttonWidth, exitButton.getHeight());
        // Buttons have maximum and minimum width. Get it to compute the position properly.
        buttonWidth = loginButton.getWidth();
        // Place Buttons in one row under text boxes
        int buttonX = (this.getWidth() / 2 - buttonWidth) / 2;
        controlY += passwordTextBox.getHeight() + controlPadding;
                
        loginButton.setPosition(buttonX, controlY);
        exitButton.setPosition(buttonX + this.getWidth() / 2, controlY);

        // Update last width used for layout. Used for orientation change detection.
        this.lastWidth = this.getWidth();
    }

    protected void handleEditorTraversal() {
        if (this.userNameTextBox.isFocused()
                && this.passwordTextBox.isEnabled()) {
            this.userNameTextBox.setFocused(false);
            this.passwordTextBox.setFocused(true);
        } else if (passwordTextBox.isFocused()) {
            this.passwordTextBox.setFocused(false);
            this.userNameTextBox.setFocused(true);
        }
    }

    protected void enableControls() {
        boolean usernameIsEmpty = userNameTextBox.isEmpty();
        boolean passwordIsEmpty = passwordTextBox.isEmpty();

        boolean enabled = !keyboardOpen;
        boolean loginEnabled = enabled;
        if (enabled) {
            if (!usernameIsEmpty) {
                if (passwordIsEmpty) {
                    // No password - disable login button
                    loginEnabled = false;
                }
            } else {
                // No user name - disable login button
                loginEnabled = false;
            }
        }
        this.passwordTextBox.setEnabled(!usernameIsEmpty);
        this.loginButton.setEnabled(loginEnabled);
        //Keep exit button always enabled
        this.exitButton.setEnabled(true);
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