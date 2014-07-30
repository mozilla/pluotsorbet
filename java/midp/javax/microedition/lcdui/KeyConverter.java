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
 * Utility class that is used to convert key codes from platform independent
 * codes to platform-specific and vice versa.
 */
class KeyConverter {

    /**
     * Return the key code that corresponds to the specified game
     * action on the device.  gameAction must be a defined game action
     * (Canvas.UP, Canvas.DOWN, Canvas.FIRE, etc.)
     * <B>Post-conditions:</B><BR> The key code of the key that
     * corresponds to the specified action is returned.  The return
     * value will be 0 if the game action is invalid or not supported
     * by the device.     
     *
     * @param gameAction The game action to obtain the key code for.
     *
     * @return the key code.
     */
    public static native int getKeyCode(int gameAction);

    /**
     * Returns the game action associated with the given key code on
     * the device.  keyCode must refer to a key that is mapped as a
     * game key on the device. The game action of the key is returned.
     * The return value will be 0 if the key is not mapped to 
     * a game action, or it will be -1 if the keycode is invalid.
     *
     * @param keyCode the key code
     *
     * @return the corresponding game action 
     *         (UP, DOWN, LEFT, RIGHT, FIRE, etc.)
     */
    public static native int getGameAction(int keyCode);

    /**
     * Returns <code>0</code> if keyCode is not a system key.  
     * Otherwise, returns one of the EventConstants.SYSTEM_KEY_ constants.
     *
     * @param keyCode get the system equivalent key.
     *
     * @return translated system key or zero if it is not a system key.
     */
    public static native int getSystemKey(int keyCode);

    /**
     * Gets an informative key string for a key. The string returned
     * should resemble the text physically printed on the key. For
     * example, on a device with function keys F1 through F4, calling
     * this method on the keycode for the F1 key will return the
     * string "F1". A typical use for this string will be to compose
     * help text such as "Press F1 to proceed."
     *
     * <p>There is no direct mapping from game actions to key
     * names. To get the string name for a game action, the
     * application must call
     *
     * <p><code>getKeyName(getKeyCode(GAME_A))</code>
     *
     * @param keyCode the key code being requested
     *
     * @return a string name for the key, or <code>null</code> if no name 
     *         is available
     */
    public static native String getKeyName(int keyCode);
}
