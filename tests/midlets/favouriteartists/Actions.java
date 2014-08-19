/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

/**
 * This class defines the action identifier constants used with ActionCommands. 
 */
package com.nokia.example.favouriteartists;

public class Actions {
    
    /** An invalid action identifier. Can be used for 'no action' identifier if needed. */
    public static final short INVALID_ACTION_ID = -1;
    
    /** Minimum possible valid action identifier. */
    public static final short MIN_VALID_ACTION_ID = 1;
    
    /** Returns to the previous UI view. */
    public static final short BACK = 1;
    
    /** Exit (terminate) the MIDlet and release all resources.*/
    public static final short EXIT_MIDLET = 2;
    
    /** Open add favourite view.*/
    public static final short SHOW_ADD_FAVOURITE = 3; 
    
    /** Add favourite.*/
    public static final short ADD_FAVOURITE = 4;
    
    /** Start arrange mode.*/
    public static final short ARRANGE_FAVOURITES = 5;
    
    /** End arrange mode.*/
    public static final short ARRANGE_FAVOURITES_DONE = 6;
    
    /** Open rating view.*/
    public static final short SHOW_RATING = 7;
    
    /** Rating is done.*/
    public static final short RATING_DONE = 8;
    
    /** Remove favourite. */
    public static final short REMOVE_FAVOURITE = 9;
}

