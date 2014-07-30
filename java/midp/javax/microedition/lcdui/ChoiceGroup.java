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
 * A <code>ChoiceGroup</code> is a group of selectable elements intended to be
 * placed within a
 * {@link Form}. The group may be created with a mode that requires a
 * single choice to be made or that allows multiple choices. The
 * implementation is responsible for providing the graphical representation of
 * these modes and must provide visually different graphics for different
 * modes. For example, it might use &quot;radio buttons&quot; for the
 * single choice
 * mode and &quot;check boxes&quot; for the multiple choice mode.
 *
 * <p> <strong>Note:</strong> most of the essential methods have been
 * specified in the {@link Choice Choice} interface.</p>
 * @since MIDP 1.0
 */

public class ChoiceGroup extends Item implements Choice {

    /**
     * Creates a new, empty <code>ChoiceGroup</code>, specifying its
     * title and its type.
     * The type must be one of <code>EXCLUSIVE</code>,
     * <code>MULTIPLE</code>, or <code>POPUP</code>. The
     * <code>IMPLICIT</code>
     * choice type is not allowed within a <code>ChoiceGroup</code>.
     *
     * @param label the item's label (see {@link Item Item})
     * @param choiceType <code>EXCLUSIVE</code>, <code>MULTIPLE</code>,
     * or <code>POPUP</code>
     * @throws IllegalArgumentException if <code>choiceType</code> 
     *      is not one of
     * <code>EXCLUSIVE</code>, <code>MULTIPLE</code>, or <code>POPUP</code>
     * @see Choice#EXCLUSIVE
     * @see Choice#MULTIPLE
     * @see Choice#IMPLICIT
     * @see Choice#POPUP
     */
    public ChoiceGroup(String label, int choiceType) {
        this(label, choiceType, new String[] {}, null);
    }

    /**
     * Creates a new <code>ChoiceGroup</code>, specifying its title,
     * the type of the
     * <code>ChoiceGroup</code>, and an array of <code>Strings</code>
     * and <code>Images</code> to be used as its
     * initial contents.
     *
     * <p>The type must be one of <code>EXCLUSIVE</code>,
     * <code>MULTIPLE</code>, or <code>POPUP</code>.  The
     * <code>IMPLICIT</code>
     * type is not allowed for <code>ChoiceGroup</code>.</p>
     *
     * <p>The <code>stringElements</code> array must be non-null and
     * every array element
     * must also be non-null.  The length of the
     * <code>stringElements</code> array
     * determines the number of elements in the <code>ChoiceGroup</code>.  The
     * <code>imageElements</code> array
     * may be <code>null</code> to indicate that the
     * <code>ChoiceGroup</code> elements have no images.
     * If the
     * <code>imageElements</code> array is non-null, it must be the
     * same length as the
     * <code>stringElements</code> array.  Individual elements of the
     * <code>imageElements</code> array
     * may be <code>null</code> in order to indicate the absence of an
     * image for the
     * corresponding <code>ChoiceGroup</code> element.  Non-null elements
     * of the
     * <code>imageElements</code> array may refer to mutable or
     * immutable images.</p>
     *
     * @param label the item's label (see {@link Item Item})
     * @param choiceType <code>EXCLUSIVE</code>, <code>MULTIPLE</code>,
     * or <code>POPUP</code>
     * @param stringElements set of strings specifying the string parts of the
     * <code>ChoiceGroup</code> elements
     * @param imageElements set of images specifying the image parts of
     * the <code>ChoiceGroup</code> elements
     *
     * @throws NullPointerException if <code>stringElements</code>
     * is <code>null</code>
     * @throws NullPointerException if the <code>stringElements</code>
     * array contains
     * any <code>null</code> elements
     * @throws IllegalArgumentException if the <code>imageElements</code>
     * array is non-null
     * and has a different length from the <code>stringElements</code> array
     * @throws IllegalArgumentException if <code>choiceType</code> 
     *      is not one of
     * <code>EXCLUSIVE</code>, <code>MULTIPLE</code>, or <code>POPUP</code>
     *
     * @see Choice#EXCLUSIVE
     * @see Choice#MULTIPLE
     * @see Choice#IMPLICIT
     * @see Choice#POPUP
     */
    public ChoiceGroup(String label, int choiceType,
                       String[] stringElements, Image[] imageElements) {

        this(label, choiceType, stringElements, imageElements, false);
    }

