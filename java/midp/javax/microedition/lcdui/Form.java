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
 * A <code>Form</code> is a <code>Screen</code> that contains
 * an arbitrary mixture of items: images,
 * read-only text fields, editable text fields, editable date fields, gauges,
 * choice groups, and custom items. In general, any subclass of the
 * {@link Item Item} class may be contained within a form.
 * The implementation handles layout, traversal, and scrolling.
 * The entire contents of the <code>Form</code> scrolls together.
 *
 * <h2>Item Management</h2>
 * <p>
 * The items contained within a <code>Form</code> may be edited
 * using append, delete,
 * insert, and set methods.  <code>Items</code> within a
 * <code>Form</code> are referred to by their
 * indexes, which are consecutive integers in the range from zero to
 * <code>size()-1</code>,
 * with zero referring to the first item and <code>size()-1</code>
 * to the last item.  </p>
 *
 * <p> An item may be placed within at most one
 * <code>Form</code>. If the application
 * attempts to place an item into a <code>Form</code>, and the
 * item is already owned by this
 * or another <code>Form</code>, an
 * <code>IllegalStateException</code> is thrown.
 * The application must
 * remove the item from its currently containing <code>Form</code>
 * before inserting it into
 * the new <code>Form</code>. </p>
 *
 * <p> If the <code>Form</code> is visible on the display when
 * changes to its contents are
 * requested by the application, updates to the display take place as soon
 * as it is feasible for the implementation to do so.
 * Applications need not take any special action to refresh a
 * <code>Form's</code> display
 * after its contents have been modified. </p>
 *
 * <a name="layout"></a>
 * <h2>Layout</h2>
 *
 * <p>Layout policy in <code>Form</code> is organized around
 * rows. Rows are typically
 * related to the width of the screen, respective of margins, scroll bars, and
 * such.  All rows in a particular <code>Form</code> will have the
 * same width.  Rows do not
 * vary in width based on the <code>Items</code> contained within
 * the <code>Form</code>, although they
 * may all change width in certain circumstances, such as when a scroll bar
 * needs to be added or removed. <code>Forms</code> generally do not scroll
 * horizontally.</p>
 *
 * <p><code>Forms</code> grow vertically and scroll vertically as
 * necessary. The height
 * of a <code>Form</code> varies depending upon the number of rows
 * and the height of
 * each row. The height of each row is determined by the items that are
 * positioned on that row. Rows need not all have the same height.
 * Implementations may also vary row heights to provide proper padding or
 * vertical alignment of <code>Item</code> labels.</p>
 *
 * <p>An implementation may choose to lay out <code>Items</code> in a
 * left-to-right or right-to-left direction depending upon the language
 * conventions in use.  The same choice of layout direction must apply to all
 * rows within a particular <code>Form</code>.</p>
 *
 * <p>Prior to the start of the layout algorithm, the
 * <code>Form</code> is considered to
 * have one empty row at the top. The layout algorithm considers each Item
 * in turn, starting at <code>Item</code> zero and proceeding in
 * order through each <code>Item</code>
 * until the last <code>Item</code> in the <code>Form</code>
 * has been processed.
 * If the layout direction (as described above) is left-to-right, the
 * beginning of the row is the left edge of the <code>Form</code>.  If the
 * layout direction is right-to-left, the beginning of the row is the right
 * edge of the <code>Form</code>.  <code>Items</code> are laid out at the
 * beginning of each row, proceeding across each row in the chosen layout
 * direction, packing as many <code>Items</code> onto each row as will fit,
 * unless a condition occurs that causes the packing of a row to be terminated
 * early.
 * A new row is then added, and
 * <code>Items</code> are packed onto it
 * as described above. <code>Items</code> are packed onto rows,
 * and new rows are added
 * below existing rows as necessary until all <code>Items</code>
 * have been processed by
 * the layout algorithm.</p>
 *
 * <p>The layout algorithm has a concept of a <em>current alignment</em>.
 * It can have the value <code>LAYOUT_LEFT</code>,
 * <code>LAYOUT_CENTER</code>, or <code>LAYOUT_RIGHT</code>.
 * The value of the current alignment at the start of the layout algorithm
 * depends upon the layout direction in effect for this <code>Form</code>.  If
 * the layout direction is left-to-right, the initial alignment value must be
 * <code>LAYOUT_LEFT</code>.  If the layout direction is right-to-left, the
 * initial alignment value must be <code>LAYOUT_RIGHT</code>.
 * The current alignment changes when the layout
 * algorithm encounters an <code>Item</code> that has one of the layout
 * directives <code>LAYOUT_LEFT</code>, <code>LAYOUT_CENTER</code>, or
 * <code>LAYOUT_RIGHT</code>.  If none of these directives is present on an
 * <code>Item</code>, the current layout directive does not change.  This
 * rule has the effect of grouping the contents of the
 * <code>Form</code> into sequences of consecutive <code>Items</code>
 * sharing an alignment value.  The alignment value of each <code>Item</code>
 * is maintained internally to the <code>Form</code> and does not affect the
 * <code>Items'</code> layout value as reported by the
 * {@link Item#getLayout Item.getLayout} method.</p>
 *
 * <p>The layout algorithm generally attempts to place an item on the same
 * row as the previous item, unless certain conditions occur that cause a
 * &quot;row break.&quot; When there is a row break, the current item
 * will be placed
 * at the beginning of a new row instead of being placed after
 * the previous item, even if there is room.</p>
 *
 * <p>A row break occurs before an item if any of the following
 * conditions occurs:</p>
 *
 * <ul>
 * <li>the previous item has a row break after it;</li>
 * <li>it has the <code>LAYOUT_NEWLINE_BEFORE</code> directive; or</li>
 * <li>it is a <code>StringItem</code> whose contents starts with
 * &quot;\n&quot;;</li>
 * <li>it is a
 * <code>ChoiceGroup</code>, <code>DateField</code>,
 * <code>Gauge</code>, or a <code>TextField</code>, and the
 * <code>LAYOUT_2</code> directive is not set; or</li>
 * <li>this <code>Item</code> has a <code>LAYOUT_LEFT</code>,
 * <code>LAYOUT_CENTER</code>, or <code>LAYOUT_RIGHT</code> directive
 * that differs from the <code>Form's</code> current alignment.</li>
 * </ul>
 *
 * <p>A row break occurs after an item if any of the following
 * conditions occurs:</p>
 *
 * <ul>
 * <li>it is a <code>StringItem</code> whose contents ends with
 * &quot;\n&quot;; or</li>
 * <li>it has the <code>LAYOUT_NEWLINE_AFTER</code> directive; or</li>
 * <li>it is a
 * <code>ChoiceGroup</code>, <code>DateField</code>,
 * <code>Gauge</code>, or a <code>TextField</code>, and the
 * <code>LAYOUT_2</code> directive is not set.</li>
 * </ul>
 *
 * <p>The presence of the <code>LAYOUT_NEWLINE_BEFORE</code> or
 * <code>LAYOUT_NEWLINE_AFTER</code> directive does not cause
 * an additional row break if there is one already present.  For example,
 * if a <code>LAYOUT_NEWLINE_BEFORE</code> directive appears on a
 * <code>StringItem</code> whose contents starts with &quot;\n&quot;,
 * there is only a single row break.  A similar rule applies with a
 * trailing &quot;\n&quot; and <code>LAYOUT_NEWLINE_AFTER</code>.
 * Also, there is only a single row
 * break if an item has the <code>LAYOUT_NEWLINE_AFTER</code> directive
 * and the next item has the <code>LAYOUT_NEWLINE_BEFORE</code> directive.
 * However, the presence of consecutive &quot;\n&quot; characters,
 * either within a single <code>StringItem</code> or in adjacent
 * <code>StringItems</code>, will cause as many row breaks as there are
 * &quot;\n&quot; characters.  This will cause empty rows to be present.
 * The height of an empty row is determined by the prevailing font height of
 * the <code>StringItem</code> within which the &quot;\n&quot; that ends the
 * row occurs.</p>
 *
 * <p>Implementations may provide additional conditions under which a row
 * break occurs.  For example, an implementation's layout policy may lay out
 * labels specially, implicitly causing a break before every
 * <code>Item</code> that has a
 * label.  Or, as another example, a particular implementation's user
 * interface style may dictate that a DateField item always appears on a row
 * by itself.  In this case, this implementation may cause row breaks to occur
 * both before and after every <code>DateField</code> item.</p>
 *
 * <p>Given two items with adjacent <code>Form</code> indexes, if
 * none of the specified
 * or implementation-specific conditions for a row break between them
 * occurs, and if space permits, these items should be placed on the same
 * row.</p>
 *
 * <p>When packing <code>Items</code> onto a row, the width of the
 * item is compared with
 * the remaining space on the row. For this purpose, the width used is the
 * <code>Item's</code> preferred width, unless the
 * <code>Item</code> has the <code>LAYOUT_SHRINK</code>
 * directive,
 * in which case the <code>Item's</code> minimum width is used. If
 * the <code>Item</code> is too wide
 * to fit in the space remaining on the row, the row is considered to be
 * full, a new row is added beneath this one, and the
 * <code>Item</code> is laid out on
 * this new row.</p>
 *
 * <p>Once the contents of a row have been determined, the space available on
 * the row is distributed by expanding items and by adding space between
 * items. If any items on this row have the
 * <code>LAYOUT_SHRINK</code> directive (that is,
 * they are shrinkable), space is first distributed to these items. Space is
 * distributed to each of these items proportionally to the difference between
 * the each <code>Item's</code> preferred size and its minimum
 * size.  At this stage, no
 * shrinkable item is expanded beyond its preferred width.</p>
 *
 * <p>For example, consider a row that has <code>30</code> pixels
 * of space available and
 * that has two shrinkable items <code>A</code> and
 * <code>B</code>. Item <code>A's</code> preferred size is
 * <code>15</code> and
 * its minimum size is <code>10</code>. Item <code>B's</code>
 * preferred size is <code>30</code> and its minimum
 * size is <code>20</code>. The difference between
 * <code>A's</code> preferred and minimum size is
 * <code>5</code>,
 * and <code>B's</code> difference is <code>10</code>. The
 * <code>30</code> pixels are distributed to these items
 * proportionally to these differences. Therefore, <code>10</code>
 * pixels are
 * distributed to item <code>A</code> and <code>20</code>
 * pixels to item <code>B</code>.</p>
 *
 * <p>If after expanding all the shrinkable items to their preferred widths,
 * there is still space left on the row, this remaining space is distributed
 * equally among the Items that have the
 * <code>LAYOUT_EXPAND</code> directive (the
 * stretchable <code>Items</code>).  The presence of any
 * stretchable items on a row will
 * cause the <code>Items</code> on this row to occupy the full
 * width of the row.</p>
 *
 * <p>If there are no stretchable items on this row, and there is still space
 * available on this row, the <code>Items</code> are packed as tightly as
 * possible and are placed on the row according to the alignment value shared
 * by the <code>Items</code> on this row.  (Since changing the current
 * alignment causes a row break, all <code>Items</code> on the same row must
 * share the same alignment value.)  If the alignment value is
 * <code>LAYOUT_LEFT</code>, the <code>Items</code> are positioned at the left
 * end of the row and the remaining space is placed at the right end of the
 * row.  If the alignment value is <code>LAYOUT_RIGHT</code>, the
 * <code>Items</code> are positioned at the right end of the row and the
 * remaining space is placed at the left end of the row.  If the alignment
 * value is <code>LAYOUT_CENTER</code>, the <code>Items</code> are positioned
 * in the middle of the row such that the remaining space on the row is
 * divided evenly between the left and right ends of the row.</p>
 *
 * <p>Given the set of items on a particular row, the heights of these
 * <code>Items</code> are inspected.  For each <code>Item</code>, the height
 * that is used is the preferred height, unless the <code>Item</code> has the
 * <code>LAYOUT_VSHRINK</code> directive, in which case the
 * <code>Item's</code> minimum height is used.
 * The height of the tallest
 * <code>Item</code> determines the
 * height of the row.  <code>Items</code> that have the
 * <code>LAYOUT_VSHRINK</code> directive are expanded to their preferred
 * height or to the height of the row, whichever is smaller.
 * <code>Items</code> that are still shorter than the
 * row height and that
 * have the <code>LAYOUT_VEXPAND</code> directive will expand to
 * the height of the row.
 * The <code>LAYOUT_VEXPAND</code> directive on an item will never
 * increase the height
 * of a row.</p>
 *
 * <p>Remaining <code>Items</code> shorter than the row height
 * will be positioned
 * vertically within the row using the <code>LAYOUT_TOP</code>,
 * <code>LAYOUT_BOTTOM</code>, and
 * <code>LAYOUT_VCENTER</code> directives.  If no vertical layout directive is
 * specified, the item must be aligned along the bottom of the row.</p>
 *
 * <p><code>StringItems</code> are treated specially in the above
 * algorithm.  If the
 * contents of a <code>StringItem</code> (its string value,
 * exclusive of its label) contain
 * a newline character (&quot;\n&quot;), the string should be split at
 * that point and
 * the remainder laid out starting on the next row.</p>
 *
 * <p>If one or both dimensions of the preferred size of
 * a <code>StringItem</code> have been locked, the <code>StringItem</code>
 * is wrapped to fit that width and height and is treated as a
 * rectangle whose minimum and preferred width and height are the width and
 * height of this rectangle. In this case, the
 * <code>LAYOUT_SHRINK</code>, <code>LAYOUT_EXPAND</code>,
 * and <code>LAYOUT_VEXPAND</code> directives are ignored.</p>
 *
 * <p>If both dimensions of the preferred size of a <code>StringItem</code>
 * are unlocked, the text from the <code>StringItem</code> may be wrapped
 * across multiple rows.  At the point in the layout algorithm where the width
 * of the <code>Item</code> is compared to the remaining space on the row, as
 * much text is taken from the beginning of the <code>StringItem</code> as
 * will fit onto the current row.  The contents of this row are then
 * positioned according to the current alignment value.  The remainder of the
 * text in the <code>StringItem</code> is line-wrapped to the full width of as
 * many new rows as are necessary to accommodate the text.  Each full row is
 * positioned according to the current alignment value.  The last line of the
 * text might leave space available on its row.  If there is no row break
 * following this <code>StringItem</code>, subsequent <code>Items</code> are
 * packed into the remaining space and the contents of the row are positioned
 * according to the current alignment value.  This rule has the effect of
 * displaying the contents of a <code>StringItem</code> as a paragraph of text
 * set flush-left, flush-right, or centered, depending upon whether the
 * current alignment value is <code>LAYOUT_LEFT</code>,
 * <code>LAYOUT_RIGHT</code>, or <code>LAYOUT_CENTER</code>, respectively.
 * The preferred width and height of a <code>StringItem</code> wrapped across
 * multiple rows, as reported by the
 * {@link Item#getPreferredWidth Item.getPreferredWidth} and
 * {@link Item#getPreferredHeight Item.getPreferredHeight}
 * methods, describe the width and height of the bounding rectangle of the
 * wrapped text.</p>
 *
 * <p><code>ImageItems</code> are also treated specially by the above
 * algorithm.  The foregoing rules concerning the horizontal alignment value
 * and the <code>LAYOUT_LEFT</code>, <code>LAYOUT_RIGHT</code>, and
 * <code>LAYOUT_CENTER</code> directives, apply to <code>ImageItems</code>
 * only when the <code>LAYOUT_2</code> directive is also present on that item.
 * If the <code>LAYOUT_2</code> directive is not present on an
 * <code>ImageItem</code>, the behavior of the <code>LAYOUT_LEFT</code>,
 * <code>LAYOUT_RIGHT</code>, and <code>LAYOUT_CENTER</code> directives is
 * implementation-specific.</p>
 *
 * <p>A <code>Form's</code> layout is recomputed automatically as
 * necessary.  This may
 * occur because of a change in an <code>Item's</code> size caused
 * by a change in its
 * contents or because of a request by the application to change the Item's
 * preferred size.  It may also occur if an <code>Item's</code>
 * layout directives are
 * changed by the application.  The application does not need to perform
 * any specific action to cause the <code>Form's</code> layout to
 * be updated.</p>
 *
 * <h2><a NAME="linebreak">Line Breaks and Wrapping</a></h2>
 *
 * <p>For all cases where text is wrapped,
 * line breaks must occur at each newline character
 * (<code>'\n'</code> = Unicode <code>'U+000A'</code>).  
 * If space does not permit
 * the full text to be displayed it is truncated at line breaks.
 * If there are no suitable line breaks, it is recommended that
 * implementations break text at word boundaries.
 * If there are no word boundaries, it is recommended that
 * implementations break text at character boundaries. </p>
 *
 * <p>Labels that contain line breaks may be truncated at the line
 * break and cause the rest of the label not to be shown.</p>
 *
 * <h2>User Interaction</h2>
 *
 * <p> When a <code>Form</code> is present on the display the user
 * can interact
 * with it and its <code>Items</code> indefinitely (for instance,
 * traversing from <code>Item</code>
 * to <code>Item</code>
 * and possibly
 * scrolling). These traversing and scrolling operations do not cause
 * application-visible events. The system notifies
 * the application when the user modifies the state of an interactive
 * <code>Item</code>
 * contained within the <code>Form</code>.  This notification is
 * accomplished by calling the
 * {@link ItemStateListener#itemStateChanged itemStateChanged()}
 * method of the listener declared to the <code>Form</code> with the
 * {@link #setItemStateListener setItemStateListener()} method. </p>
 *
 * <p> As with other <code>Displayable</code> objects, a
 * <code>Form</code> can declare
 * {@link Command commands} and declare a command listener with the
 * {@link Displayable#setCommandListener setCommandListener()} method.
 * {@link CommandListener CommandListener}
 * objects are distinct from
 * {@link ItemStateListener ItemStateListener} objects, and they are declared
 * and invoked separately. </p>
 *
 * <h2>Notes for Application Developers</h2>
 *
 * <UL>
 * <LI>Although this class allows creation of arbitrary combination of
 * components
 * the application developers should keep the small screen size in mind.
 * <code>Form</code> is designed to contain a <em>small number of
 * closely related</em>
 * UI elements. </LI>
 *
 * <LI>If the number of items does not fit on the screen, the
 * implementation may choose to make it scrollable or to fold some components
 * so that a separate screen appears when the element is edited.</LI>
 * </UL>
 *
 * <p>
 * </p>
 *
 * @see Item
 * @since MIDP 1.0
 */

public class Form extends Screen {

// ************************************************************
//  Static initializer, constructor
// ************************************************************

    /**
     * Creates a new, empty <code>Form</code>.
     *
     * @param title the <code>Form's</code> title, or
     * <code>null</code> for no title
     */
    public Form(String title) {
        this(title, null);
    }

    /**
     * Creates a new <code>Form</code> with the specified
     * contents. This is identical to
     * creating an empty <code>Form</code> and then using a set of
     * <code>append</code>
     * methods.  The
     * items array may be <code>null</code>, in which case the
     * <code>Form</code> is created empty.  If
     * the items array is non-null, each element must be a valid
     * <code>Item</code> not
     * already contained within another <code>Form</code>.
     *
     * @param title the <code>Form's</code> title string
     * @param items the array of items to be placed in the
     * <code>Form</code>, or <code>null</code> if there are no
     * items
     * @throws IllegalStateException if one of the items is already owned by
     * another container
     * @throws NullPointerException if an element of the items array is
     * <code>null</code>
     */
    public Form(String title, Item[] items) {
        super(title);

        synchronized (Display.LCDUILock) {
            if (items == null) {
                this.items = new Item[GROW_SIZE];
                // numOfItems was initialized to 0
                // so there is no need to update it

            } else {
                this.items = new Item[items.length > GROW_SIZE ?
                                      items.length : GROW_SIZE];
           
                // We have to check all items first so that some
                // items would not be added to a form that was not
                // instantiated
                for (int i = 0; i < items.length; i++) {
                    // NullPointerException will be thrown by
                    // items[i].owner if items[i] == null;
                    if (items[i].owner != null) {
                        throw new IllegalStateException();
                    }
                }
                
                numOfItems = items.length;
                
                for (int i = 0; i < numOfItems; i++) {
                    items[i].lSetOwner(this);
                    this.items[i] = items[i];
                }
            }
            displayableLF = formLF = LFFactory.getFactory().getFormLF(this);
        } // synchronized
    }

// ************************************************************
//  public methods
// ************************************************************

    /**
     * Adds an <code>Item</code> into the <code>Form</code>.  The newly
     * added <code>Item</code> becomes the last <code>Item</code> in the
     * <code>Form</code>, and the size of the <code>Form</code> grows
     * by one.
     *
     * @param item the {@link Item Item} to be added.
     * @return the assigned index of the <code>Item</code>
     * @throws IllegalStateException if the item is already owned by
     * a container
     * @throws NullPointerException if item is <code>null</code>
     */
    public int append(Item item) {
        synchronized (Display.LCDUILock) {
            // NullPointerException will be thrown
            // by item.owner if item == null
            if (item.owner != null) {
                throw new IllegalStateException();
            }
            int i = insertImpl(numOfItems, item);
            formLF.lInsert(i, item);
            return i;
        }
    }

    /**
     * Adds an item consisting of one <code>String</code> to the
     * <code>Form</code>. The effect of
     * this method is identical to 
     *
     * <p> <code>
     * append(new StringItem(null, str))
     * </code> </p>
     *
     * @param str the <code>String</code> to be added
     * @return the assigned index of the <code>Item</code>
     * @throws NullPointerException if str is <code>null</code>
     */
    public int append(String str) {
        if (str == null) {
            throw new NullPointerException();
        }

        synchronized (Display.LCDUILock) {
            Item item = new StringItem(null, str);
            int i = insertImpl(numOfItems, item);
            formLF.lInsert(i, item);
            return i;
        }
    }
    
    /**
     * Adds an item consisting of one <code>Image</code> to the
     * <code>Form</code>. The effect of
     * this method is identical to 
     *
     * <p> <code>
     * append(new ImageItem(null, img, ImageItem.LAYOUT_DEFAULT, null))
     * </code> </p>
     *
     * @param img the image to be added
     * @return the assigned index of the <code>Item</code>
     * @throws NullPointerException if <code>img</code> is <code>null</code>
     */
    public int append(Image img) {
        if (img == null) {
            throw new NullPointerException();
        }

        synchronized (Display.LCDUILock) {
            Item item = new ImageItem(null, img, 
                                      ImageItem.LAYOUT_DEFAULT, null);
            int i = insertImpl(numOfItems, item);
            formLF.lInsert(i, item);
            return i;
        }
    }

    /**
     * Inserts an item into the <code>Form</code> just prior to
     * the item specified.
     * The size of the <code>Form</code> grows by one.  The
     * <code>itemNum</code> parameter must be
     * within the range <code>[0..size()]</code>, inclusive.
     * The index of the last item is <code>size()-1</code>, and 
     * so there is actually no item whose index is
     * <code>size()</code>. If this value
     * is used for <code>itemNum</code>, the new item is inserted
     * immediately after
     * the last item. In this case, the effect is identical to
     * {@link #append(Item) append(Item)}. 
     *
     * <p> The semantics are otherwise identical to
     * {@link #append(Item) append(Item)}. </p>
     *
     * @param itemNum the index where insertion is to occur
     * @param item the item to be inserted
     * @throws IndexOutOfBoundsException if <code>itemNum</code> is invalid
     * @throws IllegalStateException if the item is already owned by
     * a container
     * @throws NullPointerException if <code>item</code> is
     * <code>null</code>
     */
    public void insert(int itemNum, Item item) {
        synchronized (Display.LCDUILock) {
            // NullPointerException will be thrown
            // by item.owner if item == null
            if (item.owner != null) {
                throw new IllegalStateException();
            }

            if (itemNum < 0 || itemNum > numOfItems) {
                throw new IndexOutOfBoundsException();
            }
            insertImpl(itemNum, item);
            formLF.lInsert(itemNum, item);
        }
    }

    /**
     * Deletes the <code>Item</code> referenced by
     * <code>itemNum</code>. The size of the <code>Form</code>
     * shrinks by one. It is legal to delete all items from a
     * <code>Form</code>.
     * The <code>itemNum</code> parameter must be 
     * within the range <code>[0..size()-1]</code>, inclusive. 
     *
     * @param itemNum the index of the item to be deleted
     * @throws IndexOutOfBoundsException if <code>itemNum</code> is invalid
     */
    public void delete(int itemNum) {
    	Item deletedItem;
        synchronized (Display.LCDUILock) {
            if (itemNum < 0 || itemNum >= numOfItems) {
                throw new IndexOutOfBoundsException();
            }

            deletedItem = items[itemNum];

            numOfItems--;

            if (itemNum < numOfItems) {
                System.arraycopy(items, itemNum + 1, items, itemNum,
                                 numOfItems - itemNum);
            }

            // Delete reference to the last item 
            // that was left after array copy
            items[numOfItems] = null;

            // The Form is clear; reset its state
            if (numOfItems == 0 && items.length > GROW_SIZE) {
                items = new Item[GROW_SIZE];                 // start fresh
            }

            formLF.lDelete(itemNum, deletedItem);

        } // synchronized
        
        deletedItem.itemDeleted();
    }

    /**
     * Deletes all the items from this <code>Form</code>, leaving
     * it with zero items.
     * This method does nothing if the <code>Form</code> is already empty.
     *
     */
    public void deleteAll() {
    	Item[] itemsCopy;
        synchronized (Display.LCDUILock) {
            if (numOfItems == 0) {
                return;
            }
            itemsCopy = new Item[numOfItems];

            for (int x = 0; x < numOfItems; x++) {
                itemsCopy[x] = items[x];
                items[x] = null;
            }
            if (items.length > GROW_SIZE) {
                items = new Item[GROW_SIZE];                     // start fresh
            }

            // Reset form state
            numOfItems = 0;

            formLF.lDeleteAll();
        }
        
        for (int x = 0; x < itemsCopy.length; x++) {
            itemsCopy[x].itemDeleted();
        }
    }

    /**
     * Sets the item referenced by <code>itemNum</code> to the
     * specified item,
     * replacing the previous item. The previous item is removed
     * from this <code>Form</code>.
     * The <code>itemNum</code> parameter must be 
     * within the range <code>[0..size()-1]</code>, inclusive. 
     *
     * <p>The end result is equal to
     * <code>insert(n, item); delete(n+1);</code><br>
     * although the implementation may optimize the repainting
     * and usage of the array that stores the items. <P>
     *
     * @param itemNum the index of the item to be replaced
     * @param item the new item to be placed in the <code>Form</code>
     *
     * @throws IndexOutOfBoundsException if <code>itemNum</code> is invalid
     * @throws IllegalStateException if the item is already owned by
     * a container
     * @throws NullPointerException if <code>item</code> is 
     * <code>null</code>
     */
    public void set(int itemNum, Item item) {
        synchronized (Display.LCDUILock) {
            // NullPointerException will be thrown
            // by item.owner if item == null
            if (item.owner != null) {
                throw new IllegalStateException();
            }

            if (itemNum < 0 || itemNum >= numOfItems) {
                throw new IndexOutOfBoundsException();
            }

            items[itemNum].lSetOwner(null);
            item.lSetOwner(this);

            items[itemNum] = item;

            formLF.lSet(itemNum, item);
        }
    }
     
    /**
     * Gets the item at given position.  The contents of the
     * <code>Form</code> are left
     * unchanged.
     * The <code>itemNum</code> parameter must be 
     * within the range <code>[0..size()-1]</code>, inclusive. 
     *
     * @param itemNum the index of item
     *
     * @return the item at the given position
     *
     * @throws IndexOutOfBoundsException if <code>itemNum</code> is invalid
     */
    public Item get(int itemNum) {
        synchronized (Display.LCDUILock) {
            if (itemNum < 0 || itemNum >= numOfItems) {
                throw new IndexOutOfBoundsException();
            }

            return items[itemNum];
        }
    }

    /**
     * Sets the <code>ItemStateListener</code> for the
     * <code>Form</code>, replacing any previous
     * <code>ItemStateListener</code>. If
     * <code>iListener</code> is <code>null</code>, simply
     * removes the previous <code>ItemStateListener</code>.
     * @param iListener the new listener, or <code>null</code> to remove it
     */
    public void setItemStateListener(ItemStateListener iListener) {
        synchronized (Display.LCDUILock) {
            itemStateListener = iListener;
        }
    }

    /**
     * Gets the number of items in the <code>Form</code>.
     * @return the number of items
     */
    public int size() {
        synchronized (Display.LCDUILock) {
	        return numOfItems;
	    }
    }

    /**
     * Returns the width in pixels of the displayable area available for items.
     * The value may depend on how the device uses the screen and may be
     * affected by the presence or absence of the ticker, title, or commands.
     * The <code>Items</code> of the <code>Form</code> are
     * laid out to fit within this width.
     * @return the width of the <code>Form</code> in pixels
     */
    public int getWidth() {
        synchronized (Display.LCDUILock) {
            return formLF.lGetWidth();
        }
    } 

    /**
     * Returns the height in pixels of the displayable area available
     * for items.
     * This value is the height of the form that can be displayed without
     * scrolling.
     * The value may depend on how the device uses the screen and may be
     * affected by the presence or absence of the ticker, title, or commands.
     * @return the height of the displayable area of the 
     * <code>Form</code> in pixels
     */
    public int getHeight() {
	synchronized (Display.LCDUILock) {
	    return formLF.lGetHeight();
	}
    }

// ************************************************************
//  protected methods
// ************************************************************

// ************************************************************
//  package private methods
// ************************************************************

    /**
     * Retrieve the ItemStateListener for this Form.
     * NOTE: calls to this method should only occur from within
     * a lock on LCDUILock.
     *
     * @return ItemStateListener The ItemStateListener of this Form,
     *                           null if there isn't one set
     */
    ItemStateListener getItemStateListener() {
        return itemStateListener;
    }

    /**
     * Used by the event handler to notify the ItemStateListener
     * of a change in the given item
     *
     * @param item the Item which state was changed
     */
    void uCallItemStateChanged(Item item) {
        // get a copy of the object reference to ItemStateListener
        ItemStateListener isl = itemStateListener;
        if (isl == null || item == null) {
            return;
        }

        // Protect from any unexpected application exceptions
        try {
            // SYNC NOTE: We lock on calloutLock around any calls
            // into application code
            synchronized (Display.calloutLock) {
                isl.itemStateChanged(item);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

// ************************************************************
//  private methods
// ************************************************************

    /**
     * Insert an Item into this Form
     *
     * @param itemNum The index into the Item array to insert this Item
     * @param item The Item to insert
     * @return int The index at which the newly inserted Item can be found
     */
    private int insertImpl(int itemNum, Item item) {

        if (items.length == numOfItems) {
            Item newItems[] = new Item[numOfItems + GROW_SIZE];
            System.arraycopy(items, 0, newItems, 0, itemNum);
            System.arraycopy(items, itemNum, newItems, itemNum + 1,
                             numOfItems - itemNum);
            items = newItems;
        } else {
            // if we're not appending
            if (itemNum != numOfItems) {
                System.arraycopy(items, itemNum, items, itemNum + 1,
                                 numOfItems - itemNum);
            }
        }

        numOfItems++;

        //
        // the arraycopy copied the reference to the item at this
        // spot. if we call setImpl without setting the index to null
        // setImpl will set the items owner to null.
        //
        item.lSetOwner(this);
        items[itemNum] = item;

        return itemNum;
    }

// ************************************************************
//  public member variables
// ************************************************************

// ************************************************************
//  protected member variables
// ************************************************************

// ************************************************************
//  package private member variables
// ************************************************************


    /** Array of Items that were added to this form. */
    Item items[];

    /** The number of actual Items added is numOfItems. */
    int  numOfItems; // = 0;

    /** The Form look&feel object associated with this Form */
    FormLF formLF;

// ************************************************************
//  private member variables
// ************************************************************


    /**
     * This is the rate at which the internal array of Items grows if
     * it gets filled up
     */
    private static final int GROW_SIZE = 4;

    /** itemStateListener that has to be notified of any state changes */
    private ItemStateListener itemStateListener;

} // class Form

