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
 * A <code>Screen</code> containing list of choices. Most of its
 * behavior is common with
 * class {@link ChoiceGroup ChoiceGroup}, and their common API. The
 * different <code>List</code> types in particular, are defined in
 * interface {@link Choice
 * Choice}.  When a <code>List</code> is present on the display, the
 * user can interact with
 * it by selecting elements and possibly by traversing and scrolling among
 * them.  Traversing and scrolling operations do not cause application-visible
 * events. The system notifies the application only when a {@link Command
 * Command} is invoked by notifying its {@link CommandListener}.  The
 * <code>List</code>
 * class also supports a select command that may be invoked specially
 * depending upon the capabilities of the device.
 *
 * <p>The notion of a <em>select</em> operation on a <code>List</code>
 * element is central
 * to the user's interaction with the <code>List</code>.  On devices
 * that have a dedicated
 * hardware &quot;select&quot; or &quot;go&quot; key, the select
 * operation is implemented with
 * that key.  Devices that do not have a dedicated key must provide another
 * means to do the select operation, for example, using a soft key.  The
 * behavior of the select operation within the different types of lists is
 * described in the following sections.</p>
 *
 * <p><code>List</code> objects may be created with <code>Choice</code> types of
 * {@link Choice#EXCLUSIVE}, {@link Choice#MULTIPLE}, and
 * {@link Choice#IMPLICIT}.  The <code>Choice</code> type {@link Choice#POPUP}
 * is not allowed on <code>List</code> objects.</p>
 *
 * <h3>Selection in <code>EXCLUSIVE</code> and <code>MULTIPLE</code> Lists</h3>
 *
 * <p>The select operation is not associated with a
 * <code>Command</code> object, so the
 * application has no means of setting a label for it or being notified when
 * the operation is performed.  In <code>Lists</code> of type
 * <code>EXCLUSIVE</code>, the select
 * operation selects the target element and deselects the previously selected
 * element.  In <code>Lists</code> of type <code>MULTIPLE</code>, the
 * select operation toggles the
 * selected state of the target element, leaving the selected state of other
 * elements unchanged.  Devices that implement the select operation using a
 * soft key will need to provide a label for it.  The label should be something
 * similar to &quot;Select&quot; for <code>Lists</code> of type
 * <code>EXCLUSIVE</code>, and it should be something
 * similar to &quot;Mark&quot; or &quot;Unmark&quot; for
 * <code>Lists</code> of type <code>MULTIPLE</code>.</p>
 *
 * <h3>Selection in <code>IMPLICIT</code> Lists</h3>
 *
 * <p>The select operation is associated with a <code>Command</code>
 * object referred to as
 * the <em>select command</em>.  When the user performs the select operation,
 * the system will invoke the select command by notifying the
 * <code>List's</code> {@link
 * CommandListener CommandListener}.  The default select command is the
 * system-provided command <code>SELECT_COMMAND</code>.  The select
 * command may be modified
 * by the application through use of the {@link #setSelectCommand(Command
 * command) setSelectCommand} method.  Devices that implement the select
 * operation using a soft key will use the label from the select command.  If
 * the select command is <code>SELECT_COMMAND</code>, the device may
 * choose to provide its
 * own label instead of using the label attribute of
 * <code>SELECT_COMMAND</code>.
 * Applications should generally provide their own select command to replace
 * <code>SELECT_COMMAND</code>.  This allows applications to provide a
 * meaningful label,
 * instead of relying on the one provided by the system for
 * <code>SELECT_COMMAND</code>.
 * The implementation must <em>not</em> invoke the select command if there are
 * no elements in the <code>List</code>, because if the
 * <code>List</code> is empty the selection does
 * not exist.  In this case the implementation should remove or disable the
 * select command if it would appear explicitly on a soft button or in a menu.
 * Other commands can be invoked normally when the <code>List</code>
 * is empty.</p>
 *
 * <h3>Use of <code>IMPLICIT</code> Lists</h3>
 *
 * <p> <code>IMPLICIT</code> <code>Lists</code> can be used to
 * construct menus by providing operations
 * as <code>List</code> elements.  The application provides a
 * <code>Command</code> that is used to
 * select a <code>List</code> element and then defines this
 * <code>Command</code> to be used as the
 * select command.  The application must also register a
 * <code>CommandListener</code> that
 * is called when the user selects or activates the <code>Command</code>:</p>
 *
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *    <pre><code>
 *     String[] elements = { ... }; //Menu items as List elements
 *     List menuList = new List("Menu", List.IMPLICIT, elements, null);
 *     Command selectCommand = new Command("Open", Command.ITEM, 1);
 *     menuList.setSelectCommand(selectCommand);
 *     menuList.setCommandListener(...);     </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 *
 * <p>The listener can query the <code>List</code> to determine which
 * element is selected
 * and then perform the corresponding action.  Note that setting a command as
 * the select command adds it to the <code>List</code> as a side effect.</p>
 *
 * <p> The select command should be considered as a <em>default operation</em>
 * that takes place when a select key is pressed.  For example, a
 * <code>List</code>
 * displaying email headers might have three operations: read, reply, and
 * delete. Read is considered to be the default operation.  </p>
 *
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *    <pre><code>
 *     List list = new List("Email", List.IMPLICIT, headers);
 *     readCommand = new Command("Read", Command.ITEM, 1);
 *     replyCommand = new Command("Reply", Command.ITEM, 2);
 *     deleteCommand = new Command("Delete", Command.ITEM, 3);
 *     list.setSelectCommand(readCommand);
 *     list.addCommand(replyCommand);
 *     list.addCommand(deleteCommand);
 *     list.setCommandListener(...);     </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 * <p>On a device with a dedicated select key, pressing this key will invoke
 * <code>readCommand</code>.  On a device without a select key, the user is
 * still able to invoke the read command, since it is also provided as an
 * ordinary <code>Command</code>.</p>
 *
 * <p> It should be noted that this kind of default operation must be used
 * carefully, and the usability of the resulting user interface must always
 * kept in mind. The default operation should always be the most intuitive
 * operation on a particular List.  </p>
 *
 * @since MIDP 1.0
 */

