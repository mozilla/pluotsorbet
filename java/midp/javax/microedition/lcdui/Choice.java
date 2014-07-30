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
 * Choice defines an API for a user interface components implementing
 * selection from predefined number of choices. Such UI components are
 * {@link List List} and {@link ChoiceGroup ChoiceGroup}.
 * The contents of the <code>Choice</code> are represented
 * with strings and images.
 *
 * <P>Each element of a <code>Choice</code> is composed of a text string part,
 * an {@link Image Image} part, and a font attribute that are all treated as a
 * unit.  The font attribute applies to the text part and can be controlled by
 * the application.  The application may provide
 * <code>null</code> for the image if the element is not to have an image
 * part.  The implementation must display the image at the beginning of the
 * text string.  If the <code>Choice</code> also has a selection
 * indicator (such as a radio
 * button or a checkbox) placed at the beginning of the text string, the
 * element's image should be placed between the selection indicator and the
 * beginning of the text string.</P>
 *
 * <P>When a new element is inserted or appended, the implementation provides
 * a default font for the font attribute.  This default font is the same font
 * that is used if the application calls <code>setFont(i, null)</code>.  All
 * <code>ChoiceGroup</code> instances must have the same default font, and all
 * <code>List</code> instances must have the same default font.  However, the
 * default font used for <code>Choice</code> objects may differ from the font
 * returned by {@link Font#getDefaultFont() Font.getDefaultFont}.</P>
 *
 * <P>The <code>Image</code> part of a <code>Choice</code> element may
 * be mutable or immutable.  If the
 * <code>Image</code> is mutable, the effect is as if snapshot of its
 * contents is taken at
 * the time the <code>Choice</code> is constructed with this
 * <code>Image</code> or when the <code>Choice</code>
 * element is created or modified with the {@link #append append},
 * {@link #insert insert}, or {@link #set set} methods.
 * The snapshot is used whenever the contents of the
 * <code>Choice</code> element are to be displayed.  Even if the
 * application subsequently
 * draws into the <code>Image</code>, the snapshot is not modified
 * until the next call to
 * one of the above methods.  The snapshot is <em>not</em> updated when the
 * <code>Choice</code> becomes visible on the display.  (This is because the
 * application does not have control over exactly when
 * <code>Displayables</code> and <code>Items</code>
 * appear and disappear from the display.)</P>
 *
 * <P>The following code illustrates a technique to refresh the image part of 
 * element <code>k</code> of a <code>Choice</code> <code>ch</code>: </P>
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *    <pre><code>
 *    ch.set(k, ch.getString(k), ch.getImage(k));    </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 *
 * <P>If the application provides an image, the implementation may choose to
 * truncate it if it exceeds the capacity of the device to display it.
 * <code>Images</code>
 * within any particular <code>Choice</code> object should all be of
 * the same size, because
 * the implementation is allowed to allocate the same amount of space for
 * every element.  The application can query the implementation's image size
 * recommendation by calling {@link Display#getBestImageWidth} and {@link
 * Display#getBestImageHeight}.</P>
 *
 * <P>If an element is very long or contains a
 * <A HREF="Form.html#linebreak">line break</A>,
 * the implementation may display only a
 * portion of it.  If this occurs, the implementation should provide
 * the user with a means to see as much as possible of the element.
 * If this is done by wrapping an element
 * to multiple lines, the second and subsequent lines should show a clear
 * indication to
 * the user that they are part of the same element and are not a
 * new element.</P>
 *
 * <P>The application can express a preference for the policy used by the
 * implementation for display of long elements including those that 
 * contain line break characters.  The characters after the first line break
 * may only be visible if the policy permits it. The {@link #setFitPolicy} and
 * {@link #getFitPolicy} methods control this preference.  The valid settings
 * are
 * {@link #TEXT_WRAP_DEFAULT}, {@link #TEXT_WRAP_ON}, and
 * {@link #TEXT_WRAP_OFF}.
 * Unless specified otherwise by <code>Choice</code> implementation
 * classes, the initial
 * value of the element fit policy is <code>TEXT_WRAP_DEFAULT</code>.</P>
 * 
 * <P>After a <code>Choice</code> object has been created, elements
 * may be inserted,
 * appended, and deleted, and each element's string part and image part may be 
 * get and set.  Elements within a <code>Choice</code> object are
 * referred to by their
 * indexes, which are consecutive integers in the range from zero to
 * <code>size()-1</code>,
 * with zero referring to the first element and <code>size()-1</code>
 * to the last element.
 * </P>
 * 
 * <P>There are four types of <code>Choices</code>: implicit-choice
 * (valid only for
 * {@link List List}), exclusive-choice,
 * multiple-choice, and pop-up (valid only for
 * {@link ChoiceGroup ChoiceGroup}). </p>
 *
 * <P>The exclusive-choice presents a series of elements and interacts with the
 * user. That is, when the user selects an element,
 * that element is shown to be selected using a distinct visual
 * representation. If there are elements present in the <code>Choice</code>,
 * one element must be selected at any given time.
 * If at any time a situation would result where there are elements in the
 * exclusive-choice but none is selected, the implementation will choose an
 * element and select it. This situation can arise when an element is added
 * to an empty <code>Choice</code>, when the selected element is
 * deleted from the
 * <code>Choice</code>, or when a <code>Choice</code> is created and
 * populated with elements
 * by a constructor.  In these cases, the choice of which element is
 * selected is left to the implementation.  Applications for
 * which the selected 
 * element is significant should set the selection explicitly.
 * The user cannot unselect an element within an
 * exclusive <code>Choice</code>.</p>
 *
 * <P>The popup choice is similar to the exclusive choice.  The selection
 * behavior of a popup choice is identical to that of an exclusive choice.
 * However, a popup choice differs from an exclusive choice in presentation
 * and interaction.  In an exclusive choice, all elements should be displayed
 * in-line.  In a popup choice, the selected element should always be
 * displayed, and the other elements should remain hidden until the user
 * performs a specific action to show them.  For example, an exclusive choice
 * could be implemented as a series of radio buttons with one always selected.
 * A popup choice could be implemented as a popup menu, with the selected
 * element being displayed in the menu button.</P>
 *
 * <P>The implicit choice is an exclusive choice where the focused or
 * highlighted element is implicitly selected when a command is initiated.  As
 * with the exclusive choice, if there are elements present in the
 * <code>Choice</code>, one element is always selected.</P>
 *
 * <P>A multiple-choice presents a series of elements and allows the user to
 * select any number of elements in any combination. As with
 * exclusive-choice, the multiple-choice interacts with the user in
 * object-operation mode. The visual appearance of a multiple-choice will
 * likely have a visual representation distinct from the exclusive-choice
 * that shows the selected state of each element as well as indicating to the
 * user that multiple elements may be selected. </P>
 *
 * <P>The selected state of an element is a property of the element. This state
 * stays with that element if other elements are inserted or deleted, causing
 * elements to be shifted around.  For example, suppose element <em>n</em> is
 * selected, and a new element is inserted at index zero.  The selected element
 * would now have index <em>n+1</em>.  A similar rule applies to deletion.  
 * Assuming <em>n</em> is greater than zero, deleting element zero would leave 
 * element <em>n-1</em> selected.  Setting the contents of an element leaves 
 * its selected state unchanged.  When a new element is inserted or appended, 
 * it is always unselected (except in the special case of adding an element to 
 * an empty Exclusive, Popup, or Implicit Choice as mentioned above).</P>
 *
 * <P>The selected state of a <code>Choice</code> object can be controlled by
 * the application with the {@link #setSelectedFlags setSelectedFlags} and
 * {@link #setSelectedIndex setSelectedIndex} methods.  This state is
 * available to the application through the {@link #getSelectedFlags
 * getSelectedFlags} and {@link #getSelectedIndex getSelectedIndex} methods.
 * The selected state reported by these methods
 * is generally identical to what has been set by the
 * application, with the following exceptions.  Adding or removing elements
 * may change the selection.  When the <code>Choice</code> is present on the
 * display, the implementation's user interface policy and direct user
 * interaction with the object may also affect the selection.  For example,
 * the implementation might update the selection to the current highlight
 * location as the user is moving the highlight, or it might set the selection
 * from the highlight only when the user is about to invoke a command.  As
 * another example, the implementation might move the highlight (and thus the
 * selection) of an implicit <code>List</code> to the first element each time
 * the <code>List</code> becomes current.  When a <code>Choice</code> object
 * is present on the display, applications should query its selected state
 * only within a {@link CommandListener CommandListener} or a {@link
 * ItemStateListener ItemStateListener} callback.  Querying the state at other
 * times might result in a value different from what has been set by the
 * application (because the user or the implementation's UI policy might have
 * changed it) and it might not reflect the user's intent (because the user
 * might still in the process of making a selection).</P>
 * 
 * <p>
 * <strong>Note:</strong> Methods have been added to the <code>Choice</code>
 * interface
 * in version 2.0.  Adding methods to interfaces is normally an incompatible
 * change.  However, <code>Choice</code> does not appear as a <em>type</em> in
 * any field, method parameter, or method return value, and so it is not
 * useful for an application to create a class that implements the
 * <code>Choice</code> interface.  Future versions of this specification may
 * make additional changes to the <code>Choice</code> interface.  In order to
 * remain compatible with future versions of this specification, applications
 * should avoid creating classes that implement the <code>Choice</code>
 * interface.
 * </p>
 * 
 * @since MIDP 1.0
 */

public interface Choice {

    /**
     * <code>EXCLUSIVE</code> is a choice having exactly one element
     * selected at time.  All
     * elements of an <code>EXCLUSIVE</code> type <code>Choice</code>
     * should be displayed in-line.  That
     * is, the user should not need to perform any extra action to traverse
     * among and select from the elements.
     *
     * <P>Value <code>1</code> is assigned to <code>EXCLUSIVE</code>.</P>
     */
    public static final int EXCLUSIVE = 1;
    
    /**
     * <code>MULTIPLE</code> is a choice that can have arbitrary number of
     * elements selected at a time.
     *
     * <P>Value <code>2</code> is assigned to <code>MULTIPLE</code>.</P>
     */
    public static final int MULTIPLE = 2;
    
    /**
     * <code>IMPLICIT</code> is a choice in which the currently focused
     * element is selected when a {@link Command Command} is initiated.
     *
     * <P>The <code>IMPLICIT</code> type is not valid for {@link
     * ChoiceGroup} objects.</P>
     *
     * <P>Value <code>3</code> is assigned to <code>IMPLICIT</code>.</P>
     */
    public static final int IMPLICIT = 3;
    
    /**
     * <code>POPUP</code> is a choice having exactly one element
     * selected at a time.  The
     * selected element is always shown.  The other elements should be hidden
     * until the user performs a particular action to show them.  When the
     * user performs this action, all elements become accessible.  For
     * example, an implementation could use a popup menu to display the
     * elements of a <code>ChoiceGroup</code> of type <code>POPUP</code>.
     *
     * <P>The <code>POPUP</code> type is not valid for {@link List} objects.</P>
     *
     * <P>Value <code>4</code> is assigned to <code>POPUP</code>.</P>
     *
     */
    public static final int POPUP = 4;

    /**
     * Constant for indicating that the application has no preference as to 
     * wrapping or truncation of text element contents and that the 
     * implementation should use its default behavior.
     *
     * <p>Field has the value <code>0</code>.</p>
     *
     * @see #getFitPolicy
     * @see #setFitPolicy
     */
    public static final int TEXT_WRAP_DEFAULT = 0;

    /**
     * Constant for hinting that text element contents should be wrapped to to
     * multiple lines if necessary to fit available content space.  The
     * Implementation may limit the maximum number of lines that it will
     * actually present.
     *
     * <p>Field has the value <code>1</code>.</p>
     * 
     * @see #getFitPolicy
     * @see #setFitPolicy
     */
    public static final int TEXT_WRAP_ON = 1;

    /**
     * Constant for hinting that text element contents should be limited to a
     * single line.  Line ending is forced, for example by cropping, if there
     * is too much text to fit to the line.  The implementation should provide
     * some means to present the full element contents.  This may be done, for
     * example, by using a special pop-up window or by scrolling the text of
     * the focused element.
     * 
     * <p>Implementations should indicate that cropping has occurred, for
     * example, by placing an ellipsis at the point where the text contents
     * have been cropped.</p>
     *
     * <p>Field has the value <code>2</code>.</p>
     * 
     * @see #getFitPolicy
     * @see #setFitPolicy
     */
    public static final int TEXT_WRAP_OFF = 2;

    /**
     * Gets the number of elements present.
     * @return the number of elements in the <code>Choice</code>
     */
    public int size();
    
    /**
     * Gets the <code>String</code> part of the element referenced by
     * <code>elementNum</code>.
     * The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()-1]</code>, inclusive. 
     *
     * @param elementNum the index of the element to be queried
     * @return the string part of the element
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getImage(int)
     */
    public String getString(int elementNum);
    
    /**
     * Gets the <code>Image</code> part of the element referenced by
     * <code>elementNum</code>.
     * The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()-1]</code>, inclusive. 
     *
     * @param elementNum the index of the element to be queried
     * @return the image part of the element, or <code>null</code>
     * if there is no image
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getString(int)
     */
    public Image getImage(int elementNum);
    
    
    /**
     * Appends an element to the <code>Choice</code>. The added
     * element will be the last
     * element of the <code>Choice</code>. The size of the
     * <code>Choice</code> grows by one.
     *
     * @param stringPart the string part of the element to be added
     * @param imagePart the image part of the element to be added, 
     * or <code>null</code> if
     * there is no image part
     * @return the assigned index of the element
     * @throws NullPointerException if <code>stringPart</code>
     * is <code>null</code>
     */
    public int append(String stringPart, Image imagePart);
    
    
    /**
     * Inserts an element into the <code>Choice</code> just prior to
     * the element specified.
     * The size of the <code>Choice</code> grows by one.
     * The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()]</code>, inclusive.  The index of the last
     * element is <code>size()-1</code>, and
     * so there is actually no element whose index is
     * <code>size()</code>. If this value
     * is used for <code>elementNum</code>, the new element is
     * inserted immediately after
     * the last element. In this case, the effect is identical to
     * {@link #append(String, Image) append()}.
     * 
     * @param elementNum the index of the element where insertion is to occur
     * @param stringPart the string part of the element to be inserted
     * @param imagePart the image part of the element to be inserted,
     * or <code>null</code> if there is no image part
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @throws NullPointerException if <code>stringPart</code>
     * is <code>null</code>
     */
    public void insert(int elementNum, String stringPart, Image imagePart);
    
    /**
     * Deletes the element referenced by <code>elementNum</code>.
     * The size of the <code>Choice</code> shrinks by
     * one. It is legal to delete all elements from a <code>Choice</code>.
     * The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()-1]</code>, inclusive. 
     *
     * @param elementNum the index of the element to be deleted
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     */
    public void delete(int elementNum);
    

    /**
     * Deletes all elements from this <code>Choice</code>, leaving it
     * with zero elements.
     * This method does nothing if the <code>Choice</code> is already empty.
     *
     */
    public void deleteAll();
  

    /**
     * Sets the <code>String</code> and <code>Image</code> parts of the
     * element referenced by <code>elementNum</code>,
     * replacing the previous contents of the element.
     * The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()-1]</code>, inclusive.  The font attribute of
     * the element is left unchanged.
     * 
     * @param elementNum the index of the element to be set
     * @param stringPart the string part of the new element
     * @param imagePart the image part of the element, or 
     * <code>null</code> if there is
     * no image part
     *
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @throws NullPointerException if <code>stringPart</code> 
     * is <code>null</code>
     */
    public void set(int elementNum, String stringPart, Image imagePart);
  

    /**
     * Gets a boolean value indicating whether this element is selected.
     * The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()-1]</code>, inclusive. 
     *
     * @param elementNum the index of the element to be queried
     *
     * @return selection state of the element
     *
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     */
    public boolean isSelected(int elementNum);
    
    
    /**
     * Returns the index number of an element in the <code>Choice</code> that is
     * selected. For
     * <code>Choice</code> types <code>EXCLUSIVE</code>,
     * <code>POPUP</code>, and <code>IMPLICIT</code>
     * there is at most one element selected, so
     * this method is useful for determining the user's
     * choice. Returns <code>-1</code> if
     * the <code>Choice</code> has no elements (and therefore has no
     * selected elements).
     *
     * <p>For <code>MULTIPLE</code>, this always returns
     * <code>-1</code> because no single
     * value can in general represent the state of such a <code>Choice</code>.
     * To get the complete state of a <code>MULTIPLE</code> <code>Choice</code>,
     * see {@link #getSelectedFlags(boolean[]) getSelectedFlags}.</p>
     *
     * @return index of selected element, or <code>-1</code> if none
     * @see #setSelectedIndex
     */
    public int getSelectedIndex();
    
    
    /**
     * Queries the state of a <code>Choice</code> and returns the
     * state of all elements
     * in the
     * boolean array
     * <code>selectedArray_return</code>. <strong>Note:</strong> this
     * is a result parameter.
     * It must be at least as long as the size
     * of the <code>Choice</code> as returned by <code>size()</code>.
     * If the array is longer, the extra
     * elements are set to <code>false</code>.
     * 
     * <p>This call is valid for all types of
     * <code>Choices</code>. For <code>MULTIPLE</code>, any
     * number of elements may be selected and set to <code>true</code>
     * in the result
     * array. For <code>EXCLUSIVE</code>, <code>POPUP</code>, and
     * <code>IMPLICIT</code>
     * exactly one element will be selected (unless there are
     * zero elements in the <code>Choice</code>). </p>
     *
     * @param selectedArray_return array to contain the results
     *
     * @return the number of selected elements in the <code>Choice</code>
     *
     * @throws IllegalArgumentException if <code>selectedArray_return</code>
     * is shorter than the size of the <code>Choice</code>.
     * @throws NullPointerException if <code>selectedArray_return</code> is
     * <code>null</code>
     * @see #setSelectedFlags
     */
    public int getSelectedFlags(boolean[] selectedArray_return);
    
    
    /**
     * For <code>MULTIPLE</code>, this simply sets an individual
     * element's selected
     * state. 
     *
     * <P>For <code>EXCLUSIVE</code> and <code>POPUP</code>,
     * this can be used only to select any
     * element, that is, the <code> selected </code> parameter must be <code>
     * true </code>. When an element is selected, the previously
     * selected element
     * is deselected. If <code> selected </code> is <code> false </code>, this
     * call is ignored. If element was already selected, the call has
     * no effect.</P>
     *
     * <P>For <code>IMPLICIT</code>,
     * this can be used only to select any
     * element, that is, the <code> selected </code> parameter must be <code>
     * true </code>. When an element is selected, the previously 
     * selected element
     * is deselected. If <code> selected </code> is <code> false </code>, this
     * call is ignored. If element was already selected, the call has
     * no effect.</P>
     *
     * <P>The call to <code>setSelectedIndex</code> does not cause
     * implicit activation of
     * any <code>Command</code>.
     * </P>
     *
     * <p>For all list types, the <code>elementNum</code> parameter
     * must be within the range
     * <code>[0..size()-1]</code>, inclusive. </p>
     * 
     * @param elementNum the index of the element, starting from zero
     * @param selected the state of the element, where <code>true</code> means
     * selected and <code>false</code> means not selected
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getSelectedIndex
     */
    public void setSelectedIndex(int elementNum, boolean selected);
    
    
    /**
     * Attempts to set the selected state of every element in the
     * <code>Choice</code>.
     * The array
     * must be at least as long as the size of the
     * <code>Choice</code>. If the array is
     * longer, the additional values are ignored. 
     *
     * <p>For <code>Choice</code> objects of type
     * <code>MULTIPLE</code>, this sets the selected
     * state of every
     * element in the <code>Choice</code>. An arbitrary number of
     * elements may be selected.
     * </p>
     *
     * <p>For <code>Choice</code> objects of type
     * <code>EXCLUSIVE</code>, <code>POPUP</code>,
     * and <code>IMPLICIT</code>, exactly one array
     * element must have the value <code>true</code>. If no element is
     * <code>true</code>, the
     * first element
     * in the <code>Choice</code> will be selected. If two or more
     * elements are <code>true</code>, the
     * implementation will choose the first <code>true</code> element
     * and select it. </p>
     *
     * @param selectedArray an array in which the method collect the
     * selection status
     * @throws IllegalArgumentException if <code>selectedArray</code> is 
     * shorter than the size of the <code>Choice</code>
     * @throws NullPointerException if <code>selectedArray</code> is 
     * <code>null</code>
     * @see #getSelectedFlags
     */
    public void setSelectedFlags(boolean[] selectedArray);

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
    public void setFitPolicy(int fitPolicy);


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
    public int getFitPolicy();

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
    public void setFont(int elementNum, Font font);

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
     * @see #setFont
     */
    public Font getFont(int elementNum);

}
