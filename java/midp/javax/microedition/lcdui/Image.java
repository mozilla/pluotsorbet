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
import javax.microedition.lcdui.game.Sprite;


/**
 * The <code>Image</code> class is used to hold graphical image
 * data. <code>Image</code>
 * objects exist independently of the display device. They exist only in
 * off-screen memory and will not be painted on the display unless an explicit
 * command is issued by the application (such as within the
 * <code>paint()</code> method of
 * a <code>Canvas</code>) or when an <code>Image</code> object is
 * placed within a <code>Form</code> screen or an
 * <code>Alert</code> screen and that screen is made current.
 *
 * <p>Images are either <em>mutable</em> or <em>immutable</em> depending upon
 * how they are created. Immutable images are generally created by loading
 * image data from resource bundles, from files, or from the network. They may
 * not be modified once created. Mutable images are created as blank images
 * containing only white pixels. The application may render on a mutable image
 * by calling {@link #getGraphics} on the <code>Image</code> to obtain
 * a <code>Graphics</code> object
 * expressly for this purpose.</p>
 *
 * <p><code>Images</code> may be placed within <code>Alert</code>,
 * <code>Choice</code>, <code>Form</code>, or <code>ImageItem</code>
 * objects.
 * The high-level user interface implementation may need to update the display
 * at any time, without notifying the application.  In order to provide
 * predictable behavior, the high-level user interface
 * objects provide snapshot semantics for the image.  That is, when a mutable
 * image is placed within an <code>Alert</code>, <code>Choice</code>,
 * <code>Form</code>, or <code>ImageItem</code> object,
 * the effect is as if a snapshot is taken of its current contents.  This
 * snapshot is then used for all subsequent painting of the high-level user
 * interface component.  If the application modifies the contents of the
 * image, the application must update the component containing the image (for
 * example, by calling <code>ImageItem.setImage</code>) in order to
 * make the modified
 * contents visible.</p>
 *
 * <p>An immutable image may be created from a mutable image through the
 * use of the {@link #createImage(Image) createImage} method. It is possible
 * to create a mutable copy of an immutable image using a technique similar
 * to the following: </p>
 *
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *    <pre><code>
 *    Image source; // the image to be copied    
 *    source = Image.createImage(...);    
 *    Image copy = Image
 *        .createImage(source.getWidth(), source.getHeight());        
 *    Graphics g = copy.getGraphics();    
 *    g.drawImage(source, 0, 0, TOP|LEFT);       </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 * <a name="alpha"></a>
 * <h3>Alpha Processing</h3>
 *
 * <p>Every pixel within a mutable image is always fully opaque.  Immutable
 * images may contain a combination of fully opaque pixels 
 * <code>(alpha = 2<sup><em>bitdepth</em></sup>&nbsp;-&nbsp;1)</code>, fully
 * transparent pixels (<code>alpha&nbsp;=&nbsp;0</code>), and
 * semitransparent pixels
 * (<code>0&nbsp;&lt;&nbsp;alpha&nbsp;&lt;&nbsp;
 * 2<sup><em>bitdepth</em></sup>&nbsp;-&nbsp;1</code>),
 * where <em>bitdepth</em> is the number of bits per sample in the source data.
 *
 * <p>Implementations must support storage, processing, and rendering of fully
 * opaque pixels and fully transparent pixels in immutable images.  When
 * creating an image from source data (whether from a PNG file or from an
 * array of ARGB data), a fully opaque pixel in the source data must always
 * result in a fully opaque pixel in the new image, and a fully transparent
 * pixel in the source data must always result in a fully transparent pixel in
 * the new image.
 *
 * <p>The required treatment of semitransparent pixel data depends upon
 * whether the implementation supports alpha blending at rendering time.  If
 * the implementation supports alpha blending, a semitransparent pixel in the
 * source data must result in a semitransparent pixel in the new image.  The
 * resulting alpha value may be modified to accommodate the number of levels
 * of semitransparency supported by the platform.  (See the {@link
 * Display#numAlphaLevels() Display.numAlphaLevels()} method.)  If an
 * implementation does not support alpha blending, any semitransparent pixels
 * in the source data must be replaced with fully transparent pixels in the
 * new image.
 *
 * <a name="PNG"></a>
 * <h3>PNG Image Format</h3>
 *
 * <p>Implementations are required to support images stored in the PNG format,
 * as specified by the <em>PNG (Portable Network Graphics) Specification,
 * Version 1.0.</em> All conforming MIDP implementations are also conformant
 * to the minimum set of requirements given by the <em>PNG Specification</em>.
 * MIDP implementations also must conform to additional requirements given
 * here with respect to handling of PNG images.  Note that the requirements
 * listed here take precedence over any conflicting recommendations given in
 * the <em>PNG Specification</em>.</p>
 *
 * <h4>Critical Chunks</h4>
 *
 * <p>All of the 'critical' chunks specified by PNG must be supported. The
 * paragraphs below describe these critical chunks.</p>
 *
 * <p>The IHDR chunk.  MIDP devices must handle the following values in
 * the IHDR chunk:</p>
 *
 * <ul>
 * <li>All positive values of width and height are supported; however, a
 * very large image may not be readable because of memory constraints. The
 * dimensions of the resulting <code>Image</code> object must match 
 * the dimensions of the PNG image.  That is, the values returned by
 * {@link #getWidth() getWidth()} and {@link #getHeight() getHeight()}
 * and the rendered width and height must
 * equal the width and height specified in the IHDR chunk.</li>
 *
 * <li>All color types are supported, although the appearance of the image will
 * be dependent on the capabilities of the device's screen.  Color types that
 * include alpha channel data are supported.</li>
 *
 * <li> For color types <code>4</code> &amp; <code>6</code> (grayscale
 * with alpha and RGB with alpha,
 * respectively) the alpha channel must be decoded. Any pixels with an alpha
 * value of zero must be treated as transparent.  Any pixels with an alpha
 * value of <code>255</code> (for images with <code>8</code> bits per
 * sample) or <code>65535</code> (for images with
 * <code>16</code> bits per sample) must be treated as opaque.  If
 * rendering with alpha
 * blending is supported, any pixels with intermediate alpha values must be
 * carried through to the resulting image.  If alpha blending is not
 * supported, any pixels with intermediate alpha values must be replaced with
 * fully transparent pixels.</li>
 *
 * <li>All bit depth values for the given color type are supported.</li>
 *
 * <li>Compression method <code>0</code> (deflate) is the only
 * supported compression method.
 * This method utilizes the &quot;zlib&quot; compression scheme, which
 * is also used for
 * jar files; thus, the decompression (inflate) code may be shared between the
 * jar decoding and PNG decoding implementations.  As noted in the PNG
 * specification, the compressed data stream may comprised internally of both
 * compressed and uncompressed (raw) data.
 * </li>
 *
 * <li>The filter method represents a series of encoding schemes that may be
 * used to optimize compression.  The PNG spec currently defines a single
 * filter method (method <code>0</code>) that is an adaptive filtering
 * scheme with five
 * basic filter types.  Filtering is essential for optimal compression since it
 * allows the deflate algorithm to exploit spatial similarities within the
 * image.  Therefore, MIDP devices must support all five filter types defined
 * by filter method <code>0</code>.</li>
 *
 * <li> MIDP devices are required to read PNG images that are encoded with
 * either interlace method <code>0</code> (None) or interlace method
 * <code>1</code> (Adam7).  Image
 * loading in MIDP is synchronous and cannot be overlapped with image
 * rendering, and so there is no advantage for an application to use interlace
 * method <code>1</code>.  Support for decoding interlaced images is
 * required for
 * compatibility with PNG and for the convenience of developers who may already
 * have interlaced images available.</li>
 *
 * </ul>
 *
 * <p>The PLTE chunk. Palette-based images must be supported.</p>
 *
 * <p>The IDAT chunk.  Image data may be encoded using any of the
 * <code>5</code> filter
 * types defined by filter method <code>0</code> (None, Sub, Up,
 * Average, Paeth).</p>
 *
 * <p>The IEND chunk.  This chunk must be found in order for the image to be
 * considered valid.</p>
 *
 * <h4>Ancillary Chunks</h4>
 *
 * <p>PNG defines several 'ancillary' chunks that may be present in a
 * PNG image but are not critical for image decoding.</p>
 *
 * <p>The tRNS chunk.  All implementations must support the tRNS chunk.
 * This chunk is used to implement transparency without providing alpha
 * channel data for each pixel. For color types <code>0</code> and
 * <code>2</code>, a particular
 * gray or RGB value is defined to be a transparent pixel.  In this case, the
 * implementation must treat pixels with this value as fully transparent.
 * Pixel value comparison must be based on the actual pixel values using the
 * original sample depth; that is, this comparison must be performed before
 * the pixel values are resampled to reflect the display capabilities
 * of the device. For color type <code>3</code> (indexed color),
 * <code>8</code>-bit alpha values are
 * potentially provided for each entry in the color palette.  In this case,
 * the implementation must treat pixels with an alpha value of
 * <code>0</code> as fully
 * transparent, and it must treat pixels with an alpha value of
 * <code>255</code> as fully
 * opaque.  If rendering with alpha blending is supported, any pixels with
 * intermediate alpha values must be carried through to the resulting image.
 * If alpha blending is not supported, any pixels with intermediate alpha
 * values must be replaced with fully transparent pixels.</p>
 *
 * <p>The implementation <em>may</em> (but is not required to) support
 * any of the other ancillary chunks. The implementation <em>must</em>
 * silently ignore any unsupported ancillary chunks that it encounters.
 * The currently defined optional ancillary chunks are:</p>
 *
 * <PRE>
 *    cHRM gAMA hIST iCCP iTXt pHYs
 *    sBIT sPLT sRGB tEXt tIME zTXt </PRE>
 *
 * <h3>Reference</h3>
 *
 * <p><em>PNG (Portable Network Graphics) Specification, Version 1.0.</em>
 * W3C Recommendation, October 1, 1996. http://www.w3.org/TR/REC-png.html.
 * Also available as RFC 2083, http://www.ietf.org/rfc/rfc2083.txt.</p>
 * @since MIDP 1.0
 */

