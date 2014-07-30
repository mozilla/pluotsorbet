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

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import com.sun.midp.chameleon.skins.resources.*;

/**
 * ScrollIndSkin represents the properties and values used to render
 * a ScrollIndLayer in the com.sun.microedition.chameleon.layers package.
 */
public class ScrollIndSkin {
    /**
     * IMPL_NOTE: constants below have been moved to 
     * ScrollIndResourcesConstants class. However, 
     * they are duplicated here because this file is 
     * used by skin authors to lookup for possible 
     * constants values. This should be changed in
     * future releases.
     */

    /**
     * An value for describing a scroll indicator which
     * uses arrows in the soft button bar.
     */
    public final static int MODE_ARROWS = 
        ScrollIndResourcesConstants.MODE_ARROWS;
    
    /**
     * An value for describing a scroll indicator which
     * uses a vertical bar with a thumb.
     */
    public final static int MODE_BAR = 
        ScrollIndResourcesConstants.MODE_BAR;

    /**
     * This field corresponds to SCROLL_MODE skin property.
     * See its comment for further details.
     */
    public static int MODE;
    
    /**
     * This field corresponds to SCROLL_WIDTH skin property.
     * See its comment for further details.
     */
    public static int WIDTH;
    
    /**
     * This field corresponds to SCROLL_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;
    
    /**
     * This field corresponds to SCROLL_COLOR_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_FG;
    
    /**
     * the arrow fill color when it's released
     */
    public static int COLOR_UP_ARROW;

    /**
     * the arrow fill color when it's pressed
     */
    public static int COLOR_DN_ARROW;

    /**
     * scroll bar frame color
     */
    public static int COLOR_FRAME;

    /**
     * This field corresponds to SCROLL_IMAGE_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_BG;
    
    /**
     * This field corresponds to SCROLL_IMAGE_FG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image foreground
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_FG;
    
    /**
     * This field corresponds to SCROLL_IMAGE_UP skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_UP;
    
    /**
     * This field corresponds to SCROLL_IMAGE_DN skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_DN;
    
    /**
     * This field corresponds to SCROLL_COLOR_AU_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_AU_BG;
    
    /**
     * This field corresponds to SCROLL_COLOR_AU_FG skin property.
     * See its comment for further details.
     */
    public static int COLOR_AU_FG;
    
    /**
     * This field corresponds to SCROLL_IMAGE_AU_BG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_AU_BG;

    /**
     * This field corresponds to SCROLL_IMAGE_AU_FG skin property.
     * See its comment for further details.
     *
     * A 'null' value for this array means there is no image background
     * and a solid fill color should be used.
     */
    public static Image[] IMAGE_AU_FG;
    
    /**
     * This field corresponds to SCROLL_IMAGE_AU_UP skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_AU_UP;
    
    /**
     * This field corresponds to SCROLL_IMAGE_AU_DN skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_AU_DN;

    // private constructor
    private ScrollIndSkin() {
    }
}
