/*
 *   
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */

package com.sun.midp.lcdui;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;

/**
 * The GraphicsAccess interface is designed to provide external access
 * to extended APIs of graphics-related functionality available within
 * javax.microedition.lcdui package only. In particular the extended
 * API for Image and Graphics is provided.
 */
public interface GraphicsAccess {

    
    /**
     * Return a romized image resource based on the identifier.
     *
     * @param imageDataArrayPtr native pointer to image data as Java int
     * @param imageDataArrayLength length of image data array
     * @return the romized Image, null if the identifier was invalid
     *         or the Image did not exist.
     */
    public Image getRomizedImage(int imageDataArrayPtr,
            int imageDataArrayLength);
    

    /**
     * Resize Image optionally saving its content clipped according
     * to the new geometry
     *
     * @param image image to resize
     * @param width new image width
     * @param height new image height
     * @param keepContent true of image content should be kept
     */
    public void resizeImage(Image image,
        int width, int height, boolean keepContent);

    /**
     * Creates a new <code>Graphics</code> object that renders to this
     * image and explicitly sets the Graphics object dimensions.
     *
     * @param image The Image to get a Graphics context for
     * @param width The width of the Graphics context
     * @param height The height of the Graphics context
     * @return a <code>Graphics</code> object with this image as its destination
     */
    public Graphics getImageGraphics(Image image, int width, int height);

    /**
     * Get maximal width of the Graphics context
     * @param g The Graphics context
     * @return The width of the Graphics context
     */
    public int getGraphicsWidth(Graphics g);

    /**
     * Get maximal height of the Graphics context
     * @param g The Graphics context
     * @return The height of the Graphics context
     */
    public int getGraphicsHeight(Graphics g);

    /**
     * Get screen width regarding the rotation mode
     * @return screen width in pixels
     */
    public int getScreenWidth();

    /**
     * Get screen height regarding the rotation mode
     * @return screen height in pixels
     */
    public int getScreenHeight();

    /**
     * Get creator of the Graphics object
     * @param g Graphics object to get creator from
     * @return Graphics creator reference
     */
    public Object getGraphicsCreator(Graphics g);

    /**
     * Set the creator of the Graphics object
     * @param g Graphics object to set creator for
     * @param creator Graphics creator reference
     */
    void setGraphicsCreator(Graphics g, Object creator);

    /**
     * Set new screen dimensions for the graphics
     * @param g Graphics object to set dimension for 
     * @param width new width
     * @param height new height
     */
    public void setDimensions(Graphics g, int width, int height);
}
