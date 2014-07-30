/*
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

package com.sun.midp.appmanager;

import javax.microedition.lcdui.Displayable;

/**
 * The interface to application setting peer used by AppSettingUI.
 */
interface AppSettings {

    /**
     * Called by UI when value of particular application setting has been
     * changed by user. AppSettings validates the user input and If value
     * is not acceptable, for example due to exclusive combination selected,
     * changeSettingValue method of AppSettingsUI could be called by AppSettings
     * to change settings to appropriate values. All necessary informational
     * alerts in this case are shown to the user by AppSettings and thus
     * AppSettingsUI has just to change UI accordingly when changeSettingValue
     * is called.
     *  
     * @param settingID id of setting
     * @param valueID id of selected value
     */
    void onSettingChanged(int settingID, int valueID);

    /**
     * Returns ValueChoice that contains set of available application
     * setting names and IDs. Selected ID represents the initial setting
     * to be shown to the user.
     * @return value choice
     */
    ValueChoice getSettings();

    /**
     * Returns ValueChoice that contains set of possible value' IDs and
     * lables for specified setting. Selected ID represents value that
     * is currently active for this setting.
     * @param settingID
     * @return available setting values
     */
    ValueChoice getSettingValues(int settingID);


    /**
     * Cancel application settings the user entered and dismiss UI.
     * Called by AppSettingsUI as response to user request.
     */
    void cancelApplicationSettings();

    /**
     * Save application settings the user entered and dismiss UI.
     * Called by AppSettingsUI as a response to user request.
     * 
     * IMPL_NOTE: This method has no arguments as AppSettings is
     * aware of changes user made due to onSettingChanged calls.
     *
     */
    void saveApplicationSettings();
}
