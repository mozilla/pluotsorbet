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
 * Image that is based on platform decoder and storage.
 */
final class ImageData implements AbstractImageData {

    /**
     * The width, height of this Image
     */
    private int width, height;

    /**
     * If this Image is mutable.
     */
    private boolean isMutable; // = false

    /**
     * Native image data pointer.
     */
    private int nativeImageData;


    /**
     * Constructs <code>ImageData </code> with width and height set to zero.
     */
    ImageData() {
        width = 0;
        height = 0;
    }

    /**
     * Constructs ImageData using passed in width and height.
     *
     * @param width The width of the <code>ImageData </code> to be created.
     * @param height The height of the <code>ImageData </code> to be created.
     * @param isMutable true to create mutable <code>ImageData</code>,
     *                  false to create immutable <code>ImageData</code>
     */
    ImageData(int width, int height, boolean isMutable) {
        this.width = width;
        this.height = height;
        this.isMutable = isMutable;
    }


    /**
     * Gets the width of the image in pixels. The value returned
     * must reflect the actual width of the image when rendered.
     * @return width of the image
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the image in pixels. The value returned
     * must reflect the actual height of the image when rendered.
     * @return height of the image
     */
    public int getHeight() {
        return height;
    }

    /**
     * Check if this image is mutable. Mutable images can be modified by
     * rendering to them through a <code>Graphics</code> object
     * obtained from the
     * <code>getGraphics()</code> method of this object.
     * @return <code>true</code> if the image is mutable,
     * <code>false</code> otherwise
     */
    public boolean isMutable() {
        return isMutable;
    }

    /**
     * Implements <code>AbstractImageData.getRGB() </code>.
     * See javadoc comments there.
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
    public native void getRGB(int[] rgbData, int offset, int scanlength,
			      int x, int y, int width, int height);

    /**
     * Cleanup any native resources used by an ImmutableImage
     */
    private native void finalize();
}
