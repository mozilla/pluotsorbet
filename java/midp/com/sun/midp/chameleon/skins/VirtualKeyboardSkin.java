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


/**
 * A skin containing images and parameters related to the PTI bar.
 */
public class VirtualKeyboardSkin {


    /**
     * This field corresponds to KEYBOARD_HEIGHT skin property.
     * See its comment for further details.
     */
    public static int HEIGHT;
    
    /**
     * This field corresponds to KEYBOARD_COEFFICIENT skin property.
     * See its comment for further details.
     */
    public static double COEFFICIENT;

    /**
     * This field corresponds to KEYBOARD_KEY skin property.
     * See its comment for further details.
     */
    public static Image KEY;

    /**
     * This field corresponds to KEYBOARD_BTN_BACKSPACE skin property.
     * See its comment for further details.
     */
    public static Image BTN_BACKSPACE;

    /**
     * This field corresponds to KEYBOARD_BTN_CAPS skin property.
     * See its comment for further details.
     */
    public static Image BTN_CAPS;

    /**
     * This field corresponds to KEYBOARD_BTN_ENTER skin property.
     * See its comment for further details.
     */
    public static Image BTN_ENTER;

    /**
     * This field corresponds to KEYBOARD_BTN_ALPHA_MODE skin property.
     * See its comment for further details.
     */
    public static Image BTN_ALPHA_MODE;

    /**
     * This field corresponds to BTN_SYMBOL_MODE skin property.
     * See its comment for further details.
     */
    public static Image BTN_SYMBOL_MODE;

    /**
     * This field corresponds to BTN_NUMERIC_MODE skin property.
     * See its comment for further details.
     */
    public static Image BTN_NUMERIC_MODE;

    /**
     * This field corresponds to KEYBOARD_BG skin property.
     * See its comment for further details.
     */
    public static Image[] BG;

    /**
     * This field corresponds to KEYBOARD_COLOR_BG skin property.
     * See its comment for further details.
     */
    public static int COLOR_BG;

    /**
     * This field corresponds to KEYBOARD_BTN_UP_SEL skin property.
     * See its comment for further details.
     */
    public static Image BTN_UP_SEL;

    /**
     * This field corresponds to KEYBOARD_BTN_UP_UN skin property.
     * See its comment for further details.
     */
    public static Image BTN_UP_UN;

    /**
     * This field corresponds to KEYBOARD_BTN_LEFT_SEL skin property.
     * See its comment for further details.
     */
    public static Image BTN_LEFT_SEL;

    /**
     * This field corresponds to KEYBOARD_BTN_LEFT_UN skin property.
     * See its comment for further details.
     */
    public static Image BTN_LEFT_UN;

    /**
     * This field corresponds to KEYBOARD_BTN_MID_SEL skin property.
     * See its comment for further details.
     */
    public static Image BTN_MID_SEL;

    /**
     * This field corresponds to KEYBOARD_BTN_MID_UN skin property.
     * See its comment for further details.
     */
    public static Image BTN_MID_UN;

    /**
     * This field corresponds to KEYBOARD_BTN_RIGHT_SEL skin property.
     * See its comment for further details.
     */
    public static Image BTN_RIGHT_SEL;

    /**
     * This field corresponds to KEYBOARD_BTN_RIGHT_UN skin property.
     * See its comment for further details.
     */
    public static Image BTN_RIGHT_UN;

    /**
     * This field corresponds to KEYBOARD_BTN_DOWN_SEL skin property.
     * See its comment for further details.
     */
    public static Image BTN_DOWN_SEL;

    /**
     * This field corresponds to KEYBOARD_BTN_DOWN_UN skin property.
     * See its comment for further details.
     */
    public static Image BTN_DOWN_UN;

    /**
     * This field corresponds to KEYBOARD_FONT skin property.
     * See its comment for further details.
     */
    public static Font FONT;



    /** private constructor */
    private VirtualKeyboardSkin() {
    }
}

