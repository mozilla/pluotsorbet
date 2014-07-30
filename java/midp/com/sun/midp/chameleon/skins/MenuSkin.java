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
import javax.microedition.lcdui.Graphics;

/**
 * A skin containing images and parameters related to the popup system menu.
 */
public class MenuSkin {

    /**
     * This field corresponds to MENU_WIDTH skin property.
     * See its comment for further details.
     */
    public static int WIDTH;

    /**
     * This field corresponds to MENU_HEIGHT skin property.
     * See its comment for further details.
     */
    public static int HEIGHT;

    /**
     * This field corresponds to MENU_ALIGN_X skin property.
     * See its comment for further details.
     */
    public static int ALIGN_X;

    /**
     * This field corresponds to MENU_ALIGN_Y skin property.
     * See its comment for further details.
     */
    public static int ALIGN_Y;

    /**
     * This field corresponds to MENU_TITLE_X skin property.
     * See its comment for further details.
     */
    public static int TITLE_X;

    /**
     * This field corresponds to MENU_TITLE_Y skin property.
     * See its comment for further details.
     */
    public static int TITLE_Y;

    /**
     * This field corresponds to MENU_TITLE_MAXWIDTH skin property.
     * See its comment for further details.
     */
    public static int TITLE_MAXWIDTH;

    /**
     * This field corresponds to MENU_TITLE_ALIGN skin property.
     * See its comment for further details.
     */
    public static int TITLE_ALIGN;

    /**
     * This field corresponds to MENU_MAX_ITEMS skin property.
     * See its comment for further details.
     */
    public static int MAX_ITEMS; 

    /**
     * This field corresponds to MENU_ITEM_HEIGHT skin property.
     * See its comment for further details.
     */
    public static int ITEM_HEIGHT;

    /**
     * This field corresponds to MENU_ITEM_TOPOFFSET skin property.
     * See its comment for further details.
     */
    public static int ITEM_TOPOFFSET;

    /**
     * This field corresponds to MENU_ITEM_INDEX_ANCHOR_X skin property.
     * See its comment for further details.
     */
    public static int ITEM_INDEX_ANCHOR_X;

    /**
     * This field corresponds to MENU_ITEM_ANCHOR_X skin property.
     * See its comment for further details.
     */
    public static int ITEM_ANCHOR_X;

    /**
     * This field corresponds to MENU_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;

    /**
     * This field corresponds to MENU_COLOR_BG_SEL skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG_SEL;

    /**
     * This field corresponds to MENU_COLOR_TITLE skin property.
     * See its comment for further details.
     */
    public static int COLOR_TITLE;    

    /**
     * This field corresponds to MENU_COLOR_INDEX skin property.
     * See its comment for further details.
     */
    public static int COLOR_INDEX;

    /**
     * This field corresponds to MENU_COLOR_INDEX_SEL skin property.
     * See its comment for further details.
     */
    public static int COLOR_INDEX_SEL;

    /**
     * This field corresponds to MENU_COLOR_ITEM skin property.
     * See its comment for further details.
     */
    public static int COLOR_ITEM;

    /**
     * This field corresponds to MENU_COLOR_ITEM_SEL skin property.
     * See its comment for further details.
     */
    public static int COLOR_ITEM_SEL;

    /**
     * This field corresponds to MENU_TEXT_TITLE skin property.
     * See its comment for further details.
     *
     * A 'null' value indicates this system menu does not have a title.
     */
    public static String TEXT_TITLE;

    /**
     * This field corresponds to MENU_FONT_TITLE skin property.
     * See its comment for further details.
     */
    public static Font FONT_TITLE;

    /**
     * This field corresponds to MENU_FONT_ITEM skin property.
     * See its comment for further details.
     */
    public static Font FONT_ITEM;

    /**
     * This field corresponds to MENU_FONT_ITEM_SEL skin property.
     * See its comment for further details.
     */
    public static Font FONT_ITEM_SEL;

    /**
     * This field corresponds to MENU_IMAGE_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BG;

    /**
     * This field corresponds to MENU_IMAGE_ITEM_SEL_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_ITEM_SEL_BG;

    /**
     * This field corresponds to MENU_IMAGE_SUBMENU skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_SUBMENU_ARROW;

    /**
     * This field corresponds to MENU_IMAGE_SUBMENU_HL skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_SUBMENU_ARROW_HL;
    
    /**
     * Private constructor
     */
    private MenuSkin() {
    }
}

