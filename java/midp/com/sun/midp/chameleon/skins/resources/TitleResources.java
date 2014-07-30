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
import com.sun.midp.chameleon.skins.TitleSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class TitleResources {
    private static boolean init;
    
    private TitleResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        TitleSkin.HEIGHT = SkinLoader.getInt(
                SkinPropertiesIDs.TITLE_HEIGHT);
        TitleSkin.MARGIN = SkinLoader.getInt(
                SkinPropertiesIDs.TITLE_MARGIN);

        int alignX = SkinLoader.getInt(
                SkinPropertiesIDs.TITLE_TEXT_ALIGN_X);
        TitleSkin.TEXT_ALIGN_X = SkinLoader.resourceConstantsToGraphics(
                alignX);

        int shdAlign = SkinLoader.getInt(
                SkinPropertiesIDs.TITLE_TEXT_SHD_ALIGN);
        TitleSkin.TEXT_SHD_ALIGN = SkinLoader.resourceConstantsToGraphics(
                shdAlign);

        TitleSkin.COLOR_FG = SkinLoader.getInt(
                SkinPropertiesIDs.TITLE_COLOR_FG);
        TitleSkin.COLOR_FG_SHD = SkinLoader.getInt(
                SkinPropertiesIDs.TITLE_COLOR_FG_SHD);
        TitleSkin.COLOR_BG = SkinLoader.getInt(
                SkinPropertiesIDs.TITLE_COLOR_BG);
        TitleSkin.FONT = SkinLoader.getFont(
                SkinPropertiesIDs.TITLE_FONT);
        TitleSkin.IMAGE_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.TITLE_IMAGE_BG, 3);

        init = true;
    }
    
}
