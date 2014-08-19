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
 * Represents a single item in the grid, responsible for drawing item contents.
 */
public class GridItem{
    	
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
	/** Horizontal padding between rating stars */
	private static final int STAR_IMG_H_PAD = 2;
	/** Rating star image filename */
	private static final String STAR_IMG_FILE = "midlets/favouriteartists/images/star.png";
	
	// Static members
	private static Image starImg;
    
	// Member data
	/** Display for colors */
	private Display display;
	/** Top-left x-coordinate */
	private int x;
	/** Top-left y-coordinate */
	private int y;
	/** Width */
	private int width;
	/** Height */
	private int height;
	/** Icon for the item. */
	private Image icon;
	/** Defines whether this item is selected, affects the drawing of the item.*/
	private boolean selected;
	/** The favourite data that this item is presenting */
	private FavouriteData favData;
	
	// Methods
	/**
	 * Getter for favourite data.
	 * 
	 * @return Favourite data.
	 */
	public FavouriteData getFavData(){
		return favData;
	}
	
	/**
	 * Setter for favourite data.
	 * @param favData Favourite data to set.
	 */
	public void setFavData(FavouriteData favData){
		this.favData = favData;
	}
	
	
	/**
	 * Constructor.
	 * 
	 * @param display Needed for color retrieval.
	 * @param imageProvider Needed for image retrieval.
	 */
	public GridItem(Display display, ImageProvider imageProvider){
		this.display = display;
		if(starImg == null){
			starImg = imageProvider.getImage(STAR_IMG_FILE);
		}
    }
	
	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	/**
     * Sets the position & size of the item.
     */
    public void setRect(int x, int y, int width, int height){
    	if (Log.TEST) Log.note("[Item#setRect]-->");
    	this.x = x;
    	this.y = y;
    	this.width = width;
    	this.height = height;
    }
    
