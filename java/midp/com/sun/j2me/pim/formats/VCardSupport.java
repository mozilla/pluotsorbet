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

import javax.microedition.pim.Contact;

/**
 * Helper methods for vCard implementations.
 *
 */
public class VCardSupport {
    /**
     * Gets the field label.
     * @param field identifier for field
     * @return label of requested field
     */
    public static String getFieldLabel(int field) {
        switch (field) {
            case Contact.FORMATTED_NAME: return "FN";
            case Contact.ADDR: return "ADR";
            case Contact.BIRTHDAY: return "BDAY";
            case Contact.NAME: return "N";
            case Contact.PHOTO: return "PHOTO";
            case Contact.PHOTO_URL: return "PHOTO;VALUE=URL";
            case Contact.TEL: return "TEL";
            case Contact.TITLE: return "TITLE";
            case Contact.REVISION: return "REV";
            case Contact.URL: return "URL";
            case Contact.UID: return "UID";
            case Contact.PUBLIC_KEY: return "KEY";
            case Contact.FORMATTED_ADDR: return "LABEL";
            case Contact.NICKNAME: return "NICKNAME";
            case Contact.NOTE: return "NOTE";
            case Contact.PUBLIC_KEY_STRING: return "KEY";
            case Contact.EMAIL: return "EMAIL";
            case Contact.ORG: return "ORG";
            default:
                return null;
        }
    }

    /**
     * Lookup the field identifier byte name.
     * @param fieldName label for field
     * @return identifier for requested field
     */
    public static int getFieldCode(String fieldName) {
        if (fieldName.equals("FN"))
            return Contact.FORMATTED_NAME;
        else if (fieldName.equals("LABEL"))
            return Contact.FORMATTED_ADDR;
        else if (fieldName.equals("ADR"))
            return Contact.ADDR;
        else if (fieldName.equals("BDAY"))
            return Contact.BIRTHDAY;
        else if (fieldName.equals("N"))
            return Contact.NAME;
        else if (fieldName.equals("PHOTO"))
            return Contact.PHOTO;
        else if (fieldName.equals("TEL"))
            return Contact.TEL;
        else if (fieldName.equals("TITLE"))
            return Contact.TITLE;
        else if (fieldName.equals("REV"))
            return Contact.REVISION;
        else if (fieldName.equals("URL"))
            return Contact.URL;
        else if (fieldName.equals("UID"))
            return Contact.UID;
        else if (fieldName.equals("KEY"))
            return Contact.PUBLIC_KEY;
        else if (fieldName.equals("NICKNAME"))
            return Contact.NICKNAME;
        else if (fieldName.equals("NOTE"))
            return Contact.NOTE;
        else if (fieldName.equals("EMAIL"))
            return Contact.EMAIL;
        else if (fieldName.equals("ORG"))
            return Contact.ORG;
        else return -1;
    }

    /**
     * Lookup the attribute name from identifier.
     * @param attr the field identifier
     * @return the name of the attribute
     */
    public static String getAttributeLabel(int attr) {
        switch (attr) {
            case Contact.ATTR_ASST: return "X-J2MEWTK-ASST";
            case Contact.ATTR_AUTO: return "CAR";
            case Contact.ATTR_FAX: return "FAX";
            case Contact.ATTR_HOME: return "HOME";
            case Contact.ATTR_MOBILE: return "CELL";
            case Contact.ATTR_OTHER: return "X-J2MEWTK-OTHER";
            case Contact.ATTR_PAGER: return "PAGER";
            case Contact.ATTR_PREFERRED: return "PREF";
            case Contact.ATTR_SMS: return "MSG";
            case Contact.ATTR_WORK: return "WORK";
            default: return Extensions.getContactAttributeLabel(attr);
        }
    }

    /**
     * Lookup the attribute identifier.
     * @param label the name of the attribute
     * @param defaultValue default value to return if
     * the attribute identifier is not found
     * @return identifier for requested attribute
     */
    public static int getAttributeCode(String label, int defaultValue) {
        if (label.equals("CAR"))
            return Contact.ATTR_AUTO;
        else if (label.equals("FAX"))
            return Contact.ATTR_FAX;
        else if (label.equals("HOME"))
            return Contact.ATTR_HOME;
        else if (label.equals("CELL"))
            return Contact.ATTR_MOBILE;
        else if (label.equals("X-J2MEWTK-OTHER"))
            return Contact.ATTR_OTHER;
        else if (label.equals("PAGER"))
            return Contact.ATTR_PAGER;
        else if (label.equals("PREF"))
            return Contact.ATTR_PREFERRED;
        else if (label.equals("MSG"))
            return Contact.ATTR_SMS;
        else if (label.equals("WORK"))
            return Contact.ATTR_WORK;
        else if (label.equals("X-J2MEWTK-ASST"))
            return Contact.ATTR_ASST;
        else
            return Extensions.getContactAttributeCode(label, defaultValue);
    }

    /**
     * Gets the value of the vCard CLASS field for the given
     * value of the Contact.CLASS field.
     * This method encapsulates the following mapping:
     * Contact.CLASS_PUBLIC -> "PUBLIC"
     * Contact.CLASS_PRIVATE -> "PRIVATE"
     * Contact.CLASS_CONFIDENTIAL -> "CONFIDENTIAL"
     *
     * @param fieldValue the value of the Contact.CLASS field
     * @return a string describing the class for the field value, or null if
     *  fieldValue is out of range
     */
    public static String getClassType(int fieldValue) {
        switch (fieldValue) {
            case Contact.CLASS_CONFIDENTIAL: return "CONFIDENTIAL";
            case Contact.CLASS_PRIVATE: return "PRIVATE";
            case Contact.CLASS_PUBLIC: return "PUBLIC";
        }
        return null;
    }

    /**
     * Gets the value of the Contact.CLASS field for the given
     * value of the vCard CLASS property.
     * This method encapsulates the following mapping:
     * Contact.CLASS_PUBLIC <- "PUBLIC"
     * Contact.CLASS_PRIVATE <- "PRIVATE"
     * Contact.CLASS_CONFIDENTIAL <- "CONFIDENTIAL"
     *
     * @param s the value of the CLASS property
     * @return the corresponding field of Contact, or -1 if s is not recognized
     */
    public static int getClassCode(String s) {
        switch (s.length()) {
            case 6:
                if (s.equals("PUBLIC")) {
                    return Contact.CLASS_PUBLIC;
                }
                break;
            case 7:
                if (s.equals("PRIVATE")) {
                    return Contact.CLASS_PRIVATE;
                }
                break;
            case 12:
                if (s.equals("CONFIDENTIAL")) {
                    return Contact.CLASS_CONFIDENTIAL;
                }
                break;
        }
        return -1;
    }

}
