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
 * An item that can contain a string. A <code>StringItem</code> is
 * display-only; the user
 * cannot edit the contents. Both the label and the textual content of a
 * <code>StringItem</code> may be modified by the application. The
 * visual representation
 * of the label may differ from that of the textual contents.
 */
public class StringItem extends Item {

    /**
     * Creates a new <code>StringItem</code> object.  Calling this
     * constructor is equivalent to calling
     * 
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     StringItem(label, text, PLAIN);     </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * @param label the <code>Item</code> label
     * @param text the text contents
     * @see #StringItem(String, String, int)
     */
    public StringItem(String label, String text) {
        this(label, text, Item.PLAIN);
    }

    /**
     * Creates a new <code>StringItem</code> object with the given label,
     * textual content, and appearance mode.
     * Either label or text may be present or <code>null</code>.
     *
     * <p>The <code>appearanceMode</code> parameter
     * (see <a href="Item.html#appearance">Appearance Modes</a>)
     * is a hint to the platform of the application's intended use
     * for this <code>StringItem</code>.  To provide hyperlink- or
     * button-like behavior,
     * the application should associate a default <code>Command</code> with this
     * <code>StringItem</code> and add an
     * <code>ItemCommandListener</code> to this
     * <code>StringItem</code>.
     * 
     * <p>Here is an example showing the use of a
     * <code>StringItem</code> as a button: </p>
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     StringItem strItem = 
     *         new StringItem("Default: ", "Set",     
     *                        Item.BUTTON);    
     *     strItem.setDefaultCommand(
     *         new Command("Set", Command.ITEM, 1);    
     *     // icl is ItemCommandListener 
     *     strItem.setItemCommandListener(icl);     </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * @param label the <code>StringItem's</code> label, or <code>null</code>
     * if no label
     * @param text the <code>StringItem's</code> text contents, or
     * <code>null</code> if the contents are initially empty
     * @param appearanceMode the appearance mode of the <code>StringItem</code>,
     * one of {@link #PLAIN}, {@link #HYPERLINK}, or {@link #BUTTON}
     * @throws IllegalArgumentException if <code>appearanceMode</code> invalid
     * 
     */
    public StringItem(java.lang.String label,
                      java.lang.String text,
                      int appearanceMode) {
        super(label);

        synchronized (Display.LCDUILock) {
            switch (appearanceMode) {
                case Item.PLAIN:
                case Item.HYPERLINK:
                case Item.BUTTON:
                    this.appearanceMode = appearanceMode;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            this.str = text;

            itemLF = stringItemLF = LFFactory.getFactory().getStringItemLF(this);

            this.font = stringItemLF.getDefaultFont();

        } 
    }

    /**
     * Gets the text contents of the <code>StringItem</code>, or
     * <code>null</code> if the <code>StringItem</code> is
     * empty.
     * @return a string with the content of the item
     * @see #setText
     */
    public String getText() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return str;
    }

    /**
     * Sets the text contents of the <code>StringItem</code>. If text
     * is <code>null</code>,
     * the <code>StringItem</code>
     * is set to be empty.
     * @param text the new content
     * @see #getText
     */
    public void setText(String text) {
        synchronized (Display.LCDUILock) {
            this.str = text;
            stringItemLF.lSetText(text);
        }
    }

    /** 
     * Returns the appearance mode of the <code>StringItem</code>.
     * See <a href="Item.html#appearance">Appearance Modes</a>.
     *
     * @return the appearance mode value,
     * one of {@link #PLAIN}, {@link #HYPERLINK}, or {@link #BUTTON}
     * 
     */
    public int getAppearanceMode() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return appearanceMode;
    }

    /**
     * Sets the application's preferred font for
     * rendering this <code>StringItem</code>.
     * The font is a hint, and the implementation may disregard
     * the application's preferred font.
     *
     * <p> The <code>font</code> parameter must be a valid <code>Font</code>
     * object or <code>null</code>. If the <code>font</code> parameter is
     * <code>null</code>, the implementation must use its default font
     * to render the <code>StringItem</code>.</p>
     *
     * @param font the preferred font to use to render this
     *             <code>StringItem</code>
     * @see #getFont
     */
    public void setFont(Font font) {
        synchronized (Display.LCDUILock) {
            if (this.font != font) {
                if (font == null) {
                    this.font = stringItemLF.getDefaultFont();
                } else {
                    this.font = font;
                }
                stringItemLF.lSetFont(this.font);
            }
        }
    }

    /**
     * Gets the application's preferred font for
     * rendering this <code>StringItem</code>. The
     * value returned is the font that had been set by the application,
     * even if that value had been disregarded by the implementation.
     * If no font had been set by the application, or if the application
     * explicitly set the font to <code>null</code>, the value is the default
     * font chosen by the implementation.
     *
     * @return the preferred font to use to render this
     *         <code>StringItem</code>
     * @see #setFont
     */
    public Font getFont() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return font;
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
        super.setPreferredSize(width, height);
    }

    // package private methods


    // package private instance variables

    /**
     * The look&feel associated with this StringItem. 
     * Set in the constructor. getLF() should return this instance.
     */
    StringItemLF stringItemLF; // = null

    /** The text of this StringItem */
    String str;

    /** The Font to render this StringItem's text in */
    Font font;

    /** The appearance hint */
    int appearanceMode;

}
