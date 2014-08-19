/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

import javax.microedition.lcdui.Image;

/**
 * Image provider interface.
 */
public interface ImageProvider {

	/**
	 * Retrieves image from a file.
	 * 
	 * @param imageFilename File name of the image.
	 * @return The image requested or null if image retrieval failed for some reason.
	 */
	public Image getImage(String imageFilename);
}
