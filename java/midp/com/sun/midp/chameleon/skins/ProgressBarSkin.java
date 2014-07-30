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
 *
 */
public class ProgressBarSkin {
    
    /**
     * This field corresponds to PBAR_ORIENT skin property.
     * See its comment for further details.
     */
    public static int ORIENTATION;

    /**
     * This field corresponds to PBAR_WIDTH skin property.
     * See its comment for further details.
     */
    public static int WIDTH;

    /**
     * This field corresponds to PBAR_HEIGHT skin property.
     * See its comment for further details.
     */
    public static int HEIGHT;

    /**
     * This field corresponds to PBAR_METER_X skin property.
     * See its comment for further details.
     */
    public static int METER_X;

    /**
     * This field corresponds to PBAR_METER_Y skin property.
     * See its comment for further details.
     */
    public static int METER_Y;

    /**
     * This field corresponds to PBAR_VALUE_X skin property.
     * See its comment for further details.
     */
    public static int VALUE_X;

    /**
     * This field corresponds to PBAR_VALUE_Y skin property.
     * See its comment for further details.
     */
    public static int VALUE_Y;

    /**
     * This field corresponds to PBAR_VALUE_WIDTH skin property.
     * See its comment for further details.
     */
    public static int VALUE_WIDTH;

    /**
     * This field corresponds to PBAR_IMAGE_BG skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_BG;

    /**
     * This field corresponds to PBAR_IMAGE_MTR_EMPTY skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_METER_EMPTY;

    /**
     * This field corresponds to PBAR_IMAGE_MTR_FULL skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_METER_FULL;

    /**
     * This field corresponds to PBAR_IMAGE_VALUES skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_VALUES;

    /**
     * This field corresponds to PBAR_IMAGE_PERCENTS skin property.
     * See its comment for further details.
     */
    public static Image IMAGE_PERCENTS;

    // private constructor
    private ProgressBarSkin() {
    }    
    
}

