/*
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */
package com.sun.midp.chameleon.input;

/**
 * The TextInputSession interface represents the relationship between the
 * system's key input, the TextInputComponent, the available InputModes, 
 * and the graphical display. 
 */
public interface TextInputSession {
    /** The names of the currently defined input subsets */
    public static final String[] INPUT_SUBSETS = {
        "IS_FULLWIDTH_DIGITS",
        "IS_FULLWIDTH_LATIN",
        "IS_HALFWIDTH_KATAKANA",
        "IS_HANJA",
        "IS_KANJI",
        "IS_LATIN",
        "IS_LATIN_DIGITS",
        "IS_SIMPLIFIED_HANZI",
        "IS_TRADITIONAL_HANZI",
        "MIDP_UPPERCASE_LATIN",
        "MIDP_LOWERCASE_LATIN"
    };

    /** max number of constraints */
    public static final int MAX_CONSTRAINTS = 6;
    
    /**
     * Start a text input session for the given TextInputComponent.
     * The TextInputComponent can be used to determine the initial
     * input mode, constraints, etc.
     *
     * @param component the TextInputComponent which is receiving text input
     */
    public void beginSession(TextInputComponent component);

    /**
     * List the appropriate InputModes available for the current input
     * session.  This method may be used by UI components in order to make
     * certain input mode choices available to the user for selection.
     * If this handler is not currently in an active text input session,
     * this method returns null.
     *
     * @return an array of InputModes which are available to use given the
     *         current TextInputComponent and its input constraints
     */
    public InputMode[] getAvailableModes();

    /**
     * Retrieve the InputMode which is the current "active" mode
     * for this TextInputSession. This does not necessarily mean there is 
     * any pending input with the InputMode itself, it means that if this 
     * TextInputSession receives key input, the returned InputMode will be
     * the mode which processes that input.
     *
     * @return the currently "active" InputMode
     */
    public InputMode getCurrentInputMode();

    /**
     * Set this TextInputSession's current "active" InputMode to the
     * given mode. The given mode must be one of the InputModes listed
     * in the array of InputModes returned from the getAvailableModes()
     * method of this TextInputSession. Calling this method will terminate
     * any existing input session with the current InputMode and will
     * result in any subsequent key input being processed by the given
     * InputMode. If the given mode is already the current "active"
     * InputMode, this method has no effect. If this TextInputSession
     * is not currently in an input session (ie, there is no active
     * TextInputComponent), this method has no effect.
     *
     * @param mode the InputMode to switch key processing to
     */
    public void setCurrentInputMode(InputMode mode);    
    
    /**
     * This method abstracts key processing to a single call (from
     * the assorted key press, release, repeat events). This method
     * should be called from the TextInputComponent to pass along
     * key input from the user. The TextInputComponent is responsible
     * for determining what key events should be processed (ie,
     * key events trigger processing on press or on release).
     * 
     * @param keyCode the numeric code representing the key which was
     *        pressed
     * @param longPress return true if it's long key press otherwise false
     * @return true if the current InputMode processed the key event,
     *         false if the key was not processed at all by the current
     *         InputMode (not all keys apply to input)
     */
    public int processKey(int keyCode, boolean longPress);


    /**
     * return the pending char
     * used to bypass the asynchronous commit mechanism
     * e.g. to immediately commit a char before moving the cursor
     *
     * @return return the pending char
     */
    public char getPendingChar();
    
    /**
     * An iterative method to return the next available match given
     * the key processing thus far. If the return value of hasMoreMatches()
     * is true, this method will return a non-null String and will iterate
     * through the entire set of available matches until the set is exhausted.
     *
     * Each subsequent call to processKey() will reset the iterator over
     * the set of available matches regardless if the key resulted in a change
     * to the set.
     *
     * The two methods, hasMoreMatches() and getNextMatch(), can be used by 
     * the User Interface system to retrieve the current set of pending inputs
     * and possibly present a chooser option to the user.
     *
     * @return a String representing the best possible pending
     *         input, or null, if there is no pending input
     */
    public String getNextMatch();
    
    /**
     * If the InputMode supports multiple matches and more matches are
     * available this method will return true, false otherwise.
     * 
     * @return true if the current InputMode supports multiple matches and
     *         there are currently more matches available
     */
    public boolean hasMoreMatches();

    /**
     * Gets the possible string matches 
     *
     * @return returns the set of options.
     */
    public String[] getMatchList();
       
    /**
     * End the current text input session and do not commit any pending
     * input to the buffer.
     */
    public void endSession();

    /**
     * Check if the given char is symbol
     * @param c char 
     * @return true if the char is symbol otherwise false. 
     */
    public boolean isSymbol(char c);
        
}
