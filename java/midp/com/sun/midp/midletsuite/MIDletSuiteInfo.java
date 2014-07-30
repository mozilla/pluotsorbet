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

import com.sun.midp.main.*;

import com.sun.midp.configurator.Constants;
import com.sun.midp.midlet.MIDletSuite;

/** Simple attribute storage for MIDlet suites */
public class MIDletSuiteInfo {
    /** ID of the MIDlet suite. */
    public int suiteId;
    /** ID of the storage where the MIDlet is installed. */
    public int storageId = Constants.INTERNAL_STORAGE_ID;
    /** ID of the folder where the MIDlet resides. */
    public int folderId = 0;
    /** Display name of the MIDlet suite. */
    public String displayName = null;
    /** Name of the MIDlet to run. */
    public String midletToRun = null;
    /** Is this single MIDlet MIDlet suite. */
    public int numberOfMidlets = 0;
    /** Is this suite enabled. */
    public boolean enabled = false;
    /** Is this suite trusted. */
    public boolean trusted = false;
    /** Is this suite temporary. */
    public boolean temporary = false;
    /** Is this suite preinstalled. */
    public boolean preinstalled = false;
    /** Icon's name for this suite. */
    public String iconName = null;

    /**
     * Copy all information from another such object.
     * @param that the source object
     */
    final public void copyFieldsFrom(MIDletSuiteInfo that) {
        // IMPL_NOTE: this method is final to prevent inheritance from
        // RunningMIDletSuiteInfo: this method deals only with
        // persistent properties.
        this.suiteId = that.suiteId;
        this.storageId = that.storageId;
        this.folderId = that.folderId;
        this.displayName = that.displayName;
        this.midletToRun = that.midletToRun;
        this.numberOfMidlets = that.numberOfMidlets;
        this.enabled = that.enabled;
        this.trusted = that.trusted;
        this.temporary = that.temporary;
        this.preinstalled = that.preinstalled;
        this.iconName = that.iconName;
    }

    /**
     * Constructs a MIDletSuiteInfo object for a suite.
     *
     * @param theID ID the system has for this suite
     */
    public MIDletSuiteInfo(int theID) {
        suiteId = theID;
    }

    /**
     * Constructs a MIDletSuiteInfo object for a suite.
     *
     * @param theID ID the system has for this suite
     * @param theMidletToRun Class name of the only midlet in the suite
     * @param theDisplayName Name to display to the user
     * @param isEnabled true if the suite is enabled
     */
    public MIDletSuiteInfo(int theID, String theMidletToRun,
            String theDisplayName, boolean isEnabled) {
        suiteId = theID;
        midletToRun = theMidletToRun;
        displayName = theDisplayName;
        enabled = isEnabled;
    }

    /**
     * Constructs a MIDletSuiteInfo object for a suite.
     *
     * @param theID ID the system has for this suite
     * @param theMidletSuite MIDletSuite information
     */
    public MIDletSuiteInfo(int theID, MIDletSuiteImpl theMidletSuite) {
        init(theID, theMidletSuite);

        numberOfMidlets = theMidletSuite.getNumberOfMIDlets();

        if (numberOfMidlets == 1) {
            MIDletInfo midlet =
                new MIDletInfo(theMidletSuite.getProperty("MIDlet-1"));

            midletToRun = midlet.classname;
        }
    }

    /**
     * Initializes MIDletSuiteInfo object.
     *
     * @param theID ID the system has for this suite
     * @param theMidletSuite MIDletSuite information
     */
    void init(int theID, MIDletSuiteImpl theMidletSuite) {
        displayName =
            theMidletSuite.getProperty(MIDletSuiteImpl.SUITE_NAME_PROP);

        if (displayName == null) {
            displayName = String.valueOf(theID);
        }

        suiteId = theID;

        enabled = theMidletSuite.isEnabled();
    }

    /**
     * Checks if the midlet suite contains single or multiple midlets.
     *
     * @return true is this midlet suite contains only one midlet,
     *         false otherwise
     */
    public boolean hasSingleMidlet() {
        return (numberOfMidlets == 1);
    }

    /**
     * Returns a string representation of the MIDletSuiteInfo object.
     * For debug only.
     */
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("suiteId  = " + suiteId);
        b.append(", folderId = " + folderId);
        b.append(", midletToRun = " + midletToRun);
        return b.toString();
    }

    /**
     * Check if this MIDletSuiteInfo object describes a MIDlet from the
     * internal suite (rather than from an installed suite).
     * The internal MIDlets are a special case: there is one MIDletSuiteInfo
     * per MIDlet, and they share the same suite id.
     * @return true if the suite id is INTERNAL_SUITE_ID
     */
    final public boolean isInternal() {
        return suiteId == MIDletSuite.INTERNAL_SUITE_ID;
    }
}
