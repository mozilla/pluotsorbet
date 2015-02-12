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

package com.sun.cdc.io.j2me.file;

import java.util.Vector;
import javax.microedition.io.file.FileSystemRegistry;

/**
 * Caches file system roots.
 */
public class RootCache {
    /** Current root paths. */
    private String[] roots = null;

    /** Reference to the root cache. */
    private static RootCache instance = null;


    /** Constructs the root cache instance. */
    private RootCache() {
        // retrieve up-to-date mounted roots; array can not be null
        Vector v = Protocol.listRoots();
        roots = new String[v.size()];
        v.copyInto(roots);
    }
    
    /**
     * Creates instance of root cache and fills it with current root paths.
     */
    public static synchronized void initialize() {
        getInstance();
    }

    /**
     * Gets reference to the root cache.
     * If an instance does not exist then creates it,
     * otherwise returns existing one.
     * @return root cache reference
     */
    public static synchronized RootCache getInstance() {
        if (instance == null) {
            instance = new RootCache();
        }
        return instance;
    }

    /**
     * Gets the list of cached roots.
     * @return array of cached file system roots
     */
    public synchronized String[] getRoots() {
        return roots;
    }

    /**
     * Checks if path is a root path.
     * @param root path to be checked
     * @return <code>true</code> if path is a root
     */
    public synchronized boolean isRoot(String root) {
        for (int i = 0; i < roots.length; i++) {
            if (root.equals(roots[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a root to the cache.
     * @param root path to add to roots
     */
    public synchronized void addRoot(String root) {
        String[] a = new String[roots.length + 1];
        System.arraycopy(roots, 0, a, 0, roots.length);
        a[roots.length] = root;
        roots = a;
    }

    /**
     * Removes a root from the cache.
     * @param root  path to be removed
     */
    public synchronized void removeRoot(String root) {
        int index = -1;
        for (int i = 0; i < roots.length && index == -1; i++) {
            if (root.equals(roots[i])) {
                index = i;
            }
        }
        if (index != -1) {
            String[] a = new String[roots.length - 1];
            System.arraycopy(roots, 0, a, 0, index);
            System.arraycopy(roots, index + 1, a, index, a.length - index);
            roots = a;
        }
    }

}
