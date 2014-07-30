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

import javax.microedition.midlet.MIDlet;

import javax.microedition.lcdui.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.midlet.MIDletStateHandler;
import com.sun.midp.midletsuite.MIDletInfo;

/**
 * A base class for the class Selector providing a simple user interface
 * to select MIDlets to run.
 * It extracts the list of MIDlets from the attributes in the 
 * descriptor file and presents them to the user using the MIDlet-&lt;n&gt;
 * name and icon if any. When the user selects a MIDlet an instance
 * of the class indicated by MIDlet-&lt;n&gt; classname is created.
 */
abstract class SelectorBase extends MIDlet implements CommandListener, Runnable {
    /**
     * The List of all the MIDlets.
     */
    private List mlist;         
    /**
     * The Display.
     */
    protected Display display;
    /**
     * Number of midlets in minfo.
     */
    private int mcount;
    /**
     * MIDlet information, class, name, icon; one per MIDlet.
     */
    private MIDletInfo[] minfo; 
    /**
     * the Command object to exit the Selector MIDlet
     */
    private Command exitCmd = new Command(Resource.getString
					  (ResourceConstants.EXIT),
                                          Command.EXIT, 2);
    /**
     * the Command object for "Launch".
     */
    private Command launchCmd = new Command(Resource.getString
					    (ResourceConstants.LAUNCH), 
					    Command.ITEM, 1);
    /**
     * Index of the selected MIDlet, starts at -1 for non-selected.
     */
    protected int selectedMidlet = -1;

    /**
     * Create and initialize a new Selector MIDlet.
     * The Display is retrieved and the list of MIDlets read
     * from the descriptor file.
     */
    public SelectorBase() {
        display = Display.getDisplay(this);
        mcount = 0;
        minfo = new MIDletInfo[20];
        readMIDletInfo();
    }

    /**
     * Put up a List of the MIDlets found in the descriptor file.
     */
    public void startApp() {
        setupList();
        mlist.addCommand(launchCmd);
        mlist.addCommand(exitCmd);
        mlist.setCommandListener(this);
        display.setCurrent(mlist);
    }

    /**
     * There are no resources that need to be released.
     */
    public void pauseApp() {
    }

    /**
     * Destroy cleans up.
     * The only resource used is in the objects that will be
     * reclaimed by the garbage collector.
     * @param unconditional is ignored; the Selector always
     * destroys itself when requested.
     */
    public void destroyApp(boolean unconditional) {
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
        if ((s == mlist && c == List.SELECT_COMMAND) || (c == launchCmd)) {
                synchronized (this) {
                    if (selectedMidlet != -1) {
                        // the previous selected MIDlet is being launched
                        return;
                    }

                    selectedMidlet = mlist.getSelectedIndex();
                }
                new Thread(this).start();
        } else if (c == exitCmd) {
            destroyApp(false);
            notifyDestroyed();
        }
    }

    /**
     * Destroys this Selector midlet and exits after scheduling
     * an execution of the next midlet in SVM, do nothing in MVM. 
     */
    protected abstract void yieldToNextMidlet();

    /**
     * Launch a the select MIDlet.
     */
    public void run() {
        MIDletStateHandler midletStateHandler =
            MIDletStateHandler.getMidletStateHandler();
        String classname = minfo[selectedMidlet].classname;
        String displayName = minfo[selectedMidlet].name;

        try {
            midletStateHandler.startMIDlet(classname,
                                           displayName);
            yieldToNextMidlet();
        } catch (Exception ex) {
            ex.printStackTrace();

            StringBuffer sb = new StringBuffer()
                .append(minfo[selectedMidlet].name)
                .append(", ")
                .append(classname)
                .append("\n")
                .append(Resource.getString(ResourceConstants.EXCEPTION))
                .append(": ")
                .append(ex.toString());

            Alert a = new Alert(Resource.getString
				(ResourceConstants.AMS_CANNOT_START), 
                                sb.toString(), null, null);
	    
            display.setCurrent(a, mlist);

            // let another MIDlet be selected after the alert
            selectedMidlet = -1;
        }
    }

    /**
     * Read the set of MIDlet names, icons and classes
     * Fill in the list.
     */
    private void setupList() {
        if (mlist == null) {
            mlist = new List(Resource.getString
			     (ResourceConstants.AMS_SELECTOR_SEL_TO_LAUNCH), 
                             Choice.IMPLICIT);

            // Add each midlet
            for (int i = 0; i < mcount; i++) {
                Image icon = null;
                if (minfo[i].icon != null) {
                    try {
                        icon = Image.createImage(minfo[i].icon);
                    } catch (java.io.IOException noImage) {
                    // TBD: use a default ICON if the app has none.
                    }
                }
                mlist.append(minfo[i].name, icon);
            }
    	}
    }
        
    /**
     * Read in and create a MIDletInfor for each MIDlet-&lt;n&gt;
     */
    private void readMIDletInfo() {
        for (int n = 1; n < 100; n++) {
            String nth = "MIDlet-" + n;
            String attr = getAppProperty(nth);
            if (attr == null || attr.length() == 0) {
                break;
            }

            addMIDlet(new MIDletInfo(attr));
        }
    }

    /**
     * Add a MIDlet to the list.
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
}
