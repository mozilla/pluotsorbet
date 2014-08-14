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

import java.lang.String;

/**
 * This class is defined by the JSR-75 specification
 * <em>PDA Optional Packages for the J2ME&trade; Platform</em>
 */
// JAVADOC COMMENT ELIDED
public interface PIMItem {
    // JAVADOC COMMENT ELIDED
    public static final int BINARY = 0;
    // JAVADOC COMMENT ELIDED
    public static final int BOOLEAN = 1;
    // JAVADOC COMMENT ELIDED
    public static final int DATE = 2;
    // JAVADOC COMMENT ELIDED
    public static final int INT = 3;
    // JAVADOC COMMENT ELIDED
    public static final int STRING = 4;
    // JAVADOC COMMENT ELIDED
    public static final int STRING_ARRAY = 5;
    // JAVADOC COMMENT ELIDED
    public static final int ATTR_NONE = 0;
    // JAVADOC COMMENT ELIDED
    public static final int EXTENDED_FIELD_MIN_VALUE = 0x1000000;
    // JAVADOC COMMENT ELIDED
    public static final int EXTENDED_ATTRIBUTE_MIN_VALUE = 0x1000000;

    // JAVADOC COMMENT ELIDED
    public abstract PIMList getPIMList();

    // JAVADOC COMMENT ELIDED
    public abstract void commit() throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract boolean isModified();

    // JAVADOC COMMENT ELIDED
    public abstract int[] getFields();

    // JAVADOC COMMENT ELIDED
    public abstract byte[] getBinary(int field, int index);

    // JAVADOC COMMENT ELIDED
    public abstract void addBinary(int field, int attributes, byte[] value,
                                    int offset, int length);

    // JAVADOC COMMENT ELIDED
    public abstract void setBinary(int field, int index, int attributes,
                                    byte[] value, int offset, int length);

    // JAVADOC COMMENT ELIDED
    public abstract long getDate(int field, int index);

    // JAVADOC COMMENT ELIDED
    public abstract void addDate(int field, int attributes, long value);

    // JAVADOC COMMENT ELIDED
    public abstract void setDate(int field, int index,
                                 int attributes, long value);

    // JAVADOC COMMENT ELIDED
    public abstract int getInt(int field, int index);

    // JAVADOC COMMENT ELIDED
    public abstract void addInt(int field, int attributes, int value);

    // JAVADOC COMMENT ELIDED
    public abstract void setInt(int field, int index, int attributes,
				int value);

    // JAVADOC COMMENT ELIDED
    public abstract String getString(int field, int index);

    // JAVADOC COMMENT ELIDED
    public abstract void addString(int field, int attributes, String value);

    // JAVADOC COMMENT ELIDED
    public abstract void setString(int field, int index,
                                   int attributes, String value);

    // JAVADOC COMMENT ELIDED
    public abstract boolean getBoolean(int field, int index);

    // JAVADOC COMMENT ELIDED
    public abstract void addBoolean(int field, int attributes, boolean value);

    // JAVADOC COMMENT ELIDED
    public abstract void setBoolean(int field, int index,
                                    int attributes, boolean value);

    // JAVADOC COMMENT ELIDED
    public abstract String[] getStringArray(int field, int index);

    // JAVADOC COMMENT ELIDED
    public abstract void addStringArray(int field, int attributes,
					String[] value);

    // JAVADOC COMMENT ELIDED
    public abstract void setStringArray(int field, int index,
                                        int attributes, String[] value);

    // JAVADOC COMMENT ELIDED
    public abstract int countValues(int field);

    // JAVADOC COMMENT ELIDED
    public abstract void removeValue(int field, int index);

    // JAVADOC COMMENT ELIDED
    public abstract int getAttributes(int field, int index);

    // JAVADOC COMMENT ELIDED
    public abstract void addToCategory(String category) throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract void removeFromCategory(String category);
    
    // JAVADOC COMMENT ELIDED
    public abstract String[] getCategories();

    // JAVADOC COMMENT ELIDED
    public abstract int maxCategories();
}
