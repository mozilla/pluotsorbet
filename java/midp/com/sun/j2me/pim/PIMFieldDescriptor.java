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

import com.sun.j2me.main.Configuration;

/**
 * Specification of a PIM field.
 *
 */
public class PIMFieldDescriptor {

    /** PIM: field. */
    private int field;
    /** PIM: type. */
    private int dataType;
    /** PIM: is the default value present? */
    private boolean hasDefaultValue;
    /** PIM: default value. */
    private Object defaultValue;
    /** PIM: label. */
    private String label;
    /** PIM: label resources. */
    String[] labelResources;
    /** PIM: attributes. */
    private long attributes;
    /** PIM: maximum number of values or -1 for unlimited data. */
    private int maxValues;

    /**
     * Constructor: field initialization.
     *
     * @param field           ID
     * @param dataType        type
     * @param hasDefaultValue is the default value present?
     * @param defaultValue    default value of the field
     * @param labelResource   label
     * @param labelResources  array of label resources
     * @param attributes      field attributes
     * @param maxValues       maximum number of values or -1
     */
    public PIMFieldDescriptor(int field,
        int dataType,
        boolean hasDefaultValue,
        Object defaultValue,
        String labelResource,
        String[] labelResources,
        long attributes,
        int maxValues) {

        this.field = field;
        this.dataType = dataType;
        this.hasDefaultValue = hasDefaultValue;
        this.defaultValue = defaultValue;
        this.label = Configuration.getProperty(labelResource);
        if (this.label == null) {
            this.label = "Label_" + labelResource;
        }
        this.labelResources = labelResources;
        this.attributes = attributes;
        this.maxValues = maxValues;
    }

    /**
     * Constructor: field initialization.
     *
     * @param field           ID
     * @param dataType        type
     * @param hasDefaultValue is the default value present?
     * @param defaultValue    default value of the field
     * @param labelResource   label (labelResources = null)
     * @param attributes      field attributes
     * @param maxValues       maximum number of values or -1
     */
    public PIMFieldDescriptor(int field,
        int dataType,
        boolean hasDefaultValue,
        Object defaultValue,
        String labelResource,
        long attributes,
        int maxValues) {

        this(field, dataType, hasDefaultValue, defaultValue,
            labelResource, null, attributes, maxValues);
    }

    /**
     * Gets field ID.
     *
     * @return the field ID
     */
    public int getField() {
        return field;
    }

    /**
     * Gets field type.
     *
     * @return the field type
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * Gets field label.
     *
     * @return the field label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Checks if the field has a default value?
     *
     * @return true if the field has a default value?
     */
    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    /**
     * Gets the default value.
     *
     * @return the default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the length of array (type STRING_ARRAY).
     *
     * @return the length of array
     */
    public int getStringArraySize() {
        return labelResources.length;
    }

    /**
     * Gets the label.
     *
     * @param arrayElement   index of label in labelResources array
     * @return the label
     */
    public String getElementlabel(int arrayElement) {
        String ret = Configuration.getProperty(labelResources[arrayElement]);
        return ret == null ? "Label_" + labelResources[arrayElement] : ret;
    }

    /**
     * Gets the supported attributes.
     *
     * @return the set of supported attributes
     */
    public long getSupportedAttributes() {
        return attributes;
    }

    /**
     * Gets the maximum values.
     *
     * @return the maximum values
     */
    public int getMaximumValues() {
        return maxValues;
    }
}
