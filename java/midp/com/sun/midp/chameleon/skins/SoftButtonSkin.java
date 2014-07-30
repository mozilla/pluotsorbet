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
 * A skin containing images and parameters related to the softbutton bar.
 */
public class SoftButtonSkin {

    /**
     * This field corresponds to SOFTBTN_HEIGHT skin property.
     * See its comment for further details.
     */
    public static int HEIGHT;
    
    /**
     * This field corresponds to SOFTBTN_NUM_BUTTONS skin property.
     * See its comment for further details.
     */
    public static int NUM_BUTTONS;
    
    /**
     * This field corresponds to SOFTBTN_BUTTON_ANCHOR_X skin property.
     * See its comment for further details. Its an array of size equal 
     * to NUM_BUTTONS.
     */
    public static int[] BUTTON_ANCHOR_X;
    
    /**
     * This field corresponds to SOFTBTN_BUTTON_ANCHOR_Y skin property.
     * See its comment for further details. Its an array of size equal 
     * to NUM_BUTTONS.
     */
    public static int[] BUTTON_ANCHOR_Y;
    
    /**
     * This field corresponds to SOFTBTN_BUTTON_ALIGN_X skin property.
     * See its comment for further details. Its an array of size equal 
     * to NUM_BUTTONS.
     */
    public static int[] BUTTON_ALIGN_X;
    
    /**
     * This field corresponds to SOFTBTN_BUTTON_MAX_WIDTH skin property.
     * See its comment for further details. Its an array of size equal 
     * to NUM_BUTTONS.
     */
    public static int[] BUTTON_MAX_WIDTH;
    
    /**
     * This field corresponds to SOFTBTN_BUTTON_SHD_ALIGN skin property.
     * See its comment for further details.
     */
    public static int BUTTON_SHD_ALIGN;
    
    /**
     * This field corresponds to SOFTBTN_COLOR_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG;
    
    /**
     * This field corresponds to SOFTBTN_COLOR_FG_SHD skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG_SHD;
    
    /**
     * This field corresponds to SOFTBTN_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;
    
    /**
     * This field corresponds to SOFTBTN_COLOR_MU_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_MU_FG;
    
    /**
     * This field corresponds to SOFTBTN_COLOR_MU_FG_SHD skin property.
     * See its comment for further details.
     */
    public static int COLOR_MU_FG_SHD;
    
    /**
     * This field corresponds to SOFTBTN_COLOR_MU_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_MU_BG;
    
    /**
     * This field corresponds to SOFTBTN_COLOR_AU_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_AU_FG;
    
    /**
     * This field corresponds to SOFTBTN_COLOR_AU_FG_SHD skin property.
     * See its comment for further details.
     */
    public static int COLOR_AU_FG_SHD;
    
    /**
     * This field corresponds to SOFTBTN_COLOR_AU_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_AU_BG;
    
    /**
     * This field corresponds to SOFTBTN_FONT skin property.
     * See its comment for further details.
     */
    public static Font FONT;
    
    /**
     * This field corresponds to SOFTBTN_IMAGE_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BG;
    
    /**
     * This field corresponds to SOFTBTN_IMAGE_MU_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_MU_BG;
    
    /**
     * This field corresponds to SOFTBTN_IMAGE_AU_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_AU_BG;
    
    /**
     * This field corresponds to SOFTBTN_TEXT_MENUCMD skin property.
     * See its comment for further details.
     */
    public static String TEXT_MENUCMD;
    
    /**
     * This field corresponds to SOFTBTN_TEXT_BACKCMD skin property.
     * See its comment for further details.
     */
    public static String TEXT_BACKCMD;

    /**
     * Private constructor
     */
    private SoftButtonSkin() {
    }
}

