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

import javax.microedition.lcdui.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.main.TrustedMIDletIcon;

import com.sun.midp.midlet.*;

import com.sun.midp.midletsuite.*;

import com.sun.midp.io.j2me.push.*;

import com.sun.midp.installer.*;

import com.sun.midp.configurator.Constants;

/**
 * The Graphical MIDlet suite information display.
 * <p>
 * The application property "arg0" will have the ID of the suite to display.
 */
public class AppInfo extends Form {

    /** Cache of the suite icon. */
    Image suiteIcon;
    /** Cache of the single suite icon. */
    Image singleSuiteIcon;

    /** Icon to display for the suite */
    Image icon;

    /** MIDlet Suite storage object. */
    MIDletSuiteStorage midletSuiteStorage;

    /** Switch to the manager when "Back" is activated */
    ApplicationManager manager;

    /** Installation information of the suite. */
    InstallInfo installInfo;
    /** Number of MIDlets in the suite. */
    int numberOfMidlets;
    /** Display name of the suite. */
    String displayName;

    /**
     * Create and initialize a new Application Info MIDlet.
     * @param suiteId - the id of the suite for
     *                  which the AppInfo should be displayed
     * @exception Exception if error occurs
     */
    public AppInfo(int suiteId) throws Throwable {

        super(null);

        midletSuiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();

        displaySuiteInfo(suiteId);
    }

    /**
     * Display the information for a suite.
     *
     * @param suiteId ID for suite to display
     * @exception Exception if error occurs
     */
    private void displaySuiteInfo(int suiteId) throws Throwable {
        StringBuffer label = new StringBuffer(40);
        StringBuffer value = new StringBuffer(40);
        Item item;
        String[] authPath;
        String temp;
        MIDletSuiteImpl midletSuite = null;

        try {
            midletSuite = midletSuiteStorage.getMIDletSuite(suiteId, false);

            initMidletSuiteInfo(midletSuite);

            label.append(Resource.getString(ResourceConstants.INFO));
            label.append(": ");
            label.append(displayName);
            setTitle(label.toString());

            append(
                new ImageItem(null, icon, ImageItem.LAYOUT_NEWLINE_BEFORE +
                              ImageItem.LAYOUT_CENTER +
                              ImageItem.LAYOUT_NEWLINE_AFTER, null));

            if (!midletSuite.isEnabled()) {
                item = new StringItem(null, Resource.getString(
                           ResourceConstants.AMS_SUITE_DISABLED_NOTE));
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                append(item);
            }

            // round up the size to a Kilobyte
            label.setLength(0);
            label.append(Resource.getString(ResourceConstants.AMS_SIZE));
            label.append(": ");
            value.append(
                Integer.toString((MIDletSuiteStorage.getMIDletSuiteStorage().
                    getStorageUsed(midletSuite.getID()) +
                    1023) / 1024));
            value.append(" K");
            item = new StringItem(label.toString(), value.toString());
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            append(item);

            label.setLength(0);
            label.append(Resource.getString(ResourceConstants.AMS_VERSION));
            label.append(": ");
            item = new StringItem(label.toString(),
                midletSuite.getProperty(MIDletSuite.VERSION_PROP));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            append(item);

            label.setLength(0);

            if (midletSuite.isTrusted()) {
                temp = Resource.getString
                          (ResourceConstants.AMS_MGR_AUTH_VENDOR);
            } else {
                temp = Resource.getString
                          (ResourceConstants.AMS_MGR_VENDOR);
            }

            label.append(temp);
            label.append(": ");
            item = new StringItem(label.toString(),
                midletSuite.getProperty(MIDletSuite.VENDOR_PROP));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            append(item);

            temp = midletSuite.getProperty(MIDletSuite.DESC_PROP);
            if (temp != null) {
                label.setLength(0);
                label.append(Resource.getString
                             (ResourceConstants.AMS_DESCRIPTION));
                label.append(": ");
                item = new StringItem(label.toString(), temp);
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                append(item);
            }

            if (numberOfMidlets != 1) {
                label.setLength(0);
                label.append(Resource.getString
                             (ResourceConstants.AMS_CONTENTS));
                label.append(":");
                item = new StringItem(label.toString(), "");
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                append(item);
                appendMIDletsToForm(midletSuite);
            }

            label.setLength(0);
            label.append(Resource.getString(ResourceConstants.AMS_WEBSITE));
            label.append(": ");
            item = new StringItem(label.toString(),
                       installInfo.getDownloadUrl());
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            append(item);


            label.setLength(0);
            label.append(Resource.getString(ResourceConstants.AMS_ADVANCED));
            label.append(": ");
            item = new StringItem(label.toString(), "");
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            append(item);

            if (midletSuite.isTrusted()) {
                append(new ImageItem(null, TrustedMIDletIcon.getIcon(),
                    ImageItem.LAYOUT_DEFAULT, null));
                temp = Resource.getString
                          (ResourceConstants.AMS_MGR_TRUSTED);
            } else {
                temp = Resource.getString
                          (ResourceConstants.AMS_MGR_UNTRUSTED);
            }

            item = new StringItem(null, temp);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            append(item);

            // Append classes verification state to advanced section
            if (Constants.VERIFY_ONCE && midletSuite.isVerified()) {
                item = new StringItem(null, Resource.getString(
                    ResourceConstants.AMS_VERIFIED_CLASSES));
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                append(item);

            }

            authPath = installInfo.getAuthPath();
            if (authPath != null) {
                label.setLength(0);
                label.append(Resource.getString
                             (ResourceConstants.AMS_AUTHORIZED_BY));
                label.append(": ");
                temp = label.toString();
                for (int i = 0; i < authPath.length; i++) {
                    item = new StringItem(temp, authPath[i]);
                    item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                    append(item);
                    temp = null;
                }
            }

            temp = PushRegistryInternal.listConnections(
                       midletSuite.getID(), false);
            if (temp != null) {
                label.setLength(0);
                label.append(Resource.getString
                             (ResourceConstants.AMS_AUTO_START_CONN));
                label.append(": ");
                item = new StringItem(label.toString(), temp);
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                append(item);
            }
        } catch (Throwable t) {
            throw t;
        } finally {
            if (midletSuite != null) {
                midletSuite.close();
            }
        }
    }

