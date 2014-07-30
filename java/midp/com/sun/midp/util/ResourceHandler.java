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
package com.sun.midp.util;

import javax.microedition.io.Connector;
import com.sun.midp.io.j2me.storage.File;
import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;
import com.sun.midp.configurator.Constants;
import com.sun.j2me.security.AccessController;

/**
 * The ResourceHandler class is a system level utility class.
 * Its purpose is to return system resources as an array of bytes
 * based on a unique String identifier. All methods in this utility
 * class should be protected through use of the SecurityToken.
 */
public class ResourceHandler {
    /**
     * Load a resource from the system and return it as a byte array.
     * This method is used to load AMS icons.
     *
     * @param token the SecurityToken to use to grant permission to
     *              execute this method.
     * @param resource a String identifier which can uniquely describe
     *                 the location of the resource to be loaded.
     * @return a byte[] containing the resource retrieved from the
     *         system. null if the resource could not be found.
     */
    public static byte[] getAmsResource(SecurityToken token,
                                        String resource) {
        return getResourceImpl(token, File.getStorageRoot(
            Constants.INTERNAL_STORAGE_ID) + resource);
    }

    /**
     * Load a resource from the system and return it as a byte array.
     * This method is used to load system level resources, such as
     * images, sounds, properties, etc.
     *
     * @param token the SecurityToken to use to grant permission to
     *              execute this method.
     * @param resource a String identifier which can uniquely describe
     *                 the location of the resource to be loaded.
     * @return a byte[] containing the resource retrieved from the
     *         system. null if the resource could not be found.
     */
    public static byte[] getSystemResource(SecurityToken token,
                                           String resource) {
        return getResourceImpl(token, File.getConfigRoot(
            Constants.INTERNAL_STORAGE_ID) + resource);
    }

    /**
     * Load a system image resource from the system and return it as 
     * a byte array. The images are stored in the configuration 
     * directory ($MIDP_HOME/lib).
     *
     * @param token the SecurityToken to use to grant permission to
     *              execute this method.
     * @param imageName name of the image
     * @return a byte[] containing the resource retrieved from the
     *         system. null if the resource could not be found.
     * @throws IllegalArgumentException if imageName contains a "/" or "\\",
     *        or imageName is null or imageName is empty
     */
    public static byte[] getSystemImageResource(SecurityToken token, 
                    String imageName) {
        byte[] imageData = getAmsResource(token, imageName + ".raw");
        if (imageData == null) {
            imageData = getAmsResource(token, imageName + ".png");
        }
        
        return imageData;
    }   

    /**
     * Load a resource from the system and return it as a byte array.
     * This method is used to load system level resources, such as
     * images, sounds, properties, etc.
     *
     * @param token the SecurityToken to use to grant permission to
     *              execute this method.
     * @param resourceFilename full path to the file containing the resource.
     * @return a byte[] containing the resource retrieved from the
     *         system. null if the resource could not be found.
     */
    private static byte[] getResourceImpl(SecurityToken token,
            String resourceFilename) {
        if (token != null) {
            token.checkIfPermissionAllowed(Permissions.MIDP);
        } else {
            AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);
        }

        // converting the file name into the resource name
        int start = resourceFilename.lastIndexOf('/');
        if (start < 0) {
            start = resourceFilename.lastIndexOf('\\');
        }
        if (start < 0) {
            start = 0;
        } else {
            start++;
        }

        String resourceName = resourceFilename.substring(start,
                resourceFilename.length());
        resourceName = resourceName.replace('.', '_');
        //

        byte[] resourceBuffer = loadRomizedResource0(resourceName);

        if (resourceBuffer == null) {
            RandomAccessStream stream;
            if (token != null) {
                stream = new RandomAccessStream(token);
            } else {
                stream = new RandomAccessStream();
            }

            try {
                stream.connect(resourceFilename, Connector.READ);
                resourceBuffer = new byte[stream.getSizeOf()];
                stream.readBytes(resourceBuffer, 0, resourceBuffer.length);
            } catch (java.io.IOException e) {
                resourceBuffer = null;
            } finally {
                try {
                    stream.disconnect();
                } catch (java.io.IOException ignored) {
                }
            }
        }

        return resourceBuffer;
    }

    /**
     * Retrieves a romized resource with the given name.
     *
     * @param resourceName name of the resource to load
     *
     * @return requested resource as an array of bytes or NULL if not found
     */
    private static native byte[] loadRomizedResource0(String resourceName);
}

