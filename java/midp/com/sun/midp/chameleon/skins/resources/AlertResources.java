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
import com.sun.midp.chameleon.skins.AlertSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class AlertResources {
    private static boolean init;
    
    private AlertResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        AlertSkin.WIDTH = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_WIDTH);

        AlertSkin.HEIGHT = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_HEIGHT);

        int alignX = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_ALIGN_X);
        AlertSkin.ALIGN_X = SkinLoader.resourceConstantsToGraphics(alignX);

        int alignY = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_ALIGN_Y);
        AlertSkin.ALIGN_Y = SkinLoader.resourceConstantsToGraphics(alignY);

        AlertSkin.MARGIN_H = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_MARGIN_H);
        AlertSkin.MARGIN_V = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_MARGIN_V);

        int titleAlign = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_TITLE_ALIGN);
        AlertSkin.TITLE_ALIGN = SkinLoader.resourceConstantsToGraphics(
                titleAlign);

        AlertSkin.TITLE_HEIGHT = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_TITLE_HEIGHT);
        AlertSkin.TITLE_MARGIN = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_TITLE_MARGIN);
        AlertSkin.TEXT_TITLE_INFO = SkinLoader.getString(
                SkinPropertiesIDs.ALERT_TEXT_TITLE_INFO);
        AlertSkin.TEXT_TITLE_WARN = SkinLoader.getString(
                SkinPropertiesIDs.ALERT_TEXT_TITLE_WARN);
        AlertSkin.TEXT_TITLE_ERRR = SkinLoader.getString(
                SkinPropertiesIDs.ALERT_TEXT_TITLE_ERRR);
        AlertSkin.TEXT_TITLE_ALRM = SkinLoader.getString(
                SkinPropertiesIDs.ALERT_TEXT_TITLE_ALRM);
        AlertSkin.TEXT_TITLE_CNFM = SkinLoader.getString(
                SkinPropertiesIDs.ALERT_TEXT_TITLE_CNFM);
        AlertSkin.PAD_HORIZ = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_PAD_HORIZ);
        AlertSkin.PAD_VERT = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_PAD_VERT); 
        AlertSkin.SCROLL_AMOUNT = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_SCROLL_AMOUNT);
        AlertSkin.TIMEOUT = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_TIMEOUT);
        AlertSkin.COLOR_BG = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_COLOR_BG);
        AlertSkin.COLOR_TITLE = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_COLOR_TITLE);
        AlertSkin.COLOR_FG = SkinLoader.getInt(
                SkinPropertiesIDs.ALERT_COLOR_FG);
        AlertSkin.FONT_TITLE = SkinLoader.getFont(
                SkinPropertiesIDs.ALERT_FONT_TITLE);
        AlertSkin.FONT_TEXT = SkinLoader.getFont(
                SkinPropertiesIDs.ALERT_FONT_TEXT);
        AlertSkin.IMAGE_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.ALERT_IMAGE_BG, 9);
        AlertSkin.IMAGE_ICON_INFO = SkinLoader.getImage(
                SkinPropertiesIDs.ALERT_IMAGE_ICON_INFO); 
        AlertSkin.IMAGE_ICON_WARN = SkinLoader.getImage(
                SkinPropertiesIDs.ALERT_IMAGE_ICON_WARN);
        AlertSkin.IMAGE_ICON_ERRR = SkinLoader.getImage(
                SkinPropertiesIDs.ALERT_IMAGE_ICON_ERRR);
        AlertSkin.IMAGE_ICON_ALRM = SkinLoader.getImage(
                SkinPropertiesIDs.ALERT_IMAGE_ICON_ALRM);
        AlertSkin.IMAGE_ICON_CNFM = SkinLoader.getImage(
                SkinPropertiesIDs.ALERT_IMAGE_ICON_CNFM);

        checkLocale();

        init = true;
    }


    public static void checkLocale() {
        String locale = System.getProperty("microedition.locale");

        if (locale != null && locale.equals("he-IL")) {
            AlertSkin.TITLE_ALIGN = Graphics.RIGHT;
            AlertSkin.ALIGN_X = Graphics.RIGHT;
        } else {
            AlertSkin.TITLE_ALIGN = Graphics.LEFT;
            AlertSkin.ALIGN_X = Graphics.LEFT;
        }
    }
}
    

