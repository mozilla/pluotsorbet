/*
 * Copyright � 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

import java.io.IOException;
import java.util.Vector;
import java.util.Random;

import javax.microedition.lcdui.*;

import com.nokia.example.favouriteartists.tool.Log;

/**
 * This class controls the operation of the MIDlet, including command handling and view creation etc.
 */
public class Control implements CommandHandler, ImageProvider {
	
	// Constants
	/** Alert timeout in milliseconds. */
    private static final int ALERT_TIMEOUT = 3000;
    
	/** Enabled value for the JAD attributes. */
    public static final String ATTR_ENABLED = "true";
    
    /** Images for testing purposes */
    private static final String[] TEST_IMG_NAMES = {
        "midlets/favouriteartists/images/artist_1.png",
        "midlets/favouriteartists/images/artist_2.png",
        "midlets/favouriteartists/images/artist_3.png",
        "midlets/favouriteartists/images/artist_4.png",
    };
    
    /** Comments for testing. */
    private static final String[] COMMENTS = {
    	"This artist sucks!",
    	"This artist is bad.",
    	"This artist is average.",
    	"This artist is ok.",
    	"This artist rocks!",
    };
    
    // Inner classes
    /** 
     * Data structure class for image cache
     **/
    private class ImageData{
    	/** Filename of the image, used as identifier */
    	public String fileName;
    	/** The image object */
    	public Image image;
    }

    
    // Member data
	/** Reference of the midlet class.*/
	private FavouriteArtistsMIDlet mainApp;	
	/** A <code>Display</code> object.*/
	private Display display;	
	/** Favourite artists view, main view of the app. */
	private FavouriteArtistsView favouritesView;	
	/** Image cache. */
	private Vector imageCache;	
	/** Artist data. */
	private ArtistData[] artistDatas;	
	/** Favourite data. */
	private Vector favouriteDatas;
	
	// Methods    
    /**
	 * Constructor.
	 * 
	 * @param app reference to the main midlet.
	 */
	public Control(FavouriteArtistsMIDlet app){
		mainApp = app;
	}
	
	/**
	 * Separate initialization function for convenience.
	 */
	public void initialize(){
		if (Log.TEST) Log.note("[Control#initialize]-->");
		display = Display.getDisplay(mainApp);
		
		imageCache = new Vector();
		
		// Hard-coded artist data for testing purposes
        artistDatas = new ArtistData[50];
        for (int i = 0; i < artistDatas.length; i++) {
        	 ArtistData artistData = new ArtistData();
        	 artistData.setName("Artist " + i);
        	 artistData.setGenres(new String[]{"Genre " + i});
        	 artistData.setShortDescription("Descr. " + i);
        	 artistData.setActiveMembers(new String[]{"Active members " + i});
        	 artistData.setFormerMembers(new String[]{"Former members " + i});
        	 artistData.setSignificantAlbums(new String[]{"Sgnf. albums " + i});
        	 artistData.setSignificantSongs(new String[]{"Sgnf. songs " + i});
        	 artistData.setSimilarArtists(new String[]{"Similar artists " + i});
        	 String imageFileName = TEST_IMG_NAMES[i % TEST_IMG_NAMES.length];
        	 artistData.setImageFileName(imageFileName);
        	 artistDatas[i] = artistData;
        }
        
        // Hard-coded favourite data for testing purposes
        favouriteDatas = new Vector(15);
        Random generator = new Random();
    	generator.setSeed(System.currentTimeMillis());
        for (int i = 0; i < 15; i++) {
        	FavouriteData favouriteData = new FavouriteData(artistDatas[i]);
        	short rating = (short)generator.nextInt(4);
        	favouriteData.setRating(rating);
        	favouriteData.setComment(COMMENTS[rating]);
        	favouriteDatas.addElement(favouriteData);
        }
        
        // Initialize main view		
		try {
			favouritesView = new FavouriteArtistsView(this, display, this);
		} catch (FavouriteArtistsException e) {
			if (Log.TEST) Log.error("[Control#initialize] exception: " + e);
			return;
		}
		
		// Update main view
		updateFavouritesView(false);
		
		// Display main view		
		display.setCurrent(favouritesView);
	}
	
