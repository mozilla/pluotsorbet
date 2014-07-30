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

package javax.microedition.lcdui.game;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Display;


/**
 * The LayerManager manages a series of Layers.  The LayerManager simplifies
 * the process of rendering the Layers that have been added to it by
 * automatically rendering the correct regions of each Layer in the
 * appropriate order.
 * <p>
 * The LayerManager maintains an ordered list to which Layers can be appended,
 * inserted and removed.  A Layer's index correlates to its z-order; the layer
 * at index 0 is closest to the user while a the Layer with the highest index
 * is furthest away from the user.  The indices are always contiguous; that
 * is, if a Layer is removed, the indices of subsequent Layers will be
 * adjusted to maintain continuity.
 * <p>
 * The LayerManager class provides several features that control how the
 * game's Layers are rendered on the screen.
 * <p>
 * The <em>view window</em> controls the size of the visible region and its
 * position relative to the LayerManager's coordinate system.  Changing the
 * position of the view window enables effects such as scrolling or panning
 * the user's view.  For example, to scroll to the right, simply move the view
 * window's location to the right.  The size of the view window controls how
 * large the user's view will be, and is usually fixed at a size that is
 * appropriate for the device's screen.
 * <P>
 * In this example, the view window is set to 85 x 85 pixels and is located at
 * (52, 11) in the LayerManager's coordinate system.  The Layers appear at
 * their respective positions relative to the LayerManager's origin.
 * <br>
 * <center><img src="doc-files/viewWindow.gif" width=558 height=292
 * ALT="Specifying the View Window"></center>
 * <br>
 * <p>
 * The {@link #paint(Graphics, int, int)} method includes an (x,y) location
 * that controls where the view window is rendered relative to the screen.
 * Changing these parameters does not change the contents of the view window,
 * it simply changes the location where the view window is drawn.  Note that
 * this location is relative to the origin of the Graphics object, and thus
 * it is subject to the translation attributes of the Graphics object.
 * <P>
 * For example, if a game uses the top of the screen to display the current
 * score, the view window may be rendered at (17, 17) to provide enough space
 * for the score.
 * <br>
 * <center><img src="doc-files/drawWindow.gif" width=321 height=324
 * ALT="Drawing the View Window"></center>
 * <br>
 * <p>
 **/
public class LayerManager {