public class List extends Screen implements Choice {

    /**
     * The default select command for <code>IMPLICIT</code> <code>Lists</code>.
     * Applications using an <code>IMPLICIT</code> <code>List</code>
     * should set their own select command
     * using 
     * {@link #setSelectCommand(Command command) setSelectCommand}.
     * 
     * <p>
     * The field values of <code>SELECT_COMMAND</code> are:<br>
     * - <code>label = &quot;&quot;</code> (an empty string)<br>
     * - <code>type = SCREEN</code><br>
     * - <code>priority = 0</code><br>
     * </p>
     * <p>(It would be more appropriate if the type were
     * <code>ITEM</code>, but the type of
     * <code>SCREEN</code> is retained for historical purposes.)</p>
     * <p>
     * The application should not use these values for recognizing
     * the <code>SELECT_COMMAND</code>. Instead, object identities of
     * the <code>Command</code> and
     * <code>Displayable</code> (<code>List</code>) should be used.
     * </p>
     * 
     * <p><code>SELECT_COMMAND</code> is treated as an ordinary
     * <code>Command</code> if it is used with other <code>Displayable</code>
     * types.</p>
     */
    public final static Command SELECT_COMMAND =
        new Command("", Command.SCREEN, 0);

    // constructors //

    /**
     * Creates a new, empty <code>List</code>, specifying its title
     * and the type of the
     * list. 
     * @param title the screen's title (see {@link Displayable Displayable})
     * @param listType one of <code>IMPLICIT</code>, <code>EXCLUSIVE</code>,
     * or <code>MULTIPLE</code>
     * @throws IllegalArgumentException if <code>listType</code> is not
     * one of
     * <code>IMPLICIT</code>,
     * <code>EXCLUSIVE</code>, or <code>MULTIPLE</code>
     * @see Choice
     */
    public List(String title, int listType) {
        this(title, listType, new String[] {}, new Image[] {});
    }

