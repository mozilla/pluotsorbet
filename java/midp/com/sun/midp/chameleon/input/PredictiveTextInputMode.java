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
import com.sun.midp.lcdui.*;
import com.sun.midp.configurator.Constants;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;

/**
 * An InputMode instance which processes the numeric 0-9 keys
 * as their literal numeric values.
 */
public class PredictiveTextInputMode implements InputMode {

    /** The InputModeMediator for the current input session */
    protected InputModeMediator mediator;
    
    /**
     * Currently formatted predictive word from the PTIterator
     */
    private String part;

    /**
     * Iterator interface to the predictive dictionary
     */
    private PTIterator  iterator;

    /**
     * Keeps the current state strign and compares diffs between current and
     * next states Class StringDiff is an inner-class defined below
     */
    private StringDiff diff;

    /** array of sub-inputModes supported by this inputMode */
    private static final int[] CAPS_MODES = {
        CAPS_SENTENCE, 
        CAPS_OFF,
        CAPS_ON
    }; 

    /** array of sub-inputModes labels, corresponding to CAPS_MODES array */
    private static final String[] CAPS_MODES_LABELS = {
        Resource.getString(ResourceConstants.LCDUI_TF_CAPS_SENTENCE_LB_PTI),
        Resource.getString(ResourceConstants.LCDUI_TF_CAPS_OFF_LB_PTI),
        Resource.getString(ResourceConstants.LCDUI_TF_CAPS_ON_LB_PTI) };

    /** points to an element of CAPS_MODES which is the current sub-inputMode */
    private int capsMode = 0;

    /** Init predictive text input mode */ 
    private void init() {
        iterator = PTDictionaryFactory.getDictionary().iterator();
        if (diff == null) {
            diff = new StringDiff();
        } 
        clear();
    }
    
    /**
     * This method is called to determine if this InputMode supports
     * the given text input constraints. The semantics of the constraints
     * value are defined in the javax.microedition.lcdui.TextField API. 
     * If this InputMode returns false, this InputMode must not be used
     * to process key input for the selected text component.
     * @param constraints current constraints.
     * The constraints format is defined in TextField. 
     *
     * @return true if this InputMode supports the given text component
     *         constraints, as defined in the MIDP TextField API
     */
    public boolean supportsConstraints(int constraints) {
        boolean isSupported = false;
        if ((constraints & TextField.CONSTRAINT_MASK) == TextField.ANY ||
            (constraints & TextField.CONSTRAINT_MASK) == TextField.URL) {
            isSupported = true;
        }
        if ((constraints & TextField.NON_PREDICTIVE) > 0) {
            isSupported = false; 
        }
        if ((constraints & TextField.SENSITIVE) > 0) {
            isSupported = false; 
        }
        if ((constraints & TextField.PASSWORD) > 0) {
            isSupported = false;        
        }
        return isSupported;
    }

