/*
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
import java.util.Hashtable;
import java.util.Enumeration;

import javax.microedition.pim.PIMException;
import javax.microedition.pim.UnsupportedFieldException;
import javax.microedition.pim.PIM;

import com.sun.j2me.main.Configuration;
import com.sun.j2me.pim.formats.FormatSupport;
import com.sun.j2me.jsr75.StringUtil;

/**
 * Porting layer implementation for PIM functionality.
 */
public class PIMProxy extends PIMHandler {

    /**
     * Table of fields for any list.
     */
    private Hashtable listFields = new Hashtable();

    /**
     * Table of attributes for any list.
     */
    private Hashtable listAttributes = new Hashtable();

    /**
     * List handle for which fields have been already initialized.
     */
    private Object initialized = null;

    /**
     * Field for storing data handler between native calls
     */
    private int dataHandler;

    /**
     * Field for storing items counter between native calls
     */
    private int itemCounter;

    /**
     * Constant representing a Contact List.
     * List handle for which fields have been already initialized.
     */
    public static final int CONTACT_LIST = 1;

    /**
     * Constant representing an Event List.
     */
    public static final int EVENT_LIST = 2;

    /**
     * Constant representing a ToDo List.
     */
    public static final int TODO_LIST = 3;

    /**
     * This class holds information about a single list.
     */
    private static class List {
        /** Native handle of the list */
        int handle;

        /** Type of the list: CONTACT_LIST, EVENT_LIST or TODO_LIST */
        int type;

        /**
         * The only constructor for this list descriptor.
         *
         * @param listHandle low-level (native) handle of the list
         * @param listType CONTACT_LIST, EVENT_LIST or TODO_LIST
         */
        List(int listHandle, int listType) {
            handle = listHandle;
            type = listType;
        }
    }

    /**
     * This class holds information about a single list item.
     */
    private static class Item {
        /** Native handle of the item */
        int handle;

        /** Binary data of the item */
        byte[] rawData;

        /** Array of categories the item belongs to */
        String[] categories;

        /**
         * Simple constructor for this item descriptor.
         *
         * @param list descriptor of the list where the item belongs
         * @param handle low-level (native) handle of the item
         * @param dataLength size of item's data (in bytes)
         */
        Item(int handle, int dataLength) {
            this.handle = handle;
            rawData = new byte[dataLength];
            categories = null;
        }

        /**
         * Constructor that allows to specify categories of the item.
         *
         * @param list descriptor of the list where the item belongs
         * @param handle low-level (native) handle of the item
         * @param dataLength size of item's data (in bytes)
         * @param cats array of categories the item belongs to
         */
        Item(int handle, int dataLength, String[] cats) {
            this(handle, dataLength);
            setCategories(cats);
        }

        /**
         * Sets categories for the item.
         *
         * @param cats array of categories the item belongs to
         */
        void setCategories(String[] cats) {
            categories = cats;
        }

        /**
         * Returns item's categories.
         *
         * @return array of categories the item belongs to
         */
        String[] getCategories() {
            String[] cats =
                new String[categories == null ? 0 : categories.length];
            if (cats.length > 0) {
                System.arraycopy(categories, 0, cats, 0, categories.length);
            }
            return cats;
        }
    }

