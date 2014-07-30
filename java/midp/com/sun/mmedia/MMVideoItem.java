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

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Display;


public class MMVideoItem extends CustomItem
{
    private boolean _isFullScreen;
    private MMVideoItemImpl _impl;
    
    // Full screen canvas
    private Canvas fullScreen = null;

    // Display to set current displayable
    private Display display = null;
    
    // Saved displayable replaced by canvas in fullscreen mode
    private Displayable oldDisplayable = null;

    public MMVideoItem( MMVideoItemImpl c )
    {
        super("");
        _impl = c;
        _isFullScreen = false;
    }
    
    protected void paint(Graphics g, int w, int h) {
        _impl.paint( g );
    }
    
    protected int getMinContentWidth() {
        return 1;
    }

    protected int getMinContentHeight() {
        return 1;
    }
    
    protected int getPrefContentWidth(int h) {
        return _impl.getWidth();
    }

    protected int getPrefContentHeight(int w) {
        return _impl.getHeight();
    }
    
    public void forcePaint(int [] frame) {
        if (frame != null)
            _impl.setFrame( frame );
        else
            invalidate();
        repaint();
    }

    public void renderImage(byte [] imageData, int imageLength) {
        _impl.setImage( imageData, imageLength );
        repaint();
    }

    // Enter fullscreen mode,
    // setting VideoControl callback to restore normal mode
    // when user manually exits fullscreen mode from the canvas.
    public Canvas toFullScreen() {
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

        if (oldDisplayable == null)
            oldDisplayable = display.getCurrent();

        // Setting fullscreen canvas
        display.setCurrent(fullScreen);

        fullScreen.setFullScreenMode(true);
        
        _isFullScreen = true;

        return fullScreen;
    }

    // Return to normal, non-fullscreen mode, restoring old displayable
    public void toNormal() {        
        if (oldDisplayable != null) {
            display.setCurrent(oldDisplayable);
            oldDisplayable = null;
        }
        
        _isFullScreen = false;
    }
    
    // Fullscreen canvas implementation
    // Any key or pointer press returns to non-fullscreen mode.
    class FullScreenCanvas extends Canvas {
        FullScreenCanvas() {
        }

        protected void paint(Graphics g) {
            g.fillRect(0, 0, getWidth(), getHeight());
            _impl.paintFullScreen( g );
        }

        // Any key returns to normal mode
        protected void keyPressed(int keyCode) {
            if ( _isFullScreen )
            {
                _impl.returnFromFullScreen();
            }
            super.keyPressed(keyCode);
        }

        // Any click returns to normal mode
        protected void pointerPressed(int x, int y) {
            if ( _isFullScreen )
            {
                _impl.returnFromFullScreen();
            }
            super.pointerPressed(x, y);
        }

        // Leave fullscreen mode when Canvas becomes invisible
        protected void hideNotify() {
            if ( _isFullScreen )
            {
                _impl.returnFromFullScreen();
            }
            super.hideNotify();
        }
    }
    
}