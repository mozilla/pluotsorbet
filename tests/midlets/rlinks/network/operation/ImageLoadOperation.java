/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.network.operation;

import com.nokia.example.rlinks.network.HttpOperation;

/**
 * An HttpOperation for loading an image.
 */
public final class ImageLoadOperation
    extends HttpOperation {

    /**
     * Listener interface used to signal of images being received.
     */
    public interface Listener {

        /**
         * Called when a image has been loaded.
         *
         * @param url Url address identifying the image
         * @param data Image data
         */
        public void imageReceived(String url, byte[] data);
    }

    private final String url;
    private final Listener listener;

    /**
     * Create a ImageLoadOperation.
     *
     * @param url URL identifying the image to be loaded
     * @param listener Listener to be invoked when loading is done
     */
    public ImageLoadOperation(String url, Listener listener) {
        this.url = url;
        this.listener = listener;
    }

    /**
     * @see NetworkOperation#getUrl() 
     */
    public final String getUrl() {
        return url;
    }

    /**
     * Overridden to signal we're not interested in possible cookies
     * received when transferring images.
     */
    public boolean isCookiesEnabled() {
        return false;
    }

    /**
     * Calls the listener, no parsing needed here.
     */
    public final void responseReceived(byte[] data) {
        finished = true;
        listener.imageReceived(url, data);
    }

    public String toString() {
        return "LoadImageOperation(url=" + url + ")";
    }
}