    /**
	 * @return the icon
	 */
	public Image getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Image icon) {
		this.icon = icon;
	}
	
	/**
	 * Set the item as selected, affects drawing of the item.
	 * 
	 * @param selected
	 */
	public void setSelected(boolean selected){
    	this.selected = selected;
    }
    
    /**
     * Checks whether this item is selected.
     * 
     * @return Boolean value.
     */
    public boolean isSelected(){
    	return selected;
    }
    
    /**
     * Get the x-coordinate of the center point of the item rectangle.
     * 
     * @return X-coordinate of the center point.
     */
    public int getCenterX(){
    	return x + width/2;
    }
    
    /**
     * Get the y-coordinate of the center point of the item rectangle.
     * 
     * @return Y-coordinate of the center point.
     */
    public int getCenterY(){
    	return y + height/2;
    }
    
    
    /**
     * Checks whether the given point is within the given rectangle.
     * 
     * @param x X-coordinate of the point.
     * @param y Y-coordinate of the point.
     * @param rectX X-coordinate of the rectangle.
     * @param rectY Y-coordinate of the rectangle.
     * @param rectWidth Width of the rectangle.
     * @param rectHeight Height of the rectangle.
     * @return true if point is inside rectangle.
     */
    public boolean isInsideRect(int x, int y, int rectX, int rectY, int rectWidth, int rectHeight){
    	if (Log.TEST) Log.note("[GridItem#isInsideRect]--> x: " + x
    			+ " y: " + y
    			+ " rectX: "+ rectX
    			+ " rectY: "+ rectY
    			+ " rectWidth: "+ rectWidth
    			+ " rectHeight: "+ rectHeight);
    	
    	if(x >= rectX && x <= rectX + rectWidth &&
    			y >= rectY && y <= rectY + rectHeight){
    		if (Log.TEST) Log.note("[GridItem#isInsideRect] return true");
    		return true;
    	}
    	return false;
    }
    
    /**
     * Checks whether the given point is inside this items rectangle.
     * 
     * @param x X-coordinate of the point.
     * @param y Y-coordinate of the point.
     * @return true if point is inside rectangle.
     */
    public boolean isInItem(int x, int y){
    	return isInsideRect(x, y, this.x, this.y, width, height);
    }
    
    /**
     * Checks whether this item is visible in the current view,
     * used to decide whether the item needs to be drawn.
     * 
     * @param viewX Top-left x-coordinate of the current view area (grid coordinates, not screen) 
     * @param viewY Top-left y-coordinate of the current view area (grid coordinates, not screen)
     * @param viewWidth Width of the current view area.
     * @param viewHeight Height of the current view area.
     * @return true if item is visible.
     */
    public boolean isVisible(int viewX, int viewY, int viewWidth, int viewHeight){
    	
    	boolean retVal = false;
    	// Top-left
    	if(isInsideRect(x, y, viewX, viewY, viewWidth, viewHeight)){
    		if (Log.TEST) Log.note("[Item#isVisible] TL");
    		retVal = true;
    	}
    	// Top-right
    	else if(isInsideRect(x + width, y, viewX, viewY, viewWidth, viewHeight)){
    		if (Log.TEST) Log.note("[Item#isVisible] TR");
    		retVal = true;
    	}
    	// Bottom-left
    	else if(isInsideRect(x, y + height, viewX, viewY, viewWidth, viewHeight)){
    		if (Log.TEST) Log.note("[Item#isVisible] BL");
    		retVal = true;
    	}
    	// Bottom-right
    	else if(isInsideRect(x + width, y + height, viewX, viewY, viewWidth, viewHeight)){
    		if (Log.TEST) Log.note("[Item#isVisible] BR");
    		retVal = true;
    	}
    	return retVal;
    }

    /**
     * Draws the item.
     * 
     * @param g Graphics context.
     * @param viewX Top-left x-coordinate of the current view area (grid coordinates, not screen) 
     * @param viewY Top-left y-coordinate of the current view area (grid coordinates, not screen)
     */
    public void paint(Graphics g, int viewX, int viewY){
		if (Log.TEST) Log.note("[GridItem#paint]-->");
    	// Calculate actual drawing coordinates from the view coordinates
		int translatedX = x - viewX;
    	int translatedY = y - viewY;
    	
        // First draw the underlying rectangle, and get color for text drawing
        if (selected) {
            g.setColor(display.getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND));
            // Draw highlighted background
            g.fillRect(translatedX, translatedY, width, height);
            g.setColor(display.getColor(Display.COLOR_HIGHLIGHTED_BORDER));
            // Draw border
            g.drawRect(translatedX, translatedY, width, height);
            // Set color for text drawing
            g.setColor(display.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND));
        } else {
        	//g.setColor(display.getColor(Display.COLOR_BORDER));
        	//g.setColor(display.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND));
                g.setColor(~display.getColor(Display.COLOR_BACKGROUND));

        	// Draw border
        	g.drawRect(translatedX, translatedY, width, height);
        	// Set color for text drawing
            g.setColor(display.getColor(Display.COLOR_FOREGROUND));
        }
        
        drawContents(g, translatedX + V_PAD, translatedY + H_PAD,
        		width - 2 * H_PAD, height - 2 * V_PAD);
        }
    
    /**
     * Draw item contents.
     * 
     * @param g Graphics context
     * @param x X-coordinate.
     * @param y Y-coordinate.
     * @param width Width of the drawable area.
     * @param height Height of the drawable area.
     */
    protected void drawContents(Graphics g, int x, int y, int width, int height){
    	if (Log.TEST) Log.note("[GridItem#drawContents]-->");
    	// Divide item vertically to three parts: icon+name, rating and comment
    	int topRowHeight = height / 3;
    	int middleRowY = y + topRowHeight;
    	int middleRowHeight = topRowHeight;
    	int bottomRowY = middleRowY + middleRowHeight;
    	int bottomRowHeight = y + height - bottomRowY;
    	
    	// Draw icon  to the top left area
    	Util.drawImageCentered(g, icon, x, y, ICON_MAX_W, ICON_MAX_H);
    	// Draw artist name next to icon
    	Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    	Util.drawStringCenteredAndTruncated(g, favData.getName(), font, x + ICON_MAX_H + ICON_TEXT_MARGIN, y,
    			width - ICON_MAX_H - ICON_TEXT_MARGIN, topRowHeight,
    			Graphics.TOP | Graphics.LEFT );
    	// Draw rating in the middle part
    	int starImgX = x + STAR_IMG_H_PAD;
		for(int i = 1; i <= favData.getRating(); i++) {
                    
                    Util.drawImageCentered(g, starImg, starImgX, middleRowY, starImg.getWidth()+ STAR_IMG_H_PAD * 2, middleRowHeight);
                    starImgX += STAR_IMG_H_PAD + starImg.getWidth();
		}
		// Draw comment in the bottom part
		font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		Util.drawStringCenteredAndTruncated(g, favData.getComment(), font, x, bottomRowY,
    			width, bottomRowHeight,
    			Graphics.TOP | Graphics.LEFT );
    }
}