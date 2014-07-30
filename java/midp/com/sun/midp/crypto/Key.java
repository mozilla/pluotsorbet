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

package com.sun.midp.crypto;

/**
 * Implements an abstract class that represents all keys (both
 * symmetric and asymmetric).
 */ 
public abstract interface Key {
    /**
     * Returns the name of the algorithm associated with this secret key.
     *
     * @return the secret key algorithm.
     */
    String getAlgorithm();
    
    /**
     * Returns the name of the primary encoding format of this key, or null 
     * if this key does not support encoding. The primary encoding format is 
     * named in terms of the appropriate ASN.1 data format, if an ASN.1 
     * specification for this key exists. For example, the name of the ASN.1 
     * data format for public keys is <I>SubjectPublicKeyInfo</I>, as defined
     * by
     * the X.509 standard; in this case, the returned format is
     * <code>"X.509"</code>.
     * Similarly, the name of the ASN.1 data format for private keys is 
     * <I>PrivateKeyInfo</I>, as defined by the PKCS #8 standard; in this
     * case, 
     * the returned format is <code>"PKCS#8"</code>. 
     *
     * @return the primary encoding format of the key.
     */
    String getFormat();
    
    /**
     * Returns the key in its primary encoding format, or null 
     * if this key does not support encoding. 
     *
     * @return the encoded key, or null if the key does not support encoding.
     */
    byte[] getEncoded();
}
