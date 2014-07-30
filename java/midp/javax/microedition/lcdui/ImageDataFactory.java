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
 * Creates ImageData based on platform decoder and storage.
 */
class ImageDataFactory implements AbstractImageDataFactory {

    /** Reference to a image cache. */
    private SuiteImageCache imageCache;

    /** Initialize the image cache factory. */
    private ImageDataFactory() {
        imageCache = SuiteImageCacheFactory.getCache();
    }

    /**
     * Singleton <code>ImageDataFactory</code> instance.
     */
    private static ImageDataFactory imageDataFactory =
        new ImageDataFactory();

    /**
     * Returns the singleton <code>ImageDataFactory</code> instance.
     *
     * @return the singleton <code>ImageDataFactory</code> instance.
     */
    public static AbstractImageDataFactory getImageDataFactory() {
        return imageDataFactory;
    }

    /**
     * Creates a new, mutable image for off-screen drawing. Every pixel
     * within the newly created image is white.  The width and height of the
     * image must both be greater than zero.
     *
     * @param width the width of the new image, in pixels
     * @param height the height of the new image, in pixels
     * @return the created image data
     */
    public ImageData createOffScreenImageData(int width, int height) {
        ImageData data = new ImageData(width, height, true);

        // Create native image data and store its pointer in nativeImageData
        try {
            createMutableImageData(data, width, height);
        } catch(OutOfMemoryError e) {
            garbageCollectImages(false);

            try {
                createMutableImageData(data, width, height);
            } catch(OutOfMemoryError e2) {
                garbageCollectImages(true);

                createMutableImageData(data, width, height);
            }
        }

        return data;
    }

