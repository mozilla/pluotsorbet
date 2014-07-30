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

package javax.microedition.pki;

import javax.microedition.pki.Certificate;

import java.lang.String;

/**
 * The <CODE>CertificateException</CODE> encapsulates an error that
 * occurred while a <CODE>Certificate</CODE> is being used.  If multiple errors
 * are found within a <CODE>Certificate</CODE> the more significant error
 * should be reported in the exception. 
 */
public class CertificateException extends java.io.IOException {

    /** The reason code for this exception */
    private byte reason;

    /**
     * The certificate that caused the exception
     */
    private Certificate cert;

    /**
     * Indicates a certificate has unrecognized critical extensions.
     * The value is 1.
     */
    public static final byte BAD_EXTENSIONS = 1;

    /** 
     * Indicates the server certificate chain exceeds the length allowed
     * by an issuer's policy.
     * The value is 2.
     */
    public static final byte CERTIFICATE_CHAIN_TOO_LONG = 2;

    /**
     * Indicates a certificate is expired.
     * The value is 3.
     */
    public static final byte EXPIRED = 3;

    /**
     * Indicates an intermediate certificate in the chain does not have the
     * authority to be a intermediate CA. The value is 4.
     */
    public static final byte UNAUTHORIZED_INTERMEDIATE_CA = 4;

    /**
     * Indicates a certificate object does not contain a signature.
     * The value is 5.
     */
    public static final byte MISSING_SIGNATURE = 5;

    /**
     * Indicates a certificate is not yet valid.
     * The value is 6.
     */
    public static final byte NOT_YET_VALID  = 6;

    /**
     * Indicates a certificate does not contain the correct site name.
     * The value is 7.
     */
    public static final byte SITENAME_MISMATCH  = 7;

    /**
     * Indicates a certificate was issued by an unrecognized entity.
     * The value is 8.
     */
    public static final byte UNRECOGNIZED_ISSUER = 8;

    /**
     * Indicates a certificate was signed using an unsupported algorithm.
     * The value is 9.
     */
    public static final byte UNSUPPORTED_SIGALG  = 9;

    /**
     * Indicates a certificate public key has been used in way deemed
     * inappropriate by the issuer. The value is 10.
     */
    public static final byte INAPPROPRIATE_KEY_USAGE = 10;

    /**
     * Indicates a certificate in a chain was not issued by the next
     * authority in the chain. The value is 11.
     */
    public static final byte BROKEN_CHAIN = 11;

    /**
     * Indicates the root CA's public key is expired. The value is 12.
     */
    public static final byte ROOT_CA_EXPIRED = 12;

    /**
     * Indicates that type of the public key in a certificate is not
     * supported by the device. The value is 13.
     */
    public static final byte UNSUPPORTED_PUBLIC_KEY_TYPE = 13;

    /**
     * Indicates a certificate failed verification.
     * The value is 14.
     */
    public static final byte VERIFICATION_FAILED  = 14;

    /**
     * Create a new exception with a <CODE>Certificate</CODE>
     * and specific error reason. The descriptive message for the new exception
     * will be automatically provided, based on the reason.
     * @param certificate the certificate that caused the exception
     * @param status the reason for the exception;
     *  the status MUST be between BAD_EXTENSIONS and VERIFICATION_FAILED
     *  inclusive.
     */
    public CertificateException(Certificate certificate, byte status) {
        super(getMessageForReason(status));
	cert = certificate;
	reason = status;
    }

    /**
     * Create a new exception with a message, <CODE>Certificate</CODE>,
     * and specific error reason.
     * @param message a descriptive message
     * @param certificate the certificate that caused the exception
     * @param status the reason for the exception;
     *  the status MUST be between BAD_EXTENSIONS and VERIFICATION_FAILED
     *  inclusive.
     */
    public CertificateException(String message, Certificate certificate, 
				byte status) {
	super(message);
	cert = certificate;
	reason = status;
    }

    /**
     * Get the <CODE>Certificate</CODE> that caused the exception.
     * @return the <CODE>Certificate</CODE> that included the failure.
     */
    public Certificate getCertificate() {
	return cert;
    }

    /**
     * Get the reason code.
     * @return the reason code
     */
    public byte getReason() {
	return reason;
    }

    // package private methods //

    /**
     * Gets the exception message for a reason.
     *
     * @param reason reason code
     *
     * @return exception message
     */
    static String getMessageForReason(int reason) {
        switch (reason) {
        case BAD_EXTENSIONS:
            return "Certificate has unrecognized critical extensions";

        case CERTIFICATE_CHAIN_TOO_LONG:
            return "Server certificate chain exceeds the length allowed " +
                "by an issuer's policy";

        case EXPIRED:
            return "Certificate is expired";

        case UNAUTHORIZED_INTERMEDIATE_CA:
            return "Intermediate certificate in the chain does not have the " +
                "authority to be an intermediate CA";

        case MISSING_SIGNATURE:
            return "Certificate object does not contain a signature";

        case NOT_YET_VALID:
            return "Certificate is not yet valid";


        case SITENAME_MISMATCH:
            return "Certificate does not contain the correct site name";

        case UNRECOGNIZED_ISSUER:
            return "Certificate was issued by an unrecognized entity";

        case UNSUPPORTED_SIGALG:
            return "Certificate was signed using an unsupported algorithm";

        case INAPPROPRIATE_KEY_USAGE:
            return "Certificate's public key has been used in a way deemed " +
                "inappropriate by the issuer";

        case BROKEN_CHAIN:
            return "Certificate in a chain was not issued by the next " +
                "authority in the chain";

        case ROOT_CA_EXPIRED:
            return "Root CA's public key is expired";

        case UNSUPPORTED_PUBLIC_KEY_TYPE:
            return "Certificate has a public key that is not a " +
                "supported type";

        case VERIFICATION_FAILED:
            return "Certificate failed verification";
        }

        return "Unknown reason (" + reason + ")";
    }
}
