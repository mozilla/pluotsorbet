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
import com.sun.midp.chameleon.skins.PTISkin;
import com.sun.midp.chameleon.skins.ScreenSkin;

/** Resources for predictive text input layer */
public class PTIResources {
    /** Flag indicated if resources have been already loaded */
    private static boolean init;

    /** Private constructor */
    private PTIResources() {
    }
     
    /** Load pti resources. Do nothing if they have been already loaded */
    public static void load() {
        load(false);
    }
        
    /**
     *  Load pti resources.
     * @param reload if true resources are being loaded even if the
     * initialization has been already done. In case of false don't
     * reload the resources 
     */
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }
        
        PTISkin.HEIGHT = SkinLoader.getInt(
                SkinPropertiesIDs.PTI_HEIGHT);

        PTISkin.MARGIN = SkinLoader.getInt(
                SkinPropertiesIDs.PTI_MARGIN);
        PTISkin.COLOR_BG = SkinLoader.getInt(
                SkinPropertiesIDs.PTI_COLOR_BG);
        PTISkin.COLOR_FG = SkinLoader.getInt(
                SkinPropertiesIDs.PTI_COLOR_FG);
        PTISkin.COLOR_FG_HL = SkinLoader.getInt(
                SkinPropertiesIDs.PTI_COLOR_FG_HL);
        PTISkin.COLOR_BG_HL = SkinLoader.getInt(
                SkinPropertiesIDs.PTI_COLOR_BG_HL);
        PTISkin.COLOR_BDR = SkinLoader.getInt(
                SkinPropertiesIDs.PTI_COLOR_BDR);
        PTISkin.FONT = SkinLoader.getFont(
                SkinPropertiesIDs.PTI_FONT);
        PTISkin.IMAGE_BG = SkinLoader.getCompositeImage(
                SkinPropertiesIDs.PTI_IMAGE_BG, 3);
        PTISkin.LEFT_ARROW = SkinLoader.getImage(
                SkinPropertiesIDs.PTI_LEFT_ARROW);
        PTISkin.RIGHT_ARROW = SkinLoader.getImage(
                SkinPropertiesIDs.PTI_RIGHT_ARROW);

        init = true;
    }
}

