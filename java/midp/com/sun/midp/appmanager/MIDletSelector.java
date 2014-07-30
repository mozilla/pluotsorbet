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
import com.sun.midp.main.MIDletProxyList;
import com.sun.midp.main.MIDletProxy;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midletsuite.MIDletInfo;
import com.sun.midp.midletsuite.MIDletSuiteCorruptedException;
import com.sun.midp.midletsuite.MIDletSuiteLockedException;
import com.sun.midp.midletsuite.MIDletSuiteStorage;

import javax.microedition.lcdui.*;
import java.util.Vector;

/**
 * Selector provides a simple user interface to select MIDlets to run.
 * It extracts the list of MIDlets from the attributes in the
 * descriptor file and presents them to the user using the MIDlet-&lt;n&gt;
 * name and icon if any. When the user selects a MIDlet an instance
 * of the class indicated by MIDlet-&lt;n&gt; classname is created.
 */
final class MIDletSelector implements CommandListener, ItemCommandListener {
    /** The Form with list of all the MIDlets */
    private Form mform;

    /** Information needed to display a list of MIDlets */
    private RunningMIDletSuiteInfo suiteInfo;

    /** The Display */
    private Display display;

    /** The parent's display able */
    private Displayable parentDisplayable;

    /** Parent app manager */
    ApplicationManager manager;

    /** Number of midlets in minfo */
    private int mcount;

    /** MIDlet information, class, name, icon; one per MIDlet */
    private MIDletInfo[] minfo;

    /**The Command object to exit back to the MIDlet Suite Manager */
    private Command backCmd = new Command(Resource.getString
                                          (ResourceConstants.BACK),
                                          Command.BACK, 2);

    /** The Command object for "Launch" */
    private Command launchCmd = new Command(Resource.getString
                                            (ResourceConstants.LAUNCH),
                                            Command.ITEM, 1);

    /** The Command object for "End" midlet */
    private Command endCmd = new Command(Resource.getString
                                         (ResourceConstants.END),
                                         Command.ITEM, 1);


    /**
     * Create and initialize a new Selector MIDlet.
     * The Display is retrieved and the list of MIDlets read
     * from the descriptor file.
     *
     * @param theSuiteInfo information needed to display a list of MIDlets
     * @param theDisplay the Display
     * @param theParentDisplayable the parent's displayable
     * @param theManager the parent application manager
     *
     * @throws MIDletSuiteCorruptedException if the suite is corrupted
     * @throws MIDletSuiteLockedException if the suite is locked
     */
    MIDletSelector(RunningMIDletSuiteInfo theSuiteInfo, Display theDisplay,
                   Displayable theParentDisplayable,
                   ApplicationManager theManager)
            throws MIDletSuiteCorruptedException, MIDletSuiteLockedException {

        MIDletSuiteStorage mss;

        suiteInfo = theSuiteInfo;
        display = theDisplay;
        parentDisplayable = theParentDisplayable;
        manager = theManager;
        mcount = 0;
        minfo = new MIDletInfo[20];

        mss = MIDletSuiteStorage.getMIDletSuiteStorage();

        readMIDletInfo(mss);
        setupForm(mss);

        mform.addCommand(backCmd);
        mform.setCommandListener(this);

        display.setCurrent(mform);
        
        /* for locked suite, we need storage lock until some MIDlet is launched.
         * This prevents reinstallation of the locked suite. */
        if (suiteInfo.isLocked()) {
            suiteInfo.grabStorageLock();
        }
    }

    /**
     * Gets structure containing information about suite accessible by this
     * selector.
     * @return the suite info
     */
    public RunningMIDletSuiteInfo getSuiteInfo() {
        return suiteInfo;
    }

    /** Displays this selector on the screen */
    public void show() {
        refreshList();
        display.setCurrent(mform);
    }

    /**
     * Called when MIDlet execution exited.
     * Removes the MIDlet from list of running MIDlets and shows selector on the
     * screen.
     * @param midlet ClassName of MIDlet which just exited
     */
    public void notifyMidletExited(String midlet) {
        
        /* If main MIDlet is exited and all other MIDlets as well, exit the
         * selector. */
        if (!suiteInfo.hasRunningMidlet() && suiteInfo.hasMainMidlet()) {
            leaveSelector();
            return;
        }
        
        /* If no more MIDlets are running from a locked suite, we need 
         * the storage lock until another MIDlet is launched. This prevents 
         * reinstallation of the locked suite. */
        if (!suiteInfo.hasRunningMidlet() && suiteInfo.isLocked()) {
            suiteInfo.grabStorageLock();
        }
            
        refreshList();
    }

    /** If no MIDlet is running, exits the suite */
    public void exitIfNoMidletRuns() {
        if (!suiteInfo.hasRunningMidlet()) {
            if (suiteInfo.holdsStorageLock()) {
                suiteInfo.releaseStorageLock();
            }
            if (suiteInfo.isLocked()) {
                suiteInfo.unlock();
            }
            manager.notifyMIDletSelectorExited(suiteInfo);
        }
    }
    
    /**
     * Respond to a command issued on any Screen.
     * The commands on list is Select and About.
     * Select triggers the creation of the MIDlet of the same name.
     * About puts up the copyright notice.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == backCmd) {
            leaveSelector();
        }
    }

    /**
     * Leaves the MIDlet selector.
     */
    public void leaveSelector() {
        if (parentDisplayable != null) {
            display.setCurrent(parentDisplayable);
        } else {
            manager.shutDown();
        }
        exitIfNoMidletRuns();
    }
    
