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

import com.sun.midp.chameleon.skins.resources.*;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * ImageItemSkin represents the properties and values used to render
 * a ImageItem in the javax.microedition.lcdui package.
 */
public class ImageItemSkin {

    /**
     * This field corresponds to IMAGEITEM_COLOR_BG_LNK_FOC skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG_LINK_FOCUS;

    /**
     * This field corresponds to IMAGEITEM_COLOR_BG_BTN skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG_BUTTON;

    /**
     * This field corresponds to IMAGEITEM_COLOR_BORDER_LT skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_LT;

    /**
     * This field corresponds to IMAGEITEM_COLOR_BORDER_DK skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_DK;

    /**
     * This field corresponds to IMAGEITEM_PAD_LNK_H skin property.
     * See its comment for further details.
     */
    public static int PAD_LINK_H;   

    /**
     * This field corresponds to IMAGEITEM_PAD_LNK_H skin property.
     * See its comment for further details.
     */
    public static int PAD_LINK_V;   

    /**
     * This field corresponds to IMAGEITEM_PAD_BTN_H skin property.
     * See its comment for further details.
     */
    public static int PAD_BUTTON_H;

    /**
     * This field corresponds to IMAGEITEM_PAD_BTN_V skin property.
     * See its comment for further details.
     */
    public static int PAD_BUTTON_V;

    /**
     * This field corresponds to IMAGEITEM_BTN_BORDER_W skin property.
     * See its comment for further details.
     */
    public static int BUTTON_BORDER_W;

    /**
     * This field corresponds to IMAGEITEM_IMAGE_LNK_H skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_LINK_H;

    /**
     * This field corresponds to IMAGEITEM_IMAGE_LNK_V skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_LINK_V;

    /**
     * This field corresponds to IMAGEITEM_IMAGE_BUTTON skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color and line border should be used.
     */
    public static Image[] IMAGE_BUTTON;

    // private constructor
    private ImageItemSkin() {
    }
    
}

