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
 * This utility class is used to show & hide native system menus.
 * Is not needed in Chameleon (needed in platform_widgets). 
 * shall be used only by Display.
 */
class NativeMenu {

    /**
     * Set the current set of active Abstract Commands.
     * 
     * @param itemCommands The list of Item Commands that should be active
     * @param numItemCommands The number of Item commands in the list
     * @param commands The list of Commands that should be active
     * @param numCommands The number of commands in the list
     */
    public static native void updateCommands(
        Command[] itemCommands, int numItemCommands,
        Command[] commands, int numCommands);

    /**
     * Called to show system menu on the screen
     */
    public static void show() {
        showMenu();
        inMenu = true;
    }

    /**
     * Called to force the display manager to clear whatever system screen
     * has interrupted the current Displayable and allow the foreground
     * Display to resume painting.
     */
    public static void dismiss() {
        inMenu = false;
        dismissMenuAndPopup();
    }

    /**
     * Returns current status of the system menu.
     * Intended to be used by displayEventConsumer.handlecommandEvent() 
     * (implemented by Display.DisplayAccessor).
     *
     * @return true if menu is active, false otherwise.
     */
    public static boolean getState() {
        return inMenu;
    }
   
    /**
     * Clear state of system menu without physical dismiss.
     * Intended to be used by displayEventConsumer.handlecommandEvent() 
     * (implemented by Display.DisplayAccessor).
     */
    public static void clearState() {
        inMenu = false;
    }
   
    /**
     * Native method to show the command menu on the screen
     */
    private static native void showMenu();

    /**
     * Native method to dismiss the current menu or popup 
     * in the case of setCurrent()
     * being called while the Display is suspended by a system screen.
     */
    private static native void dismissMenuAndPopup();

    /** True if the foreground display is the menu screen. */
    private static boolean inMenu = false;

}        

