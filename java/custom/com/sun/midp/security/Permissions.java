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

package com.sun.midp.security;

import java.util.Hashtable;
import java.util.Vector;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import java.io.IOException;

/**
 * This class is a standard list of permissions that
 * a suite can do and is used by all internal security
 * features. This class also builds a list of permission for each
 * security domain. This only class that would need to be updated in order to
 * add a new security domain.
 */
public final class Permissions {

    /** Name of the MIDP permission. */
    public static final String MIDP_PERMISSION_NAME = "com.sun.midp";

    /** Name of the AMS permission. */
    public static final String AMS_PERMISSION_NAME = "com.sun.midp.ams";

    /** Binding name of the Manufacturer domain. (all permissions allowed) */
    public static final String MANUFACTURER_DOMAIN_BINDING = "manufacturer";

    /** Binding name of the Operator domain. */
    public static final String OPERATOR_DOMAIN_BINDING = "operator";

    /** Binding name of the Third party Identified domain. */
    public static final String IDENTIFIED_DOMAIN_BINDING =
            "identified_third_party";

    /** Binding name of the Third party Unidentified domain. */
    public static final String UNIDENTIFIED_DOMAIN_BINDING =
            "unidentified_third_party";

    /**
     * Binding name of the Minimum domain for testing.
     * (all permissions denied)
     */
    public static final String MINIMUM_DOMAIN_BINDING = "minimum";

    /**
     * Binding name of the Maximum domain for testing.
     * (all public permissions allowed)
     */
    public static final String MAXIMUM_DOMAIN_BINDING = "maximum";

    /**
     * The maximum levels are held in the first element of the permissions
     * array.
     */
    public static final int MAX_LEVELS = 0;

    /**
     * The current levels are held in the first element of the permissions
     * array.
     */
    public static final int CUR_LEVELS = 1;

    /** com.sun.midp permission ID. */
    public static final int MIDP = 0;

    /** com.sun.midp.ams permission ID. */
    public static final int AMS = 1;

    /** Never allow the permission. */
    public static final byte NEVER = 0;

    /** Allow an permission with out asking the user. */
    public static final byte ALLOW = 1;

    /**
     * Permission granted by the user until the the user changes it in the
     * settings form.
     */
    public static final byte BLANKET_GRANTED = 2;

    /**
     * Allow a permission to be granted or denied by the user
     * until changed in the settings form.
     */
    public static final byte BLANKET = 4;

    /** Allow a permission to be granted only for the current session. */
    public static final byte SESSION = 8;

    /** Allow a permission to be granted only for one use. */
    public static final byte ONESHOT = 16;

    /**
     * Permission denied by the user until the user changes it in the
     * settings form.
     */
    public static final byte BLANKET_DENIED = -128;

    /** Permission to group map table. */
    static PermissionSpec[] permissionSpecs = null;

    /** Number of permissions. */
    public static int NUMBER_OF_PERMISSIONS;

    /**
     * Get the name of a permission.
     *
     * @param permission permission number
     *
     * @return permission name
     *
     * @exception SecurityException if the permission is invalid
     */
    public static String getName(int permission) {
        // The code that uses this method doesn't actually use the return value, but
        // passes it to Permissions.getId. So we can return anything.
        return "com.sun.midp";
    }

    /**
     * Get the dialog title for a permission.
     *
     * @param permission permission number
     *
     * @return Resource constant for the permission dialog title
     * @exception SecurityException if the permission is invalid
     */
    public static String getTitle(int permission) {
        return "Title";
    }

    /**
     * Get the question for a permission.
     *
     * @param permission permission number
     *
     * @return Resource constant for the permission question
     *
     * @exception SecurityException if the permission is invalid
     */
    public static String getQuestion(int permission) {
        return "Question";
    }

    /**
     * Get the oneshot question for a permission.
     *
     * @param permission permission number
     *
     * @return Resource constant for the permission question
     *
     * @exception SecurityException if the permission is invalid
     */
    public static String getOneshotQuestion(int permission) {
        return "Oneshot Question";
    }

    /**
     * Get the ID of a permission.
     *
     * @param name permission name
     *
     * @return permission ID
     *
     * @exception SecurityException if the permission is invalid
     */
    public static int getId(String name) {
        // Returns the ID of the permission. The callers will use this ID to check the
        // permission in the permissions array returned by Permissions::forDomain.
        return 0;
    }

    /**
     * Determine if a domain is a trusted domain.
     *
     * @param domain Binding name of a domain
     *
     * @return true if a domain is trusted, false if not
     */
    public static boolean isTrusted(String domain) {
        // Always return true to make Java think the MIDlet domain is trusted.
        // We rely on the web security model, so we don't need to ask permissions
        // to the user.
        return true;
    }

    /**
     * Returns domain for unsigned midlets.
     *
     * @return domain name
     */
    public static String getUnsignedDomain() {
        return UNIDENTIFIED_DOMAIN_BINDING;
    }

    /**
     * Create a list of permission groups a domain is permitted to perform.
     *
     * @param name binding name of domain
     *
     * @return 2 arrays, the first containing the maximum level for each
     *     permission, the second containing the default or starting level
     *     for each permission supported
     */
    public static byte[][] forDomain(String name) {
        // The 2 additional permissions are the two hardcoded MIPS and AMS permissions.
        NUMBER_OF_PERMISSIONS = PermissionsStrings.PERMISSION_STRINGS.length + 2;

        byte[] maximums = new byte[NUMBER_OF_PERMISSIONS];
        byte[] defaults = new byte[NUMBER_OF_PERMISSIONS];
        byte[][] permissions = {maximums, defaults};

        for (int i = 0; i < NUMBER_OF_PERMISSIONS; i++) {
            maximums[i] = ALLOW;
            defaults[i] = ALLOW;
        }

        return permissions;
    }

    /**
     * Create an empty list of permission groups.
     *
     * @return array containing the empty permission groups
     */
    public static byte[] getEmptySet() {
        byte[] permissions = new byte[NUMBER_OF_PERMISSIONS];

        // Assume perms array is non-null
        for (int i = 0; i < permissions.length; i++) {
            // This is default permission
            permissions[i] = Permissions.NEVER;
        }

        return permissions;
    }

    /**
     * Get a list of all permission groups for the settings dialog.
     *
     * @return array of permission groups
     */
    static PermissionGroup[] getSettingGroups() {
        return new PermissionGroup[0];
    }

    static native byte      getMaxValue(String domain, String group);
    static native byte      getDefaultValue(String domain, String group);
}

/** Specifies a permission name and its group. */
class PermissionSpec {
    /** Name of permission. */
    String name;

    /** Group of permission. */
    PermissionGroup group;

    /**
     * Construct a permission specification.
     *
     * @param theName Name of permission
     * @param theGroup Group of permission
     */
    PermissionSpec(String theName, PermissionGroup theGroup) {
        name = theName;
        group = theGroup;
    }
}
