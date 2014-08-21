/*
 * Copyright © 2013 Nokia Corporation. All rights reserved. Nokia and Nokia
 * Connecting People are registered trademarks of Nokia Corporation. Oracle and
 * Java are trademarks or registered trademarks of Oracle and/or its affiliates.
 * Other product and company names mentioned herein may be trademarks or trade
 * names of their respective owners. See LICENSE.TXT for license information.
 */

package com.nokia.example;

import com.nokia.mid.ui.TextEditor;
import javax.microedition.lcdui.Graphics;

/**
 * Class encapsulating basic controls for TextEditor.
 */
public class Controls {

    private TextEditor owner;
    private int color;
    private int backgroundColor;
    private int backgroundColorPressed;
    private int cornersDiameter = 10;
    private Button[] buttons;

    public Controls(TextEditor owner, int color, int bgColor, int bgColorPressed) {
        this.owner = owner;
        this.color = color;
        this.backgroundColor = bgColor;
        this.backgroundColorPressed = bgColorPressed;
    }

    /*
     * Paint the buttons.
     *
     * On the first call the buttons are created.
     */
    public void paint(Graphics g, int right, int bottom) {
        if (buttons == null) {
            createButtons(g, right, bottom);
        }
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].paint(g);
        }
    }

    /*
     * Create the buttons.
     */
    private void createButtons(Graphics g, int right, int bottom) {
        buttons = new Button[3];
        int w = g.getFont().getHeight() + 5;
        int left = right - w;
        int top = bottom - w;
        buttons[0] = new Button("C", left, top, w, w, new ButtonListener() {

            public void onClick() {
                int caretPosition = owner.getCaretPosition();
                if (caretPosition > 0) {
                    owner.delete(caretPosition - 1, 1);
                    owner.setCaret(caretPosition - 1);
                }
            }
        });
        left -= w;
        buttons[1] = new Button(">", left, top, w, w, new ButtonListener() {

            public void onClick() {
               int caretPosition = owner.getCaretPosition();
                if (caretPosition < owner.size()) {
                    owner.setCaret(caretPosition + 1);
                }
            }
        });
        left -= w;
        buttons[2] = new Button("<", left, top, w, w, new ButtonListener() {

            public void onClick() {
                int caretPosition = owner.getCaretPosition();
                if (caretPosition > 0) {
                    owner.setCaret(caretPosition - 1);
                }
            }
        });
    }

    public void handlePointerPressed(int x, int y) {
        if (buttons == null) {
            return;
        }
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].handlePointerPressed(x, y);
        }
    }

    public void handlePointerReleased(int x, int y) {
        if (buttons == null) {
            return;
        }
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].handlePointerReleased(x, y);
        }
    }

    private class Button {

        private final String text;
        private final int left, top, width, height;
        private final ButtonListener listener;
        private boolean pressed = false;

        public Button(String text, int left, int top, int width, int height, ButtonListener listener) {
            this.text = text;
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height-2;
            this.listener = listener;
        }

        public void paint(Graphics g) {
            if (pressed) {
                g.setColor(backgroundColorPressed);
            } else {
                g.setColor(backgroundColor);
            }
            g.fillRoundRect(left, top, width, height, cornersDiameter, cornersDiameter);
            g.setColor(color);
            g.drawRoundRect(left, top, width, height, cornersDiameter, cornersDiameter);
            g.drawString(text, left + width / 2, top + (height - g.getFont().getHeight()) / 2, Graphics.HCENTER | Graphics.TOP);
        }

        public void handlePointerPressed(int x, int y) {
            if (hits(x, y)) {
                pressed = true;
                listener.onClick();
            } else {
                pressed = false;
            }
        }

        public void handlePointerReleased(int x, int y) {
            pressed = false;
        }

        private boolean hits(int x, int y) {
            return x > left && x < left + width && y > top && y < top + height;
        }
    }

    private interface ButtonListener {

        void onClick();
    }
}
