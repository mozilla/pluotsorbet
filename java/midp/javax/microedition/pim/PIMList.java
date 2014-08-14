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
import java.util.Enumeration;

/**
 * This class is defined by the JSR-75 specification
 * <em>PDA Optional Packages for the J2ME&trade; Platform</em>
 */
// JAVADOC COMMENT ELIDED
public interface PIMList {
    // JAVADOC COMMENT ELIDED
    public static final String UNCATEGORIZED = null;

    // JAVADOC COMMENT ELIDED
    public abstract String getName();

    // JAVADOC COMMENT ELIDED
    public abstract void close() throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract Enumeration items() throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract Enumeration items(PIMItem matchingItem)
          throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract Enumeration items(String matchingValue) throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract Enumeration itemsByCategory(String category)
        throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract String[] getCategories() throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract boolean isCategory(String category) throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract void addCategory(String category) throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract void deleteCategory(String category,
					boolean deleteUnassignedItems)
             throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract void renameCategory(String currentCategory,
					String newCategory)
        throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract int maxCategories();

    // JAVADOC COMMENT ELIDED
    public abstract boolean isSupportedField(int field);

    // JAVADOC COMMENT ELIDED
    public abstract int[] getSupportedFields();

    // JAVADOC COMMENT ELIDED
    public abstract boolean isSupportedAttribute(int field, int attribute);

    // JAVADOC COMMENT ELIDED
    public abstract int[] getSupportedAttributes(int field);

    // JAVADOC COMMENT ELIDED
    public abstract boolean isSupportedArrayElement(int stringArrayField,
                                                    int arrayElement);

    // JAVADOC COMMENT ELIDED
    public abstract int[] getSupportedArrayElements(int stringArrayField);

    // JAVADOC COMMENT ELIDED
    public abstract int getFieldDataType(int field);

    // JAVADOC COMMENT ELIDED
    public abstract String getFieldLabel(int field);

    // JAVADOC COMMENT ELIDED
    public abstract String getAttributeLabel(int attribute);

    // JAVADOC COMMENT ELIDED
    public abstract String getArrayElementLabel(int stringArrayField,
						int arrayElement);

    // JAVADOC COMMENT ELIDED
    public abstract int maxValues(int field);

    // JAVADOC COMMENT ELIDED
    public abstract int stringArraySize(int stringArrayField);
}
