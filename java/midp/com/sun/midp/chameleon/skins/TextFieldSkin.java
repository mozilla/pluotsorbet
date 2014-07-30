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
 * TextFieldSkin represents the properties and values used to render
 * a TextField in the javax.microedition.lcdui package.
 */
public class TextFieldSkin {
    
    /**
     * This field corresponds to TEXTFIELD_PAD_H skin property.
     * See its comment for further details.
     */
    public static int PAD_H;

    /**
     * This field corresponds to TEXTFIELD_PAD_V skin property.
     * See its comment for further details.
     */
    public static int PAD_V;

    /**
     * This field corresponds to TEXTFIELD_BOX_MARGIN skin property.
     * See its comment for further details.
     */
    public static int BOX_MARGIN;

    /**
     * This field corresponds to TEXTFIELD_WIDTH_CARET skin property.
     * See its comment for further details.
     */
    public static int WIDTH_CARET;

    /**
     * This field corresponds to TEXTFIELD_SCRL_RATE skin property.
     * See its comment for further details.
     */
    public static int SCROLL_RATE;

    /**
     * This field corresponds to TEXTFIELD_SCRL_SPD skin property.
     * See its comment for further details.
     */
    public static int SCROLL_SPEED;

    /**
     * This field corresponds to TEXTFIELD_COLOR_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG;

    /**
     * This field corresponds to TEXTFIELD_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;

    /**
     * This field corresponds to TEXTFIELD_COLOR_BRDR skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER;

    /**
     * This field corresponds to TEXTFIELD_COLOR_BRDR_SHD skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_SHD;

    /**
     * This field corresponds to TEXTFIELD_COLOR_FG_UE skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG_UE;

    /**
     * This field corresponds to TEXTFIELD_COLOR_BG_UE skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG_UE;

    /**
     * This field corresponds to TEXTFIELD_COLOR_BRDR_UE skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_UE;

    /**
     * This field corresponds to TEXTFIELD_COLOR_BRDR_SHD_UE skin property.
     * See its comment for further details.
     */
    public static int COLOR_BORDER_SHD_UE;

    /**
     * This field corresponds to TEXTFIELD_IMAGE_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BG;

    /**
     * This field corresponds to TEXTFIELD_IMAGE_BG_UE skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BG_UE;

    // private constructor
    private TextFieldSkin() {
    }
    
}

