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

package javax.microedition.lcdui;

/* import  javax.microedition.lcdui.KeyConverter; */
import com.sun.midp.chameleon.layers.VirtualKeyListener;

import com.sun.midp.i18n.ResourceConstants;

import java.util.Vector;
import java.util.Enumeration;

/**
* This is the look amps; feel implementation for Canvas.
*/
class CanvasLFImpl extends DisplayableLFImpl implements CanvasLF, VirtualKeyListener {


    /**
     * Constructor.
     * @param canvas - the canvas being stored in this object.
     */
    CanvasLFImpl(Canvas canvas) {
        super(canvas);
        this.canvas = canvas;
        if (currentDisplay != null) {
            isDisplayRotated = currentDisplay.wantRotation;
        }
    }

    // ************************************************************
    //  public methods - CanvasLF interface implementation
    // ************************************************************

    /**
     * Notifies look &amps; feel object that repaint of a (x, y, width, height)
     * area is needed.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param x The x coordinate of the region to repaint
     * @param y The y coordinate of the region to repaint
     * @param width The width of the region to repaint
     * @param height The height of the region to repaint
     * @param target an optional paint target to receive the paint request
     *               when it returns via callPaint()
     */
    public void lRepaint(int x, int y, int width, int height, Object target) {
        lRequestPaint(x, y, width, height);
    }
    
    /**
     * Notifies that repaint of the entire Canvas look &amps; feel is
     * needed and should repaint the viewport area.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     */
    public void lRepaint() {
        lRequestPaint();
    }

    /**
     * Request serviceRepaints from current Display.
     * SYNC NOTE: No locking is held when
     * this function is called because Display.serviceRepaints()
     * needs to handle its own locking.
     */
    public void uServiceRepaints() {
        // Avoid locking by making a copy of currentDisplay
        // -- an atomic operation -- before testing and using it.
        Display d = currentDisplay;

        if (d != null) {
            d.serviceRepaints(this);
        }
    }

    /**
     * Notify this Canvas it is being shown on the given Display
     */
    public void uCallShow() {

        // Check full screen mode and call lCallShow below
        super.uCallShow();

        // SYNC NOTE: Call into app code. So do it outside LUICDLock
        synchronized (Display.calloutLock) {
            try {
                canvas.showNotify();
                /* For MMAPI VideoControl in a Canvas */
                if (mmHelper != null) {
                    for (Enumeration e = embeddedVideos.elements(); 
                                              e.hasMoreElements();) {
                        mmHelper.showVideo(e.nextElement());
                    }
                }
            } catch (Throwable t) {
                Display.handleThrowable(t);
            }
        }
    }

    /**
     * Prepare this CanvasLF to show.
     * Override the version in DisplayableLFImpl to perform layout.
     */
    void lCallShow() {
        int oldState = state;
        super.lCallShow();
        if (oldState != FROZEN) {
            layout();
        }
    }

    /**
     * Notify this Canvas it is being hidden on the given Display
     */
    public void uCallHide() {

        int oldState = state;

        super.uCallHide();

        // SYNC NOTE: Call into app code. So do it outside LUICDLock
        synchronized (Display.calloutLock) {
            if (oldState == SHOWN) { 
                try {
                    canvas.hideNotify();
                    if (mmHelper != null) {
                        for (Enumeration e = embeddedVideos.elements(); 
                                                  e.hasMoreElements();) {
                            mmHelper.hideVideo(e.nextElement());
                        }
                    }
                } catch (Throwable t) {
                    Display.handleThrowable(t);
                }
                needRepaintBackground = true;
            }
        }
    }

    /**
     * Notify this <code>Canvas</code> that it is being frozen on the 
     * given <code>Display</code>.
     */
    public void uCallFreeze() {
        
        int oldState = state;

        // Delete native resources including title and ticker
        super.uCallFreeze();

        // Notify canvas subclass after hiding the native resource
        synchronized (Display.calloutLock) {
            if (oldState == SHOWN) {
                try {
                    canvas.hideNotify();
                    if (mmHelper != null) {
                        for (Enumeration e = embeddedVideos.elements();
                                                  e.hasMoreElements();) {
                            mmHelper.hideVideo(e.nextElement());
                        }
                    }
                } catch (Throwable t) {
                    Display.handleThrowable(t);
                }
                needRepaintBackground = true;
           }
        }
    }

    /**
     * Paint this Canvas
     *
     * @param g the Graphics to paint to
     * @param target the target Object of this repaint
     */
    public void uCallPaint(Graphics g, Object target) {

        synchronized (Display.LCDUILock) {
            // SYNC NOTE: We assert the super function will not call into
            // app code. So we can call it inside sync block.
            super.lCallPaint(g, target);

            // Optimize to not call paint at all
            // when clipping is out of bound (relative to viewport)
            if (g.getClipY() + g.getClipHeight() <= 0) {
                return;
            }
        }

        // We prevent the Canvas from drawing outside of the
        // allowable viewport - such as over the command labels
        // or over the theme area.
        // We also need to preserve the original translation.
        g.preserveMIDPRuntimeGC(0, 0, viewport[WIDTH], viewport[HEIGHT]);

        // Reset the graphics context according to
        // the spec. requirement. This is a must
        // before we call canvas's paint(g) since
        // the title or ticker drawing routines may
        // change the GC before.
        g.resetGC();

        synchronized (Display.calloutLock) {
            // We need repaint background if an orientation has changed
            if (currentDisplay != null) {
                boolean isRotated = currentDisplay.wantRotation;
                if (isDisplayRotated != isRotated) {
                    isDisplayRotated = isRotated;
                    needRepaintBackground = true;
                }
            }
            try {
                // Paint black background under the canvas
                boolean isShown = canvas.isShown();
                if (needRepaintBackground && isShown) {
                    // We should repaint whole canvas: remove the clipping
                    g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());
                    // Draw a black rectangle
                    int savedColor = g.getColor();
                    g.setColor(0, 0, 0);
                    g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    g.setColor(savedColor);
                }
                needRepaintBackground = !isShown;
                canvas.paint(g);
                // If there are any video players in this canvas,
                // let the helper class invoke video rendering
                // Update frames of any video players displayed on this Canvas
                if (mmHelper != null) {
                    for (Enumeration e = embeddedVideos.elements(); 
                                                  e.hasMoreElements();) {
                        mmHelper.paintVideo(e.nextElement(), g);
                    }
                }
            } catch (Throwable t) {
                Display.handleThrowable(t);
            }
        }

