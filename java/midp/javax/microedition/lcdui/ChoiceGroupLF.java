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

/**
 * Look and Feel interface used by ChoiceGroup.
 * <p>
 * See <a href="doc-files/naming.html">Naming Conventions</a>
 * for information about method naming conventions.
 */
interface ChoiceGroupLF extends ItemLF {
    
    /**
     * Notifies L&F that an element was inserted into the 
     * <code>ChoiceGroup</code> at the the elementNum specified.
     *
     * @param elementNum the index of the element where insertion occurred
     * @param stringPart the string part of the element to be inserted
     * @param imagePart the image part of the element to be inserted,
     * or <code>null</code> if there is no image part
     */
    void lInsert(int elementNum, String stringPart, Image imagePart);

    /**
     * Notifies L&F that an element referenced by <code>elementNum</code>
     * was deleted in the corresponding ChoiceGroup.
     *
     * @param elementNum the index of the deleted element
     */
    void lDelete(int elementNum);

    /**
     * Notifies L&F that all elements 
     * were deleted in the corresponding ChoiceGroup.
     */
    void lDeleteAll();

    /**
     * Notifies L&F that the <code>String</code> and 
     * <code>Image</code> parts of the
     * element referenced by <code>elementNum</code> were set in
     * the corresponding ChoiceGroup,
     * replacing the previous contents of the element.
     *
     * @param elementNum the index of the element set
     * @param stringPart the string part of the new element
     * @param imagePart the image part of the element, or <code>null</code>
     * if there is no image part
     */
    void lSet(int elementNum, String stringPart, Image imagePart);

    /**
     * Notifies L&F that an element was selected/deselected in the 
     * corresponding ChoiceGroup.
     *
     * @param elementNum the number of the element. Indexing of the
     * elements is zero-based
     * @param selected the new state of the element <code>true=selected</code>,
     * <code>false=not</code> selected
     */
    void lSetSelectedIndex(int elementNum, boolean selected);

    /**
     * Notifies L&F that selected state was changed on several elements 
     * in the corresponding MULTIPLE ChoiceGroup.
     * @param selectedArray an array in which the method collect the
     * selection status
     */
    void lSetSelectedFlags(boolean[] selectedArray);

    /**
     * Notifies L&F that a new text fit policy was set in the corresponding
     * ChoiceGroup.
     * @param fitPolicy preferred content fit policy for choice elements
     */
    void lSetFitPolicy(int fitPolicy);

    /**
     * Notifies L&F that a new font was set for an element with the 
     * specified elementNum in the corresponding ChoiceGroup.
     * @param elementNum the index of the element, starting from zero
     * @param font the preferred font to use to render the element
     */
    void lSetFont(int elementNum, Font font);

    /**
     * Get current selected index.
     * @return currently selected index
     */
    int lGetSelectedIndex();

    /**
     * Get selected flags.
     * @param selectedArray_return to contain the results
     * @return the number of selected elements
     */
    int lGetSelectedFlags(boolean[] selectedArray_return);


    /**
     * Determines if an element with a passed in index
     * is selected or not.
     * @param elementNum the index of an element in question
     * @return true if the element is selected, false - otherwise
     */
    boolean lIsSelected(int elementNum);

    /**
     * Gets default font to render ChoiceGroup element if it was not
     * set by the application
     * @return - the font to render ChoiceGroup element if it was not 
     *           set by the app
     */
    Font getDefaultFont();

}
