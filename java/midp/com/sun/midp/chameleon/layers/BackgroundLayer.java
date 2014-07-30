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

package com.sun.midp.chameleon.layers;

import com.sun.midp.chameleon.*;
import javax.microedition.lcdui.*;

/**
 * Background layer responsible for window background painting,
 * should be added as the most bottom layer of a window 
 */
public class BackgroundLayer extends CLayer {

    public BackgroundLayer(Image bgImage, int bgColor) {
        super(bgImage, bgColor);
        super.opaque = true;
        visible = !transparent;
        tileBG = true;
    }

    /**
     * Set new background image or color for the layer
     * @param bgImage image to be tiled as background
     * @param bgColor the color to fill background with
     *   in the case null tile image is specified
     */
    public void setBackground(Image bgImage, int bgColor) {
        setBackground(bgImage, tileBG, bgColor);
        visible = !transparent;
    }
}

