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
 * Copyright (C) 2002-2003 PalmSource, Inc.  All Rights Reserved.
 */

package javax.microedition.pim;


/**
 * This class is defined by the JSR-75 specification
 * <em>PDA Optional Packages for the J2ME&trade; Platform</em>
 */
// JAVADOC COMMENT ELIDED
public class PIMException extends java.lang.Exception {
    // JAVADOC COMMENT ELIDED
    public static final int FEATURE_NOT_SUPPORTED = 0;
    // JAVADOC COMMENT ELIDED
    public static final int GENERAL_ERROR = 1;
    // JAVADOC COMMENT ELIDED
    public static final int LIST_CLOSED = 2;
    // JAVADOC COMMENT ELIDED
    public static final int LIST_NOT_ACCESSIBLE = 3;
    // JAVADOC COMMENT ELIDED
    public static final int MAX_CATEGORIES_EXCEEDED = 4;
    // JAVADOC COMMENT ELIDED
    public static final int UNSUPPORTED_VERSION = 5;
    // JAVADOC COMMENT ELIDED
    public static final int UPDATE_ERROR = 6;
    /** Reason for current exception. */
    private int exception_reason;

    // JAVADOC COMMENT ELIDED
    public PIMException() {
        super();
        exception_reason = GENERAL_ERROR;
    }

    // JAVADOC COMMENT ELIDED
    public PIMException(String detailMessage) {
        super(detailMessage);
        exception_reason = GENERAL_ERROR;
    }

    // JAVADOC COMMENT ELIDED
    public PIMException(String detailMessage, int reason) {
        super(detailMessage);
        exception_reason = reason;
    }

    // JAVADOC COMMENT ELIDED
    public int getReason() {
        return exception_reason;
    };
}
