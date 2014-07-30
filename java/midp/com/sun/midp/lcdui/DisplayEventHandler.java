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

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

import com.sun.midp.events.EventQueue;

/**
 * This class works around the fact that public classes can not
 * be added to a javax package by an implementation.
 */
public interface DisplayEventHandler {
    
    /**
     * Preempt the current displayable with
     * the given displayable until donePreempting is called.
     *
     * @param d displayable to show the user
     * @param waitForDisplay if true this method will wait if the
     *        screen is being preempted by another thread.
     *
     * @return an preempt token object to pass to donePreempting done if prempt
     *  will happen, else null
     *
     * @exception SecurityException if the caller does not have permission
     *   the internal MIDP permission.
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public Object preemptDisplay(Displayable d, boolean waitForDisplay)
        throws InterruptedException;

    /**
     * Display the displayable that was being displayed before
     * preemptDisplay was called.
     *
     * @param preemptToken the token returned from preemptDisplay
     */
    public void donePreempting(Object preemptToken);

    /**
     * Called by Display to notify DisplayEventHandler that
     * Display has been sent to the background to finish
     * preempt process if any.
     *
     * @param displayId id of Display
     */
    public void onDisplayBackgroundProcessed(int displayId);

    /**
     * Initialize Display Event Handler
     *
     * @param theDisplayEventProducer producer for display events
     * @param theForegroundController controls which display has the foreground
     * @param theRepaintEventProducer producer for repaint events events
     * @param theDisplayContainer container for display objects
     * @param theDisplayDeviceContainer container for display device objects
     */
    public void initDisplayEventHandler(
        DisplayEventProducer theDisplayEventProducer,
        ForegroundController theForegroundController,
        RepaintEventProducer theRepaintEventProducer,
        DisplayContainer theDisplayContainer,
	DisplayDeviceContainer theDisplayDeviceContainer);
    
    /**
     * Sets the trusted state of the display event handler.
     *
     * @param drawTrustedIcon true, to draw the trusted icon in the upper
     *                status bar for every display of this suite
     */
    void setTrustedState(boolean drawTrustedIcon);
}
