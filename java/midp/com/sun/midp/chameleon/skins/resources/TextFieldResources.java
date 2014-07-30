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
import com.sun.midp.chameleon.skins.TextFieldSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class TextFieldResources {
    private static boolean init;
    
    // private constructor
    private TextFieldResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        TextFieldSkin.PAD_H = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_PAD_H);
        TextFieldSkin.PAD_V = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_PAD_V);
        TextFieldSkin.BOX_MARGIN = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_BOX_MARGIN);
        TextFieldSkin.WIDTH_CARET = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_WIDTH_CARET);
        TextFieldSkin.SCROLL_RATE = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_SCRL_RATE);
        TextFieldSkin.SCROLL_SPEED = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_SCRL_SPD);
        TextFieldSkin.COLOR_FG = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_COLOR_FG);
        TextFieldSkin.COLOR_BG = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_COLOR_BG);
        TextFieldSkin.COLOR_BORDER = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_COLOR_BRDR);
        TextFieldSkin.COLOR_BORDER_SHD = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_COLOR_BRDR_SHD);     
        TextFieldSkin.COLOR_FG_UE = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_COLOR_FG_UE);
        TextFieldSkin.COLOR_BG_UE = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_COLOR_BG_UE);
        TextFieldSkin.COLOR_BORDER_UE = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_COLOR_BRDR_UE);
        TextFieldSkin.COLOR_BORDER_SHD_UE = SkinLoader.getInt(
                SkinPropertiesIDs.TEXTFIELD_COLOR_BRDR_SHD_UE);
        /*
        Uncomment if background image for (un)editable text component is used
        TextFieldSkin.IMAGE_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.TEXTFIELD_IMAGE_BG, 9);
        TextFieldSkin.IMAGE_BG_UE = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.TEXTFIELD_IMAGE_BG_UE, 9);
        */

        init = true;
    }

}
