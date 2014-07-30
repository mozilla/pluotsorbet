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

import com.sun.midp.configurator.Constants;

/**
 * A skin containing values for standard "screen" type stuff,
 * such as width and height, standard background, etc.
 */
public class    ScreenSkin {
    
    /**
     * This field corresponds to SCREEN_TEXT_ORIENT skin property.
     * See its comment for further details.
     */
    public static int TEXT_ORIENT;   
    
    /**
     * This field corresponds to SCREEN_PAD_FORM_ITEMS skin property.
     * See its comment for further details.
     */
    public static int PAD_FORM_ITEMS;
    
    /**
     * This field corresponds to SCREEN_PAD_LABEL_VERT skin property.
     * See its comment for further details.
     */
    public static int PAD_LABEL_VERT;
    
    /**
     * This field corresponds to SCREEN_PAD_LABEL_HORIZ skin property.
     * See its comment for further details.
     */
    public static int PAD_LABEL_HORIZ;
    
    /**
     * This field corresponds to SCREEN_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;
    
    /**
     * This field corresponds to SCREEN_COLOR_HS_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_HS_BG;
    
    /**
     * This field corresponds to SCREEN_COLOR_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG;
    
    /**
     * This field corresponds to SCREEN_COLOR_BG_HL skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG_HL;
    
    /**
     * This field corresponds to SCREEN_COLOR_FG_HL skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG_HL;
    
    /**
     * This field corresponds to SCREEN_COLOR_BORDER skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER;
    
    /**
     * This field corresponds to SCREEN_COLOR_BORDER_HL skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_HL;
    
    /**
     * This field corresponds to SCREEN_COLOR_TRAVERSE_IND skin property.
     * See its comment for further details.
     */
    public static int COLOR_TRAVERSE_IND;
    
    /**
     * This field corresponds to SCREEN_BORDER_STYLE skin property.
     * See its comment for further details.
     */
    public static int BORDER_STYLE;
    
    /**
     * This field corresponds to SCREEN_SCROLL_AMOUNT skin property.
     * See its comment for further details.
     */
    public static int SCROLL_AMOUNT;

    /**
     * This field corresponds to SCREEN_FONT_LABEL skin property.
     * See its comment for further details.
     */
    public static Font FONT_LABEL;
    
    /**
     * This field corresponds to SCREEN_FONT_INPUT_TEXT skin property.
     * See its comment for further details.
     */
    public static Font FONT_INPUT_TEXT;
    
    /**
     * This field corresponds to SCREEN_FONT_STATIC_TEXT skin property.
     * See its comment for further details.
     */
    public static Font FONT_STATIC_TEXT;
    
    /**
     * This field corresponds to SCREEN_IMAGE_WASH skin property.
     * See its comment for further details. It may be null if 
     * image hasn't been specified.
     */
    public static Image IMAGE_WASH;
    
    /**
     * This field corresponds to SCREEN_IMAGE_BG skin property.
     * See its comment for further details. It may be null if 
     * image hasn't been specified.
     */
    public static Image IMAGE_BG;
    
    /**
     * This field corresponds to SCREEN_IMAGE_BG_W_TITLE skin property.
     * See its comment for further details. A 'null' value for this 
     * array means there is no image background and a solid fill color 
     * should be used.
     */
    public static Image[] IMAGE_BG_W_TITLE;
    
    /**
     * This field corresponds to SCREEN_IMAGE_BG_WO_TITLE skin property.
     * See its comment for further details. A 'null' value for this 
     * array means there is no image background and a solid fill color 
     * should be used.
     */
    public static Image[] IMAGE_BG_WO_TITLE;
    
    /**
     * The Image to use as a tile for the "home" screen background.
     * This image could be either fullsize, or if smaller than fullsize,
     * it will be tiled both horizontally and vertically to fill the
     * entire screen background.
     */
    public static Image IMAGE_HS_BG_TILE;

    /**
     * This field corresponds to SCREEN_IMAGE_HS_BG_W_TITLE skin property.
     * See its comment for further details. A 'null' value for this 
     * array means there is no image background and a solid fill color 
     * should be used.
     */
    public static Image[] IMAGE_HS_BG_W_TITLE;
    
    /**
     * This field corresponds to SCREEN_IMAGE_HS_BG_WO_TITLE skin property.
     * See its comment for further details. A 'null' value for this 
     * array means there is no image background and a solid fill color 
     * should be used.
     */
    public static Image[] IMAGE_HS_BG_WO_TITLE;

    /**
     * Layout diraction. True if current diraction is right-to-left
     */
    public static boolean RL_DIRECTION;

    /**
     * Radius of finger if finger support is on
     */
    public static int TOUCH_RADIUS;
    

    // private constructor
    private ScreenSkin() {
    }

}

