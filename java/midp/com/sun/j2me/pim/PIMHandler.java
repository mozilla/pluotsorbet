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

import javax.microedition.pim.PIMException;
import com.sun.j2me.main.Configuration;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;

/**
 * Porting layer for PIM functionality. Implemented by PIMBridge
 *
 */
public abstract class PIMHandler {
    /** Current handle for PIMHandler. */
    private static PIMHandler instance;

    /**
     * Gets a handle to the current PIMHandler.
     * @return PIM handler
     */
    public static PIMHandler getInstance() {
        if (instance == null) {
            String className = Configuration
		.getProperty("javax.microedition.pim.handler");
            if (className == null) {
                className = "com.sun.j2me.pim.PIMProxy";
            }
            boolean isExcThrown = false;
            try {
                instance = (PIMHandler) Class.forName(className).newInstance();
            } catch (ClassNotFoundException e) {
                isExcThrown = true;
            } catch (Error e) {
                isExcThrown = true;
            } catch (IllegalAccessException e) {
                isExcThrown = true;
            } catch (InstantiationException e) {
                isExcThrown = true;
            }
            if (isExcThrown) {
                throw new Error("PIM handler could not be initialized.");
            }
        }

        return instance;
    }

    /**
     * Gets all fields that are supported in the given list.
     *
     * @param listHandle handle of list
     * @return  an int array containing all supported fields.
     */
    public abstract int[] getSupportedFields(Object listHandle);

    /**
     * Checks if field is supported in list.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return <code>true</code> if field supported
     */
    public abstract boolean isSupportedField(Object listHandle, int field);

    /**
     * Checks if field has default value.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return <code>true</code> if field supported
     */
    public abstract boolean hasDefaultValue(Object listHandle, int field);

    /**
     * Gets the data type of the field.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return data type identifier
     */
    public abstract int getFieldDataType(Object listHandle, int field);

    /**
     * Gets the label of the field.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return label of the field
     */
    public abstract String getFieldLabel(Object listHandle, int field);

    /**
     * Gets the default integer value for the given field. This will
     *  only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return default value of the field
     */
    public abstract int getDefaultIntValue(Object listHandle, int field);

    /**
     * Gets the default string value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return default value of the field
     */
    public abstract String getDefaultStringValue(Object listHandle, int field);

    /**
     * Gets the default String[] value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return default value of the field
     */
    public abstract String[] getDefaultStringArrayValue(Object listHandle,
							int field);

    /**
     * Gets the default date value for the given field. This will only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return default value of the field
     */
    public abstract long getDefaultDateValue(Object listHandle, int field);

    /**
     * Gets the default byte[] value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return default value of the field
     */
    public abstract byte[] getDefaultBinaryValue(Object listHandle, int field);

    /**
     * Gets the default boolean value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return default value of the field
     */
    public abstract boolean getDefaultBooleanValue(Object listHandle,
        int field);

    /**
     * Gets the supported attributes for the given field.
     * @param listHandle handle of list
     * @param field identifier of field
     * @return array of supported attributes of the field
     */
    public abstract int[] getSupportedAttributes(Object listHandle, int field);

    /**
     * Gets a mask containing all possible attributes for the given field.
     *
     * @param listHandle handle of list
     * @param field the field number
     * @return supported attribute mask
     */
    public abstract int getSupportedAttributesMask(Object listHandle,
        int field);

    /**
     * Gets attribute label for the given field attribute.
     *
     * @param listHandle handle of list
     * @param attribute identifier of attribute
     * @return attribute label
     */
    public abstract String getAttributeLabel(Object listHandle, int attribute);

    /**
     * Checks if attribute is supported.
     *
     * @param listHandle handle of list
     * @param field the field number
     * @param attribute identifier of attribute
     * @return <code>true</code> if attribute is supported
     */
    public abstract boolean isSupportedAttribute(Object listHandle, int field,
						 int attribute);

    /**
     * Checks if size of the string array.
     *
     * @param listHandle handle of list
     * @param field the field number
     * @return size of the string array
     */
    public abstract int getStringArraySize(Object listHandle, int field);

    /**
     * Gets the array of supported elements.
     *
     * @param listHandle handle of list
     * @param field the field number
     * @return array of supported elements
     */
    public abstract int[] getSupportedArrayElements(Object listHandle,
        int field);

    /**
     * Gets the array element label.
     *
     * @param listHandle handle of list
     * @param field the field number
     * @param arrayElement the element identifier
     * @return label fro the array element
     */
    public abstract String getArrayElementLabel(Object listHandle, int field,
						int arrayElement);

    /**
     * Checks if the array element is supported.
     *
     * @param listHandle handle of list
     * @param field the field number
     * @param arrayElement the element identifier
     * @return <code>true</code> if attribute element is supported
     */
    public abstract boolean isSupportedArrayElement(Object listHandle,
        int field, int arrayElement);

