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
package com.sun.j2me.security;

import com.sun.midp.security.SecurityToken;

public final class Token {

    private SecurityToken securityToken;

    public Token(SecurityToken securityToken) {
        this.securityToken = securityToken;
    }

    public SecurityToken getSecurityToken() {
        return securityToken;
    }

    /**
     * Check to see the suite has the ALLOW level for specific permission.
     * This is used by internal APIs that only provide access to
     * trusted system applications.
     *
     * @param permission permission to be checked
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     */
    public void checkIfPermissionAllowed(Permission permission) {
        securityToken.checkIfPermissionAllowed(permission.getName());
    }
}