    /**
     * Responds to a command issued on an Item in MIDlet selector
     * @param c command activated by the user
     * @param item the Item the command was on.
     */
    public void commandAction(Command c, Item item) {
        int selected = ((SelectorMIDletCustomItem)item).index;
        if (selected < 0 || selected >= mcount) {
            return;
        }

        String midletClassName = minfo[selected].classname;
        if (c == launchCmd) {

            if (suiteInfo.getProxyFor(midletClassName) != null) {
                manager.moveToForeground(suiteInfo, midletClassName);
                return;
            }

            /* if we hold a storage lock, release it to allow the started MIDlet
             * to take it */
            if (suiteInfo.holdsStorageLock()) {
                suiteInfo.releaseStorageLock();
            }

            manager.launchSuite(suiteInfo, midletClassName);

        } else if (c == endCmd) {

            manager.exitMidlet(suiteInfo, midletClassName);
            display.setCurrent(mform);
        }
    }

    /**
     * Reads the set of MIDlet names, icons and classes
     * Fill in the list.
     *
     * @param mss the midlet suite storage
     */
    private void setupForm(MIDletSuiteStorage mss) {
        MIDletProxyList mpl = MIDletProxyList.getMIDletProxyList();
        if (mform == null) {
            mform = new Form(Resource.getString
                    (ResourceConstants.AMS_SELECTOR_SEL_TO_LAUNCH));

            // Add each midlet
            for (int i = 0; i < mcount; i++) {
                Image icon = null;
                if (minfo[i].icon != null) {
                    icon = RunningMIDletSuiteInfo.getIcon(
                        suiteInfo.suiteId, minfo[i].icon, mss);
                }
                // the MIDlet is running iff the MIDlet proxy is found
                MIDletProxy mp = mpl.findMIDletProxy(
                    suiteInfo.suiteId, minfo[i].classname);

                SelectorMIDletCustomItem mci =
                    new SelectorMIDletCustomItem(minfo[i].name, icon, i);

                mci.updateState(mp);
                mci.addCommand(launchCmd);
                mci.addCommand(endCmd);
                mci.setDefaultCommand(launchCmd);
                mci.setOwner(mform);
                mci.setItemCommandListener(this);
                
                mform.append(mci);
            }
        }
    }

    /** Refreshes the MIDlet list, showing the updated statuses */
    private void refreshList() {
        MIDletProxyList mpl = MIDletProxyList.getMIDletProxyList();
        for (int i = 0; i < mcount; i++) {
            SelectorMIDletCustomItem mci = (SelectorMIDletCustomItem)mform.get(i);
            int index = mci.index;
            MIDletProxy mp = mpl.findMIDletProxy(
                suiteInfo.suiteId, minfo[index].classname);
            mci.updateState(mp);
            mci.update();
        }
    }

    /**
     * Reads in and create a MIDletInfo for each MIDlet-&lt;n&gt;
     *
     * @param mss the midlet suite storage
     * @throws MIDletSuiteCorruptedException if the suite is corrupted
     * @throws MIDletSuiteLockedException if the suite is locked 
     */
    private void readMIDletInfo(MIDletSuiteStorage mss)
            throws MIDletSuiteCorruptedException, MIDletSuiteLockedException {
        MIDletSuite midletSuite = mss.getMIDletSuite(suiteInfo.suiteId, false);

        if (midletSuite == null) {
            return;
        }

        try {
            for (int n = 1; n < 100; n++) {
                String nth = "MIDlet-"+ n;
                String attr = midletSuite.getProperty(nth);
                if (attr == null || attr.length() == 0)
                    break;

                addMIDlet(new MIDletInfo(attr));
            }
        } finally {
            midletSuite.close();
        }
    }

    /**
     * Adds a MIDlet to the list.
     * @param info MIDlet information to add to MIDlet
     */
    private void addMIDlet(MIDletInfo info) {
        if (mcount >= minfo.length) {
            MIDletInfo[] n = new MIDletInfo[mcount+4];
            System.arraycopy(minfo, 0, n, 0, mcount);
            minfo = n;
        }

        minfo[mcount++] = info;
    }

    /** The inner class to represent MIDlet items in the MIDlet Selector screen */
    class SelectorMIDletCustomItem extends MIDletCustomItem {
        /** Predefined index of the item in the form */
        int index;
        /** Running state of the item */
        boolean isActive;

        /**
         * Constructs new item
         * @param displayName MIDlet name
         * @param icon MIDlet icon
         * @param index index in the list of MIDlets
         */
        SelectorMIDletCustomItem(String displayName, Image icon, int index) {
            super(displayName, icon);
            this.index = index;
        }

        /**
         * Updates state of the MIDlet item 
         * @param midletProxy MIDlet proxy of running MIDlet item
         */
        void updateState(MIDletProxy midletProxy) {
            isActive = (midletProxy != null &&
                MIDletProxy.MIDLET_ACTIVE == midletProxy.getMidletState());
        }

        /** Overrides #MIDletCustomItem.isRunning() */
        boolean isRunning() {
            return isActive;
        }

    }

}
