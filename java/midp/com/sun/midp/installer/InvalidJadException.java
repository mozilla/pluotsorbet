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

package com.sun.midp.installer;

/**
 * Thrown when a downloaded JAD is discovered to be invalid. This includes
 * this signed JAD's also. Since some of the conditions that cause this
 * exception may be recovered from, this exception contains a specific
 * RC (reason code) that so corrective action can be performed.
 */
public class InvalidJadException extends java.io.IOException {

    /** (1) The server for the JAD was not found. */
    public static final int JAD_SERVER_NOT_FOUND     =  1;

    /** (2) The JAD was not found. */
    public static final int JAD_NOT_FOUND            =  2;

    /** (4) The content provider certificate is missing. */
    public static final int MISSING_PROVIDER_CERT    =  4;

    /** (5) The content provider certificate cannot be decoded. */
    public static final int CORRUPT_PROVIDER_CERT    =  5;

    /**
     * (6) The CA that issued the content provider certificate is unknown.
     * The extra data will be the CA's name as a String.
     */
    public static final int UNKNOWN_CA               =  6;

    /**
     * (7) The signature of the content provider certificate is invalid.
     * The extra data will be the subject's name as a String.
     */
    public static final int INVALID_PROVIDER_CERT    =  7;

    /** (8) The JAR signature cannot be decoded. */
    public static final int CORRUPT_SIGNATURE        =  8;

    /** (9) The signature of the JAR is invalid. */
    public static final int INVALID_SIGNATURE        =  9;

    /** (10) The content provider certificate has an unsupported version. */
    public static final int UNSUPPORTED_CERT         = 10;

    /**
     * (11) The content provider certificate is expired.
     * The extra data will be the subject's name as a String.
     */
    public static final int EXPIRED_PROVIDER_CERT    = 11;

    /**
     * (12) The CA's public key has expired.
     * The extra data will be the CA's name as a String.
     */
    public static final int EXPIRED_CA_KEY           = 12;

    /** (13) The name of MIDlet suite is missing. */
    public static final int MISSING_SUITE_NAME       = 13;

    /** (14) The vendor is missing. */
    public static final int MISSING_VENDOR           = 14;

    /** (15) The version is missing. */
    public static final int MISSING_VERSION          = 15;

    /** (16) The format of the version is invalid. */
    public static final int INVALID_VERSION          = 16;

    /**
     * (17) This suite is older that the one currently installed.
     * The extra data is the installed version.
     */
    public static final int OLD_VERSION              = 17;

    /** (18) The URL for the JAR is missing. */
    public static final int MISSING_JAR_URL          = 18;

    /**
     * (19) The server for the JAR was not found at the URL given in
     * the JAD. The extra data is the JAR URL.
     */
    public static final int JAR_SERVER_NOT_FOUND     = 19;

    /**
     * (20) The JAR was not found at the URL given in the JAD.
     * The extra data is the JAR URL.
     */
    public static final int JAR_NOT_FOUND            = 20;

    /** (21) The JAR size is missing. */
    public static final int MISSING_JAR_SIZE         = 21;

    /**
     * (25) The MIDlet suite name does not match the one in the JAR
     * manifest.
     */
    public static final int SUITE_NAME_MISMATCH      = 25;

    /** (26) The version does not match the one in the JAR manifest. */
    public static final int VERSION_MISMATCH         = 26;

    /** (27) The vendor does not match the one in the JAR manifest. */
    public static final int VENDOR_MISMATCH          = 27;

    /**
     * (28) A key for an attribute is not formatted correctly.
     * The extra data is the key or the line of the attribute.
     */
    public static final int INVALID_KEY              = 28;

    /**
     * (29) A value for an attribute is not formatted correctly.
     * The extra data is the key of the attribute.
     */
    public static final int INVALID_VALUE            = 29;

    /**
     * (30) Not enough storage for this suite to be installed
     * The extra data will be storage needed for the suite in K bytes
     * rounded up.
     */
    public static final int INSUFFICIENT_STORAGE     = 30;

    /** (31) The JAR downloaded was not size in the JAD. */
    public static final int JAR_SIZE_MISMATCH        = 31;

    /**
     * (32) This suite is newer that the one currently installed.
     * The extra data is the installed version.
     */
    public static final int NEW_VERSION              = 32;

    /** (33) Webserver authentication required or failed. */
    public static final int UNAUTHORIZED             = 33;

    /**
     * (34) The JAD URL is for an installed suite but different than the
     * original JAD URL.
     * The extra data will be previous JAD URL.
     */
    public static final int JAD_MOVED                = 34;

    /** (35) Server does not support basic authentication. */
    public static final int CANNOT_AUTH              = 35;

    /**
     * (36) An entry could not be read from the JAR. The extra data is the
     * entry name.
     */
    public static final int CORRUPT_JAR              = 36;

    /**
     * (37) The server did not hava a resource with the correct type
     * (code 406) or the JAD downloaded has the wrong media type. In the
     * second case the extra data is the Media-Type from the response.
     */
    public static final int INVALID_JAD_TYPE         = 37;

    /**
     * (38) The server did not hava a resource with the correct type
     * (code 406) or the JAR downloaded has the wrong media type. In the
     * second case the extra data is the Media-Type from the response.
     */
    public static final int INVALID_JAR_TYPE         = 38;

