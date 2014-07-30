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
package com.sun.midp.midletsuite;

import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;
import com.sun.midp.security.Permissions;

import java.io.IOException;
import javax.microedition.io.Connector;

/**
 * The settings for the suite.
 */
public class SuiteSettings {

    /** Permissions for this suite. */
    byte[] permissions;

    /** Can this MIDlet suite interrupt other suites. */
    byte pushInterruptSetting;

    /** Push options. */
    int pushOptions;

    /** The ID of this suite. */
    int suiteId;

    /** If true, MIDlet from this suite can be run. */
    boolean enabled;

    /**
     * Public constructor for SuiteSettings.
     *
     * @param id of the suite for these settings
     */
    public SuiteSettings(int id) {
        suiteId = id;
        enabled = true; /* default is to enable a newly installed suite */
        permissions = Permissions.getEmptySet();
    }

    /**
     * Gets list of permissions for this suite.
     *
     * @return array of permissions from {@link Permissions}
     * @see #setPermissions
     */
    public byte[] getPermissions() {
        return permissions;
    }

    /**
     * Sets new permissions for the suite.
     *
     * @param newPermissions for the suite
     * @see #getPermissions
     *
     */
    public void setPermissions(byte[] newPermissions) {
        permissions = newPermissions;
    }

    /**
     * Gets push setting for interrupting other MIDlets.
     *
     * @return push setting for interrupting MIDlets the value
     *        will be permission level from {@link Permissions}
     * @see #setPushInterruptSetting
     */
    public byte getPushInterruptSetting() {
        return pushInterruptSetting;
    }

    /**
     * Sets new PushInterruptSetting for the suite.
     *
     * @param newSetting for the suite
     * @see #getPushInterruptSetting
     *
     */
    public void setPushInterruptSetting(byte newSetting) {
        pushInterruptSetting = newSetting;
    }

    /**
     * Gets push options for the suite from persistent store.
     *
     * @return push options are defined in {@link PushRegistryImpl}
     * @see #setPushOptions
     */
    public int getPushOptions() {
        return pushOptions;
    }

    /**
     * Sets new PushOptions for the suite.
     *
     * @param newOption for the suite
     * @see #getPushOptions
     *
     */
    public void setPushOptions(int newOption) {
        pushOptions = newOption;
    }

    /**
     * Gets the unique ID of the suite.
     *
     * @return suite ID
     */
    public int getSuiteId() {
        return suiteId;
    }

    /**
     * Determine if the a MIDlet from this suite can be run. Note that
     * disable suites can still have their settings changed and their
     * install info displayed.
     *
     * @return true if suite is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the suite settings suite from persistent store.
     */
    native void load();

    /**
     * Saves the suite settings to persistent store. Except the enabled
     * state.
     */
    void save() {
        try {
            save0(suiteId, pushInterruptSetting, pushOptions, permissions);
        } catch (IOException ioe) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                               "SuiteSettings.save0() threw IOException");
            }
        }
    }

    /**
     * Saves the suite settings to persistent store.
     *
     * @param suiteId ID of the suite
     * @param pushInterruptSetting push interrupt setting
     * @param pushOptions push options
     * @param permissions current permissions
     *
     * @throws IOException if an I/O error occurs
     */
    private native void save0(
        int suiteId,
        byte pushInterruptSetting,
        int pushOptions,
        byte[] permissions) throws IOException;
}
