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

package com.sun.midp.chameleon.layers;

import com.sun.midp.chameleon.*;
import javax.microedition.lcdui.*;

/**
 * A "Popup" layer is a special kind of layer which can
 * also have commands associated with it. When a popup
 * layer is added to a MIDPWindow, its commands (if it has any)
 * can be accessed through the soft button bar. If a popup layer
 * does have commands associated with it, any commands on the
 * current displayable/item are no longer accessible. If
 * the popup layer does not have its own commands, any
 * existing commands from the displayable/item remain.
 *
 * NOTE: For now, a PopupLayer is also always visible,
 * that is isVisible() always returns true. To control the
 * visibility of a PopupLayer, you add it and remove it
 * from a MIDPWindow. IMPL_NOTE: determine if a relationship between
 * PopupLayer and MIDPWindow can allow non visible popup layers.
 */
public class PopupLayer extends CLayer {

    /**
     * The set of Commands for this PopupLayer
     */
    protected Command[] commands;

    /**
     * The CommandListener to notify when a Command is selected
     */
    protected CommandListener listener;
    
    /**
     * Construct a new PopupLayer. By default, setSupportsInput()
     * is set to true.
     */
    public PopupLayer() {
        this((Image)null, -1);
    }

    /**
     * Construct a new PopupLayer, given a background image.
     * By default, setSupportsInput() is set to true, and so
     * is setVisible().
     */
    public PopupLayer(Image bgImage, int bgColor) {
        super(bgImage, bgColor);
        this.supportsInput = true;
    }
    
    /**
     * Construct a new PopupLayer, given a 9 pc background image.
     * By default, setSupportsInput() is set to true, and so
     * is setVisible().
     */
    public PopupLayer(Image[] bgImage, int bgColor) {
        super(bgImage, bgColor);
        this.supportsInput = true;
    }

    /**
     * The setVisible() method is overridden in PopupLayer
     * so as not to have any effect. PopupLayers are always
     * visible by their very nature. In order to hide a
     * PopupLayer, it should be removed from its containing
     * MIDPWindow.
     */
    public void setVisible(boolean visible) {
    }

    /**
     * Set the set of Commands associated with this PopupLayer.
     *
     * @param commands the set of Commands associated with this
     *                 PopupLayer. 'null' means there are no Commands
     */
    public void setCommands(Command[] commands) {
        this.commands = commands;
    }

    /**
     * Get the set of Commands associated with this PopupLayer
     *
     * @return the set of Commands associated with this PopupLayer.
     *         'null' means there are no Commands.
     */
    public Command[] getCommands() {
        return commands;
    }
    
    /**
     * Establish a listener for the commands for this popup layer.
     * NOTE: When the CommandListener is notified of a command action,
     * the 'displayable' argument in its commandAction() method will
     * always be null.
     *
     * @param listener the CommandListener to call when a command on
     *                 this popup layer is selected
     */
    public void setCommandListener(CommandListener listener) {
        this.listener = listener;
    }
    
    /**
     * Get the CommandListener associated with this popup layer.
     * If the listener is null, any commands added to this popup layer
     * will not be visible when this popup is added to a MIDPWindow.
     * 
     * @return the CommandListener (if any) associated with this popup
     */
    public CommandListener getCommandListener() {
        return listener;
    }

    /**
     * Returns true for popup layer because almost all pointer events
     * have to be handled by popup even if it's out of bounds. The most of
     * popups has to be closed if the pointer event is out of its bounds.
     * The exception is the pointer is a part of the command layer. 
     *
     * @param x the "x" coordinate of the point
     * @param y the "y" coordinate of the point
     * @return true if the point is handled by this layer
     */
    public boolean handlePoint(int x, int y) {
        boolean ret = true;
        if (commands != null && commands.length > 0 && owner != null) {
            ret = !((MIDPWindow)owner).belongToCmdLayers(x, y);
        }
        return ret | containsPoint(x, y);
    }

}

