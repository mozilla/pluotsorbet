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

import com.sun.midp.chameleon.layers.BackgroundLayer;
import javax.microedition.lcdui.*;

/**
 * This class is a top-level "window" in Chameleon. A window is
 * a collection of other layers and serves to maintain a z-ordering
 * of those layers. The window also contains the complex repaint logic
 * to support semi-transparent windows, their dirty regions, and the
 * rectangle logic to repaint other layers of the window when necessary.
 */
public abstract class CWindow {
    
    /**
     * An array holding the bounds of this window. The indices are
     * as follows:
     * 0 = window's 'x' coordinate
     * 1 = window's 'y' coordinate
     * 2 = window's width
     * 3 = window's height
     * 
     * Note: The window's x and y coordinate can only be interpreted
     * by some outside entity. For example, if some sort of manager
     * was in charge of overseeing the placement of windows on the
     * screen, it could do so by using the x and y coordinate values
     * of this window's bounds.
     */
    public int[]   bounds;
    
    /**
     * Flag indicating that at least one layer belonging to this
     * window is in need of repainting 
     */
    protected boolean dirty;

    /**
     * Ordered bi-directional list with all the layers of this window.
     */
    protected CLayerList layers;


    /** The number of dirty layers to repaint */
    protected int dirtyCount;

    /** Initial maximal number of the dirty layers */
    protected int dirtyMaxCount = 10;

    /** Layers replication to not keep the lock on painting */
    protected CLayer[] dirtyLayers = new CLayer[dirtyMaxCount];

    /**
     * Background layer of this window, should be the bottom most layer
     * of the window, can be invisible for transparent windows. 
     */
    protected BackgroundLayer bgLayer;

    /** Cache values for the clip rectangle */
    protected int cX, cY, cW, cH;
    
    /** Cache values for the graphics translation */
    protected int tranX, tranY;
    
    /** Cache value for the graphics font */
    protected Font font;
    
    /** Cache value for the graphics foreground color */
    protected int color;
    
    /**
     * Construct a new CWindow given the background image and color.
     * If the background image is null, the fill color will be used
     * instead. In the case null image and negative color are specified
     * the window is considered to be transparent.
     *
     * @param bgImage the background image to use for the window background
     * @param bgColor the background fill color in 0xrrggbbaa format to use
     *          for the window background if the background image is null.
     */
    public CWindow(Image bgImage, int bgColor, int width, int height) {
        bounds = new int[4];
        bounds[X] = 0; bounds[Y] = 0;
        bounds[W] = width;
        bounds[H] = height;

        layers = new CLayerList();

        /* Add the most bottom background layer */
        bgLayer = new BackgroundLayer(bgImage, bgColor);
        bgLayer.setBounds(0, 0, width, height);
        addLayer(bgLayer);
    }

    /** Resize window and its background according to updated skin values */
    public void resize(int width, int height) {
        bounds[W] = width;
        bounds[H] = height;
        bgLayer.setBounds(0, 0, width, height);
    }

