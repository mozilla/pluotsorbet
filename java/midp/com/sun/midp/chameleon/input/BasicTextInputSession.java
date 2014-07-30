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

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;

/**
 * The BasicTextInputSession represents the relationship between the 
 * system's key input, the TextInputComponent, the available InputModes, 
 * and the graphical display. 
 */
public class BasicTextInputSession implements 
    TextInputSession, InputModeMediator 
{
    
    /** The currently "active" InputMode */
    protected InputMode currentMode;
    
    /** The set of all possible InputModes */
    protected InputMode[] inputModeSet;
    
    /** The current Display object */
    private Display currentDisplay;

    /** The previous Displayable */
    private Displayable previousScreen;

    /** 
     * If the user has specifically chosen an InputMode, that choice
     * becomes sticky when the InputSession chooses the InputMode to
     * make active.
     */
    protected InputMode stickyMode;
    
    /** The text component receiving the input */
    protected TextInputComponent textComponent;

    
    /**
     * Construct a new BasicTextInputSession
     */
    public BasicTextInputSession() { 
        inputModeSet = InputModeFactory.createInputModes();
    }
    
    /**
     * Start a text input session for the given TextInputComponent.
     * The TextInputComponent can be used to determine the initial
     * input mode, constraints, etc.
     *
     * @param component the TextInputComponent which is receiving text input
     */
    public void beginSession(TextInputComponent component) {
        if (component == null) {
            throw new IllegalArgumentException(
                "Null TextInputComponent in beginSession()");
        }
        
        if (this.textComponent == null) {
            this.textComponent = component;
        } else if (this.textComponent != component) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                    "[Basic.beginSession()] " +
                        "InputModeHandler in use by another TextInputComponent");
            }
            throw new IllegalStateException(
                "InputModeHandler in use by another TextInputComponent");
        }
        
        // Select a suitable InputMode
        selectInputMode();
    }
        
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
    public InputMode[] getAvailableModes() {
        if (textComponent == null) {
            throw new IllegalStateException(
                "Call to InputModeHandler while outside of a valid session");
        }
        
        int constraints = textComponent.getConstraints();
        Vector v = new Vector();
        for (int i = 0; i < inputModeSet.length; i++) {
            if (inputModeSet[i].supportsConstraints(constraints)) {
                v.addElement(inputModeSet[i]);
            }
        }
        
        if (v.size() == 0) {
            return null;
        }
        
        InputMode[] modes = new InputMode[v.size()];
        v.copyInto(modes);
        
        return modes;
    }
    
    /**
     * Retrieve the InputMode which is the current "active" mode
     * for this TextInputSession. This does not necessarily mean there is 
     * any pending input with the InputMode itself, it means that if this 
     * TextInputSession receives key input, the returned InputMode will be
     * the mode which processes that input.
     *
     * @return the currently "active" InputMode
     */
    public InputMode getCurrentInputMode() {
        return currentMode;
    }
    
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
    public void setCurrentInputMode(InputMode mode) {
        if (mode == null || mode == currentMode) {
            return;
        }
        
        for (int i = 0; i < inputModeSet.length; i++) {
            if (inputModeSet[i] == mode) {
                try {
                    endInputMode(currentMode);
                    setInputMode(inputModeSet[i]);
                } catch (Throwable t) {
                    // IMPL_NOTE Log exception?
                }
                break;
            }
        }
    }
        
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
    public int processKey(int keyCode, boolean longPress) {
        try {
            return currentMode.processKey(keyCode, longPress);
        } catch (Throwable t) {
            // Since InputModes are pluggable, we'll catch any possible
            // Throwable when calling into one
            // IMPL_NOTE : log the throwable
        }
        return InputMode.KEYCODE_NONE;
    }

    /**
     * return the pending char
     * used to bypass the asynchronous commit mechanism
     * e.g. to immediately commit a char before moving the cursor
     *
     * @return return the pending char
     */
    public char getPendingChar() { 
        return currentMode != null ? currentMode.getPendingChar() : 0;
    }

        
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
    public String getNextMatch() {
        try {               
            return currentMode.getNextMatch();
        } catch (Throwable t) {
            // Since InputModes are pluggable, we'll catch any possible
            // Throwable when calling into one
            // IMPL_NOTE : log the throwable
        }
        return null;
    }

    
    /**
     * If the InputMode supports multiple matches and more matches are
     * available this method will return true, false otherwise.
     * 
     * @return true if the current InputMode supports multiple matches and
     *         there are currently more matches available
     */
    public boolean hasMoreMatches() {
        try {
            return currentMode.hasMoreMatches();
        } catch (Throwable t) {
            // Since InputModes are pluggable, we'll catch any possible
            // Throwable when calling into one
            // IMPL_NOTE : log the throwable
        }
        return false;
    }


    /**
     * Gets the possible string matches 
     *
     * @return returns the set of options.
     */
    public String[] getMatchList() {
        return currentMode != null ? currentMode.getMatchList() : new String[0];
    }


    /**
     * End the current text input session and do not commit any pending
     * characters to the buffer.
     */
    public void endSession() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[BTIS.endSession]");
        }

        if (currentMode != null) {
            endInputMode(currentMode);
            setInputMode(null);
        }
        textComponent = null;
        stickyMode = null;
    }
    
    // ******* Begin InputModeMediator Interface *******    
    /**
     * Called by an InputMode in order to automatically commit the given 
     * input to the Text component. For example, when the timer expires
     * in an AlphaNumeric InputMode it will commit the current pending
     * character.
     * @param input text to commit
     */
    public void commit(String input) {
        if (input != null && textComponent != null) {
            textComponent.commit(input);
        }
    }

    /**
     * Clear the particular number of symbols 
     *
     * @param num number of symbols
     */
    public void clear(int num) {
        if (num == 0) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "WARNING: BasicTextInput.clear calld with 0");
            }
            return;
        }
        textComponent.clear(num);
    }

    /**
     * Called by an InputMode to inform a TextComponent of a sub-inputMode
     * change. 
     */
    public void subInputModeChanged() {
        textComponent.notifyModeChanged();
    }
    
    /**
     * Called by an InputMode in order to signal that the input process
     * has been completed with respect to the InputMode. Subsequent key 
     * input should be handled in a new input session, possibly by the
     * same InputMode or by a different InputMode alltogether. For example,
     * when the timer expires in an AlphaNumeric InputMode, the character
     * is committed and the AlphaNumeric InputMode signals its completion.
     * Further key input may start a new session with the AlphaNumeric
     * InputMode or possibly some other InputMode.
     */
    public void inputModeCompleted() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[Basic.inputModeCompleted()] >>> ");
        }
        try {
            if (currentMode != null) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "[Basic.inputModeCompleted()] !=null");
                }
                endInputMode(currentMode);
                setInputMode(null);
            }
            // Select a suitable InputMode
            selectInputMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[Basic.inputModeCompleted()] <<<< ");
        }
    }

    // ******* End InputModeMediator Interface *******
    
    /**
     * Based on the constraints of the current TextInputComponent,
     * select the most appropriate InputMode from the list available.
     * This method will also start the session with the InputMode by
     * calling the InputMode's beginInput() method.
     */
    protected void selectInputMode() {
        if (textComponent == null) {
            throw new IllegalStateException(
                "Attempted input on null TextInputComponent");
        }

        int constraints = textComponent.getConstraints();
        
	InputMode newMode = null;
       
        if (stickyMode != null && stickyMode.supportsConstraints(constraints)) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "[BTIS.selectInputMode] setting mode to sticky:" +
                        stickyMode.getName());
            }
            newMode = stickyMode;
        } else {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "[BTIS.selectInputMode] not setting mode to sticky");
            }
            for (int i = 0; i < inputModeSet.length; i++) {
                if (inputModeSet[i].supportsConstraints(constraints)) {
                    boolean[][] map = inputModeSet[i].getIsConstraintsMap();
                    int index = 0;
                    String is = textComponent.getInitialInputMode();
                    for (; index < INPUT_SUBSETS.length; index++) {
                        if (INPUT_SUBSETS[index].equals(is)) {
                            break;
                        }
                    }
                    int constraint = constraints & TextField.CONSTRAINT_MASK;
                    if (constraint < TextInputSession.MAX_CONSTRAINTS &&
                        map[index][constraint]) {
                        newMode = inputModeSet[i];
                        break;
                    }
                }
            }
        }

        if (newMode != null) {
            if (newMode != currentMode) {
                endInputMode(currentMode);
                setInputMode(newMode);
            }
        } else {
            throw new IllegalStateException(
             "No InputMode found supporting the current constraints");
        }
    }

    /**
     * Set the required input mode. Sticky mode can be set as the old mode just
     * in case it will have to be reverted back. Text component has to be
     * notified about the mode change. 
     * 
     * @param mode the required input mode 
     */
    private void setInputMode(InputMode mode) {
        InputMode oldMode = currentMode;
        currentMode = mode;

        if (currentMode != null && textComponent != null) {
            currentMode.beginInput(this,
                                   textComponent.getInitialInputMode(),
                                   textComponent.getConstraints());
            if (currentMode.hasDisplayable()) {
                currentDisplay = textComponent.getDisplay();
                previousScreen = currentDisplay.getCurrent();
                currentDisplay.setCurrent(currentMode.getDisplayable());
                stickyMode = oldMode;
            } else {
                stickyMode = currentMode;
            }
            textComponent.notifyModeChanged();
        }
    }
    
    /**
     * End the expired input mode. 
     * 
     * @param mode expired input mode 
     */
    private void endInputMode(InputMode mode) {
        if (mode != null) {
            mode.endInput();
            if (mode.hasDisplayable() && textComponent != null) {
                currentDisplay.setCurrent(previousScreen);
                previousScreen = null;
                currentDisplay = null;
            }
        }
    }

    /**
     * Check if the given char is symbol
     * @param c char 
     * @return true if the char is symbol otherwise false. 
     */
    public boolean isSymbol(char c) {
        return SymbolInputMode.isSymbol(c);
    }

    /**
     * Returns true if the keyCode is used as 'clear'
     * @param keyCode key code
     * @return true if key code is Clear one, false otherwise
     */
    public boolean isClearKey(int keyCode) {
        return textComponent != null &&
            textComponent.isClearKey(keyCode);
    }

    /**
     * Returns the available size (number of characters) that can be
     * stored in this <code>TextInputComponent</code>.
     * @return available size in characters
     */
    public int getAvailableSize() {
        return textComponent != null ? textComponent.getAvailableSize() : 0;
    }

    /**
     * Returns true if the keyCode is used as 'enter' (user types in \n)
     * ('select' plays the role of 'enter' in some input modes).
     *
     * @param keyCode key code
     * @return true if key code is the one for newline, false otherwise
     */
    public boolean isNewlineKey(int keyCode) {
        return textComponent != null &&
            textComponent.isNewlineKey(keyCode);
    }
}

