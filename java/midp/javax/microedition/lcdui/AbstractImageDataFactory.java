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

import java.io.InputStream;
import java.io.IOException;

/**
 * Creates ImageDate that is based on platform decoder and storage.
 */
interface AbstractImageDataFactory {

    /**
     * Creates a new, mutable image data for off-screen drawing. Every pixel
     * within the newly created image is white.  The width and height of the
     * image must both be greater than zero.
     *
     * @param width the width of the new image, in pixels
     * @param height the height of the new image, in pixels
     * @return the created image data
     */
    ImageData createOffScreenImageData(int width, int height);

    /**
     * Creates an immutable image data from a source mutable image data.
     *
     * <p> This method is useful for placing the contents of mutable images
     * into <code>Choice</code> objects.  The application can create
     * an off-screen image
     * using the
     * {@link #createImage(int, int) createImage(w, h)}
     * method, draw into it using a <code>Graphics</code> object
     * obtained with the
     * {@link #getGraphics() getGraphics()}
     * method, and then create an immutable copy of it with this method.
     * The immutable copy may then be placed into <code>Choice</code>
     * objects. </p>
     *
     * @param mutableSource the source mutable image data to be copied
     * @return the new immutable image data
     */
    ImageData createImmutableCopy(ImageData mutableSource);

    /**
     * Creates an immutable image data from decoded image data obtained 
     * from the
     * named resource.  The name parameter is a resource name as defined by
     * {@link Class#getResourceAsStream(String)
     * Class.getResourceAsStream(name)}.  The rules for resolving resource 
     * names are defined in the
     * <a href="../../../java/lang/package-summary.html">
     * Application Resource Files</a> section of the
     * <code>java.lang</code> package documentation.
     *
     * @param name the name of the resource containing the image data in one of
     * the supported image formats
     * @return the created image data
     * @throws java.io.IOException if the resource does not exist,
     * the data cannot be loaded, or the image data cannot be decoded
     */
    ImageData createResourceImageData(String name) throws IOException;

    /**
     * Creates an immutable image data which is decoded from the data stored in
     * the specified byte array at the specified offset and length. The data
     * must be in a self-identifying image file format supported by the
     * implementation, such as <a href="#PNG">PNG</A>.
     *
     * <p>The <code>imageoffset</code> and <code>imagelength</code>
     * parameters specify a range of
     * data within the <code>imageData</code> byte array. The
     * <code>imageOffset</code> parameter
     * specifies the
     * offset into the array of the first data byte to be used. It must
     * therefore lie within the range
     * <code>[0..(imageData.length-1)]</code>. The
     * <code>imageLength</code>
     * parameter specifies the number of data bytes to be used. It must be a
     * positive integer and it must not cause the range to extend beyond
     * the end
     * of the array. That is, it must be true that
     * <code>imageOffset + imageLength &lt; imageData.length</code>. </p>
     *
     * <p> This method is intended for use when loading an
     * image from a variety of sources, such as from
     * persistent storage or from the network.</p>
     *
     * @param imageData the array of image data in a supported image format
     * @param imageOffset the offset of the start of the data in the array
     * @param imageLength the length of the data in the array
     *
     * @return the created image data
     * @throws IllegalArgumentException if <code>imageData</code> is incorrectly
     * formatted or otherwise cannot be decoded
     */
    ImageData createImmutableImageData(byte[] imageData, 
                                       int imageOffset, 
                                       int imageLength);

