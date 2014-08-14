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
public interface Contact extends PIMItem {
    // JAVADOC COMMENT ELIDED
    public static final int ADDR = 100;
    // JAVADOC COMMENT ELIDED
    public static final int BIRTHDAY = 101;
    // JAVADOC COMMENT ELIDED
    public static final int CLASS = 102;
    // JAVADOC COMMENT ELIDED
    public static final int EMAIL = 103;
    // JAVADOC COMMENT ELIDED
    public static final int FORMATTED_ADDR = 104;
    // JAVADOC COMMENT ELIDED
    public static final int FORMATTED_NAME = 105;
    // JAVADOC COMMENT ELIDED
    public static final int NAME = 106;
    // JAVADOC COMMENT ELIDED
    public static final int NICKNAME = 107;
    // JAVADOC COMMENT ELIDED
    public static final int NOTE = 108;
    // JAVADOC COMMENT ELIDED
    public static final int ORG = 109;
    // JAVADOC COMMENT ELIDED
    public static final int PHOTO = 110;
    // JAVADOC COMMENT ELIDED
    public static final int PHOTO_URL = 111;
    // JAVADOC COMMENT ELIDED
    public static final int PUBLIC_KEY = 112;
    // JAVADOC COMMENT ELIDED
    public static final int PUBLIC_KEY_STRING = 113;
    // JAVADOC COMMENT ELIDED
    public static final int REVISION = 114;
    // JAVADOC COMMENT ELIDED
    public static final int TEL = 115;
    // JAVADOC COMMENT ELIDED
    public static final int TITLE = 116;
    // JAVADOC COMMENT ELIDED
    public static final int UID = 117;
    // JAVADOC COMMENT ELIDED
    public static final int URL = 118;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_ASST = 1;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_AUTO = 2;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_FAX = 4;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_HOME = 8;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_MOBILE = 16;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_OTHER = 32;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_PAGER = 64;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_PREFERRED = 128;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_SMS = 256;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_WORK = 512;
    // JAVADOC COMMENT ELIDED
    public static final int ADDR_POBOX = 0;
    // JAVADOC COMMENT ELIDED
    public static final int ADDR_EXTRA = 1;
    // JAVADOC COMMENT ELIDED
    public static final int ADDR_STREET = 2;
    // JAVADOC COMMENT ELIDED
    public static final int ADDR_LOCALITY = 3;
    // JAVADOC COMMENT ELIDED
    public static final int ADDR_REGION = 4;
    // JAVADOC COMMENT ELIDED
    public static final int ADDR_POSTALCODE = 5;
    // JAVADOC COMMENT ELIDED
    public static final int ADDR_COUNTRY = 6;
    // JAVADOC COMMENT ELIDED
    public static final int NAME_FAMILY = 0;
    // JAVADOC COMMENT ELIDED
    public static final int NAME_GIVEN = 1;
    // JAVADOC COMMENT ELIDED
    public static final int NAME_OTHER = 2;
    // JAVADOC COMMENT ELIDED
    public static final int NAME_PREFIX = 3;
    // JAVADOC COMMENT ELIDED
    public static final int NAME_SUFFIX = 4;
    // JAVADOC COMMENT ELIDED
    public static final int CLASS_CONFIDENTIAL = 200;
    // JAVADOC COMMENT ELIDED
    public static final int CLASS_PRIVATE = 201;
    // JAVADOC COMMENT ELIDED
    public static final int CLASS_PUBLIC = 202;

    // JAVADOC COMMENT ELIDED
    public int getPreferredIndex(int field);
}
