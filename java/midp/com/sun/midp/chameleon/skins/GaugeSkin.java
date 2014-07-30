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
import javax.microedition.lcdui.Graphics;

/**
 * GaugeSkin represents the properties and values used to render
 * a Gauge in the javax.microedition.lcdui package.
 */
public class GaugeSkin {

    /**
     * This field corresponds to GAUGE_ORIENT skin property.
     * See its comment for further details.
     */
    public static int ORIENTATION;

    /**
     * This field corresponds to GAUGE_WIDTH skin property.
     * See its comment for further details.
     */
    public static int WIDTH;

    /**
     * This field corresponds to GAUGE_HEIGHT skin property.
     * See its comment for further details.
     */
    public static int HEIGHT;

    /**
     * This field corresponds to GAUGE_METER_X skin property.
     * See its comment for further details.
     */
    public static int METER_X;

    /**
     * This field corresponds to GAUGE_METER_Y skin property.
     * See its comment for further details.
     */
    public static int METER_Y;

    /**
     * This field corresponds to GAUGE_INC_BTN_X skin property.
     * See its comment for further details.
     */
    public static int INC_BTN_X;

    /**
     * This field corresponds to GAUGE_INC_BTN_Y skin property.
     * See its comment for further details.
     */
    public static int INC_BTN_Y;

    /**
     * This field corresponds to GAUGE_DEC_BTN_X skin property.
     * See its comment for further details.
     */
    public static int DEC_BTN_X;

    /**
     * This field corresponds to GAUGE_DEC_BTN_Y skin property.
     * See its comment for further details.
     */
    public static int DEC_BTN_Y;

    /**
     * This field corresponds to GAUGE_VALUE_X skin property.
     * See its comment for further details.
     */
    public static int VALUE_X;

    /**
     * This field corresponds to GAUGE_VALUE_Y skin property.
     * See its comment for further details.
     */
    public static int VALUE_Y;

    /**
     * This field corresponds to GAUGE_VALUE_WIDTH skin property.
     * See its comment for further details.
     */
    public static int VALUE_WIDTH;

    /**
     * This field corresponds to GAUGE_IMAGE_BG skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_BG;

    /**
     * This field corresponds to GAUGE_IMAGE_MTR_EMPTY skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_METER_EMPTY;

    /**
     * This field corresponds to GAUGE_IMAGE_MTR_FULL skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_METER_FULL;

    /**
     * This field corresponds to GAUGE_INC_BTN skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_INC_BTN;

    /**
     * This field corresponds to GAUGE_DEC_BTN skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_DEC_BTN;

    /**
     * This field corresponds to GAUGE_IMAGE_VALUES skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_VALUES;
            
    // private constructor
    private GaugeSkin() {
    }
     
}

