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
import com.sun.midp.chameleon.skins.DateFieldSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class DateFieldResources {
    private static boolean init;
    
    // private constructor
    private DateFieldResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }

        DateFieldSkin.PAD_H = SkinLoader.getInt(
                SkinPropertiesIDs.DATEFIELD_PAD_H);
        DateFieldSkin.PAD_V = SkinLoader.getInt(
                SkinPropertiesIDs.DATEFIELD_PAD_V);
        DateFieldSkin.BUTTON_BORDER_W = SkinLoader.getInt(
               SkinPropertiesIDs.DATEFIELD_BTN_BRDR_W); 
        DateFieldSkin.FONT = SkinLoader.getFont(
                SkinPropertiesIDs.DATEFIELD_FONT);
        DateFieldSkin.COLOR_FG = SkinLoader.getInt(
                SkinPropertiesIDs.DATEFIELD_COLOR_FG);
        DateFieldSkin.COLOR_BG = SkinLoader.getInt(
                SkinPropertiesIDs.DATEFIELD_COLOR_BG);
        DateFieldSkin.COLOR_BORDER = SkinLoader.getInt(
                SkinPropertiesIDs.DATEFIELD_COLOR_BRDR);
        DateFieldSkin.COLOR_BORDER_LT = SkinLoader.getInt(
                SkinPropertiesIDs.DATEFIELD_COLOR_BRDR_LT);
        DateFieldSkin.COLOR_BORDER_DK = SkinLoader.getInt(
                SkinPropertiesIDs.DATEFIELD_COLOR_BRDR_DK);
        DateFieldSkin.COLOR_BORDER_SHD = SkinLoader.getInt(
                SkinPropertiesIDs.DATEFIELD_COLOR_BRDR_SHD);
        /*
        Uncomment if background image is used
        DateFieldSkin.IMAGE_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.DATEFIELD_IMAGE_BG, 9);
        */
        DateFieldSkin.IMAGE_BUTTON_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.DATEFIELD_IMAGE_BTN_BG, 9);
        DateFieldSkin.IMAGE_ICON_DATE = SkinLoader.getImage(
                SkinPropertiesIDs.DATEFIELD_IMAGE_ICON_DATE);
        DateFieldSkin.IMAGE_ICON_TIME = SkinLoader.getImage(
                SkinPropertiesIDs.DATEFIELD_IMAGE_ICON_TIME);
        DateFieldSkin.IMAGE_ICON_DATETIME = SkinLoader.getImage(
                SkinPropertiesIDs.DATEFIELD_IMAGE_ICON_DATETIME);

        init = true;
    }
}

