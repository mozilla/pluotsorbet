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

    /** Third Party Never permission group. */
    static final PermissionGroup NEVER_GROUP =
        new PermissionGroup("", null, null, null, null, null);

    /** Permission to group map table. */
    static PermissionSpec[] permissionSpecs = null;

    /** Number of permissions. */
    public static int NUMBER_OF_PERMISSIONS;

    /* list of domains */
    private static DomainPolicy[] domainsAll = null;

    /* The domain name for unsigned midlets */
    private static String unsignedDomain = null;

    /* Permissions index lookup table */
    private static Hashtable permissionsHash;

    /* list of groups */
    private static PermissionGroup[] groupsAll = null;
    
    // Internal defined groups
    private static PermissionGroup NET_ACCESS_GROUP;
    private static PermissionGroup LOW_LEVEL_NET_ACCESS_GROUP;
    private static PermissionGroup CALL_CONTROL_GROUP;
    private static PermissionGroup SEND_MESSAGE_GROUP;
    private static PermissionGroup READ_MESSAGE_GROUP;
    private static PermissionGroup SEND_RESTRICTED_MESSAGE_GROUP;
    private static PermissionGroup READ_RESTRICTED_MESSAGE_GROUP;
    private static PermissionGroup AUTO_INVOCATION_GROUP;
    private static PermissionGroup READ_USER_DATA_GROUP;
    private static PermissionGroup MULTIMEDIA_GROUP;
    private static PermissionGroup LOCAL_CONN_GROUP;
    /** artificially constructed group for Push Interrupt Group. */
    private static PermissionGroup PUSH_INTERRUPT_GROUP;

    /**
     * Retuns artificially constructed group for Push Interrupt Group
     * @return Push Interrupt Group
     */
    public static PermissionGroup getPushInterruptGroup() {
        if (PUSH_INTERRUPT_GROUP == null) {
            PUSH_INTERRUPT_GROUP = new PermissionGroup(
                Resource.getString(ResourceConstants.AMS_MGR_INTRUPT),
                Resource.getString(ResourceConstants.AMS_MGR_INTRUPT_QUE),
                Resource.getString(ResourceConstants.AMS_MGR_INTRUPT_QUE_DONT),
                Resource.getString(ResourceConstants.PERMISSION_AUTO_START_DIALOG_TITLE),
                Resource.getString(ResourceConstants.PERMISSION_AUTO_START_QUE),
                null);
        }
        return PUSH_INTERRUPT_GROUP;
    }

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
     * Checks if group is read message group
     * @param group
     * @return returns true if group is read message group, false otherwise
     */
    public static boolean isReadMessageGroup(PermissionGroup group) {
        if (group == null) {
            return false;
        }
        
        if (group == READ_MESSAGE_GROUP ||
                group == READ_RESTRICTED_MESSAGE_GROUP) {
            return true;
        }
        return false;
    }


    /**
     * Returns domain for unsigned midlets.
     *
     * @return domain name
     */
    public static String getUnsignedDomain() {
        return unsignedDomain;
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
        return groupsAll;
    }

    /**
     * Get a list of all permission groups for the settings dialog.
     *
     * @param levels current permission levels
     * 
     * @return array of permission groups
     */
    public static PermissionGroup[] getSettingGroups(byte[] levels) {
        PermissionGroup[] groups = null;

        if (groupsAll != null) {
            // merge two SEND/READ_[RESTRICTED_]MESSAGE_GROUP groups into one
            // for each of two pairs (messaging/restricted messaging)
            groups = new PermissionGroup[groupsAll.length - 2];
            int n = 0;

            for (int i = 0; i < groupsAll.length; i++) {
                if (groupsAll[i] == READ_MESSAGE_GROUP ||
                    groupsAll[i] == READ_RESTRICTED_MESSAGE_GROUP) {
                    continue;
                } if (groupsAll[i] == SEND_MESSAGE_GROUP) {
                    if (getPermissionGroupLevel(levels,
                                                SEND_MESSAGE_GROUP) != NEVER) {
                        groups[n++] = SEND_MESSAGE_GROUP;
                    } else {
                        groups[n++] = READ_MESSAGE_GROUP;
                    }
                } else if (groupsAll[i] == SEND_RESTRICTED_MESSAGE_GROUP) {
                    if (getPermissionGroupLevel(levels,
                            SEND_RESTRICTED_MESSAGE_GROUP) != NEVER) {
                        groups[n++] = SEND_RESTRICTED_MESSAGE_GROUP;
                    } else {
                        groups[n++] = READ_RESTRICTED_MESSAGE_GROUP;
                    }
                } else {
                    groups[n++] = groupsAll[i];
                }
            }
        }

        return groups;
    }

    /**
     * Find the max level of all the permissions in the same group.
     *
     * This is a policy dependent function for permission grouping.
     *
     * @param levels array of permission levels
     * @param group desired permission group
     * @param currentLevels true if the levels is the current permission levels,
     *                      false if the levels is an array of maximum allowed
     *                      permission levels.
     *
     * @return permission level
     */
    private static byte getPermissionGroupLevelImpl(byte[] levels,
            PermissionGroup group, boolean currentLevels) {
        byte maxLevel = NEVER;

        for (int i = 0; i < permissionSpecs.length; i++) {
            if (permissionSpecs[i].group == group && levels[i] != NEVER) {
                /*
                 * Except for NEVER the lower the int value the higher
                 * the permission level.
                 */
                if (levels[i] < maxLevel || maxLevel == NEVER) {
                    maxLevel = levels[i];
                }
            }
        }

        /**
         * For Read Message Group, consider group level is OneShot if maximum
         * permission level is Blanket, but not Blanket_granted, because it would
         * mean that the user explicitly set the interraction level either from
         * the application settings or by answering the corresponding question.
         */
        if ((group == READ_MESSAGE_GROUP ||
                group == READ_RESTRICTED_MESSAGE_GROUP) &&
                    (maxLevel == BLANKET)) {
            if (currentLevels) {
                maxLevel = ONESHOT;
            }
        }

        return maxLevel;
    }

    /**
     * Find the max level of all the current permissions in the group.
     *
     * This is a policy dependent function for permission grouping.
     *
     * @param levels array of permission levels
     * @param group desired permission group
     *
     * @return permission level
     */
    public static byte getPermissionGroupLevel(byte[] levels,
            PermissionGroup group) {
        return getPermissionGroupLevelImpl(levels, group, true);
    }

    /**
     * Find the max level of all the maximum allowed permissions in the group.
     *
     * This is a policy dependent function for permission grouping.
     *
     * @param levels array of permission levels
     * @param group desired permission group
     *
     * @return permission level
     */
    public static byte getMaximumPermissionGroupLevel(byte[] levels,
            PermissionGroup group) {
        return getPermissionGroupLevelImpl(levels, group, false);
    }

    /**
     * Set the level of all the permissions in the same group as this
     * permission to the given level.
     * <p>
     * This is a policy dependent function for permission grouping.</p>
     *
     * The following combinations of permissions are mutually exclusive:
     * <ul>
     * <li> Any of Net Access, Messaging or Local Connectivity set to Blanket
     *      in combination with any of Multimedia recording or Read User Data
     *      Access set to Blanket</li>
     * <li> Application Auto Invocation (or push interrupt level) set to
     *      Blanket and Net Access set to Blanket</li>
     * </ul>
     *
     * @param current current permission levels
     * @param pushInterruptLevel Push interrupt level
     * @param group desired permission group
     * @param level permission level
     *
     * @exception SecurityException if the change would produce a mutually
     *                              exclusive combination
     */
    public static void setPermissionGroup(byte[] current,
            byte pushInterruptLevel, PermissionGroup group, byte level)
                    throws SecurityException {

        PermissionGroup[] pg = checkForMutuallyExclusiveCombination(current,
                pushInterruptLevel, group, level);
        if (pg != null) {
            throw new SecurityException(
                    createMutuallyExclusiveErrorMessage(pg[0], pg[1]));
        }

        for (int i = 0; i < permissionSpecs.length; i++) {
            if (permissionSpecs[i].group == group) {
                setPermission(current, i, level);
            }
        }

        /*
         * For some reason specs do not want separate send and
         * receive message groups, but want the questions and interrupt
         * level to be different for send, so internally we have 2 groups
         * that must be kept in synch. The setting dialog only presents
         * the send message group, see the getSettingGroups method.
         */
        PermissionGroup readGroup = null;

        if (group == SEND_MESSAGE_GROUP) {
            readGroup = READ_MESSAGE_GROUP;
        } else if (group == SEND_RESTRICTED_MESSAGE_GROUP) {
            readGroup = READ_RESTRICTED_MESSAGE_GROUP;
        }

        if (readGroup != null) {
            /*
             * Since the send group have a max level of oneshot, this method
             * will only code get used by the settings dialog, when a user
             * changes the send group from blanket denied to oneshot.
             */
            if (level != BLANKET_DENIED) {
                /*
                 * If send is set to to any thing but blanket denied
                 * then receive is set to blanket.
                 */
                level = BLANKET_GRANTED;
            }

            for (int i = 0; i < permissionSpecs.length; i++) {
                if (permissionSpecs[i].group == readGroup) {
                    setPermission(current, i, level);
                }
            }

            return;
        }

        PermissionGroup sendGroup = null;

        if (group == READ_MESSAGE_GROUP) {
            sendGroup = SEND_MESSAGE_GROUP;
        } else if (group == READ_RESTRICTED_MESSAGE_GROUP) {
            sendGroup = SEND_RESTRICTED_MESSAGE_GROUP;
        }

        if (sendGroup != null) {
            if (level == ONESHOT) {
                for (int i = 0; i < permissionSpecs.length; i++) {
                    if (permissionSpecs[i].group == group) {
                        setPermission(current, i, BLANKET);
                    }
                }
            }

            /*
             * Keep both subgoups in synch when READ_[RESTRICTED_]MESSAGE_GROUP is
             * changed.
             */
            if (level != BLANKET_GRANTED) {
                for (int i = 0; i < permissionSpecs.length; i++) {
                    if (permissionSpecs[i].group == sendGroup) {
                        setPermission(current, i, level);
                    }
                }
            }
        }
    }

    /**
     * Grant or deny of a permission and all of the other permissions in
     * it group.
     * <p>
     * This is a policy dependent function for permission grouping.</p>
     *
     * This method must only be used when not changing the interaction level
     * (blanket, session, one shot).
     *
     * @param current current permission levels
     * @param permission permission ID from the group
     * @param level permission level
     * @exception SecurityException if the change would produce a mutually
     *                              exclusive combination
     */
    public static void setPermissionGroup(byte[] current, int permission,
            byte level) throws SecurityException {

        if (permission < 0 || permission >= permissionSpecs.length) {
            return;
        }

        PermissionGroup group = permissionSpecs[permission].group;

        setPermissionGroup(current, NEVER, group, level);
    }

    /**
     * Check to see if a given push interrupt level would produce a mutually
     * exclusive combination for the current security policy. If so, throw
     * an exception.
     * <p>
     * This is a policy dependent function for permission grouping.</p>
     *
     * The mutually combination is the push interrupt level set to Blanket and
     * Net Access set to Blanket.
     *
     * @param current current permission levels
     * @param pushInterruptLevel Push interrupt level
     *
     * @exception SecurityException if the change would produce a mutually
     *                              exclusive combination
     */
    public static void checkPushInterruptLevel(byte[] current,
            byte pushInterruptLevel) throws SecurityException {
        byte level;

        if (pushInterruptLevel != BLANKET_GRANTED) {
            return;
        }

        final PermissionGroup[] netGroups = {
            NET_ACCESS_GROUP, LOW_LEVEL_NET_ACCESS_GROUP
        };

        for (int i = 0; i < netGroups.length; i++) {
            level = getPermissionGroupLevel(current, netGroups[i]);
            if (level == BLANKET_GRANTED || level == BLANKET) {
                throw new SecurityException(createMutuallyExclusiveErrorMessage(
                    Resource.getString(ResourceConstants.AMS_MGR_INTRUPT),
                    netGroups[i].getName()));
            }
        }
    }

    /**
     * Check to see if a given push interrupt level would produce a mutually
     * exclusive combination for the current security policy. If so, throw
     * an exception.
     * <p>
     * This is a policy dependent function for permission grouping.</p>
     *
     * The mutually combination is the push interrupt level set to Blanket and
     * Net Access set to Blanket.
     *
     * @param current current permission levels
     * @param pushInterruptLevel Push interrupt level
     * @return mutually exclusive groups
     */
    public static PermissionGroup[] checkForMutuallyExclusiveCombination(byte[] current,
            byte pushInterruptLevel) {

        byte level;

        if (pushInterruptLevel != BLANKET_GRANTED) {
            return null;
        }

        level = getPermissionGroupLevel(current, NET_ACCESS_GROUP);
        if (level == BLANKET_GRANTED || level == BLANKET) {
            PermissionGroup[] ret = new PermissionGroup[2];
            ret[0] = PUSH_INTERRUPT_GROUP;
            ret[1] = NET_ACCESS_GROUP;
            return ret;
        }

        return null;
    }

    /**
     * Set the level the permission if the permission is not set to NEVER
     * or ALLOW.
     *
     * @param current current permission levels
     * @param permission permission ID for permission to set
     * @param level permission level
     */
    private static void setPermission(byte[] current, int permission,
                                      byte level) {
        if (current[permission] != NEVER || current[permission] != ALLOW) {
            current[permission] = level;
        }
    }

    /**
     * Check to see if a given level for a group would produce a mutually
     * exclusive combination for the current security policy. If so,
     * return mutually exclusive groups.
     * <p>
     * This is a policy dependent function for permission grouping.</p>
     *
     * The following combinations of permissions are mutually exclusive:
     * <ul>
     * <li> Any of Net Access, Messaging or Local Connectivity set to Blanket
     *      in combination with any of Multimedia recording or Read User Data
     *      Access set to Blanket</li>
     * <li> Application Auto Invocation set to Blanket and Net Access set to
     *      Blanket</li>
     * </ul>
     *
     * @param current current permission levels
     * @param pushInterruptLevel Push interrupt level
     * @param group desired permission group
     * @param newLevel permission level
     * @return mutually exclusive groups
     */
    public static PermissionGroup[] checkForMutuallyExclusiveCombination(byte[] current,
            byte pushInterruptLevel, PermissionGroup group, byte newLevel) {

        byte level;

        if (newLevel != BLANKET_GRANTED) {
            return null;
        }

        if (group == NET_ACCESS_GROUP || group == LOW_LEVEL_NET_ACCESS_GROUP) {
            if (pushInterruptLevel == BLANKET_GRANTED ||
                   pushInterruptLevel == BLANKET) {
                PermissionGroup[] ret = new PermissionGroup[2];
                ret[0] = group;
                ret[1] = PUSH_INTERRUPT_GROUP;
                return ret;
            }

            level = getPermissionGroupLevel(current, AUTO_INVOCATION_GROUP);
            if (level == BLANKET_GRANTED || level == BLANKET) {
                PermissionGroup[] ret = new PermissionGroup[2];
                ret[0] = group;
                ret[1] = AUTO_INVOCATION_GROUP;
                return ret;
            }

            return null;
        }

        if (group == AUTO_INVOCATION_GROUP) {

           final PermissionGroup[] netGroups = {
               NET_ACCESS_GROUP, LOW_LEVEL_NET_ACCESS_GROUP
           };

           for (int i = 0; i < netGroups.length; i++) {
               level = getPermissionGroupLevel(current, netGroups[i]);
               if (level == BLANKET_GRANTED || level == BLANKET) {
                   PermissionGroup[] ret = new PermissionGroup[2];
                   ret[0] = AUTO_INVOCATION_GROUP;
                   ret[1] = netGroups[i];
                   return ret;
               }
           }
        }
        
        return null;
    }

    /**
     * Check to see if a given level for a group would produce a potentially
     * dangerous combination for the current security policy. If so,
     * return a warning message, else - null.
     * <p>
     * This is a policy dependent function for permission grouping.</p>
     *
     * The following combinations of permissions are potentially dangerous:
     * <ul>
     * <li> Any of Net Access, Low Level Net Access, Messaging, Restricted Messaging,
     *      Call Control or Local Connectivity set to Blanket
     *      in combination with any of Multimedia recording or Read User Data
     *      Access set to Blanket</li>
     * </ul>
     *
     * @param current current permission levels
     * @param pushInterruptLevel Push interrupt level
     * @param group desired permission group
     * @param newLevel permission level
     *
     * @return warning message if the change would produce a potentially
     *         dangerous combination or null otherwise
     */
    public static String getInsecureCombinationWarning(byte[] current,
            byte pushInterruptLevel, PermissionGroup group, byte newLevel) {

        if (newLevel != BLANKET_GRANTED) {
            return null;
        }

        final PermissionGroup[] groups1 = {
            NET_ACCESS_GROUP, LOW_LEVEL_NET_ACCESS_GROUP,
            SEND_MESSAGE_GROUP, READ_MESSAGE_GROUP,
            SEND_RESTRICTED_MESSAGE_GROUP, READ_RESTRICTED_MESSAGE_GROUP,
            CALL_CONTROL_GROUP, LOCAL_CONN_GROUP    

        };
        final PermissionGroup[] groups2 = {
            MULTIMEDIA_GROUP, READ_USER_DATA_GROUP
        };
        /**
         * IMPL_NOTE: we will get warning only about first pair found.
         * If it is more than one pair, there should be more complex
         * warning created.
         */

        for (int i = 0; i < groups1.length; i++) {
            if (group == groups1[i]) {
                for (int j = 0; j < groups2.length; j++) {
                    byte level = getPermissionGroupLevel(current, groups2[j]);
                    if (level == BLANKET_GRANTED || level == BLANKET) {
                        return createInsecureCombinationWarningMessage(
                            group, groups2[j]);
                    }
                }
                return null;
            }
        }

        for (int i = 0; i < groups2.length; i++) {
            if (group == groups2[i]) {
                for (int j = 0; j < groups1.length; j++) {
                    byte level = getPermissionGroupLevel(current, groups1[j]);
                    if (level == BLANKET_GRANTED || level == BLANKET) {
                        return createInsecureCombinationWarningMessage(
                            group, groups1[j]);
                    }
                }
                return null;
            }
        }

        return null;
    }

    /**
     * Create a mutally exclusive permission setting error message.
     *
     * @param groupToSet Group that is to be set
     * @param blanketGroup The a mutually exclusive group that was set to
     *                     blanket
     *
     * @return Translated error message with both group names in it
     */
    private static String createMutuallyExclusiveErrorMessage(
            PermissionGroup groupToSet, PermissionGroup blanketGroup) {
        return createMutuallyExclusiveErrorMessage(groupToSet.getName(),
            blanketGroup.getName());
    }

    /**
     * Create a potentially dangerous permission setting warning message.
     *
     * @param groupToSet Group that is to be set
     * @param blanketGroup The a mutually exclusive group that was set to
     *                     blanket
     *
     * @return Translated error message with both group names in it
     */
    private static String createInsecureCombinationWarningMessage(
            PermissionGroup groupToSet, PermissionGroup blanketGroup) {

        return createInsecureCombinationWarningMessage(groupToSet.getName(),
            blanketGroup.getName());
    }

    /**
     * Create a mutally exclusive permission setting error message.
     *
     * @param name name of the first group in the message
     * @param otherName name of other group
     *
     * @return Translated error message with both group names in it
     */
    private static String createMutuallyExclusiveErrorMessage(
            String name, String otherName) {
        String[] values = {name, otherName};
        return Resource.getString(
            ResourceConstants.PERMISSION_MUTUALLY_EXCLUSIVE_ERROR_MESSAGE,
                values);
    }

    static native byte      getMaxValue(String domain, String group);
    static native byte      getDefaultValue(String domain, String group);

    /**
     * Create a potentially dangerous permission setting warning message.
     *
     * @param name name of the first group in the message
     * @param otherName name of other group
     *
     * @return Translated error message with both group names in it
     */
    private static String createInsecureCombinationWarningMessage(
            String name, String otherName) {

        String[] values = {name, otherName};

        return Resource.getString(
            ResourceConstants.PERMISSION_SECURITY_WARNING_ERROR_MESSAGE,
                values);
    }

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
