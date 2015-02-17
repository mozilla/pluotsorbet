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

package javax.microedition.lcdui;

/**
 * This class defines colors, fonts, and padding values that
 * together form consistent Look & Feel.
 * If it is desired to create a Theme of the existing Look by
 * changing colors or font this is the class that has to be modified.
 */
class Theme {

    /** Special content font, shared amongst the LCDUI package */
    static Font curContentFont = Font.getDefaultFont();

    /** Special content height, shared amongst the LCDUI package */
    static int CONTENT_HEIGHT = curContentFont.getHeight();
        
    /** this is for no border */
    final static int BORDER_NONE  = 0;
    /** this is for a solid border */
    final static int BORDER_SOLID = 1;
    /** this is for a dotted border */
    final static int BORDER_DOTTED  = 2;    
    
    /**
     * A boolean declaring whether the viewport is capable of
     * scrolling horizontally. FALSE by default
     */
    final static boolean SCROLLS_HORIZONTAL = false;
    
    /**
     * A boolean declaring whether the viewport is capable of
     * scrolling vertically. TRUE by default
     */
    final static boolean SCROLLS_VERTICAL = true;

    /**
     * The preferred image width for an image as part of an element of
     * a choice (12 pixels).  
     */
    static final int PREFERRED_IMG_W = 12;

    /**
     * The preferred image height for an image as part of an element of
     * a choice (12 pixels).
     */
    static final int PREFERRED_IMG_H = 12;
    
    /**
     */
    static int getColor(int colorSpecifier) {
        switch (colorSpecifier) {
        case Display.COLOR_BACKGROUND:
            return 0xffffff;
        case Display.COLOR_FOREGROUND:
            return 0;
        case Display.COLOR_HIGHLIGHTED_BACKGROUND:
            return 0;
        case Display.COLOR_HIGHLIGHTED_FOREGROUND:
            return 0xffffff;
	case Display.COLOR_BORDER:
            return 0;
        case Display.COLOR_HIGHLIGHTED_BORDER:
            return 0;
	}
	return 0;
    }

    /**
     *
     */
    static int getBorderStyle(boolean highlighted) {
        return (highlighted == true ? Graphics.SOLID : Graphics.DOTTED);
    }
}