    /**
     * Creates a new LayerManager.
     */
    public LayerManager() {
        setViewWindow(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Appends a Layer to this LayerManager.  The Layer is appended to the
     * list of existing Layers such that it has the highest index (i.e. it
     * is furthest away from the user).  The Layer is first removed
     * from this LayerManager if it has already been added.
     *
     * @see #insert(Layer, int)
     * @see #remove(Layer)
     * @param l the <code>Layer</code> to be added
     * @throws NullPointerException if the <code>Layer</code> is
     * <code>null</code>
     */
    public void append(Layer l) {
	// remove the Layer if it is already present
	// will throw NullPointerException if the Layer is null
	removeImpl(l);
        addImpl(l, nlayers);
    }

    /**
     * Inserts a new Layer in this LayerManager at the specified index.
     * The Layer is first removed from this LayerManager if it has already
     * been added.
     * @see #append(Layer)
     * @see #remove(Layer)
     * @param l the <code>Layer</code> to be inserted
     * @param index the index at which the new <code>Layer</code> is
     * to be inserted
     * @throws NullPointerException if the <code>Layer</code> is
     * <code>null</code>
     * @throws IndexOutOfBoundsException if the index is less than
     * <code>0</code> or
     * greater than the number of Layers already added to the this or
     * if the index is greater than the number of Layers already added
     * to this LayerManager minus one and the Layer has already been added
     * to this LayerManager
     * <code>LayerManager</code>
     */
    public void insert(Layer l, int index) {
	/* Check for correct arguments:  index in bounds	     */
	if ((index < 0) || (index > nlayers) || (exist(l) && (index >= nlayers))) {
	    throw new IndexOutOfBoundsException();
	}

    // if the Layer is already present
	// remove it
	// will throw NullPointerException if the Layer is null
	removeImpl(l);
	// insert it at the specified index
	addImpl(l, index);
    }

    /**
     * Gets the Layer with the specified index.
     * @param index the index of the desired Layer
     * @return the Layer that has the specified index
     * @throws IndexOutOfBoundsException if the specified
     * <code>index</code> is less than
     * zero, or if it is equal to or greater than the number of Layers added
     * to the this <code>LayerManager</code>
     **/
    public Layer getLayerAt(int index) {
	if ((index < 0) || (index >= nlayers)) {
	    throw new IndexOutOfBoundsException();
	}
	return component[index];
    }

    /**
     * Gets the number of Layers in this LayerManager.
     * <p>
     * @return the number of Layers
     */
    public int getSize() {
	return nlayers;
    }

    /**
     * Removes the specified Layer from this LayerManager.  This method does
     * nothing if the specified Layer is not added to the this LayerManager.
     * @see #append(Layer)
     * @see #insert(Layer, int)
     * @param l the <code>Layer</code> to be removed
     * @throws NullPointerException if the specified <code>Layer</code> is
     * <code>null</code>
     */
    public void remove(Layer l) {
	removeImpl(l);
    }

    /**
     * Renders the LayerManager's current view window at the specified
     * location.
     * <p>
     * The LayerManager renders each of its layers in order of descending
     * index, thereby implementing the correct z-order.  Layers that are
     * completely outside of the view window are not rendered.
     * <p>
     * The coordinates passed to this method determine where the
     * LayerManager's view window will be rendered relative to the origin
     * of the Graphics object.  For example, a game may use the top of the
     * screen to display the current score, so to render the game's layers
     * below that area, the view window might be rendered at (0, 20).  The
     * location is relative to the Graphics object's origin, so translating
     * the Graphics object will change where the view window is rendered on
     * the screen.
     * <p>
     * The clip region of the Graphics object is intersected with a region
     * having the same dimensions as the view window and located at (x,y).
     * The LayerManager then translates the graphics object such that the
     * point (x,y) corresponds to the location of the viewWindow in the
     * coordinate system of the LayerManager.  The Layers are then rendered
     * in the appropriate order.  The translation and clip region of the
     * Graphics object are restored to their prior values before this method
     * returns.
     * <p>
     * Rendering is subject to the clip region and translation of the Graphics
     * object.  Thus, only part of the specified view window may be rendered
     * if the clip region is not large enough.
     * <p>
     * For performance reasons, this method may ignore Layers that are
     * invisible or that would be rendered entirely outside of the Graphics
     * object's clip region.  The attributes of the Graphics object are not
     * restored to a known state between calls to the Layers' paint methods.
     * The clip region may extend beyond the bounds of a Layer; it is the
     * responsibility of the Layer to ensure that rendering operations are
     * performed within its bounds.
     * <p>
     * @see #setViewWindow
     * @param g the graphics instance with which to draw the LayerManager
     * @param x the horizontal location at which to render the view window,
     * relative to the Graphics' translated origin
     * @param y the vertical location at which to render the view window,
     * relative to the Graphics' translated origin
     * @throws NullPointerException if <code>g</code> is <code>null</code>
     */
    public void paint(Graphics g, int x, int y) {

        // if g == null g.getClipX will throw NullPointerException;

        // save the original clip
        int clipX = g.getClipX();
	int clipY = g.getClipY();
        int clipW = g.getClipWidth();
        int clipH =  g.getClipHeight();


	// translate the LayerManager co-ordinates to Screen co-ordinates
	g.translate(x - viewX, y - viewY);
        // set the clip to view window
        g.clipRect(viewX, viewY, viewWidth, viewHeight);

	// draw last to first
	for (int i = nlayers; --i >= 0; ) {
            Layer comp = component[i];
	    if (comp.visible) {
                // IMPL NOTE: do this if outside Graphics clip region don't paint
		// (comp.contains(x - comp.x, y - comp.y)) &&
	        // paint will happen only in clipped region of view window
	        comp.paint(g);
	    }
	}

	// restore Screen co-ordinates origin and clip

	g.translate(-x + viewX, -y + viewY);
        g.setClip(clipX, clipY, clipW, clipH);
    }

    /**
     * Sets the view window on the LayerManager.
     * <p>
     * The view window specifies the region that the LayerManager draws when
     * its {@link #paint} method is called.  It allows the developer to
     * control the size of the visible region, as well as the location of the
     * view window relative to the LayerManager's coordinate system.
     * <p>
     * The view window stays in effect until it is modified by another call
     * to this method.  By default, the view window is located at (0,0) in
     * the LayerManager's coordinate system and its width and height are both
     * set to Integer.MAX_VALUE.
     *
     * @param x the horizontal location of the view window relative to the
     * LayerManager's origin
     * @param y the vertical location of the view window relative to the
     * LayerManager's origin
     * @param width the width of the view window
     * @param height the height of the view window
     * @throws IllegalArgumentException if the <code>width</code> or
     * <code>height</code> is less than <code>0</code>
     */
    public void setViewWindow(int x, int y, int width, int height) {

	if (width < 0 || height < 0) {
	    throw new IllegalArgumentException();
	}

	viewX = x;
        viewY = y;
        viewWidth = width;
        viewHeight = height;
    }

    /**
     * add or insert a layer
     * @param layer      The Layer to be inserted
     * @param index  the position at which to insert the layer
     */
    private void addImpl(Layer layer, int index) {
	// Add component to list;
	// allocate new array if necessary.

	if (nlayers == component.length) {
	    Layer newcomponents[] = new Layer[nlayers + 4];
	    System.arraycopy(component, 0, newcomponents, 0, nlayers);
            System.arraycopy(component, index, newcomponents,
                             index + 1, nlayers - index);
	    component = newcomponents;
	} else {
	    System.arraycopy(component, index, component,
				 index + 1, nlayers - index);
	}

	component[index] = layer;
	nlayers++;
    }

    /**
     * Removes the specified Layer from this LayerManager.
     * @param l The Layer to be removed
     * @throws NullPointerException if the specified Layer is null
     **/
    private void removeImpl(Layer l) {
        if (l == null) {
            throw new NullPointerException();
        }
        /**
         * Search backwards, expect that more recent additions
	 * are more likely to be removed.
         */

	for (int i = nlayers; --i >= 0; ) {
	    if (component[i] == l) {
		    remove(i);
	    }
	}
    }

    /**
     * Check existence the specified Layer in this LayerManager.
     * @param l The Layer to be check
     * @return true if Layer exist
     **/
    private boolean exist(Layer l) {
        if (l == null) {
            return false;
        }

        for (int i = nlayers; --i >= 0; ) {
            if (component[i] == l) {
                return true;
            }
        }
        return false;
    }

    /**
     * remove a layer at the specified index.
     * @param     index    the position at which to insert the layer,
     */
    private void remove(int index) {
	    System.arraycopy(component, index + 1,
			     component, index,
			     nlayers - index - 1);
	    component[--nlayers] = null;
    }

    /**
     * The number of layers in this LayerManager.
     * This value can be null.
     */
    private int nlayers; // = 0;

    /** 
     * The Layers in this LayerManager.
     * @see #append(Layer)
     */
    private Layer component[] = new Layer[4];

    /**
     * the view window co-ordinates.
     */
    private int viewX, viewY, viewWidth, viewHeight; // = 0;

}


