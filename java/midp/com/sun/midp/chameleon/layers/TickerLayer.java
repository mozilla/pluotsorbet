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
import java.util.*;

import com.sun.midp.chameleon.skins.TickerSkin;
import com.sun.midp.chameleon.skins.SoftButtonSkin;

/**
 *
 */
public class TickerLayer extends CLayer {
    protected String text;

    protected int textLoc;
    protected int textLen;

    /**
     * A Timer which will handle firing repaints of the TickerPainter
     */
    protected Timer tickerTimer;

    /**
     * A TimerTask which will repaint the Ticker on a repeated basis
     */
    protected TickerPainter tickerPainter;

    public TickerLayer() {
        super(TickerSkin.IMAGE_BG, TickerSkin.COLOR_BG);
    }

    public void toggleAlert(boolean alertUp) {
        if (alertUp && TickerSkin.IMAGE_AU_BG != null) {
            super.setBackground(TickerSkin.IMAGE_AU_BG, 0);
        } else if (!alertUp) {
            super.setBackground(TickerSkin.IMAGE_BG, TickerSkin.COLOR_BG);
        }
    }

    protected void initialize() {
        super.initialize();

        setAnchor();
        tickerTimer = new Timer();
    }

    public void setAnchor() {
	if (owner == null) {
	    return;
	}
        bounds[X] = 0;
	bounds[W] = owner.bounds[W];
	
	bounds[H] = TickerSkin.HEIGHT;
        if (textLoc > bounds[X] + bounds[W]) {
            textLoc = bounds[X] + bounds[W];
        }
        switch (TickerSkin.ALIGN) {
	case(Graphics.TOP):
	    bounds[Y] = 0;
	    break;
            case(Graphics.BOTTOM):
	default:
	    bounds[Y] = owner.bounds[H];
	    bounds[Y] -= SoftButtonSkin.HEIGHT + bounds[H];
        }
    }

    /**
     * Set the ticker of this ticker layer.
     * @param text the text to be displayed on the ticker
     * @return * @return true if visability of layer was changed
     */
    public boolean setText(String text) {
        boolean oldVisable = super.visible;
        synchronized (this) {
            this.text = text;
            super.visible = (text != null && text.trim().length() > 0);
            textLoc = bounds[X] + bounds[W];
            textLen = (text == null) ? 0 : TickerSkin.FONT.stringWidth(text);
            setDirty();
        }
        return (oldVisable != super.visible);
    }

    public String getText() {
        return text;
    }

    protected void paintBody(Graphics g) {
        synchronized (this) {
            if (text == null) {
                return;
            }
            g.setFont(TickerSkin.FONT);
            if (TickerSkin.COLOR_FG_SHD != TickerSkin.COLOR_FG) {
                int x = textLoc;
                int y = TickerSkin.TEXT_ANCHOR_Y;
                switch (TickerSkin.TEXT_SHD_ALIGN) {
                    case(Graphics.TOP | Graphics.LEFT):
                        x -= 1;
                        y -= 1;
                        break;
                    case(Graphics.TOP | Graphics.RIGHT):
                        x += 1;
                        y -= 1;
                        break;
                    case(Graphics.BOTTOM | Graphics.LEFT):
                        x -= 1;
                        y += 1;
                        break;
                    case(Graphics.BOTTOM | Graphics.RIGHT):
                    default:
                        x += 1;
                        y += 1;
                        break;
                }
                g.setColor(TickerSkin.COLOR_FG_SHD);
                g.drawString(text, x, y, Graphics.TOP | TickerSkin.DIRECTION);
            }

            g.setColor(TickerSkin.COLOR_FG);
            g.drawString(text, textLoc, TickerSkin.TEXT_ANCHOR_Y,
                    Graphics.TOP | TickerSkin.DIRECTION);

            if (tickerPainter == null) {
                startTicker();
            }
        }
    }

    public void startTicker() {
        if (!visible) {
            return;
        }

        stopTicker();
        tickerPainter = new TickerPainter();
        tickerTimer.schedule(tickerPainter, 0, TickerSkin.RATE);
    }

    /**
     * Stop the ticking of the ticker.
     */
    public void stopTicker() {
        if (tickerPainter == null) {
            return;
        }
        tickerPainter.cancel();
        tickerPainter = null;
    }

    /**
     * Update bounds of layer
     *
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        super.update(layers);
        setAnchor();
    }

    /**
     * A special helper class to repaint the Ticker
     * if one has been set
     */
    private class TickerPainter extends TimerTask {
        /**
         * Repaint the ticker area of this Screen
         */
        public final void run() {
            if (!visible) {
                stopTicker();
            } else {
                requestRepaint();
                switch (TickerSkin.DIRECTION) {
                    case Graphics.RIGHT:
                        textLoc += TickerSkin.SPEED;
                        if (textLoc >= (bounds[W] + textLen)) {
                            textLoc = -(textLen);
                        }
                        break;
                    case Graphics.LEFT:
                    default:
                        textLoc -= TickerSkin.SPEED;
                        if (textLoc <= -(textLen)) {
                            textLoc = bounds[X] + bounds[W];
                        }
                }
            }
        }
    }

}