    /**
     * Creates a new <code>List</code>, specifying its title, the type
     * of the <code>List</code>, and
     * an array of <code>Strings</code> and <code>Images</code> to be
     * used as its initial contents.
     *
     * <p>The <code>stringElements</code> array must be non-null and
     * every array element
     * must also be non-null.  The length of the
     * <code>stringElements</code> array
     * determines the number of elements in the <code>List</code>.
     * The <code>imageElements</code> array
     * may be <code>null</code> to indicate that the <code>List</code>
     * elements have no images.  If the
     * <code>imageElements</code> array is non-null, it must be the
     * same length as the
     * <code>stringElements</code> array.  Individual elements of the
     * <code>imageElements</code> array
     * may be <code>null</code> in order to indicate the absence of an
     * image for the
     * corresponding <code>List</code> element. Non-null elements of the
     * <code>imageElements</code> array may refer to mutable or
     * immutable images.</p>
     *
     * @param title the screen's title (see {@link Displayable Displayable})
     * @param listType one of <code>IMPLICIT</code>, <code>EXCLUSIVE</code>,
     * or <code>MULTIPLE</code>
     * @param stringElements set of strings specifying the string parts of the
     * <code>List</code> elements
     * @param imageElements set of images specifying the image parts of
     * the <code>List</code> elements
     *
     * @throws NullPointerException if <code>stringElements</code> is
     * <code>null</code>
     * @throws NullPointerException if the <code>stringElements</code>
     * array contains any null elements
     * @throws IllegalArgumentException if the <code>imageElements</code>
     * array is non-null
     * and has a different length from the <code>stringElements</code> array
     * @throws IllegalArgumentException if <code>listType</code> is not one 
     * of <code>IMPLICIT</code>,
     * <code>EXCLUSIVE</code>, or <code>MULTIPLE</code>
     *
     * @see Choice#EXCLUSIVE
     * @see Choice#MULTIPLE
     * @see Choice#IMPLICIT
     */
    public List(String title, int listType, String[] stringElements,
                Image[] imageElements) {

        super(title);

        if (!(listType == IMPLICIT   ||
              listType == EXCLUSIVE  ||
              listType == MULTIPLE)) {
            throw new IllegalArgumentException();
        }

        synchronized (Display.LCDUILock) {

            cg = new ChoiceGroup(null, listType,
                                 stringElements, imageElements, true);
            cg.lSetOwner(this);

            displayableLF = listLF = LFFactory.getFactory().getListLF(this);
        }
    }
// *****************************************************
//  Public  methods
// *****************************************************
    /**
     * Gets the number of elements in the <code>List</code>.
     * @return the number of elements in the <code>List</code>
     */
    public int size() {
        return cg.size();
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
        return cg.getString(elementNum);
    }

    /**
     * Gets the <code>Image</code> part of the element referenced by
     * <code>elementNum</code>.
     *
     * @param elementNum the number of the element to be queried
     * @return the image part of the element, or <code>null</code>
     * if there is no image
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getString(int)
     */
    public Image getImage(int elementNum) {
        return cg.getImage(elementNum);
    }

    /**
     * Appends an element to the <code>List</code>.
     * 
     * @param stringPart the string part of the element to be added
     * @param imagePart the image part of the element to be added, or
     * <code>null</code> if there is no image part
     * @return the assigned index of the element
     * @throws NullPointerException if <code>stringPart</code> is
     * <code>null</code>
     */
    public int append(String stringPart, Image imagePart) {
        return cg.append(stringPart, imagePart);
    }