	/**
	 * Updates the favourites view with new data
	 * 
	 * @param repaint If true, a repaint is initiated for the view.
	 */ 
	private void updateFavouritesView(boolean repaint){
		if (Log.TEST) Log.note("[Control#updateFavouritesView]-->");
		FavouriteData[] favArray = new FavouriteData[favouriteDatas.size()];
        favouriteDatas.copyInto(favArray);
        favouritesView.updateView(favArray, repaint);
	}
	
	/**
	 * Retrieves image from cache if already loaded or creates new image from file.
	 * 
	 * @see ImageProvider#getImage(String)
	 */
	public Image getImage(String imageFilename){
		if (Log.TEST) Log.note("[Control#getImage]-->");
		
		if(imageFilename == null){
			if (Log.TEST) Log.note("[Control#getImage] null param");
			return null;
		}
		
		// Search for image
		for(int i = 0; i < imageCache.size(); i++){
			ImageData imageData = (ImageData)imageCache.elementAt(i);
			if(imageFilename.equals(imageData.fileName)){
				if (Log.TEST) Log.note("[Control#getImage] image found from cahce");
				return imageData.image;
			}
		}
		// Create new image
		ImageData imgData = new ImageData();
		try{
			if (Log.TEST) Log.note("[Control#getImage] creating new image");
			imgData.image = Image.createImage("/" + imageFilename);
			imgData.fileName = imageFilename;
			imageCache.addElement(imgData);
		} catch (IOException e) { 
		  if (Log.TEST) Log.error("[Control#getImage] Exception: " + e.getMessage());
		}
		return imgData.image;
	}
	
	/**
	 * Set a view to the current display.
	 * 
	 * @param disp a view to set.
	 */
	public void setCurrent(Displayable disp){
		if (display == null)
			display = Display.getDisplay(mainApp);
		display.setCurrent(disp);
	}
	
	/**
     * @see CommandListener#commandAction(Command, Displayable)
     */
    public void commandAction(Command c, Displayable d) {
    	if (Log.TEST) Log.note("[Control#commandAction]--> CommandListener");
        
        ActionCommand ac = (ActionCommand) c;
        short actionId = ac.getActionId();
        handleAction(actionId, null, d);    
    }
    
    /**
     * @see ItemCommandListener#commandAction(Command, Item)
     */
    public void commandAction(Command c, Item item) {
    	if (Log.TEST) Log.note("[Control#commandAction]--> ItemCommandListener");
        
        ActionCommand ac = (ActionCommand) c;
        short actionId = ac.getActionId();
        handleAction(actionId, item, null);
    }
    
