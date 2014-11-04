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

import com.sun.j2me.app.AppPackage;
import com.sun.j2me.security.Permission;
import com.sun.j2me.security.MMAPIPermission;
import com.sun.j2me.security.FileConnectionPermission;
import com.sun.j2me.security.ConnectorPermission;

/**
 * A Wrapper class for platform/product specific permission management.
 * This file contains permission checker, which is based on abstractions.
 */
final class PermissionAccessorAbstractions {
    
    /**
     * Method indended to be called by PermissionAccessor to check
     * if user application has enough permissions to perform
     * a secured operation ...
     *
     * @param thePermission - one of PERMISSION_* constants that
     *        define permissions in an product-independent form.
     */
    public static void checkPermissions(String locator, int thePermission) throws SecurityException, InterruptedException {
        try {
            Permission permission = null;
            
            switch(thePermission) {
                case PermissionAccessor.PERMISSION_SNAPSHOT:
                    permission = new MMAPIPermission(
                            MMAPIPermission.SNAPSHOT.getName(), locator);
                    break;
                case PermissionAccessor.PERMISSION_RECORDING:
                    permission = new MMAPIPermission(
                            MMAPIPermission.RECORDING.getName(), locator);
                    break;
                case PermissionAccessor.PERMISSION_FILE_READ:
                    permission = new FileConnectionPermission(
                            FileConnectionPermission.READ.getName(), locator);
                    break;
                case PermissionAccessor.PERMISSION_FILE_WRITE:
                    permission = new FileConnectionPermission(
                            FileConnectionPermission.WRITE.getName(), locator);
                    break;
                    
                case PermissionAccessor.PERMISSION_HTTP_READ:
                case PermissionAccessor.PERMISSION_HTTP_WRITE:
                    permission = new ConnectorPermission(
                            ConnectorPermission.HTTP.getName(), locator);
                    break;
                    
                case PermissionAccessor.PERMISSION_HTTPS_READ:
                case PermissionAccessor.PERMISSION_HTTPS_WRITE:
                    permission = new ConnectorPermission(
                            ConnectorPermission.HTTPS.getName(), locator);
                    break;
                    
                case PermissionAccessor.PERMISSION_SOCKET_READ:
                case PermissionAccessor.PERMISSION_SOCKET_WRITE:
                    permission = new ConnectorPermission(
                            ConnectorPermission.TCP.getName(), locator);
                    break;
                    
                default:
                    throw new SecurityException("Failed to check user permission");
            }
            
            AppPackage appPackage = AppPackage.getInstance();
            appPackage.checkForPermission(permission);
            
        } catch (SecurityException se) {
            ///*DEBUG:*/ se.printStackTrace();
            throw se;
        } catch (Exception e) {
            ///*DEBUG:*/ e.printStackTrace();
            throw new SecurityException(
                    "Failed to check user permission");
        }
    }
}
