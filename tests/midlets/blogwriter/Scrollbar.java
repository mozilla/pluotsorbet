/*
 * Copyright © 2013 Nokia Corporation. All rights reserved. Nokia and Nokia
 * Connecting People are registered trademarks of Nokia Corporation. Oracle and
 * Java are trademarks or registered trademarks of Oracle and/or its affiliates.
 * Other product and company names mentioned herein may be trademarks or trade
 * names of their respective owners. See LICENSE.TXT for license information.
 */

package com.nokia.example;

import javax.microedition.lcdui.Graphics;
import com.nokia.mid.ui.TextEditor;

/**
 * Class encapsulating a very basic scrollbar for TextEditor.
 */
public class Scrollbar {

    private TextEditor owner;
    private int thumbColor;
    private int backgroundColor;
    private int cornersDiameter = 20;
    private int thumbY;
    private int thumbHeight;
    private final int margin = 5;
    public static final int width = 30;

    public Scrollbar(TextEditor owner, int thumbColor, int backgroundColor) {
        this.owner = owner;
        this.thumbColor = thumbColor;
        this.backgroundColor = backgroundColor;
    }

    /**
     * Handles pointer events and sets TextEditor's caret position. Pointer
     * events are relative to Canvas owning the TextEditor. This method expects
     * only pointer pressed events.
     */
    public void handlePointerPressed(int x, int y) {
        int scrollbarX = this.owner.getPositionX() + this.owner.getWidth();
        int textEditorY = this.owner.getPositionY();
        int height = this.owner.getHeight();
        if (x > scrollbarX && x < scrollbarX + Scrollbar.width
                && y > textEditorY && y < textEditorY + height) {
            this.updateThumb();
            
            if(BlogWriter.isS60Platform())
            {
                // Setting caret position works only on Symbian platform.
            	
                int caretY = (int) (this.owner.getContentHeight()
                        * ((float) (y - textEditorY) / (float) height));
                if (caretY < 5) {
                    caretY = 0;
                    this.owner.setCaret(0);
                } else {
                	this.owner.setCaret(caretY - this.owner.getVisibleContentPosition());
                }
            }
            
            /*           		
             	//Enable indicator on JRT version 2.1 for symbian devices by uncommenting the following code. 
         		//JRT versions 2.2 and newer already contain the indicator in the virtual keyboard.

            if (this.owner instanceof com.nokia.mid.ui.S60TextEditor) {
                // Setting caret position works only on Symbian platform.
            	com.nokia.mid.ui.S60TextEditor editor = (com.nokia.mid.ui.S60TextEditor) this.owner;
                int caretY = (int) (this.owner.getContentHeight()
                        * ((float) (y - textEditorY) / (float) height));
                if (caretY < 5) {
                    caretY = 0;
                    this.owner.setCaret(0);
                } else {
                    editor.setCaretXY(0, caretY - this.owner.getVisibleContentPosition());
                	
                }
            }*/
            
            
            
        }
        
    }

    /**
     * Paints the Scrollbar. This method should be called from CanvasTextBox
     * paint method.
     */
    public void paint(Graphics gfx, int x, int y) {
        int height = this.owner.getHeight();

        gfx.setColor(this.backgroundColor);
        gfx.fillRoundRect(
                x + this.margin, y,
                Scrollbar.width - 2 * this.margin, height,
                this.cornersDiameter, this.cornersDiameter);

        this.updateThumb();
        gfx.setColor(this.thumbColor);
        gfx.fillRoundRect(
                x + this.margin + 1, y + this.thumbY + 1,
                Scrollbar.width - 2 * this.margin - 2, this.thumbHeight - 2,
                this.cornersDiameter, this.cornersDiameter);
    }

    /**
     * Updates thumb size and position.
     */
    private void updateThumb() {
        int height = this.owner.getHeight();
        int contentHeight = this.owner.getContentHeight();
        this.thumbHeight = (int) (height * ((float) height / (float) contentHeight));
        if (this.thumbHeight > height) {
            this.thumbHeight = height;
        }
        if (this.thumbHeight < 30) {
            this.thumbHeight = 30;
        }

        int visibleContentPos = this.owner.getVisibleContentPosition();
        // S40 has a bug and return always 0
        int endPosition = contentHeight - height;
        if (visibleContentPos > 0) {
            if (contentHeight - visibleContentPos <= height
                    || endPosition == visibleContentPos) {
                this.thumbY = (int) (height - this.thumbHeight);
            } else {
                this.thumbY = (int) (((float) visibleContentPos / (float) contentHeight) * height);
            }
        } else {
            this.thumbY = 0;
        }
    }
}
