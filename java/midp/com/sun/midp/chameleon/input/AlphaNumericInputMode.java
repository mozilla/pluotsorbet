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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.TextField;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;


/**
 * An InputMode instance which processes the numeric 0-9 keys
 * as their literal numeric values.
 */
public class AlphaNumericInputMode extends BasicInputMode {

    /** set of chars for alpha upper-case input */
    private static char[][] upperKeyMap;

    /** set of chars for alpha low-case input */
    private static char[][] lowerKeyMap;

    /** set of chars for long key press */
    private static char[][] longPressKeyMap;

    /** array of sub-inputModes supported by this inputMode */
    protected static final int[] CAPS_MODES = {
        CAPS_SENTENCE, 
        CAPS_OFF,
        CAPS_ON
    }; 

    /** array of sub-inputModes labels, corresponding to CAPS_MODES array */
    private static final String[] CAPS_MODES_LABELS = {
        Resource.getString(ResourceConstants.LCDUI_TF_CAPS_SENTENCE),
        Resource.getString(ResourceConstants.LCDUI_TF_CAPS_OFF), 
        Resource.getString(ResourceConstants.LCDUI_TF_CAPS_ON) 
    };

    /** the possible key maps for this input mode */ 
    private static char[][][] keyMaps;            

    /** points to an element of CAPS_MODES which is the current sub-inputMode */
    protected int capsModePointer = 0;


    /** Default constructor. Init key maps for all constraints */
    public AlphaNumericInputMode() {
        String upperInLine = Resource.getString(
                             ResourceConstants.LCDUI_TF_CAPS_ALPHA_KEY_MAP);
        upperKeyMap = getMapByLine(upperInLine);
        String lowerInLine = Resource.getString(
                             ResourceConstants.LCDUI_TF_ALPHA_KEY_MAP);
        lowerKeyMap = getMapByLine(lowerInLine);
        String longInLine = Resource.getString(
                             ResourceConstants.LCDUI_TF_ALPHA_DIGIT_KEY_MAP);
        longPressKeyMap = getMapByLine(longInLine);

        keyMaps = new char[3][][];
        keyMaps[0] = upperKeyMap;
        keyMaps[1] = lowerKeyMap;
        keyMaps[2] = upperKeyMap;
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
    protected boolean setKeyMap(int constraints, boolean longPress) {
        char[][] oldKeyMap = keyMap;
            
        keyMap = longPress ?
            longPressKeyMap: 
        keyMaps[capsModePointer];
        
        return oldKeyMap != keyMap;
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
    public boolean supportsConstraints(int constraints) {
        switch (constraints & TextField.CONSTRAINT_MASK) {
            case TextField.NUMERIC:
            case TextField.DECIMAL:
            case TextField.PHONENUMBER:
                return false;
            default:
                return true;
        }
    }
    
    /**
     * Returns the display name which will represent this InputMode to 
     * the user, such as in a selection list or the softbutton bar.
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    public String getName() {
        return CAPS_MODES_LABELS[capsModePointer];
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
     * Set the next capital mode for this input method
     */
    protected void nextCapsMode() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[A.nextCapsMode]");
        }
        capsModePointer++;
        if (capsModePointer == CAPS_MODES.length) {
            capsModePointer = 0;
        }
        if (CAPS_MODES[capsModePointer] == CAPS_OFF) {
            keyMap = lowerKeyMap;
        } else {
            keyMap = upperKeyMap;
        }

        mediator.subInputModeChanged();
    }
   

    /**
     * Gets the possible matches for the key code
     *
     * @param lastKey the key code
     *
     * @return returns the set of options. Return null if matches are not found.
     */
    protected char[] getCharOptions(int lastKey) {
        char[] chars = null;
    
        switch (lastKey) {
        case Canvas.KEY_NUM0:
            chars = keyMap[0];
            break;          
        case Canvas.KEY_NUM1:
            chars = keyMap[1];
            break;
        case Canvas.KEY_NUM2:
            chars = keyMap[2];
            break;
        case Canvas.KEY_NUM3:
            chars = keyMap[3];
            break;
        case Canvas.KEY_NUM4:
            chars = keyMap[4];
            break;
        case Canvas.KEY_NUM5:
            chars = keyMap[5];
            break;
        case Canvas.KEY_NUM6:
            chars = keyMap[6];
            break;
        case Canvas.KEY_NUM7:
            chars = keyMap[7];
            break;
        case Canvas.KEY_NUM8:
            chars = keyMap[8];
            break;
        case Canvas.KEY_NUM9:
            chars = keyMap[9];
            break;
        case Canvas.KEY_POUND:
            chars = keyMap[10];
            break;
        case Canvas.KEY_STAR:
            nextCapsMode();
            break;
                
        default:
            // This can actually happen if the Timer went off without
            // a pending key, which can sometimes happen.
            break;

        }
       
        return chars;
    }

    /**
     * This method is used to immediately commit the pending
     * character because a new character is now pending.
     *
     * @return true if char has been committed otherwise false
     */
    protected boolean commitPendingChar() {
        boolean committed = super.commitPendingChar();
        if (committed) {
            if (CAPS_MODES[capsModePointer] == CAPS_SENTENCE) {
                nextCapsMode();
            }
        }
        return committed;
    }
    
    /** input subset x constraint map */
    private static final boolean[][] isMap = {
        // |ANY|EMAILADDR|NUMERIC|PHONENUMBER|URL|DECIMAL
        { false, false, false, false, false, false }, // IS_FULLWIDTH_DIGITS
        { true,  true,  false, false, true,  false }, // IS_FULLWIDTH_LATIN
        { false, false, false, false, false, false }, // IS_HALFWIDTH_KATAKANA
        { false, false, false, false, false, false }, // IS_HANJA 
        { false, false, false, false, false, false }, // IS_KANJI
        { true,  true,  false, false, true,  false }, // IS_LATIN
        { true,  true,  false, false, true,  false }, // IS_LATIN_DIGITS
        { false, false, false, false, false, false }, // IS_SIMPLIFIED_HANZI
        { false, false, false, false, false, false }, // IS_TRADITIONAL_HANZI
        { true,  true,  false, false, true,  false }, // MIDP_UPPERCASE_LATIN
        { true,  true,  false, false, true,  false }, // MIDP_LOWERCASE_LATIN
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

    /** 
     * Notify about current input subset
     * @param inputSubset current input subset
     */
    protected void setInputSubset(String inputSubset) {
        int mode = -1;
        if ("MIDP_UPPERCASE_LATIN".equals(inputSubset)) {
            mode = CAPS_ON;
        } else if ("MIDP_LOWERCASE_LATIN".equals(inputSubset)) {
            mode = CAPS_OFF;
        }
        for (int i = CAPS_MODES.length - 1; i >= 0; i--) {
            if (CAPS_MODES[i] == mode) {
                capsModePointer = i;
                break;
            }
        }
    }
}
