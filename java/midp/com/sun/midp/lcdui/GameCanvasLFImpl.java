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
package com.sun.midp.lcdui;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;
import java.util.Vector;
import java.util.Enumeration;
import java.lang.ref.WeakReference;

/**
* This is the look &amp; feel implementation for GameCanvas.
*/
public class GameCanvasLFImpl {

    /**
     * The owner of this view.
     */
    GameCanvas owner;

    /**
     * Currently every GameCanvas has one offscreen buffer 
     * Can be optimized so that we put a limit on number of 
     * offscreen buffers an application can have
     */
    private Image offscreenBuffer;

    /** Cached reference to GraphicsAccess instance */
    private GraphicsAccess graphicsAccess;
    
    /**
     * To keep track of graphics provided users with
     */
    Vector gVector = new Vector();
    
    /**
     * Create new implementation instance for the given GameCanvas
     * @param c GameCanvas instance to create the implementation for
     */ 
    public GameCanvasLFImpl(GameCanvas c) {
        owner = c;
        graphicsAccess = GameMap.getGraphicsAccess();


        /* IMPL_NOTE: The initial off-screen buffer has the same width
         *   and height as the entire screen. Further resizing will not
         *   cause memory reallocation until new geometry is bigger than
         *   the current one. Screen rotation is one of the cases the
         *   reallocation is needed.
         *
         *   User can override the methods getWidth() and getHeight() of
         *   GameCanvas, so they should not be used for off-screen buffer
         *   initial allocation.
         */
        offscreenBuffer = Image.createImage(
            graphicsAccess.getScreenWidth(),
            graphicsAccess.getScreenHeight());
    }

    /**
     * Handle screen size change event to update internal
     * state of the GameCanvas accordingly
     *
     * @param w new screen width
     * @param h new screen height
     */
    public void lCallSizeChanged(int w, int h) {
        // Resize off-screen buffer in the case it is not big enough only
        if (w > offscreenBuffer.getWidth() ||
                h > offscreenBuffer.getHeight()) {

            // OutOfMemoryError can be thrown
            graphicsAccess.resizeImage(
                offscreenBuffer, w, h, true);

            /* Getting all graphics updated to the new dismesion
             * so that users can adjust to it.
             * In some cases like 3D engine and screen rotation,
             * midlets don't care about new size resulting in garbled display
             */
            synchronized(gVector) {
                Enumeration enum = gVector.elements();
                
                Graphics g = null;
                WeakReference wr = null;
                while (enum.hasMoreElements()) {
                    wr = (WeakReference)enum.nextElement();
                    g = (Graphics)wr.get();
                    if (g != null) {
                        graphicsAccess.setDimensions(g, w, h);
                    } else {
                        gVector.removeElement(wr);
                    }
                }
            }
            
        }
    }

    /**
     * Obtains the Graphics object for rendering a GameCanvas.  The returned
     * Graphics object renders to the off-screen buffer belonging to this
     * GameCanvas.
     *
     * IMPL_NOTE: The dimensions of the Graphics object are explicitly
     *   set to GameCanvas size, since off-screen buffer larger than
     *   GameCanvas can be used, while some JSR clients need to translate
     *   the coordinates regarding the GameCanvas size.
     *       Anyway if GameCanvas has zero width or height, the Graphics
     *   dimensions are set to entire off-screen buffer.
     *
     * @return  the Graphics object that renders to current GameCanvas
     */
    public Graphics getGraphics() {
        if (offscreenBuffer != null) {
            int w = owner.getWidth();
            int h = owner.getHeight();
            
            Graphics g = ((w <= 0) || (h <= 0)) ?
                offscreenBuffer.getGraphics() :
                graphicsAccess.getImageGraphics(offscreenBuffer, w, h);

            graphicsAccess.setGraphicsCreator(g, owner);
            // Keep track of all graphics provided midlets with until now
            synchronized(gVector) {
                gVector.addElement(new WeakReference(g));

                Enumeration enum = gVector.elements();

                // clear the empty references
                WeakReference wr = null;
                while (enum.hasMoreElements()) {
                    wr = (WeakReference)enum.nextElement();
                    if (wr.get() == null) {
                        gVector.removeElement(wr);
                    }
                }
            }
            return g;
        }
        
        return null;
    }

    /**
     * Render the off-screen buffer content to the Graphics object
     * @param g the Graphics object to render off-screen buffer content
     */
    public void drawBuffer(Graphics g) {
        // NullPointerException will be thrown in drawImage if g == null
       if (offscreenBuffer != null) {
            g.drawImage(offscreenBuffer, 0, 0,
                Graphics.TOP | Graphics.LEFT);
       }
    }

    /**
     * Flushes the off-screen buffer to the display. The size
     * of the flushed area is equal to the size of the GameCanvas.
     */
    public void flushGraphics() {
        DisplayAccess displayAccess = GameMap.getDisplayAccess(owner);
        if (displayAccess != null && offscreenBuffer != null) {
	        displayAccess.flush(owner, offscreenBuffer,
			      0, 0, owner.getWidth(), owner.getHeight());
        }
    }

    /**
     * Flushes the specified region of the off-screen buffer to the display.
     * @param x the left edge of the region to be flushed
     * @param y the top edge of the region to be flushed
     * @param width the width of the region to be flushed
     * @param height the height of the region to be flushed
     */
    public void flushGraphics(int x, int y, int width, int height) {
        // check the region bounds 
        int diff = 0;
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        
        diff = x + width - owner.getWidth();
        if (diff > 0) width -= diff;

        diff = y + height - owner.getHeight();
        if (diff > 0) height -= diff;
        
        if (width < 1 || height < 1) {
            return;
        }
        
        DisplayAccess displayAccess = GameMap.getDisplayAccess(owner);
        if (displayAccess != null && offscreenBuffer != null) {
            displayAccess.flush(owner, offscreenBuffer,
                                x, y,	width, height);
        }
    }

    /**
     * Gets the states of the physical game keys.
      * @return An integer containing the key state information (one bit per
     * key), or 0 if the GameCanvas is not currently shown.
     */
    public int getKeyStates() {
        DisplayAccess displayAccess = GameMap.getDisplayAccess(owner);
        if (displayAccess != null) {
            return displayAccess.getKeyMask();
        }
        return 0;
    }
}