    /**
     * Special constructor used by List
     *
     * @param label the item's label (see {@link Item Item})
     * @param choiceType EXCLUSIVE or MULTIPLE
     * @param stringElements set of strings specifying the string parts of the
     * ChoiceGroup elements
     * @param imageElements set of images specifying the image parts of
     * the ChoiceGroup elements
     * @param implicitAllowed Flag to allow implicit selection
     *
     * @throws NullPointerException if stringElements is null
     * @throws NullPointerException if the stringElements array contains
     * any null elements
     * @throws IllegalArgumentException if the imageElements array is non-null
     * and has a different length from the stringElements array
     * @throws IllegalArgumentException if choiceType is neither
     * EXCLUSIVE nor MULTIPLE
     * @throws IllegalArgumentException if any image in the imageElements
     * array is mutable
     *
     * @see Choice#EXCLUSIVE
     * @see Choice#MULTIPLE
     * @see Choice#IMPLICIT
     */
    ChoiceGroup(String label, int choiceType, String[] stringElements,
            Image[] imageElements, boolean implicitAllowed) {

        super(label);

        if (!((choiceType == Choice.MULTIPLE) ||
                (choiceType == Choice.EXCLUSIVE) ||
                ((choiceType == Choice.IMPLICIT) && implicitAllowed) ||
                (choiceType == Choice.POPUP))) {
            throw new IllegalArgumentException();
        }

        // If stringElements is null NullPointerException will be thrown
        // as expected
        for (int x = 0; x < stringElements.length; x++) {
            if (stringElements[x] == null) {
                throw new NullPointerException();
            }
        }

        if (imageElements != null) {
            if (stringElements.length != imageElements.length) {
                throw new IllegalArgumentException();
            }
        }

        synchronized (Display.LCDUILock) {
            this.choiceType = choiceType;
            numOfEls = stringElements.length;

            cgElements = new CGElement[numOfEls + GROW_FACTOR];

            if (imageElements != null) {

                for (int i = 0; i < numOfEls; i++) {
                    cgElements[i] = new CGElement(stringElements[i],
                                                  imageElements[i]);
                }

            } else {

                for (int i = 0; i < numOfEls; i++) {
                    cgElements[i] = new CGElement(stringElements[i],
                                                  null /* image */);
                }
            }

            itemLF = choiceGroupLF = LFFactory.getFactory().getChoiceGroupLF(this);

	    // initialize fonts to default one in all elements;
	    // this has to be done after ChoiceGroupLF is created
	    for (int i = 0; i < numOfEls; i++) {
		cgElements[i].setFont(null);
	    }
        } // synchronized
    }

