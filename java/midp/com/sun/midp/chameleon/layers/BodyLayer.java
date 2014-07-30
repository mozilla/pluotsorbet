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
import com.sun.midp.chameleon.skins.ScrollIndSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.resources.ScrollIndResourcesConstants;

/**
 * Basic layer containing the application area of the display. This layer
 * contains the current Displayable contents, such as a Form or Canvas.
 */
public class BodyLayer extends CLayer
    implements ScrollListener {

    /**
     * The scroll indicator layer to notify of scroll settings
     * in case not all content can fit on the menu.
     */
    protected ScrollIndLayer scrollInd;

    /** Tunnel instance to call Display methods */
    ChamDisplayTunnel tunnel;

    /**
     * Create a new BodyLayer.
     *
     * @param tunnel BodyLayer needs a "tunnel" class to cross the package
     *        protection boundary and access methods inside the 
     *        javax.microedition.lcdui package
     */
    public BodyLayer(ChamDisplayTunnel tunnel) {
        this((Image)null, -1, tunnel);
    }

    /**
     * Create a new BodyLayer with the given background image or color.
     * If the image is null, the color will be used.
     *
     * @param bgImage a background image array to use to render the
     *        background of this layer
     * @param bgColor a solid background fill color to use if the image
     *        background is null
     * @param tunnel BodyLayer needs a "tunnel" class to cross the package
     *        protection boundary and access methods inside the 
     *        javax.microedition.lcdui package
     */
    public BodyLayer(Image bgImage[], int bgColor, ChamDisplayTunnel tunnel)
    {
        super(bgImage, bgColor);
        this.tunnel = tunnel;
        this.visible = false;

        setScrollInd(ScrollIndLayer.getInstance(ScrollIndSkin.MODE));
    }
    
    /**
     * Create a new BodyLayer with the given background image or color.
     * If the image is null, the color will be used.
     *
     * @param bgImage a single background image to use to render the
     *        background of this layer
     * @param bgColor a solid background fill color to use if the image
     *        background is null
     * @param tunnel BodyLayer needs a "tunnel" class to cross the package
     *        protection boundary and access methods inside the 
     *        javax.microedition.lcdui package
     */
    public BodyLayer(Image bgImage, int bgColor, ChamDisplayTunnel tunnel) 
    {
        super(bgImage, bgColor);
        this.tunnel = tunnel;
        this.visible = false;
        setScrollInd(ScrollIndLayer.getInstance(ScrollIndSkin.MODE));
    }

    /**
     * Toggle the visibility state of this layer within its containing
     * window.
     *
     * @param visible If true, this layer will be painted as part of its
     *                containing window, as well as receive events if it
     *                supports input.
     */
    public void setVisible(boolean visible) {
        boolean oldVis = this.visible;
        super.setVisible(visible);
        if (oldVis != visible) {
            if (scrollInd != null && !visible) {
                scrollInd.setVisible(visible);
            } else {
                updateScrollIndicator();
            }
        }
    }

    /**
     * Prepare Graphics context for optimized painting of the Canvas
     * holded by this BodyLayer instance. Bounds and dirty region of
     * the layer are used to set Graphics clip area and translation.
     * 
     * @param g Graphics context to prepare
     */
    public void setGraphicsForCanvas(Graphics g) {
        // NOTE: note the two different orders of clip and translate
        // below. That is because the layer's bounds are stored in
        // the coordinate space of the window. But its internal dirty
        // region is stored in the coordinate space of the layer itself.
        // Thus, for the first one, the clip can be set and then translated,
        // but in the second case, the translate must be done first and then
        // the clip set.
        if (isDirty()) {
            if (isEmptyDirtyRegions()) {
                g.setClip(bounds[X], bounds[Y], bounds[W], bounds[H]);
                g.translate(bounds[X], bounds[Y]);
            } else {
                g.translate(bounds[X], bounds[Y]);
                g.setClip(dirtyBounds[X], dirtyBounds[Y],
                    dirtyBounds[W], dirtyBounds[H]);
            }
            cleanDirty();
        } else {
            // NOTE: the layer can be not dirty, e.g. in the case an empty
            // area was requested for repaint, set empty clip area then.
            g.translate(bounds[X], bounds[Y]);
            g.setClip(0, 0, 0, 0);
        }
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
     * Mark this layer as being dirty. By default, this would also mark the
     * containing window (if there is one) as being dirty as well. However,
     * this parent class behavior is overridden in BodyLayer so as to not 
     * mark the containing window and therefor not require a full
     * Chameleon repaint when only the application area needs updating.
     */    
    public void setDirty() {
        setDirtyButNotNotifyOwner();
    }
    
    /**
     * Scrolling the contents according to the scrolling parameters.
     * @param scrollType  can be SCROLL_LINEUP, SCROLL_LINEDOWN, SCROLL_PAGEUP,
     *                SCROLL_PAGEDOWN or SCROLL_THUMBTRACK
     * @param thumbPosition only valid when scrollType is SCROLL_THUMBTRACK
     * 
     */
    public void scrollContent(int scrollType, int thumbPosition) {
        tunnel.callScrollContent(scrollType, thumbPosition);
    }

    /**
     * Called by CWindow to notify the layer that is has been 
     * added to the active stack. 
     */
    public void addNotify() {
        if (scrollInd != null && owner != null) {
            if (owner.addLayer(scrollInd)) {
                updateScrollIndicator();    
            }    
        }
    }

    /**
     * Called by CWindow to notify the layer that is has been 
     * removed from the active stack. 
     * @param owner an instance of CWindow this layer has been removed from 
     */
    public void removeNotify(CWindow owner) {
        if (scrollInd != null && owner != null) {
            owner.removeLayer(scrollInd);
        }
     }


    public void setScrollInd(ScrollIndLayer newScrollInd) {
        if (scrollInd != newScrollInd ||
                scrollInd != null && scrollInd.scrollable != this ||
                scrollInd != null && scrollInd.listener != this) {
            if (scrollInd != null) {
                boolean vis = scrollInd.isVisible();
                scrollInd.setScrollable(null);
                scrollInd.setListener(null);

                if (owner != null) {
                    if (owner.removeLayer(scrollInd) &&
                            ScrollIndSkin.MODE == ScrollIndResourcesConstants.MODE_BAR &&
                            vis) {
                        bounds[W] += scrollInd.bounds[W];
                        if (ScreenSkin.RL_DIRECTION) {
                            bounds[X] -= scrollInd.bounds[W];
                        }

                        addDirtyRegion();
                    }
                }
            }

            scrollInd = newScrollInd;
            if (scrollInd != null) {
                scrollInd.setScrollable(this);
                scrollInd.setListener(this);

                if (owner != null) {
                    owner.addLayer(scrollInd);
                }
            }
        }
        updateScrollIndicator();
    }

    /**
     * Updates the scroll indicator.
     */
    public void updateScrollIndicator() {
        tunnel.updateScrollIndicator();
    }

    /**
     * Set the current vertical scroll position and proportion.
     *
     * @param scrollPosition vertical scroll position.
     * @param scrollProportion vertical scroll proportion.
     * @return true if set vertical scroll occures
     */
    public boolean setVerticalScroll(int scrollPosition, int scrollProportion) {
        if (scrollInd != null) {
            boolean wasVisible = scrollInd.isVisible();
            scrollInd.setVerticalScroll(scrollPosition, scrollProportion);
            boolean scrollVisible = scrollInd.isVisible();

            if (wasVisible != scrollVisible) {
                if (owner != null) {
                    bounds[X] = 0;
                    bounds[W] = owner.bounds[W];
                    updateBoundsByScrollInd();
                }
                return true;
            }
        }
        return false;
    }


    /**
     * Paint the contents of this layer. This method is overridden from
     * the parent class to use the package tunnel to call back into the
     * javax.microedition.lcdui package and cause the current Displayable
     * to paint its contents into the body of this layer.
     *
     * @param g the Graphics to paint to
     */
    protected void paintBody(Graphics g) {
        if (tunnel != null) {
            tunnel.callPaint(g);
        }
    }

    /**
     * Update bounds of layer
     *
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        super.update(layers);
        if (owner == null) {
            return;
        }
        bounds[X] = 0;
        bounds[W] = owner.bounds[W];
        bounds[H] = owner.bounds[H];
        CLayer l = layers[MIDPWindow.PTI_LAYER];
        if (l != null && l.isVisible()) {
            bounds[H] -= l.bounds[H];
        }
        l = layers[MIDPWindow.KEYBOARD_LAYER];
        if (l != null && l.isVisible()) {
            bounds[H] -= l.bounds[H];
        }
        l = layers[MIDPWindow.TITLE_LAYER];
        if (l != null) {
            bounds[Y] = l.bounds[Y];
            if (l.isVisible()) {
                bounds[Y] += l.bounds[H];
                bounds[H] -= l.bounds[H];
            }
        }
        l = layers[MIDPWindow.TICKER_LAYER];
        if (l != null && l.isVisible()) {
            bounds[H] -= l.bounds[H];
        }
        l = layers[MIDPWindow.BTN_LAYER];
        if (l != null && l.isVisible()) {
            bounds[H] -= l.bounds[H];
        }

        if (scrollInd != null) {
            scrollInd.update(layers);
        }
        updateBoundsByScrollInd();
    }


    /**
     *  * Update bounds of layer depend on visability of scroll indicator layer
     */
    public void updateBoundsByScrollInd() {
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

