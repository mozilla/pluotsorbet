/*
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
package com.sun.mmedia;

import javax.microedition.lcdui.*;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.VideoControl;

/**
 * CustomItem, which supports full screen
 * (as an off-screen canvas, replacing current Displayable)...
 */
public abstract class MMCustomItem extends CustomItem {
    
    // Full screen canvas
    private Canvas fullScreen = null;

    // Display to set current displayable
    private Display display = null;
    
    // Saved displayable replaced by canvas in fullscreen mode
    private Displayable oldDisplayable = null;

    // Callback to VideoControl implementation to restore sizes of the item
    // when user manually exits fullscreen mode from the canvas.
    private VideoControl callerVideoControl = null;

    // Canvas painter
    private MIDPVideoPainter videoPainter = null;

    // Constructor
    protected MMCustomItem(String label) { super(label); }

    // Enter fullscreen mode,
    // setting VideoControl callback to restore normal mode
    // when user manually exits fullscreen mode from the canvas.
    public Canvas toFullScreen(VideoControl caller, MIDPVideoPainter painter) {
        if (fullScreen == null) {
            fullScreen = new FullScreenCanvas();
        }

        if (display == null) {
            MMHelper mmh = MMHelper.getMMHelper();
            if (mmh == null)
                return null;

            display = mmh.getDisplayFor(this);
            if (display == null)
                return null;
        }

        callerVideoControl = caller;
        videoPainter = painter;
        
        if (oldDisplayable == null)
            oldDisplayable = display.getCurrent();

        // Setting fullscreen canvas
        display.setCurrent(fullScreen);

        fullScreen.setFullScreenMode(true);

        return fullScreen;
    }

    // Return to normal, non-fullscreen mode, restoring old displayable
    public void toNormal() {        
        if (oldDisplayable != null) {
            display.setCurrent(oldDisplayable);
            oldDisplayable = null;
        }

        callerVideoControl = null;
        videoPainter = null;
    }
    
    // Fullscreen canvas implementation
    // Any key or pointer press returns to non-fullscreen mode.
    class FullScreenCanvas extends Canvas {
        FullScreenCanvas() {
        }

        protected void paint(Graphics g) {
            g.fillRect(0, 0, getWidth(), getHeight());
            videoPainter.paintVideo(g);
        }

        // Any key returns to normal mode
        protected void keyPressed(int keyCode) {
            if (callerVideoControl != null)
                try {
                    callerVideoControl.setDisplayFullScreen(false);
                } catch (MediaException me) {}
            super.keyPressed(keyCode);
        }

        // Any click returns to normal mode
        protected void pointerPressed(int x, int y) {
            if (callerVideoControl != null)
                try {
                    callerVideoControl.setDisplayFullScreen(false);            
                } catch (MediaException me) {}
            super.pointerPressed(x, y);
        }

        // Leave fullscreen mode when Canvas becomes invisible
        protected void hideNotify() {
            if (callerVideoControl != null)
                try {
                    callerVideoControl.setDisplayFullScreen(false);            
                } catch (MediaException me) {}
            super.hideNotify();
        }
    }
}
