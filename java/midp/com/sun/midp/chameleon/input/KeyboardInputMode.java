/*
 *  
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

import javax.microedition.lcdui.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

/**
 * An InputMode instance which processes the numeric 0-9 keys
 * as their literal numeric values.
 */
public class KeyboardInputMode implements InputMode {

    /** A holder for the keyCode which was last processed */
    protected int lastKey = -1;
    
    /** The InputModeMediator for the current input session */
    protected InputModeMediator mediator;
    
    /**
     * This method is called to determine if this InputMode supports
     * the given text input constraints. The semantics of the constraints
     * value are defined in the javax.microedition.lcdui.TextField API. 
     * If this InputMode returns false, this InputMode must not be used
     * to process key input for the selected text component.
     *
     * @param constraints text input constraints. The semantics of the 
     * constraints value are defined in the TextField API.
     *
     * @return true if this InputMode supports the given text component
     *         constraints, as defined in the MIDP TextField API
     */
    public boolean supportsConstraints(int constraints) {
        return true;
    }

    /**
     * Returns the display name which will represent this InputMode to 
     * the user, such as in a selection list or the softbutton bar.
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    public String getName() {
        return Resource.getString(ResourceConstants.LCDUI_TF_KEYBOARD);
    }

    /**
     * Returns the command name which will represent this InputMode in
     * the input menu
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    public String getCommandName() {
        return getName();
    }
    
    /**
     * This method will be called before any input keys are passed
     * to this InputMode to allow the InputMode to perform any needed
     * initialization. A reference to the InputModeMediator which is
     * currently managing the relationship between this InputMode and
     * the input session is passed in. This reference can be used
     * by this InputMode to commit text input as well as end the input
     * session with this InputMode. The reference is only valid until
     * this InputMode's endInput() method is called.
     *
     * @param constraints text input constraints. The semantics of the 
     * constraints value are defined in the TextField API.
     *
     * @param mediator the InputModeMediator which is negotiating the
     *        relationship between this InputMode and the input session
     *
     * @param inputSubset current input subset
     */
    public void beginInput(InputModeMediator mediator, String inputSubset,
                           int constraints) {
        validateState(false);
        this.mediator = mediator;
    }
    
    /**
     * Returns true if input mode is using its own displayable, false ifinput
     * mode does not require the speial displayable for its representation.
     * By default - false 
     * @return true if input mode is using its own displayable, otherwise false
     */
    public boolean hasDisplayable() {
        return false;
    }
    
    /**
     * Process the given key code as input.
     * 
     * This method will return true if the key was processed successfully,
     * false otherwise.
     *
     * @param keyCode the keycode of the key which was input
     * @param longPress return true if it's long key press otherwise false
     * @return true if the key was processed by this InputMode, false
     *         otherwise.
     */
    public int processKey(int keyCode, boolean longPress) {
        int ret = KEYCODE_NONE;
        validateState(true);
        // if the key is printable one
        if (mediator != null &&
            !longPress) {
            if( keyCode >= ' ' && keyCode != 127 ) {
                mediator.commit("" + (char)keyCode);
            } else if ( mediator.isNewlineKey(keyCode)) {
                mediator.commit("\n");
            }
        }
        return ret;
    }

    /**
     * return the pending char
     * used to bypass the asynchronous commit mechanism
     * e.g. to immediately commit a char before moving the cursor
     * @return return the pending char
     */
    public char getPendingChar() {
        return 0;
    }
        
    
    /**
     * Return the next possible match for the key input processed thus
     * far by this InputMode. A call to this method should be preceeded
     * by a check of hasMoreMatches(). If the InputMode has more available
     * matches for the given input, this method will return them one by one.
     *
     * @return a String representing the next available match to the key 
     *         input thus far
     */
    public String getNextMatch() {
        return null;
    }

    /**
     * True, if after processing a key, there is more than one possible
     * match to the input. If this method returns true, the getNextMatch()
     * method can be called to return the value.
     *
     * @return true if after processing a key, there is more than the one
     *         possible match to the given input
     */
    public boolean hasMoreMatches() {
        return false;
    }

    /**
     * Gets the possible string matches 
     *
     * @return returns the set of options.
     */
    public String[] getMatchList() {
        return new String[0];
    }
    
    /**
     * Mark the end of this InputMode's processing. The only possible call
     * to this InputMode after a call to endInput() is a call to beginInput()
     * to begin a new input session.
     */
    public void endInput() {
        validateState(true);
        this.mediator = null;
    }

    /**
     * By default the regular input method has no specific displayable
     * representation so it returns null.  
     * @return null by default 
     */
    public Displayable getDisplayable() {
        return null;
    }

    /**
     * This method will validate the state of this InputMode. If this
     * is a check for an "active" operation, the TextInputMediator must
     * be non-null or else this method will throw an IllegalStateException.
     * If this is a check for an "inactive" operation, then the
     * TextInputMediator should be null.
     *
     * @param activeOperation true if any operation is active otherwise false.
     */
    protected void validateState(boolean activeOperation) {
        if (activeOperation && this.mediator == null) {
            throw new IllegalStateException(
            "Illegal operation on an input session already in progress");
        } else if (!activeOperation && this.mediator != null) {
            throw new IllegalStateException(
            "Illegal operation on an input session which is not in progress");
        }
    }

    /** input subset x constraint map */
    private static final boolean[][] isMap = {
        // |ANY|EMAILADDR|NUMERIC|PHONENUMBER|URL|DECIMAL
        { false, false, false, false, false, false }, // IS_FULLWIDTH_DIGITS
        { false, false, false, false, false, false }, // IS_FULLWIDTH_LATIN  
        { true,  true,  false, false, true,  false }, // IS_HALFWIDTH_KATAKANA
        { true,  true,  false, false, true,  false }, // IS_HANJA          
        { true,  true,  false, false, true,  false }, // IS_KANJI           
        { false, false, false, false, false, false }, // IS_LATIN           
        { false, false, false, false, false, false }, // IS_LATIN_DIGITS    
        { true,  true,  false, false, true,  false }, // IS_SIMPLIFIED_HANZI
        { true,  true,  false, false, true,  false }, // IS_TRADITIONAL_HANZI
        { false, false, false, false, false, false }, // MIDP_UPPERCASE_LATIN
        { false, false, false, false, false, false }, // MIDP_LOWERCASE_LATIN
        { true,  true,  false, false, true,  false }  // NULL
    };
    
    /**
     * Returns the map specifying this input mode is proper one for the
     * particular pair of input subset and constraint. The form of the map is
     *
     *                       |ANY|EMAILADDR|NUMERIC|PHONENUMBER|URL|DECIMAL|
     * ---------------------------------------------------------------------
     * IS_FULLWIDTH_DIGITS   |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_FULLWIDTH_LATIN    |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_HALFWIDTH_KATAKANA |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_HANJA              |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_KANJI              |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_LATIN              |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_LATIN_DIGITS       |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_SIMPLIFIED_HANZI   |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_TRADITIONAL_HANZI  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * MIDP_UPPERCASE_LATIN  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * MIDP_LOWERCASE_LATIN  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * NULL                  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     *
     * @return input subset x constraint map
     */
    public boolean[][] getIsConstraintsMap() {
        return isMap;
    }
}