    /**
     * Add a new CLayer to the "deck" of layers associated
     * with this CWindow. This method will sequentially add
     * layers to the window, placing subsequently added layers
     * on top of previously added layers.
     *
     * @param layer the new layer to add to this window
     * @return true if new layer was added, false otherwise 
     */
    public boolean addLayer(CLayer layer) {
        if (layer != null) {
            if (CGraphicsQ.DEBUG) {
                System.err.println("Add Layer: " + layer);
            }
            synchronized (layers) {
                if (layers.find(layer) == null) {
                    layer.owner = this;
                    layers.addLayer(layer);
                    layer.addDirtyRegion();
                    requestRepaint();
                    layer.addNotify(); 
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove a layer from this CWindow. This method will remove
     * the given layer from the "deck" of layers associated with
     * this CWindow. If successfull, this method will return true,
     * false otherwise (for example, if the layer does not belong
     * to this window).
     *
     * @param layer the layer to remove from this window
     * @return true if successful, false otherwise
     */
    public boolean removeLayer(CLayer layer) {
        synchronized (layers) {
            CLayerElement le = sweepLayer(layer);
            if (le != null) {
                if (CGraphicsQ.DEBUG) {
                    System.err.println("Remove Layer: " + layer);
                }
                layer.owner = null;
                requestRepaint();
                layers.removeLayerElement(le);
                layer.removeNotify(this); 
                return true;
            }
        }
        return false;
    }

    /**
     * Move layer to anotger location
     * @param newBounds new bounds for this layer 
     * @param x New 'x' coordinate of the layer's origin
     * @param y New 'y' coordinate of the layer's origin
     * @param w New width of the layer
     * @param h New height of the layer

     * @return true if successful, false otherwise
     */
    public boolean relocateLayer(CLayer layer, int x, int y, int w, int h) {

        if (layer != null) {
            synchronized (layers) {
                if (sweepLayer(layer) != null) {
                    if (CGraphicsQ.DEBUG) {
                        System.err.println("Relocate Layer: " + layer);
                    }
                    int[] oldBounds = { 
                                layer.bounds[X],
                                layer.bounds[Y],
                                layer.bounds[W],
                                layer.bounds[H] };

                    if (oldBounds[X] != x || oldBounds[Y] != y ||
                        oldBounds[W] != w || oldBounds[H] != h) {
                        layer.setBounds(x, y, w, h);
                        layer.addDirtyRegion();
                        requestRepaint();
                        layer.relocateNotify(oldBounds); 
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Allow this window to process key input. The type of key input
     * will be press, release, repeat, etc. The key code will identify
     * which key generated the event. This method will return true if
     * the event was processed by this window or one of its layers,
     * false otherwise.
     *
     * @param type the type of key event (press, release, repeat)
     * @param keyCode the identifier of the key which generated the event
     * @return true if this window or one of its layers processed the event
     */
    public boolean keyInput(int type, int keyCode) {
        CLayer layer;
        synchronized (layers) {
            for (CLayerElement le = layers.getTop();
                    le != null; le = le.getLower()) {
                layer = le.getLayer();
                if (layer.supportsInput &&
                        layer.keyInput(type, keyCode))
                {
                    return true;
                }
            }
        } // sync
        return false;
    }

    /**
     * Allow this window to process pointer input. The type of pointer input
     * will be press, release, drag, etc. The x and y coordinates will 
     * identify the point at which the pointer event occurred in the coordinate
     * system of this window. This window will translate the coordinates
     * appropriately for each layer contained in this window. This method will
     * return true if the event was processed by this window or one of its 
     * layers, false otherwise.
     *
     * @param type the type of pointer event (press, release, drag)
     * @param x the x coordinate of the location of the event
     * @param y the y coordinate of the location of the event
     * @return true if this window or one of its layers processed the event
     */
    public boolean pointerInput(int type, int x, int y) {
        CLayer layer;
        synchronized (layers) {
            for (CLayerElement le = layers.getTop();
                    le != null; le = le.getLower()) {
                layer = le.getLayer();
                if (layer.visible && layer.supportsInput &&
                    layer.handlePoint(x, y))
                {
                    // If the layer is visible, supports input, and
                    // contains the point of the pointer press, we translate
                    // the point into the layer's coordinate space and
                    // pass on the input
                    if (layer.pointerInput(type, x - layer.bounds[X],
                                           y - layer.bounds[Y]))
                    {
                        return true;
                    }
                }
            }
        } // sync
        return false;
    }

    /**
     * Handle input from some type of device-dependent
     * input method. This could be input from something
     * such as T9, or a phonebook lookup, etc.
     *
     * @param str the text to handle as direct input
     * @return true if this window or one of its layers processed the event
     */
    public boolean methodInput(String str) {
        CLayer layer;
        synchronized (layers) {
            for (CLayerElement le = layers.getTop();
                    le != null; le = le.getLower()) {
                layer = le.getLayer();
                if (layer.visible && layer.supportsInput &&
                    layer.methodInput(str))
                {
                    return true;
                }
            }
        } // sync
        return false;
    }

    /**
     * Request a repaint. This method MUST be overridden
     * by subclasses to provide the implementation.
     */
    public abstract void requestRepaint();


    /**
     * Check whether layer is overlapped with a higher visible layer
     * in the layer stack of the window
     *
     * @param l layer to check overlapping
     * @return true if overlapped, false otherwise
     */
    public boolean isOverlapped(CLayer l) {
        synchronized(layers) {
            CLayerElement le = layers.find(l);
            if (le != null) {
                CLayer l2;
                for (le = le.getUpper(); le != null; le = le.getUpper()) {
                    l2 = le.getLayer();
                    if (l2.isVisible() && l.intersects(l2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Subtract this layer area from an underlying dirty regions.
     * The method is designed to reduce dirty regions of a layres
     * below the opaque visible layer.
     *
     * @param le layer list element
     */
    private void cleanLowerDirtyRegions(CLayerElement le) {
        if (CGraphicsQ.DEBUG) {
            System.err.println("Clean dirty regions under opaque layer: " +
                le.getLayer());
        }

        CLayer l = le.getLayer();
        for(CLayerElement le2 = le.getLower();
                le2 != null; le2 = le2.getLower()) {
            CLayer l2 = le2.getLayer();
            if (l2.isDirty()) {
                l2.subDirtyRegion(
                    l.bounds[X] - l2.bounds[X],
                    l.bounds[Y] - l2.bounds[Y],
                    l.bounds[W], l.bounds[H]);
            }
        }
    }

    /**
     * Update dirty regions of all visible layers in the stack regarding
     * the entire area of the given layer as being dirty. The method is
     * needed to perform layer move/resize/remove opertion, since other
     * layers should be informed of changed area.
     *
     * @param layer the layer whose area should be reported as dirty to
     *   other stack layers
     * @return element of the window layers list that contains swept
     *   layer, it can be used for further layer processing
     */
    CLayerElement sweepLayer(CLayer layer) {
        if (layer != null) {
            if (CGraphicsQ.DEBUG) {
                System.err.println("Sweep Layer: " + layer);
            }
            synchronized (layers) {
                CLayerElement le = layers.find(layer);
                if (le != null) {
                    // IMPL NOTE: when a layer gets removed (or has its setVisible(false))
                    // called, the parent window must loop through all the other
                    // layers and mark them as dirty if they intersect with the
                    // layer being removed (or having its visibility changed).
                    layer.addDirtyRegion();
                    sweepAndMarkDirtyLayer(le, true);
                    return le;
                }
            }
        }
        return null;
    }

    /**
     * Propagate dirty region of the layer to other layers in the stack.
     * The method should be called for dirty layers only.
     * The dirty layer can be invisible in the case it has been
     * hidden since the previous paint.
     *
     * IMPL_NOTE: The layer been removed or set to invisible state since
     *   the previous paint is considered as "hidden", thus it should be
     *   entirely dirty and must affect other visible layers accordingly.   
     *
     * @param le dirty layer element to be propagated to other layers
     * @param hidden indicates whether the dirty layer has been hidden
     *   since the previous repaint
     * @return the highest layer element above le with modified dirty
     *   region, or null if none
     */
    private CLayerElement sweepAndMarkDirtyLayer(
            CLayerElement le, boolean hidden) {

        if (CGraphicsQ.DEBUG) {
            System.err.println("Sweep and mark dirty layer: " +
                le.getLayer());
        }

        CLayer l2;
        CLayerElement res = null;
        CLayer l = le.getLayer();

        // Prepare absolute dirty region coordinates of layer l
        int dx = l.bounds[X];
        int dy = l.bounds[Y];
        int dh, dw;
        if (l.isEmptyDirtyRegions()) {
            dw = l.bounds[W];
            dh = l.bounds[H];
        } else {
            dx += l.dirtyBounds[X];
            dy += l.dirtyBounds[Y];
            dw = l.dirtyBounds[W];
            dh = l.dirtyBounds[H];
        }

        // Sweep dirty region to upper layers
        for (CLayerElement le2 = le.getUpper();
                le2 != null; le2 = le2.getUpper()) {
            
            l2 = le2.getLayer();
            if (l2.visible) {
                if (l2.addDirtyRegion(
                        dx-l2.bounds[X], dy-l2.bounds[Y], dw, dh)) {
                    // Remember the highest changed layer
                    res = le2;
                }
            }
        }

        // Sweep non-opaque dirty region to undelying layers
        if (!l.opaque || hidden) {
            for (CLayerElement le2 = le.getLower();
                    le2 != null; le2 = le2.getLower()) {
                l2 = le2.getLayer();

                if (l2.visible) {
                    l2.addDirtyRegion(
                        dx-l2.bounds[X], dy-l2.bounds[Y], dw, dh);
                }
            }

            // A newly hidden layer should be dirty only for the first
            // succeeded paint, it should be cleaned as soon as underlying
            // layers are properly marked as dirty. 
            if (hidden) {
                l.cleanDirty();
            }
        }

        return res;
    }

    // Heuristic Explanation: Any layer that needs painting also
    // requires all layers below and above that region to be painted.
    // This is required because layers may be transparent or even
    // partially translucent - thus they require that all layers beneath
    // and above them be repainted as well.

    // To accomplish this we loop through the stack of layers from the top
    // most to the bottom most. If a layer is "dirty" (has its dirty bit
    // set), we find all layers that intersect with the dirty region and
    // we mark that layer to be painted as well. If that layer is already
    // marked to be painted and has its own dirty region, we union
    // the existing region with the new region. If that layer does
    // not have a dirty region, we simply set a new one. In the case a
    // dirty region is modified for a higher layer been processed already
    // we need to restart the loop from the modified layer.

    // After doing this initial iteration, all layers will now be
    // marked dirty where appropriate and have their individual dirty
    // regions set. We then make another iteration from the bottom
    // most layer to the top, painting the dirty region of each layer.

    /**
     * First Pass: We do sweep and mark of all layers requiring a repaint,
     * the areas behind a visible opaque layers need no repaint
     */
    private void sweepAndMarkLayers() {
        if (CGraphicsQ.DEBUG) {
            System.err.println("[Sweep and mark layers]");
        }

        CLayer l;
        CLayerElement changed;
        CLayerElement le = layers.getTop();

        while (le != null) {
            l = le.getLayer();

            if (l.visible && l.opaque) {
                cleanLowerDirtyRegions(le);
            }

            // The dirty layer can be invisible, that means it
            // has been hidden since the previous paint.
            if (l.isDirty()) {
                // In the case higher layer was changed we need to
                // restart all the algorithm from the changed layer
                changed = sweepAndMarkDirtyLayer(le, !l.visible);
                if (changed != null) {
                    if (CGraphicsQ.DEBUG) {
                        System.err.println("Restart sweep and mark: " +
                            changed.getLayer());
                    }
                    le = changed;
                    changed = null;
                    continue;
                }
            }
            // Go to next lower layer
            le = le.getLower();
        }
    }

    /**
     * Copy dirty layer references to array for further painting.
     * The copying is needed to not keep lock on layers list when
     * layers painting will happen. Dirty states of the layers are
     * cleaned after the copying. Layers painting can change layers
     * state again, but it will be served on next repaint only.
     */
    private void copyAndCleanDirtyLayers() {
        if (CGraphicsQ.DEBUG) {
            System.err.println("[Copy dirty layers]");
        }
        CLayer l;
        dirtyCount = 0;
        int layersCount = layers.size();
        // Heuristics to increase array for copied dirty layers
        if (layersCount > dirtyMaxCount) {
            dirtyMaxCount += layersCount;
            dirtyLayers = new CLayer[dirtyMaxCount];
        }
        // Copy dirty layer references and reset dirty layer states
        for (CLayerElement le = layers.getBottom();
                le != null; le = le.getUpper()) {
            l = le.getLayer();
            if (l.visible && l.isDirty()) {
                l.copyAndCleanDirtyState();
                dirtyLayers[dirtyCount++] = l;
            } else { // !(visible && dirty)
                if (CGraphicsQ.DEBUG) {
                    System.err.println("Skip Layer: " + l);
                }
            } // if
        } // for

    }

    /**
     * Second Pass: We sweep through the layers from the bottom to
     * the top and paint each one that is marked as dirty
     *
     * Note, that the painting for copied layers is done here to
     * not hold the layers lock during the painting.
     *
     * @param g The graphics object to use to paint this window.
     * @param refreshQ The custom queue which holds the set of refresh
     *        regions needing to be blitted to the screen
     */
    private void paintLayers(Graphics g, CGraphicsQ refreshQ) {
        if (CGraphicsQ.DEBUG) {
            System.err.println("[Paint dirty layers]");
        }

        for (int i = 0; i < dirtyCount; i++) {
            CLayer l = dirtyLayers[i];

            // Prepare relative dirty region coordinates
            // of the current layer
            int dx = l.dirtyBoundsCopy[X];
            int dy = l.dirtyBoundsCopy[Y];
            int dw = l.dirtyBoundsCopy[W];
            int dh = l.dirtyBoundsCopy[H];

            // Before we call into the layer to paint, we
            // translate the graphics context into the layer's
            // coordinate space
            g.translate(l.boundsCopy[X], l.boundsCopy[Y]);

            if (CGraphicsQ.DEBUG) {
                System.err.println("Painting Layer: " + l);
                System.err.println("\tClip: " +
                    dx + ", " + dy + ", " + dw + ", " + dh);
            }

            // Clip the graphics to only contain the dirty region of
            // the layer (if the dirty region isn't set, clip to the
            // whole layer contents).
            g.clipRect(dx, dy, dw, dh);
            refreshQ.queueRefresh(
                l.boundsCopy[X] + dx, l.boundsCopy[Y] + dy, dw, dh);
            l.paint(g);

            // We restore our graphics context to prepare
            // for the next layer
            g.translate(-g.getTranslateX(), -g.getTranslateY());
            g.translate(tranX, tranY);

            // We reset our clip to this window's bounds again.
            g.setClip(bounds[X], bounds[Y], bounds[W], bounds[H]);

            g.setFont(font);
            g.setColor(color);
        } // for
    }

    /**
     * Sets all visible layers to dirty state.
     * The method is needed on system events like screen rotation,
     * when generic layers system is not capabel to properly analyze
     * layers changes, e.g. of move/resize kind. It could be fixed in
     * the future and this method will be out of use. 
     */
    public void setAllDirty() {
        synchronized(layers) {
            CLayer l;
            for (CLayerElement le = layers.getBottom();
                    le != null; le = le.getUpper()) {
                l = le.getLayer();
                if (l.visible) {
                    l.addDirtyRegion();
                } // if
            } // for
        } // synchronized
    }

    /**
     * Paint this window. This method should not generally be overridden by
     * subclasses. This method carefully stores the clip, translation, and
     * color before calling into subclasses. The graphics context should be
     * translated such that it is in this window's coordinate space (0,0 is
     * the top left corner of this window). 
     *
     * @param g The graphics object to use to paint this window.
     * @param refreshQ The custom queue which holds the set of refresh
     *        regions needing to be blitted to the screen
     */
    public void paint(Graphics g, CGraphicsQ refreshQ) {
        // We reset our dirty flag first. Any layers that become
        // dirty in the duration of this method will then cause it
        // to toggle back to true for the subsequent pass.
        // IMPL NOTE: when layers start to do complex animation, there will
        // likely need to be better atomic handling of the dirty state,
        // and layers becoming dirty and getting painted
        this.dirty = false;

        // Store the clip, translate, font, color
        cX = g.getClipX();
        cY = g.getClipY();
        cW = g.getClipWidth();
        cH = g.getClipHeight();

        tranX = g.getTranslateX();
        tranY = g.getTranslateY();

        font = g.getFont();
        color = g.getColor();

        // We set the basic clip to the size of this window
        g.setClip(bounds[X], bounds[Y], bounds[W], bounds[H]);

        synchronized (layers) {
            sweepAndMarkLayers();
            copyAndCleanDirtyLayers();
        }
        paintLayers(g, refreshQ);

        // We restore the original clip. The original font, color, etc.
        // have already been restored
        g.setClip(cX, cY, cW, cH);
    }

    /**
     * Establish a background. This method will evaluate the parameters
     * and create a background which is appropriate. If the image is non-null,
     * the image will be used to create the background. If the image is null,
     * the values for the colors will be used and the background will be
     * painted in fill color instead. If the image is null, and the background
     * color is a negative value, this layer will become transparent and no
     * background will be painted.
     *
     * @param bgImage the image to use for the background tile (or null)
     * @param bgColor if the image is null, use this color as a background
     *                fill color
     */
    synchronized void setBackground(Image bgImage, int bgColor) {
          bgLayer.setBackground(bgImage, bgColor);
    }
    
    /**
     * Returns true if any layer of this window is in need of repainting.
     *
     * @return true if any layer of this window is marked as 'dirty' 
     *         and needs repainting.
     */
    public boolean isDirty() {
        return this.dirty;
    }
    
    /**
     * Mark this window as being dirty and requiring a repaint.
     */    
    public void setDirty() {
        this.dirty = true;
    }

    /** Constant used to reference the '0' index of the bounds array */
    public static final int X = 0;
    
    /** Constant used to reference the '1' index of the bounds array */
    public static final int Y = 1;
    
    /** Constant used to reference the '2' index of the bounds array */
    public static final int W = 2;
    
    /** Constant used to reference the '3' index of the bounds array */
    public static final int H = 3;
}

