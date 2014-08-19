/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

import com.nokia.example.favouriteartists.tool.Log;

import javax.microedition.lcdui.*;

/**
 * View for adding a new artist to favourites.
 * 
 * The list control {@link CustomList} implements the actual list UI with gesture handling.
 * This class acts as an owner and delegates the drawing etc. to the list control.
 */
public class AddFavouriteView extends Canvas implements CustomListOwner {
	
	/** List control */
	CustomList customList;
	/** Command handler */
	CommandHandler commandHandler;
	/** Display */
	Display display;
	/** Image provider */
	ImageProvider imageProvider;
	
    /**
     * Constructor.
     * 
     * @param commandHandler Command handler.
     * @param display Display reference for retrieving colors.
     * @param artistDatas The artist data to show.
     * @throws FavouriteArtistsException 
     */
    public AddFavouriteView(CommandHandler commandHandler, Display display,
    		ImageProvider imageProvider, ArtistData[] artistDatas) throws FavouriteArtistsException {
    	if (Log.TEST) Log.note("[AddFavouriteView#AddFavouriteView]-->");
    	
    	setTitle("Add favourite");
    	this.commandHandler = commandHandler;
    	this.display = display;
    	this.imageProvider = imageProvider;
    	customList = new CustomList(this, Actions.ADD_FAVOURITE, Actions.ADD_FAVOURITE, display);
    	customList.setHeight(getHeight());
    	updateView(artistDatas, false);
    	
    	addCommand(new ActionCommand(Actions.BACK, "Back", Command.BACK, 0));
        
        setCommandListener(commandHandler);
    }
    
    /**
     * Updates the view with new artist data.
     * 
     * @param artistDatas New data array.
     * @param repaint If true, then a repaint will be requested.
     */
    public void updateView(ArtistData[] artistDatas, boolean repaint){
    	if (Log.TEST) Log.note("[AddFavouriteView#updateView]-->");
    	
    	if(artistDatas == null){
    		return;
    	}
    	// First clear old items
    	customList.clearList();
    	
    	// Create items from data and add them to list control
    	for(int i = 0; i < artistDatas.length; i++){
    		ArtistItem artistItem = new ArtistItem(display,
    				artistDatas[i], imageProvider, getWidth());
    		customList.appendItem(artistItem);
    	}
    	if(repaint){
    		repaint();
    	}
    }

    /**
	 * Getter for selected item.
	 * 
	 * @return selected item or null if nothing selected.
	 */
	public ArtistItem getSelectedItem(){
		return (ArtistItem)customList.getSelectedItem();
	}
    
    /**
     * @see javax.microedition.lcdui.Canvas#paint(javax.microedition.lcdui.Graphics)
     */
    protected void paint(Graphics g) {
     	if (Log.TEST) Log.note("[AddFavouriteView#paint]-->");
        customList.paint(g);
     	
    }
 
	
	/**
	 * @see com.nokia.example.favouriteartists.CustomListOwner#handleAction(short)
	 */
	public void handleAction(short actionId) {
		if (Log.TEST) Log.note("[AddFavouriteView#handleAction]-->");
		commandHandler.handleAction(actionId, null, this);
	}

	/**
	 * @see com.nokia.example.favouriteartists.CustomListOwner#requestRepaint()
	 */
	public void requestRepaint(){
		repaint();
	}	
}
