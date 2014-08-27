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

package com.sun.j2me.pim.formats;

import javax.microedition.pim.ToDo;

/**
 * Helper methods for vToDo implementations.
 *
 */
public class VToDoSupport {

    /**
     * Converts a JSR75 field code to a vToDo property name.
     * @param field identifier of requested field
     * @return label for requested field
     */
    public static String getFieldLabel(int field) {
        switch (field) {
            case ToDo.COMPLETED: return "STATUS";
            case ToDo.COMPLETION_DATE: return "COMPLETED";
            case ToDo.PRIORITY: return "PRIORITY";
            case ToDo.DUE: return "DUE";
            case ToDo.NOTE: return "DESCRIPTION";
            case ToDo.REVISION: return "LAST-MODIFIED";
            case ToDo.SUMMARY: return "SUMMARY";
            case ToDo.UID: return "UID";
            case ToDo.CLASS: return "CLASS";
            default:
                return null;
        }
    }


    /**
     * Converts a vToDo property name to a JSR75 field code.
     * @param fieldName label of requested field
     * @return identifier of requested field
     */
    public static int getFieldCode(String fieldName) {
        if (fieldName.equals("DESCRIPTION"))
            return ToDo.NOTE;
        else if (fieldName.equals("LAST-MODIFIED"))
            return ToDo.REVISION;
        else if (fieldName.equals("SUMMARY"))
            return ToDo.SUMMARY;
        else if (fieldName.equals("UID"))
            return ToDo.UID;
        else if (fieldName.equals("PRIORITY"))
            return ToDo.PRIORITY;
        else if (fieldName.equals("DESCRIPTION"))
            return ToDo.NOTE;
        else if (fieldName.equals("COMPLETED"))
            return ToDo.COMPLETION_DATE;
        else if (fieldName.equals("STATUS"))
            return ToDo.COMPLETED;
        else if (fieldName.equals("DUE"))
            return ToDo.DUE;
        else if (fieldName.equals("CLASS"))
            return ToDo.CLASS;
        else return -1;
    }

    /**
     * Gets the value of the vToDo CLASS field for the given
     * value of the ToDo.CLASS field.
     * This method encapsulates the following mapping:
     * ToDo.CLASS_PUBLIC -> "PUBLIC"
     * ToDo.CLASS_PRIVATE -> "PRIVATE"
     * ToDo.CLASS_CONFIDENTIAL -> "CONFIDENTIAL"
     *
     * @param fieldValue the value of the ToDo.CLASS field
     * @return a string describing the class for the field value, or null if
     *  fieldValue is out of range
     */
    public static String getClassType(int fieldValue) {
        switch (fieldValue) {
            case ToDo.CLASS_CONFIDENTIAL: return "CONFIDENTIAL";
            case ToDo.CLASS_PRIVATE: return "PRIVATE";
            case ToDo.CLASS_PUBLIC: return "PUBLIC";
        }
        return null;
    }

    /**
     * Gets the value of the ToDo.CLASS field for the given
     * value of the vToDo CLASS property.
     * This method encapsulates the following mapping:
     * ToDo.CLASS_PUBLIC <- "PUBLIC"
     * ToDo.CLASS_PRIVATE <- "PRIVATE"
     * ToDo.CLASS_CONFIDENTIAL <- "CONFIDENTIAL"
     *
     * @param s the value of the CLASS property
     * @return the corresponding field of ToDo, or -1 if s is not recognized
     */
    public static int getClassCode(String s) {
        switch (s.length()) {
            case 6:
                if (s.equals("PUBLIC")) {
                    return ToDo.CLASS_PUBLIC;
                }
                break;
            case 7:
                if (s.equals("PRIVATE")) {
                    return ToDo.CLASS_PRIVATE;
                }
                break;
            case 12:
                if (s.equals("CONFIDENTIAL")) {
                    return ToDo.CLASS_CONFIDENTIAL;
                }
                break;
        }
        return -1;
    }


}