    /**
     * Returns the number of elements in the <code>ChoiceGroup</code>.
     * @return the number of elements in the <code>ChoiceGroup</code>
     */
    public int size() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return numOfEls;
    }

    /**
     * Gets the <code>String</code> part of the element referenced by
     * <code>elementNum</code>.
     *
     * @param elementNum the index of the element to be queried
     * @return the string part of the element
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getImage(int)
     */
    public String getString(int elementNum) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);
            // return stringEls[elementNum];
            return cgElements[elementNum].stringEl;
        }
    }

    /**
     * Gets the <code>Image</code> part of the element referenced by
     * <code>elementNum</code>.
     *
     * @param elementNum the number of the element to be queried
     * @return the image part of the element, or null if there is no image
     * @throws IndexOutOfBoundsException if elementNum is invalid
     * @see #getString(int)
     */
    public Image getImage(int elementNum) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);

            // return as mutable, if possible
            return (cgElements[elementNum].mutableImageEl == null ?
                    cgElements[elementNum].imageEl :
                    cgElements[elementNum].mutableImageEl);
        }
    }

    /**
     * Appends an element to the <code>ChoiceGroup</code>.
     *
     * @param stringPart the string part of the element to be added
     * @param imagePart the image part of the element to be added, or
     * <code>null</code> if there is no image part
     * @return the assigned index of the element
     * @throws NullPointerException if <code>stringPart</code> is
     * <code>null</code>
     */
    public int append(String stringPart, Image imagePart) {
        int elementNum = -1;

        synchronized (Display.LCDUILock) {
            checkNull(stringPart);
            if ((elementNum = insertImpl(numOfEls, stringPart, imagePart)) 
                >= 0) {
                choiceGroupLF.lInsert(elementNum, stringPart, imagePart);
            }
        }
        return elementNum;
    }

    /**
     * Inserts an element into the <code>ChoiceGroup</code> just prior to
     * the element specified.
     *
     * @param elementNum the index of the element where insertion is to occur
     * @param stringPart the string part of the element to be inserted
     * @param imagePart the image part of the element to be inserted,
     * or <code>null</code> if there is no image part
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @throws NullPointerException if <code>stringPart</code>
     * is <code>null</code>
     */
    public void insert(int elementNum, String stringPart,
                       Image imagePart) {

        synchronized (Display.LCDUILock) {
            if (elementNum < 0 || elementNum > numOfEls) {
                throw new IndexOutOfBoundsException();
            }
            checkNull(stringPart);
            if (insertImpl(elementNum, stringPart, imagePart) >= 0) {
                choiceGroupLF.lInsert(elementNum, stringPart, imagePart);
            }
        }
    }

    /**
     * Deletes the element referenced by <code>elementNum</code>.
     *
     * @param elementNum the index of the element to be deleted
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     */
    public void delete(int elementNum) {
	
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);

	    --numOfEls;

            // setup new elements array
            if (elementNum != numOfEls) {
                System.arraycopy(cgElements, elementNum + 1, cgElements,
                                 elementNum, numOfEls - elementNum);
            }

            // free some memory... (efficient for very large arrays) 
            if (cgElements.length > (GROW_FACTOR * 10) &&
		cgElements.length / numOfEls >= 2) {
                CGElement[] newArray = new CGElement[numOfEls + GROW_FACTOR];
                System.arraycopy(cgElements, 0, newArray, 0, numOfEls);
                cgElements = newArray;
                newArray = null;
            }
        
            cgElements[numOfEls] = null;

            // notify l&f
            choiceGroupLF.lDelete(elementNum);

        } // synchronized

    }

    /**
     * Deletes all elements from this <code>ChoiceGroup</code>.
     */
    public void deleteAll() {
        synchronized (Display.LCDUILock) {

            cgElements = new CGElement[GROW_FACTOR]; // initial size

            numOfEls = 0;

            choiceGroupLF.lDeleteAll();
        }
    }

    /**
     * Sets the <code>String</code> and <code>Image</code> parts of the
     * element referenced by <code>elementNum</code>,
     * replacing the previous contents of the element.
     *
     * @param elementNum the index of the element to be set
     * @param stringPart the string part of the new element
     * @param imagePart the image part of the element, or <code>null</code>
     * if there is no image part
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @throws NullPointerException if <code>stringPart</code> is
     * <code>null</code>
     */
    public void set(int elementNum, String stringPart, Image imagePart) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);
            checkNull(stringPart);

            cgElements[elementNum].set(stringPart, imagePart);

            choiceGroupLF.lSet(elementNum, stringPart, imagePart);
        }
    }

    /**
     * Gets a boolean value indicating whether this element is selected.
     *
     * @param elementNum the index of the element to be queried
     *
     * @return selection state of the element
     *
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     */
    public boolean isSelected(int elementNum) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);

	    return choiceGroupLF.lIsSelected(elementNum);
        }
    }

    /**
     * Returns the index number of an element in the
     * <code>ChoiceGroup</code> that is
     * selected. For <code>ChoiceGroup</code> objects of type
     * <code>EXCLUSIVE</code> and <code>POPUP</code>
     * there is at most one element selected, so
     * this method is useful for determining the user's choice.
     * Returns <code>-1</code> if
     * there are no elements in the <code>ChoiceGroup</code>.
     *
     * <p>For <code>ChoiceGroup</code> objects of type
     * <code>MULTIPLE</code>, this always
     * returns <code>-1</code> because no
     * single value can in general represent the state of such a
     * <code>ChoiceGroup</code>.
     * To get the complete state of a <code>MULTIPLE</code>
     * <code>Choice</code>, see {@link
     * #getSelectedFlags getSelectedFlags}.</p>
     *
     * @return index of selected element, or <code>-1</code> if none
     * @see #setSelectedIndex
     */
    public int getSelectedIndex() {
        synchronized (Display.LCDUILock) {
	    return choiceGroupLF.lGetSelectedIndex();
	}
    }

    /**
     * Queries the state of a <code>ChoiceGroup</code> and returns the state of
     * all elements in the
     * boolean array
     * <code>selectedArray_return</code>. <strong>Note:</strong> this
     * is a result parameter.
     * It must be at least as long as the size
     * of the <code>ChoiceGroup</code> as returned by <code>size()</code>.
     * If the array is longer, the extra
     * elements are set to <code>false</code>.
     *
     * <p>For <code>ChoiceGroup</code> objects of type
     * <code>MULTIPLE</code>, any
     * number of elements may be selected and set to true in the result
     * array.  For <code>ChoiceGroup</code> objects of type
     * <code>EXCLUSIVE</code> and <code>POPUP</code>
     * exactly one element will be selected, unless there are
     * zero elements in the <code>ChoiceGroup</code>. </p>
     *
     * @return the number of selected elements in the <code>ChoiceGroup</code>
     *
     * @param selectedArray_return array to contain the results
     * @throws IllegalArgumentException if <code>selectedArray_return</code>
     * is shorter than the size of the <code>ChoiceGroup</code>
     * @throws NullPointerException if <code>selectedArray_return</code>
     * is null
     * @see #setSelectedFlags
     */
    public int getSelectedFlags(boolean[] selectedArray_return) {
        checkFlag(selectedArray_return);

        synchronized (Display.LCDUILock) {
	    int numSelected = 0;
	    if (numOfEls > 0) {
                numSelected = 
		    choiceGroupLF.lGetSelectedFlags(selectedArray_return);
            }

	    for (int i = numOfEls; i < selectedArray_return.length; i++) {
		selectedArray_return[i] = false;
	    }
	    return numSelected;
        }
    }

    /**
     * For <code>ChoiceGroup</code> objects of type
     * <code>MULTIPLE</code>, this simply sets an
     * individual element's selected state.
     *
     * <P>For <code>ChoiceGroup</code> objects of type
     * <code>EXCLUSIVE</code> and <code>POPUP</code>, this can be used only to
     * select an element.  That is, the <code> selected </code> parameter must
     * be <code> true </code>. When an element is selected, the previously
     * selected element is deselected. If <code> selected </code> is <code>
     * false </code>, this call is ignored.</P>
     *
     * <p>For both list types, the <code>elementNum</code> parameter
     * must be within
     * the range
     * <code>[0..size()-1]</code>, inclusive. </p>
     *
     * @param elementNum the number of the element. Indexing of the
     * elements is zero-based
     * @param selected the new state of the element <code>true=selected</code>,
     * <code>false=not</code> selected
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getSelectedIndex
     */
    public void setSelectedIndex(int elementNum, boolean selected) {
        checkIndex(elementNum);

        synchronized (Display.LCDUILock) {
            choiceGroupLF.lSetSelectedIndex(elementNum, selected);
        } // synchronized
    }

    /**
     * Attempts to set the selected state of every element in the
     * <code>ChoiceGroup</code>. The array
     * must be at least as long as the size of the
     * <code>ChoiceGroup</code>. If the array is
     * longer, the additional values are ignored. <p>
     *
     * For <code>ChoiceGroup</code> objects of type
     * <code>MULTIPLE</code>, this sets the selected
     * state of every
     * element in the <code>Choice</code>. An arbitrary number of
     * elements may be selected.
     * <p>
     *
     * For <code>ChoiceGroup</code> objects of type
     * <code>EXCLUSIVE</code> and <code>POPUP</code>, exactly one array
     * element must have the value <code>true</code>. If no element is
     * <code>true</code>,
     * the first element
     * in the <code>Choice</code> will be selected. If two or more
     * elements are <code>true</code>, the
     * implementation will choose the first <code>true</code> element
     * and select it. <p>
     *
     * @param selectedArray an array in which the method collect the
     * selection status
     * @throws IllegalArgumentException if <code>selectedArray</code>
     * is shorter than the size of the <code>ChoiceGroup</code>
     * @throws NullPointerException if the <code>selectedArray</code>
     * is <code>null</code>
     * @see #getSelectedFlags
     */
    public void setSelectedFlags(boolean[] selectedArray) {
        synchronized (Display.LCDUILock) {
            checkFlag(selectedArray);

            if (numOfEls == 0) {
                return;
            }

            if (choiceType == Choice.MULTIPLE) {
                for (int i = 0; i < numOfEls; i++) {
                    cgElements[i].setSelected(selectedArray[i]);
                }
                choiceGroupLF.lSetSelectedFlags(selectedArray);
            } else {
                for (int i = 0; i < numOfEls; i++) {
                    if (selectedArray[i]) {
                        choiceGroupLF.lSetSelectedIndex(i, true);
                        return;
                    }
                }
                choiceGroupLF.lSetSelectedIndex(0, true);
            }

        } // synchronized
    }

    /**
     * Sets the application's preferred policy for fitting
     * <code>Choice</code> element
     * contents to the available screen space. The set policy applies for all
     * elements of the <code>Choice</code> object.  Valid values are
     * {@link #TEXT_WRAP_DEFAULT}, {@link #TEXT_WRAP_ON},
     * and {@link #TEXT_WRAP_OFF}. Fit policy is a hint, and the
     * implementation may disregard the application's preferred policy.
     *
     * @param fitPolicy preferred content fit policy for choice elements
     * @throws IllegalArgumentException if <code>fitPolicy</code> is invalid
     * @see #getFitPolicy
     */
    public void setFitPolicy(int fitPolicy) {
        if (fitPolicy < TEXT_WRAP_DEFAULT || fitPolicy > TEXT_WRAP_OFF) {
            throw new IllegalArgumentException();
        }
        synchronized (Display.LCDUILock) {
            if (this.fitPolicy != fitPolicy) {
                this.fitPolicy = fitPolicy;
                choiceGroupLF.lSetFitPolicy(fitPolicy);
            }
        }
    }

    /**
     * Gets the application's preferred policy for fitting
     * <code>Choice</code> element
     * contents to the available screen space.  The value returned is the
     * policy that had been set by the application, even if that value had
     * been disregarded by the implementation.
     *
     * @return one of {@link #TEXT_WRAP_DEFAULT}, {@link #TEXT_WRAP_ON}, or
     * {@link #TEXT_WRAP_OFF}
     * @see #setFitPolicy
     */
    public int getFitPolicy() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return fitPolicy;
    }

    /**
     * Sets the application's preferred font for
     * rendering the specified element of this <code>Choice</code>.
     * An element's font is a hint, and the implementation may disregard
     * the application's preferred font.
     *
     * <p> The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()-1]</code>, inclusive.</p>
     *
     * <p> The <code>font</code> parameter must be a valid <code>Font</code>
     * object or <code>null</code>. If the <code>font</code> parameter is
     * <code>null</code>, the implementation must use its default font
     * to render the element.</p>
     *
     * @param elementNum the index of the element, starting from zero
     * @param font the preferred font to use to render the element
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getFont
     */
    public void setFont(int elementNum, Font font) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);

            cgElements[elementNum].setFont(font);

            choiceGroupLF.lSetFont(elementNum, 
				   cgElements[elementNum].getFont());
        }
    }

    /**
     * Gets the application's preferred font for
     * rendering the specified element of this <code>Choice</code>. The
     * value returned is the font that had been set by the application,
     * even if that value had been disregarded by the implementation.
     * If no font had been set by the application, or if the application
     * explicitly set the font to <code>null</code>, the value is the default
     * font chosen by the implementation.
     *
     * <p> The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()-1]</code>, inclusive.</p>
     *
     * @param elementNum the index of the element, starting from zero
     * @return the preferred font to use to render the element
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #setFont(int elementNum, Font font)
     */
    public Font getFont(int elementNum) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);

            return cgElements[elementNum].getFont();
        }
    }

