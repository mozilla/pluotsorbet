/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists.tool;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A collection of utilities.
 */
public class Util {
	/** The string used to indicate that the string has been truncated. */
	protected static final String TRUNCATION_INDICATOR = "...";
	
	/**
     * Returns the given text truncated to fit the specified width. 
     * If text needs to be truncated then three dots are appended to the end.
     * 
     * @param original the original text.
     * @param width the width in pixels.
     * @param font the font used in drawing the text.
     * @return the truncated text or the original if no truncation is needed.
     * 
     * @throws NullPointerException if <code>text</code> or <code>font</code> is <code>null</code>.
     */
    public static final String truncateText(String original, int width, Font font) {
        int textWidth = font.stringWidth(original);
        
        if (textWidth > width) {
            // Text needs to be truncated.
            final int indicatorWidth = font.stringWidth(TRUNCATION_INDICATOR);
            final int truncateToWidth = width - indicatorWidth;
            
            if (indicatorWidth >= truncateToWidth) {
                // Unlikely situation normally but there isn't enough space
                // for even the indicator.
                return "";
            }
            
            int len = 0;
            
            // Find out how many chars can be added before exceeding the reserved width:
            while (font.substringWidth(original, 0, ++len) <= truncateToWidth) {
            }

            len--;
            StringBuffer sb = new StringBuffer(len + TRUNCATION_INDICATOR.length());    
            sb.append(original.substring(0, len));
            sb.append(TRUNCATION_INDICATOR);
            
            return sb.toString();
        } else {
            return original;
        }
    }
    
    /**
     * Draws an image centered to the given rectangular area.
     * @param g Graphics context.
     * @param img Image to draw.
     * @param x X-coordinate.
     * @param y Y-coordiante.
     * @param maxW Maximum width of image.
     * @param maxH Maximum height of image.
     */
    public static void drawImageCentered(Graphics g, Image img, int x, int y, int maxW, int maxH){
    	if (Log.TEST) Log.note("[Util#drawImageCentered]-->");
        if (img != null) {
            int imgXOff = x + (maxW - img.getWidth()) / 2;
            int imgYOff = y + (maxH - img.getHeight()) / 2;
            g.drawImage(img, imgXOff, imgYOff, Graphics.TOP | Graphics.LEFT);
        }
    }
    
    /**
     * Draws the given string to the given rectangular area, truncates text if needed.
     * 
     * @param g Graphics context.
     * @param str String to draw.
     * @param font Font to use for drawing.
     * @param x X-coordinate.
     * @param y Y-coordinate.
     * @param width Width of the drawable area.
     * @param height Height of the drawable area.
     * @param anchor Anchor point.
     */
    public static void drawStringCenteredAndTruncated(Graphics g, String str, Font font, int x, int y,
    		int width,int height, int anchor){
        if (Log.TEST) Log.note("[ListItem#drawStringCenteredAndTruncated]-->");           
        String truncatedText = truncateText(str, width, font);
        g.setFont(font);
        int fontHeight = font.getHeight();
        g.drawString(truncatedText, x, y + ((height - fontHeight) / 2), anchor);
    }
}
