/*
 * Copyright © 2013 Nokia Corporation. All rights reserved. Nokia and Nokia
 * Connecting People are registered trademarks of Nokia Corporation. Oracle and
 * Java are trademarks or registered trademarks of Oracle and/or its affiliates.
 * Other product and company names mentioned herein may be trademarks or trade
 * names of their respective owners. See LICENSE.TXT for license information.
 */

package com.nokia.example;

import com.nokia.mid.ui.CanvasGraphicsItem;
import com.nokia.mid.ui.CanvasItem;
import java.io.IOException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/*
 * Button control is based on CanvasGraphicsItem. This class show the basic
 * usage of Canvas graphics control to encapsulate simple control.
 *
 * Button control handles pointer events to change between normal and pressed
 * state, it executes the "click" event handler given through the constructor
 * when Button is pressed and released. Button can be disabled.
 *
 * Button appearance is defined in class ButtonState (class definition at the
 * end of this this file) objects which allow usage of different background
 * bitmaps and font colors.
 */
public class Button extends CanvasGraphicsItem {
    // Text to be shown on Button

    private String text = null;
    // Button states
    private ButtonState normalState;
    private ButtonState pressedState;
    private ButtonState dimmedState;
    private ButtonState currentState;
    private boolean enabled = true;
    private boolean pressed = false;
    Runnable clickHandler;

    Button(Canvas parent, String text, Runnable clickHandler) throws Exception {
        // CanvasGraphicsItem needs a non-zero size for its construction.
        super(1, 1);
        this.setParent(parent);
        this.text = text;
        this.clickHandler = clickHandler;
        try {
            // Create different button states with distinct look and colors
            this.normalState = new ButtonState(
                    this, "midlets/blogwriter/images/NormalButton.png", 0xFFFFFF);
            this.pressedState = new ButtonState(
                    this, "midlets/blogwriter/images/PressedButton.png", 0xFFB600);
            this.dimmedState = new ButtonState(
                    this, "midlets/blogwriter/images/DimmedButton.png", 0xa0a0a0);
        } catch (Exception ex) {
            throw ex;
        }
        // Set size based on normal state size
        this.setSize(this.normalState.getWidth(), this.normalState.getHeight());
        this.updateState();
        this.setVisible(true);
        this.repaint();
    }

    /**
     * Enables or disables Button. Disabled Button doesn't process pointer
     * events.
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        this.updateState();
        this.repaint();
    }

    /**
     * Sets whether Button is pressed.
     */
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
        this.updateState();
        this.repaint();
    }

    /**
     * This method sets size of the Button by setting size of the
     * CanvasGraphicsItem. Width is checked for minimum and maximum value.
     */
    public void setSize(int w, int h) {
        int buttonWidth = w;
        if (buttonWidth < 70) {
            buttonWidth = 70;
        } else if (buttonWidth > this.normalState.getWidth()) {
            buttonWidth = this.normalState.getWidth();
        }

        super.setSize(buttonWidth, h);
    }

    /**
     * This is very basic pointer event handling. It does not expect drag
     * events, only consecutive pressed and released events.
     *
     * CanvasGraphicsItem does not receive any pointer events, they are
     * delivered to parent Canvas, so this method needs to be called from
     * Canvas.pointerPressed() and Canvas.pointerReleased() overrides.
     */
    public void handlePointerEvent(int x, int y) {
        if (this.isVisible() && this.enabled) {
            if (x >= this.getPositionX()
                    && x < (this.getPositionX() + this.getWidth())
                    && y >= this.getPositionY()
                    && y < (this.getPositionY() + this.getHeight())) {
                // Pointer event is in Button area
                if (this.pressed) {
                    // Call event handler
                    this.clickHandler.run();
                    this.setPressed(false);
                } else {
                    this.setPressed(true);
                }
            } else if (this.pressed) {
                this.setPressed(false);
            }
        }
    }

    /**
     * Paint the Button by painting its current state.
     */
    public void paint(Graphics graphics) {
        this.currentState.paint(graphics, this.text);
    }

    /**
     * It is necessary to set parent of the CanvasGraphicsItem to null before
     * the MIDlet terminates.
     */
    public void dispose() {
        this.setParent(null);
    }

    /**
     * Checks the state flags pressed and enabled, and updates the current
     * appearance of the Button.
     */
    private void updateState() {
        if (this.enabled) {
            if (this.pressed) {
                this.currentState = this.pressedState;
            } else {
                this.currentState = this.normalState;
            }
        } else {
            this.currentState = this.dimmedState;
        }
    }

    /**
     * Class encapsulating Button state. Each state has its background image and
     * text color.
     */
    class ButtonState {

        protected int labelColor;
        protected Image background;
        protected CanvasItem owner;

        public ButtonState(CanvasItem owner, String image, int color)
                throws Exception {
            this.owner = owner;
            this.labelColor = color;
            try {
                this.background = Image.createImage(image);
            } catch (IOException e) {
                throw new Exception("Unable to load graphics resources.");
            }
        }

        public int getWidth() {
            return this.background.getWidth();
        }

        public int getHeight() {
            return this.background.getHeight();
        }

        /**
         * Paint the state. Should be called from Button's paint method.
         */
        public void paint(Graphics gfx, String text) {
            gfx.drawImage(
                    background,
                    owner.getWidth() / 2, owner.getHeight() / 2,
                    Graphics.VCENTER | Graphics.HCENTER);
            gfx.setColor(0x000000);
            gfx.drawRect(0, 0, owner.getWidth() - 1, owner.getHeight() - 1);
            gfx.setColor(this.labelColor);
            gfx.drawString(
                    text,
                    owner.getWidth() / 2, (owner.getHeight() / 3) * 2,
                    Graphics.BASELINE | Graphics.HCENTER);
        }
    }
}
