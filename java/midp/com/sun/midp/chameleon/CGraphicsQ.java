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

package com.sun.midp.chameleon;

import javax.microedition.lcdui.*;
import java.util.Vector;

/**
 * Chameleon graphics queue class. This class contains methods 
 * to help to better control when, how, and how many pixels 
 * actually get blitted from the buffer to the physical display.
 */
public class CGraphicsQ {
    /** 
     * By turning on "debug" mode, all of Chameleon's graphics engine
     * will output tracing info regarding dirty regions, layer locations,
     * bounds, repaints, etc.
     */    
    public static final boolean DEBUG = false;
    
    /** A queue of refresh areas, represented by 4 element arrays */
    protected Vector refreshQ;
    
    /**
     * Construct a new Graphics queue. 
     */
    public CGraphicsQ() {
        refreshQ = new Vector();
    }
    
    /**
     * Add the specified region to the queue of areas to be
     * refreshed. That is, the region specified by the given
     * coordinates represents a region that has been repainted
     * and needs to be blitted to the display. The coordinates
     * of the region should be in raw screen coordinates, that
     * is, 0,0 would represent the topleft pixel on the screen.
     *
     * @param x the 'x' anchor coordinate of the region
     * @param y the 'y' anchor coordinate of the region
     * @param w the width of the region
     * @param h the height of the region
     */
    public void queueRefresh(int x, int y, int w, int h) {
        synchronized (refreshQ) {
            int[] region;
            for (int i = 0; i < refreshQ.size(); i++) {
                region = (int[])refreshQ.elementAt(i);
                
                // We test to see if we already have the dirty region
                if (region[0] == x && region[1] == y &&
                    region[2] == w && region[3] == h)
                {
                    return;
                }

                // We also test to see if the dirty region is wholely
                // contained within another region
                if (x >= region[0] && y >= region[1] &&                
                    (x + w) <= (region[0] + region[1]) && 
                    (y + h) <= (region[1] + region[3])) 
                {
                    return;                    
                }

                // Lastly, we do a special case whereby the region
                // is congruent with a previous region. For instance,
                // when changing screens, the title area will repaint,
                // the body area will repaint, and the soft button
                // area will repaint. All 3 areas are congruent and
                // can be coalesced.
                if (x == region[0] && w == region[2]) {
                    if ((region[1] + region[3]) == y ||
                        (y + h) == region[1]) 
                    {
                        if (region[1] > y) {
                            region[1] = y;
                        }
                        region[3] += h;
                        return;
                    } 
                }
            }
            refreshQ.addElement(new int[] {x, y, w, h});
        }
    }
    
    /**
     * Get the queue of all areas of the screen to be refreshed
     * (blitted to the screen). This method will empty the queue
     * and return its contents. Each element in the array will be
     * a 4 element int[] holding the x, y, w, and h of each refresh
     * region
     *
     * @return the queue of all areas of the screen to be refreshed,
     *         as an array of arrays
     */
    public Object[] getRefreshRegions() {
        synchronized (refreshQ) {
            Object[] q = new Object[refreshQ.size()];
            refreshQ.copyInto(q);
            refreshQ.removeAllElements();
            return q;
        }
    }
}

