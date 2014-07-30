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
import com.sun.midp.chameleon.skins.ScrollIndSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.resources.ScrollIndResourcesConstants;

import javax.microedition.lcdui.*;

/**
 * A "Popup" layer is a special kind of layer which can
 * also have commands associated with it. When a popup
 * layer is added to a MIDPWindow, its commands (if it has any)
 * can be accessed through the soft button bar. If a popup layer
 * does have commands associated with it, any commands on the
 * current displayable/item are no longer accessible. If
 * the popup layer does not have its own commands, any
 * existing commands from the displayable/item remain.
 *
 * NOTE: For now, a PopupLayer is also always visible,
 * that is isVisible() always returns true. To control the
 * visibility of a PopupLayer, you add it and remove it
 * from a MIDPWindow. IMPL_NOTE: determine if a relationship between
 * PopupLayer and MIDPWindow can allow non visible popup layers.
 */
public class ScrollablePopupLayer extends PopupLayer
    implements ScrollListener {

    /**
     * The scroll indicator layer to notify of scroll settings
     * in case not all content can fit on the menu.
     */
    protected ScrollIndLayer scrollInd;
    
    
    /**
     * Construct a new PopupLayer. By default, setSupportsInput()
     * is set to true.
     */
    public ScrollablePopupLayer() {
        super((Image)null, -1);
    }


    /**
     * Construct a new PopupLayer, given a background image.
     * By default, setSupportsInput() is set to true, and so
     * is setVisible().
     */
    public ScrollablePopupLayer(Image bgImage, int bgColor) {
        super(bgImage, bgColor);
    }
    
    /**
     * Construct a new PopupLayer, given a 9 pc background image.
     * By default, setSupportsInput() is set to true, and so
     * is setVisible().
     */
    public ScrollablePopupLayer(Image[] bgImage, int bgColor) {
        super(bgImage, bgColor);
    }

    /**
     * Scrolling the contents according to the scrolling parameters.
     * @param scrollType  can be SCROLL_LINEUP, SCROLL_LINEDOWN, SCROLL_PAGEUP,
     *                SCROLL_PAGEDOWN or SCROLL_THUMBTRACK
     * @param thumbPosition only valid when scrollType is SCROLL_THUMBTRACK
     * 
     */
    public void scrollContent(int scrollType, int thumbPosition) {
    }

    public void setScrollInd(ScrollIndLayer newScrollInd) {
        if (scrollInd != newScrollInd ||
            scrollInd != null && scrollInd.scrollable != this ||
            scrollInd != null &&  scrollInd.listener != this) {
            if (scrollInd != null) {
                scrollInd.setScrollable(null);
                scrollInd.setListener(null);
            }
            if (owner != null) {
                owner.removeLayer(scrollInd);
            }
            
            scrollInd = newScrollInd;
            if (scrollInd != null) {
                scrollInd.setScrollable(this);
                scrollInd.setListener(this);
            }
        }
        updateScrollIndicator();
        updateBoundsByScrollInd();
    }

    /**
     * Update bounds of layer
     *
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        super.update(layers);
        if (scrollInd != null) {
            scrollInd.update(layers);
        }
        updateBoundsByScrollInd();
    }
    
    /**
     * Add this layer's entire area to be marked for repaint. Any pending
     * dirty regions will be cleared and the entire layer will be painted
     * on the next repaint.
     * TODO: need to be removed as soon as removeLayer algorithm
     * takes into account layers interaction
     */
    public void addDirtyRegion() {
        super.addDirtyRegion();
        if (scrollInd != null) {
            scrollInd.addDirtyRegion();
        }
    }

    /**
     * Updates the scroll indicator.
     */
    public void updateScrollIndicator() {
        if (scrollInd != null && owner != null)  {
            if (scrollInd.isVisible()) {
                owner.addLayer(scrollInd);
            } else {
                owner.removeLayer(scrollInd);
            }
        }
    }

    /**
     *  * Update bounds of layer depend on visability of scroll indicator layer
     */
    protected void updateBoundsByScrollInd() {
        if (scrollInd != null && scrollInd.isVisible() ) {
            if (ScrollIndSkin.MODE == ScrollIndResourcesConstants.MODE_BAR ) {
                bounds[W] -= scrollInd.bounds[W];
                if (ScreenSkin.RL_DIRECTION) {
                    bounds[X] += scrollInd.bounds[W];
                }
            }
            scrollInd.setBounds();
        }
    }
}

