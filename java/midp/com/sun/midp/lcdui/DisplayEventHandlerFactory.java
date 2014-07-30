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

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Display;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

/**
 * This class works around the fact that public classes can not
 * be added to a javax package by an implementation.
 */
public class DisplayEventHandlerFactory {
    /** The real implementation of the display event handler. */
    private static DisplayEventHandler managerImpl;

    /**
     * Set the implementation of the display manager, if one
     * is not already set.
     * <p>
     * This implementation class will be in the as the Display class for
     * security. But needs placed here to be visible to com.sun.midp classes.
     *
     * @param dm reference to the system display manager
     */
    public static void SetDisplayEventHandlerImpl(DisplayEventHandler dm) {
        if (managerImpl != null) {
            return;
        }

        managerImpl = dm;
    };

    /**
     * Return a reference to the singleton display manager object.
     *
     * @param token security token with the MIDP permission "allowed"
     *
     * @return display manager reference.
     */
    public static DisplayEventHandler
            getDisplayEventHandler(SecurityToken token) {

        token.checkIfPermissionAllowed(Permissions.MIDP);

        if (managerImpl != null) {
            return managerImpl;
        }

        /**
         * The display manager implementation is a private class of Display
         * and is create in the class init of Display, we need to call a
         * static method of display to get the class init to run, because
         * some classes need to get the display manager to create a display
         */
        try {
            // this will yield a null pointer exception on purpose
            Display.getDisplay(null);
        } catch (NullPointerException npe) {
            // this is normal for this case, do nothing
        }

        return managerImpl;
    };
}