    /**
     * Set up field and attribute descriptions.
     *
     * @param listHandle descriptor of the list which fields and
     *        attributes will be retrieved
     */
    synchronized private void initialize(Object listHandle) {
        if (initialized != listHandle) {
            listFields.clear();
            listAttributes.clear();
            int list = ((List)listHandle).handle;

            int [] tmpArray = new int [1];
            int numFields = getFieldsCount0(list, tmpArray);
            if (numFields <= 0) {
                return;
            }
            PIMFieldDescriptor[] desc = new PIMFieldDescriptor[numFields];
            for (int i = 0; i < numFields; i++) {
                int numLabels = getFieldLabelsCount0(list, i, tmpArray[0]);
                desc[i] = new PIMFieldDescriptor(0, 0, false, null, " ",
                        new String[numLabels], 0L, 0);
            }
            getFields0(list, desc, tmpArray[0]);
            for (int i = 0; i < numFields; i++) {
                listFields.put(new Integer(desc[i].getField()), desc[i]);
            }

            tmpArray[0] = 0;
            int numAttributes = getAttributesCount0(list, tmpArray);
            PIMAttribute[] attr = new PIMAttribute[numAttributes];
            for (int i = 0; i < numAttributes; i++) {
                attr[i] = new PIMAttribute();
            }
            getAttributes0(list, attr, tmpArray[0]);
            for (int i = 0; i < numAttributes; i++) {
                listAttributes.put(new Integer(attr[i].getAttr()), attr[i]);
            }

            initialized = listHandle;
        }
    }

    /**
     *  Gets the descriptor for given field.
     *
     * @param field the field ID
     *
     * @return field descriptor
     */
    private PIMFieldDescriptor getFieldDescriptor(int field) {
        return (PIMFieldDescriptor)listFields.get(new Integer(field));
    }

    /**
     * Gets all fields that are supported in the given list.
     *
     * @param listHandle handle of list
     * @return  an int array containing all supported fields.
     */
    public int[] getSupportedFields(Object listHandle) {
        initialize(listHandle);
        int[] result = new int[listFields.size()];
        Enumeration fieldNumbers = listFields.keys();
        for (int i = 0; i < result.length; i++) {
	    result[i] = ((Integer)fieldNumbers.nextElement()).intValue();
        }
        return result;
    }

    /**
     * Checks if field is supported in list.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return <code>true</code> if field supported
     */
    public boolean isSupportedField(Object listHandle, int field) {
        initialize(listHandle);
        return getFieldDescriptor(field) != null;
    }

    /**
     * Checks if field has default value.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return <code>true</code> if field supported
     */
    public boolean hasDefaultValue(Object listHandle, int field) {
        initialize(listHandle);
        return getFieldDescriptor(field).hasDefaultValue();
    }

    /**
     * Gets the data type of the field.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return data type identifier
     */
    public int getFieldDataType(Object listHandle, int field) {
        initialize(listHandle);
        try {
            return getFieldDescriptor(field).getDataType();
        } catch (NullPointerException npe) {
            return -1;
        }
    }

