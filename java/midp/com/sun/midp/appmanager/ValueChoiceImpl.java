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

package com.sun.midp.appmanager;

import com.sun.midp.security.PermissionGroup;

import java.util.Vector;


/**
 *  Class contains set of pairs (ID, label)
 *  and one entity market as selected.
 *  It can be used as a data for exclusive
 *  option buttons where each option has an ID.
 */
class ValueChoiceImpl implements ValueChoice {

    /** Choice title. */
    private String title;

    /** Keeps track of the choice IDs. */
    private Vector ids;

    /** Choice lables. */
    private Vector labels;

    /** Id of selected item. */
    private int selectedID;

    /** Correspondent permission group. */
    private PermissionGroup permissionGroup;

    /** ID of the permission group */
    private int permissionGroupID;

    /**
     * Creates empty ValueChoice
     * @param title of the choice
     */
    ValueChoiceImpl(PermissionGroup permissionGroup,
                    int permissionGroupID, String title) {
        this.title = title;
        ids = new Vector(5);
        labels = new Vector(5);
        this.permissionGroup = permissionGroup;
        this.permissionGroupID = permissionGroupID;
    }

    /**
     * Appends choice to the set.
     *
     * @param label the lable of the element to be added
     * @param id ID for the item
     */
    void append(String label, int id) {
        ids.addElement(new Integer(id));
        labels.addElement(label);
    }

    /**
     * Set the selected item.
     *
     * @param id ID of selected item
     */
    void setSelectedID(int id) {
        selectedID = id;
    }

    /**
     * Checks if specified ID exists in the list
     * @param id to find
     * @return true if it is present, false otherwise
     */
    boolean idExists(int id) {
        Integer ID = new Integer(id);
        for (int i = 0; i < ids.size(); i++) {
            if (ID.equals(ids.elementAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the ID of the selected item.
     *
     * @return ID of selected element
     */
    public int getSelectedID() {
        return selectedID;
    }

    /**
     * Returns ID of specified item.
     * @param index item index
     * @return item ID
     */
    public int getID(int index) {
        return ((Integer)ids.elementAt(index)).intValue();
    }

    /**
     * Returns label of cpecified choice items.
     * @param index item index
     * @return label
     */
    public String getLabel(int index) {
        return (String)labels.elementAt(index);
    }

    /**
     * Returns count of items
     * @return count
     */
    public int getCount() {
        return ids.size();
    }

    /**
     * Returns choice title.
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns corresponding permission group.
     * @return permission group
     */
    public PermissionGroup getPermissionGroup() {
        return permissionGroup;
    }

    /**
     * Returns corresponding permission group ID.
     * @return permission group ID
     */
    public int getPermissionGroupID() {
        return permissionGroupID;
    }

}
