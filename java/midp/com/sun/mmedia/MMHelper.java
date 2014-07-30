/*
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
package com.sun.mmedia;

import javax.microedition.lcdui.*;

/**
 * This is a helper interface to communicate between the LCDUI <code>Canvas
 * </code> and
 * the MMAPI video players. It has methods to register and unregister
 * <code>MIDPVideoPainter</code>s with an LCDUI Canvas.
 */
public abstract class MMHelper {
    static private MMHelper mmh = null;

    /**
     * This is the link to the LCDUI canvas implementation for special
     * repaint events. This is called by javax.microedition.lcdui.MMHelperImpl.
     */
    public static void setMMHelper(MMHelper mmhelper) {
        // Safeguard to make sure its called only once
        if (mmh == null)
            mmh = mmhelper;
    }

    /**
     * This method is called by MIDPVideoPainter implementation
     * to get a hold of the MMHelper...
     */
    static MMHelper getMMHelper() {
        return mmh;
    }

    /**
     * Registers a video control (which implements MIDPVideoPainter) with
     * the corresponding Canvas where the video is to show up.
     */
    public abstract void registerPlayer(Canvas c, MIDPVideoPainter vp);

    /**
     * Unregisters a video control so that it doesn't get paint callbacks
     * anymore after the player is closed. This also reduces load on the
     * Canvas repaint mechanism.
     */
    public abstract void unregisterPlayer(Canvas c, MIDPVideoPainter vp);


    /**
     * Get Display being used for Item painting. Platform-dependent.
     */
    public abstract Display getDisplayFor(Item item);

    /**
     * Get Display being used for painting of given Displayable. Platform-dependent.
     */
    public abstract Display getDisplayFor(Displayable displayable);

    /**
     * Get width of given display.
     * @param display
     * @return number of pixels Display occupies horizontally
     */
    public abstract int getDisplayWidth(Display display);

    /**
     * Get height of given display.
     * @param display
     * @return number of pixels Display occupies vertically
     */
    public abstract int getDisplayHeight(Display display);

    /**
     * Is current Display (playing video) overlapped by system layers
     */
    public abstract boolean isDisplayOverlapped(Graphics g);

};
