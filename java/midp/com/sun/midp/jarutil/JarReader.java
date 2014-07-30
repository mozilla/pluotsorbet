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

package com.sun.midp.jarutil;

import java.lang.String;

import java.io.IOException;

import com.sun.j2me.security.AccessController;

import com.sun.midp.io.Util;

import com.sun.midp.security.Permissions;

/**
 * This class provides a Java API for reading an entry from a Jar file stored
 * on the file system.
 */
public class JarReader {
    /**
     * Returns the content of the given entry in the JAR file on the
     * file system given by jarFilePath.
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * @param jarFilePath file pathname of the JAR file to read. May
     *          be a relative pathname.
     * @param entryName name of the entry to return.
     *
     * @return the content of the given entry in a byte array or null if
     *          the entry was not found
     *
     * @exception IOException if JAR is corrupt or not found
     * @exception IOException if the entry does not exist.
     * @exception SecurityException if the caller does not have permission
     *   to install software.
     */
    public static byte[] readJarEntry(String jarFilePath, String entryName)
            throws IOException {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        if (entryName.charAt(0) == '/') {
            /*
             * Strip off the leading directory separator, or the
             * resource will not be found in the JAR.
             */
            entryName = entryName.substring(1, entryName.length());
        }

        return readJarEntry0(jarFilePath, entryName);
    }

    /**
     * Performs the same function as readJarEntry.
     *
     * @param localJarFilePath file pathname of the JAR file to read. May
     *          be a relative pathname.
     * @param localEntryName name of the entry to return.
     *
     * @return the content of the given entry in a byte array or null if
     *         the entry was not found
     *
     * @exception IOException if JAR is corrupt or not found
     */
    private static native byte[] readJarEntry0(String localJarFilePath, 
                                       String localEntryName)
        throws IOException;
}
