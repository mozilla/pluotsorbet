/*
 * Copyright � 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.nokia.example.favouriteartists.tool.Log;
import com.nokia.example.favouriteartists.tool.Util;

/**
 * List item specialization for favourite.
 */
public class FavouriteItem extends ListItem {

	// Constants
	private static final String STAR_IMG_FILE = "midlets/favouriteartists/images/star.png";
	private static final int STAR_IMG_H_PAD = 2;
	
	// Member data
	private static Image starImg;
	private FavouriteData data;
	
	// Methods
	/**
	 * Constructor.
	 * 
	 * @param data The favourite data of this item.
	 * @param imageProvider Image provider for retrieving possible image.
	 */
	public FavouriteItem(Display display, FavouriteData data, ImageProvider imageProvider,
			int width){
		super(display, null, null, null, ListItem.TYPE_TWO_ROW | ListItem.TYPE_ICON_LEFT,
				width,
				ListItem.ICON_MAX_H + 2 * V_PAD);
		if (Log.TEST) Log.note("[FavouriteItem#FavouriteItem]-->");
		this.data = data;
		setText1(data.getName());
		setText2(data.getSignificantSongs()[0]);
		setIcon(imageProvider.getImage(data.getImageFilename()));
		if(starImg == null){
			starImg = imageProvider.getImage(STAR_IMG_FILE);
		}
	}
	
	/**
	 * Getter for data.
	 */
	FavouriteData getData(){
		return data;
	}
	
	/**
	 * @see com.nokia.example.favouriteartists.ListItem#drawBottomRow(javax.microedition.lcdui.Graphics, int[])
	 */
	protected void drawBottomRow(Graphics g, int[] rect){
		if (Log.TEST) Log.note("[FavouriteItem#drawBottomRow]-->");
		int x = rect[0];
    	int y = rect[1];
    	int height = rect[3];
		x += STAR_IMG_H_PAD;
		// Draw stars according to rating
		for(int i = 1; i <= data.getRating(); i++) {
			Util.drawImageCentered(g, starImg, x, y, starImg.getWidth()+ STAR_IMG_H_PAD * 2, height);
			x += STAR_IMG_H_PAD + starImg.getWidth();
		}
	}
}
