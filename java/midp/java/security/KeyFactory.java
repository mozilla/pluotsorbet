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

package java.security;

import com.sun.satsa.crypto.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.InvalidKeySpecException;

/**
 * Key factories are used to convert <I>key specifications</I>
 * (transparent representations of the underlying key material) 
 * into <I>keys</I> (opaque cryptographic keys of type <code>Key</code>).
 *
 *
 *
 * @version 1.28, 05/07/02
 *
 * @see Key
 * @see PublicKey
 * @see java.security.spec.KeySpec
 * @see java.security.spec.X509EncodedKeySpec
 *
 * @since 1.2
 */

public class KeyFactory {
    /**
     * Creates a KeyFactory object.
     */
    KeyFactory() {}

    /**
     * Generates a KeyFactory object that implements the specified 
     * algorithm. 
     *
     * @param algorithm the name of the requested key algorithm. 
     * See Appendix A in the 
     * Java Cryptography Architecture API Specification &amp; Reference 
     * for information about standard algorithm names.
     *
     * @return a <code>KeyFactory</code> object for the specified algorithm.
     *
     * @exception NoSuchAlgorithmException if the requested algorithm is
     * not available
     */
    public static KeyFactory getInstance(String algorithm) 
        throws NoSuchAlgorithmException {

        if (algorithm.toUpperCase().equals("RSA")) {
            return new KeyFactory();
        }
        throw new NoSuchAlgorithmException();
    }

    /**
     * Generates a public key object from the provided key specification
     * (key material).
     *
     * @param keySpec the specification (key material) of the public key.
     *
     * @return the public key.
     *
     * @exception InvalidKeySpecException if the given key specification
     * is inappropriate for this key factory to produce a public key.
     */
    public final PublicKey generatePublic(KeySpec keySpec)
        throws InvalidKeySpecException {
        return new RSAPublicKey(keySpec);
    }
}
