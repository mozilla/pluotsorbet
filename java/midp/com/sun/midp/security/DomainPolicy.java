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


/**
 * This class contains the information on policy domain.
 */
public class DomainPolicy {

    /**
     *  The domain name.
     */
    private String name;

    /**
     *  Is this domain is a trusted domain
     */
    private boolean isTrusted;

    /**
     *  Array of values of default permission access.
     */
    private byte[] defValues;

    /**
     * Array of values of maximum permission access.
     */
    private byte[] maxValues;


    /**
     * Create <code>DomainPolicy</code> class for a given domain
     * name and if it is trusted.
     *
     * @param name domain name
     * @param isTrusted true if the domain is trusted, false otherwise
     */
    public DomainPolicy(String name,  boolean isTrusted) {
        this.name = name;
        this.isTrusted = isTrusted;
        loadValues();
    }  
    
    /**
     * Get the name of the domain.
     *
     * @return domain name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Check if domain is a trusted domain
     *
     * @return true if domain is trusted
     */
    public boolean isTrusted() {
        return isTrusted;
    }
    
    /**
     * Fill up array with values of permissions levels defined in 
     * this domain 
     *
     * @param list this array is filled up with the permissions 
     * values.
     * @param level the level values that required, MAXIMUM values 
     * or default values. 
     */
    public void getPermissionlevels(byte [] list, int level) {
        if (level == Permissions.CUR_LEVELS) {
            System.arraycopy(defValues, 0, list, 0, defValues.length);
        } else {
            System.arraycopy(maxValues, 0, list, 0, maxValues.length);
        }
    }
    
    public String toString() {
        return name + ((isTrusted)? ",trusted" : ",untrusted");
    }
    
    /**
     * Private method that uses to load permissions values of the 
     * domain from native level (instead of being hard-coded).
     * The default values of MIDP and AMS permissions are NEVER, the 
     * correct value of "manufacturer" domain is assigned in 
     * Permissions.java 
     */
    private void loadValues() {
        PermissionGroup [] list = Permissions.getSettingGroups();
        byte [] groupDefValues = new byte[list.length];
        byte [] groupMaxValues = new byte[list.length];

        for (int i1 = 0; i1 < list.length; i1++) {
            String nativeName = list[i1].getNativeName();
            boolean isMessagingGroup = nativeName.endsWith("_messaging");
            boolean isReadGroup = false;

            if (isMessagingGroup) {
                if (nativeName.startsWith("read_")) {
                    isReadGroup = true;
                }
                // skip "send"/"read" prefix
                nativeName = nativeName.substring(5);
            }

            groupDefValues[i1] =
                    Permissions.getDefaultValue(name, nativeName);
            groupMaxValues[i1] =
                    Permissions.getMaxValue(name, nativeName);

            if (isReadGroup) {
                /*
                 * For MSA compliance, we need to hard-code BLANKET effective
                 * mode for READ_[RESTRICTED_]MESSAGING_GROUP if current
                 * interaction mode for this group is set to ONESHOT.
                 */
                if (groupDefValues[i1] == Permissions.ONESHOT) {
                    groupDefValues[i1] = Permissions.BLANKET;
                }
            }
        }

        defValues = new byte[Permissions.NUMBER_OF_PERMISSIONS];
        maxValues = new byte[Permissions.NUMBER_OF_PERMISSIONS];
        defValues[Permissions.MIDP] = Permissions.NEVER;
        defValues[Permissions.AMS] = Permissions.NEVER;
        maxValues[Permissions.MIDP] = Permissions.NEVER;
        maxValues[Permissions.AMS] = Permissions.NEVER;

        for (int i1 = 2; i1 < defValues.length; i1++) {
            String group =
                    Permissions.permissionSpecs[i1].group.getNativeName();
            for (int i2 = 0; i2 < list.length; i2++) {
                if (group.equals(list[i2].getNativeName())) {
                    defValues[i1] = groupDefValues[i2];
                    maxValues[i1] = groupMaxValues[i2];
                    break;
                }
            }
        }
    }
}