public class Image {
    /**
     * Width of the image in pixels.
     */
    private int width;

    /**
     * Height of the image in pixels.
     */
    private int height;

    /**
     * <code>ImageData</code> instance associated with this <code>Image</code>.
     */
    private ImageData imageData;
    
    /**
     * Valid transforms possible are 0 - 7
     */
    static final int INVALID_TRANSFORM_BITS = 0xFFFFFFF8;

    /**
     * Transform swap axis bit is the 3 bit
     */
    static final int TRANSFORM_SWAP_AXIS = 4;

    /**
     * Creates a new, mutable image for off-screen drawing. Every pixel
     * within the newly created image is white.  The width and height of the
     * image must both be greater than zero.
     *
     * @param width the width of the new image, in pixels
     * @param height the height of the new image, in pixels
     * @return the created image
     *
     * @throws IllegalArgumentException if either <code>width</code> or
     * <code>height</code> is zero or less
     */
    public static Image createImage(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }

        // SYNC NOTE: Not accessing any shared data, no locking necessary
        return new Image(ImageDataFactory.getImageDataFactory().
                         createOffScreenImageData(width, height));
    }

    /**
     * Creates an immutable image from a source image.
     * If the source image is mutable, an immutable copy is created and
     * returned.  If the source image is immutable, the implementation may
     * simply return it without creating a new image.  If an immutable source
     * image contains transparency information, this information is copied to
     * the new image unchanged.
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
     * @param source the source image to be copied
     * @return the new, immutable image
     *
     * @throws NullPointerException if <code>source</code> is <code>null</code>
     */
    public static Image createImage(Image source) {
        // SYNC NOTE: Not accessing any shared data, no locking necessary
        if (source.isMutable()) {
            return new Image(ImageDataFactory.getImageDataFactory().
                             createImmutableCopy(source.imageData));
        } else {
            return source;
        }
    }

    /**
     * Creates an immutable image from decoded image data obtained from the
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
     * @return the created image
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @throws java.io.IOException if the resource does not exist,
     * the data cannot
     * be loaded, or the image data cannot be decoded
     */
    public static Image createImage(java.lang.String name)
        throws java.io.IOException {
        return new Image(ImageDataFactory.getImageDataFactory().
                         createResourceImageData(name));
    }

    /**
     * Creates an immutable image which is decoded from the data stored in
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
     * @return the created image
     * @throws ArrayIndexOutOfBoundsException if <code>imageOffset</code>
     * and <code>imageLength</code>
     * specify an invalid range
     * @throws NullPointerException if <code>imageData</code> is
     * <code>null</code>
     * @throws IllegalArgumentException if <code>imageData</code> is incorrectly
     * formatted or otherwise cannot be decoded
     */
    public static Image createImage(byte[] imageData, int imageOffset,
                                    int imageLength) {

        if (imageOffset < 0 || imageOffset >= imageData.length ||
            imageLength < 0 ||
            imageOffset + imageLength > imageData.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return new Image(ImageDataFactory.getImageDataFactory().
                         createImmutableImageData(imageData, 
                                                  imageOffset, 
                                                  imageLength));
    }

    /**
     * Creates an immutable image using pixel data from the specified
     * region of a source image, transformed as specified.
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
     * @param image the source image to be copied from
     * @param x the horizontal location of the region to be copied
     * @param y the vertical location of the region to be copied
     * @param width the width of the region to be copied
     * @param height the height of the region to be copied
     * @param transform the transform to be applied to the region
     * @return the new, immutable image
     *
     * @throws NullPointerException if <code>image</code> is <code>null</code>
     * @throws IllegalArgumentException if the region to be copied exceeds
     * the bounds of the source image
     * @throws IllegalArgumentException if either <code>width</code> or
     * <code>height</code> is zero or less
     * @throws IllegalArgumentException if the <code>transform</code>
     * is not valid
     *
     */
    public static Image createImage(Image image,
                                    int x, int y, int width, int height,
                                    int transform) {

        if ((transform & INVALID_TRANSFORM_BITS) != 0) {
            throw new IllegalArgumentException();
        }

        if (x < 0 || y < 0 ||
            (x + width) > image.getWidth() || // throws NPE if image is null
            (y + height) > image.getHeight() ||
            width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }

        if (x == 0 && y == 0 
            && width == image.getWidth() && height == image.getHeight()
            && transform == Sprite.TRANS_NONE) {
            return createImage(image);
        } else {
            return 
              new Image(ImageDataFactory.getImageDataFactory().
                        createImmutableImageData(image.imageData, 
                                                 x, y, 
                                                 width, height, 
                                                 transform));
        }
    }

    /**
     * Creates an immutable image from decoded image data obtained froH an
     * <code>InputStream</code>.  This method blocks until all image data has 
     * been read and decoded.  After this method completes (whether by 
     * returning or by throwing an exception) the stream is left open and its 
     * current position is undefined.
     *
     * @param stream the name of the resource containing the image data
     * in one of the supported image formats
     *
     * @return the created image
     * @throws NullPointerException if <code>stream</code> is <code>null</code>
     * @throws java.io.IOException if an I/O error occurs, if the image data
     * cannot be loaded, or if the image data cannot be decoded
     *
     */
    public static Image createImage(InputStream stream)
        throws java.io.IOException {

        if (stream == null) {
            throw new java.lang.NullPointerException();
        }

        try {
            return new Image(ImageDataFactory.getImageDataFactory().
                             createImmutableImageData(stream));
        } catch (IllegalArgumentException e) {
            throw new java.io.IOException();
        }
    }

    /**
     * Creates an immutable image from a sequence of ARGB values, specified
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
     * @return the created image
     * @throws NullPointerException if <code>rgb</code> is <code>null</code>.
     * @throws IllegalArgumentException if either <code>width</code> or
     * <code>height</code> is zero or less
     * @throws ArrayIndexOutOfBoundsException if the length of
     * <code>rgb</code> is
     * less than<code> width&nbsp;*&nbsp;height</code>.
     *
     */
    public static Image createRGBImage(int rgb[], int width,
                                       int height, boolean processAlpha) {

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }

        if ((width * height) > rgb.length) { 
            throw new ArrayIndexOutOfBoundsException();
        }

        return new Image(ImageDataFactory.getImageDataFactory().
                         createImmutableImageData(rgb, 
                                                  width, height, 
                                                  processAlpha));
    }

    /**
     * Creates a new <code>Graphics</code> object that renders to this
     * image. This image
     * must be
     * mutable; it is illegal to call this method on an immutable image.
     * The mutability of an image may be tested
     * with the <code>isMutable()</code> method.
     *
     * <P>The newly created <code>Graphics</code> object has the
     * following properties:
     * </P>
     * <UL>
     * <LI>the destination is this <code>Image</code> object;</LI>
     * <LI>the clip region encompasses the entire <code>Image</code>;</LI>
     * <LI>the current color is black;</LI>
     * <LI>the font is the same as the font returned by
     * {@link Font#getDefaultFont() Font.getDefaultFont()};</LI>
     * <LI>the stroke style is {@link Graphics#SOLID SOLID}; and
     * </LI>
     * <LI>the origin of the coordinate system is located at the upper-left
     * corner of the Image.</LI>
     * </UL>
     *
     * <P>The lifetime of <code>Graphics</code> objects created using
     * this method is
     * indefinite.  They may be used at any time, by any thread.</P>
     *
     * @return a <code>Graphics</code> object with this image as its destination
     * @throws IllegalStateException if the image is immutable
     */
    public Graphics getGraphics() {
        if (isMutable()) {
            // SYNC NOTE: no locking necessary as getGraphics() only allocates
            // a new object
            return Graphics.getImageGraphics(this);
        } else {
            // SYNC NOTE: Not accessing any shared data, no locking necessary
            throw new IllegalStateException();
        }
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
        return imageData.isMutable();
    }


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
     * <p> The returned values are not guaranteed to be identical to values
     * from the original source, such as from
     * <code>createRGBImage</code> or from a PNG
     * image.  Color values may be resampled to reflect the display
     * capabilities of the device (for example, red, green or blue pixels may
     * all be represented by the same gray value on a grayscale device).  On
     * devices that do not support alpha blending, the alpha value will be
     * <code>0xFF</code> for opaque pixels and <code>0x00</code> for
     * all other pixels (see <a
     * href="#alpha">Alpha Processing</a> for further discussion.)  On devices
     * that support alpha blending, alpha channel values may be resampled to
     * reflect the number of levels of semitransparency supported.</p>
     *
     * <p>The <code>scanlength</code> specifies the relative offset within the
     * array between the corresponding pixels of consecutive rows.  In order
     * to prevent rows of stored pixels from overlapping, the absolute value
     * of <code>scanlength</code> must be greater than or equal to
     * <code>width</code>.  Negative values of <code>scanlength</code> are
     * allowed.  In all cases, this must result in every reference being
     * within the bounds of the <code>rgbData</code> array.</p>
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
     *    rgbData[offset + (a - x) + (b - y) * scanlength] = P(a, b);
     *         </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * <p>for</p>
     *
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     x &lt;= a &lt; x + width
     *     y &lt;= b &lt; y + height    </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     *
     * <p>The source rectangle is required to not exceed the bounds of
     * the image.  This means: </p>
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *   x &gt;= 0
     *   y &gt;= 0
     *   x + width &lt;= image width
     *   y + height &lt;= image height    </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * <p>
     * If any of these conditions is not met an
     * <code>IllegalArgumentException</code> is thrown.  Otherwise, in
     * cases where <code>width &lt;= 0</code> or <code>height &lt;= 0</code>,
     * no exception is thrown, and no pixel data is copied to
     * <code>rgbData</code>.</p>
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
     *
     * @throws ArrayIndexOutOfBoundsException if the requested operation would
     * attempt to access an element in the <code>rgbData</code> array
     * whose index is either
     * negative or beyond its length (the contents of the array are unchanged)
     *
     * @throws IllegalArgumentException if the area being retrieved
     * exceeds the bounds of the source image
     *
     * @throws IllegalArgumentException if the absolute value of
     * <code>scanlength</code> is less than <code>width</code>
     *
     * @throws NullPointerException if <code>rgbData</code> is <code>null</code>
     *
     * @since MIDP 2.0
     */
    public void getRGB(int[] rgbData, int offset, int scanlength,
		       int x, int y, int width, int height) {

	int img_width  = imageData.getWidth();
	int img_height = imageData.getHeight();


	// see if absolute value of scanlength is greater than or 
        // equal to width
	if (scanlength >= 0 && scanlength < width) {
	    throw new IllegalArgumentException();
	} else if (scanlength < 0 && (0 - scanlength) < width) {
	    throw new IllegalArgumentException();
	} else if((y < 0) || (x < 0) || (x + width > img_width) ||
		  (y + height > img_height)) {
	    throw new IllegalArgumentException();
	} else if (height < 0 || width < 0 ) {
	    /* spec says noop in this case */
	} else {
	    // will throw a NullPointerException
	    int buflen = rgbData.length;
	    
	    if (offset < 0
		|| offset + ((height - 1) * scanlength) + width > buflen
		|| offset + ((height - 1) * scanlength) < 0) {
		throw new ArrayIndexOutOfBoundsException();
	    }
	
	    imageData.getRGB(rgbData, offset, scanlength, 
			     x, y, width, height);
	}
    }

    /**
     * Returns <code>ImageData</code> associated with this 
     * <code>Image</code>.
     *
     * @return The <code>ImageData </code> associated with this 
     * <code>Image</code>.
     */
    ImageData getImageData() {
        return imageData;
    }


    /**
     * Function to load an romized Image.
     *
     * @param imageDataArrayPtr native pointer to image data as Java int
     * @param imageDataArrayLength length of image data array
     * @return image created. Null if no romized image matches the id.
     */
    static Image getRomizedImage(int imageDataArrayPtr, 
            int imageDataArrayLength) {

        try {
            AbstractImageDataFactory f = 
                ImageDataFactory.getImageDataFactory();
            ImageData data = f.createImmutableImageData(imageDataArrayPtr, 
                    imageDataArrayLength);
            return new Image(data);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    /**
     * Creates an Immutable image from the given ImageData.
     * @param imageData <code>ImageData</code> instance to be used to
     *                  create new Image
     */
    private Image(ImageData imageData) {
        this.imageData = imageData;
        this.width     = imageData.getWidth();
        this.height    = imageData.getHeight();
    }

    /**
     * Resize Image optionally saving its content clipped according
     * to the new geometry
     *
     * @param width new width of the Image
     * @param height new height of the Image
     * @param keepContent keep current content of the image
     *   binded to the (0, 0) of the resized image and clipped
     *   according to the new image dimensions 
     */
    void resize(int width, int height, boolean keepContent) {
        if (!imageData.isMutable() || width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }

        // IMPL_NOTE: In the case content is not kept it is possible
        //   to resize the image more efficiently, especially for the
        //   case of rotation, when the memory reallocation is not needed.
        //   However, now there are no scenarios when resize is needed 
        //   without content saving.
        Image newImage = createImage(width, height);
        synchronized(this) {
            if (keepContent) {
                Graphics.getImageGraphics(newImage).render(this, 0, 0,
                    Graphics.TOP | Graphics.LEFT);
            }
            this.width = width;
            this.height = height;
            imageData = newImage.getImageData();
        }
    }

}
