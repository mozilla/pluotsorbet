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
 * The attributes of a permission group.
 */
public final class PermissionGroup {
    /** the Visible Name string. */
    private String name;

    /** the internal Name string (as appears in
     *  PermissionsStrings.java (auto-generated)). */
    private String nativeName;

    /** Settings question. */
    private String settingsQuestion;

    /** Disable setting choice string. */
    private String disableSettingChoice;

    /** Title string for the permission dialog. */
    private String runtimeDialogTitle;

    /** Question string for the permission dialog. */
    private String runtimeQuestion;

    /** Oneshot question string for the permission dialog. */
    private String runtimeOneshotQuestion;

    /**
     * Constructs a third party domain permission group.
     *
     * @param theName name of the group
     * @param theSettingsQuestion question for the settings dialog
     * @param theDisableSettingChoice disable setting choice
     * @param theRuntimeDialogTitle Title for the runtime permission dialog
     * @param theRuntimeQuestion Question for the runtime permission dialog
     * @param theRuntimeOneshotQuestion Oneshot question for the runtime
     *                                  permission dialog
     */
    PermissionGroup(String theName, String theSettingsQuestion,
        String theDisableSettingChoice, String theRuntimeDialogTitle,
        String theRuntimeQuestion, String theRuntimeOneshotQuestion) {
        this (theName, theName,
              theSettingsQuestion, theDisableSettingChoice,
              theRuntimeDialogTitle, theRuntimeQuestion,
              theRuntimeOneshotQuestion);
    }

    PermissionGroup(String nativeName, String theName, String theSettingsQuestion,
        String theDisableSettingChoice, String theRuntimeDialogTitle,
        String theRuntimeQuestion, String theRuntimeOneshotQuestion) {

        this.nativeName = nativeName;
        name = theName;

        settingsQuestion = theSettingsQuestion;
        if (settingsQuestion == null) {
            settingsQuestion = "n/a";
        }

        disableSettingChoice = theDisableSettingChoice;
        if (disableSettingChoice == null) {
            disableSettingChoice = "n/a";
        }

        runtimeDialogTitle = theRuntimeDialogTitle;
        if (runtimeDialogTitle == null) {
            runtimeDialogTitle = "n/a";
        }

        runtimeQuestion = theRuntimeQuestion;
        if (runtimeQuestion == null) {
            runtimeQuestion = "n/a";
        }
        
        runtimeOneshotQuestion = theRuntimeOneshotQuestion;
        if (runtimeOneshotQuestion == null) {
            runtimeOneshotQuestion = runtimeQuestion;
        }
    }
    /**
     * Get the name string ID.
     *
     * @return string ID or zero if there is no name for the settings dialog
     */
    public String getName() {
        return name;
    }

    public String getNativeName() {
        return nativeName;
    }

    /**
     * Get the settings question ID.
     *
     * @return stringID or 0 if there is no question
     */
    public String getSettingsQuestion() {
        return settingsQuestion;
    }

    /**
     * Get the disable setting choice string ID.
     *
     * @return string ID or 0 if there is not disable setting choice
     */
    public String getDisableSettingChoice() {
        return disableSettingChoice;
    }

    /**
     * Get the title string ID for the permission dialog.
     *
     * @return string ID
     */
    public String getRuntimeDialogTitle() {
        return runtimeDialogTitle;
    }

    /**
     * Get the question string ID for the permission dialog.
     *
     * @return string ID
     */
    public String getRuntimeQuestion() {
        return runtimeQuestion;
    }

    /**
     * Get the oneshot question string ID for the permission dialog.
     *
     * @return string ID
     */
    public String getRuntimeOneshotQuestion() {
        return runtimeOneshotQuestion;
    }
}
