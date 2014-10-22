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

/**
 * This class represents the ASN.1 encoding of a public key,
 * encoded according to the ASN.1 type <code>SubjectPublicKeyInfo</code>.
 * The <code>SubjectPublicKeyInfo</code> syntax is defined in the X.509 
 * standard as follows:
 *
 * <pre>
 * SubjectPublicKeyInfo ::= SEQUENCE {
 *   algorithm AlgorithmIdentifier,
 *   subjectPublicKey BIT STRING }
 * </pre>
 *
 *
 * @version 1.17, 01/23/03
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see EncodedKeySpec
 *
 * @since 1.2
 */

public class X509EncodedKeySpec extends EncodedKeySpec {

    /**
     * Creates a new X509EncodedKeySpec with the given encoded key.
     *
     * @param encodedKey the key, which is assumed to be
     * encoded according to the X.509 standard.
     */
    public X509EncodedKeySpec(byte[] encodedKey) {
	super(encodedKey);
    }

    /**
     * Returns the key bytes, encoded according to the X.509 standard.
     *
     * @return the X.509 encoding of the key.
     */
    public byte[] getEncoded() {
	return super.getEncoded();
    }

    /**
     * Returns the name of the encoding format associated with this
     * key specification.
     *
     * @return the string <code>"X.509"</code>.
     */
    public final String getFormat() {
	return "X.509";
    }
}
