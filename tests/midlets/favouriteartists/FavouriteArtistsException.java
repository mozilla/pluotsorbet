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
 * Own exception class for identifying application-specific exceptions.
 */
public class FavouriteArtistsException extends Exception {
	
	/**
	 * Constructs with the specified detail message.
	 * 
	 * @param s detail message
	 */
	public FavouriteArtistsException(String s) {
		super(s);
	}
}