    /**
     * Creates an immutable image data using pixel data from the specified
     * region of a source image data, transformed as specified.
     *
     * <p>The source image may be mutable or immutable.  For immutable source
     * images, transparency information, if any, is copied to the new
     * image unchanged.</p>
     *
     * <p>On some devices, pre-transformed images may render more quickly
     * than images that are transformed on the fly using
     * <code>drawRegion</code>.
     * However, creating such images does consume additional heap space,
     * so this technique should be applied only to images whose rendering
     * speed is critical.</p>
     *
     * <p>The transform function used must be one of the following, as defined
     * in the {@link javax.microedition.lcdui.game.Sprite Sprite} class:<br>
     *
     * <code>Sprite.TRANS_NONE</code> - causes the specified image
     * region to be copied unchanged<br>
     * <code>Sprite.TRANS_ROT90</code> - causes the specified image
     * region to be rotated clockwise by 90 degrees.<br>
     * <code>Sprite.TRANS_ROT180</code> - causes the specified image
     * region to be rotated clockwise by 180 degrees.<br>
     * <code>Sprite.TRANS_ROT270</code> - causes the specified image
     * region to be rotated clockwise by 270 degrees.<br>
     * <code>Sprite.TRANS_MIRROR</code> - causes the specified image
     * region to be reflected about its vertical center.<br>
     * <code>Sprite.TRANS_MIRROR_ROT90</code> - causes the specified image
     * region to be reflected about its vertical center and then rotated
     * clockwise by 90 degrees.<br>
     * <code>Sprite.TRANS_MIRROR_ROT180</code> - causes the specified image
     * region to be reflected about its vertical center and then rotated
     * clockwise by 180 degrees.<br>
     * <code>Sprite.TRANS_MIRROR_ROT270</code> - causes the specified image
     * region to be reflected about its vertical center and then rotated
     * clockwise by 270 degrees.<br></p>
     *
     * <p>
     * The size of the returned image will be the size of the specified region
     * with the transform applied.  For example, if the region is
     * <code>100&nbsp;x&nbsp;50</code> pixels and the transform is
     * <code>TRANS_ROT90</code>, the
     * returned image will be <code>50&nbsp;x&nbsp;100</code> pixels.</p>
     *
     * <p><strong>Note:</strong> If all of the following conditions
     * are met, this method may
     * simply return the source <code>Image</code> without creating a
     * new one:</p>
     * <ul>
     * <li>the source image is immutable;</li>
     * <li>the region represents the entire source image; and</li>
     * <li>the transform is <code>TRANS_NONE</code>.</li>
     * </ul>
     *
     * @param imageData the source image data to be copied from
     * @param x the horizontal location of the region to be copied
     * @param y the vertical location of the region to be copied
     * @param width the width of the region to be copied
     * @param height the height of the region to be copied
     * @param transform the transform to be applied to the region
     * @return the new immutable image data
     *
     */
    ImageData createImmutableImageData(ImageData imageData,
                                       int x, int y, 
                                       int width, int height, 
                                       int transform);

    /**
     * Creates an immutable image data from decoded image data obtained from an
     * <code>InputStream</code>.  This method blocks until all image data has 
     * been read and decoded.  After this method completes (whether by 
     * returning or by throwing an exception) the stream is left open and its 
     * current position is undefined.
     *
     * @param stream the name of the resource containing the image data
     * in one of the supported image formats
     *
     * @return the created image data
     * @throws java.io.IOException if an I/O error occurs, if the image data
     * cannot be loaded, or if the image data cannot be decoded
     *
     */
    ImageData createImmutableImageData(InputStream stream) throws IOException;

    /**
     * Creates an immutable image data from a sequence of ARGB values, 
     * specified
     * as <code>0xAARRGGBB</code>.
     * The ARGB data within the <code>rgb</code> array is arranged
     * horizontally from left to right within each row,
     * row by row from top to bottom.
     * If <code>processAlpha</code> is <code>true</code>,
     * the high-order byte specifies opacity; that is,
     * <code>0x00RRGGBB</code> specifies
     * a fully transparent pixel and <code>0xFFRRGGBB</code> specifies
     * a fully opaque
     * pixel.  Intermediate alpha values specify semitransparency.  If the
     * implementation does not support alpha blending for image rendering
     * operations, it must replace any semitransparent pixels with fully
     * transparent pixels.  (See <a href="#alpha">Alpha Processing</a>
     * for further discussion.)  If <code>processAlpha</code> is
     * <code>false</code>, the alpha values
     * are ignored and all pixels must be treated as fully opaque.
     *
     * <p>Consider <code>P(a,b)</code> to be the value of the pixel
     * located at column <code>a</code> and row <code>b</code> of the
     * Image, where rows and columns are numbered downward from the
     * top starting at zero, and columns are numbered rightward from
     * the left starting at zero. This operation can then be defined
     * as:</p>
     *
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *    P(a, b) = rgb[a + b * width];    </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * <p>for</p>
     *
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     0 &lt;= a &lt; width
     *     0 &lt;= b &lt; height    </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * <p> </p>
     *
     * @param rgb an array of ARGB values that composes the image
     * @param width the width of the image
     * @param height the height of the image
     * @param processAlpha <code>true</code> if <code>rgb</code>
     * has an alpha channel,
     * <code>false</code> if all pixels are fully opaque
     * @return the created immutable image data
     */
    ImageData createImmutableImageData(int rgb[], 
                                       int width, 
                                       int height,
                                       boolean processAlpha);

    /**
     * Create a immutable image from romized image data.
     * 
     * @param imageDataArrayPtr native pointer to image data as Java int
     * @param imageDataArrayLength length of image data array
     * @throws IllegalArgumentException if the id is invalid
     * @return the created image data
     */
    ImageData createImmutableImageData(int imageDataArrayPtr, 
            int imageDataArrayLength);
}
