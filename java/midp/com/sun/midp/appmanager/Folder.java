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

package com.sun.midp.appmanager;

import javax.microedition.lcdui.Image;

/**
 * Represents Folder object.
 */
class Folder {
    /**
     * Constructor.
     *
     * @param id folder ID
     * @param parentId parent folder ID
     * @param name folder's name
     * @param icon folder's icon
     */
    Folder(int id, int parentId, String name, Image icon) {
    }

    /**
     * Returns this folder's ID.
     *
     * @return ID of this folder
     */
    public int getId() {
        return -1;
    }

    /**
     * Returns ID of this folder's parent.
     *
     * @return ID of the parent folder
     */
    public int getParentId() {
        return -1;
    }

    /**
     * Returns a name of this folder.
     *
     * @return name this folder
     */
    public String getName() {
        return "";
    }

    /**
     * Returns an icon should be shown for this folder.
     *
     * @return icon of this folder
     */
    public Image getIcon() {
        return null;
    }
}