// ***********************************************************
//  package private
// ***********************************************************

    /**
     * Return whether the Item takes user input focus.
     *
     * @return return <code>true</code> if contents is not null or have
     * abstract commands.
     */
    boolean acceptFocus() {
	return super.acceptFocus() || numOfEls > 0;
    }

// ***********************************************************
//  private
// ***********************************************************

    /**
     * Insert a particular element of this ChoiceGroup
     *
     * @param elementNum The index to insert the element
     * @param stringPart The string part of the element to insert
     * @param imagePart The image part of the element to insert
     * @return int  The index of the newly inserted element
     */
    private int insertImpl(int elementNum, String stringPart,
                           Image imagePart) {
        // cgElements is created in the constructor and cannot be null
        // full capacity reached
        if (numOfEls == cgElements.length) {
            CGElement[] newCGEls = 
		new CGElement[numOfEls + GROW_FACTOR];
            System.arraycopy(cgElements, 0, newCGEls, 0, elementNum);
            System.arraycopy(cgElements, elementNum,
                             newCGEls, elementNum + 1, numOfEls - elementNum);
            cgElements = newCGEls; // swap them

        } else if (elementNum != numOfEls) {
            // if we're not appending
            System.arraycopy(cgElements, elementNum,
                             cgElements, elementNum + 1,
                             numOfEls - elementNum);
        }

        numOfEls++;

        cgElements[elementNum] = new CGElement(stringPart, imagePart);

        return elementNum;

    }

    /**
     * Check the validity of a given element index
     *
     * @param elementNum The index to check
     * @throws IndexOutOfBoundsException If no element exists at the
     *                                   that index
     */
    private void checkIndex(int elementNum) {
        if (elementNum < 0 || elementNum >= numOfEls) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Check the given values for null.
     *
     * @param stringPart The string part of the element
     * @throws NullPointerException If the string part is null
     */
    private void checkNull(String stringPart) {
        if (stringPart == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Check the validity of the selection array
     *
     * @param flag  The array of boolean flags representing the
     *              selected state of the elements
     * @throws NullPointerException If the flag array is null
     * @throws IllegalArgumentException If the flag array is not
     *                                  the same size as the element array
     */
    private void checkFlag(boolean[] flag) {
        if (flag == null) {
            throw new NullPointerException();
        }

        if (flag.length < numOfEls) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * The look&feel associated with this ChoiceGroup. 
     * Set in the constructor. 
     */
    ChoiceGroupLF choiceGroupLF; // = null

    /**
     * The type of this ChoiceGroup
     */
    int choiceType;

    /**
     * The string fit policy for this ChoiceGroup
     * '0' by default, which is Choice.TEXT_WRAP_DEFAULT
     */
    int fitPolicy;

    /*
     * NOTE:  If this is a POPUP choice group, regardless of
     * the set fit policy the system will behave as though
     * fitPolicy == Choice.TEXT_WRAP_OFF.  Popup choice elements
     * will never wrap, and may be truncated.
     */

    /**
     * The number of elements in this ChoiceGroup
     */
    int numOfEls;


    /**
     * The array containing the Font of each element (null if no setFont()
     * method was ever called). If fontEls is non-null, only the elements
     * which were set by setFont() are non-null.
     */


    // see class on the bottom of this file
    CGElement[] cgElements;


    /**
     * Optimization for CGElement array size management.
     * Notice that cgElements.length is not equal to numOfEls.
     * Use numOfEls only when accessing the array.
     */
    static final int GROW_FACTOR = 4;

    /**
     * Helper method, used solely by the native method 
     * call: updatePopupElements()
     * @return an array of string elements
     */ 
    String[] getStringElements() {
        String[] ret = new String[numOfEls];
        for (int i = 0; i < numOfEls; i++) {
            ret[i] = cgElements[i].stringEl;
        }
        return ret;
    }

    /** 
     * Helper method, used solely by the native method 
     * call: updatePopupElements()
     * @return an array of image elements
     */
    Image[] getImageElements() {
        Image[] ret = new Image[numOfEls];
        for (int i = 0; i < numOfEls; i++) {
            ret[i] = cgElements[i].imageEl;
        }
        return ret;
    }


    /**
     * Class that groups information about a single ChoiceGroup element
     * (such as its string, image, font). It also contains current state
     * information like selection.
     */
    class CGElement {

        /**
         * Creates CGElement
         * @param str - the string to be used for this ChoiceGroup element
         * @param img - the image to be used for this ChoiceGroup element
         */
        CGElement(String str, Image img) {
            set(str, img);

            // If CGElement is created from ChoiceGroup constructor
            // choiceGroupLF is not yet created there
            if (choiceGroupLF != null) {
                fontEl = choiceGroupLF.getDefaultFont();
            }
        }
        
        /**
         * Sets the string and image.
         * @param str - the string to be used for this ChoiceGroup element
         * @param img - the image to be used for this ChoiceGroup element
         */
        void set(String str, Image img) {
            stringEl = str;
            
            if (img != null && img.isMutable()) {
                // Save original, mutable Image
                mutableImageEl = img;
                // Create a snapshot for display
                imageEl = Image.createImage(img);
            } else {
                // Save the immutable image for display
                imageEl = img;
                mutableImageEl = null;
            }

            if (imageEl != null) {
              imageDataEl = imageEl.getImageData();
            }
        }

        /**
         * Set the selection.
         * @param sel the selection
         */
        void setSelected(boolean sel) {
            selected = sel;
        }

        /**
         * Returns the font that was set by the application.
         * If it was set to null or was unset, null will be returned.
         * @return the font
         */
        Font getFont() {
            return fontEl;
        }

        /**
         * Sets the font.
         * @param f - the font to set.
         */
        void setFont(Font f) {
            if (f == null) {
                f = choiceGroupLF.getDefaultFont();
            }
            fontEl = f;
        }

        /**
          * Needed for CGElementLFImpl
          * @return boolean indicating whether text wrap is on or off
          */
        boolean isWrap() {
            return (fitPolicy == TEXT_WRAP_OFF);
        }



        /** String portion of this ChoiceGroup element */
        String stringEl;      // = null;

        /** Image portion of this ChoiceGroup element (non-mutable) */
        Image imageEl;        // = null;

        /** Image portion of this ChoiceGroup element (if mutable) */
        Image mutableImageEl; // = null;

        /** ImageData portion of this ChoiceGroup element (non-mutable) */
        private ImageData imageDataEl;        // = null;

        /** Selected state of this ChoiceGroup element */
        boolean selected;     // = false;

        /**
         * Font to be used for rendering this ChoiceGroup element.
         * It should be null if setFont() was 
         * not called for this element.
         */
        private Font fontEl;       //  = null;
    }
}
