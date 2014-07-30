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
import com.sun.midp.chameleon.skins.GaugeSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class GaugeResources {
    private static boolean init;
    
    // private constructor
    private GaugeResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        GaugeSkin.ORIENTATION = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_ORIENT);
        GaugeSkin.WIDTH = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_WIDTH);
        GaugeSkin.HEIGHT = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_HEIGHT);
        GaugeSkin.METER_X = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_METER_X);
        GaugeSkin.METER_Y = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_METER_Y);
        GaugeSkin.INC_BTN_X = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_INC_BTN_X);
        GaugeSkin.INC_BTN_Y = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_INC_BTN_Y);
        GaugeSkin.DEC_BTN_X = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_DEC_BTN_X);
        GaugeSkin.DEC_BTN_Y = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_DEC_BTN_Y);
        GaugeSkin.VALUE_X = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_VALUE_X);
        GaugeSkin.VALUE_Y = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_VALUE_Y);
        GaugeSkin.VALUE_WIDTH = SkinLoader.getInt(
                SkinPropertiesIDs.GAUGE_VALUE_WIDTH);
        GaugeSkin.IMAGE_BG = SkinLoader.getImage(
                SkinPropertiesIDs.GAUGE_IMAGE_BG);
        GaugeSkin.IMAGE_METER_EMPTY = SkinLoader.getImage(
                SkinPropertiesIDs.GAUGE_IMAGE_MTR_EMPTY);
        GaugeSkin.IMAGE_METER_FULL = SkinLoader.getImage(
                SkinPropertiesIDs.GAUGE_IMAGE_MTR_FULL);
        GaugeSkin.IMAGE_INC_BTN = SkinLoader.getImage(
                SkinPropertiesIDs.GAUGE_IMAGE_INC_BTN);
        GaugeSkin.IMAGE_DEC_BTN = SkinLoader.getImage(
                SkinPropertiesIDs.GAUGE_IMAGE_DEC_BTN);
        GaugeSkin.IMAGE_VALUES = SkinLoader.getImage(
                SkinPropertiesIDs.GAUGE_IMAGE_VALUES);
        
        init = true;
    }
}