    /**
     * Returns the display name which will represent this InputMode to 
     * the user, such as in a selection list or the softbutton bar.
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    public String getName() {
        return CAPS_MODES_LABELS[capsMode];
    }

    /**
     * Returns the command name which will represent this InputMode to 
     * the user
     *
     * @return the locale-appropriate command name to represent this InputMode
     *         to the user
     */
    public String getCommandName() {
        return Resource.getString(ResourceConstants.LCDUI_TF_CMD_PTI);
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
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[*** beginInput]");
        }
        validateState(false);
        this.mediator = mediator;
        // need to re-init dictionary every time because the language/locale
        // can be changed.
        init();
    }

    /**
     * Mark the end of this InputMode's processing. The only possible call
     * to this InputMode after a call to endInput() is a call to beginInput()
     * to begin a new input session.
     */
    public void endInput() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "********[endInput]");
        }
        validateState(true);
        this.mediator = null;
        clear();
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
     * Returns true if input mode is using its own displayable, false ifinput
     * mode does not require the speial displayable for its representation.
     * By default returns false 
     * @return true if input mode is using its own displayable, otherwise false
     */
    public boolean hasDisplayable() {
        return false;
    }

    /**
     * Clear the iterator
     */
    public void clear() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[clear]");
        }
        diff.clear();
        part = "";
        iterator.reset();
    }

      
    /**
     * Process the given key code as input.
     * 
     * This method will return true if the key was processed successfully,
     * false otherwise.
     *
     * @param keyCode the keycode of the key which was input
     * @param longPress true if long key press happens, otherwise false.
     * @return the key code if the key has been committed for the input, or
     * KEYCODE_NONE if the key has not been habdled by the input mode, or
     * KEYCODE_INVISIBLE if the key has been handled by the input mode but
     * this key has not been displayed
     */
    public int processKey(int keyCode, boolean longPress) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[PT.processKey] keyCode = " + keyCode);
        }

        int ret = KEYCODE_NONE;
        boolean gotoNextState = true;
        boolean needClear = false;
        boolean needFinishWord = false;

        validateState(true);

        if (mediator != null && mediator.isClearKey(keyCode)) {
            if (longPress) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "         **isClearALL**");
                }
                clear();
                gotoNextState = false;
            } else {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "         **isClearOne**");
                }
                if (part.length() <= 1) {
                    clear();
                    gotoNextState = false;
                    //                    return KEYCODE_NONE;
                } else {
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                            "           part.length()>1");
                    }
                    iterator.prevLevel();
                    part = getNextMatch();
                    // part=part.substring(0, part.length()-1);
                    // diff.stateModified(part);
                    ret = KEYCODE_INVISIBLE;
                }
            }
        } else if (longPress) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "         **longPress**");
            }
            needFinishWord = true;
            if (isValidKey(keyCode)) {
                // if (part.length()>0) {
                // }
                part = part.substring(0, part.length() - 1) +
                    String.valueOf((char) keyCode);
            }
            needClear = true;
        } else if (isNextOption(keyCode)) {
            /**
             * 2. handle '#' (show next completion option) case 
             */
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "         **isNextOption**");
            }

            if (part.length() == 0) {
                gotoNextState = false;
            } else {
                part = getNextMatch();
            }
        } else if (isPrevOption(keyCode)) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "         **isPrev**");
            }
            part = getPrevMatch();
        } else if (isKeyMapChange(keyCode)) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "         **isKeyMapChange**");
            }
            /**
             * 3. handle '*' (key map change) case 
             */
            nextCapsMode();
        } else if (isWhiteSpace(keyCode)) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "         **isWhiteSpace**");
            }
            /**
             * 4. handle whitespace  
             */
            needFinishWord = true;
            if (keyCode == '#') {
                part = part + ' ';
            }
            needClear = true;
        } else {
            /**
             * 5. handle standard '2'-'9' keys
             */
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "         **is key to process**");
            }
            if (isValidKey(keyCode)) {
                processKeyCode(keyCode);
            } else {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "invalid key, returning KEYCODE_NONE.");
                }
                gotoNextState = false;
            }
        }

        /**
         * Call StringDiff.nextState() method with the next resulting entry
         * output StringDiff will check if the underlying entry changed and 
         * will invoke the InputMethodClient with the differences.
         */
        if (gotoNextState) {
            diff.nextState(part);
            if (needClear) {
                clear();
            }
        }
        if (needFinishWord) {
            finishWord();
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
     * Process a new key in range '2'-'9'.
     * Advnace the iterator and update the word part
     *
     * @param keyCode char in range '0'-'9','#', '*'
     */
    void processKeyCode(int keyCode) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[processKeyCode] keyCode=" + keyCode);
        }
        iterator.nextLevel(keyCode);
        if (iterator.hasNext()) {
            part = iterator.next();
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "iterator.hasNext = true\n" +
                    "iterator part\n" +
                    "     :) [processKeyCode] iterator.hasNext: part=" + part);
            }
        } else {
            // ignore the key
            // part=part+keyCode2Char(keyCode);
            // IMPL NOTE: Consider a better solution: maybe jump to standard mode?
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXX\n\n" +
                    ":( [processKeyCode] !iterator.hasNext part=" + part + "\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXX");
            }
        }
        part = modifyCaps(part);
    }


    /**
     * Modify the caps for the string depending on the current caps mode
     * @param str text
     * @return the same text with required caps 
     */
    private String modifyCaps(String str) {
        // log("[handleCaps] capsMode=" + CAPS_MODES_LABELS[capsMode]);
        String ret = str;
        if (str != null && str.length() > 0) { 
            switch (CAPS_MODES[capsMode]) { 
            case  CAPS_OFF: 
                ret = str.toLowerCase(); 
                break; 
            case  CAPS_ON: 
                ret = str.toUpperCase(); 
                break; 
            case  CAPS_SENTENCE: 
                str = str.toLowerCase(); 
                char[] chars = str.toCharArray();  
                chars[0] = Character.toUpperCase(chars[0]); 
                ret = new String(chars); 
                break; 
            }
        }
        return ret; 
    }
    
    /**
     * Check if keyCode represents a whitespace key (i.e. not in '2'..'9')
     *
     * @param keyCode char in range '0'-'9','#', '*', CLEAR
     * @return true if whitespace, false otherwise
     */
    private boolean isWhiteSpace(int keyCode) {
        return keyCode == '#' ||
            keyCode == Canvas.LEFT ||
            keyCode == Canvas.RIGHT ||
            keyCode == Constants.KEYCODE_SELECT;
    }

    /**
     * Check if keyCode indicates a next completion key event
     *
     * @param keyCode key code
     * @return true if next completion key, false otherwise
     */
    private boolean isNextOption(int keyCode) {
        return keyCode == Canvas.DOWN;
    }

    /**
     * Check if keyCode indicates a previous completion key event
     *
     * @param keyCode key code
     * @return true if prev completion key, false otherwise
     */
    private boolean isPrevOption(int keyCode) {
        return keyCode == Canvas.UP;
    }

    /**
     * Check if keyCode represents a change of keymap event ('*' key)
     *
     * @param keyCode key code
     * @return true if keymap chage key code, false otherwise
     */
    private boolean isKeyMapChange(int keyCode) {
        return keyCode == '*';
    }

    /**
     * Check if the key has to be handled this input mode
     * @param keyCode key
     * @return true if this key can be  handled by this input mode,
     * otherwise false
     */
    private boolean isValidKey(int keyCode) {
        int available = mediator != null ?
            mediator.getAvailableSize() : 0;

        return available > 0 &&
            keyCode >= '0' && keyCode <= '9';
    }

    /**
     * Return the next possible match for the key input processed thus
     * far by this InputMode. A call to this method should be preceeded
     * by a check of hasMoreMatches(). If the InputMode has more available
     * matches for the given input, this method will return them one by one.
     *
     * @return a String representing the next available match to the key 
     *         input thus far, or 'null' if no pending input is available
     */
    public String getNextMatch() {
        String retStr = null;
        if (part == null || part.length() == 0) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "[getNextMatch] <<< returning null");
            }
            return null;
        }

        if (!iterator.hasNext()) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "     [getNextMatch] rewinding...");
            }
            iterator.resetNext();
        }
        if (iterator.hasNext()) {
            retStr = iterator.next();
            retStr = modifyCaps(retStr);
        }
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[getNextMatch] <<< returning " + retStr);
        }
        return retStr;
    }



    /**
     * Return the previous possible match for the key input processed thus
     * far by this InputMode. 
     *
     * @return a String representing the previous available match to the key 
     *         input thus far, or 'null' if no pending input is available
     */
    public String getPrevMatch() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "getPrevMatch");
        }
        String prevMatch = "";
        String match = "";
        int num;
        if (part == null || part.length() == 0 ||
            (prevMatch = match = getNextMatch()) == null) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "[getPrevMatch] <<< returning empty str");
            }
            return prevMatch;
        }

        while (match.compareTo(part) != 0) {
            prevMatch = match;
            match = getNextMatch();
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[getPrevMatch] <<< returning " + prevMatch);
        }
        return prevMatch;

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
        // log("[hasMoreMatches]");
        return iterator.hasNext();
    }

    /**
     * Gets the possible string matches 
     *
     * @return returns the set of options.
     */
    public String[] getMatchList() {
        String[] ret = null;
        
        if (part == null || part.length() <= 0) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "getMatchList returning empty array");
            }
            ret = new String[0];
        } else {
            int num = 0;
            String[] matches = new String[MAX_MATCHES];
            String match = part;

            do {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "    [getMatchList()] got nother match: " + match);
                }
                matches[num] = match;
                num++;
            } while (num < MAX_MATCHES &&
                (match = getNextMatch()) != null &&
                match.compareTo(part) != 0);

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "getMatchList returning array of size " + num);
            }
            ret = new String[num];
            System.arraycopy(matches, 0, ret, 0, num);
        }
        return ret;
    }
        
    
    /**
     * This method will validate the state of this InputMode. If this
     * is a check for an "active" operation, the TextInputMediator must
     * be non-null or else this method will throw an IllegalStateException.
     * If this is a check for an "inactive" operation, then the
     * TextInputMediator should be null. 
     * @param activeOperation true if any operation is active otherwise false.
     */
    protected void validateState(boolean activeOperation) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[validateState]");
        }
        if (activeOperation && this.mediator == null) {
            throw new IllegalStateException(
                "Illegal operation on an input session already in progress");
        } else if (!activeOperation && this.mediator != null) {
            throw new IllegalStateException(
                "Illegal operation on an input session which is not in progress");
        }
    }

    /**
     * Set the next capital mode for this input method
     */
    private void nextCapsMode() {
        capsMode++;
        if (capsMode == CAPS_MODES.length) {
            capsMode = 0;
        }
        part = modifyCaps(part);
        mediator.subInputModeChanged();
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[nextCapsMode] capsMode = " + capsMode + ", " +
                    CAPS_MODES_LABELS[capsMode]);
        }
    }
    

    /**
     * Finish the word
     */
    private void finishWord() { 
        if (CAPS_MODES[capsMode] == CAPS_SENTENCE) {
            nextCapsMode();
        }
    }

    /**
     * Class StringDiff 
     *
     * Inner class to handle state changes and to output the differeces to 
     * listener InputMethodHandler.
     */
    private class StringDiff {
        /**
         * holds current entry state
         */
        private String state;

        /**
         * constructor
         */
        public StringDiff() {
            clear();
        }
        
        /**
         * clear current state
         */
        public void clear() {
            state = "";
        }

        /**
         * Update string
         * @param modified new string 
         */
        public void stateModified(String modified) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "[stateModified] state = " + state);
            }
            state = modified;
        }

        /**
         * Change of state processing.
         * Find the differences between state and nextState:
         * - If the diffrences are in new chars added to nextState, send
         * them to the listener.
         * - If chars submitted to the listener were retroactively changes,
         * resend all
         *   
         * @param nextState sets next state
         */
        public void nextState(String nextState) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "[nextState] nextState = " + nextState + "(length = " +
                        nextState.length() + ") state=" + state + "(length=" +
                        state.length() + ")");
            }

            if (mediator != null) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "           resending all");
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "clearing " + state + ": " + state.length() + " chars");
                }
                mediator.clear(state.length());
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "         commiting " + nextState);
                }
                mediator.commit(nextState);
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "[resendAll] <<<<");
                }
                state = nextState;
            }
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "[nextState] <<<<");
            }
        }
    }
    /** this mode is not set as default. So the map is initialized by false */
    private static final boolean[][] isMap =
        new boolean[TextInputSession.INPUT_SUBSETS.length + 1]
        [TextInputSession.MAX_CONSTRAINTS];
    
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
