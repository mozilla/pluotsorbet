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
import javax.microedition.lcdui.Displayable;

/**
 * An interface to a text input mode, such as alphanumeric, numeric, etc. 
 *
 * Before sending key input to any InputMode, it should be verified that
 * it supports the input constraints of the target text component. This is
 * done by verifying that the input constraints of the text component
 * (as defined in the MIDP specification for TextField) are supported by
 * this InputMode by calling its supportsConstraints() method.
 *
 * The display name of this InputMode is returned by the getName() method.
 * This value should be a locale appropriate value and suitable for 
 * displaying on the screen, such as an option on the device's system
 * menu.
 *
 * The remaining methods represent a simple state machine with the following
 * characteristics:
 *
 * To begin an input session, the first call MUST be beginInput(). This 
 * establishes an input session with this InputMode and allows the InputMode
 * to perform any session initialization needed. The parameter to beginInput()
 * is the InputModeMediator for the input session. The InputModeMediator is
 * a callback mechanism for the InputMode to automatically commit pending
 * input as well as end the input session.
 *   
 * Subsequent key input is passed to the InputMode via the processKey()
 * method. This method returns a String element. The returned String
 * represents the best possible input value based on the total key input 
 * during the input session. For example, for an AlphaNumeric InputMode, 
 * the pending input will only ever be a single String, representing
 * the character that corresponds to the last key entered. A 'null' return 
 * value from processKey() is an indication that this InputMode did not 
 * process the key at all, and it may need to be processed by other elements
 *  of the system. 
 * 
 * For a predictive
 * text InputMode such as T9, there could be several possible matches.
 * The return value from processKey() in this instance will be what the
 * system believes to be the best possible match. The return value of
 * the hasMoreMatches() method will indicate whether the InputMode has
 * more than one possible match to the given input. The matches can then
 * be retrieved using the getNextMatch() method. 
 *
 * Key processing should be considered complete after the call to the
 * InputMode's endInput() method. At this point, the InputMode should
 * consider the reference to the TextInputMediator passed to its
 * beginInput() method to be no longer valid. 
 *
 * The InputMode may itself at any time commit pending input by calling the
 * InputModeMediator's commitInput() method. The InputMode may also end
 * the input session by calling the inputModeCompleted() method. Calling
 * this method will result in a subsequent call to this InputMode's 
 * endInput() method.
 *
 * A typical call exchange with an InputMode could go as follows:
 *
 * InputMode im = ...;
 * im.beginInput(inputModeHandler);
 *
 * int res = im.processKey([key code for 4]);
 * if (pendingInput == null) {
 *     // The InputMode could not process the key code
 * } else {
 *     // Present the pendingInput value to the user as the
 *     // best possible match
 *     ...
 *     while (im.hasMoreMatches()) {
 *         pendingInput = im.getNextMatch();
 *         // Present the pendingInput value to the user as
 *         // the best possible match
 *     }
 * }
 *
 * im.endInput();
 */
public interface InputMode {

    /** The key code does not mean to be displayed */
    public static final int KEYCODE_INVISIBLE = -4;

    /**
     * The key code is not handled by the input mode.
     * Most likely it is handled by the text component */
    public static final int KEYCODE_NONE = -3; // dont clash

    /** 
     * a sub-inputMode which may be supported by an InputMode.
     * all letters are uppercase  
     */
    public static final int CAPS_ON = 1;

    /** 
     * a sub-inputMode which may be supported by an InputMode.
     * all letters are lowercase  
     */
    public static final int CAPS_OFF = 2;

    /** 
     * a sub-inputMode which may be supported by an InputMode.
     * the first letter is capitalized, the rest of the letters are lowercase  
     */
    public static final int CAPS_SENTENCE = 3;

    /** The limitation for the number of matches */
    public static final int MAX_MATCHES = 20;

    
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
    public boolean supportsConstraints(int constraints);
    
    /**
     * Returns the display name which will represent this InputMode to 
     * the user, such as in a selection list or the softbutton bar.
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    public String getName();

    /**
     * Returns the command name which will represent this InputMode in
     * the input menu
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    public String getCommandName();
    
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
                           int constraints) 
        throws IllegalStateException;
    
    /**
     * Process the given key code as input.
     * 
     * This method will return true if the key was processed successfully,
     * false otherwise.
     *
     * @param keyCode the keycode of the key which was input
     * @param longPress return true if it's long key press otherwise false
     * @return the key code if the key has been committed for the input, or
     * KEYCODE_NONE if the key has not been habdled by the input mode, or
     * KEYCODE_INVISIBLE if the key has been handled by the input mode but
     * this key has not been displayed
     */
    public int processKey(int keyCode, boolean longPress) 
        throws IllegalStateException;

    /**
     * return the pending char
     * used to bypass the asynchronous commit mechanism
     * e.g. to immediately commit a char before moving the cursor
     * @return return the pending char
     */
    public char getPendingChar();

    /**
     * Return the next possible match for the key input processed thus
     * far by this InputMode. A call to this method should be preceeded
     * by a check of hasMoreMatches(). If the InputMode has more available
     * matches for the given input, this method will return them one by one.
     *
     * @return a String representing the next available match to the key 
     *         input thus far, or 'null' if no pending input is available
     */
    public String getNextMatch();
        
    /**
     * True, if after processing a key, there is more than one possible
     * match to the input. If this method returns true, the getNextMatch()
     * method can be called to return the value.
     *
     * @return true if after processing a key, there is more than the one
     *         possible match to the given input
     */
    public boolean hasMoreMatches();


    /**
     * Gets the possible string matches 
     *
     * @return returns the set of options.
     */
    public String[] getMatchList();
    
    /**
     * Mark the end of this InputMode's processing. The only possible call
     * to this InputMode after a call to endInput() is a call to beginInput()
     * to begin a new input session.
     */
    public void endInput() 
        throws IllegalStateException;

    /**
     * Gets displayable for particular input method. If the input method has no
     * specific displayable representation returns null.  
     * @return displayable 
     */
    public Displayable getDisplayable(); 
    

    /** 
     * Returns true if input mode is using its own displayable, false ifinput
     * mode does not require the speial displayable for its representation 
     * @return true if input mode is using its own displayable, otherwise false
     */
    public boolean hasDisplayable();

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
    public boolean[][] getIsConstraintsMap();
}


