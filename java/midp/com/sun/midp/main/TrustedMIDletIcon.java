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

package com.sun.midp.main;

import javax.microedition.lcdui.Image;

import com.sun.j2me.security.AccessController;

import com.sun.midp.util.ResourceHandler;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.SecurityInitializer;
import com.sun.midp.security.ImplicitlyTrustedClass;


/**
 * This holds the trusted MIDlet icon.
 */
public class TrustedMIDletIcon {
    /** The trusted icon. */
    private static Image trustedIcon;

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /** Security token to allow access to implementation APIs */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /**
     * Get the Image of the trusted icon for this Display.
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * @return an Image of the trusted icon.
     *
     * @exception SecurityException if the suite calling does not have the
     * the AMS permission
     */
    public static Image getIcon() {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        if (trustedIcon == null) {
            byte[] imageData = ResourceHandler.getSystemImageResource(
                    classSecurityToken, "trustedmidlet_icon");
            if (imageData != null) {
                trustedIcon = Image.createImage(imageData, 0,
                                                imageData.length);
            } else {
                // Use a empty immutable image as placeholder
                trustedIcon = Image.createImage(Image.createImage(16, 16));
            }
        }

        return trustedIcon;
    }
}






