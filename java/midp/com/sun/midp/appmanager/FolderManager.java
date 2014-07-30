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

import java.util.Vector;

/**
 * Provides functionality to manage folders.
 */
class FolderManager {
    /** Folder ID that is never used. */
    public static final int UNUSED_FOLDER_ID  = -1;

    /**
     * ID of the folder where user applications
     * will be installed by default.
     */
    public static final int DEFAULT_FOLDER_ID = 1;

    /**
     * Returns the number of folders present in the system
     *
     * @return number of folders present in the system
     */
    public static int getFolderCount() {
        return 0;
    }

    /**
     * Returns a vector of folders present in the system
     *
     * @return vector of <code>Folder</code> objects
     */
    public static Vector getFolders() {
        return null;
    }

    /**
     * Returns an ID of the folder where user applications
     * are installed by default. 
     *
     * @return ID of the folder where user applications are installed by default
     */
    public static int getDefaultFolderId() {
        return DEFAULT_FOLDER_ID;
    }

    /**
     * Returns a folder with the given ID
     *
     * @param folderId ID of the folder to find
     *
     * @return <code>Folder</code> having the given ID or null if not found
     */
    public static Folder getFolderById(int folderId) {
        return null;
    }

    /**
     * Creates a new folder.
     *
     * @param f folder to create
     */
    public static void createFolder(Folder f) {
    }

    /**
     * Removes a folder.
     *
     * @param f folder to delete
     */
    public static void deleteFolder(Folder f) {
    }
}
