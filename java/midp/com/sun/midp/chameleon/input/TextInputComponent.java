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

import javax.microedition.lcdui.Display;

/**
 * An interface which defines the protocol between LCDUI component 
 * implementations (TextFieldLFImpl, TextBoxLFImpl) and the TextInputMediator
 * to enable text input from the system's input modes.
 */
public interface TextInputComponent {

    /**
     * Retrieve the initial input mode of this text component as
     * defined by the MIDP TextField API.
     *
     * @return the initial input mode for this text component or 'null'
     *         if none was set
     */
    public String getInitialInputMode();
    
    /**
     * Retrieve the constraints of this text component as defined
     * by the MIDP TextField API.
     *
     * @return a bitmask which defines the constraints set on this
     *         text component, or 0 if none were set
     */
    public int getConstraints(); 
   
    /**
     * Returns the available size (number of characters) that can be
     * stored in this <code>TextInputComponent</code>.
     * @return available size in characters
     */
    public int getAvailableSize(); 
   

    /**
     * This is a direct call from the text input system to immediately
     * commit the given input to this TextInputComponent's state. 
     * This call constitutes a change to the value of this TextInputComponent
     * and should result in any of its change listeners being notified.
     *
     * @param input String needs to be commited
     */
    public void commit(String input);
    
    /**
     * This is a notification from the input session that the selected
     * input mode has changed. If the TextInputComponent is interested,
     * it can query the session for the new InputMode.
     */
    public void notifyModeChanged();

    /**
     * Clear the particular number of symbols 
     *
     * @param num number of symbols
     */
    public void clear(int num);

    /**
     * Gets the current Display 
     * @return current Display
     */
    public Display getDisplay();

    /**
     * Returns true if the keyCode is used as 'clear'
     * @param keyCode key code
     * @return true if key code is Clear one, false otherwise
     */
    public boolean isClearKey(int keyCode);

    /**
     * Returns true if the keyCode is used as 'enter' (user types in \n)
     * ('select' plays the role of 'enter' in some input modes).
     *
     * @param keyCode key code
     * @return true if key code is the one for newline, false otherwise
     */
    boolean isNewlineKey(int keyCode);
}
