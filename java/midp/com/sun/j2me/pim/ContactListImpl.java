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

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import com.sun.j2me.security.PIMPermission;

/**
 * Class ContactListImpl implements methods of PIM interface ContactList.
 *
 */
class ContactListImpl extends AbstractPIMList implements ContactList {
    /**
     * Constructs a Contact List handler.
     * @param name label for the list
     * @param mode readable or writable access
     * @param handle handle of the list
     */    
    ContactListImpl(String name, int mode, Object handle) {
        super(PIM.CONTACT_LIST, name, mode, handle);
    }

    /**
     * Creates a Contact entry.
     * @return Contact entry
     */
    public Contact createContact() {
        return new ContactImpl(this);
    }
    
    /**
     * Initializes a Contact entry from a previous
     * Contact entry.
     * @param contact input data
     * @return new Contact initialized from contact
     */
    public Contact importContact(Contact contact) {
        return new ContactImpl(this, contact);
    }

    /**
     * Removes a contact from the list.
     * @param contact entry to be removed
     * @throws PIMException if the entry is not in the list
     */    
    public void removeContact(Contact contact) throws PIMException {
        removeItem(contact);
    }

    /**
     * Verifies read permission.
     *
     * @param action description of the operation
     * @throws SecurityException if operation is not permitted
     */
    protected void checkReadPermission(String action)
        throws SecurityException {
        checkReadPermission();
        checkPermission(PIMPermission.CONTACT_READ, action);
    }

    /**
     * Verifies write permission.
     *
     * @param action description of the operation
     * @throws SecurityException if operation is not permitted
     */
    protected void checkWritePermission(String action)
        throws SecurityException {
        checkWritePermission();
        checkPermission(PIMPermission.CONTACT_WRITE, action);
    }
}
