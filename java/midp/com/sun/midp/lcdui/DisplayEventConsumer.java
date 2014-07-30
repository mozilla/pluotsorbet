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

import javax.microedition.lcdui.Displayable;

/**
 * This interface provides a set of methods to handle 
 * display-related events: user input, drawing, 
 * callbacks that are assosiated with displays.
 *
 * Implementor of this I/F shall be instance specific 
 * i.e. associated with a Display object instance 
 * (ex. Display.DisplayEventConsumerImpl class).
 *
 * Therefore it is assumed that target identification 
 * (displayId -> Display/DisplayAccess/DisplayEventConsumerImpl) 
 * is done before calling I/F methods in EventListener.
 *
 */
public interface DisplayEventConsumer {
    
    /*
     * LOCAL USER INPUT EVENTS - produced by Native code
     *
     * KEY/non-IME
     * KEY/IME
     * PEN or POINTER (what name is better?)
     * CMD
     * PEER_CHANGED
     * 
     */
    
    /**
     * Called by event delivery when a key press,release or repeate event 
     * needs to be processed.
     *
     * @param keyType key press, release or repeate
     *        is one of EventConstants.PRESSED, EventConstants.RELEASED,
     *        EventConstants.REPEATED.
     * @param keyCode key code to process
     */
    public void handleKeyEvent(int keyType, int keyCode);
    
    /**
     * Called by event delivery when an input method event is processed.
     *
     * @param inputText string to process
     */
    public void handleInputMethodEvent(String inputText);

    /**
     * Called by event delivery when a pointer press,release or drag event 
     * needs to be processed.
     *
     * @param pointerType pointer press, release or drag
     *        is one of EventConstants.PRESSED, EventConstants.RELEASED,
     *        or EventConstants.DRAGGED.     
     * @param x x-coordinate of the pointer event
     * @param y y-coordinate of the pointer event
     */
    public void handlePointerEvent(int pointerType, int x, int y);

    /**
     * Called by event delivery when an abstract Command is fired.
     * The parameter is an index into the list of Commands that are
     * current, i.e. those associated with the visible Screen.
     *
     * TBD: param screenId Id of the command target (Displayable) 
     * @param cmdId command Id 
     */
    public void handleCommandEvent(/* int screenId, */ int cmdId);

    /**
     * Called by event delivery to notify an ItemLF in current DisplayableLF
     * of a change in its native peer state.
     *
     * @param modelVersion the version of the peer's data model
     * @param subType sub type of the peer change that happened
     * @param itemPeerId the id of the ItemLF's peer whose state has changed
     * @param hint some value that is interpreted only between the peers
     */
    public void handlePeerStateChangeEvent(
            int modelVersion, 
            int subType,
            int itemPeerId,
            int hint);

    /*
     * LOCAL DISPLAY MANAGEMENT EVENTS
     *
     * REPAINT - invoked through RepaintEventListener
     * SCREEN_CHANGE
     * INVALIDATE
     */

    /**
     * Called by event delivery when a repaint should occur.
     *
     * @param x1 The origin x coordinate of the repaint region
     * @param y1 The origin y coordinate of the repaint region
     * @param x2 The bounding x coordinate of the repaint region
     * @param y2 The bounding y coordinate of the repaint region
     * @param target The optional paint target
     */
    public void handleRepaintEvent(
            int x1, int y1, 
            int x2, int y2, 
            Object target);

    /**
     * Called by event delivery when a screen change needs to occur.
     *
     * @param screen The Displayable to make current in the Display
     */
    public void handleScreenChangeEvent(Displayable screen);

    /**
     * Called by event delivery to process a Form invalidation.
     */
    public void handleInvalidateEvent();

    /*
     * LOCAL CALLBACK MANAGEMEMT EVENTS
     *
     * CALL_SERIALLLY
     */
    
    /**
     * Called by event delivery to batch process 
     * all pending serial callbacks. 
     */
    public void handleCallSeriallyEvent();
    
    
    /*
     * ITEM EVENTS - not associated with a particular Display.
     *
     * ITEM_CHANGED/STATE_CHANGE
     * ITEM_CHANGED/SIZE_REFRESH
     * ITEM_CHANGED/MAKE_VISIBLE
     *
     * Now processed by ItemEventConsumer. 
     */


     /*
      * Called by event delivery when size of screen was changed.
      */
     public void handleRotationEvent();

     /*
      * Called by event delivery when clamshell event occurs.
      */
     public void handleClamshellEvent();
    

     /*
      * Called by event delivery when state of display device is changed.
      */
 
     public void handleDisplayDeviceStateChangedEvent(int state);
     /*
      * Called by event delivery when full screen repaint is requested.
      */
     public void handleScreenRepaintEvent();

     /*
      * Called by event delivery when need to show or hide virtual keyboard
      */
     public void handleVirtualKeyboardEvent();

    /*
     * Called by event delivery when locale is changed
     */
    public void handleChangeLocaleEvent();
}