    /**
     * Gets the label of the field.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return label of the field
     */
    public String getFieldLabel(Object listHandle, int field) {
        initialize(listHandle);
        try {
            return getFieldDescriptor(field).getLabel();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    /**
     * Gets the default integer value for the given field. This will
     *  only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public int getDefaultIntValue(Object listHandle, int field) {
        initialize(listHandle);
        PIMFieldDescriptor descriptor = getFieldDescriptor(field);
        return ((Integer) descriptor.getDefaultValue()).intValue();
    }

    /**
     * Gets the default string value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public String getDefaultStringValue(Object listHandle, int field) {
        initialize(listHandle);
        return null;
    }

    /**
     * Gets the default String[] value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public String[] getDefaultStringArrayValue(Object listHandle, int field) {
        int length = getStringArraySize(listHandle, field);
        return new String[length];
    }

    /**
     * Gets the default date value for the given field. This will only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public long getDefaultDateValue(Object listHandle, int field) {
        initialize(listHandle);
        return 0;
    }

    /**
     * Gets the default byte[] value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public byte[] getDefaultBinaryValue(Object listHandle, int field) {
        initialize(listHandle);
        return null;
    }

    /**
     * Gets the default boolean value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public boolean getDefaultBooleanValue(Object listHandle, int field) {
        initialize(listHandle);
        return false;
    }

    /**
     * Gets the supported attributes for the given field.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return array of supported attributes of the field
     */
    public int[] getSupportedAttributes(Object listHandle, int field) {
        initialize(listHandle);
        long attributes = getFieldDescriptor(field).getSupportedAttributes();
        // ATTR_NONE is supported for all Contact fields
        int elementCount = 0;
        for (long a = attributes; a > 0; a >>= 1) {
            if ((a & 1) == 1) {
                elementCount++;
            }
        }
        int[] result = new int[elementCount];
        if (elementCount > 0) {
            int a = 1;
            for (int i = 0; i < elementCount; i++) {
                while ((attributes & a) == 0) a <<= 1;
                result[i] = a;
                a <<= 1;
            }
        }
        return result;
    }

    /**
     * Gets a mask containing all possible attributes for the given field.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @return supported attribute mask
     */
    public int getSupportedAttributesMask(Object listHandle, int field) {
        initialize(listHandle);
        return (int)getFieldDescriptor(field).getSupportedAttributes();
    }

    /**
     * Gets attribute label for the given field attribute.
     *
     * @param listHandle handle of the list
     * @param attribute identifier of attribute
     * @return attribute label
     */
    public String getAttributeLabel(Object listHandle, int attribute) {
        initialize(listHandle);
        if (attribute == PIMItem.ATTR_NONE) {
            String tag = "PIM.Attributes.None";
            String ret = Configuration.getProperty(tag);
            return ret == null ? "Label_" + tag : ret;
        }
        try {
            return ((PIMAttribute)listAttributes.
                    get(new Integer(attribute))).getLabel();
        } catch (NullPointerException npe) {
            throw new UnsupportedFieldException("Attribute " + attribute +
                " is not supported");
        }
    }

    /**
     * Checks if attribute is supported.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @param attribute identifier of attribute
     * @return <code>true</code> if attribute is supported
     */
    public boolean isSupportedAttribute(Object listHandle, int field,
        int attribute) {
        initialize(listHandle);
        if (attribute == PIMItem.ATTR_NONE) {
            return true;
        } else {
            long attributes = getFieldDescriptor(field).
                    getSupportedAttributes();
            return (attributes & attribute) != 0;
        }
    }

    /**
     * Checks if size of the string array.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @return size of the string array
     */
    public int getStringArraySize(Object listHandle, int field) {
        initialize(listHandle);
        try {
            return getFieldDescriptor(field).getStringArraySize();
        } catch (NullPointerException e) {
            // debug.exception(Debug.LIGHT, e);
            return 0;
        }
    }

    /**
     * Gets the array of supported elements.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @return array of supported elements
     */
    public int[] getSupportedArrayElements(Object listHandle, int field) {
        int size = getStringArraySize(listHandle, field);
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = i;
        }
        return result;
    }

    /**
     * Gets the array element label.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @param arrayElement the element identifier
     * @return label fro the array element
     */
    public String getArrayElementLabel(Object listHandle, int field,
						int arrayElement) {
        initialize(listHandle);
        return getFieldDescriptor(field).getElementlabel(arrayElement);
    }

    /**
     * Checks if the array element is supported.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @param arrayElement the element identifier
     * @return <code>true</code> if attribute element is supported
     */
    public boolean isSupportedArrayElement(Object listHandle, int field,
						    int arrayElement) {
        return arrayElement >= 0 &&
	    arrayElement < getStringArraySize(listHandle, field);
    }

    /**
     * Get the maximum number of values that can be stored in the given field.
     *
     * @param listHandle handle of the list
     * @param field the field type
     * @return the maximum value
     */
    public int getMaximumValues(Object listHandle, int field) {
        initialize(listHandle);
        return getFieldDescriptor(field).getMaximumValues();
    }

    /**
     * Get the supported list names for the given list type. All list elements
     * must be unique within the list.
     *
     * @param listType the type of the list
     * @return a non-null array of supported list names. A copy of this array is
     * returned by PIM.listPIMLists()
     */
    synchronized public String[] getListNames(int listType) {
        int namesCount = getListNamesCount0(listType);
        String[] listNames = new String[namesCount];

        if (namesCount != 0) {
            getListNames0(listNames);
        }

        return listNames;
    }

    /**
     * Opens list.
     *
     * @param listType the type of the list
     * @param listName the name of the list
     * @param openMode open mode:
     * <ul>
     *  <li> {@link javax.microedition.pim.PIM#READ_ONLY}
     *  <li> {@link javax.microedition.pim.PIM#WRITE_ONLY}
     *  <li> {@link javax.microedition.pim.PIM#READ_WRITE}
     * </ul>
     * @return list handle that will be used to access this list
     * @throws PIMException  in case of I/O error.
     */
    public Object openList(int listType, String listName, int openMode)
        throws PIMException {
        int listHandle = listOpen0(listType, listName, openMode);
        if (listHandle == 0) {
            throw new PIMException("Unable to open list");
        }
        return new List(listHandle, listType);
    }

    /**
     * Closes list.
     *
     * @param listHandle handle of list
     * @throws PIMException  in case of I/O error.
     */
    public void closeList(Object listHandle)
        throws PIMException {
        if (!listClose0(((List)listHandle).handle)) {
            throw new PIMException();
        }
    }

    /**
     * Get list element keys.
     *
     * @param listHandle handle of the list
     * @return an array of objects representing PIM element keys. These keys
     * are to be passed to getListElement() and commitListElement().
     * @throws PIMException  in case of I/O error.
     */
    synchronized public Object[] getListKeys(Object listHandle)
        throws PIMException {
        Vector keys = new Vector();
        int[] itemDesc = new int[4];
        int handle = ((List)listHandle).handle;

        while (getNextItemDescription0(handle, itemDesc)) {
            Item nextItem = new Item(itemDesc[0], itemDesc[1]);
            getNextItemData0(nextItem.handle, nextItem.rawData, itemDesc[3]);
            keys.addElement(nextItem);
            String catList = getItemCategories0(nextItem.handle, itemDesc[3]);
            if (catList != null) {
                nextItem.setCategories(StringUtil.split(catList, ',', 0));
            }
        }
        Item[] items = new Item[keys.size()];
        for (int index = 0; index < items.length; index++) {
            items[index] = (Item)keys.elementAt(index);
        }
        return items;
    }

    /**
     * Get the data for a list element.
     * @param listHandle handle of the list
     * @param elementKey the key of the requested element
     * @return a byte array containing the element data in a supported format
     * @throws PIMException  in case of I/O error.
     */
    public byte[] getListElement(Object listHandle,
        Object elementKey) throws PIMException {
        return ((Item)elementKey).rawData;
    }

    /**
     * Get categories for the specified list element.
     * @param listHandle handle of list
     * @param elementKey the key of the requested element
     * @return an array of categories names
     * @throws PIMException  in case of I/O error.
     */
    public String[] getListElementCategories(Object listHandle,
        Object elementKey) throws PIMException {

        return ((Item)elementKey).getCategories();
    }

    /**
     * Commit a list element.
     *
     * @param listHandle handle of the list
     * @param elementKey the key of the element to be stored, or null if this
     * is a new element.
     * @param element element data in a form that can be interpreted
     * by getListElement()
     * @param categories list of categories which the list element belongs to
     * @return a non-null key for this element, to be used in future calls
     * to commitListElement() and getListElement()
     * @throws PIMException  in case of I/O error.
     */
    synchronized public Object commitListElement(Object listHandle,
        Object elementKey,
        byte[] element,
        String[] categories) throws PIMException {
        Item item = (Item)elementKey;
        List list = (List)listHandle;

        if (elementKey == null) {
            /* Add new item */
            int itemHandle = addItem0(list.handle, element, categories == null ?
                null : StringUtil.join(categories, ","));
            if (itemHandle == 0) {
                throw new PIMException("Unable to add new item");
            }
            item = new Item(itemHandle, element.length, categories);
            item.rawData = element;
        } else if (element == null) {
            /* Remove item */
            if (!removeItem0(list.handle, item.handle)) {
                throw new PIMException("Unable to delete item");
            }
        } else {
            item.rawData = element;
            if (!commitItemData0(list.handle, item.handle,
                element, categories == null ?
                null : StringUtil.join(categories, ","))) {
                throw new PIMException("Unable to update",
                    PIMException.UPDATE_ERROR);
            }
        }
        return item;
    }

    /**
     * Gets the set of categories defined for a list.
     *
     * @param listHandle handle of the list
     * @return the set of defined categories
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     */
    public String[] getCategories(Object listHandle)
        throws PIMException {
        String categories = getListCategories0(((List)listHandle).handle);
        return categories != null ? StringUtil.split(categories, ',', 0) :
                                    new String[0];
    }

    /**
     * Gets the maximum number of categories this list can have.
     * 
     * @param listHandle handle to the list
     * @return the number of categories supported by this list.
     *         0 indicates no category support and -1 indicates there is no
     *         limit for the number of categories that this list can have
     */
    public int getMaxCategories(Object listHandle) {
        return getListMaxCategories0(((List)listHandle).handle);
    }

    /**
     * Gets the maximum number of categories a list's item can be assigned to.
     * 
     * @param listHandle handle to the list
     * @return the number of categories an item can be assigned to.
     *         0 indicates no category support and -1 indicates there is no
     *         limit for the number of categories an item can be assigned to
     */
    public int getMaxCategoriesPerItem(Object listHandle) {
        return getListMaxCategoriesPerItem0(((List)listHandle).handle);
    }

    /**
     * Adds a category to the categories defined for a list.
     *
     * @param listHandle handle of list
     * @param category category name
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     * @see #getCategories
     */
    public void addCategory(Object listHandle,
        String category) throws PIMException {
        if (getCategories(listHandle).length ==
            getListMaxCategories0(((List)listHandle).handle)) {
            throw new PIMException("Maximum number of categories exceeded",
                PIMException.MAX_CATEGORIES_EXCEEDED);
        }
        if (!addListCategory0(((List)listHandle).handle, category)) {
            throw new PIMException("Unable to add category",
                PIMException.UPDATE_ERROR);
        }
    }

    /**
     * Deletes a category from the categories defined for a list.
     *
     * @param listHandle handle of list
     * @param category category name
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     * @see #getCategories
     */
    public void deleteCategory(Object listHandle,
        String category) throws PIMException {
        if (!deleteListCategory0(((List)listHandle).handle, category)) {
            throw new PIMException("Unable to delete category",
                PIMException.UPDATE_ERROR);
        }
    }

    /**
     * Rename a category.
     *
     * @param listHandle handle of list
     * @param currentCategory current category name
     * @param newCategory new category name
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     * @see #getCategories
     */
    public void renameCategory(Object listHandle,
        String currentCategory, String newCategory) throws PIMException {
        if (!renameListCategory0(((List)listHandle).handle, currentCategory,
            newCategory)) {
            throw new PIMException("Unable to rename category",
                PIMException.UPDATE_ERROR);
        }
    }

    /**
     * Returns number of lists of the specified type.
     *
     * @param listType CONTACT_LIST, EVENT_LIST or TODO_LIST
     *
     * @return number of lists
     * @see #getListNames0
     */
    private native int getListNamesCount0(int listType);

    /**
     * Retrieves list names for the selected list type.
     *
     * @param names array where list names will be stored
     *        (must have sufficient number of elements for list names)
     * @see #getListNamesCount0
     */
    private native void getListNames0(String[] names);

    /**
     * Get the name of the default list for the given type.
     *
     * @param listType the type of the list
     * @return the name of the default list, or null if no list of this type
     * is supported.
     */
    public native String getDefaultListName(int listType);

    /**
     * Opens the specified list.
     *
     * @param listType CONTACT_LIST, EVENT_LIST or TODO_LIST
     * @param listName name of the list to open
     * @param mode open mode:
     * <ul>
     *  <li> {@link javax.microedition.pim.PIM#READ_ONLY}
     *  <li> {@link javax.microedition.pim.PIM#WRITE_ONLY}
     *  <li> {@link javax.microedition.pim.PIM#READ_WRITE}
     * </ul>
     *
     * @return native handle of the opened list
     * @see #listClose0
     */
    private native int listOpen0(int listType, String listName, int mode);

    /**
     * Closes the specified list.
     *
     * @param listHandle native handle of the list that was previously opened
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     * @see #listOpen0
     */
    private native boolean listClose0(int listHandle);

    /**
     * Retrieves general information about the next item in the list.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param description buffer to store the item description
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private native boolean getNextItemDescription0(int listHandle,
        int[] description);

    /**
     * Retrieves next item's data from the list.
     *
     * @param itemHandle native handle of the item
     * @param data buffer to store the item's data
     * @param dataHandle handle for data buffer
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private native boolean getNextItemData0(int itemHandle, byte[] data, int dataHandle);

    /**
     * Writes modified item data to persistent storage.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param itemHandle native handle of the item
     * @param data raw data of the item
     * @param categories item's categories, separated by comma
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private native boolean commitItemData0(int listHandle, int itemHandle,
        byte[] data, String categories);

    /**
     * Adds item to the list.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param data raw data of the item
     * @param categories item's categories, separated by comma
     *
     * @return native handle of the item
     */
    private native int addItem0(int listHandle, byte[] data, String categories);

    /**
     * Removes specified item from the list.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param itemHandle native handle of the item
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private native boolean removeItem0(int listHandle, int itemHandle);

    /**
     * Retrieves categories defined for the specified list.
     *
     * @param listHandle native handle of the list that was previously opened
     *
     * @return item's categories, separated by comma
     */
    private native String getListCategories0(int listHandle);

    /**
     * Returns maximum number of categories supported for the given list.
     *
     * @param listHandle native handle of the list that was previously opened
     *
     * @return maximum number of categories for the list
     */
    private native int getListMaxCategories0(int listHandle);

    /**
     * Returns maximum number of categories per item supported for the given
     * list.
     *
     * @param listHandle native handle of the list that was previously opened
     *
     * @return maximum number of categories per item in the list
     */
    private native int getListMaxCategoriesPerItem0(int listHandle);

    /**
     * Adds category to the specified list.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param category name of the category to add
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private native boolean addListCategory0(int listHandle, String category);

    /**
     * Removes category from the specified list.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param category name of the category to delete
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private native boolean deleteListCategory0(int listHandle, String category);

    /**
     * Renames category supported by the given list.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param currentCategory old category name
     * @param newCategory new category name
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private native boolean renameListCategory0(int listHandle,
        String currentCategory, String newCategory);

    /**
     * Retrieves list of categories the specified item belongs to.
     *
     * @param itemHandle native handle of the item
     * @param dataHandle handle for data buffer
     * @return item's categories, separated by comma
     */
    private native String getItemCategories0(int itemHandle, int dataHandle);

    /**
     * Returns number of fields supported by the given list.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param dataHandle to save data handle in
     *
     * @return number of supported fields
     */
    private native int getFieldsCount0(int listHandle, int [] dataHandle);

    /**
     * Returns number of labels for the specified field.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param fieldIndex index of the field
     * @param dataHandle handle of data
     *
     * @return number of labels
     */
    private native int getFieldLabelsCount0(int listHandle, int fieldIndex, int dataHandle);

    /**
     * Retrieves information about all fields supported by the list.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param desc array where field descriptions will be stored
     * @param dataHandle handle of data
     */
    private native void getFields0(int listHandle, PIMFieldDescriptor[] desc, int dataHandle);

    /**
     * Returns number of attributes supported by the given list.
     *
     * @param listHandle native handle of the list that was previously opened
     *
     * @param dataHandle array to store data handle in
     * @return number of supported attributes
     */
    private native int getAttributesCount0(int listHandle, int[] dataHandle);

    /**
     * Retrieves information about all attributes supported by the list.
     *
     * @param listHandle native handle of the list that was previously opened
     * @param attr array where attribute descriptions will be stored
     * @param dataHandle data handle
     */
    private native void getAttributes0(int listHandle, PIMAttribute[] attr, int dataHandle);
}