	/**
     * @see com.nokia.example.favouriteartists.CommandHandler#handleAction(short, Item, Displayable)
     */
    public synchronized void handleAction(short actionId, Item item, Displayable view) {
    	
    	if (Log.TEST) Log.note("[Control#handleAction]-->");
    	switch(actionId) {
    	
	    	case Actions.EXIT_MIDLET:{
	    		if (Log.TEST) Log.note("[Control#handleAction] Actions.EXIT_MIDLET");
	    		exitMidlet();
	    		break;
	    	}
	    	case Actions.BACK:{
	    		if (Log.TEST) Log.note("[Control#handleAction] Actions.BACK");
	    		// Favourites view is always the view to go back to
	    		displayFavouritesView();
	    		break;
	    	}    	
	    	case Actions.SHOW_RATING:{
	    		if (Log.TEST) Log.note("[Control#handleAction] Actions.SHOW_RATING");	    		     		
	    		// Get selected item from the view. Should be valid during action handling.
	    		FavouriteData favData = favouritesView.getSelectedItem();
	    		try {
	    			RatingView ratingView = new RatingView(favData, this, this);
	    			display.setCurrent(ratingView);
				} catch (FavouriteArtistsException e) {
					displayNote(null, "Operation failed!", AlertType.ERROR);
				}
	    		break;
	    	}
	    	case Actions.RATING_DONE:{
	    		if (Log.TEST) Log.note("[Control#handleAction] Actions.RATING_DONE");
	    		// Rating is done, get the fav data that needs to be changed.
	    		RatingView ratingView = (RatingView)view;
	    		FavouriteData favData = ratingView.getFavouriteData();
	    		// Item in the Favourites view has reference to same object, so we can
	    		// modify it directly.
	    		favData.setName(ratingView.getName());
	    		favData.setRating(ratingView.getRatingValue());
	    		favData.setComment(ratingView.getComment());
	    		displayFavouritesView();
	    		break;
	    	}
	    	case Actions.SHOW_ADD_FAVOURITE:{
	    		if (Log.TEST) Log.note("[Control#handleAction] Actions.SHOW_ADD_FAVOURITE");
	    		// Open add fav view
	    		AddFavouriteView addFavView = null;
				try {
					addFavView = new AddFavouriteView(this, display, this, artistDatas);
				} catch (FavouriteArtistsException e) {
					displayNote(null, "Operation failed!", AlertType.ERROR);
					break;
				}
	    		display.setCurrent(addFavView);
	    		break;
	    	}
	    	case Actions.ADD_FAVOURITE:{
	    		if (Log.TEST) Log.note("[Control#handleAction] Actions.ADD_FAVOURITE");
	    		AddFavouriteView addFavView = (AddFavouriteView)view;
	    		// Get selected artist
	    		ArtistData artistData = addFavView.getSelectedItem().getData();
	    		// Create a new favourite from the artist
	    		FavouriteData favData = new FavouriteData(artistData);
	    		// Set some default comment for the favourite, for testing purposes.
	    		favData.setComment(COMMENTS[favData.getRating()]);
	    		favouriteDatas.addElement(favData);
	    		// Update favourites view, needed because amount of items has changed.
	    		updateFavouritesView(false);
	    		displayFavouritesView();
	    		break;
	    	}
	    	
	    	case Actions.REMOVE_FAVOURITE:{
	    		if (Log.TEST) Log.note("[Control#handleAction] Actions.REMOVE_FAVOURITE");
	    		RatingView ratingView = (RatingView)view;
	    		// Get the item and remove it
	    		FavouriteData favToRemove = ratingView.getFavouriteData();
	    		favouriteDatas.removeElement(favToRemove);
	    		// Update favourites view, needed because amount of items has changed.
	    		updateFavouritesView(false);
	    		displayFavouritesView();
	    		break;
	    	}
	    	case Actions.ARRANGE_FAVOURITES:{
	    		arrangeFavourites();
	    		updateFavouritesView(true);
	    		break;
	    	}
    	}
    }
	
    
    /**
     * Show the main view.
     */
    private void displayFavouritesView() {
    	if (Log.TEST) Log.note("[Control#displayFavouritesView]-->");
        display.setCurrent(favouritesView);
    }
    
    /**
     * Called when the user has chosen to exit the MIDlet or when
     * the MIDlet receives a <code>destroyApp()</code> call.
     */
   public final void exitMidlet() {
        if (Log.TEST) Log.note("[Control#exitMidlet]-->");         
        mainApp.closeLogging();
        mainApp.notifyDestroyed();
    }
   	
   /**
     * Displays an alert that times out after {@link #ALERT_TIMEOUT} milliseconds.
     * The shown alert has no command and it will not trigger any action.
     */
    public void displayNote(String title, String msg, AlertType type) {        
        Alert alert = new Alert(title, msg, null, type);
        alert.setTimeout(ALERT_TIMEOUT);
        displayAlert(alert);
    }
    
    /**
     * Display an alert.
     * 
     * @param alert The alert to display.
     */
    private void displayAlert(Alert alert) {
        
        try {
            display.setCurrent(alert, favouritesView);
        } catch (Exception e) {
            if (Log.TEST) Log.error("[Control#displayAlert] error:", e);
        }
    }
    
    /**
     * Arrange favourites by rating.
     */
    private void arrangeFavourites(){
    	
    	boolean swapped;
    	do{
    		swapped = false;
    		for( int i = 0; i <= favouriteDatas.size() - 2; i++){
        		FavouriteData element1 = (FavouriteData)favouriteDatas.elementAt(i);
        		FavouriteData element2 = (FavouriteData)favouriteDatas.elementAt(i + 1);
        		if( element1.rating < element2.rating ){
        			favouriteDatas.setElementAt(element2, i);
        			favouriteDatas.setElementAt(element1, i + 1);
        			swapped = true;
        		}
        	}
    	} while (swapped == true);
    }
}
