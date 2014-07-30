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
import java.lang.IllegalArgumentException;

/**
 * A blank, non-interactive item that has a settable minimum size.  The
 * minimum width is useful for allocating flexible amounts of space between
 * <code>Items</code> within the same row of a
 * <code>Form</code>.  The minimum height is useful for
 * enforcing a particular minimum height of a row.  The application can set 
 * the minimum width or height to any non-negative value.  The implementation 
 * may enforce implementation-defined maximum values for the minimum width and 
 * height.
 *
 * <p>The unlocked preferred width of a <code>Spacer</code> is the same as its
 * current minimum width.  Its unlocked preferred height is the same as its
 * current minimum height.</p>
 * 
 * <p>Since a <code>Spacer's</code> primary purpose is to position
 * other items, it is
 * restricted to be non-interactive, and the application is not allowed to add
 * <code>Commands</code> to a <code>Spacer</code>.  Since the
 * presence of a label on an <code>Item</code> may affect
 * layout in device-specific ways, the label of a
 * <code>Spacer</code> is restricted to
 * always be <code>null</code>, and the application is not allowed
 * to change it.</p>
 * 
 */

public class Spacer extends Item {

    /**
     * Creates a new <code>Spacer</code> with the given minimum
     * size.  The <code>Spacer's</code> label
     * is <code>null</code>.
     * The minimum size must be zero or greater.
     * If <code>minWidth</code> is greater than the
     * implementation-defined maximum width, the maximum
     * width will be used instead.
     * If <code>minHeight</code> is greater than the
     * implementation-defined maximum height, the maximum
     * height will be used instead.
     *
     * @param minWidth the minimum width in pixels
     * @param minHeight the minimum height in pixels
     * @throws IllegalArgumentException if either <code>minWidth</code>
     * or <code>minHeight</code> is less than zero
     */
    public Spacer(int minWidth, int minHeight) {
        super(null);

        if (minWidth < 0 || minHeight < 0) {
             throw new IllegalArgumentException();
        }

        synchronized (Display.LCDUILock) {
            width  = minWidth;
            height = minHeight;

	    itemLF = spacerLF = LFFactory.getFactory().getSpacerLF(this);
        }
    }

    /**
     * Sets the minimum size for this spacer.  The
     * <code>Form</code> will not
     * be allowed to make the item smaller than this size.
     * The minimum size must be zero or greater.
     * If <code>minWidth</code> is greater than the
     * implementation-defined maximum width, the maximum
     * width will be used instead.
     * If <code>minHeight</code> is greater than the
     * implementation-defined maximum height, the maximum
     * height will be used instead.
     * 
     * @param minWidth the minimum width in pixels
     * @param minHeight the minimum height in pixels
     * @throws IllegalArgumentException if either <code>minWidth</code>
     * or <code>minHeight</code> is less than zero
     */
    public void setMinimumSize(int minWidth, int minHeight) {
        if (minWidth < 0 || minHeight < 0) {
             throw new IllegalArgumentException();
        }

        synchronized (Display.LCDUILock) {
            width  = minWidth;
            height = minHeight;

	    spacerLF.lSetMinimumSize(minWidth, minHeight);
        }
    }

    /**
     * <code>Spacers</code> are restricted from having
     * <code>Commands</code>, so this method will always
     * throw <code>IllegalStateException</code> whenever it is called.
     *
     * @param cmd the <code>Command</code>
     *
     * @throws IllegalStateException always
     */
    public void addCommand(Command cmd) {
        throw new IllegalStateException();
    }

    /**
     * Spacers are restricted from having <code>Commands</code>,
     * so this method will always
     * throw <code>IllegalStateException</code> whenever it is called.
     *
     * @param cmd the <code>Command</code>
     *
     * @throws IllegalStateException always
     */
    public void setDefaultCommand(Command cmd) {
        throw new IllegalStateException();
    }

    /**
     * <code>Spacers</code> are restricted to having
     * <code>null</code> labels, so this method will
     * always throw 
     * <code>IllegalStateException</code> whenever it is called.
     * 
     * @param label the label string
     *
     * @throws IllegalStateException always
     */
    public void setLabel(String label) { 
        throw new IllegalStateException();
    }


    /** Spacer Look & Feel associated with this Spacer */
    SpacerLF spacerLF; 

    /** The preferred (and minimum) width of this Spacer */
    int width;

    /** The preferred (and minimum) height of this Spacer */
    int height;

}
