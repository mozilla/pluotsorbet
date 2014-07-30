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

import com.sun.midp.lcdui.Text;
import com.sun.midp.chameleon.CLayer;
import javax.microedition.lcdui.*;
import com.sun.midp.chameleon.MIDPWindow;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.TitleSkin;
import com.sun.midp.chameleon.skins.TickerSkin;

/**
 * A basic "title" layer. This layer holds a screens title information.
 */
public class TitleLayer extends CLayer {

    /** The text to draw as the title */
    protected String title;

    /** This is the anchor position for the title */
    protected int titlex, titley, titlew, titleh;
    
    /**
     * Construct a new TitleLayer. This will construct a new layer using
     * the background image and color settings as defined in the TitleSkin.
     */
    public TitleLayer() {
        super(TitleSkin.IMAGE_BG, TitleSkin.COLOR_BG);
    }

    /**
     * The TitleLayer overrides the initialize method in order to locate
     * the title layer in the window at the position and dimensions defined
     * in the TitleSkin.
     */
    protected void initialize() {
        super.initialize();

       setAnchor();
    }

    /**
     * Sets the anchor constraints for rendering operation.
     */
    public void setAnchor() {
	if (owner == null) {
	    return;
	}
        bounds[X] = 0;
        bounds[Y] = 0;

	bounds[W] = owner.bounds[W];
        bounds[H] = TitleSkin.HEIGHT;
        titlex = 0;
    }


    /**
     * Set the title of this title layer.
     *
     * @param title the text to draw as the title
     * @return true if visability of layer was changed
     */
    public boolean setTitle(String title) {
        boolean oldVisible = this.visible;
        this.title = title;

        setDirty();
        this.visible = (title != null);

        // force a re-calc of the text anchor location
        titlex = 0;
        return (oldVisible != this.visible);
    }

    /**
     * Get the title of this layer.
     *
     * @return the title of this layer, or null if this TitleLayer has no
     *         title set.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Draw the body of this layer. This method uses settings in the
     * TitleSkin to render the title at the correct location using
     * specified font and color values.
     *
     * @param g the Graphics to draw to
     */
    protected void paintBody(Graphics g) {
        String title = this.title;
        if (title == null) {
            return;
        }

        if (titlex == 0) {
            // anchor isn't set yet
            titlew = TitleSkin.FONT.stringWidth(title);
            if (titlew > (bounds[W] - (2 * TitleSkin.MARGIN))) {
                titlew = bounds[W] - (2 * TitleSkin.MARGIN);
            }            
            
            switch (TitleSkin.TEXT_ALIGN_X) {
                case Graphics.HCENTER:
                    titlex = (bounds[W] - titlew) / 2;
                    break;
                case Graphics.RIGHT:
                    titlex = 
                        (bounds[W] - TitleSkin.MARGIN - titlew);
                    break;
                case Graphics.LEFT:
                    titlex = TitleSkin.MARGIN;
                    break;
                default:
                    if (ScreenSkin.RL_DIRECTION) {
                        titlex =
                        (bounds[W] - TitleSkin.MARGIN - titlew);
                    } else {
                        titlex = TitleSkin.MARGIN;    
                    }
                    break;
            }
            
            // We center the title vertically in the
            // space provided
            titleh = TitleSkin.FONT.getHeight();
            if (titleh < bounds[H]) {
                titley = (bounds[H] - titleh) / 2;
            } else {
                titley = 0;
            }
        }

        g.translate(titlex, titley);
        Text.drawTruncStringShadowed(g, title, TitleSkin.FONT, TitleSkin.COLOR_FG,
                TitleSkin.COLOR_FG_SHD, TitleSkin.TEXT_SHD_ALIGN,
                titlew);
        g.translate(-titlex, -titley);
    }

    /**
     * Update bounds of layer
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        super.update(layers);
        setAnchor();
        CLayer t = layers[MIDPWindow.TICKER_LAYER];
        if (t != null && t.isVisible() && TickerSkin.ALIGN == Graphics.TOP) {
            bounds[Y] = t.bounds[Y] + t.bounds[H];
        }

    }

}
