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
public class NumericInputMode extends BasicInputMode {

    /**
     * The numeric key map.
     */
    protected char[][] numericKeyMap;

    /**
     * The decimal key map.
     */
    protected char[][] decimalKeyMap;
    
    /**
     * The phone numeric key map.
     */
    protected char[][] phoneNumericKeyMap;

    /**
     * The any numeric key map.
     */
    protected char[][] anyKeyMap;

    /** Default constructor. Init key maps for all constraints */
    public NumericInputMode() {
        String numericInLine = Resource.getString(
                             ResourceConstants.LCDUI_TF_NUMERIC_KEY_MAP);
        numericKeyMap = getMapByLine(numericInLine);
        String decimalInLine = Resource.getString(
                             ResourceConstants.LCDUI_TF_DECIMAL_KEY_MAP);
        decimalKeyMap = getMapByLine(decimalInLine);
        String phoneInLine = Resource.getString(
                             ResourceConstants.LCDUI_TF_PHONE_KEY_MAP);
        phoneNumericKeyMap = getMapByLine(phoneInLine);
        String anyInLine = Resource.getString(
                             ResourceConstants.LCDUI_TF_NUMERIC_ANY_KEY_MAP);
        anyKeyMap = getMapByLine(anyInLine);
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
	// Numbers are allowed by any input constraints
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
        return Resource.getString(ResourceConstants.LCDUI_TF_NUMERIC);
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
        if (constraints == TextField.PHONENUMBER) {
            keyMap = phoneNumericKeyMap;
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "setting keymap to phone");
            }
        } else if (constraints == TextField.DECIMAL) {
            keyMap = decimalKeyMap;
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "setting keymap to decimalKeyMap");
            }
        } else if (constraints == TextField.NUMERIC) {
            keyMap = numericKeyMap;
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "setting keymap to numeric");
            }
        } else {
            keyMap = anyKeyMap;
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                    "setting keymap to any");
            }
        }
        return oldKeyMap != keyMap;
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
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                " getCharOptions lastKey=" + lastKey);
        }

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
            case Canvas.KEY_STAR:
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        " getCharOptions got star");
                }
                chars = keyMap[10];
                break;
            case Canvas.KEY_POUND:
                chars = keyMap[11];
                break;

            default:
                // This can actually happen if the Timer went off without
                // a pending key, which can sometimes happen.
                break;
        }
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "getCharOptions returning:");
        }
	if (chars != null) {
	    for (int i = 0; i < chars.length; i++) {
		if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
				   chars[i] + ",");
		}
	    }
	}
        return chars;
    }

    /** input subset x constraint map */
    private static final boolean[][] isMap = {
        // |ANY|EMAILADDR|NUMERIC|PHONENUMBER|URL|DECIMAL
        { true,  true,  true, true, true,  true }, // IS_FULLWIDTH_DIGITS
        { false, false, true, true, false, true }, // IS_FULLWIDTH_LATIN
        { false, false, true, true, false, true }, // IS_HALFWIDTH_KATAKANA 
        { false, false, true, true, false, true }, // IS_HANJA              
        { false, false, true, true, false, true }, // IS_KANJI              
        { false, false, true, true, false, true }, // IS_LATIN              
        { false, false, true, true, false, true }, // IS_LATIN_DIGITS       
        { false, false, true, true, false, true }, // IS_SIMPLIFIED_HANZI 
        { false, false, true, true, false, true }, // IS_TRADITIONAL_HANZI
        { false, false, true, true, false, true }, // MIDP_UPPERCASE_LATIN
        { false, false, true, true, false, true }, // MIDP_LOWERCASE_LATIN
        { false, false, true, true, false, true }  // NULL
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
