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

package javax.microedition.lcdui;


/**
 * Image data that is based on platform decoder and storage.
 */
interface AbstractImageData {
    /**
     * Gets the width of the image in pixels. The value returned
     * must reflect the actual width of the image when rendered.
     * @return width of the image
     */
    int getWidth();

    /**
     * Gets the height of the image in pixels. The value returned
     * must reflect the actual height of the image when rendered.
     * @return height of the image 
     */
    int getHeight();

    /**
     * Check if this image is mutable. Mutable images can be modified by
     * rendering to them through a <code>Graphics</code> object
     * obtained from the
     * <code>getGraphics()</code> method of this object.
     * @return <code>true</code> if the image is mutable,
     * <code>false</code> otherwise
     */
    boolean isMutable();


    /**
     * Obtains ARGB pixel data from the specified region of this image and
     * stores it in the provided array of integers.  Each pixel value is
     * stored in <code>0xAARRGGBB</code> format, where the high-order
     * byte contains the
     * alpha channel and the remaining bytes contain color components for
     * red, green and blue, respectively.  The alpha channel specifies the
     * opacity of the pixel, where a value of <code>0x00</code>
     * represents a pixel that
     * is fully transparent and a value of <code>0xFF</code>
     * represents a fully opaque
     * pixel.
     *
     * Note that it is called by <code>Image.java</code> and 
     * the following checks are bing done in <code>Image.java</code>.
     * - throws ArrayIndexOutOfBoundsException if the requested operation would
     * attempt to access an element in the <code>rgbData</code> array
     * whose index is either
     * negative or beyond its length (the contents of the array are unchanged)
     *
     * - throws IllegalArgumentException if the area being retrieved
     *   exceeds the bounds of the source image
     *
     * - throws IllegalArgumentException if the absolute value of
     *   <code>scanlength</code> is less than <code>width</code>
     *
     * - throws NullPointerException if <code>rgbData</code> 
     *   is <code>null</code>
     *
     * @param rgbData an array of integers in which the ARGB pixel data is
     * stored
     * @param offset the index into the array where the first ARGB value
     * is stored
     * @param scanlength the relative offset in the array between
     * corresponding pixels in consecutive rows of the region
     * @param x the x-coordinate of the upper left corner of the region
     * @param y the y-coordinate of the upper left corner of the region
     * @param width the width of the region
     * @param height the height of the region
     */
    void getRGB(int[] rgbData, int offset, int scanlength,
		int x, int y, int width, int height);
}
