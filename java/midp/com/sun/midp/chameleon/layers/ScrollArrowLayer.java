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

import com.sun.midp.chameleon.skins.SoftButtonSkin;

import com.sun.midp.chameleon.*;
import javax.microedition.lcdui.*;
import com.sun.midp.chameleon.skins.ScrollIndSkin;
import com.sun.midp.lcdui.EventConstants;

/**
 * A ScrollArrowLayer is a region of the display used for showing scroll indicator
 * status arrows.
 *
 */
public class ScrollArrowLayer extends ScrollIndLayer {
    /**
     * True if up arrow is visible
     */
    protected boolean upViz;
    
    /**
     * True if down arrow is visible
     */
    protected boolean downViz;
    
    /**
     * Construct a new ScrollIndLayer, visible, but transparent :)
     */
    public ScrollArrowLayer(CLayer layer) {
        this(layer, null);
    }

    /**
     * Additional constructor.
     * @param layer the scrollable controlling the scrolling layer 
     * @param listener the scrolling listener
     */
    public ScrollArrowLayer(CLayer layer, ScrollListener listener) {
        super(layer, listener);
    }

    /**
     * Called by MIDPWindow to initialize this layer
     */
    protected void initialize() {
        super.initialize();
        setBounds();
    }

    /**
     * Calculate layer bounds depending on the scrollable
     */
    public void setBounds() {
	if (owner == null) {
	    return;
	}
	
        bounds[H] = SoftButtonSkin.HEIGHT;
        if (ScrollIndSkin.IMAGE_UP != null) {
            bounds[W] = ScrollIndSkin.IMAGE_UP.getWidth();
            bounds[H] = (2 * ScrollIndSkin.IMAGE_UP.getHeight());
            bounds[Y] = (SoftButtonSkin.HEIGHT - bounds[H]) / 3;
            bounds[H] += bounds[Y];
	    bounds[Y] = owner.bounds[H] - SoftButtonSkin.HEIGHT +
                bounds[Y];
	} else {
	    bounds[W] = ScrollIndSkin.WIDTH;
            bounds[Y] = 3;
	}
	bounds[X] = (owner.bounds[W] - bounds[W]) / 2;
    }

    /**
     * Set the current vertical scroll position and proportion.
     *
     * @param scrollPosition vertical scroll position.
     * @param scrollProportion vertical scroll proportion.
     */
    public void setVerticalScroll(int scrollPosition, 
                                  int scrollProportion) {	
        boolean up = upViz;
        boolean dn = downViz;
        if ( (scrollable != null) &&
             (scrollProportion < 100)) {
            upViz = (scrollPosition > 0);
            downViz = (scrollPosition < 100);
        } else {
            upViz = downViz = false;
        }
        setVisible(upViz || downViz);
        if (up != upViz || dn != downViz) {
            requestRepaint();
        }
    }
    
    /**
     * Paint the scroll indicator.  The indicator arrows may be appear 
     * individually
     * or together, and may vary in appearance based on whether they appear
     * in the normalsoft button region or an alert's softbutton region.
     * The visible state is based on the state of the <code>alertMode</code>, 
     * <code>upViz</code>, and <code>downViz</code> variables set by the
     * <code>setVerticalScroll</code> method.
     * @param g the graphics context to paint in
     */
    protected void paintBody(Graphics g) {
        if (upViz) {
            Image i = ScrollIndSkin.IMAGE_UP;
            if (alertMode && ScrollIndSkin.IMAGE_AU_UP != null) {
                i = ScrollIndSkin.IMAGE_AU_UP;
            }
            if (i != null) {
                g.drawImage(i, 0, 0, Graphics.LEFT| Graphics.TOP);
            }
        }
        
        if (downViz) {
            Image i = ScrollIndSkin.IMAGE_DN;
            if (alertMode && ScrollIndSkin.IMAGE_AU_DN != null) {
                i = ScrollIndSkin.IMAGE_AU_DN;
            }
            if (i != null) {
                g.drawImage(i, 0, 
                    bounds[H] - ScrollIndSkin.IMAGE_DN.getHeight(),
                    Graphics.LEFT | Graphics.TOP);
            }
        }
    }
    

    /**
     * Handle input from a pen tap. Parameters describe
     * the type of pen event and the x,y location in the
     * layer at which the event occurred. Important : the
     * x,y location of the pen tap will already be translated
     * into the coordinate space of the layer.
     *
     * @param type the type of pen event
     * @param x the x coordinate of the event
     * @param y the y coordinate of the event
     */
    public boolean pointerInput(int type, int x, int y) {               
        switch(type) {
        case EventConstants.PRESSED:
            // case EventConstants.HOLD:
            // no action for tap-and-hold in scrollbar
            // cancel timer for any press.
            
            int scrollType = getScrollType(x, y);
            if (scrollType == SCROLL_LINEDOWN ||
                scrollType ==  SCROLL_LINEUP) {
                listener.scrollContent(scrollType, 0);
            }
            break;
        case EventConstants.RELEASED:
            // do nothing 
            break;
        default:
            break;
        }
        
        /* we should process all of the pointer event inside scroll layer
           and don't pass it to underlying layer */
        return true;
    }

    /**
     * Determine he scroll type basing on the pointer coordinates
     * @param x - x coordinate 
     * @param y - y coordinate
     * @return the scroll type
     * The possible types of scrolling are 
     * @see #SCROLL_NONE
     * @see #SCROLL_LINEUP
     * @see #SCROLL_LINEDOWN
     */ 
    private int getScrollType(int x, int y) {
        int ret = SCROLL_NONE;
        if (x >= 0 && x <= bounds[W] &&
            y >= 0 && y <= bounds[H]) {
            if (upViz && y < bounds[H] / 2) {
                ret = SCROLL_LINEUP;
            } else if (downViz && y > bounds[H] / 2) {
                ret = SCROLL_LINEDOWN;
            }
        } 
        return ret;
    }


    /**
     * Set new scrollable 
     * @param layer new scrollable controlling the scrolling layer
     * @return true if the scrollable is changed, false - otherwise
     */
    public boolean setScrollable(CLayer layer) {
        boolean ret = super.setScrollable(layer);
        if (ret) {
            alertMode |= scrollable instanceof MenuLayer;
        }
        upViz = downViz = false;
        return ret;
    }

    /**
     * Update bounds of layer
     *
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        super.update(layers);
        setBounds();
    }

}
