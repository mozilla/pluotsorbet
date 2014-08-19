/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;  

import javax.microedition.lcdui.*;

import com.nokia.example.favouriteartists.tool.Log;
import com.nokia.mid.ui.LCDUIUtil;

/**
 * Rating view, used for modifying favourite rating and comment as well as displaying other favourite details.
 */
public class RatingView extends Form {

	// Member data
	/** Favourite data */
	FavouriteData favData;
	/** Rating item */
	RatingItem ratingItem;
	/** Comment item */
	TextField comment;
	/** Name item */
	TextField name;
	/** Image item */
	ImageItem img;
	/** Genres item */
	StringItem genres;
	/** Description item */
	StringItem shortDescription;
	/** Active members item */
	StringItem activeMembers;
	/** Former members item */
	StringItem formerMembers;
	/** Significant albums item */
	StringItem significantAlbums;
	/** Significant songs item */
	StringItem significantSongs;
	/** Similar artists item */
	StringItem similarArtists;
	
    /**
     * Constructor.
     * 
     * @param favData Favourite data to display.
     * @param commandHandler For command handling.
     * @param imageProvider For image retrieval.
     * @throws FavouriteArtistsException
     */
	public RatingView(FavouriteData favData, CommandHandler commandHandler,
			ImageProvider imageProvider) throws FavouriteArtistsException {
		super("Details");
		if (Log.TEST) Log.note("[RatingView#RatingView]-->");
		
		this.favData = favData;
		
		// Initialize items
		ratingItem = new RatingItem(imageProvider, getWidth(), favData.rating);
                LCDUIUtil.setObjectTrait(ratingItem, "nokia.ui.s40.item.direct_touch", new Boolean(true));
		comment = new TextField("Comment", favData.getComment(), 1024, TextField.ANY);
		name = new TextField("Name", favData.getName(), 128, TextField.ANY);
		img = new ImageItem("Image", imageProvider.getImage(favData.getImageFilename()),
				ImageItem.LAYOUT_DEFAULT, "no image");
		genres = new StringItem("Genres", combineStringArray(favData.getGenres()));
		shortDescription = new StringItem("Description", favData.getShortDescription());
		activeMembers = new StringItem("Active members", combineStringArray(favData.getActiveMembers()));
		formerMembers = new StringItem("Former members", combineStringArray(favData.getFormerMembers()));
		significantAlbums = new StringItem("Signf. albums", combineStringArray(favData.getSignificantAlbums()));
		significantSongs = new StringItem("Signf. songs", combineStringArray(favData.getSignificantSongs()));
		similarArtists = new StringItem("Similar artists", combineStringArray(favData.getSimilarArtists()));
		
		append(ratingItem);
		append(comment);
		append(name);
		append(img);
		append(genres);
		append(shortDescription);
		append(activeMembers);
		append(formerMembers);
		append(significantAlbums);
		append(significantSongs);
		append(similarArtists);
		
		addCommand(new ActionCommand(Actions.RATING_DONE, "Done", Command.OK, 0));
		addCommand(new ActionCommand(Actions.REMOVE_FAVOURITE, "Delete", Command.SCREEN, 1));
		addCommand(new ActionCommand(Actions.BACK, "Back", Command.BACK, 0));
		setCommandListener(commandHandler);
	}
	
	/**
	 * Combine string array to one string so it can be displayed in a TextField.
	 * @param array
	 * @return Combined string.
	 */
	private String combineStringArray(String[] array){
		StringBuffer strBuf = new StringBuffer();
		for(int i = 0; i < array.length; i++){
			strBuf.append(array[i]);
			if(i < array.length - 1){
				strBuf.append(", ");
			}
		}
		return strBuf.toString();
	}
	
	/**
	 * Getter for rating value.
	 * 
	 * @return rating value
	 */
	public short getRatingValue(){
		return ratingItem.getRating();
	}
	
	/**
	 * Getter for comment.
	 * 
	 * @return Comment
	 */
	public String getComment(){
		return comment.getString();
	}
	
	/**
	 * Getter for name.
	 * 
	 * @return Name
	 */
	public String getName(){
		return name.getString();
	}
	
	/**
	 * Getter for favourite data.
	 * 
	 * @return fav data
	 */
	public FavouriteData getFavouriteData(){
		return favData;
	}
}
