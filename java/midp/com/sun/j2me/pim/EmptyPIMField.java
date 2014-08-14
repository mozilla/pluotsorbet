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

package com.sun.j2me.pim;

/**
 * Encapsulation of an empty PIM field
 *
 */

class EmptyPIMField implements PIMField {
    /**
     * Adds a value to a PIM field.
     * @param attributes properties to update
     * @param value entry to be updated
     */
    public void addValue(int attributes, Object value) {
    }

    /**
     * Gets current value.
     * @param index field identifier
     * @return requested field
     */
    public Object getValue(int index) {
        return null;
    }

    /**
     * Sets the field value.
     * @param attributes field properties
     * @param value field to update
     * @param index field offset
     */
    public void setValue(int attributes, Object value, int index) {
    }

    /**
     * Gets the field attributes.
     * @param index field offset
     * @return coded attribute settings
     */
    public int getAttributes(int index) {
        throw new IndexOutOfBoundsException("No data in field");
    }

    /**
     * Checks field for contents.
     * @return <code>true</code> if contains data
     */
    public boolean containsData() {
        return false;
    }

    /**
     * Gets the count of values.
     * @return count
     */
    public int getValueCount() {
        return 0;
    }

    /**
     * Removes the value.
     * @param index value offset
     */
    public void removeValue(int index) {
    }
    
    /**
     * Check if field contains scalar value.
     * @return <code>true</code> if scalar value
     */
    public boolean isScalar() {
        return true;
    }
    
}
