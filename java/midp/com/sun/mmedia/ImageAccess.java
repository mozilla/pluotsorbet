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

/**
 *  Provides methods to set parameters of platform specific images.
 *  Since Image class is defined differently on different platforms 
 *  (midp, j2se, etc), core components can refer to images only as instances
 *  of class Object. 
 *  This interface can help in checking if a given Object is an image and to 
 *  retrieve image parameters.
 *
 * @created    November 8, 2005
 */
public interface ImageAccess {
    
    /*
     * returns how many alpha levels supported by the system. 
     * Min value is 2: totally opaque & totally transparent.
     */
    int alphaLevelsNumber();
    
    /* 
     * Checks if the object is Image or not.
     */
    boolean isImage(Object image);
    
    /* 
     * Checks if the object is mutable or immutable Image.
     * Shall be called only after isImage() check passed. 
     */
    boolean isMutableImage(Object image);
    
    /* 
     * Returns Image width, or -1 if Object is not Image.
     */
    int getImageWidth(Object image);
    
    /* 
     * Returns Image height, or -1 if Object is not Image.
     */
    int getImageHeight(Object image);
    
    /* 
     * Returns RGB image data (4 bytes in ARGB format per pixel) 
       or null if Object is not Image.
     */
    byte[] getRGBByteImageData(Object image);
    
    /* 
     * Returns RGB image data (32-bit int per pixel) 
       or null if Object is not Image.
     */
    int[] getRGBIntImageData(Object image);
    
    /* 
     * Returns an immutable copy of a given Image or null if Object is not Image.
     */
    Object imageCreateFromImage(Object image);
    
    /* 
     * Returns an image created from the stream or null in case of failure.
     */
    Object imageCreateFromStream(java.io.InputStream stream);
    
    /* 
     * Returns an image created from the byte array or null in case of filure.
     */
    Object imageCreateFromByteArray(byte[] data, int offset, int length);
}

