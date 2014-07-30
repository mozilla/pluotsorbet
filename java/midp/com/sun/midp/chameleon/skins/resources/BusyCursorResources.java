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
import com.sun.midp.chameleon.skins.BusyCursorSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class BusyCursorResources {
    private static boolean init;
    
    // private constructor
    private BusyCursorResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        BusyCursorSkin.WIDTH = SkinLoader.getInt(
                SkinPropertiesIDs.BUSYCRSR_WIDTH);
        BusyCursorSkin.HEIGHT = SkinLoader.getInt(
                SkinPropertiesIDs.BUSYCRSR_HEIGHT);
        BusyCursorSkin.NUM_FRAMES = SkinLoader.getInt(
                SkinPropertiesIDs.BUSYCRSR_NUM_FRAMES);
        BusyCursorSkin.FRAME_X = SkinLoader.getInt(
                SkinPropertiesIDs.BUSYCRSR_FRAME_X);
        BusyCursorSkin.FRAME_Y = SkinLoader.getInt(
                SkinPropertiesIDs.BUSYCRSR_FRAME_Y);
        BusyCursorSkin.FRAME_SEQUENCE = SkinLoader.getNumbersSequence(
                SkinPropertiesIDs.BUSYCRSR_FRAME_SEQU);
        BusyCursorSkin.IMAGE_BG = SkinLoader.getImage(
                SkinPropertiesIDs.BUSYCRSR_IMAGE_BG);
        BusyCursorSkin.IMAGE_FRAME = SkinLoader.getCompositeImage(
            SkinPropertiesIDs.BUSYCRSR_IMAGE_FRAME, 
            BusyCursorSkin.NUM_FRAMES);

        init = true;
    }
    
}



