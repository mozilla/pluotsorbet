/*
 *
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*
 * (c) Copyright 2001, 2002 Motorola, Inc.  ALL RIGHTS RESERVED.
 */
package javax.obex;

/*
 * This class is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED 
public class ResponseCodes {

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_OK = 0xA0;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_CREATED = 0xA1;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_ACCEPTED = 0xA2;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_NOT_AUTHORITATIVE = 0xA3;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_NO_CONTENT = 0xA4;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_RESET = 0xA5;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_PARTIAL = 0xA6;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_MULT_CHOICE = 0xB0;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_MOVED_PERM = 0xB1;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_MOVED_TEMP = 0xB2;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_SEE_OTHER = 0xB3;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_NOT_MODIFIED = 0xB4;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_USE_PROXY = 0xB5;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_BAD_REQUEST = 0xC0;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_UNAUTHORIZED = 0xC1;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_PAYMENT_REQUIRED = 0xC2;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_FORBIDDEN = 0xC3;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_NOT_FOUND = 0xC4;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_BAD_METHOD = 0xC5;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_NOT_ACCEPTABLE = 0xC6;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_PROXY_AUTH = 0xC7;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_TIMEOUT = 0xC8;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_CONFLICT = 0xC9;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_GONE = 0xCA;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_LENGTH_REQUIRED = 0xCB;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_PRECON_FAILED = 0xCC;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_ENTITY_TOO_LARGE = 0xCD;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_REQ_TOO_LARGE = 0xCE;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_UNSUPPORTED_TYPE = 0xCF;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_INTERNAL_ERROR = 0xD0;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_NOT_IMPLEMENTED = 0xD1;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_BAD_GATEWAY = 0xD2;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_UNAVAILABLE = 0xD3;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_GATEWAY_TIMEOUT = 0xD4;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_HTTP_VERSION = 0xD5;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_DATABASE_FULL = 0xE0;

    // JAVADOC COMMENT ELIDED 
    public static final int OBEX_DATABASE_LOCKED = 0xE1;

    // JAVADOC COMMENT ELIDED 
    private ResponseCodes() {}
}
