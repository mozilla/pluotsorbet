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
package com.sun.jsr211.security;

import com.sun.j2me.security.TrustedClass;
import com.sun.j2me.security.Token;
import com.sun.midp.security.SecurityToken;

/**
 * A utility class that initializes internal security token for 
 * JSR 211 implemenation classes. Modify this class instead of
 * com.sun.midp.security.SecurityInitializer each time another 
 * JSR 211 implementation class requires initializing security token.
 */
public final class SecurityInitializer {
    /**
     * Internal implementation of the JSR security initializer
     * redispatching security token requested from the core
     * security initializer
     */
    private static SecurityToken internalSecurityToken =
        com.sun.midp.security.SecurityInitializer.requestToken(null);

    /**
     * Hand out internal security token to trusted requesters
     *
     * @param trusted object to check whether token can be given to caller
     * @return if the object is really trusted to requested
     */
    final public static Token requestToken(TrustedClass trusted) {
        return new Token(internalSecurityToken);
    }
}
