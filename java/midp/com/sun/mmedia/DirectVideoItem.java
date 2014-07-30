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

public class DirectVideoItem extends CustomItem
{
    private DirectVideoItemImpl _impl;
    
    public DirectVideoItem( DirectVideoItemImpl content )
    {
        super( null );
        _impl = content;
    }
    
    public void forcePaint()
    {
        repaint();
    }
    
    protected void sizeChanged(int w, int h) {
        _impl.sizeChanged( w, h );
        repaint();
    }

    // Now this function used to control visible state of direct video preview
    // Called from MIDPWindow class
    protected void showNotify() {
        _impl.showNotify();
        repaint();
    }

    // Now this function used to control visible state of direct video preview
    // Called from MIDPWindow class
    protected void hideNotify() {
        _impl.hideNotify();
        repaint();
    }
    
    
    protected void paint(Graphics g, int w, int h) {
        _impl.paint( g, w, h );
    }
    
    protected int getMinContentWidth() {
        return 1;
    }

    protected int getMinContentHeight() {
        return 1;
    }

    protected int getPrefContentWidth(int height) {
        return _impl.getWidth();
    }

    protected int getPrefContentHeight(int width) {
        return _impl.getHeight();
    }

    
    
}

