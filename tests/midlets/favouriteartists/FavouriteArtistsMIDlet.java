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
import com.nokia.mid.ui.VirtualKeyboard;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.*;

/**
 * This is the main class of the Favourite Artists MIDlet.
 * <p>
 * The purpose of this MIDlet is to give an introduction for creating an
 * UI-application for S40 Touch UI and more specifically, to demonstrate
 * some example use cases for Gestures API and Frame Animator API. 
 */
public class FavouriteArtistsMIDlet extends MIDlet {

	/** A <code>Display</code> instance.*/
	private Display display;
	
	/** Controls the operation of the midlet.*/
	private Control control;
	
	/**
	 * Creates the midlet and initiates the display.
	 */
	public FavouriteArtistsMIDlet() {
		display = Display.getDisplay(this);
	}
	
	/**
	 * Destroys the midlet and exit.
	 */
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		if (Log.TEST) Log.note("[destroyApp] ");
		control.exitMidlet();
	}
	
	/**
	 * Called when the midlet is paused.
	 */
	protected void pauseApp() {
		
	}
	
	/**
	 * Called when the midlet starts.
	 */
	protected void startApp() throws MIDletStateChangeException {
		initLogging();
		control = new Control(this);
		control.initialize();
		
	    /**  
	     * VirtualKeyboard is supported for Java Runtime 2.0.0 for Series 40
	     * onwards. This hides the open keypad command from the options menu.
	     */
            if(System.getProperty("com.nokia.keyboard.type").equals("None"))
            {
                VirtualKeyboard.hideOpenKeypadCommand(true);
            }


	}
	
	/**
	 * Sets a view to the display.
	 * @param disp a Displayable object to display.
	 */
	public void setCurrentDisplay(Displayable disp){
		display.setCurrent(disp);
	}
	
	private void initLogging() {
        // Read default log settings from Jad attributes.
		if (Log.TEST) Log.note("[FavouriteArtistsMIDlet.initLogging]");
		Log.initLogging(this, "FavouriteArtists");
                if (Log.TEST) Log.note("===========================================================");
        }
	
	/**
	 * Closes the logging.
	 */
	public void closeLogging(){
		try{
			if (Log.TEST) Log.note("[FavouriteArtistsMIDlet.closeLogging]  closing log file");
			Log.closeLogging();
		} catch(Exception e){
			if (Log.TEST) Log.note("[FavouriteArtistsMIDlet.closeLogging]  " + e);
		}
	}
}
