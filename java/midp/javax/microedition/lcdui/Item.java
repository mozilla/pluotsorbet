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
 * A superclass for components that can be added to a {@link Form
 * Form}. All <code>Item</code> objects have a label field,
 * which is a string that is
 * attached to the item. The label is typically displayed near the component
 * when it is displayed within a screen.  The label should be positioned on
 * the same horizontal row as the item or
 * directly above the item.  The implementation should attempt to distinguish
 * label strings from other textual content, possibly by displaying the label
 * in a different font, aligning it to a different margin, or appending a
 * colon to it if it is placed on the same line as other string content.
 * If the screen is scrolling, the implementation should try
 * to keep the label visible at the same time as the <code>Item</code>.
 *
 * <p>In some cases,
 * when the user attempts to interact with an <code>Item</code>,
 * the system will switch to
 * a system-generated screen where the actual interaction takes place. If
 * this occurs, the label will generally be carried along and displayed within
 * this new screen in order to provide the user with some context for the
 * operation. For this reason it is recommended that applications supply a
 * label to all interactive Item objects. However, this is not required, and
 * a <code>null</code> value for a label is legal and specifies
 * the absence of a label.
 * </p>
 *
 * <h3>Item Layout</h3>
 *
 * <p>An <code>Item's</code> layout within its container is
 * influenced through layout directives:</p>
 *
 * <ul>
 * <li> <code>LAYOUT_DEFAULT</code> </li>
 * <li> <code>LAYOUT_LEFT</code> </li>
 * <li> <code>LAYOUT_RIGHT</code> </li>
 * <li> <code>LAYOUT_CENTER</code> </li>
 * <li> <code>LAYOUT_TOP</code> </li>
 * <li> <code>LAYOUT_BOTTOM</code> </li>
 * <li> <code>LAYOUT_VCENTER</code> </li>
 * <li> <code>LAYOUT_NEWLINE_BEFORE</code> </li>
 * <li> <code>LAYOUT_NEWLINE_AFTER</code> </li>
 * <li> <code>LAYOUT_SHRINK</code> </li>
 * <li> <code>LAYOUT_VSHRINK</code> </li>
 * <li> <code>LAYOUT_EXPAND</code> </li>
 * <li> <code>LAYOUT_VEXPAND</code> </li>
 * <li> <code>LAYOUT_2</code> </li>
 * </ul>
 *
 * <p>The <code>LAYOUT_DEFAULT</code> directive indicates
 * that the container's default
 * layout policy is to be used for this item.
 * <code>LAYOUT_DEFAULT</code> has the value
 * zero and has no effect when combined with other layout directives.  It is
 * useful within programs in order to document the programmer's intent.</p>
 *
 * <p>The <code>LAYOUT_LEFT</code>, <code>LAYOUT_RIGHT</code>, and
 * <code>LAYOUT_CENTER</code> directives indicate
 * horizontal alignment and are mutually exclusive.  Similarly, the
 * <code>LAYOUT_TOP</code>, <code>LAYOUT_BOTTOM</code>, and
 * <code>LAYOUT_VCENTER</code> directives indicate vertical
 * alignment and are mutually exclusive.</p>
 *
 * <p>A horizontal alignment directive, a vertical alignment directive, and
 * any combination of other layout directives may be combined using the
 * bit-wise <code>OR</code> operator (<code>|</code>) to compose a
 * layout directive value.  Such a value
 * is used as the parameter to the {@link #setLayout} method and is the return
 * value from the {@link #getLayout} method.</p>
 *
 * <p>Some directives have no defined behavior in some contexts.  A layout
 * directive is ignored if its behavior is not defined for the particular
 * context within which the <code>Item</code> resides.</p>
 *
 * <p>A complete specification of the layout of <code>Items</code>
 * within a <code>Form</code> is given
 * <a href="Form.html#layout">here</a>.</p>
 *
 * <a name="sizes"></a>
 * <h3>Item Sizes</h3>
 *
 * <p><code>Items</code> have two explicit size concepts: the <em>minimum</em>
 * size and the
 * <em>preferred</em> size.  Both the minimum and the preferred sizes refer to
 * the total area of the <code>Item</code>, which includes space for the
 * <code>Item's</code> contents,
 * the <code>Item's</code> label, as well as other space that is
 * significant to the layout
 * policy.  These sizes do not include space that is not significant for
 * layout purposes.  For example, if the addition of a label to an
 * <code>Item</code> would
 * cause other <code>Items</code> to move in order to make room,
 * then the space occupied by
 * this label is significant to layout and is counted as part of
 * the <code>Item's</code>
 * minimum and preferred sizes.  However, if an implementation were to place
 * the label in a margin area reserved exclusively for labels, this would not
 * affect the layout of neighboring <code>Items</code>.
 * In this case, the space occupied
 * by the label would not be considered part of the minimum and preferred
 * sizes.</p>
 *
 * <p>The minimum size is the smallest size at which the
 * <code>Item</code> can function and
 * display its contents, though perhaps not optimally.  The minimum size
 * may be recomputed whenever the <code>Item's</code> contents changes.</p>
 *
 * <p>The preferred size is generally a size based on the
 * <code>Item's</code> contents and
 * is the smallest size at which no information is clipped and text wrapping
 * (if any) is kept to a tolerable minimum.  The preferred size may be
 * recomputed whenever the <code>Item's</code> contents changes.
 * The application can
 * <em>lock</em> the preferred width or preferred height (or both) by
 * supplying specific values for parameters to the {@link #setPreferredSize
 * setPreferredSize} method.  The manner in which an
 * <code>Item</code> fits its contents
 * within an application-specified preferred size is implementation-specific.
 * However, it is recommended that textual content be word-wrapped to fit the
 * preferred size set by the application.  The application can <em>unlock</em>
 * either or both dimensions by supplying the value <code>-1</code>
 * for parameters to the <code>setPreferredSize</code> method.</p>
 *
 * <p>When an <code>Item</code> is created, both the preferred width
 * and height are
 * unlocked.  In this state, the implementation computes the preferred width
 * and height based on the <code>Item's</code> contents, possibly
 * including other relevant
 * factors such as the <code>Item's</code> graphic design and the
 * screen dimensions.
 * After having locked either the preferred width or height, the application
 * can restore the initial, unlocked state by calling
 * <code>setPreferredSize(-1,&nbsp;-1)</code>.</p>
 *
 * <p>The application can lock one dimension of the preferred size and leave
 * the other unlocked.  This causes the system to compute an appropriate value
 * for the unlocked dimension based on arranging the contents to fit the
 * locked dimension.  If the contents changes, the size on the unlocked
 * dimension is recomputed to reflect the new contents, but the size on the
 * locked dimension remains unchanged.  For example, if the application called
 * <code>setPreferredSize(50,&nbsp;-1)</code>, the preferred width would be
 * locked at <code>50</code> pixels and the preferred height would
 * be computed based on the
 * <code>Item's</code> contents.  Similarly, if the application called
 * <code>setPreferredSize(-1,&nbsp;60)</code>, the preferred height would be
 * locked at <code>60</code> pixels and the preferred width would be
 * computed based on the
 * <code>Item's</code> contents.  This feature is particularly useful
 * for <code>Items</code> with
 * textual content that can be line wrapped.</p>
 *
 * <p>The application can also lock both the preferred width and height to
 * specific values.  The <code>Item's</code> contents are truncated or padded
 * as necessary to honor this request.  For <code>Items</code> containing
 * text, the text should be wrapped to the specified width, and any truncation
 * should occur at the end of the text.</p>
 *
 * <p><code>Items</code> also have an implicit maximum size provided by the
 * implementation.  The maximum width is typically based on the width of the
 * screen space available to a <code>Form</code>.  Since <code>Forms</code>
 * can scroll vertically, the maximum height should typically not be based on
 * the height of the available screen space.</p>
 *
 * <p>If the application attempts to lock a preferred size dimension to a
 * value smaller than the minimum or larger than the maximum, the
 * implementation may disregard the requested value and instead use either the
 * minimum or maximum as appropriate.  If this occurs, the actual values used
 * must be visible to the application via the values returned from the
 * {@link #getPreferredWidth getPreferredWidth} and
 * {@link #getPreferredHeight getPreferredHeight} methods.
 * </p>
 *
 * <h3>Commands</h3>
 *
 * <p>A <code>Command</code> is said to be present on an <code>Item</code>
 * if the <code>Command</code> has been
 * added to this <code>Item</code> with a prior call to {@link #addCommand}
 * or {@link #setDefaultCommand} and if
 * the <code>Command</code> has not been removed with a subsequent call to
 * {@link #removeCommand}.  <code>Commands</code> present on an
 * item should have a command
 * type of <code>ITEM</code>.  However, it is not an error for a
 * command whose type is
 * other than <code>ITEM</code> to be added to an item.
 * For purposes of presentation and
 * placement within its user interface, the implementation is allowed to
 * treat a command's items as if they were of type <code>ITEM</code>. </p>
 *
 * <p><code>Items</code> may have a <em>default</em> <code>Command</code>.
 * This state is
 * controlled by the {@link #setDefaultCommand} method.  The default
 * <code>Command</code> is eligible to be bound to a special
 * platform-dependent user
 * gesture.  The implementation chooses which gesture is the most
 * appropriate to initiate the default command on that particular
 * <code>Item</code>.
 * For example, on a device that has a dedicated selection key, pressing
 * this key might invoke the item's default command.  Or, on a
 * stylus-based device, tapping on the <code>Item</code> might
 * invoke its default
 * command.  Even if it can be invoked through a special gesture, the
 * default command should also be invokable in the same fashion as
 * other item commands.</p>
 *
 * <p>It is possible that on some devices there is no special gesture
 * suitable for invoking the default command on an item.  In this case
 * the default command must be accessible to the user in the same
 * fashion as other item commands.  The implementation may use the state
 * of a command being the default in deciding where to place the command
 * in its user interface.</p>
 *
 * <p>It is possible for an <code>Item</code> not to have a default command.
 * In this
 * case, the implementation may bind its special user gesture (if any)
 * for another purpose, such as for displaying a menu of commands.  The
 * default state of an <code>Item</code> is not to have a default command.
 * An <code>Item</code>
 * may be set to have no default <code>Command</code> by removing it from
 * the <code>Item</code> or
 * by passing <code>null</code> to the <code>setDefaultCommand()</code>
 * method.</p>
 *
 * <p>The same command may occur on more than one
 * <code>Item</code> and also on more than
 * one <code>Displayable</code>.  If this situation occurs, the user
 * must be provided with
 * distinct gestures to invoke that command on each <code>Item</code> or
 * <code>Displayable</code> on
 * which it occurs, while those <code>Items</code> or <code>Displayables</code>
 * are visible on the
 * display.  When the user invokes the command, the listener
 * (<code>CommandListener</code>
 * or <code>ItemCommandListener</code> as appropriate) of just the
 * object on which the
 * command was invoked will be called.</p>
 *
 * <p>Adding commands to an <code>Item</code> may affect its appearance, the
 * way it is laid out, and the traversal behavior.  For example, the presence
 * of commands on an <code>Item</code> may cause row breaks to occur, or it
 * may cause additional graphical elements (such as a menu icon) to appear.
 * In particular, if a <code>StringItem</code> whose appearance mode is
 * <code>PLAIN</code> (see below) is given one or more <code>Commands</code>,
 * the implementation is allowed to treat it as if it had a different
 * appearance mode.</p>
 *
 * <a name="appearance"></a>
 * <h3>Appearance Modes</h3>
 *
 * <p>The <code>StringItem</code> and <code>ImageItem</code> classes have an
 * <em>appearance mode</em> attribute that can be set in their constructors.
 * This attribute can have one of the values {@link #PLAIN PLAIN},
 * {@link #HYPERLINK HYPERLINK}, or {@link #BUTTON BUTTON}.
 * An appearance mode of <code>PLAIN</code> is typically used
 * for non-interactive
 * display of textual or graphical material.  The appearance
 * mode values do not have any side effects on the interactivity of the item.
 * In order to be interactive, the item must have one or more
 * <code>Commands</code>
 * (preferably with a default command assigned), and it must have a
 * <code>CommandListener</code> that receives notification of
 * <code>Command</code> invocations.  The
 * appearance mode values also do not have any effect on the semantics of
 * <code>Command</code> invocation on the item.  For example,
 * setting the appearance mode
 * of a <code>StringItem</code> to be <code>HYPERLINK</code>
 * requests that the implementation display
 * the string contents as if they were a hyperlink in a browser.  It is the
 * application's responsibility to attach a <code>Command</code>
 * and a listener to the
 * <code>StringItem</code> that provide behaviors that the user
 * would expect from invoking
 * an operation on a hyperlink, such as loading the referent of the link or
 * adding the link to the user's set of bookmarks.</p>
 *
 * <p>Setting the appearance mode of an <code>Item</code> to be other than
 * <code>PLAIN</code> may affect its minimum, preferred, and maximum sizes, as
 * well as the way it is laid out.  For example, a <code>StringItem</code>
 * with an appearance mode of <code>BUTTON</code> should not be wrapped across
 * rows.  (However, a <code>StringItem</code> with an appearance mode of
 * <code>HYPERLINK</code> should be wrapped the same way as if its appearance
 * mode is <code>PLAIN</code>.)</p>
 *
 * <p>A <code>StringItem</code> or <code>ImageItem</code>
 * in <code>BUTTON</code> mode can be used to create a
 * button-based user interface.  This can easily lead to applications that are
 * inconvenient to use.  For example, in a traversal-based system, users must
 * navigate to a button before they can invoke any commands on it.  If buttons
 * are spread across a long <code>Form</code>, users may be required
 * to perform a
 * considerable amount of navigation in order to discover all the available
 * commands.  Furthermore, invoking a command from a button at the
 * other end of the <code>Form</code> can be quite cumbersome.
 * Traversal-based systems
 * often provide a means of invoking commands from anywhere (such as from a
 * menu), without the need to traverse to a particular item.  Instead of
 * adding a command to a button and placing that button into a
 * <code>Form</code>, it would
 * often be more appropriate and convenient for users if that command were
 * added directly to the <code>Form</code>.  Buttons should be used
 * only in cases where
 * direct user interaction with the item's string or image contents is
 * essential to the user's understanding of the commands that can be invoked
 * from that item.</p>
 *
 * <h3>Default State</h3>
 *
 * <p>Unless otherwise specified by a subclass, the default state of newly
 * created <code>Items</code> is as follows:</p>
 *
 * <ul>
 * <li>the <code>Item</code> is not contained within
 * (&quot;owned by&quot;) any container;</li>
 * <li>there are no <code>Commands</code> present;</li>
 * <li>the default <code>Command</code> is <code>null</code>;</li>
 * <li>the <code>ItemCommandListener</code> is <code>null</code>;</li>
 * <li>the layout directive value is <code>LAYOUT_DEFAULT</code>; and</li>
 * <li>both the preferred width and preferred height are unlocked.</li>
 * </ul>
 *
 * @since MIDP 1.0
 */

abstract public class Item {

// ************************************************************
//  public member variables
// ************************************************************

    /**
     * A layout directive indicating that this <code>Item</code> 
     * should follow the default layout policy of its container.
     *
     * <P>Value <code>0</code> is assigned to <code>LAYOUT_DEFAULT</code>.</P>
     *
     */
    public final static int LAYOUT_DEFAULT = 0;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * left-aligned layout.
     *
     * <P>Value <code>1</code> is assigned to <code>LAYOUT_LEFT</code>.</P>
     */
    public final static int LAYOUT_LEFT = 1;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * right-aligned layout.
     *
     * <P>Value <code>2</code> is assigned to <code>LAYOUT_RIGHT</code>.</P>
     */
    public final static int LAYOUT_RIGHT = 2;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * horizontally centered layout.
     *
     * <P>Value <code>3</code> is assigned to <code>LAYOUT_CENTER</code>.</P>
     */
    public final static int LAYOUT_CENTER = 3;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * top-aligned layout.
     *
     * <P>Value <code>0x10</code> is assigned to <code>LAYOUT_TOP</code>.</P>
     */
    public final static int LAYOUT_TOP = 0x10;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * bottom-aligned layout.
     *
     * <P>Value <code>0x20</code> is assigned to <code>LAYOUT_BOTTOM</code>.</P>
     */
    public final static int LAYOUT_BOTTOM = 0x20;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * vertically centered layout.
     *
     * <P>Value <code>0x30</code> is assigned to 
     * <code>LAYOUT_VCENTER</code>.</P>
     */
    public final static int LAYOUT_VCENTER = 0x30;

    /**
     * A layout directive indicating that this <code>Item</code> 
     * should be placed at the beginning of a new line or row.
     *
     * <P>Value <code>0x100</code> is assigned to 
     * <code>LAYOUT_NEWLINE_BEFORE</code>.</P>
     */
    public final static int LAYOUT_NEWLINE_BEFORE = 0x100;

    /**
     * A layout directive indicating that this <code>Item</code>
     * should the last on its line or row, and that the next
     * <code>Item</code> (if any) in the container
     * should be placed on a new line or row.
     *
     * <P>Value <code>0x200</code> is assigned to 
     * <code>LAYOUT_NEWLINE_AFTER</code>.</P>
     */
    public final static int LAYOUT_NEWLINE_AFTER = 0x200;

    /**
     * A layout directive indicating that this <code>Item's</code>
     * width may be reduced to its minimum width.
     *
     *<P>Value <code>0x400</code> is assigned to <code>LAYOUT_SHRINK</code></P>
     */
    public final static int LAYOUT_SHRINK = 0x400;

    /**
     * A layout directive indicating that this <code>Item's</code> 
     * width may be increased to fill available space.
     *
     *<P>Value <code>0x800</code> is assigned to <code>LAYOUT_EXPAND</code>.</P>
     */
    public final static int LAYOUT_EXPAND = 0x800;

    /**
     * A layout directive indicating that this <code>Item's</code>
     * height may be reduced to its minimum height.
     *
     * <P>Value <code>0x1000</code> is assigned to
     * <code>LAYOUT_VSHRINK</code>.</P>
     */
    public final static int LAYOUT_VSHRINK = 0x1000;

    /**
     * A layout directive indicating that this <code>Item's</code> 
     * height may be increased to fill available space.
     *
     * <P>Value <code>0x2000</code> is assigned to 
     * <code>LAYOUT_VEXPAND</code>.</P>
     */
    public final static int LAYOUT_VEXPAND = 0x2000;

    /**
     * A layout directive indicating that new MIDP layout
     * rules are in effect for this <code>Item</code>.  If this
     * bit is clear, indicates that MIDP 1.0 layout behavior
     * applies to this <code>Item</code>.
     *
     * <P>Value <code>0x4000</code> is assigned to
     * <code>LAYOUT_2</code>.</P>
     * 
     */
    public static final int LAYOUT_2 = 0x4000;

    /**
     * An appearance mode value indicating that the <code>Item</code> is to have
     * a normal appearance.
     *
     * <P>Value <code>0</code> is assigned to <code>PLAIN</code>.</P>
     */
    public final static int PLAIN = 0;

    /**
     * An appearance mode value indicating that the <code>Item</code>
     * is to appear as a hyperlink.
     * <P>Value <code>1</code> is assigned to <code>HYPERLINK</code>.</P>
     */
    public final static int HYPERLINK = 1;

    /**
     * An appearance mode value indicating that the <code>Item</code>
     * is to appear as a button.
     * <P>Value <code>2</code> is assigned to <code>BUTTON</code>.</P>
     */
    public final static int BUTTON = 2;


// ************************************************************
//  Static initializer, constructor
// ************************************************************

    /**
     * Creates a new item with a given label.
     *
     * @param label the label string; null is allowed
     */
    Item(String label) {
        // SYNC NOTE: probably safe, but since subclasses can't lock
        // around their call to super(), we'll lock it here
        synchronized (Display.LCDUILock) {
            this.label = label;
        }
    }

// ************************************************************
//  public methods
// ************************************************************

    /**
     * Sets the label of the <code>Item</code>. If <code>label</code>
     * is <code>null</code>, specifies that this item has no label.
     * 
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within  an <code>Alert</code>.</p>
     * 
     * @param label the label string
     * @throws IllegalStateException if this <code>Item</code> is contained 
     * within an <code>Alert</code>
     * @see #getLabel
     */
    public void setLabel(String label) {
        synchronized (Display.LCDUILock) {
            if (label != this.label && 
	        (label == null || !label.equals(this.label))) {
                this.label = label;
                itemLF.lSetLabel(label);
            }
        }
    }
    
    /**
     * Gets the label of this <code>Item</code> object.
     * @return the label string
     * @see #setLabel
     */
    public String getLabel() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return label;
    }

    /**
     * Gets the layout directives used for placing the item.
     * @return a combination of layout directive values
     * @see #setLayout
     */
    public int getLayout() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return layout;
    }

    /**
     * Sets the layout directives for this item.
     *
     * <p>It is illegal to call this method if this <code>Item</code> 
     * is contained within an <code>Alert</code>.</p>
     * 
     * @param layout a combination of layout directive values for this item
     * @throws IllegalArgumentException if the value of layout is not a
     * bit-wise OR combination of layout directives
     * @throws IllegalStateException if this <code>Item</code> is
     * contained within an <code>Alert</code>
     * @see #getLayout
     */
    public void setLayout(int layout) {
        synchronized (Display.LCDUILock) {
            int oldLayout = this.layout;
            setLayoutImpl(layout);
            if (oldLayout != this.layout) {
                itemLF.lSetLayout(layout);
            }
        }
    }

    /**
     * Adds a context sensitive <code>Command</code> to the item.
     * The semantic type of
     * <code>Command</code> should be <code>ITEM</code>. The implementation
     * will present the command
     * only when the item is active, for example, highlighted.
     * <p>
     * If the added command is already in the item (tested by comparing the
     * object references), the method has no effect. If the item is
     * actually visible on the display, and this call affects the set of
     * visible commands, the implementation should update the display as soon
     * as it is feasible to do so.
     *
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within an <code>Alert</code>.</p>
     *
     * @param cmd the command to be added
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     * @throws NullPointerException if cmd is <code>null</code>
     */
    public void addCommand(Command cmd) {
        synchronized (Display.LCDUILock) {
            addCommandImpl(cmd);
        }
    }

    /**
     * Removes the context sensitive command from item. If the command is not
     * in the <code>Item</code> (tested by comparing the object references),
     * the method has
     * no effect. If the <code>Item</code> is actually visible on the display, 
     * and this  call
     * affects the set of visible commands, the implementation should update
     * the display as soon as it is feasible to do so.
     *
     *
     * If the command to be removed happens to be the default command,
     * the command is removed and the default command on this Item is
     * set to <code>null</code>.
     *
     * The following code:
     * <CODE> <pre>
     *     // Command c is the default command on Item item
     *     item.removeCommand(c);
     * </pre> </CODE>
     * is equivalent to the following code:
     * <CODE> <pre>
     *     // Command c is the default command on Item item
     *     item.setDefaultCommand(null);
     *     item.removeCommand(c);
     * </pre> </CODE>
     *
     *
     * @param cmd the command to be removed
     */
    public void removeCommand(Command cmd) {
        synchronized (Display.LCDUILock) {
            removeCommandImpl(cmd);
        }
    }

    /**
     * Sets a listener for <code>Commands</code> to this <code>Item</code>,
     * replacing any previous
     * <code>ItemCommandListener</code>. A <code>null</code> reference
     * is allowed and has the effect of
     * removing any existing listener.
     *
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within an <code>Alert</code>.</p>
     *
     * @param l the new listener, or <code>null</code>.
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     */
    public void setItemCommandListener(ItemCommandListener l) {
        synchronized (Display.LCDUILock) {
            commandListener = l;
        }
    }

    /**
     * Gets the preferred width of this <code>Item</code>.  
     * If the application has locked
     * the width to a specific value, this method returns that value.
     * Otherwise, the return value is computed based on the 
     * <code>Item's</code> contents,
     * possibly with respect to the <code>Item's</code> preferred height 
     * if it is locked.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.
     *
     * @return the preferred width of the Item
     * @see #getPreferredHeight
     * @see #setPreferredSize
     */
    public int getPreferredWidth() {
	synchronized (Display.LCDUILock) {
	    if (lockedWidth != -1) {
		return lockedWidth;
	    } else { 
		return itemLF.lGetPreferredWidth(lockedHeight);
	    }
	}
    }

    /**
     * Gets the preferred height of this <code>Item</code>.  
     * If the application has locked
     * the height to a specific value, this method returns that value.
     * Otherwise, the return value is computed based on the 
     * <code>Item's</code> contents,
     * possibly with respect to the <code>Item's</code> preferred 
     * width if it is locked.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.
     *
     * @return the preferred height of the <code>Item</code>
     * @see #getPreferredWidth
     * @see #setPreferredSize
     */
    public int getPreferredHeight() {
	synchronized (Display.LCDUILock) {
	    if (lockedHeight != -1) {
		return lockedHeight;
	    } else {
		return itemLF.lGetPreferredHeight(lockedWidth);
	    }
	}
    }

    /**
     * Sets the preferred width and height for this <code>Item</code>.
     * Values for width and height less than <code>-1</code> are illegal.
     * If the width is between zero and the minimum width, inclusive,
     * the minimum width is used instead.
     * If the height is between zero and the minimum height, inclusive,
     * the minimum height is used instead.
     *
     * <p>Supplying a width or height value greater than the minimum width or 
     * height <em>locks</em> that dimension to the supplied
     * value.  The implementation may silently enforce a maximum dimension for 
     * an <code>Item</code> based on factors such as the screen size. 
     * Supplying a value of
     * <code>-1</code> for the width or height unlocks that dimension.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.</p>
     * 
     * <p>It is illegal to call this method if this <code>Item</code> 
     * is contained within  an <code>Alert</code>.</p>
     * 
     * @param width the value to which the width should be locked, or
     * <code>-1</code> to unlock
     * @param height the value to which the height should be locked, or 
     * <code>-1</code> to unlock
     * @throws IllegalArgumentException if width or height is less than 
     * <code>-1</code>
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     * @see #getPreferredHeight
     * @see #getPreferredWidth
     */
    public void setPreferredSize(int width, int height) {
        if (width < -1 || height < -1) {
            throw new IllegalArgumentException();
        }

        synchronized (Display.LCDUILock) {

            if (owner != null && owner instanceof Alert) {
                throw new IllegalStateException();
            }

            userPreferredWidth = width;
            userPreferredHeight = height;

            lUpdateLockedSize();

        } // synchronized
    }

    /**
     * Re-calculate the locked width and height using the current values
     * of preferred and minimum width and height.
     */
    void lUpdateLockedSize() {
        // Collect minimum size information
        int minWidth  = itemLF.lGetMinimumWidth();
        int minHeight = itemLF.lGetMinimumHeight();

        int newLockedWidth  = (userPreferredWidth != -1 && userPreferredWidth < minWidth)
                          ? minWidth
                          : userPreferredWidth;

        int newLockedHeight = (userPreferredHeight != -1 && userPreferredHeight < minHeight)
                          ? minHeight
                          : userPreferredHeight;
        if (newLockedWidth != lockedWidth || newLockedHeight != lockedHeight) {
            lockedWidth = newLockedWidth;
            lockedHeight = newLockedHeight;
            itemLF.lSetPreferredSize(lockedWidth, lockedHeight);
        }
    }

    /**
     * Gets the minimum width for this <code>Item</code>.  This is a width
     * at which the item can function and display its contents,
     * though perhaps not optimally.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.
     * 
     * @return the minimum width of the item
     */
    public int getMinimumWidth() {
	synchronized (Display.LCDUILock) {
	    return itemLF.lGetMinimumWidth();
	}
    }

    /**
     * Gets the minimum height for this <code>Item</code>.  This is a height
     * at which the item can function and display its contents,
     * though perhaps not optimally.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.
     *
     * @return the minimum height of the item
     */
    public int getMinimumHeight() {
	synchronized (Display.LCDUILock) {
	    return itemLF.lGetMinimumHeight();
	}
    }

    /**
     * Sets default <code>Command</code> for this <code>Item</code>.  
     * If the <code>Item</code> previously had a
     * default <code>Command</code>, that <code>Command</code> 
     * is no longer the default, but it
     * remains present on the <code>Item</code>.
     *
     * <p>If not <code>null</code>, the <code>Command</code> object
     * passed becomes the default <code>Command</code>
     * for this <code>Item</code>.  If the <code>Command</code> object
     * passed is not currently present
     * on this <code>Item</code>, it is added as if {@link #addCommand}
     * had been called
     * before it is made the default <code>Command</code>.</p>
     *
     * <p>If <code>null</code> is passed, the <code>Item</code> is set to
     * have no default <code>Command</code>.
     * The previous default <code>Command</code>, if any, remains present
     * on the <code>Item</code>.
     * </p>
     *
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within  an <code>Alert</code>.</p>
     * 
     * @param cmd the command to be used as this <code>Item's</code> default
     * <code>Command</code>, or <code>null</code> if there is to 
     * be no default command
     *
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     */
    public void setDefaultCommand(Command cmd) {
	synchronized (Display.LCDUILock) {
	    if (defaultCommand != cmd) {
		// Make sure the command present on the Item
		if (cmd != null) {
		    addCommandImpl(cmd);
		}

		// Set it as new default command
		defaultCommand = cmd;
		itemLF.lSetDefaultCommand(defaultCommand, numCommands);
	    }
	}
    }

    /**
     * Causes this <code>Item's</code> containing <code>Form</code> to notify
     * the <code>Item's</code> {@link ItemStateListener}.
     * The application calls this method to inform the
     * listener on the <code>Item</code> that the <code>Item's</code>
     * state has been changed in
     * response to an action.  Even though this method simply causes a call
     * to another part of the application, this mechanism is useful for
     * decoupling the implementation of an <code>Item</code> (in particular, the
     * implementation of a <code>CustomItem</code>, though this also applies to
     * subclasses of other items) from the consumer of the item.
     *
     * <p>If an edit was performed by invoking a separate screen, and the
     * editor now wishes to &quot;return&quot; to the form which contained the
     * selected <code>Item</code>, the preferred method is
     * <code>Display.setCurrent(Item)</code>
     * instead of <code>Display.setCurrent(Displayable)</code>,
     * because it allows the
     * <code>Form</code> to restore focus to the <code>Item</code>
     * that initially invoked the editor.</p>
     *
     * <p>In order to make sure that the documented behavior of
     * <code>ItemStateListener</code> is maintained, it is up to the caller
     * (application) to guarantee that this function is
     * not called unless:</p>
     *
     * <ul>
     * <li>the <code>Item's</code> value has actually been changed, and</li>
     * <li>the change was the result of a user action (an &quot;edit&quot;)
     * and NOT as a result of state change via calls to
     * <code>Item's</code> APIs </li>
     * </ul>
     *
     * <p>The call to <code>ItemStateListener.itemStateChanged</code>
     * may be delayed in order to be serialized with the event stream.
     * The <code>notifyStateChanged</code> method does not block awaiting
     * the completion of the <code>itemStateChanged</code> method.</p>
     *
     * @throws IllegalStateException if the <code>Item</code> is not owned
     * by a <code>Form</code>
     */
    public void notifyStateChanged() { 
	
	synchronized (Display.LCDUILock) {
	    // Among the public Displayables,
	    // List, TextBox and Form are legal to own Items.
	    // Canvas does not accept Item.
	    // Alert is the only one that we need to prevent.
	    if (owner == null || owner instanceof Alert) {
		throw new IllegalStateException();
	    }

	    // SYNC NOTE: Displayable.itemStateChanged() will
	    // simply schedule a state change event. So it's
	    // OK to call it while holding LCDUILock.
	    owner.itemStateChanged(this);
	}
    }

// ************************************************************
//  protected methods
// ************************************************************

// ************************************************************
//  package private methods
// ************************************************************

    /**
     * Set the Screen owner of this Item
     * SYNC NOTE: Caller must hold LCDUILock around this call.
     *
     * @param owner The Screen containing this Item
     */
    void lSetOwner(Screen owner) {
        if (this.owner != null && owner != null) {
	    throw new IllegalStateException();
        }

	Screen oldOwner = this.owner;
        this.owner = owner;

	itemLF.lSetOwner(oldOwner);
    }


    /**
     * Gets look & feel object associated with this
     * Item.
     * @return - ItemLF associated with this Item.
     */
    ItemLF getLF() {
        return itemLF;
    }

    /**
     * Return whether the Item takes user input focus.
     *
     * @return return <code>true</code> if abstract commands are present
     */
    boolean acceptFocus() {
	// user needs to access abstract commands
	return (numCommands > 0);
    }
    
    /**
     * Adds a context sensitive Command to the item.
     * LCDUI Lock must be acquired prior to calling this method.
     *
     * @param cmd the command to be added
     * @exception NullPointerException if cmd is null
     */
    private void addCommandImpl(Command cmd) {
        if (cmd == null) {
            throw new NullPointerException();
        }

        for (int i = 0; i < numCommands; ++i) {
            if (commands[i] == cmd) {
                return;
            }
        }

        if ((commands == null) || (numCommands == commands.length)) {
            Command[] newCommands = new Command[numCommands + 4];
            if (commands != null) {
                System.arraycopy(commands, 0, newCommands, 0,
                                 numCommands);
            }
            commands = newCommands;
        }

        commands[numCommands] = cmd;
        ++numCommands;

	itemLF.lAddCommand(cmd, numCommands);
    }

    /**
     * Removes the context sensitive command from item.
     * @param cmd the command to be removed
     */
    void removeCommandImpl(Command cmd) {
        // LCDUI Lock must be acquired
        // prior to calling this method.
        for (int i = 0; i < numCommands; ++i) {
            if (commands[i] == cmd) {
                commands[i] = commands[--numCommands];
                commands[numCommands] = null;

                if (cmd == defaultCommand) {
                    defaultCommand = null;
		    itemLF.lSetDefaultCommand(null, i);
                }

		itemLF.lRemoveCommand(cmd, i);
		return;
            }
        }
    }

    /**
     * Set the layout type of this Item
     *
     * @param layout The layout type.
     */
    void setLayoutImpl(int layout) {
        // LCDUI Lock must be acquired
        // prior to calling this method.
        if ((layout & ~VALID_LAYOUT) != 0) {
            throw new IllegalArgumentException();
        }

        this.layout = layout;
    }

     /**
      * Notify the item to the effect that it has been recently deleted
      */     
     void itemDeleted() {
         synchronized (Display.LCDUILock) {
             lSetOwner(null);
         }
     }

// ************************************************************
//  protected member variables
// ************************************************************

// ************************************************************
//  package private member variables
// ************************************************************

    /** internal bitmask representing a valid layout mask */
    final static int VALID_LAYOUT;

    static {
        VALID_LAYOUT =
            Item.LAYOUT_DEFAULT |
            Item.LAYOUT_LEFT |
            Item.LAYOUT_RIGHT |
            Item.LAYOUT_CENTER |
            Item.LAYOUT_TOP |
            Item.LAYOUT_BOTTOM |
            Item.LAYOUT_VCENTER |
            Item.LAYOUT_SHRINK |
            Item.LAYOUT_EXPAND |
            Item.LAYOUT_VSHRINK |
            Item.LAYOUT_VEXPAND |
            Item.LAYOUT_NEWLINE_BEFORE |
            Item.LAYOUT_NEWLINE_AFTER |
            Item.LAYOUT_2;
    }

    /** 
     * Item' Look&Feel object associated with this item
     *  It is set  in the subclasses constructors
     */
    ItemLF itemLF;

    /**
     * commandListener that has to be notified of when ITEM command is
     * activated
     */
    ItemCommandListener commandListener; // = null;

    /** The label of this Item */
    String label;      // = null

    /**
     * The owner Screen for this Item
     */
    Screen owner;     // = null

    /**
     * The layout type of this Item
     */
    int layout;       // = 0 ; LAYOUT_DEFAULT = 0

    /** An array of Commands added to this Item */
    Command commands[];

    /** The number of Commands added to this Item */
    int numCommands; // = 0

    /**
     * This is a default Command which represents the callback
     * to a selection event.
     */
    Command defaultCommand; // = null

    /** The locked width of this Item, -1 by default.
     * If non-default, locked width is the maximum of minimal
     * width and the preferred width. */
    int lockedWidth = -1;

    /** The preferred width of this Item, specified in the last call
     * of setPreferredSize(int width, int height), -1 by default. */
    int userPreferredWidth = -1;

    /** The locked height of this Item, -1 by default.
     * If non-default, locked height is the maximum of minimal
     * height and the preferred height. */
    int lockedHeight = -1;

    /** The preferred height of this Item, specified in the last call
     * of setPreferredSize(int width, int height), -1 by default. */
    int userPreferredHeight  = -1;
}
