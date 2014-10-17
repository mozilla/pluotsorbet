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

package java.security.spec;

import com.sun.j2me.crypto.Util;

/**
 * This class represents a public key in encoded format.
 *
 *
 * @version 1.18, 01/23/03
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see X509EncodedKeySpec
 *
 * @since 1.2
 */

public abstract class EncodedKeySpec implements KeySpec {
    /** The encoded key to use in the signature processing. */
    private byte[] encodedKey;

    /**
     * Creates a new EncodedKeySpec with the given encoded key.
     *
     * @param encodedKey the encoded key.
     */
    public EncodedKeySpec(byte[] encodedKey) {

	this.encodedKey = Util.cloneArray(encodedKey);
    }

    /**
     * Returns the encoded key.
     *
     * @return the encoded key.
     */
    public byte[] getEncoded() {

	return Util.cloneArray(encodedKey);
    }

    /**
     * Returns the name of the encoding format associated with this
     * key specification.
     *
     * <p>If the opaque representation of a key
     * (see {@link java.security.Key Key}) can be generated
     * (see {@link java.security.KeyFactory KeyFactory})
     * from this key specification (or a subclass of it),
     * <code>getFormat</code> called
     * on the opaque key returns the same value as the
     * <code>getFormat</code> method
     * of this key specification.
     *
     * @return a string representation of the encoding format.
     */
    public abstract String getFormat();
}
