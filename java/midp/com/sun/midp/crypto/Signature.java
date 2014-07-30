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
 * This <code>Signature</code> class is used to provide applications 
 * the functionality
 * of a digital signature algorithm. Digital signatures are used for
 * authentication and integrity assurance of digital data.
 *
 * <p> The signature algorithm can be, among others, the NIST standard
 * DSA, using DSA and SHA-1. The DSA algorithm using the
 * SHA-1 message digest algorithm can be specified as <tt>SHA1withDSA</tt>.
 * In the case of RSA, there are multiple choices for the message digest
 * algorithm, so the signing algorithm could be specified as, for example,
 * <tt>MD2withRSA</tt>, <tt>MD5withRSA</tt>, or <tt>SHA1withRSA</tt>.
 * The algorithm name must be specified, as there is no default.
 *
 * When an algorithm name is specified, the system will
 * determine if there is an implementation of the algorithm requested
 * available in the environment, and if there is more than one, if
 * there is a preferred one.<p>
 *
 * <p>A <code>Signature</code> object can be used to generate and 
 * verify digital signatures.
 *
 * <p>There are three phases to the use of a <code>Signature</code>
 *  object for verifying a signature:<ol>
 *
 * <li>Initialization, with a public key, which initializes the
 * signature for  verification
 * </li>
 *
 * <li>Updating
 *
 * <p>Depending on the type of initialization, this will update the
 * bytes to be verified. </li>
 * <li> Verifying a signature on all updated bytes. </li>
 *
 * </ol>
 */ 
public abstract class Signature {

    /**
     * Protected constructor.
     */ 
    protected Signature() {
    }
    
    /**
     * Generates a <code>Signature</code> object that implements
     * the specified digest
     * algorithm.
     *
     * @param algorithm the standard name of the algorithm requested. 
     * See Appendix A in the 
     * Java Cryptography Architecture API Specification &amp; Reference 
     * for information about standard algorithm names.
     *
     * @return the new <code>Signature</code> object.
     *
     * @exception NoSuchAlgorithmException if the algorithm is
     * not available in the environment.
     */
    public static Signature getInstance(String algorithm)
	throws NoSuchAlgorithmException {

        if (algorithm == null) {
            throw new NoSuchAlgorithmException();
        }

        algorithm = algorithm.toUpperCase();

        try {
            Class sigClass;

            if (algorithm.equals("MD2WITHRSA")) {
                sigClass = Class.forName("com.sun.midp.crypto.RsaMd2Sig");
            } else if (algorithm.equals("MD5WITHRSA")) {
                sigClass = Class.forName("com.sun.midp.crypto.RsaMd5Sig");
            } else if (algorithm.equals("SHA1WITHRSA")) {
                sigClass = Class.forName("com.sun.midp.crypto.RsaShaSig");
            } else {
                throw new NoSuchAlgorithmException();
            }
                
            return (Signature)sigClass.newInstance();
        } catch (Throwable e) {
            throw new NoSuchAlgorithmException("Provider not found");
        }
    }
    
    /** 
     * Gets the signature algorithm.
     * 
     * @return the algorithm code defined above
     */ 
    public abstract String getAlgorithm();
    
    /**
     * Gets the byte length of the signature data.
     * 
     * @return the byte length of signature data
     */ 
    public abstract int getLength();
    
    /**
     * Initializes the <CODE>Signature</CODE> object with the appropriate
     * <CODE>Key</CODE> for signature verification.
     * <P />
     * @param theKey the key object to use for verification
     *
     * @exception InvalidKeyException if the key type is inconsistent 
     * with the mode or signature implementation.
     */
    public abstract void initVerify(PublicKey theKey)
        throws InvalidKeyException;

    /**
     * Initializes the <CODE>Signature</CODE> object with the appropriate
     * <CODE>Key</CODE> for signature creation.
     * <P />
     * @param theKey the key object to use for signing
     *
     * @exception InvalidKeyException if the key type is inconsistent 
     * with the mode or signature implementation.
     */
    public abstract void initSign(PrivateKey theKey)
        throws InvalidKeyException;

    /**
     * Accumulates a signature of the input data. When this method is used,
     * temporary storage of intermediate results is required. This method
     * should only be used if all the input data required for the signature
     * is not available in one byte array. The sign() or verify() method is 
     * recommended whenever possible. 
     * <P />
     * @param inBuf the input buffer of data to be signed
     * @param inOff starting offset within the input buffer for data to
     *              be signed
     * @param inLen the byte length of data to be signed
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly.          
     */ 
    public abstract void update(byte[] inBuf, int inOff, int inLen)
        throws SignatureException;
    
    /**
     * Generates the signature of all/last input data. A call to this
     * method also resets this signature object to the state it was in
     * when previously initialized via a call to initSign() and the
     * message to sign given via a call to update(). 
     * That is, the object is reset and available to sign another message.
     * <P />
     * @param outbuf the output buffer to store signature data
     *
     * @return number of bytes of signature output in sigBuf
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly, or outbuf.length is less than the actual signature
     */ 
    public int sign(byte[] outbuf) throws SignatureException {
        return sign(outbuf, 0, outbuf.length);
    }
    
    /**
     * Generates the signature of all/last input data. A call to this
     * method also resets this signature object to the state it was in
     * when previously initialized via a call to initSign() and the
     * message to sign given via a call to update(). 
     * That is, the object is reset and available to sign another message.
     * <P />
     * @param outbuf the output buffer to store signature data
     * @param offset starting offset within the output buffer at which
     *               to begin signature data
     * @param len    max byte to write to the buffer
     *
     * @return number of bytes of signature output in sigBuf
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly, or len is less than the actual signature
     */
    public abstract int sign(byte[] outbuf, int offset, int len)
        throws SignatureException;
    
    /**
     * Verifies the passed-in signature. 
     * 
     * <p>A call to this method resets this signature object to the state 
     * it was in when previously initialized for verification via a
     * call to <code>initVerify(PublicKey)</code>. That is, the object is 
     * reset and available to verify another signature from the identity
     * whose public key was specified in the call to <code>initVerify</code>.
     *      
     * @param signature the signature bytes to be verified.
     *
     * @return true if the signature was verified, false if not. 
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly, or the passed-in signature is improperly 
     * encoded or of the wrong type, etc.
     */
    public boolean verify(byte[] signature) throws SignatureException {
        return verify(signature, 0, signature.length);
    }

    /**
     * Verifies the passed-in signature. 
     * 
     * <p>A call to this method resets this signature object to the state 
     * it was in when previously initialized for verification via a
     * call to <code>initVerify(PublicKey)</code>. That is, the object is 
     * reset and available to verify another signature from the identity
     * whose public key was specified in the call to <code>initVerify</code>.
     *
     * @param signature the input buffer containing signature data
     * @param offset starting offset within the sigBuf where signature
     *               data begins
     * @param length byte length of signature data
     *
     * @return true if signature verifies, false otherwise
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly, or the passed-in signature is improperly 
     * encoded or of the wrong type, etc.
     */ 
    public abstract boolean verify(byte[] signature, int offset, int length)
        throws SignatureException;
}
