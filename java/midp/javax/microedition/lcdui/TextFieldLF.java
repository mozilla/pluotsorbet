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
package javax.microedition.lcdui;

import com.sun.midp.lcdui.DynamicCharacterArray;

/**
 * Look and Feel interface used by TextField.
 * <p>
 * See <a href="doc-files/naming.html">Naming Conventions</a>
 * for information about method naming conventions.
 */
interface TextFieldLF extends ItemLF {
    
    /**
     * Update the character buffer in TextField with latest user input.
     * @return true if there is new user input updated in the buffer
     */
    boolean lUpdateContents();

    /**
     * Notifies L&F of a content change in the corresponding TextField.
     */
    void lSetChars();

    /**
     * Notifies L&F of a character insertion in the corresponding TextField.
     * @param data the source of the character data
     * @param offset the beginning of the region of characters copied
     * @param length the number of characters copied
     * @param position the position at which insertion occurred
     */
    void lInsert(char data[], int offset, int length, int position);

    /**
     * Notifies L&F of character deletion in the corresponding TextField.
     * @param offset the beginning of the deleted region
     * @param length the number of characters deleted
     */
    void lDelete(int offset, int length);

    /**
     * Notifies L&F of a maximum size change in the corresponding TextField.
     * @param maxSize - the new maximum size
     */
    void lSetMaxSize(int maxSize);

    /**
     * Gets the current input position.
     * @return the current caret position, <code>0</code> if at the beginning
     */
    int lGetCaretPosition();

    /**
     * Notifies L&F that constraints have to be changed.
     */
    void lSetConstraints();

    /**
     * Validate a given character array against a constraints.
     *
     * @param buffer a character array
     * @param constraints text input constraints
     * @return true if constraints is met by the character array
     */
    public boolean lValidate(DynamicCharacterArray buffer, int constraints);

    /**
     * Notifies L&F that preferred initial input mode was changed.
     * @param characterSubset a string naming a Unicode character subset,
     * or <code>null</code>
     */
    void lSetInitialInputMode(String characterSubset);
    
    /**
     * Notifies item that it has been recently deleted
     */
    void itemDeleted();
}
