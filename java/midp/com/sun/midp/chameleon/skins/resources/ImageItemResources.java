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

package com.sun.midp.chameleon.skins.resources;

import com.sun.midp.chameleon.skins.SkinPropertiesIDs;
import com.sun.midp.chameleon.skins.ImageItemSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class ImageItemResources {
    private static boolean init;
    
    // private constructor   
    private ImageItemResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        ImageItemSkin.COLOR_BG_LINK_FOCUS = SkinLoader.getInt(
                SkinPropertiesIDs.IMAGEITEM_COLOR_BG_LNK_FOC);
        ImageItemSkin.COLOR_BG_BUTTON = SkinLoader.getInt(
                SkinPropertiesIDs.IMAGEITEM_COLOR_BG_BTN);
        ImageItemSkin.COLOR_BORDER_LT = SkinLoader.getInt(
                SkinPropertiesIDs.IMAGEITEM_COLOR_BORDER_LT);
        ImageItemSkin.COLOR_BORDER_DK = SkinLoader.getInt(
                SkinPropertiesIDs.IMAGEITEM_COLOR_BORDER_DK);
        ImageItemSkin.PAD_LINK_H = SkinLoader.getInt(
                SkinPropertiesIDs.IMAGEITEM_PAD_LNK_H);
        ImageItemSkin.PAD_LINK_V = SkinLoader.getInt(
                SkinPropertiesIDs.IMAGEITEM_PAD_LNK_V);
        ImageItemSkin.PAD_BUTTON_H = SkinLoader.getInt(
                SkinPropertiesIDs.IMAGEITEM_PAD_BTN_H);
        ImageItemSkin.PAD_BUTTON_V = SkinLoader.getInt(
                SkinPropertiesIDs.IMAGEITEM_PAD_BTN_V);
        ImageItemSkin.BUTTON_BORDER_W = SkinLoader.getInt(
                SkinPropertiesIDs.IMAGEITEM_BTN_BORDER_W);
        ImageItemSkin.IMAGE_LINK_H = SkinLoader.getImage(
                SkinPropertiesIDs.IMAGEITEM_IMAGE_LNK_H);
        ImageItemSkin.IMAGE_LINK_V = SkinLoader.getImage(
                SkinPropertiesIDs.IMAGEITEM_IMAGE_LNK_V);
        /*
        Uncomment if background image for button is used
        ImageItemSkin.IMAGE_BUTTON = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.IMAGEITEM_IMAGE_BUTTON, 9);
        */

        init = true;
    }
}

