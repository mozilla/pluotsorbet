/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

/** 
 * Interface that {@link CustomList} gets to it's owner.
 * Needed for requesting repaints and handling actions.
 */
public interface CustomListOwner{
	
	// Methods	
	/**
	 * Request a repaint from the owner.
	 */
    public void requestRepaint();
    
    /**
     * Forward action handling to owner.
     * 
     * @param actionId The occurred action.
     */
    public void handleAction(short actionId);
}