    /**
     * (39) The JAD matches a version of a suite already installed.
     * The extra data is the installed version.
     */
    public static final int ALREADY_INSTALLED        = 39;

    /**
     * (40) The device does not support either the configuration or
     * profile in the JAD.
     */
    public static final int DEVICE_INCOMPATIBLE      = 40;

    /** (41) The configuration is missing from the manifest. */
    public static final int MISSING_CONFIGURATION    = 41;

    /** (42) The profile is missing from the manifest. */
    public static final int MISSING_PROFILE          = 42;

    /** (43) The JAD URL is invalid. */
    public static final int INVALID_JAD_URL          = 43;

    /** (44) The JAR URL is invalid. The extra data is the JAR URL. */
    public static final int INVALID_JAR_URL          = 44;

    /**
     * (45) The connection in a push entry is already taken.
     * The extra data is the URL of the failed connection.
     */
    public static final int PUSH_DUP_FAILURE         = 45;

    /**
     * (46) The format of a push attribute has an invalid format.
     * The extra data is the URL of the failed connection.
     */
    public static final int PUSH_FORMAT_FAILURE      = 46;

    /**
     * (47) The connection in a push attribute is not supported.
     * The extra data is the URL of the failed connection.
     */
    public static final int PUSH_PROTO_FAILURE       = 47;

    /**
     * (48) The class in a push attribute is not in MIDlet-&lt;n&gt; attribute.
     * The extra data is the URL of the failed connection.
     */
    public static final int PUSH_CLASS_FAILURE       = 48;

    /**
     * (49) Application authorization failure.
     * The extra data is the name of the permission.
     */
    public static final int AUTHORIZATION_FAILURE    = 49;

    /**
     * (50) A attribute in both the JAD and JAR manifest does not match.
     * For trusted suites only. The extra data is the name of the attribute.
     */
    public static final int ATTRIBUTE_MISMATCH       = 50;

    /**
     * (51) Indicates that the user must first authenticate with
     * the proxy.
     */
    public static final int PROXY_AUTH               = 51;

    /**
     * (52) Indicates that the user tried to overwrite a trusted suite
     * with an untrusted suite during an update.
     * The extra data is the name of signer of the current version.
     */
    public static final int TRUSTED_OVERWRITE_FAILURE = 52;

    /**
     * (53) Indicates that either the JAD or manifest has too many properties
     * to fit into memory.
     */
    public static final int TOO_MANY_PROPS = 53;

    /**
     * (54) The MicroEdition-Handler-&lt;n&gt; attribute has invalid
     * values.  The classname may be missing or there are too many fields
     */
    public static final int INVALID_CONTENT_HANDLER = 54;

    /**
     * (55) The installation of a content handler would
     * conflict with an already installed handler.
     */
    public static final int CONTENT_HANDLER_CONFLICT = 55;

    /**
     * (56) Not all classes within JAR package can be successfully
     * verified with class verifier.
     */
    public static final int JAR_CLASSES_VERIFICATION_FAILED = 56;

    /**
     * (57) Indicates that the payment information provided with the MIDlet
     * suite is incompatible with the current implementation.
     */
    public static final int UNSUPPORTED_PAYMENT_INFO = 57;

    /**
     * (58) Indicates that the payment information provided with the MIDlet
     * suite is incomplete or incorrect.
     */
    public static final int INVALID_PAYMENT_INFO = 58;

    /**
     * (59) Indicates that the MIDlet suite has payment provisioning
     * information but it is not trusted.
     */
    public static final int UNTRUSTED_PAYMENT_SUITE = 59;

    /**
     * (60) Indicates that trusted CA for this suite has been disable for
     * software authorization. The extra data contains the CA's name.
     */
    public static final int CA_DISABLED = 60;

    /**
     * (61) Indicates that the character encoding specified in the MIME type
     * is not supported.
     */
    public static final int UNSUPPORTED_CHAR_ENCODING = 61;

    /**
     * (62) The certificate has been revoked.
     * The extra data will be the subject's name as a String.
     */
    public static final int REVOKED_CERT              = 62;

    /**
     * (63) The certificate is unknown to OCSP server.
     * The extra data will be the subject's name as a String.
     */
    public static final int UNKNOWN_CERT_STATUS       = 63;

    /** The reason why this exception occurred. */
    private int reason;

    /** Extra data need to correct the problem. */
    private String extraData = null;

    /**
     * Constructs a InvalidJadException with the specified reason.
     * @param theReason specific RC (reason code)
     */
    public InvalidJadException(int theReason) {
        super("Reason = " + theReason);
        reason = theReason;
    }

    /**
     * Constructs a InvalidJadException with the specified reason and
     * extra data.
     * @param theReason specific reason code
     * @param theExtraData an object that contains data to solve the problem
     */
    public InvalidJadException(int theReason, String theExtraData) {
        this(theReason);
        extraData = theExtraData;
    }

    /**
     * Returns specific reason why the exception was thrown.
     * @return the RC (reason code)
     */
    public int getReason() {
        return reason;
    }

    /**
     * Returns an extra data String to help to resolve the problem.
     * @return extra data string, may be null,
     *           type depends on the reason for the exception
     */
    public String getExtraData() {
        return extraData;
    }
}