    /**
     * Creates an immutable image from a source mutable image.
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
     * @param mutableSource the source mutable image to be copied
     * @return the new immutable image
     */
    public ImageData createImmutableCopy(ImageData mutableSource) {
        ImageData data = new ImageData(mutableSource.getWidth(),
                                       mutableSource.getHeight(),
                                       false);


        // Duplicate mutable image contents
        try {
            createImmutableImageDataCopy(data, mutableSource);
        } catch(OutOfMemoryError e) {
            garbageCollectImages(false);

            try {
                createImmutableImageDataCopy(data, mutableSource);
            } catch(OutOfMemoryError e2) {
                garbageCollectImages(true);

                createImmutableImageDataCopy(data, mutableSource);
            }
        }

        return data;
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
     * @return the created image data
     * @throws java.io.IOException if the resource does not exist,
     * the data cannot be loaded, or the image data cannot be decoded
     */
    public ImageData createResourceImageData(String name) throws IOException {
        if (name == null) {
            throw new java.lang.NullPointerException();
        }

        ImageData data = new ImageData();

        // width, height and native data will be set below

        /*
         * Load native image data from cache and create
         * image, if available. If image is not cached,
         * proceed to load and create image normally.
         */
        if (loadAndCreateImmutableImageDataFromCache(data, name)) {
            return data;
        }
        
        /*
         * allocate an array and read in the bits using
         * Class.getResourceAsStream(name);
         */
        InputStream is = getClass().getResourceAsStream(name);

        /*
         * If the InputStream "is" is null, when "name" is
         * is not null, throw an IOException, not a NullPointerException
         */
        if (is == null) {
            throw new java.io.IOException();
        }

        try {
            getImageDataFromStream(data, is);
        } catch(OutOfMemoryError e) {
            garbageCollectImages(false);

            try {
                getImageDataFromStream(data, is);
            } catch(OutOfMemoryError e2) {
                garbageCollectImages(true);

                getImageDataFromStream(data, is);
            }
        }

        return data;
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
     * @param imageBytes the array of image data in a supported image format
     * @param imageOffset the offset of the start of the data in the array
     * @param imageLength the length of the data in the array
     *
     * @return the created image data
     * @throws IllegalArgumentException if <code>imageData</code> is incorrectly
     * formatted or otherwise cannot be decoded
     */
    public ImageData createImmutableImageData(byte[] imageBytes,
                                              int imageOffset,
                                              int imageLength) {
        ImageData data = new ImageData();

        // width, height and native data will be set below
        try {
            createImmutableImageDecodeImage(data, imageBytes, imageOffset,
                                            imageLength);
        } catch(OutOfMemoryError e) {
            garbageCollectImages(false);

            try {
                createImmutableImageDecodeImage(data,
                                                imageBytes, imageOffset,
                                                imageLength);
            } catch(OutOfMemoryError e2) {
                garbageCollectImages(true);

                createImmutableImageDecodeImage(data, imageBytes, imageOffset,
                                                imageLength);
            }
        }

        return data;
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
     * @param dataSource the source image data to be copied from
     * @param x the horizontal location of the region to be copied
     * @param y the vertical location of the region to be copied
     * @param width the width of the region to be copied
     * @param height the height of the region to be copied
     * @param transform the transform to be applied to the region
     * @return the new immutable image data
     *
     */
    public ImageData createImmutableImageData(ImageData dataSource,
                                              int x, int y,
                                              int width, int height,
                                              int transform) {
        ImageData dataDest;

        if ((transform & Image.TRANSFORM_SWAP_AXIS) != 0x0) {
            dataDest = new ImageData(height, width, false);
        } else {
            dataDest = new ImageData(width, height, false);
        }

        // Copy native data from the source region
        try {
            createImmutableImageDataRegion(dataDest, dataSource,
                                           x, y, width, height,
                                           transform,
                                           dataSource.isMutable());
        } catch(OutOfMemoryError e) {
            garbageCollectImages(false);

            try {
                createImmutableImageDataRegion(dataDest,
                                               dataSource,
                                               x, y, width, height,
                                               transform,
                                               dataSource.isMutable());
            } catch(OutOfMemoryError e2) {
                garbageCollectImages(true);

                createImmutableImageDataRegion(dataDest, dataSource,
                                               x, y, width, height,
                                               transform,
                                               dataSource.isMutable());
            }
        }

        return dataDest;
    }

    /**
     * Creates an immutable image from decoded image data obtained from an
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
    public ImageData createImmutableImageData(InputStream stream)
      throws IOException {
        ImageData data = new ImageData();

        // width, height and native data will be set below
        try {
            getImageDataFromStream(data, stream);
        } catch(OutOfMemoryError e) {
            garbageCollectImages(false);

            try {
                getImageDataFromStream(data, stream);
            } catch(OutOfMemoryError e2) {
                garbageCollectImages(true);

                getImageDataFromStream(data, stream);
            }
        }

        return data;
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
     * @return the created image data
     */
    public ImageData createImmutableImageData(int rgb[], int width, int height,
                                              boolean processAlpha) {
        ImageData data = new ImageData(width, height, false);

        // create native image data below
        try {
            createImmutableImageDecodeRGBImage(data, rgb,
                                               width, height,
                                               processAlpha);
        } catch(OutOfMemoryError e) {
            garbageCollectImages(false);

            try {
                createImmutableImageDecodeRGBImage(data, rgb,
                                                   width, height,
                                                   processAlpha);
            } catch(OutOfMemoryError e2) {
                garbageCollectImages(true);

                createImmutableImageDecodeRGBImage(data, rgb,
                                                   width, height,
                                                   processAlpha);
            }
        }

        return data;
    }

    /**
     * Create a immutable image from romized image data.
     *
     * @param imageDataArrayPtr native pointer to image data as Java int
     * @param imageDataArrayLength length of image data array
     * @return the created image data
     * @throws IllegalArgumentException if the id is invalid
     */
    public ImageData createImmutableImageData(int imageDataArrayPtr,
            int imageDataArrayLength) {

        ImageData data = new ImageData();

        // width, height and native image data will be set below
        if (!loadRomizedImage(data, imageDataArrayPtr,
                    imageDataArrayLength)) {
            throw new IllegalArgumentException();
        }

        return data;
    }
    
    /** 
     * Load and create image data from cache. The real work is done in 
     * the native function. 
     * 
     * @param   data The ImageData object 
     * @param   resName  Image resource name 
     * @return  true if image was loaded and created, false otherwise 
     */ 
    private boolean loadAndCreateImmutableImageDataFromCache(ImageData data, 
                                                             String resName) { 
        try { 
            return imageCache.loadAndCreateImmutableImageData(data, resName);
        } catch(OutOfMemoryError e) { 
            garbageCollectImages(false); 
 
            try { 
                return imageCache.loadAndCreateImmutableImageData(data,
                                                                 resName);
            } catch(OutOfMemoryError e2) { 
                garbageCollectImages(true); 
 
                return imageCache.loadAndCreateImmutableImageData(data,
                                                                 resName);
            } 
        } 
    } 

    /**
     * helper function called by the create functions above.
     * Upon return, the input stream will be closed.
     *
     * @param data  the ImageData object
     * @param istream the name of the input stream containing image
     *                data in a supported format
     * @throws IOException if there is an error with the stream
     */
    private void getImageDataFromStream(ImageData data, InputStream istream)
        throws java.io.IOException
    {
        int blocksize = 4096; // the size of blocks to read and allocate

        /*
         * Allocate an array assuming available is correct.
         * Only reading an EOF is the real end of file
         * so allocate an extra byte to read the EOF into.
         * If available is incorrect, increase the buffer
         * size and keep reading.
         */
        int l = istream.available();
        byte[] buffer = new byte[l+1];
        int length = 0;

        // TBD: Guard against an implementation with incorrect available
        while ((l = istream.read(buffer, length,
                                 buffer.length-length)) != -1) {
            length += l;
            if (length == buffer.length) {
                byte[] b = new byte[buffer.length + blocksize];
                System.arraycopy(buffer, 0, b, 0, length);
                buffer = b;
            }
        }

        try {
            createImmutableImageDecodeImage(data, buffer, 0, length);
        } catch (IllegalArgumentException iae) {
            // Data cannot be not decoded
            throw new java.io.IOException();
        } finally {
            istream.close();
        }
    }

    /**
     * Create a mutable image data
     *
     * @param data The ImageData object
     * @param width The width of the new mutable image
     * @param height The height of the new mutable image
     */
    private native void createMutableImageData(ImageData data,
                                               int width, int height);

    /**
     * Native function to create an immutable copy of an image data.
     *
     * @param dest  The ImageData where to make a copy
     * @param source The Image to make a copy of, either mutable or immutable.
     */
    private native void createImmutableImageDataCopy(ImageData dest,
                                                     ImageData source);


    /**
     * Native function that creates an immutable image data from
     * a region of another image data, applying the given transform
     *
     * @param dataDest The ImageData to make a copy to
     * @param dataSource The ImageData to make a copy of
     * @param x The horizontal offset of the top left of the region to copy
     * @param y The vertical offset of the top left of the region to copy
     * @param width The width of the new Image
     * @param height The height of the new Image
     * @param transform The transformation to apply
     * @param isMutable True if the Image is mutable, false otherwise
     *
     */
    private native void createImmutableImageDataRegion(ImageData dataDest,
                                                       ImageData dataSource,
                                                       int x, int y,
                                                       int width, int height,
                                                       int transform,
                                                       boolean isMutable);


    /**
     * Native function to decode an ImageData from a byte array
     *
     * @param data The ImageData object
     * @param inputData The byte array image data
     * @param offset The start of the image data within the byte array
     * @param length The length of the image data in the byte array
     * @throws IllegalArgumentException if the data cannot be decoded
     */
    private native void createImmutableImageDecodeImage(ImageData data,
                                                        byte[] inputData,
                                                        int offset,
                                                        int length);

    /**
     * Native function to load an ImageData directly out of the rom image.
     *
     * @param data the ImageData object
     * @param imageDataArrayPtr native pointer to image data as Java int
     * @param imageDataArrayPtrLength length of image data array
     * @return true if the imaged data loading was successful,
     *         otherwise it returns false
     */
    private native boolean loadRomizedImage(ImageData data,
            int imageDataArrayPtr, int imageDataArrayPtrLength);

    /**
     * Native function to decode an ImageData from an array of RGB data
     *
     * @param data the ImageData object
     * @param inputData an array of ARGB values that composes
     *                  the image.
     * @param width the width of the image
     * @param height the height of the image
     * @param processAlpha true if rgb has an alpha channel,
     *                     false if all pixels are fully opaque
     */
    private native void createImmutableImageDecodeRGBImage(ImageData data,
                                                           int[] inputData,
                                                           int width,
                                                           int height,
                                                   boolean processAlpha);
    /**
     * Garbage collected to free native resources from zombie images.
     *
     * @param doFullGC boolean indicating whether to do a full GC or only
     *                 request young generation GC.
     */
    private static native void garbageCollectImages(boolean doFullGC);
}