        g.restoreMIDPRuntimeGC();
    }

    // ************************************************************
    //  package private methods
    // ************************************************************

    /**
     * Handle a key press
     *
     * @param keyCode The key that was pressed
     */
    void uCallKeyPressed(int keyCode) {
        if (allowKey(keyCode)) {
            synchronized (Display.calloutLock) {
                try {
                    canvas.keyPressed(keyCode);
                } catch (Throwable t) {
                    Display.handleThrowable(t);
                }
            }
        }
    }

    /**
     * Handle a key release
     *
     * @param keyCode The key that was released
     */
    void uCallKeyReleased(int keyCode) {
        if (allowKey(keyCode)) {
            synchronized (Display.calloutLock) {
                try {
                    canvas.keyReleased(keyCode);
                } catch (Throwable t) {
                    Display.handleThrowable(t);
                }
            }
        }
    }

    /**
     * Handle a repeated key press
     *
     * @param keyCode The key that was pressed
     */
    void uCallKeyRepeated(int keyCode) {
        if (allowKey(keyCode)) {
            synchronized (Display.calloutLock) {
                try {
                    canvas.keyRepeated(keyCode);
                } catch (Throwable t) {
                    Display.handleThrowable(t);
                }
            }
        }
    }

    /**
     * Handle a pointer press event
     *
     * @param x The x coordinate of the press
     * @param y The y coordinate of the press
     */
    void uCallPointerPressed(int x, int y) {
        synchronized (Display.calloutLock) {
            try {
                canvas.pointerPressed(x, y);
            } catch (Throwable t) {
                Display.handleThrowable(t);
            }
        }
    }

    /**
     * Handle a pointer release event
     *
     * @param x The x coordinate of the release
     * @param y The y coordinate of the release
     */
    void uCallPointerReleased(int x, int y) {
        synchronized (Display.calloutLock) {
            try {
                canvas.pointerReleased(x, y);
            } catch (Throwable t) {
                Display.handleThrowable(t);
            }
        }
    }

    /**
     * Handle a pointer drag event
     *
     * @param x The x coordinate of the drag
     * @param y The y coordinate of the drag
     */
    void uCallPointerDragged(int x, int y) {
        synchronized (Display.calloutLock) {
            try {
                canvas.pointerDragged(x, y);
            } catch (Throwable t) {
                Display.handleThrowable(t);
            }
        }
    }

    /**
     * Add embedded video player.
     * These are called by <code>MMHelperImpl</code>, whenever a video 
     * player joins this canvas.
     *
     * @param video The player joining this canvas.
     */
     void addEmbeddedVideo(Object video) {
         embeddedVideos.addElement(video);
     }

    /**
     * Remove embedded video player.
     * This is called by <code>MMHelperImpl</code>, whenever a video 
     * player leaves this canvas.
     *
     * @param video The player leaving this canvas.
     */
     void removeEmbeddedVideo(Object video) {
         embeddedVideos.removeElement(video);
     }


    // ************************************************************
    //  private methods
    // ************************************************************

    /**
     * Test to see if the given keyCode should be sent to
     * the application
     *
     * @param keyCode the key code to pass to the application
     * @return true if the key should be allowed
     */
    private boolean allowKey(int keyCode) {
        if (!canvas.suppressKeyEvents) {
            return true;
        }

        switch (KeyConverter.getGameAction(keyCode)) {
            case -1:
                // Invalid keycode, don't
                // block this key.
                return true;
            case Canvas.UP:
            case Canvas.DOWN:
            case Canvas.LEFT:
            case Canvas.RIGHT:
            case Canvas.FIRE:
            case Canvas.GAME_A:
            case Canvas.GAME_B:
            case Canvas.GAME_C:
            case Canvas.GAME_D :
                // don't generate key events for
                // the defined game keys
                return false;
            default:
                return true;
        }
    }

    /**
     * Canvas being stored in this object.
     */
    Canvas canvas;

    /**
     * Is a repaint of black background needed?
     */
    private boolean needRepaintBackground = true;
    
    /**
     * Is the display rotated?
     */
    private boolean isDisplayRotated = false;
    
    /**
     * A vector of embedded video players.
     */
    private Vector embeddedVideos = new Vector(1);

    /**
     * The MMHelperImpl instance.
     */
    private static MMHelperImpl mmHelper = MMHelperImpl.getInstance();

    public void processKeyPressed(int keyCode) {
        uCallKeyPressed(keyCode);
    }

    public void processKeyReleased(int keyCode) {
        uCallKeyReleased(keyCode);
    }
}
