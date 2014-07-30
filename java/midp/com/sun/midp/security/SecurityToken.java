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

package com.sun.midp.security;

/**
 * Contains methods to get various security state information of the currently
 * running MIDlet suite.
 */
public final class SecurityToken {

    /** The standard security exception message. */
    public static final String STD_EX_MSG =
        "Application not authorized to access the restricted API";

    /** Enables the first domain be constructed without a domain. */
    private static boolean firstCaller = true;

    /** Permission list. */
    private byte permissions[];

    /**
     * Creates a security domain with a list of permitted actions or no list
     * to indicate all actions. The caller must be have permission for
     * <code>Permissions.MIDP</code> or be the first caller of
     * the method for this instance of the VM.
     * @param securityToken security token of the caller, can be null for
     *                       the first caller
     * @param ApiPermissions for the token
     * @exception SecurityException if caller is not permitted to call this
     *            method
     */
    SecurityToken(SecurityToken securityToken, byte[][] ApiPermissions) {
        if (firstCaller) {
            // The first call is during system initialization.
            firstCaller = false;
        } else {
            securityToken.checkIfPermissionAllowed(Permissions.MIDP);
        }

        permissions = ApiPermissions[Permissions.CUR_LEVELS];
    }

    /**
     * Check to see the suite has the ALLOW level for specific permission.
     * This is used for by internal APIs that only provide access to
     * trusted system applications.
     *
     * @param permission permission ID from com.sun.midp.security.Permissions
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     */
    public void checkIfPermissionAllowed(int permission) {
        checkIfPermissionAllowed(permission, STD_EX_MSG);
    }

    /**
     * Check to see the suite has the ALLOW level for specific permission.
     * This is used for by internal APIs that only provide access to
     * trusted system applications.
     *
     * @param permission permission String from com.sun.midp.security.Permissions
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     */
    public void checkIfPermissionAllowed(String permission) {
        checkIfPermissionAllowed(Permissions.getId(permission), STD_EX_MSG);
    }

    /**
     * Check to see the suite has the ALLOW level for specific permission.
     * This is used for by internal APIs that only provide access to
     * trusted system applications.
     *
     * @param permission permission ID from com.sun.midp.security.Permissions
     * @param exceptionMsg message if a security exception is thrown
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     */
    private void checkIfPermissionAllowed(int permission, String exceptionMsg) {
        if (permissions == null) {
            /* totally trusted, all permission allowed */
            return;
        }
        if (permission >= 0 && permission < permissions.length &&
            (permissions[permission] == Permissions.ALLOW)) {
            return;
        }

        // this method do not ask the user
        throw new SecurityException(exceptionMsg);
    }
}
