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

import com.sun.j2me.pim.formats.VCard30Format;
import javax.microedition.pim.Contact;
import javax.microedition.pim.PIM;

/**
 * Implementation of a PIM contact.
 *
 */
public class ContactImpl extends AbstractPIMItem implements Contact {
    /**
     * Constructs a Contact list.
     * @param list handle for Contact List implementation
     */
    public ContactImpl(AbstractPIMList list) {
        super(list, PIM.CONTACT_LIST);
        if (list != null && !(list instanceof ContactListImpl)) {
            throw new RuntimeException("Wrong list passed");
        }
    }

    /**
     * Constructs a Contact list from a handler and base Contact
     * record.
     * @param list Contact List implementation handler
     * @param base Contact record template
     */
    ContactImpl(AbstractPIMList list, Contact base) {
        super(list, base);
        if (!(list instanceof ContactListImpl)) {
            throw new RuntimeException("Wrong list passed");
        }
    }

    /**
     * Gets preferred index for requested field.
     * @param field requested element
     * @return preferred index for requested field
     */
    public int getPreferredIndex(int field) {
        int indices = countValues(field);
        for (int i = 0; i < indices; i++) {
            int attributes = getAttributes(field, i);
            if ((attributes & ATTR_PREFERRED) != 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets encoding format for this contact record.
     * @return handle for encoding format
     */
    PIMFormat getEncodingFormat() {
        return new VCard30Format();
    }

    /**
     * Ensures valid field.
     * @param field identifier to validate
     * @return <code>true</code> if contact field identifier is
     * supported
     */
    static boolean isValidPIMField(int field) {
        switch (field) {
            case Contact.ADDR:
            case Contact.BIRTHDAY:
            case Contact.CLASS:
            case Contact.EMAIL:
            case Contact.FORMATTED_ADDR:
            case Contact.FORMATTED_NAME:
            case Contact.NAME:
            case Contact.NICKNAME:
            case Contact.NOTE:
            case Contact.ORG:
            case Contact.PHOTO:
            case Contact.PHOTO_URL:
            case Contact.PUBLIC_KEY:
            case Contact.PUBLIC_KEY_STRING:
            case Contact.REVISION:
            case Contact.TEL:
            case Contact.TITLE:
            case Contact.UID:
            case Contact.URL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Adds a binary value to the current Contact.
     * @param field identifier for current element
     * @param attributes property to insert
     * @param value binary value to be recorded
     * @param offset index into value array
     * @param length size of data to copy
     */
    public void addBinary(int field, int attributes, byte[] value,
            int offset, int length) {
        super.addBinary(field, attributes, value, offset, length);
        if (field == PUBLIC_KEY) {
            // remove any values from PUBLIC_KEY_STRING
            while (countValues(PUBLIC_KEY_STRING) > 0) {
                removeValue(PUBLIC_KEY_STRING, 0);
            }
        }
    }

    /**
     * Adds a string to the current Contact.
     * @param field identifier for current element
     * @param attributes property to insert
     * @param value string to be recorded
     */
    public void addString(int field, int attributes, String value) {
        super.addString(field, attributes, value);
        if (field == PUBLIC_KEY_STRING) {
            // remove any values from PUBLIC_KEY
            while (countValues(PUBLIC_KEY) > 0) {
                removeValue(PUBLIC_KEY, 0);
            }
        }
    }

    /**
     * Gets the revision field identifier.
     * @return revision field identifier
     */
    protected int getRevisionField() {
        return REVISION;
    }

    /**
     * Gets the UID field identifier.
     * @return UID field identifier
     */
    protected int getUIDField() {
        return UID;
    }

    /**
     * Converts the Contact to a printable format.
     * @return formatted Contact record
     */
    protected String toDisplayableString() {
        return "Contact[" + formatData() + "]";
    }

}
