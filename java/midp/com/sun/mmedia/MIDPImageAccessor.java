/*
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
package com.sun.mmedia;

import javax.microedition.lcdui.Image;

/**
 * The image access class for MIDP.
 *
 * @created    December 16, 2005
 */
class MIDPImageAccessor implements ImageAccess {
    
    /*
     * ImageAccess I/F method
     */
    public int alphaLevelsNumber() {
        /** 
         * TBD: get display for current MIDlet
         * javax.microedition.lcdui.Display d = ...; 
         * return d.numAlphaLevels();
         */
        return 2;
    }
    
    /*
     * ImageAccess I/F method
     */
    public boolean isImage(Object image) {
        return ((null != image) && (image instanceof Image));
    }
    
    /*
     * ImageAccess I/F method
     */
    public boolean isMutableImage(Object image){
        if (!isImage(image)) return false;

        Image img = (Image)image;
        return img.isMutable();
    }
    
    /*
     * ImageAccess I/F method
     */
    public int getImageWidth(Object image) {
        if (!isImage(image)) return -1;
        
        Image img = (Image)image;
        return img.getWidth();
    }
    
    /*
     * ImageAccess I/F method
     */
    public int getImageHeight(Object image) {
        if (!isImage(image)) return -1;
        
        Image img = (Image)image;
        return img.getHeight();
    }
    
    /*
     * ImageAccess I/F method
     */
    public byte[] getRGBByteImageData(Object image) {
        if (!isImage(image)) return null;

        return FormatConversionUtils.
                intArrayToByteArray(getRGBIntImageData(image));
    }
    
    /*
     * ImageAccess I/F method
     */
    public int[] getRGBIntImageData(Object image) {
        if (!isImage(image)) return null;
        
        Image img = (Image)image;
        
        int w = img.getWidth();
        int h = img.getHeight();
        int[] data = new int[w * h]; 
        
        img.getRGB(data, 0, w, 0, 0, w, h);
        return data;
    }
    
    /*
     * ImageAccess I/F method
     */
    public Object imageCreateFromImage(Object image) {
        if (!isImage(image)) return null;
        
        Image img = (Image)image;
        
        return Image.createImage(img);
    }

    /*
     * ImageAccess I/F method
     */
    public Object imageCreateFromStream(java.io.InputStream stream) {
        if (stream == null)
            return null;
        
        Image image = null;
        try {
            image = Image.createImage(stream);
        } catch (java.io.IOException ioe) {
            return null;
        }
        return image;
    }
    
    /*
     * ImageAccess I/F method
     */
    public Object imageCreateFromByteArray(byte[] data, int offset, int length) {
        return Image.createImage(data, offset, length);
    }
}
