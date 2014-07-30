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
package com.sun.midp.chameleon.skins;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

/**
 * A skin containing images and parameters related to the Input Mode indicator.
 */
public class InputModeSkin {
    
    /**
     * This field corresponds to INPUT_MODE_MARGIN skin property.
     * See its comment for further details.
     */
    public static int MARGIN;
    
    /**
     * This field corresponds to INPUT_MODE_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;
    
    /**
     * This field corresponds to INPUT_MODE_COLOR_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG;
    
    /**
     * This field corresponds to INPUT_MODE_COLOR_BDR skin property.
     * See its comment for further details.
     */
    public static int COLOR_BDR;

    /**
     * This field corresponds to INPUT_MODE_FONT skin property.
     * See its comment for further details.
     */
    public static Font FONT;
    
    /**
     * This field corresponds to INPUT_MODE_IMAGE_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BG;
    

    /** private constructor */
    private InputModeSkin() {
    }
}

