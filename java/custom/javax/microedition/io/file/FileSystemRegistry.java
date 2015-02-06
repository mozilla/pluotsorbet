/*
 *   
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*
 * Copyright (C) 2002-2003 PalmSource, Inc.  All Rights Reserved.
 */

package javax.microedition.io.file;

import com.sun.cdc.io.j2me.file.RootCache;
import com.sun.cdc.io.j2me.file.Protocol;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.j2me.security.FileConnectionPermission;
import com.sun.j2me.app.AppPackage;
import java.util.NoSuchElementException;

/**
 * This class is defined by the JSR-75 specification
 * <em>PDA Optional Packages for the J2ME&trade; Platform</em>
 */
// JAVADOC COMMENT ELIDED
public class FileSystemRegistry {

    /** Currently registered listeners. */
    private static Vector fileSystemListeners = new Vector(2);

    /**
     * Determines whether internal filesystem events listener
     * is created and registered.
     */
    private static boolean isListenerRegistered = false;

    /** Constructor. */
    FileSystemRegistry() {
    }

    // JAVADOC COMMENT ELIDED
    public static boolean addFileSystemListener(FileSystemListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        // checkReadPermission();

        // Create and register file system events listener
        // (if there is no registered yet)
        if (!isListenerRegistered) {
            // Create root cache object and fill it's internal cache with
            // currently mounted roots.
            // Cache is used to determine which roots were mounted/unmounted
            // if EventTypes.FC_DISKS_CHANGED_EVENT event arrives.
            RootCache.initialize();

            FileSystemEventHandler.setListener(new FileSystemEventHandler());
            isListenerRegistered = true;
        }

        fileSystemListeners.addElement(listener);

        return true;
    }

    // JAVADOC COMMENT ELIDED
    public static boolean removeFileSystemListener(
            FileSystemListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return fileSystemListeners.removeElement(listener);
    }

    // JAVADOC COMMENT ELIDED
    public static Enumeration listRoots() {
        // checkReadPermission();
        // retrieve up-to-date list of mounted roots
        return Protocol.listRoots().elements();
    }

    /**
     * Gets a list of cached file system roots without checking permissions.
     * @return Enumeration of roots
     */
    static Enumeration listCachedRoots() {
        /** List of file system roots. */
        return new Enumeration() {
            /** Array of root pathnames. */
            String[] roots = RootCache.getInstance().getRoots();
            /** Current index int the enumeration. */
            int index = 0;
            /**
              * Checks if more data available.
              * @return <code>true</code> if more
              * elements available.
              */
            public boolean hasMoreElements() {
                return index < roots.length;
            }
            /**
              * Gets the next element.
              * @return next object in list
              */
            public Object nextElement() {
                try {
                    return roots[index++];
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    /**
     * Adds a root to the cache.
     * @param root path to add to roots
     */
    static synchronized void addRoot(String root) {
        RootCache cache = RootCache.getInstance();
        if (!cache.isRoot(root)) {
            cache.addRoot(root);
            notifyListeners(FileSystemListener.ROOT_ADDED, root);
        }
    }

    /**
     * Removes a root from the cache.
     * @param root  path to be removed
     */
    static synchronized void removeRoot(String root) {
        RootCache cache = RootCache.getInstance();
        if (cache.isRoot(root)) {
            cache.removeRoot(root);
            notifyListeners(FileSystemListener.ROOT_REMOVED, root);
        }
    }

    /**
     * Notify registered listeners about mount/unmount event.
     * @param event root added or removed event
     * @param root pathname of the root file system
     */
    private static void notifyListeners(int event, String root) {
        for (int i = 0; i < fileSystemListeners.size(); i++) {
            try {
                ((FileSystemListener)fileSystemListeners.elementAt(i)).
                    rootChanged(event, root);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

    /**
     * Checks the read permission.
     * @throws SecurityException if read is not allowed
     */
    private static void checkReadPermission() {
        AppPackage app = AppPackage.getInstance();

        try {
            app.checkForPermission(FileConnectionPermission.READ);
        } catch (InterruptedException ie) {
            throw new SecurityException(
                "Interrupted while trying to ask the user permission");
        }
    }
}
