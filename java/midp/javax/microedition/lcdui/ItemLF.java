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
 * Look and Feel interface used by Item.
 * <p>
 * See <a href="doc-files/naming.html">Naming Conventions</a>
 * for information about method naming conventions.
 */
interface ItemLF {
    
    /**
     * Get the preferred width of this Item
     *
     * @param h tentative locked height
     * @return the preferred width
     */
    int lGetPreferredWidth(int h);
    
    /**
     * Get the preferred height of this Item
     * @param w tentative locked width
     * @return the preferred height
     */
    int lGetPreferredHeight(int w);
    
    /**
     * Get the minimum width of this Item
     *
     * @return the minimum width
     */
    int lGetMinimumWidth();
    
    /**
     * Get the minimum height of this Item
     *
     * @return the minimum height
     */
    int lGetMinimumHeight();


    /**
     * Notifies L&F of a label change in the corresponding Item.
     * @param label the new label string
     */
    void lSetLabel(String label);

    /**
     * Notifies L&F of a layout change in the corresponding Item.
     * @param layout the new layout descriptor
     */
    void lSetLayout(int layout);

    /**
     * Notifies L&F of a command addition in the corresponding Item.
     * @param cmd the newly added command
     * @param i the index of the added command in the ChoiceGroup's
     *        commands[] array
     */
    void lAddCommand(Command cmd, int i);

    /**
     * Notifies L&F of a command removal in the corresponding Item.
     * @param cmd the newly removed command
     * @param i the index of the removed command in the ChoiceGroup's
     *        commands[] array
     */
    void lRemoveCommand(Command cmd, int i);

    /**
     * Notifies L&F of a preferred size change in the corresponding Item.
     * @param width the value to which the width is locked, or
     * <code>-1</code> if it is unlocked
     * @param height the value to which the height is locked, or 
     * <code>-1</code> if it is unlocked
     */
    void lSetPreferredSize(int width, int height);

    /**
     * Notifies L&F of the default command change in the corresponding Item.
     * @param cmd the newly set default command
     * @param i index of this new command in the ChoiceGroup's commands array
     */
    void lSetDefaultCommand(Command cmd, int i);

    /**
     * Notify this itemLF that its owner screen has changed.
     *
     * @param oldOwner old owner screen before this change. New owner 
     * 			can be found in Item model.
     */
    public void lSetOwner(Screen oldOwner);

    /**
     * Called to commit any pending user interaction for the current
     * item before an abstract command is fired.
     * Caller should hold LCDUILock around this call.
     */
    void lCommitPendingInteraction();

    /**
     * Return whether the cached requested sizes are valid.
     *
     * @return <code>true</code> if the cached requested sizes are up to date.
     *         <code>false</code> if they have been invalidated.
     */
    boolean isRequestedSizesValid();
}
