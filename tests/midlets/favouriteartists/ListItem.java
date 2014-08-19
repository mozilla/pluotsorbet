/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.nokia.example.favouriteartists.tool.Log;
import com.nokia.example.favouriteartists.tool.Util;


/**
 * Represents a single item in the list, responsible for drawing item contents.
 */
public class ListItem {
	
    // Constants
	/** Vertical padding. */
	protected static final int V_PAD = 2;
	/** Horizontal padding. */
	protected static final int H_PAD = 2;
	/** Maximum width of icon */
	protected static final int ICON_MAX_W = 48;
	/** Maximum height of icon */
	protected static final int ICON_MAX_H = 48;
	/** Margin between icon and title text */
	protected static final int ICON_TEXT_MARGIN = 4;
	/** One row item */
    protected static final int TYPE_ONE_ROW = 1;
    /** Two row item */
    protected static final int TYPE_TWO_ROW = 2;
    /** Icon on the left */
    protected static final int TYPE_ICON_LEFT = 4;
    /** Icon on the right */
    protected static final int TYPE_ICON_RIGHT = 8;

    // Member data
    private Display display;
    private String text1;
    private String text2;
    private Image icon;
    private int itemType;
    private int width = 0;
    private int height = 0;
    private boolean selected = false;
    
    // Methods
    /**
     * Constructor.
     * 
     * @param display For retrieving colors.
     * @param text1 Text 1 (e.g. top row text)
     * @param text2 Text 2 (e.g. bottom row text)
     * @param icon Image to use for icon. 
     * @param itemType Item type.
     */
    public ListItem(Display display, String text1, String text2,
    		Image icon, int itemType, int width, int height) {        	 
    	if (Log.TEST) Log.note("[ListItem#ListItem]-->");
    	this.display = display;
        this.text1 = text1;
        this.text2 = text2;
        this.icon = icon;
        this.itemType = itemType;
        this.height = height;
        this.width = width;
    }

    /**
     * Setter for width.
     * 
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Getter for text1.
     * 
     * @return text1
     */
    public String getText1() {
        return text1;
    }
    
    /**
     * Setter for text1
     * 
     * @param text1
     */
    public void setText1(String text1){
    	this.text1 = text1;
    }
    
    /**
     * Getter for text2.
     * 
     * @return text2
     */
    public String getText2() {
        return text2;
    }
    
    /**
     * Setter for text2
     * 
     * @param text2
     */
    public void setText2(String text2){
    	this.text2 = text2;
    }
    
    /**
     * Setter for icon.
     * 
     * @param icon
     */
    public void setIcon(Image icon){
    	this.icon = icon;
    }
    
