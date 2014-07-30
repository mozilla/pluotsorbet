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

package com.sun.midp.pki.ocsp;

import java.lang.String;

/**
 * The <CODE>OCSPException</CODE> encapsulates an error that
 * was indicated in the responce received from OCSP Responder.
 */
public class OCSPException extends Exception {

    /** The reason code for this exception */
    private byte reason;

    /** The reason code for this exception */
    private String errorMessage;

    /**
     * Indicates that OCSP request doesn't conform to the OCSP syntax.
     */
    public static final byte MALFORMED_REQUEST = 1;

    /**
     * Indicates that OCSP responder reached an inconsistent internal state.
     */
    public static final byte INTERNAL_ERROR    = 2;

    /**
     * Indicates that the service exists, but is temporarily unable to respond.
     */
    public static final byte TRY_LATER         = 3;

    /**
     * Indicates that the server requires the client sign the request.
     */
    public static final byte SIG_REQUIRED      = 5;

    /**
     * Indicates that the client is not authorized to make this query.
     */
    public static final byte UNAUTHORIZED      = 6;

    /**
     * Indicates that the connection to OCSP server could not be opened.
     */
    public static final byte SERVER_NOT_FOUND  = 7;

    /**
     * Indicates that an error occured when connecting
     * to the OCSP server.
     */
    public static final byte CANNOT_OPEN_CONNECTION  = 8;

    /**
     * Indicates that an error occured when sending
     * a request to the OCSP server.
     */
    public static final byte CANNOT_SEND_REQUEST  = 9;

    /**
     * Indicates that an error occured when receiving
     * a response from the OCSP server.
     */
    public static final byte CANNOT_RECEIVE_RESPONSE = 10;

    /**
     * Indicates that the OCSP server did not respond.
     */
    public static final byte SERVER_NOT_RESPONDING  = 11;

    /**
     * Indicates that the signature of the OCSP responder
     * can't be verified.
     */
    public static final byte CANNOT_VERIFY_SIGNATURE = 12;

    /**
     * Indicates that the signature of the OCSP responder
     * can't be verified.
     */
    public static final byte INVALID_RESPONDER_CERTIFICATE = 13;

    /**
     * Indicates that some unexpected error has occured.
     */
    public static final byte UNKNOWN_ERROR            = 14;

    /**
     * Create a new exception with a specific error reason.
     * The descriptive message for the new exception will be
     * automatically provided, based on the reason.
     *
     * @param status the reason for the exception
     */
    public OCSPException(byte status) {
        reason = status;
        errorMessage = null;
    }

    /**
     * Create a new exception with a specific error reason.
     * The descriptive message for the new exception will be
     * automatically provided, based on the reason.
     *
     * @param status the reason for the exception
     * @param message error message, may be null
     */
    public OCSPException(byte status, String message) {
        reason = status;
        errorMessage = message;
    }

    /**
     * Get the reason code.
     * @return the reason code
     */
    public byte getReason() {
        return reason;
    }

    /**
     * Get the error message.
     * @return the error message or empty string if it was not given
     */
    public String getErrorMessage() {
        return (errorMessage == null) ? "" : errorMessage;
    }

    /**
     * Gets the exception message for a reason.
     *
     * @param reason reason code
     *
     * @return exception message
     */
    static String getMessageForReason(int reason) {
        switch (reason) {
            case MALFORMED_REQUEST: {
                return "OCSP request doesn't conform to the OCSP syntax";
            }
            case INTERNAL_ERROR: {
                return "Internal error";
            }
            case TRY_LATER: {
                return "Try again later";
            }
            case SIG_REQUIRED: {
                return "Request must be signed";
            }
            case UNAUTHORIZED: {
                return "Request is unauthorized";
            }
        }

        return "Unknown reason (" + reason + ")";
    }
}
