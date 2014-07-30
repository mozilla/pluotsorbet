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

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.ItemCommandListener;

/**
 * The ChamDisplayTunnel interface is a special interface which
 * defines the relationship between Chameleon and the lcdui
 * Display class. It exists to provide cross-package access
 * from the Chameleon user interface library to the lcdui
 * Display class.
 */
public interface ChamDisplayTunnel {

    /**
     * This method is used by Chameleon to paint the current Displayable.
     *
     * @param g the graphics context to paint with
     */
    public void callPaint(Graphics g);

    /**
     * This method is used by Chameleon to schedule a paint event with
     * the event scheduler. This will result in a subsequent paint()
     * call to Chameleon on the event thread.
     */
    public void scheduleRepaint();

    /**
     * This method is used by Chameleon to indicate that a screen
     * command has been selected.
     *
     * @param cmd the Command which was selected
     * @param listener the CommandListener which was established at
     *                 the time the Command was added
     */
    public void callScreenListener(Command cmd, CommandListener listener);
    
    /**
     * This method is used by Chameleon to indicate that an item
     * command has been selected.
     *
     * @param cmd the Command which was selected
     * @param listener the item CommandListener which was established at
     *                 the time the Command was added
     */
    public void callItemListener(Command cmd, ItemCommandListener listener);
   
    /**
     * This method is used by Chameleon to invoke
     * Displayable.sizeChanged() method.
     * 
     * @param w the new width
     * @param h the new height
     */
    public void callSizeChanged(int w, int h);

    /**
     * This method is used by Chameleon to invoke 
     * Displayable.uCallScrollContent() method.
     *
     * @param scrollType scrollType
     * @param thumbPosition
     */
    public void callScrollContent(int scrollType, int thumbPosition);

    /**
     * Updates the scroll indicator.
     */
    public void updateScrollIndicator();

    /**
     * Called to get current display width.
     * @return Display width.
     */
    public int getDisplayWidth();
	
    /**
     * Called to get current display height.
     * @return Display height.
     */
    public int getDisplayHeight();

    /**
     * This method is used by Chameleon to invoke
     * CanvasLFImpl.uCallKeyPressed() method.
     *
     * @param keyCode key code
     */
    public void callKeyPressed(int keyCode);

    /**
     * This method is used by Chameleon to invoke
     * CanvasLFImpl.uCallKeyReleased() method.
     *
     * @param keyCode key code
     */
    public void callKeyReleased(int keyCode);

}

