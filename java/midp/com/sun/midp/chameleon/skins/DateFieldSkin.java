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
package com.sun.midp.chameleon.skins;

import com.sun.midp.chameleon.skins.resources.*;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

/**
 * DateFieldSkin represents the properties and values used to render
 * DateField and DateEditor in the javax.microedition.lcdui package.
 */
public class DateFieldSkin {
    
    /**
     * This field corresponds to DATEFIELD_PAD_H skin property.
     * See its comment for further details.
     */
    public static int PAD_H;

    /**
     * This field corresponds to DATEFIELD_PAD_V skin property.
     * See its comment for further details.
     */
    public static int PAD_V;

    /**
     * This field corresponds to DATEFIELD_BTN_BRDR_W skin property.
     * See its comment for further details.
     */
    public static int BUTTON_BORDER_W;

    /**
     * This field corresponds to DATEFIELD_FONT skin property.
     * See its comment for further details.
     */
    public static Font FONT;

    /**
     * This field corresponds to DATEFIELD_COLOR_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG;

    /**
     * This field corresponds to DATEFIELD_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;

    /**
     * This field corresponds to DATEFIELD_COLOR_BRDR skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER;

    /**
     * This field corresponds to DATEFIELD_COLOR_BRDR_LT skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_LT;

    /**
     * This field corresponds to DATEFIELD_COLOR_BRDR_DK skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_DK;

    /**
     * This field corresponds to DATEFIELD_COLOR_BRDR_SHD skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_SHD;

    /**
     * This field corresponds to DATEFIELD_IMAGE_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BG;

    /**
     * This field corresponds to DATEFIELD_IMAGE_BTN_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BUTTON_BG;

    /**
     * This field corresponds to DATEFIELD_IMAGE_ICON_DATE skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_ICON_DATE;

    /**
     * This field corresponds to DATEFIELD_IMAGE_ICON_TIME skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_ICON_TIME;

    /**
     * This field corresponds to DATEFIELD_IMAGE_ICON_DATETIME skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_ICON_DATETIME;
    
    // private constructor
    private DateFieldSkin() {
    }
        
}
