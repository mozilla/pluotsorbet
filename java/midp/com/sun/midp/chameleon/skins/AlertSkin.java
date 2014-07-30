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

import com.sun.midp.chameleon.skins.ScreenSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

/**
 * StringItemSkin represents the properties and values used to render
 * a StringItem in the javax.microedition.lcdui package.
 */
public class AlertSkin {
    
    /**
     * This field corresponds to ALERT_WIDTH skin property.
     * See its comment for further details.
     */
    public static int WIDTH;
    
    /**
     * This field corresponds to ALERT_HEIGHT skin property.
     * See its comment for further details.
     */
    public static int HEIGHT;
    
    /**
     * This field corresponds to ALERT_ALIGN_X skin property.
     * See its comment for further details.
     */
    public static int ALIGN_X;
    
    /**
     * This field corresponds to ALERT_ALIGN_Y skin property.
     * See its comment for further details.
     */
    public static int ALIGN_Y;
    
    /**
     * This field corresponds to ALERT_MARGIN_H skin property.
     * See its comment for further details.
     */
    public static int MARGIN_H;
    
    /**
     * This field corresponds to ALERT_MARGIN_V skin property.
     * See its comment for further details.
     */
    public static int MARGIN_V;
    
    /**
     * This field corresponds to ALERT_TITLE_ALIGN skin property.
     * See its comment for further details.
     */
    public static int TITLE_ALIGN;
    
    /**
     * This field corresponds to ALERT_TITLE_HEIGHT skin property.
     * See its comment for further details.
     */
    public static int TITLE_HEIGHT;
    
    /**
     * This field corresponds to ALERT_TITLE_MARGIN skin property.
     * See its comment for further details.
     */
    public static int TITLE_MARGIN;

    /**
     * This field corresponds to ALERT_TEXT_TITLE_INFO skin property.
     * See its comment for further details.
     */
    public static String TEXT_TITLE_INFO;

    /**
     * This field corresponds to ALERT_TEXT_TITLE_WARN skin property.
     * See its comment for further details.
     */
    public static String TEXT_TITLE_WARN;
    
    /**
     * This field corresponds to ALERT_TEXT_TITLE_ERRR skin property.
     * See its comment for further details.
     */
    public static String TEXT_TITLE_ERRR;
    
    /**
     * This field corresponds to ALERT_TEXT_TITLE_ALRM skin property.
     * See its comment for further details.
     */
    public static String TEXT_TITLE_ALRM;
    
    /**
     * This field corresponds to ALERT_TEXT_TITLE_CNFM skin property.
     * See its comment for further details.
     */
    public static String TEXT_TITLE_CNFM;
    
    /**
     * This field corresponds to ALERT_PAD_HORIZ skin property.
     * See its comment for further details.
     */
    public static int PAD_HORIZ;
    
    /**
     * This field corresponds to ALERT_PAD_VERT skin property.
     * See its comment for further details.
     */
    public static int PAD_VERT;
    
    /**
     * This field corresponds to ALERT_SCROLL_AMOUNT skin property.
     * See its comment for further details.
     */
    public static int SCROLL_AMOUNT;
    
    /**
     * This field corresponds to ALERT_TIMEOUT skin property.
     * See its comment for further details.
     */
    public static int TIMEOUT;
    
    /**
     * This field corresponds to ALERT_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;
    
    /**
     * This field corresponds to ALERT_COLOR_TITLE skin property.
     * See its comment for further details.
     */
    public static int COLOR_TITLE;
    
    /**
     * This field corresponds to ALERT_COLOR_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG;
    
    /**
     * This field corresponds to ALERT_FONT_TITLE skin property.
     * See its comment for further details.
     */
    public static Font FONT_TITLE;
    
    /**
     * This field corresponds to ALERT_FONT_TEXT skin property.
     * See its comment for further details.
     */
    public static Font FONT_TEXT;
    
    /**
     * This field corresponds to ALERT_IMAGE_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BG;
    
    /**
     * This field corresponds to ALERT_IMAGE_ICON_INFO skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_ICON_INFO;
    
    /**
     * This field corresponds to ALERT_IMAGE_ICON_WARN skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_ICON_WARN;
    
    /**
     * This field corresponds to ALERT_IMAGE_ICON_ERRR skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_ICON_ERRR;
    
    /**
     * This field corresponds to ALERT_IMAGE_ICON_ALRM skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_ICON_ALRM;
    
    /**
     * This field corresponds to ALERT_IMAGE_ICON_CNFM skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_ICON_CNFM;

    // private constructor
    private AlertSkin() {
    }
        
}
