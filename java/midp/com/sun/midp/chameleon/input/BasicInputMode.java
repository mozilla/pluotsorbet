/*
 *   *
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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Displayable;
import com.sun.midp.i18n.*;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;


/**
 * An InputMode instance which processes the numeric 0-9 keys
 * as their literal numeric values.
 */
abstract class BasicInputMode implements InputMode, Runnable {
    /**
     * Text input constraints for this input mode. The semantics of the 
     * constraints value are defined in the TextField API.
     */
    protected int constraints;

    /**
     * Text input modifiers for this input mode. The semantics of the 
     * modifiers value are defined in the TextField API.
     */
    protected int modifiers;

    /**
     * A timeout, in ms, after which a pending key will be committed
     * to the text component. By default this is set to 250ms.
     */
    protected static final int KEY_COMMIT_TIMEOUT = 1000;
    
    /** A holder for the keyCode which was last processed */
    protected int lastKey = -1;
    
    /** The InputModeMediator for the current input session */
    protected InputModeMediator mediator;
    
    /** 
     * A boolean flag used by the timer in this input mode to
     * determine if a character should be committed to the text
     * component or not
     */
    protected boolean commitChar;
    
    /**
     * The number of times the user pressed the last key code. This value
     * acts as an index into the array of possible characters of any one
     * key. For example, a lastKey == to Canvas.KEY_NUM2 and a clickCount
     * of 3 would yield a 'c'.
     */
    protected int clickCount;
    
    /**
     * The single, pending character based on the key presses thus far
     */
    protected int pendingChar = KEYCODE_NONE;

    /**
     * Flag indicating if more matches exist
     */
    protected boolean hasMoreMatches;
    