    /**
     * Initialize the MIDlet suite info fields for a given suite.
     *
     * @param midletSuite the MIDletSuiteImpl object instance
     *
     * @exception Exception if problem occurs while getting the suite info
     */
    private void initMidletSuiteInfo(MIDletSuiteImpl midletSuite)
        throws Exception {

        installInfo = midletSuite.getInstallInfo();
        numberOfMidlets = midletSuite.getNumberOfMIDlets();

        if (numberOfMidlets == 1) {
            String value = midletSuite.getProperty("MIDlet-1");
            MIDletInfo temp = new MIDletInfo(value);
            displayName = temp.name;
            icon = getSingleSuiteIcon();
        } else {
            displayName =
                midletSuite.getProperty(MIDletSuiteImpl.SUITE_NAME_PROP);
            icon = getSuiteIcon();
        }
    }

    /**
     * Appends a names of all the MIDlets in a suite to a Form, one per line.
     *
     * @param midletSuite information of a suite of MIDlets
     */
    private void appendMIDletsToForm(MIDletSuiteImpl midletSuite) {
        int numberOfMidlets;
        MIDletInfo midletInfo;
        StringItem item;

        numberOfMidlets = midletSuite.getNumberOfMIDlets();
        for (int i = 1; i <= numberOfMidlets; i++) {
            midletInfo = new MIDletInfo(
                             midletSuite.getProperty("MIDlet-" + i));

            item = new StringItem(null, midletInfo.name);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            append(item);
        }
    }

    /**
     * Gets the MIDlet suite icon from storage.
     *
     * @return icon image
     */
    private Image getSuiteIcon() {
        if (suiteIcon != null) {
            return suiteIcon;
        }

        suiteIcon = GraphicalInstaller.getImageFromInternalStorage("_suite8");
        return suiteIcon;
    }

    /**
     * Gets the single MIDlet suite icon from storage.
     *
     * @return icon image
     */
    private Image getSingleSuiteIcon() {
        if (singleSuiteIcon != null) {
            return singleSuiteIcon;
        }

        singleSuiteIcon = GraphicalInstaller.
            getImageFromInternalStorage("_single8");
        return singleSuiteIcon;
    }
}
