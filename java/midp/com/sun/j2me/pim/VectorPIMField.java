/*
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

import java.util.Vector;

/**
 * Encapsulation of a multi-value PIM field
 *
 */
class VectorPIMField implements PIMField {
    /** Array of values. */
    private final Vector values = new Vector();
    /** Array of attributes. */
    private final Vector attributes = new Vector();

    /**
     * Adds a value to a PIM field.
     * @param attributes properties to update
     * @param value entry to be updated
     */
    public void addValue(int attributes, Object value) {
        this.values.addElement(value);
        this.attributes.addElement(new Integer(attributes));
    }

    /**
     * Gets the value of the field.
     * @param index element index
     * @return field value
     */
    public Object getValue(int index) {
        return values.elementAt(index);
    }

    /**
     * Sets the value of the field.
     * @param attributes field attributes requested
     * @param value new value for field
     * @param index element identifier
     */
    public void setValue(int attributes, Object value, int index) {
        this.values.setElementAt(value, index);
        this.attributes.setElementAt(new Integer(attributes), index);
    }

    /**
     * Gets the field attributes.
     * @param index element identifier
     * @return encoded attributes
     */
    public int getAttributes(int index) {
        return ((Integer) attributes.elementAt(index)).intValue();
    }

    /**
     * Checks if the field contains data.
     * @return <code>true</code> if field contains data
     */
    public boolean containsData() {
        return values.size() > 0;
    }

    /**
     * Gets the number of value elements.
     * @return always returns <code>1</code>
     */
    public int getValueCount() {
        return values.size();
    }

    /**
     * Removes a value element.
     * @param index identifier for value to remove
     */
    public void removeValue(int index) {
        this.values.removeElementAt(index);
        this.attributes.removeElementAt(index);
    }

    /**
     * Checks if field has scalar value.
     * @return always returns <code>true</code>
     */
    public boolean isScalar() {
        return false;
    }

}
