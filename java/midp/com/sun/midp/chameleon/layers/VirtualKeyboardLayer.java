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

import com.sun.midp.lcdui.*;
import com.sun.midp.chameleon.skins.VirtualKeyboardSkin;
import com.sun.midp.chameleon.CLayer;
import com.sun.midp.chameleon.MIDPWindow;
import javax.microedition.lcdui.*;

/**
 * This is a popup layer 
 */
public class VirtualKeyboardLayer extends PopupLayer implements VirtualKeyboardListener {

    /** Instance of current displayable */
    private VirtualKeyListener listener;

    /** the instance of the virtual keyboard */
    private static VirtualKeyboard vk = null;

    /**
     * if virtualKeyboardVisible = true Virtual Keyboard Layer is visible
     */
    private boolean virtualKeyboardVisible;

    /**
     * Default width of virtual keyboard that is used for calculation of key buttons position
     */
    private int defaultWidth;

    /**
     * Create an instance of KeyboardLayer
     */
    public VirtualKeyboardLayer() {
        super(VirtualKeyboardSkin.BG, VirtualKeyboardSkin.COLOR_BG);
    }

    /**
     * Method return true if current virtual keybpard implementation supports java virtual keyboard
     * @return status of java virtual keyboard support
     */
    public static boolean isSupportJavaKeyboard() {
        return VirtualKeyboard.isSupportJavaKeyboard();
    }

    /**
     * Return standalone instance of VirtualKeyboardLayer
     * @return
     */
    public void init() {

        if (VirtualKeyboard.isSupportJavaKeyboard() && vk == null) {
            vk = VirtualKeyboard.getVirtualKeyboard(this);
         }
    }

    /**
     * Set initial keyboard mode depend on current listener
     * @param listener - current layer 
     */
    public void setVirtualKeyboardLayerListener(VirtualKeyListener listener) {
        this.listener = listener;
    }

    /**
     * Set new keyboard type
     * @param keyboard
     */
    public void setKeyboardType(String keyboard) {
        vk.changeKeyboad(keyboard);
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
        if (vk != null && vk.isSupportJavaKeyboard()) {
            this.visible = visible;
        } else {
            this.setVisible(false);
        }
    }

    /**
     * Sets the anchor constants for rendering operation.
     */
    private void setAnchor() {
        if (owner == null) {
            return;
        }
        bounds[W] = owner.bounds[W];
        bounds[H] = VirtualKeyboardSkin.HEIGHT;
        bounds[X] = (owner.bounds[W] - bounds[W]) >> 1;
        bounds[Y] = owner.bounds[H] - bounds[H];

        if (defaultWidth == 0) {
            defaultWidth = bounds[W];
        }
    }

    /**
     * Utility method to determine if this layer wanna handle
     * the given point. PTI layer handles the point if it
     * lies within the bounds of this layer.  The point should be in
     * the coordinate space of this layer's containing CWindow.
     *
     * @param x the "x" coordinate of the point
     * @param y the "y" coordinate of the point
     * @return true if the coordinate lies in the bounds of this layer
     */
    public boolean handlePoint(int x, int y) {
        return containsPoint(x, y);
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
        return vk.pointerInput(type,x,y);
    }

    /**
     * Paints the body of the popup layer.
     *
     * @param g The graphics context to paint to
     */
    public void paintBody(Graphics g) {
        vk.paint(g);
    }
    
    /**
     * Update bounds of layer
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        super.update(layers);

        if (owner == null) {
            return;
        }
        if (visible) {
            setAnchor();
            int screenBounds = owner.bounds[H];

            bounds[Y] = layers[MIDPWindow.TITLE_LAYER].bounds[Y];
            if (layers[MIDPWindow.TITLE_LAYER].isVisible()) {
                bounds[Y] += layers[MIDPWindow.TITLE_LAYER].bounds[H];
                screenBounds -= layers[MIDPWindow.TITLE_LAYER].bounds[H];
            }
            if (layers[MIDPWindow.TICKER_LAYER].isVisible()) {
                screenBounds -= layers[MIDPWindow.TICKER_LAYER].bounds[H];
            }
            if (layers[MIDPWindow.BTN_LAYER].isVisible()) {
                screenBounds -= layers[MIDPWindow.BTN_LAYER].bounds[H];
            }
            bounds[H] = (int) (VirtualKeyboardSkin.COEFFICIENT * screenBounds);
            bounds[H] = (bounds[H] > VirtualKeyboardSkin.HEIGHT) ?
                    VirtualKeyboardSkin.HEIGHT : bounds[H];
            bounds[Y] += (screenBounds - bounds[H]);
            double khrinkX = ((double) bounds[W]) / defaultWidth;
            double kshrinkY = ((double) bounds[H]) / VirtualKeyboardSkin.HEIGHT;
            this.vk.resize(khrinkX, kshrinkY);
        }

    }

    // ********** package private *********** //

    /** Indicates if this popup layer is shown (true) or hidden (false). */
    boolean open = false;

    /**
     * key press callback
     * MIDlet that wants the receive events from the virtual
     * keyboard needs to implement this interface, and register as
     * a listener.
     * @param keyCode char selected by the user from the virtual keyboard
     *
     */
    public void virtualKeyPressed(int keyCode) {

        if (listener != null) {
            listener.processKeyPressed(keyCode);
        }
    }

    /**
     * key release callback
     * MIDlet that wants the receive events from the virtual
     * keyboard needs to implement this interface, and register as
     * a listener.
     * @param keyCode char selected by the user from the virtual keyboard
     *
     */
    public void virtualKeyReleased(int keyCode) {
        if (listener != null) {
            listener.processKeyReleased(keyCode);
        }
    }

    /**
     * repaint the virtual keyboard.
     */
    public void repaintVirtualKeyboard() {
        requestRepaint();
    }

    public boolean isVirtualKeyboardVisible() {
        return virtualKeyboardVisible;
    }

    public void setVirtualKeyboardVisible(boolean visible) {
        virtualKeyboardVisible = visible;
    }

}
