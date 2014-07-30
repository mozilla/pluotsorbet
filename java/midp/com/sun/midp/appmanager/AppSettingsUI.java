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

package com.sun.midp.appmanager;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Display;

interface AppSettingsUI {
    /**
     * Shows UI with application settings.
     * All information regarding available settings, possible setting values
     * and current setting value should be queried from AppSettings peer.
     * @param appSettings AppSettings peer, where information regarding
     *  available settings and current setting value could be found.
     *  Also appSettings is used to change application settings or to cancel
     *  the process and dismiss this form. Method onSettingChanged of
     *  appSettings should be called when attempt to change value for
     *  particular setting occures. As a result changeSettingValue could be
     *  called by appSettings when proposed setting value leads to changes in
     *  other settings or is not allowed. This may happen for example when mutual
     *  exclusive combinations selected. All necessary alerts in this case are
     *  shown to the user by AppSettings and thus AppSettingsUIImpl has just
     *  to change UI accordingly when changeSettingValue is called.
     * @param title
     * @param display - The display instance associated with the manager
     * @param displayError - The UI used to display error messages
     */
    void showAppSettings(AppSettings appSettings, String title,
                         Display display, DisplayError displayError);

    /**
     * Called by AppSettings when specified value shoud be changed in UI.
     * Could be called as a result of user input validation by AppSettings
     * to correct the invalid setting combination. All necessary informational
     * alerts in this case are shown to the user by AppSettings and thus
     * AppSettingsUI has just to change UI accordingly.
     * 
     * @param settingID id of setting
     * @param  valueID id of selected value
     */
    void changeSettingValue(int settingID, int valueID);

    /**
     * Returns the main displayable of the AppSettingsUI.
     * @return main screen
     */
    Displayable getMainDisplayable();

}
