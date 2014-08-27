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
 * Data class for artist details.
 */
public class ArtistData {
	
	// Member data
	/** Name of the artist/band */
	private String name;
	/** Musical genres of the artist/band */
	private String[] genres;
	/** A short description of the artist/band */
	private String shortDescription;
	/** Currently active members */
	private String[] activeMembers;
	/** Former members */
	private String[] formerMembers;
	/** Significant albums */
	private String[] significantAlbums;
	/** Significant songs */
	private String[] significantSongs;
	/** Similar artists/bands */
	private String[] similarArtists;
	/** Image filename for the artist/band */
	private String imageFileName;
	
	// Methods
	/**
	 * Constructor.
	 */
	public ArtistData(){
		if (Log.TEST) Log.note("[ArtistData#ArtistData]-->");
	}
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param genres
	 * @param shortDescription
	 * @param activeMembers
	 * @param formerMembers
	 * @param significantAlbums
	 * @param significantSongs
	 * @param similarArtists
	 */
	public ArtistData(String name, String[] genres, String shortDescription,
			String[] activeMembers, String[] formerMembers,
			String[] significantAlbums, String[] significantSongs,
			String[] similarArtists, String imageFileName) {
		this.name = name;
		this.genres = genres;
		this.shortDescription = shortDescription;
		this.activeMembers = activeMembers;
		this.formerMembers = formerMembers;
		this.significantAlbums = significantAlbums;
		this.significantSongs = significantSongs;
		this.similarArtists = similarArtists;
		this.imageFileName = imageFileName;
	}
	
	/**
	 * Copy-constructor.
	 */
	public ArtistData(ArtistData artistData){
		this(artistData.getName(), artistData.getGenres(), artistData.getShortDescription(),
				artistData.getActiveMembers(), artistData.getFormerMembers(),
				artistData.getSignificantAlbums(), artistData.getSignificantSongs(),
				artistData.getSimilarArtists(), artistData.getImageFilename());
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the genres
	 */
	public String[] getGenres() {
		return genres;
	}
	/**
	 * @param genres the genres to set
	 */
	public void setGenres(String[] genres) {
		this.genres = genres;
	}
	/**
	 * @return the shortDescription
	 */
	public String getShortDescription() {
		return shortDescription;
	}
	/**
	 * @param shortDescription the shortDescription to set
	 */
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	/**
	 * @return the activeMembers
	 */
	public String[] getActiveMembers() {
		return activeMembers;
	}
	/**
	 * @param activeMembers the activeMembers to set
	 */
	public void setActiveMembers(String[] activeMembers) {
		this.activeMembers = activeMembers;
	}
	/**
	 * @return the formerMembers
	 */
	public String[] getFormerMembers() {
		return formerMembers;
	}
	/**
	 * @param formerMembers the formerMembers to set
	 */
	public void setFormerMembers(String[] formerMembers) {
		this.formerMembers = formerMembers;
	}
	/**
	 * @return the significantAlbums
	 */
	public String[] getSignificantAlbums() {
		return significantAlbums;
	}
	/**
	 * @param significantAlbums the significantAlbums to set
	 */
	public void setSignificantAlbums(String[] significantAlbums) {
		this.significantAlbums = significantAlbums;
	}
	/**
	 * @return the significantSongs
	 */
	public String[] getSignificantSongs() {
		return significantSongs;
	}
	/**
	 * @param significantSongs the significantSongs to set
	 */
	public void setSignificantSongs(String[] significantSongs) {
		this.significantSongs = significantSongs;
	}
	/**
	 * @return the similarArtists
	 */
	public String[] getSimilarArtists() {
		return similarArtists;
	}
	/**
	 * @param similarArtists the similarArtists to set
	 */
	public void setSimilarArtists(String[] similarArtists) {
		this.similarArtists = similarArtists;
	}

	/**
	 * @return the imageFileName
	 */
	public String getImageFilename() {
		return imageFileName;
	}

	/**
	 * @param imageFileName the imageFileName to set
	 */
	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
}
