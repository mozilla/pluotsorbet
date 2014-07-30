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
 * An interface which defines the protocol between
 * TextInputComponent and InputMoode
 */
public interface InputModeMediator {
    
    /**
     * Called by an InputMode in order to automatically commit the given 
     * input to the Text component. For example, when the timer expires
     * in an AlphaNumeric InputMode it will commit the current pending
     * character.
     * @param input String needs to be commited
     */
    public void commit(String input);
    
    /**
     * Clear the particular number of symbols 
     *
     * @param num number of symbols
     */
    public void clear(int num);

    /** 
     * Called by an InputMode to inform a TextComponent of a sub-inputMode
     * change. 
     */
    public void subInputModeChanged();

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
    public void inputModeCompleted();

    /**
     * Returns true if the keyCode is used as 'clear'
     * @param keyCode key code
     * @return true if key code is Clear one, false otherwise
     */
    public boolean isClearKey(int keyCode);

    /**
     * Returns the available size (number of characters) that can be
     * stored in this <code>TextInputComponent</code>.
     * @return available size in characters
     */
    public int getAvailableSize();

    /**
     * Returns true if the keyCode is used as 'enter' (user types in \n)
     * ('select' plays the role of 'enter' in some input modes).
     * @param keyCode key code
     * @return true if key code is the one for newline, false otherwise
     */
    public boolean isNewlineKey(int keyCode);
}
