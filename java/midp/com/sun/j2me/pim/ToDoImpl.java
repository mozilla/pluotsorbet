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

import com.sun.j2me.pim.formats.VCalendar10Format;
import javax.microedition.pim.PIM;
import javax.microedition.pim.ToDo;

/**
 * Implementation of a PIM ToDo.
 *
 */
public class ToDoImpl extends AbstractPIMItem implements ToDo {
    /**
     * Constructs a ToDo list.
     * @param list template list
     */    
    public ToDoImpl(AbstractPIMList list) {
        super(list, PIM.TODO_LIST);
        if (list != null && !(list instanceof ToDoListImpl)) {
            throw new RuntimeException("Wrong list passed");
        }
    }
    
    /**
     * Constructs a ToDo list.
     * @param list template list
     * @param base ToDo entry
     */    
    ToDoImpl(AbstractPIMList list, ToDo base) {
        super(list, base);
        if (!(list instanceof ToDoListImpl)) {
            throw new RuntimeException("Wrong list passed");
        }
    }

    /**
     * Gets the encoding format.
     * @return handle to format implementation
     */    
    PIMFormat getEncodingFormat() {
        return new VCalendar10Format();
    }
    /**
     * Checks if field is supported.
     * @param field identifier for field
     * @return <code>true</code> if field is supported
     */    
    static boolean isValidPIMField(int field) {
        switch (field) {
            case ToDo.CLASS:
            case ToDo.COMPLETED:
            case ToDo.COMPLETION_DATE:
            case ToDo.DUE:
            case ToDo.NOTE:
            case ToDo.PRIORITY:
            case ToDo.REVISION:
            case ToDo.SUMMARY:
            case ToDo.UID:
                return true;
            default:
                return false;
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
     * Converts the ToDo record to a printable format.
     * @return formatted ToDo record
     */
    protected String toDisplayableString() {
        return "ToDo[" + formatData() + "]";
    }

}
