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
import com.sun.midp.chameleon.skins.ScrollIndSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class ScrollIndResources {
    private static boolean init;
    
    // private constructor
    private ScrollIndResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        ScrollIndSkin.MODE = SkinLoader.getInt(
                SkinPropertiesIDs.SCROLL_MODE);
        ScrollIndSkin.WIDTH = SkinLoader.getInt(
                SkinPropertiesIDs.SCROLL_WIDTH);
    
        ScrollIndSkin.COLOR_BG = SkinLoader.getInt(
                SkinPropertiesIDs.SCROLL_COLOR_BG);
        ScrollIndSkin.COLOR_FG = SkinLoader.getInt(
                SkinPropertiesIDs.SCROLL_COLOR_FG);
        ScrollIndSkin.COLOR_FRAME = SkinLoader.getInt(
                SkinPropertiesIDs.SCROLL_COLOR_FRAME);
        ScrollIndSkin.COLOR_DN_ARROW = SkinLoader.getInt(
                SkinPropertiesIDs.SCROLL_COLOR_DN_ARROW);
        ScrollIndSkin.COLOR_UP_ARROW = SkinLoader.getInt(
                SkinPropertiesIDs.SCROLL_COLOR_UP_ARROW);

        /*
        Uncomment if background/foreground images are used
        ScrollIndSkin.IMAGE_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.SCROLL_IMAGE_BG, 3);
        ScrollIndSkin.IMAGE_FG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.SCROLL_IMAGE_FG, 3);
        */
        ScrollIndSkin.IMAGE_UP = SkinLoader.getImage(
                SkinPropertiesIDs.SCROLL_IMAGE_UP);
        ScrollIndSkin.IMAGE_DN = SkinLoader.getImage(
                SkinPropertiesIDs.SCROLL_IMAGE_DN);
    
        ScrollIndSkin.COLOR_AU_BG = SkinLoader.getInt(
                SkinPropertiesIDs.SCROLL_COLOR_AU_BG);
        ScrollIndSkin.COLOR_AU_FG = SkinLoader.getInt(
                SkinPropertiesIDs.SCROLL_COLOR_AU_FG);
        /*
        Uncomment if background/foreground images are used for an Alert
        ScrollIndSkin.IMAGE_AU_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.SCROLL_IMAGE_AU_BG, 3);
        ScrollIndSkin.IMAGE_AU_FG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.SCROLL_IMAGE_AU_FG, 3);
        */
        ScrollIndSkin.IMAGE_AU_UP = SkinLoader.getImage(
                SkinPropertiesIDs.SCROLL_IMAGE_AU_UP);
        ScrollIndSkin.IMAGE_AU_DN = SkinLoader.getImage(
                SkinPropertiesIDs.SCROLL_IMAGE_AU_DN);

        init = true;
    }
}