    /**
     * Get the maximum number of values that can be stored in the given field.
     *
     * @param listHandle handle of list
     * @param field the field type
     * @return the maximum value
     */
    public abstract int getMaximumValues(Object listHandle, int field);

    /**
     * Get the supported list names for the given list type. All list elements
     * must be unique within the list.
     *
     * @param listType the type of the list
     * @return a non-null array of supported list names. A copy of this array is
     * returned by PIM.listPIMLists()
     */
    public abstract String[] getListNames(int listType);

    /**
     * Get the name of the default list for the given type.
     *
     * @param listType the type of the list
     * @return the name of the default list, or null if no list of this type
     * is supported.
     */
    public abstract String getDefaultListName(int listType);

    /**
     * Opens list.
     *
     * @param listType the type of the list
     * @param listName the name of the list
     * @param openMode open mode
     * @return list handle that will be used to access this list
     * @throws PIMException  in case of I/O error.
     */
    public abstract Object openList(int listType, String listName, int openMode)
        throws PIMException;

    /**
     * Closes list.
     *
     * @param listHandle handle of list
     * @throws PIMException  in case of I/O error.
     */
    public abstract void closeList(Object listHandle)
        throws PIMException;

    /**
     * Get list element keys.
     *
     * @param listHandle handle of list
     * @return an array of objects representing PIM element keys. These keys
     * are to be passed to getListElement() and commitListElement().
     * @throws PIMException  in case of I/O error.
     */
    public abstract Object[] getListKeys(Object listHandle)
        throws PIMException;

    /**
     * Get the data for a list element.
     * @param listHandle handle of list
     * @param elementKey the key of the requested element
     * @return a byte array containing the element data in a supported format
     * @throws PIMException  in case of I/O error.
     */
    public abstract byte[] getListElement(Object listHandle,
        Object elementKey) throws PIMException;

    /**
     * Get categories for the specified list element.
     * @param listHandle handle of list
     * @param elementKey the key of the requested element
     * @return an array of categories names
     * @throws PIMException  in case of I/O error.
     */
    public abstract String[] getListElementCategories(Object listHandle,
        Object elementKey) throws PIMException;

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
    public abstract Object commitListElement(Object listHandle,
        Object elementKey,
        byte[] element,
        String[] categories) throws PIMException;

    /**
     * Gets the set of categories defined for a list.
     *
     * @param listHandle handle of list
     * @return the set of defined categories
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     */
    public abstract String[] getCategories(Object listHandle)
        throws PIMException;

    /**
     * Gets the maximum number of categories this list can have.
     * 
     * @param listHandle handle to the list
     * @return the number of categories supported by this list.
     *         0 indicates no category support and -1 indicates there is no
     *         limit for the number of categories that this list can have
     */
    public abstract int getMaxCategories(Object listHandle);

    /**
     * Gets the maximum number of categories a list's item can be assigned to.
     * 
     * @param listHandle handle to the list
     * @return the number of categories an item can be assigned to.
     *         0 indicates no category support and -1 indicates there is no
     *         limit for the number of categories an item can be assigned to
     */
    public abstract int getMaxCategoriesPerItem(Object listHandle);

    /**
     * Adds a category to the categories defined for a list.
     *
     * @param listHandle handle of list
     * @param category category name
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     * @see #getCategories
     */
    public abstract void addCategory(Object listHandle,
        String category) throws PIMException;

    /**
     * Deletes a category from the categories defined for a list.
     *
     * @param listHandle handle of list
     * @param category category name
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     * @see #getCategories
     */
    public abstract void deleteCategory(Object listHandle,
        String category) throws PIMException;

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
    public abstract void renameCategory(Object listHandle,
        String currentCategory, String newCategory) throws PIMException;

    /** YEAR - constant from Calendar class. */
    private static final int YEAR = Calendar.YEAR;
    /** MONTH - constant from Calendar class. */
    private static final int MONTH = Calendar.MONTH;
    /** DAY_OF_MONTH - constant from Calendar class. */
    private static final int DAY_OF_MONTH = Calendar.DAY_OF_MONTH;
    /** HOUR_OF_DAY - constant from Calendar class. */
    private static final int HOUR_OF_DAY = Calendar.HOUR_OF_DAY;
    /** MINUTE - constant from Calendar class. */
    private static final int MINUTE = Calendar.MINUTE;
    /** SECOND - constant from Calendar class. */
    private static final int SECOND = Calendar.SECOND;

    /**
     *  Adds the "0" prefix to one-digit number in string representation.
     *
     * @param str  number in string representation
     *
     * @return two-digit number in string representation
     */
    private String prefix_0(String str) {
        String returnValue = "";
        if (str.length() < 2) { // x convert to 0x
            returnValue = "0";
        }
        returnValue += str;
        return returnValue;
    }

