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

/**
 * Data class for favourite artist details.
 */
public class FavouriteData extends ArtistData {

	//Member data
	/** User's rating for the artist/band */
	short rating = 1;
	/** User's comment of the artist/band */
	String comment;
	
	//Methods
	/**
	 * Constructor.
	 */
	public FavouriteData(ArtistData artistData){
		super(artistData);
		if (Log.TEST) Log.note("[FavouriteData#FavouriteData]-->");
	}
	
	/**
	 * @return the rating
	 */
	public short getRating() {
		return rating;
	}
	/**
	 * @param rating the rating to set
	 */
	public void setRating(short rating) {
		this.rating = rating;
	}
	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
}
