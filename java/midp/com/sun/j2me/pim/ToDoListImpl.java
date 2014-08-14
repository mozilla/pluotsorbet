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

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.ToDoList;
import com.sun.j2me.security.PIMPermission;

/**
 * Class ToDoListImpl implements methods of PIM interface ToDoList.
 *
 */
class ToDoListImpl extends AbstractPIMList implements ToDoList {
    /**
     * Construct a ToDo handler.
     * @param name descriptive name for list
     * @param mode access mode
     * @param handle handle of the list
     */
    ToDoListImpl(String name, int mode, Object handle) {
        super(PIM.TODO_LIST, name, mode, handle);
    }

    /**
     * Creates a ToDo entry.
     * @return ToDo entry
     */
    public ToDo createToDo() {
        return new ToDoImpl(this);
    }

    /**
     * Initializes a ToDo entry from a previous item.
     * @param item template containing  input data
     * @return initialized ToDo entry
     */
    public ToDo importToDo(ToDo item) {
        return new ToDoImpl(this, item);
    }

    /**
     * Gets an Enumeration of the items in the list.
     * @param field identifier of field
     * @param startDate beginning of range
     * @param endDate end of range
     * @return Enumeration of matching events
     */
    public Enumeration items(int field, long startDate, long endDate)
            throws PIMException {
        if (getFieldDataType(field) != PIMItem.DATE) {
            throw new IllegalArgumentException("Not a DATE field");
        }
        if (endDate < startDate) {
            throw new IllegalArgumentException("Start date"
                + " must precede end date");
        }
        Vector results = new Vector();
        Vector keys = new Vector();
        for (Enumeration e = items(); e.hasMoreElements(); ) {
            ToDo item = (ToDo) e.nextElement();
            int indices = item.countValues(field);
            for (int i = 0; i < indices; i++) {
                long date = item.getDate(field, i);
                if (date >= startDate && date <= endDate) {
                    // include result
                    KeySortUtility.store(keys, results, date, item);
                    break;
                }
            }
        }
        return results.elements();
    }

    /**
     * Removes an entry from a ToDo list.
     * @param item the entry to be removed
     * @throws PIMException if the item is not int the list
     */
    public void removeToDo(ToDo item) throws PIMException {
        removeItem(item);
    }

    /**
     * Verifies read permission.
     *
     * @param action description of the operation
     * @throws SecurityException if operation is not permitted
     */
    protected void checkReadPermission(String action)
        throws SecurityException {
        super.checkReadPermission();
        checkPermission(PIMPermission.TODO_READ, action);
    }

    /**
     * Verifies write permission.
     *
     * @param action description of the operation
     * @throws SecurityException if operation is not permitted
     */
    protected void checkWritePermission(String action)
        throws SecurityException {
        super.checkWritePermission();
        checkPermission(PIMPermission.TODO_WRITE, action);
    }
}
