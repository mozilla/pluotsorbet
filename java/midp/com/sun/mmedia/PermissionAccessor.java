/*
 *  Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License version
 *  2 only, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License version 2 for more details (a copy is
 *  included at /legal/license.txt).
 *
 *  You should have received a copy of the GNU General Public License
 *  version 2 along with this work; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 *  Clara, CA 95054 or visit www.sun.com if you need additional
 *  information or have any questions.
 */
package com.sun.mmedia;

/**
 * A manager class for platform/product specific permission management.
 */
public final class PermissionAccessor {
    
    public static final int PERMISSION_SYSTEM       = 0;
    
    public static final int PERMISSION_HTTP_READ    = 1;
    public static final int PERMISSION_HTTP_WRITE   = 2;
    public static final int PERMISSION_FILE_READ    = 3;
    public static final int PERMISSION_FILE_WRITE   = 4;
    public static final int PERMISSION_SOCKET_READ  = 5;
    public static final int PERMISSION_SOCKET_WRITE = 6;
    
    public static final int PERMISSION_SNAPSHOT     = 7;
    public static final int PERMISSION_RECORDING    = 8;

    public static final int PERMISSION_HTTPS_READ   = 9;
    public static final int PERMISSION_HTTPS_WRITE  = 10;
    
    /**
     * Method indended to be called by Players & Controls to check
     * if user application has enough permissions to perform
     * a secured operation ...
     *
     *
     * @param locator - Locator name.
     * @param thePermission - one of PERMISSION_* constants that 
     *        define permissions in an product-independent form.
     */
    public static void checkPermissions(String locator, int thePermission) throws SecurityException, InterruptedException {
        /* 
         * IMPL_NOTE: Choose appropriate PermissionAccessorPlatform
         */
        PermissionAccessorAbstractions.checkPermissions(locator, thePermission);
    }
    
    private static final String locatorTypes[] = {
        "capture://audio",
        "capture://video",
        "capture://radio",
        "capture://",
        "device://",
        "file://",
        "http://",
        "https://",
    };
    
    // inidicates that corresponding locator type needs no special permissions.
    private static final int NEED_NO_PERMISSIONS = -2;
    private static final int FAILED_PERMISSIONS  = -1;
            
    private static final int mapLocatorPermissions[] = {
        /* "capture://audio" */ NEED_NO_PERMISSIONS,
        /* "capture://video" */ NEED_NO_PERMISSIONS,
        /* "capture://radio" */ NEED_NO_PERMISSIONS,
        /* "capture://"      */ NEED_NO_PERMISSIONS,
        /* "device://"       */ NEED_NO_PERMISSIONS,
        /* "file://"         */ PERMISSION_FILE_READ,
        /* "http://"         */ PERMISSION_HTTP_READ,
        /* "https://"        */ PERMISSION_HTTPS_READ
    };
    
    /**
     * Method indended to be called by Manager.createDataSource(locator)
     * and checks if user application has enough permissions to use given type
     * of locators to play media contents.
     *
     * @param locator - the URL to be used as media source for player
     */
    public static void checkLocatorPermissions(String locator) throws SecurityException {
        int permission = FAILED_PERMISSIONS;
        try {
            /* 
             * Find Locator type, and map this type to permission.
             * Any incorrect locator will result in 
             * ArrayIndexOutOfBoundsException or NullPointerException -> 
             * a SecurityException will be thrown !
             */
            String locStr = locator.toLowerCase();
            for (int i = 0; i < locatorTypes.length; ++i) {
                if (locStr.startsWith(locatorTypes[i])) {
                    permission = mapLocatorPermissions[i];
                    if (permission == NEED_NO_PERMISSIONS)
                        return; 
                    break;
                }
            }
            
            checkPermissions(locator, permission);
        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            throw new SecurityException(
                "Failed to check locator permission");
        }
    }

    /**
     * Method indended to be called by Manager.createPlayer(DataSource)
     * and checks if user application has enough permissions to playback 
     * media of a given content-type using given type
     * of locators.
     *
     * @param locator - the URL to be used as media source for player, 
     *        can be null if DataSOurce has been created not from locator
     * @param contentType - content-type boolean of the media
     */
    public static void checkContentTypePermissions(
            String locator, String contentType) throws SecurityException {
        /*
         * THIS IS A STUB
         */
    }
}
