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
import com.sun.midp.chameleon.skins.StringItemSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class StringItemResources {
    private static boolean init;
    
    // private constructor
    private StringItemResources() {
    }   
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        StringItemSkin.PAD_BUTTON_H = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_PAD_BUTTON_H);
        StringItemSkin.PAD_BUTTON_V = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_PAD_BUTTON_V);
        StringItemSkin.BUTTON_BORDER_W = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_BUTTON_BORDER_W);
        StringItemSkin.COLOR_FG_LINK = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_COLOR_FG_LNK);
        StringItemSkin.COLOR_FG_LINK_FOCUS = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_COLOR_FG_LNK_FOC);
        StringItemSkin.COLOR_BG_LINK_FOCUS = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_COLOR_BG_LNK_FOC);
        StringItemSkin.COLOR_FG_BUTTON = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_COLOR_FG_BTN);
        StringItemSkin.COLOR_BG_BUTTON = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_COLOR_BG_BTN);
        StringItemSkin.COLOR_BORDER_LT = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_COLOR_BORDER_LT);
        StringItemSkin.COLOR_BORDER_DK = SkinLoader.getInt(
                SkinPropertiesIDs.STRINGITEM_COLOR_BORDER_DK);
        StringItemSkin.FONT = SkinLoader.getFont(
                SkinPropertiesIDs.STRINGITEM_FONT);
        StringItemSkin.FONT_LINK = SkinLoader.getFont(
                SkinPropertiesIDs.STRINGITEM_FONT_LNK);
        StringItemSkin.FONT_BUTTON = SkinLoader.getFont(
                SkinPropertiesIDs.STRINGITEM_FONT_BTN);
        StringItemSkin.IMAGE_LINK = SkinLoader.getImage(
                SkinPropertiesIDs.STRINGITEM_IMAGE_LNK);
        StringItemSkin.IMAGE_BUTTON = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.STRINGITEM_IMAGE_BTN, 9);

        init = true;
    }
}

