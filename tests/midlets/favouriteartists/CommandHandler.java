/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

/**
 * Composite interface for command handling.
 */
public interface CommandHandler extends CommandListener, ItemCommandListener {
	
	/**
     * Called when an action needs to be handled. The implementation
     * must not throw any exceptions. 
     * 
     * @param actionId the identifier of the action.
     * @param item the Item related to this action
     * @param d the Displayable related to this action.
     */
    public void handleAction(short actionId, Item item, Displayable d);
}
