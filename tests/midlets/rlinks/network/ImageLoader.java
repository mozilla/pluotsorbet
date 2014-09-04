/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.network;

import com.nokia.example.rlinks.network.operation.ImageLoadOperation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.lcdui.Image;

/**
 * Utility for loading image resources.
 */
public final class ImageLoader {

    private static ImageLoader self;

    /**
     * An interface for notifying that an image has been loaded.
     */
    public interface Listener {

        void imageLoaded(Image image);
    }

    private ImageLoader() {}

    /**
     * Returns an ImageLoader instance.
     *
     * @return ImageLoader Singleton
     */
    public static ImageLoader getInstance() {
        if (self == null) {
            self = new ImageLoader();
        }
        return self;
    }

    /**
     * Loads an image from resources and returns it.
     * 
     * Caches all loaded images in hopes of saving some memory.
     *
     * @param imagePath
     * @return loaded image
     * @throws IOException
     */
    public final Image loadImage(final String imagePath, final Hashtable cache)
        throws IOException {
        Image image = null;
        if (cache != null) {
            image = (Image) cache.get(imagePath);
        }
        if (image == null) {
            InputStream in = this.getClass().getResourceAsStream(imagePath);
            if (in == null) {
                throw new IOException("Image not found.");
            }
            image = Image.createImage(in);
            if (cache != null) {
                cache.put(imagePath, image);
            }
        }
        return image;
    }

    /**
     * Load an image from resources or network.
     *
     * Caches all loaded images.
     *
     * @param url URL to load image from
     * @param defaultImage A default image which is returned while the image is
     * loaded from network
     * @param listener Listener which is notified when the image is loaded from
     * network
     * @return The image or while the image is loaded from the network the
     * default image
     */
    public final Image loadImage(final String url, final Image defaultImage,
        final Listener listener, final Hashtable cache) {
        try {
            return loadImage(url, cache);
        }
        catch (IOException e) {
            if (cache != null) {
                cache.put(url, defaultImage);
            }
            
            new ImageLoadOperation(url, new ImageLoadOperation.Listener() {
                public void imageReceived(String url, byte[] data) {
                    Image image = defaultImage;
                    try {
                        image = Image.createImage(data, 0, data.length);
                    }
                    catch (IllegalArgumentException e) {}
                    catch (NullPointerException e) {}

                    if (cache != null) {
                        cache.put(url, image);
                    }
                    listener.imageLoaded(image);
                }
            }).start();
        }
        return defaultImage;
    }
}
