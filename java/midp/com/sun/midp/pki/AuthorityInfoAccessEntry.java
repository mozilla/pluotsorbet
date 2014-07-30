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

package com.sun.midp.pki;

/** Class representing one entry of AuthorityInfoAccess extension. */
public class AuthorityInfoAccessEntry {
    /** DER encoding for id-ad-ocsp: id-pkix 48 1 */
    public static final byte[] ACCESS_METHOD_OCSP = {
        0x2b, 0x06, 0x01, 0x05, 0x05, 0x07, 0x30, 0x01
    };

    /** DER encoding for id-ad-caIssuers: id-pkix 48 2 */
    public static final byte[] ACCESS_METHOD_CA_ISSUERS = {
        0x2b, 0x06, 0x01, 0x05, 0x05, 0x07, 0x30, 0x02
    };

    /** Access method. */
    private byte[] accessMethod = null;
    /** Access location. */
    private String accessLocation = null;

    /**
     * Constructor.
     * @param method access method
     * @param location access location
     */
    public AuthorityInfoAccessEntry(byte[] method, String location) {
        if (method != null) {
            accessMethod = new byte[ method.length];
            System.arraycopy(method, 0, accessMethod, 0, method.length);
        } else {
            accessMethod = null;
        }
        
        accessLocation = location;
    }

    /**
     * Returns access method field of AuthorityInfoAccess extension.
     *
     * @return access method
     */
    public byte[] getAccessMethod() {
        byte[] copyOfAccessMethod = null;
        if (accessMethod != null) {
            copyOfAccessMethod = new byte[accessMethod.length];
            System.arraycopy(accessMethod, 0, copyOfAccessMethod,
                             0, accessMethod.length);
        }
        return copyOfAccessMethod;
    }

    /**
     * Returns access location field of AuthorityInfoAccess extension.
     *
     * @return access location
     */
    public String getAccessLocation() {
        return accessLocation;
    }
}
