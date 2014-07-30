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
import com.sun.midp.chameleon.skins.resources.ScrollIndResourcesConstants;


/**
 * A ScrollIndLayer is a region of the display used for showing scroll indicator
 * status either as arrows or bar.  
 */
public abstract class ScrollIndLayer extends CLayer {
    /**
     * Scrollbar operation types
     */
    public static final int SCROLL_NONE = 0;
    public static final int SCROLL_LINEUP = 1;
    public static final int SCROLL_LINEDOWN= 2;
    public static final int SCROLL_PAGEUP = 3;
    public static final int SCROLL_PAGEDOWN= 4;
    public static final int SCROLL_THUMBTRACK= 5;

    /**
     * scrollIndArrow layer is just one for all scrollable layers.
     * It is chached and shared between all layers
     */ 
    private static ScrollIndLayer scrollIndArrows;

    /** Scrollable layer */ 
    protected CLayer scrollable;
    /**
     * True if special alert indicator bar should be drawn instead of 
     * the regular one
     */
    protected boolean alertMode;

    /**
     * Scrolling listener.
     * This layer is notified if the scroll indicator is changed
     */
    protected ScrollListener listener;

    /**
     * Set the current vertical scroll position and proportion.
     *
     * @param scrollPosition vertical scroll position.
     * @param scrollProportion vertical scroll proportion.
     */
    public abstract void setVerticalScroll(int scrollPosition, 
                                  int scrollProportion);
    /**
     * Calculate layer bounds depending on the scrollable
     */
    public abstract void setBounds();

    /**
     * Common constructor.
     * @param layer the scrollable controlling the scrolling layer 
     */
    protected ScrollIndLayer(CLayer layer) {
        super();
        setOpaque(false);
        setSupportsInput(true);
        scrollable = layer;
        alertMode = scrollable instanceof AlertLayer;
    }

    /**
     * Additional constructor.
     * @param layer the scrollable controlling the scrolling layer 
     * @param listener the scrolling listener
     */
    public ScrollIndLayer(CLayer layer, ScrollListener listener) {
        this(layer);
        this.listener = listener;
    }


    /**
     * Set new scrollable 
     * @param layer new scrollable controlling the scrolling layer
     * @return true if the scrollable is changed, false - otherwise
     */
    public boolean setScrollable(CLayer layer) {
        boolean ret = scrollable != layer;
        if (ret) {
            setVisible(false);
            scrollable = layer;
            alertMode = scrollable instanceof AlertLayer;
        }
        return ret;
    }
    
    /**
     * Set new listener 
     * @param newListener new scrolling listener
     * @return true if the listener is changed, false - otherwise
     */
    public boolean setListener(ScrollListener newListener) {
        boolean ret = listener != newListener;
        if (ret) {
            listener = newListener;
            if (listener != null) {
                listener.updateScrollIndicator();
            }
        }
        return ret;
    }

    public static ScrollIndLayer getInstance(int type) {
        ScrollIndLayer s = null;
        switch (type) {
        case ScrollIndResourcesConstants.MODE_ARROWS:
            if (scrollIndArrows == null) {
                scrollIndArrows = new ScrollArrowLayer(null, null);
            }
            s = scrollIndArrows;
            break;
        case ScrollIndResourcesConstants.MODE_BAR:
            s = new ScrollBarLayer(null, null);
            break;
        default:
            break;
        }
        return s;
    }
}
