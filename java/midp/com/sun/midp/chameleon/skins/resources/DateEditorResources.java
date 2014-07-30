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
import com.sun.midp.chameleon.skins.DateEditorSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class DateEditorResources {
    private static boolean init;
    
    // private constructor
    private DateEditorResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        DateEditorSkin.HEIGHT = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_HEIGHT);
        DateEditorSkin.HEIGHT_POPUPS = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_HEIGHT_POPUPS);
        DateEditorSkin.WIDTH_DATE = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_WIDTH_D);
        DateEditorSkin.WIDTH_TIME = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_WIDTH_T);
        DateEditorSkin.WIDTH_DATETIME = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_WIDTH_DT);
        DateEditorSkin.COLOR_BG = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_COLOR_BG);
        DateEditorSkin.COLOR_POPUPS_BG = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_COLOR_POPUPS_BG);
        DateEditorSkin.COLOR_BORDER = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_COLOR_BRDR);
        DateEditorSkin.COLOR_TRAVERSE_IND = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_COLOR_TRAV_IND);
        DateEditorSkin.COLOR_CLOCKHAND_LT = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_COLOR_CLK_LT);
        DateEditorSkin.COLOR_CLOCKHAND_DK = SkinLoader.getInt(
                SkinPropertiesIDs.DATEEDITOR_COLOR_CLK_DK);
        DateEditorSkin.FONT_POPUPS = SkinLoader.getFont(
                SkinPropertiesIDs.DATEEDITOR_FONT_POPUPS);
        /*
        Uncomment if background image is used
        DateEditorSkin.IMAGE_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_BG, 9);
        */
        DateEditorSkin.IMAGE_MONTH_BG = SkinLoader.getImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_MON_BG);
        DateEditorSkin.IMAGE_YEAR_BG = SkinLoader.getImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_YR_BG);
        DateEditorSkin.IMAGE_CAL_BG = SkinLoader.getImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_CAL_BG);
        DateEditorSkin.IMAGE_DATES = SkinLoader.getImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_DATES);
        DateEditorSkin.IMAGE_DAYS = SkinLoader.getImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_DAYS);
        DateEditorSkin.IMAGE_TIME_BG = SkinLoader.getImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_TIME_BG);
        DateEditorSkin.IMAGE_RADIO = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_RADIO, 2);
        DateEditorSkin.IMAGE_AMPM = SkinLoader.getImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_AMPM);
        DateEditorSkin.IMAGE_CLOCK_BG = SkinLoader.getImage(
                SkinPropertiesIDs.DATEEDITOR_IMAGE_CLOCK_BG);
        /*
          Locale is set to a country where uses RIGHT-TO-LEFT note
          e.g Hebrew, Arabic
          */
        DateEditorSkin.IMAGE_MONTH_HE_BG = SkinLoader.getImage(
                 SkinPropertiesIDs.DATEEDITOR_IMAGE_MON_HE_BG);
         DateEditorSkin.IMAGE_DAY_HE_BG = SkinLoader.getImage(
                 SkinPropertiesIDs.DATEEDITOR_IMAGE_DAY_HE_BG);
         DateEditorSkin.IMAGE_YEAR_HE_BG = SkinLoader.getImage(
                 SkinPropertiesIDs.DATEEDITOR_IMAGE_YR_HE_BG);
         DateEditorSkin.IMAGE_TIME_HE_BG = SkinLoader.getImage(
                 SkinPropertiesIDs.DATEEDITOR_IMAGE_TIME_HE_BG);   

        init = true;
    }
}


