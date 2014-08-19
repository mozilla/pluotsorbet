/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

import javax.microedition.lcdui.Display;

import com.nokia.example.favouriteartists.tool.Log;

/**
 * List item specialization for an artist.
 */
public class ArtistItem extends ListItem {
	
	// Member data
	private ArtistData data;
	
	// Methods
	/**
	 * Constructor.
	 * 
	 * @param data The artist data of this item.
	 * @param imageProvider Image provider for retrieving possible image.
	 */
	public ArtistItem(Display display, ArtistData data, ImageProvider imageProvider,
			int width){
		super(display, null, null, null, ListItem.TYPE_ONE_ROW | ListItem.TYPE_ICON_LEFT,
				width,
				ListItem.ICON_MAX_H + 2 * V_PAD);
		if (Log.TEST) Log.note("[ArtistItem#ArtistItem]-->");
		this.data = data;
		setText1(data.getName());
		setIcon(imageProvider.getImage(data.getImageFilename()));
	}
	
	/**
	 * Getter for data.
	 */
	ArtistData getData(){
		return data;
	}
}
