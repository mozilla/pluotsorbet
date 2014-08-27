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
public interface Event extends PIMItem {
    // JAVADOC COMMENT ELIDED
    public static final int ALARM = 100;
    // JAVADOC COMMENT ELIDED
    public static final int CLASS = 101;
    // JAVADOC COMMENT ELIDED
    public static final int END = 102;
    // JAVADOC COMMENT ELIDED
    public static final int LOCATION = 103;
    // JAVADOC COMMENT ELIDED
    public static final int NOTE = 104;
    // JAVADOC COMMENT ELIDED
    public static final int REVISION = 105;
    // JAVADOC COMMENT ELIDED
    public static final int START = 106;
    // JAVADOC COMMENT ELIDED
    public static final int SUMMARY = 107;
    // JAVADOC COMMENT ELIDED
    public static final int UID = 108;
    // JAVADOC COMMENT ELIDED
    public static final int CLASS_CONFIDENTIAL = 200;
    // JAVADOC COMMENT ELIDED
    public static final int CLASS_PRIVATE = 201;
    // JAVADOC COMMENT ELIDED
    public static final int CLASS_PUBLIC = 202;

    // JAVADOC COMMENT ELIDED
    public abstract RepeatRule getRepeat();

    // JAVADOC COMMENT ELIDED
    public abstract void setRepeat(RepeatRule value);
}
