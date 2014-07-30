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
import com.sun.midp.chameleon.skins.ProgressBarSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class ProgressBarResources {
    private static boolean init;
    
    // private constructor
    private ProgressBarResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        ProgressBarSkin.ORIENTATION = SkinLoader.getInt(
                SkinPropertiesIDs.PBAR_ORIENT);
        ProgressBarSkin.WIDTH = SkinLoader.getInt(
                SkinPropertiesIDs.PBAR_WIDTH);
        ProgressBarSkin.HEIGHT = SkinLoader.getInt(
                SkinPropertiesIDs.PBAR_HEIGHT);
        ProgressBarSkin.METER_X = SkinLoader.getInt(
                SkinPropertiesIDs.PBAR_METER_X);
        ProgressBarSkin.METER_Y = SkinLoader.getInt(
                SkinPropertiesIDs.PBAR_METER_Y);
        ProgressBarSkin.VALUE_X = SkinLoader.getInt(
                SkinPropertiesIDs.PBAR_VALUE_X);
        ProgressBarSkin.VALUE_Y = SkinLoader.getInt(
                SkinPropertiesIDs.PBAR_VALUE_Y);
        ProgressBarSkin.VALUE_WIDTH = SkinLoader.getInt(
                SkinPropertiesIDs.PBAR_VALUE_WIDTH);
        ProgressBarSkin.IMAGE_BG = SkinLoader.getImage(
                SkinPropertiesIDs.PBAR_IMAGE_BG);
        ProgressBarSkin.IMAGE_METER_EMPTY = SkinLoader.getImage(
                SkinPropertiesIDs.PBAR_IMAGE_MTR_EMPTY);
        ProgressBarSkin.IMAGE_METER_FULL = SkinLoader.getImage(
                SkinPropertiesIDs.PBAR_IMAGE_MTR_FULL);
        ProgressBarSkin.IMAGE_VALUES = SkinLoader.getImage(
                SkinPropertiesIDs.PBAR_IMAGE_VALUES);
        ProgressBarSkin.IMAGE_PERCENTS = SkinLoader.getImage(
                SkinPropertiesIDs.PBAR_IMAGE_PERCENTS);
            
        init = true;
    }
}
