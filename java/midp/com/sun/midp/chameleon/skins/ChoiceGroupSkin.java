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

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Choice;

/**
 * ChoiceGroupSkin represents the properties and values used to render
 * a ChoiceGroup in the javax.microedition.lcdui package.
 */
public class ChoiceGroupSkin {
    
    /**
     * This field corresponds to CHOICE_WIDTH_IMAGE skin property.
     * See its comment for further details.
     */
    public static int WIDTH_IMAGE;

    /**
     * This field corresponds to CHOICE_HEIGHT_IMAGE skin property.
     * See its comment for further details.
     */
    public static int HEIGHT_IMAGE;
    
    /**
     * This field corresponds to CHOICE_WIDTH_SCROLL skin property.
     * See its comment for further details.
     * IMPL NOTE: all scrolling should be handled by a ScrollLayer
     */
    public static int WIDTH_SCROLL;
    
    /**
     * This field corresponds to CHOICE_WIDTH_THUMB skin property.
     * See its comment for further details.
     * IMPL NOTE: all scrolling should be handled by a ScrollLayer
     */
    public static int WIDTH_THUMB;    

    /**
     * This field corresponds to CHOICE_HEIGHT_THUMB skin property.
     * See its comment for further details.
     * IMPL NOTE: all scrolling should be handled by a ScrollLayer
     */
    public static int HEIGHT_THUMB;
    
    /**
     * This field corresponds to CHOICE_PAD_H skin property.
     * See its comment for further details.
     */
    public static int PAD_H;
    
    /**
     * This field corresponds to CHOICE_PAD_V skin property.
     * See its comment for further details.
     */
    public static int PAD_V;

    /**
     * This field corresponds to CHOICE_COLOR_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG;
    
    /**
     * This field corresponds to CHOICE_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;
    
    /**
     * This field corresponds to CHOICE_COLOR_BRDR skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER;
    
    /**
     * This field corresponds to CHOICE_COLOR_BRDR_SHD skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_SHD;
    
    /**
     * This field corresponds to CHOICE_COLOR_SCROLL skin property.
     * See its comment for further details.
     * IMPL NOTE: all scrolling should be handled by a ScrollLayer
     */
    public static int COLOR_SCROLL;
    
    /**
     * This field corresponds to CHOICE_COLOR_THUMB skin property.
     * See its comment for further details.
     * IMPL NOTE: all scrolling should be handled by a ScrollLayer
     */
    public static int COLOR_THUMB;
    
    /**
     * This field corresponds to CHOICE_FONT skin property.
     * See its comment for further details.
     */
    public static Font FONT;
    
    /**
     * This field corresponds to CHOICE_FONT_FOCUS skin property.
     * See its comment for further details.
     */
    public static Font FONT_FOCUS;
    
    /**
     * This field corresponds to CHOICE_IMAGE_RADIO skin property.
     * See its comment for further details.
     */
    public static Image[] IMAGE_RADIO;
    
    /**
     * This field corresponds to CHOICE_IMAGE_CHKBX skin property.
     * See its comment for further details.
     */
    public static Image[] IMAGE_CHKBOX;
    
    /**
     * This field corresponds to CHOICE_IMAGE_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BG;
    
    /**
     * This field corresponds to CHOICE_IMAGE_BTN_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BUTTON_BG;
    
    /**
     * This field corresponds to CHOICE_IMAGE_BTN_ICON skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_BUTTON_ICON;
    
    /**
     * This field corresponds to CHOICE_IMAGE_POPUP_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_POPUP_BG;    
    
    // private constructor
    private ChoiceGroupSkin() {
    }

    /**
     * Returns best image width to be used in ChoiceGroup/List after
     * checking that skin resources were loaded.
     * @return best image width for ChoiceGroup/List
     */
    public static int getBestImageWidth() {
        ChoiceGroupResources.load();
        return WIDTH_IMAGE;
    }

    /**
     * Returns best image height to be used in ChoiceGroup/List after
     * checking that skin resources were loaded.
     * @return best image height for ChoiceGroup/List
     */
    public static int getBestImageHeight() {
        ChoiceGroupResources.load();
        return HEIGHT_IMAGE;
    }
}
