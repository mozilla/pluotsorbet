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

import com.sun.midp.appmanager.FolderManager;
import com.sun.midp.appmanager.Folder;

import javax.microedition.lcdui.*;
import java.util.Vector;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

        
/**
 * The list of exisitng MIDlet suite folders.
 * Used in AppManagerUI and GraphicalInstaller.
 */
public class FolderList extends javax.microedition.lcdui.List {
    
   /**
    * vector of available folders.
    */
   private Vector folders;

   /**
    * Create and initialize a new Folder List.
    */
    FolderList() {
       super(Resource.getString(
                ResourceConstants.AMS_SELECT_FOLDER), Choice.IMPLICIT);
       this.folders = null;
       setFitPolicy(TEXT_WRAP_OFF);

       refresh();
    }


    private synchronized void refresh() {
        folders = FolderManager.getFolders();
        if (folders != null) {
            for (int i = 0;  i < folders.size(); i++) {
                Folder f = (Folder)folders.elementAt(i);
                append(f.getName(), f.getIcon());
            }
        }
    }

   /**
    * If folder list has any items.
    */
   synchronized boolean hasItems() {
       if (folders != null) {
           return folders.size() > 0;
       } else {
           return false;
       }
   }


    /**
     * gets currently selected folder or null if there is no selection
     *
     * @return folder
     */
    public synchronized Folder getSelectedFolder() {
        int ind = getSelectedIndex();
        if (ind != -1) {
            return (Folder)(folders.elementAt(ind));
        }
        return null;
    }

    /**
     * Select item that correcponds to passed folder id
     * @param folderId
     */
    public synchronized void setSelectedFolder(int folderId) {
        folders = FolderManager.getFolders();
        if (folders != null) {
            for (int i = 0;  i < folders.size(); i++) {
                Folder f = (Folder)folders.elementAt(i);
                if (f.getId() == folderId) {
                    this.setSelectedIndex(i, true);
                    break;
                }
            }
        }
    }

}
