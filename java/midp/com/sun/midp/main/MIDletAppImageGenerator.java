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

package com.sun.midp.main;

import com.sun.midp.midletsuite.MIDletSuiteStorage;
import com.sun.midp.configurator.Constants;

/**
 * This class is designed to provide the functionality
 * needed for generating the binary image from the MIDlet suite
 * classes.
 */
public class MIDletAppImageGenerator {

    /**
     * Creates new binary application image. If application image already exists
     * then it will be removed and replaced by new one.
     *
     * @param suiteId - suite ID
     * @param suiteStorage - suite storage
     * @return true if application image was generated successfully,
     *            false otherwise
     */
    public static boolean createAppImage(int suiteId,
                                         MIDletSuiteStorage suiteStorage) {
        if (!Constants.MONET_ENABLED) {
            throw new RuntimeException("Monet is disabled");
        }

        String binFile = suiteStorage.getMidletSuiteAppImagePath(suiteId);
        String suiteJar = suiteStorage.getMidletSuiteJarPath(suiteId);
        removeAppImage(binFile);
        return MIDletAppImageGeneratorBase.generateAppImage(
            suiteJar, binFile, 0);
    }

    /**
     * Removes the given application image.
     *
     * @param binFile path to the application image
     * @return true if application image was successfully removed,
     *            false otherwise
     */
    private static native boolean removeAppImage(String binFile);
}