    /**
	 * Set the item as selected, affects drawing of the item.
	 * 
	 * @param selected
	 */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    
    /**
     * Draws the item.
     * 
     * @param g Graphics context.
     * @param yOffset Y-offset to draw from.
     */
    public void paint(Graphics g, final int yOffset) {
    	if (Log.TEST) Log.note("[ListItem#paint]-->");
        int y = yOffset;
        if (Log.TEST) Log.note("[ListItem#paint] y: " + y);

        // First draw the underlying rect, and get color for text drawing
        if (selected) {
            g.setColor(display.getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND));
            g.fillRect(0, y, width, height);
            g.setColor(display.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND));
        } else {
            g.setColor(display.getColor(Display.COLOR_FOREGROUND));
        }
        
        y += V_PAD;
        int x = H_PAD;
        
        int[] contentsRect = {x, y, width - 2 * H_PAD, height - 2 * V_PAD};

        drawContents(g, contentsRect );

        // Get border color
        if (selected) {
            g.setColor(display.getColor(Display.COLOR_HIGHLIGHTED_BORDER));
        } else {
           // g.setColor(display.getColor(Display.COLOR_BORDER));
            g.setColor(~display.getColor(Display.COLOR_BACKGROUND));
        }
        
        // Draw border
        g.drawLine(0, yOffset + height, width, yOffset + height);
        
        if (Log.TEST) Log.note("[ListItem#paint]<--");
    }
    
    /**
     * Draws item contents.
     * 
     * @param g Graphics context.
     * @param rect Rectangle to draw in.
     */
    protected void drawContents(Graphics g, int[] rect){
    	if (Log.TEST) Log.note("[ListItem#drawContents]-->");
    	// First draw icon
    	int[] remainderRect = drawIcon(g, rect);
    	if((itemType & TYPE_TWO_ROW) > 0){
    		// Draw top row
        	int topRowHeight = drawTopRow(g, remainderRect, false);
        	// Update remainder rect accordingly
        	remainderRect[1] += topRowHeight; // y 
        	remainderRect[3] -= topRowHeight; // height
        	// Last, draw bottom row
	    	drawBottomRow(g, remainderRect);
    	} else {
    		// Draw top row
        	drawTopRow(g, remainderRect, true);
    	}
    }
    
    /**
     * Draws the icon.
     * 
     * @param g Graphics context.
     * @param rect Rectangle to draw in
     * @return remainder rect.
     */
    protected int[] drawIcon(Graphics g, int[] rect){
    	if (Log.TEST) Log.note("[ListItem#drawIcon]-->");
    	if(icon == null){
    		return rect;
    	}
    	int x = rect[0];
    	int y = rect[1];
    	int width = rect[2];
    	int height = rect[3];
    	int iconX = 0;
    	int remainderX = 0;
    	int remainderWidth = 0;
    	if((itemType & TYPE_ICON_LEFT) > 0){
    		iconX = x;
    		remainderX = x + ICON_MAX_W + ICON_TEXT_MARGIN;
    		remainderWidth = width - ICON_MAX_W;
    	} else if ((itemType & TYPE_ICON_RIGHT) > 0) {
    		iconX = x + width - ICON_MAX_W;
    		remainderX = x;
    		remainderWidth = width - ICON_MAX_W - ICON_TEXT_MARGIN;
    	}
    	// Draw the image
    	Util.drawImageCentered(g, icon, iconX, y, ICON_MAX_W, ICON_MAX_H);
    	int[] remainderRect = {remainderX,
        		y,
        		remainderWidth,
        		height};
        return remainderRect;
    }
    
    /**
     * Draws the top row.
     * 
     * @param g Graphics context.
     * @param rect Rectangle to draw in.
     * @param useWholeArea if true then top row will occupy the whole rect.
     * @return Height used for top row.
     */
    protected int drawTopRow(Graphics g, int[] rect, boolean useWholeArea){
    	if (Log.TEST) Log.note("[ListItem#drawTopRow]-->");
    	int x = rect[0];
    	int y = rect[1];
    	int width = rect[2];
    	int height = rect[3];
    	Font font = null;
    	if((itemType & TYPE_ONE_ROW) > 0){
    		font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    	} else if ((itemType & TYPE_TWO_ROW) > 0){
    		font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    	}
    	if(!useWholeArea){
    		height = font.getHeight() + V_PAD * 2;
    	}
    	// Draw the text to the middle of the top row
    	Util.drawStringCenteredAndTruncated(g, text1, font, x, y + V_PAD, width, height, Graphics.TOP | Graphics.LEFT );
    	return height;
    }
    
    /**
     * Draws the bottom row.
     * 
     * @param g Graphics context.
     * @param rect Rectangle to draw in.
     */
    protected void drawBottomRow(Graphics g, int[] rect){
    	if (Log.TEST) Log.note("[ListItem#drawBottomRow]-->");
    	int x = rect[0];
    	int y = rect[1];
    	int width = rect[2];
    	int height = rect[3];
    	Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    	height = font.getHeight() + V_PAD * 2;
    	// Draw the text to the middle of the bottom row
    	Util.drawStringCenteredAndTruncated(g, text2, font, x, y + V_PAD, width, height, Graphics.TOP | Graphics.LEFT );
    }
    
    /**
     * Getter for height.
     * 
     * @return height
     */
    public int getHeight() {
        return height;
    }
}
