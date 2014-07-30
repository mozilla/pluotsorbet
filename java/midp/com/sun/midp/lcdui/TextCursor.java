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

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Graphics;

import com.sun.midp.chameleon.skins.TextFieldSkin;

/**
 * Class that represents the character index, and (x,y) position
 * of a text cursor in a TextField
 */
public class TextCursor {

    /** x, y coordinates */
    public int x, y;
   
    /** width, height */
    public int width, height;
   
    /** array index */
    public int index;

    /** drawing options: can be one of the PAINT_* variables in Text.java */
    public int option;

    /** whether or not this cursor is visible */
    public boolean visible;

    /** preferred x location when traversing vertically */
    public int preferredX;

    /** yOffset to modify paint by for calculating h value */
    public int yOffset;

    /**
     * Construct a new text cursor with the given array index
     * 
     * @param index index into the array that this cursor will be drawn
     */
    public TextCursor(int index) {
        this.index = index;
        option = Text.PAINT_USE_CURSOR_INDEX;
        visible = true;
    }
    /**
     * Copy a TextCursor object
     *
     * @param tc TextCursor object to copy
     */
    public TextCursor(TextCursor tc) {
        this(0);

        if (tc != null) {
            this.x       = tc.x;
            this.y       = tc.y;
            this.option  = tc.option;
            this.index   = tc.index;
            this.visible = tc.visible;
	    this.preferredX = tc.preferredX;
	    this.yOffset = tc.yOffset;
        }
    }

    /**
     * Paint this cursor in the given graphics context
     *
     * @param g the graphics context to paint in
     */
    public void paint(Graphics g) {
        // Stroke should already be SOLID
        g.setColor(TextFieldSkin.COLOR_FG);
        g.fillRect(x, y - yOffset - height + 2, 
                   TextFieldSkin.WIDTH_CARET, height - 3);
        g.setColor(0); // back to default black
    }
}