    /**
     * Inserts an element into the <code>List</code> just prior to
     * the element specified.
     * 
     * @param elementNum the index of the element where insertion is to occur
     * @param stringPart the string part of the element to be inserted
     * @param imagePart the image part of the element to be inserted,
     * or <code>null</code> if there is no image part
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @throws NullPointerException if <code>stringPart</code> is
     * <code>null</code>
     */
    public void insert(int elementNum,
                       String stringPart, Image imagePart) {
        cg.insert(elementNum, stringPart, imagePart);
    }

    /**
     * Deletes the element referenced by <code>elementNum</code>.
     * 
     * @param elementNum the index of the element to be deleted
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     */
    public void delete(int elementNum) {
        cg.delete(elementNum);
    }

    /**
     * Deletes all elements from this List.
     */
    public void deleteAll() {
        cg.deleteAll();
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
     *
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @throws NullPointerException if <code>stringPart</code> is
     * <code>null</code>
     */
    public void set(int elementNum, String stringPart, Image imagePart) {
        cg.set(elementNum, stringPart, imagePart);
    }

    /**
     * Gets a boolean value indicating whether this element is selected.
     * 
     * @param elementNum index to element to be queried
     *
     * @return selection state of the element
     *
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     */
    public boolean isSelected(int elementNum) {
        return cg.isSelected(elementNum);
    }

    /**
     * Returns the index number of an element in the <code>List</code>
     * that is selected.
     * 
     * @return index of selected element, or <code>-1</code> if none
     * @see #setSelectedIndex
     */
    public int getSelectedIndex() {
        return cg.getSelectedIndex();
    }

    /**
     * Queries the state of a <code>List</code> and returns the
     * state of all elements
     * in the
     * boolean array
     * <code>selectedArray_return</code>.
     * 
     * @param selectedArray_return array to contain the results
     *
     * @return the number of selected elements in the <code>Choice</code>
     *
     * @throws IllegalArgumentException if <code>selectedArray_return</code>
     * is shorter than the size of the List
     * @throws NullPointerException if <code>selectedArray_return</code> 
     * is <code>null</code>
     * @see #setSelectedFlags
     */
    public int getSelectedFlags(boolean[] selectedArray_return) {
        return cg.getSelectedFlags(selectedArray_return);
    }

    /**
     * Sets the selected state of an element.
     * 
     * @param elementNum the index of the element, starting from zero
     * @param selected the state of the element, where <code>true</code> means
     * selected and <code>false</code> means not selected
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getSelectedIndex
     */
    public void setSelectedIndex(int elementNum, boolean selected) {
        cg.setSelectedIndex(elementNum, selected);
    }

    /**
     * Sets the selected state of all elements of the <code>List</code>.
     * 
     * @param selectedArray an array in which the method collect
     * the selection status
     * @throws IllegalArgumentException if <code>selectedArray</code> is
     * shorter than the size of the <code>List</code>
     * @throws NullPointerException if <code>selectedArray</code> is
     * <code>null</code>
     * @see #getSelectedFlags
     */
    public void setSelectedFlags(boolean[] selectedArray) {
        cg.setSelectedFlags(selectedArray);
    }

    /**
     * The same as {@link Displayable#removeCommand Displayable.removeCommand} 
     * but with the following additional semantics.
     * 
     * <p>If the command to be removed happens to be the select command, the
     * <code>List</code> is set to have no select command, and the command is
     * removed from the <code>List</code>.</p>
     *
     * <p>The following code: </P>
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     // Command c is the select command on List list    
     *     list.removeCommand(c);     </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * <P>
     * is equivalent to the following code: </P>
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     // Command c is the select command on List list    
     *     list.setSelectCommand(null);    
     *     list.removeCommand(c);     </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * @param cmd the command to be removed
     *
     */
    public void removeCommand(Command cmd) {
        synchronized (Display.LCDUILock) {
            int i = super.removeCommandImpl(cmd);
            if (cmd == selectCommand) {
                selectCommand = null;
            }
            if (i != -1) {
                listLF.lRemoveCommand(cmd, i);
            }
        } // synchronized
    }

    /**
     * Sets the <code>Command</code> to be used for an
     * <code>IMPLICIT</code> <code>List</code> selection
     * action.
     * By default, an implicit selection of a List will result in the
     * predefined <code>List.SELECT_COMMAND</code> being used. This
     * behavior may be
     * overridden by calling the <code>List.setSelectCommand()</code>
     * method with an
     * appropriate parameter value.  If a <code>null</code> reference
     * is passed, this
     * indicates that no &quot;select&quot; action is appropriate for
     * the contents
     * of this <code>List</code>.
     *
     * <p> If a reference to a command object is passed, and
     * it is not the special command <code>List.SELECT_COMMAND</code>, and
     * it is not currently present on this <code>List</code> object,
     * the command object is added to this <code>List</code> as if
     * <code>addCommand(command)</code> had been called
     * prior to the command being made the select command.  This
     * indicates that this command
     * is to be invoked when the user performs the &quot;select&quot;
     * on an element of
     * this <code>List</code>. </p>
     *
     * <p> The select command should have a command type of
     * <code>ITEM</code> to indicate
     * that it operates on the currently selected object.  It is not an error
     * if the command is of some other type.
     * (<code>List.SELECT_COMMAND</code> has a type
     * of <code>SCREEN</code> for historical purposes.)  For purposes
     * of presentation and
     * placement within its user interface, the implementation is allowed to
     * treat the select command as if it were of type <code>ITEM</code>. </p>
     *
     * <p> If the select command is later removed from the <code>List</code>
     * with <code>removeCommand()</code>, the <code>List</code> is set to have
     * no select command as if <code>List.setSelectCommand(null)</code> had
     * been called.</p>
     *
     * <p> The default behavior can be reestablished explicitly by calling
     * <code>setSelectCommand()</code> with an argument of
     * <code>List.SELECT_COMMAND</code>.</p>
     *
     * <p> This method has no effect if the type of the
     * <code>List</code> is not <code>IMPLICIT</code>. </p>
     *
     * @param command the command to be used for an <code>IMPLICIT</code> list
     * selection action, or <code>null</code> if there is none
     *
     */
    public void setSelectCommand(Command command) {
        // If we're not an IMPLICIT List, ignore this method
        // call entirely
        if (cg.choiceType != Choice.IMPLICIT) {
            return;
        }

        // Here we're just resetting the default behavior
        // of this implicit List
        if (command == List.SELECT_COMMAND) {
            selectCommand = command;
            return;
        }

        // Here we're deciding there is no appropriate default
        // command for a selection
        if (command == null) {
            selectCommand = null;
            return;
        }

        // SYNC NOTE: We grab the lock here because we need to determine
        // if the command is in the Displayables command set AND we need
        // to protect ourselves from the Command being removed from the
        // set just after we've done the check. #See how we override the
        // removeCommand() method in this class
        synchronized (Display.LCDUILock) {
            // We ensure that the provided Command has been added
            // to this List.
            addCommandImpl(command);

            selectCommand = command;
        }
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
        cg.setFitPolicy(fitPolicy);
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
        return cg.getFitPolicy();
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
            cg.setFont(elementNum, font);
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
        return cg.getFont(elementNum);
    }

    // package private members

    // SYNC NOTE: The List constructor establishes 'cg' as non-null
    // and which remains constant for the lifetime of this object.
    // All public api calls are delegated to the 'cg' object and
    // therefore no synchronization is necessary.
    /**
     * An internal choicegroup to handle the selections
     */
    ChoiceGroup cg;

    /**
     * This is an internal Command which represents the callback
     * to a selection event of an IMPLICIT list. By default, this
     * command is the predefined List.SELECT_COMMAND. This can be
     * overridden however using the setSelectCommand().
     */
    Command selectCommand = SELECT_COMMAND;

    /**
     * Look & Feel object associated with this List
     */
    FormLF listLF;
}
