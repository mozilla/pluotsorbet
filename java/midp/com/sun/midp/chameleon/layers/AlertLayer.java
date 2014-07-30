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

import javax.microedition.lcdui.*;

import com.sun.midp.chameleon.ChamDisplayTunnel;
import com.sun.midp.chameleon.CLayer;

import com.sun.midp.chameleon.skins.*;
import com.sun.midp.chameleon.skins.resources.ScrollIndResourcesConstants;

/**
 * AlertLayer 
 */
public class AlertLayer extends BodyLayer {

    /**
     * The AlertLayer constructor. Initializes background image if 
     * there is one set in AlertSkin.
     * @param tunnel - The ChamDisplayTunnel to do paint calls
     */
    public AlertLayer(ChamDisplayTunnel tunnel) {
        super(AlertSkin.IMAGE_BG, AlertSkin.COLOR_BG, tunnel);
        setVisible(false);
    }

    /**
     * Sets content to be displayed in the Alert Layer.
     * This AlertLayer will be made visible if  <code>alertVisible</code>
     * is true and will be hidden - otherwise.
     * @param alertVisible - true if the AlertLayer should be shown,
     *                       and false - otherwise
     * @param alert - The <code>Alert</code> instance that is currently
     *                visible
     * @param height the preferred height for the Alert. This is accepted
     *               as long as it is less than AlertSkin.HEIGHT
     */
    public void setAlert(boolean alertVisible, Alert alert, int height) {
        this.alert = alert;
        setDirty();
        setVisible(alertVisible);
    }
    
    /**
     * Toggle the visibility state of Alert layer within its containing
     * window.
     *
     * @param visible if alert should be visible, false otherwise
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setSupportsInput(visible);
    }


    /**
     * Initialize the bounds of this AlertLayer. Overrides
     * initialize in superclasses. The dimensions of the
     * specific AlertLayer are specified in AlertSkin.
     *
     * The X and Y coordinates represent the upper left position
     * of this CLayer in the physical display's coordinate space.
     */
    protected void initialize() {
        super.initialize();
        setAnchor();
    }

    /**
     * Align alert depend on skin
     */
    public void setAnchor() {
	if (owner == null)
	    return;
	if (AlertSkin.WIDTH == -1) {
            AlertSkin.WIDTH = (int)(.95 * owner.bounds[W]);
        }

        if (AlertSkin.HEIGHT == -1) {
            AlertSkin.HEIGHT = (int)(.75 * owner.bounds[H]);
        }

        bounds[W] = AlertSkin.WIDTH;
        bounds[H] = AlertSkin.HEIGHT;

        switch (AlertSkin.ALIGN_X) {
        case Graphics.LEFT:
            bounds[X] = 0;
            break;
        case Graphics.RIGHT:
            bounds[X] = owner.bounds[W] - bounds[W];
            break;
        case Graphics.HCENTER:
        default:
	    bounds[X] = (owner.bounds[W] - bounds[W]) >> 1;
            break;
        }
        switch (AlertSkin.ALIGN_Y) {
        case Graphics.TOP:
            bounds[Y] = 0;
            if (alert != null &&
                alert.getTicker() != null &&
                TickerSkin.ALIGN == Graphics.TOP) {
                bounds[Y] += TickerSkin.HEIGHT;
            } 
            break;
        case Graphics.VCENTER:
	    bounds[Y] = owner.bounds[H] - SoftButtonSkin.HEIGHT - bounds[H];
            if (alert != null && alert.getTicker() != null) {
                bounds[Y] -= TickerSkin.HEIGHT;
            }
            bounds[Y] >>= 1;
            break;
        case Graphics.BOTTOM:
        default:
	    bounds[Y] = owner.bounds[H] - SoftButtonSkin.HEIGHT -
		    bounds[H];
            if (alert != null &&
                alert.getTicker() != null &&
                TickerSkin.ALIGN != Graphics.TOP) {
                bounds[Y] -= TickerSkin.HEIGHT;
            }
            break;
        }
        updateBoundsByScrollInd();

    }

    /** The Alert instance which content is currently visible */
    private Alert alert;

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
     *  * Update bounds of layer depend on visability of scroll indicator layer
     */
    public void updateBoundsByScrollInd() {
        bounds[W] = AlertSkin.WIDTH;
        if (owner != null) {
            switch (AlertSkin.ALIGN_X) {
                case Graphics.LEFT:
                    bounds[X] = 0;
                    break;
                case Graphics.RIGHT:
                    bounds[X] = owner.bounds[W] - bounds[W];
                    break;
                case Graphics.HCENTER:
                default:
                    bounds[X] = (owner.bounds[W] - bounds[W]) >> 1;
                    break;
            }
        }
        super.updateBoundsByScrollInd();
    }
}

