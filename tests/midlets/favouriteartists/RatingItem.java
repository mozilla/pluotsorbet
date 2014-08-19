/*
 * Copyright � 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.nokia.example.favouriteartists.tool.Log;
import com.nokia.example.favouriteartists.tool.Util;
import com.nokia.mid.ui.gestures.*;


/**
 * Displays rating stars that can be tapped to modify the rating. Rating stars are displayed either empty or
 * filled, depending on rating. Tapping the first star when rating is 1 will change rating to 0.
 * As with other form items, the user first has to attain focus for the item by tapping it, after that,
 * the item will receive gesture events upon successive taps.
 * <p>
 * This class illustrates the use of Gestures API with multiple gesture zones and in a CustomItem.
 * Points of interests are: the constructor {@link #RatingItem(ImageProvider, int, short)} and
 * Gestures API callback {@link #gestureAction(Object, GestureInteractiveZone, GestureEvent)}
 * <p>
 * Gestures used in this example: 
 * {@link GestureInteractiveZone#GESTURE_TAP}
 */
public class RatingItem extends CustomItem implements GestureListener {

	// Constants
	/** Filled rating star image filename */
	private static final String BIG_STAR_FILLED_IMG_FILE = "midlets/favouriteartists/images/big_star_filled.png";
	/** Empty rating star image filename */
	private static final String BIG_STAR_EMPTY_IMG_FILE = "midlets/favouriteartists/images/big_star_empty.png";
	/** Vertical padding */
	private static final int V_PAD = 2;
	/** Horizontal padding */
	private static final int H_PAD = 20;
	/** Number of stars displayed in the item */
	private static final int STAR_COUNT = 3;
	/** Width of individual gesture zone */
	private static final int GESTURE_ZONE_WIDTH = 60;
	/** Height of individual gesture zone */
	private static final int GESTURE_ZONE_HEIGHT = 60;
	
	// Member data
	/** Width of the item */
	int width;
	/** Height of the item */
	int height;
	/** The font used for drawing title text */
	Font font;
	/** Filled star image */
	Image starImgFilled;
	/** Empty star image */
	Image starImgEmpty;
	/** The gesture zones for each star. */
	GestureInteractiveZone[] zones;
	/** Rating value. */
	int rating;
	
	
	/**
	 * Constructor.
	 * 
	 * @param imageProvider For image retrieval.
	 * @param width Width of the item.
	 * @param rating Initial rating.
	 * @throws FavouriteArtistsException
	 */
	protected RatingItem(ImageProvider imageProvider, int width, short rating) throws FavouriteArtistsException {
		super(null);
		
		this.width = width;
		this.rating = rating;
		
		// Create images
		starImgFilled = imageProvider.getImage(BIG_STAR_FILLED_IMG_FILE);
		starImgEmpty = imageProvider.getImage(BIG_STAR_EMPTY_IMG_FILE);
		
		// Get font
		font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
		
		// Calculate height
		height += V_PAD * 2;
		height += font.getHeight();
		height += GESTURE_ZONE_HEIGHT;
		
		// Create GIZ for each star icon
		zones = new GestureInteractiveZone[STAR_COUNT];
		int starX = 0; 
		int starY = font.getHeight() + V_PAD;
		for(int i = 0; i < STAR_COUNT; i++){
			GestureInteractiveZone zone =
				new GestureInteractiveZone(GestureInteractiveZone.GESTURE_TAP);
			// Set dimensions of the GIZ in relation to the Displayable
			zone.setRectangle(starX, starY, GESTURE_ZONE_WIDTH, GESTURE_ZONE_HEIGHT);
			// Register the GIZ
			if(GestureRegistrationManager.register(this, zone) != true){
	        	throw new FavouriteArtistsException("GestureRegistrationManager.register() failed!");
	        }
	        // Add a listener for gesture events.
	        GestureRegistrationManager.setListener(this, this);
			zones[i] = zone;
			starX += starImgFilled.getWidth() + H_PAD;
		}
	}
	
	/**
	 * @see javax.microedition.lcdui.CustomItem#getMinContentHeight()
	 */
	protected int getMinContentHeight() {
		
		return height;
	}

	
	/**
	 * @see javax.microedition.lcdui.CustomItem#getMinContentWidth()
	 */
	protected int getMinContentWidth() {
		
		return width;
	}

	
	/**
	 * @see javax.microedition.lcdui.CustomItem#getPrefContentHeight(int)
	 */
	protected int getPrefContentHeight(int width) {
		
		return height;
	}

	
	/**
	 * @see javax.microedition.lcdui.CustomItem#getPrefContentWidth(int)
	 */
	protected int getPrefContentWidth(int height) {
		
		return width;
	}

	
	/**
	 * @see javax.microedition.lcdui.CustomItem#paint(javax.microedition.lcdui.Graphics, int, int)
	 */
	protected void paint(Graphics g, int w, int h) {
		// Draw the title text
		Util.drawStringCenteredAndTruncated(g, "Rating:", font, 0, 0, w, font.getHeight(),
				Graphics.TOP | Graphics.LEFT );
		for(int i = 0; i < zones.length; i++) {
			GestureInteractiveZone zone = zones[i];
			Image image = null;
			// Determine whether a full or empty star needs to be drawn
			if(i < rating){
				image = starImgFilled;
			} else{
				image = starImgEmpty;
			}
			// Draw a star image
			Util.drawImageCentered(g, image, zone.getX(), zone.getY(),
					zone.getWidth(), zone.getHeight());
		}
	}
	
	/**
	 * Setter for rating.
	 * 
	 * @param rating
	 */
	public void setRating(short rating){
		this.rating = rating;
	}
	
	/**
	 * Getter for rating.
	 * 
	 * @return rating
	 */
	public short getRating(){
		return (short)rating;
	}
	
	/**
     * @see com.nokia.mid.ui.gestures.GestureListener#gestureAction(java.lang.Object, com.nokia.mid.ui.gestures.GestureInteractiveZone, com.nokia.mid.ui.gestures.GestureEvent)
     */
    public void gestureAction(Object container, GestureInteractiveZone zone, GestureEvent event) {
    	if (Log.TEST) Log.note("[RatingItem#gestureAction]-->");
    	
    	switch (event.getType()) {
    	case GestureInteractiveZone.GESTURE_TAP:{
    		if (Log.TEST) Log.note("[RatingItem#gestureAction] tap");
    		// Only tap gesture is handled here
    		for(int i = 0; i < zones.length; i++){
    			if(zone == zones[i]){
    				// Special case; change rating to zero if first star is tapped when rating is already 1.
    				if(rating == 1 && i == 0){
    					rating = 0;
    				} else {
    					rating = i + 1;
    				}
    				repaint();
    				break;
    			}
    		}
            break;
    	}
    	default:
    		break;
    	}
    }
}
