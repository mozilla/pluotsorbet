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

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import javax.microedition.lcdui.*;

class AppSettingsUIImpl extends Form
        implements AppSettingsUI, CommandListener, ItemStateListener {

    /** application settings peer. */
    AppSettings appSettings;

    /** The ID of the last selected group. */
    private int lastGroupChoiceID;
    /** The index of the last selected group. */
    private int lastGroupChoiceIndex;
    /** The settings choice group. */
    private RadioButtonSet groupChoice;
    /** The level choice groups. */
    private RadioButtonSet[] groupSettings;
    /** The ID of the setting displayed in the form. */
    private int displayedSettingID;
    
    
    /** Command object for "OK" command for the form. */
    private Command saveAppSettingsCmd =
        new Command(Resource.getString(ResourceConstants.SAVE),
                    Command.OK, 1);
    /** Command object for "Cancel" command for the form. */
    private Command cancelCmd =
        new Command(Resource.getString(ResourceConstants.CANCEL),
                    Command.CANCEL, 1);

    /**
     * Create and initialize a new application settings MIDlet.
     */
    AppSettingsUIImpl() {
        super(null);
    }


    /**
     * Display the MIDlet suite settings in combo box
     * and available setting values in option button set.
     */
    private void loadApplicationSettings() {
        // create popup with available settings
        ValueChoice settings = appSettings.getSettings();
        if (settings.getCount() == 0) {
            // no settings available
            return;
        }
        groupChoice = new RadioButtonSet(settings.getTitle(), true);
        groupSettings = new RadioButtonSet[settings.getCount()];
        for (int i = 0; i < settings.getCount(); i++) {
            groupChoice.append(settings.getLabel(i), settings.getID(i));
            ValueChoice settingValues = appSettings.getSettingValues(settings.getID(i));
            // for each group create option button set with available levels
            groupSettings[i] = new RadioButtonSet(settingValues.getTitle(), false);
            for (int j = 0; j < settingValues.getCount(); j++) {
                groupSettings[i].append(settingValues.getLabel(j), settingValues.getID(j));
            }
            //select current level
            groupSettings[i].setSelectedID(settingValues.getSelectedID());
            groupSettings[i].setPreferredSize(getWidth(), -1);
        }
        // select default group
        groupChoice.setSelectedID(settings.getSelectedID());
        append(groupChoice);
        lastGroupChoiceID = settings.getSelectedID();
        lastGroupChoiceIndex = groupChoice.getSelectedIndex();
        displayedSettingID = append(groupSettings[lastGroupChoiceIndex]);
    }

    /**
     * Respond to a command issued on any Screen.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == saveAppSettingsCmd) {
            appSettings.saveApplicationSettings();
        } else if (c == cancelCmd) {
            appSettings.cancelApplicationSettings();
        }
    }

    /**
     * Called when internal state of an item in Settings form is
     * changed by the user. This is used to dynamically display
     * the setting the user chooses from the settings popup.
     *
     * @param item the item that was changed
     */
    public void itemStateChanged(Item item) {

        if (item == groupChoice) {
            int selected;
            selected = groupChoice.getSelectedID();
            if (selected == lastGroupChoiceID) {
                return;
            }

            lastGroupChoiceID = selected;
            lastGroupChoiceIndex = groupChoice.getSelectedIndex();

            delete(displayedSettingID);

            try {
                displayedSettingID = append(groupSettings[lastGroupChoiceIndex]);
            } catch (IndexOutOfBoundsException e) {
                // for safety/completeness.
                displayedSettingID = 0;
                Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                    "AppSettings: selected=" + selected);
            }
        } else {
            appSettings.onSettingChanged(lastGroupChoiceID,
                    groupSettings[lastGroupChoiceIndex].getSelectedID());
        }
    }

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
    public void changeSettingValue(int settingID, int valueID) {
        groupSettings[groupChoice.indexFor(settingID)].setSelectedID(valueID);
        
    }

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
    public void showAppSettings(AppSettings appSettings, String title,
        Display display, DisplayError displayError) {
        
        setTitle(title);
        this.appSettings = appSettings;
        loadApplicationSettings();
        if (groupChoice != null) {
            setItemStateListener(this);
            addCommand(saveAppSettingsCmd);
        }
        addCommand(cancelCmd);
        setCommandListener(this);

        display.setCurrent(this);
    }

    /**
     * Returns the main displayable of the AppSettingsUI.
     * @return main screen
     */
    public Displayable getMainDisplayable() {
        return this;
    }

}

/**
 * A <code>RadioButtonSet</code> is a group radio buttons intended to be
 * placed within a <code>Form</code>. However the radio buttons can be
 * accessed by a assigned ID instead of by index. This lets the calling
 * code be the same when dealing with dynamic sets.
 */
class RadioButtonSet extends ChoiceGroup {
    /** Size increment for the ID array. */
    private static final int SIZE_INCREMENT = 5;

    /** Keeps track of the button IDs. */
    private int[] ids;

    /**
     * Creates a new, empty <code>RadioButtonSet</code>, specifying its
     * title.
     *
     * @param label the item's label (see {@link Item Item})
     * @param popup true if the radio buttons should be popup
     */
    RadioButtonSet(String label, boolean popup) {
        super(label, popup ? Choice.POPUP : Choice.EXCLUSIVE);
        ids = new int[SIZE_INCREMENT];
    }

    /**
     * Appends choice to the set.
     *
     * @param stringPart the string part of the element to be added
     * @param id ID for the radio button
     *
     * @throws IllegalArgumentException if the image is mutable
     * @throws NullPointerException if <code>stringPart</code> is
     * <code>null</code>
     * @throws IndexOutOfBoundsException this call would exceed the maximum
     *         number of buttons for this set
     */
    void append(String stringPart, int id) {
        int buttonNumber = append(stringPart, null);

        if (buttonNumber >= ids.length) {
            expandIdArray();
        }

        ids[buttonNumber] = id;
    }

    /**
     * Selects specufued item.
     *
     * @param id ID of item
     *
     * @throws IndexOutOfBoundsException if <code>id</code> is invalid
     */
    void setSelectedID(int id) {
        setSelectedIndex(indexFor(id), true);
    }

    /**
     * Returns the ID of the selected radio button.
     *
     * @return ID of selected element
     */
    int getSelectedID() {
        return ids[getSelectedIndex()];
    }

    /**
     * Find the index for an ID.
     *
     * @param id button id
     *
     * @return index for a button
     *
     * @exception IndexOutOfBoundsException If no element exists with that ID
     */
    int indexFor(int id) {
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == id) {
                return i;
            }
        }

        throw new IndexOutOfBoundsException();
    }

    /** Expands the ID array. */
    private void expandIdArray() {
        int[] prev = ids;

        ids = new int[prev.length + SIZE_INCREMENT];
        for (int i = 0; i < prev.length; i++) {
            ids[i] = prev[i];
        }
    }

    /**
     * Returns ID of specified item.
     * @param index item index
     * @return item ID
     */
    int getID(int index) {
        return ids[index];
    }
}

