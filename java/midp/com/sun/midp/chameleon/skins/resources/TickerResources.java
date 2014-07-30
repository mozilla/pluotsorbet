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
import com.sun.midp.chameleon.skins.TickerSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class TickerResources {
    private static boolean init;
    
    private TickerResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        TickerSkin.HEIGHT = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_HEIGHT);

        int align = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_ALIGN);
        TickerSkin.ALIGN = SkinLoader.resourceConstantsToGraphics(align);

        int direction = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_DIRECTION);
        TickerSkin.DIRECTION = SkinLoader.resourceConstantsToGraphics(
                direction);

        TickerSkin.RATE = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_RATE);
        TickerSkin.SPEED = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_SPEED);
        TickerSkin.TEXT_ANCHOR_Y = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_TEXT_ANCHOR_Y);

        int shdAlign = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_TEXT_SHD_ALIGN);
        TickerSkin.TEXT_SHD_ALIGN = SkinLoader.resourceConstantsToGraphics(
                shdAlign);

        TickerSkin.COLOR_BG = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_COLOR_BG);
        TickerSkin.COLOR_FG = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_COLOR_FG);
        TickerSkin.COLOR_FG_SHD = SkinLoader.getInt(
                SkinPropertiesIDs.TICKER_COLOR_FG_SHD);
        TickerSkin.FONT = SkinLoader.getFont(
                SkinPropertiesIDs.TICKER_FONT);
        TickerSkin.IMAGE_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.TICKER_IMAGE_BG, 3);
        TickerSkin.IMAGE_AU_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.TICKER_IMAGE_AU_BG, 3);

        init = true;
    }
}