    /**
     * A boolean flag used by the timer in this input mode to
     * know when to completely shut down the timer thread. That is,
     * when the input session is no longer active, the timer thread
     * in this input mode will quit entirely, freeing up system
     * resources.
     */
    protected boolean sessionIsLive;

  
    /** the possible key maps for this input mode */ 
    static char[][] keyMap;

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
     * @param inputSubset current input subset
     */
    public void beginInput(InputModeMediator mediator,
                           String inputSubset, int constraints) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[basic.beginInput] >>");
        }
        validateState(false);
        this.mediator = mediator;
        this.constraints = constraints & TextField.CONSTRAINT_MASK;
        this.modifiers = constraints & ~TextField.CONSTRAINT_MASK;
        startTimer();
        setInputSubset(inputSubset);
        setKeyMap(constraints, false);
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[basic.beginInput] <<");
        }
    }

    /**
     * Start timmer which will attempt to commit
     * a pending character after a certain timeout
     */
    private void startTimer() {
        (new Thread(this)).start();
    }
    
    /**
     * Stop timmer which will attempt to commit
     * a pending character after a certain timeout
     */
    private void stopTimer() {
        sessionIsLive = false;
        // Lastly, we'll interrupt the timer to end it.
        synchronized (this) {
            try {
                notify();
            } catch (IllegalMonitorStateException ignore) { }
        }
    }

    /**
     * Reset timmer which will attempt to commit
     * a pending character after a certain timeout
     */
    private void resetTimer() {
        sessionIsLive = true;
        // Lastly, we'll interrupt the timer to end it.
        synchronized (this) {
            try {
                notify();
            } catch (IllegalMonitorStateException ignore) { }
        }
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
     * Converts the string to key map. The rows are separated each from other
     * by '$'. The characters inside of one row follow each to other without
     * any separator.
     * @param line string combines all keys
     * @return map of the keys in char[][] format
     */
    protected char[][] getMapByLine(String line) {
        char[] chars = line.toCharArray();
        int rows = 1;
        for (int i = line.length() - 1; i >= 0; i--) {
            if (chars[i] == '$') rows++;
        }
        
        char[][] map = new char[rows][];
        for (int start = 0, j = 0; start < line.length(); j++) {           
            int end = line.indexOf('$', start);
            
            // if '$' is not found that means the end of string is reached
            if (end == -1) end = line.length();
            map[j] = line.substring(start, end).toCharArray();
            start = end + 1;
        }
        return map;
    }

    /**
     * Set the corresponding key map.
     *
     * @param constraints text input constraints. The semantics of the 
     * constraints value are defined in the TextField API.
     *
     * @param longPress return true if it's long key press otherwise false
     *
     * @return true if the key map has been changed otherwise false
     */
    protected abstract boolean setKeyMap(int constraints, boolean longPress); 

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
    public int processKey(int keyCode, boolean longPress) {
        int ret = KEYCODE_NONE;
        if (isValidKey(keyCode, longPress)) {
            
            // We immediately disable the commit of any pending character
            // input in case the timer expires
            commitChar = false;
            validateState(true);
            
            if (mediator != null && mediator.isClearKey(keyCode) || 
                keyCode == Canvas.LEFT || 
                keyCode == Canvas.RIGHT || 
                keyCode == Canvas.UP ||
                keyCode == Canvas.DOWN) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "[processKey] got clear or arrow. lastKey=" + lastKey);
                }
                completeInputMode(true);
            } else {

                if (setKeyMap(constraints, longPress)) {
                    pendingChar = KEYCODE_NONE;
                    clickCount = 0;
                }
                
                // at first check if previous key has to be committed
            
                // If we have a pending keycode and this new keycode is
                // different, we will commit the previous key and continue
                if (lastKey != -1 && lastKey != keyCode) {
                    commitPendingChar();
                }

                clickCount++;
            
                // If the pending key code has just one match or long key
                // press happens commit the current key 

                if (longPress) {
                    if (lastKey != -1) {
                        lastKey = keyCode;                  
                        commitPendingChar();
                    } 
                } else if (hasOneCase(keyCode)) {
                    lastKey = keyCode;                  
                    commitPendingChar();
                } else {
                    lastKey = keyCode;                  
                }
                
                // Lastly, we'll interrupt the timer to reset it or start it if
                // timer is still not working.
                resetTimer();        

                if (getNextChar() == KEYCODE_NONE) {
                    lastKey = -1;
                }
                
                ret = getPendingCharInternal();
            }            
        } else {
            ret = InputMode.KEYCODE_INVISIBLE;
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "[processKey] returning KEYCODE_INVISIBLE");
            }
        }
        return ret;
    }

    /**
     * Check if the key has to be handled this input mode
     * @param keyCode key
     * @param longPress true if long key press happens otherwise false.
     * @return true if this key can be  handled by this input mode,
     * otherwise false
     */
    private boolean isValidKey(int keyCode, boolean longPress) {
        if ((keyCode != Canvas.KEY_STAR && 
             keyCode != Canvas.KEY_POUND && 
             (mediator != null && !mediator.isClearKey(keyCode)) && 
             keyCode != Canvas.LEFT && 
             keyCode != Canvas.RIGHT && 
             keyCode != Canvas.UP && 
             keyCode != Canvas.DOWN && 
             (keyCode < Canvas.KEY_NUM0 || 
              keyCode > Canvas.KEY_NUM9)) || 
            (longPress && 
             lastKey != keyCode && 
             lastKey != -1)) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "INVALID KEY");
            }
            return false;
        }
        return true;
    }

    
    /**
     * Set the next capital mode for this input method
     */
    protected void nextCapsMode() {}

    /**
     * Get next possble char
     * @return next key code 
     */ 
    protected int getNextChar() {
        pendingChar = KEYCODE_NONE;
        return getPendingCharInternal();
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
        String value = null;

        int ch = getNextChar();
        if (ch != KEYCODE_NONE) {
            value = String.valueOf((char)ch);
        }
        hasMoreMatches = false;

        return value;
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
        return hasMoreMatches;
    }

    /**
     * Gets the possible string matches 
     *
     * @return returns the set of options.
     */
    public String[] getMatchList() {
        //   String[] value = null;
        //   int ch = getPendingCharInternal();
        
        //   if (ch != KEYCODE_NONE) {
        //      value = new String[1];
        //      value[0] = String.valueOf((char)ch);
        //   } else {
        //      value = new String[0];
        //   }
        //   return value;
        //         
        return new String[0];
    }

            
    /**
     * Mark the end of this InputMode's processing. The only possible call
     * to this InputMode after a call to endInput() is a call to beginInput()
     * to begin a new input session.
     */
    public void endInput() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[basic.endInput]");
        }
        validateState(true);
        this.mediator = null;
        clickCount = 0;
        lastKey = -1;

        stopTimer();
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
     * Implementation of a timer routine which will attempt to commit
     * a pending character after a certain timeout (depending on the
     * state of the commitChar boolean).
     */
    public void run() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[run] sessionIsLive=" + sessionIsLive + " commitChar=" + commitChar);
        }
        // We initially block until the first key press is processed
        if (!sessionIsLive) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (Throwable t) {
            } // ignore interruptions
        }

        while (sessionIsLive) {
            try {
                synchronized (this) {
                    // Just before we start the timeout we set the
                    // commit flag to true. If it doesn't get reset
                    // to false by the processKey method, we will
                    // commit the pending key when we wake up
                    commitChar = true;
                    wait(KEY_COMMIT_TIMEOUT);
                }
            } catch (Throwable t) {
            } // ignore any exceptions here

            if (sessionIsLive && commitChar) {
                completeInputMode(true);
            }
        }
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
        if (activeOperation && this.mediator == null) {
            throw new IllegalStateException(
            "Illegal operation on an input session already in progress");
        } else if (!activeOperation && this.mediator != null) {
            throw new IllegalStateException(
            "Illegal operation on an input session which is not in progress");
        }
    }
    
    /**
     * return the pending char for internal use 
     *
     * @return return the pending char
     */
    public int getPendingCharInternal() {
        if (pendingChar == KEYCODE_NONE) {
            char[] chars = null;
            char c;
            // log("[basic.getPendingCharInternal] lastKey=" + lastKey);
            if (lastKey == -1 || clickCount <= 0) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "[getPendingCharInternal] returning KEYCODE_NONE");
                }
            } else {
                chars = getCharOptions(lastKey);
                if (chars == null) {
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                            "[getPendingCharInternal] returning KEYCODE_NONE");
                    }
                } else {
                    if (clickCount > chars.length) {
                        clickCount = 1;
                    }

                    if (chars.length > 0) {
                        pendingChar = chars[clickCount - 1];
                    }

                    hasMoreMatches = true;
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                            "[getPendingCharInternal] returning " + pendingChar);
                    }
                }
            }
        }
        return pendingChar;
    }

    /**
     * Check if only one char option exists for the key code
     * @param keyCode key code
     * @return true if only one char option exists otherwise false. 
     */
    private boolean hasOneCase(int keyCode) {
        boolean ret = false;
        if (keyCode != -1) {
            char[] options = getCharOptions(keyCode);
            ret = (options == null) || options.length <= 1;
        }
        return ret;
    }
        
    /**
     * return the pending char
     * used to bypass the asynchronous commit mechanism
     * e.g. to immediately commit a char before moving the cursor
     *
     * @return return the pending char
     */
    public char getPendingChar() {
        int code = getPendingCharInternal();
        char c = code == KEYCODE_NONE ? 0 : (char) code;
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[getPendingChar] returning " + c);
        }
        return c;
    }

    /**
     * Gets the possible matches for the key code
     *
     * @param lastKey the key code
     *
     * @return returns the set of options. Return null if matches are not found.
     */
    protected abstract char[] getCharOptions(int lastKey);
      
    
    /**
     * This method is used to immediately commit the pending
     * character because a new character is now pending.
     *
     * @return true if char has been committed otherwise false
     */
    protected boolean commitPendingChar() {
        boolean committed = false;
        int c = getPendingCharInternal();
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[commitPendingChar] getPendingChar=" + c);
        }
        if (c != KEYCODE_NONE) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "[commitPendingChar] commiting " + String.valueOf((char) c));
            }
            committed = true;
            mediator.commit(String.valueOf((char) c));
        }

        lastKey = -1;
        clickCount = 0;
        pendingChar = KEYCODE_NONE;
        return committed;
    }
    
    /**
     * This method is used to immediately commit the given
     * string and then call the TextInputMediator's inputModeCompleted()
     * method
     * @param commit true if the char is accepted, false if the char is rejected
     */
    protected void completeInputMode(boolean commit) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[Basic.completeInputMode] commit = " + commit);
        }
        if (commit) {
            commitPendingChar();
        }

        clickCount = 0;
        lastKey = -1;

        stopTimer();
        startTimer();
    }

    /** 
     * Notify about current input subset
     * @param inputSubset current input subset
     */
    protected void setInputSubset(String inputSubset) {
    }

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
    abstract public boolean supportsConstraints(int constraints);
    /**
     * Returns the display name which will represent this InputMode to 
     * the user, such as in a selection list or the softbutton bar.
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    abstract public String getName();

    /**
     * Returns the command name which will represent this InputMode in
     * the input menu
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    abstract public String getCommandName();
    
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
    abstract public boolean[][] getIsConstraintsMap();
}

