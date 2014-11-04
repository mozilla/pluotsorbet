/*
 *  Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License version
 *  2 only, as published by the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License version 2 for more details (a copy is
 *  included at /legal/license.txt).
 *  
 *  You should have received a copy of the GNU General Public License
 *  version 2 along with this work; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *  
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 *  Clara, CA 95054 or visit www.sun.com if you need additional
 *  information or have any questions.
 */
package com.sun.mmedia;

import javax.microedition.media.Control;

/**
 *  Description of the Class
 */
public abstract class VideoRenderer {
    /**
     *  Description of the Field
     */
    public final static int RGB565 = 1; // short []
    /**
     *  Description of the Field
     */
    public final static int RGB888 = 2; // byte []
    /**
     *  Description of the Field
     */
    public final static int XRGB888 = 3; // int []
    /**
     *  Description of the Field
     */
    public final static int XBGR888 = 4; // int []
    /**
     *  Description of the Field
     */
    public final static int RGBX888 = 5; // int []
    /**
     *  Description of the Field
     */
    public final static int YUV420_PLANAR = 6; // byte []
    /**
     *  Description of the Field
     */
    public final static int YUV422_PLANAR = 7; // byte []
    /**
     *  Description of the Field
     */
    public final static int YUYV = 8; // byte []
    /**
     *  Description of the Field
     */
    public final static int UYVY = 9; // byte []
    /**
     *  Description of the Field
     */
    public final static int YVYU = 10; // byte []
    /**
     *  Description of the Field
     */
    public final static int NATIVE_RENDER = 128; // to be ORed with above
    /**
     *  Description of the Field
     */
    public final static int USE_ALPHA = 256;
    
    public abstract void initRendering(int colorMode, int width, int height);    
    public abstract void render(byte[] colorData);
    public abstract void render(short[] colorData);
    public abstract void render(int[] colorData);
    public abstract void close();
    
    public abstract Control getVideoControl();
}