    /**
     *  Converts date in format yyyy-MM-dd or yyyyMMdd to milliseconds.
     *
     * @param s  date in format yyyy-MM-dd or yyyyMMdd
     *
     * @return number of milliseconds
     */
    public long parseDate(String s) {
        Calendar local_calendar =
            Calendar.getInstance();

        // reset calendar to the state it is in composeDate method to get
        // the same values for hours, minutes, seconds and milliseconds. 
        Date cldc_date = new Date(0);
        local_calendar.setTime(cldc_date);

        int year, month, day;
        if (s.indexOf('-') != -1) { // yyyy-MM-dd
            year =  Integer.parseInt(s.substring(0, 4));
            month = Integer.parseInt(s.substring(5, 7));
            day =   Integer.parseInt(s.substring(8, 10));
        } else { // yyyyMMdd
            year =  Integer.parseInt(s.substring(0, 4));
            month = Integer.parseInt(s.substring(4, 6));
            day =   Integer.parseInt(s.substring(6, 8));
        }

        local_calendar.set(Calendar.YEAR, year);
        local_calendar.set(Calendar.MONTH, month - 1);
        local_calendar.set(Calendar.DAY_OF_MONTH, day);

        return (local_calendar.getTime()).getTime();
    }

    /**
     *  Converts date in milliseconds to yyyy-MM-dd string.
     *
     * @param date  number of milliseconds
     *
     * @return date in format yyyy-MM-dd
     */
    public String composeDate(long date) { // yyyy-MM-dd
        Calendar local_calendar =
            Calendar.getInstance();

        Date cldc_date = new Date(date);
        local_calendar.setTime(cldc_date);

        String returnValue = Integer.toString(local_calendar.get(YEAR)) +
            "-" + prefix_0(Integer.toString(local_calendar.get(MONTH)+1)) +
            "-" + prefix_0(Integer.toString(local_calendar.get(DAY_OF_MONTH)));

        return returnValue;
    }

    /**
     *  Converts date in milliseconds to yyyymmdd string.
     *
     * @param date  number of milliseconds
     *
     * @return date in format yyyymmdd
     */
    public String composeDate1(long date) { // yyyymmdd
        Date cldc_date = new Date(date);
        Calendar local_calendar =
            Calendar.getInstance();
        local_calendar.setTime(cldc_date);

        String returnValue = Integer.toString(local_calendar.get(YEAR)) +
            prefix_0(Integer.toString(local_calendar.get(MONTH)+1)) +
            prefix_0(Integer.toString(local_calendar.get(DAY_OF_MONTH)));

        return returnValue;
    }

    /**
     *  Converts date/time in format yyyyMMddTHHmmss(Z) to milliseconds.
     *
     * @param s  date/time in format yyyyMMddTHHmmss(Z)
     *
     * @return number of milliseconds
     */
    public long parseDateTime(String s) {
        Calendar local_calendar;
        if (s.length() > 15 && 
            s.charAt(15) == 'Z') { // absolute time
            local_calendar =
                Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        } else {
            local_calendar =
                Calendar.getInstance();
        }
        int year, month, day, hour, min, sec; // yyyyMMddTHHmmss
        year =  Integer.parseInt(s.substring(0, 4));
        month = Integer.parseInt(s.substring(4, 6));
        day =   Integer.parseInt(s.substring(6, 8));
        hour =  Integer.parseInt(s.substring(9, 11));
        min =   Integer.parseInt(s.substring(11, 13));
        sec =   Integer.parseInt(s.substring(13, 15));

        local_calendar.set(Calendar.YEAR, year);
        local_calendar.set(Calendar.MONTH, month - 1);
        local_calendar.set(Calendar.DAY_OF_MONTH, day);
        local_calendar.set(Calendar.HOUR_OF_DAY, hour);
        local_calendar.set(Calendar.MINUTE, min);
        local_calendar.set(Calendar.SECOND, sec);
        local_calendar.set(Calendar.MILLISECOND, 0);

        return (local_calendar.getTime()).getTime();
    }

    /**
     *  Converts date/time in milliseconds to yyyyMMddTHHmmss.
     *
     * @param date  number of milliseconds
     *
     * @return date/time in format yyyyMMddTHHmmss
     */
    public String composeDateTime(long date) { // yyyyMMddTHHmmss
        Date cldc_date = new Date(date);
        Calendar local_calendar =
            Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        local_calendar.setTime(cldc_date);
        local_calendar.setTimeZone(TimeZone.getDefault());

        String returnValue = Integer.toString(local_calendar.get(YEAR)) +
            prefix_0(Integer.toString(local_calendar.get(MONTH)+1)) +
            prefix_0(Integer.toString(local_calendar.get(DAY_OF_MONTH)))
            + "T" +
            prefix_0(Integer.toString(local_calendar.get(HOUR_OF_DAY))) +
            prefix_0(Integer.toString(local_calendar.get(MINUTE))) +
            prefix_0(Integer.toString(local_calendar.get(SECOND)));

        return returnValue;
    }
}
