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
 * This interface is used by applications which need to receive
 * events that indicate changes in the internal
 * state of the interactive items within a {@link Form Form}
 * screen.
 *
 * @see Form#setItemStateListener(ItemStateListener)
 * @since MIDP 1.0
 */
public interface ItemStateListener {

    /**
     * Called when internal state of an <code>Item</code> has been
     * changed by the user.
     * This happens when the user:
     * <UL>
     * <LI>changes the set of selected values in a
     * <code>ChoiceGroup</code>;</LI>
     * <LI>adjusts the value of an interactive <code>Gauge</code>;</LI>
     * <LI>enters or modifies the value in a <code>TextField</code>;</LI>
     * <LI>enters a new date or time in a <code>DateField</code>; and</LI>
     * <LI>{@link Item#notifyStateChanged} was called on an
     * <code>Item</code>.</LI>
     * </UL>
     *
     * <p> It is up to the device to decide when it considers a
     * new value to have been entered into an <code>Item</code>.  For example,
     * implementations of text editing within a <code>TextField</code>
     * vary greatly
     * from device to device. </P>
     *
     * <p>In general, it is not expected that the listener will be called 
     * after every change is made. However, if an item's value
     * has been changed, the listener 
     * will be called to notify the application of the change
     * before it is called for a change on another item, and before a 
     * command is delivered to the <code>Form's</code> 
     * <code>CommandListener</code>. For implementations that have the
     * concept of an input
     * focus, the listener should be called no later than when the focus moves 
     * away from an item whose state has been changed.  The listener
     * should be called only if the item's value has actually been
     * changed.</P>
     *
     * <p> The listener is not called if the application changes
     * the value of an interactive item. </p>
     *
     * @param item the item that was changed
     */
    public void itemStateChanged(Item item);
}
