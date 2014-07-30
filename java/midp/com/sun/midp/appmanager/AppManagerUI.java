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

import com.sun.midp.main.*;

import javax.microedition.lcdui.Displayable;


interface AppManagerUI {

    /**
     * The AppManagerPeer manages list of available MIDlet suites
     * and informs AppManagerUI regarding changes in list through
     * itemAppended callback when new item is appended to the list.
     *
     * The order in which the MIDlets are shown is up to the UI
     * and need not be the order of itemAppended invocations.
     *  
     * @param suiteInfo the midlet suite info
     */
    void itemAppended(RunningMIDletSuiteInfo suiteInfo);

    /**
     * The AppManagerPeer manages list of available MIDlet suites
     * and informs AppManagerUI regarding changes in list through
     * itemRemoved callback when item is removed from the list.
     *
     * @param suiteInfo the midlet suite info
     */
    void itemRemoved(RunningMIDletSuiteInfo suiteInfo);

    /**
     * Called when a new internal midlet was launched
     *
     * @param midlet proxy of a newly launched MIDlet
     */
    void notifyInternalMidletStarted(MIDletProxy midlet);

    /**
     * Called when a new midlet was launched.
     *
     * @param si corresponding midlet suite info
     * @param className MIDlet class name
     */
    void notifyMidletStarted(RunningMIDletSuiteInfo si, String className);

    /**
     * Called when state of a running midlet has changed.
     *
     * @param si corresponding midlet suite info
     * @param midlet
     */
    void notifyMidletStateChanged(RunningMIDletSuiteInfo si, MIDletProxy midlet);

    /**
     * Called when a running internal midlet exited.
     * @param midlet proxy of the midlet that has exited
     */
    void notifyInternalMidletExited(MIDletProxy midlet);

    /**
     * Called when a running midlet exited.
     * @param si corresponding midlet suite info
     * @param midletClassName Class name of the exited midlet
     */
    void notifyMidletExited(RunningMIDletSuiteInfo si, String midletClassName);

    /**
     * Called by AppManagerPeer after a MIDlet suite
     * is successfully installed on the device,
     * to ask the user whether or not to launch
     * the MIDlet from the suite. 
     * @param si corresponding suite info
     */
    void notifySuiteInstalled(RunningMIDletSuiteInfo si);

    /**
     * Called when a new MIDlet suite is installed externally.
     * @param si corresponding suite info
     */
    void notifySuiteInstalledExt(RunningMIDletSuiteInfo si);

    /**
     * Called when a suite exited (lastr running MIDlet in suite exited).
     * @param suiteInfo Suite which just exited
     */
    void notifySuiteExited(RunningMIDletSuiteInfo suiteInfo);

    /**
     * Called when MIDlet selector exited.
     * @param suiteInfo Containing ID of suite
     */
    void notifyMIDletSelectorExited(RunningMIDletSuiteInfo suiteInfo);
    
    /**
     * Called when a MIDlet suite has been removed externally.
     * @param si corresponding suite info
     */
    void notifySuiteRemovedExt(RunningMIDletSuiteInfo si);

    /**
     * Called when MIDlet suite being enabled
     * @param si corresponding suite info
     */
    void notifyMIDletSuiteEnabled(RunningMIDletSuiteInfo si);

    /**
     * Called when MIDlet suite icon hase changed
     * @param si corresponding suite info
     */
    void notifyMIDletSuiteIconChaged(RunningMIDletSuiteInfo si);

    /**
     * Called when a midlet could not be launched.
     *
     * @param suiteId suite ID of the MIDlet
     * @param className class name of the MIDlet
     * @param errorCode error code
     * @param errorDetails error code details
     */
    void notifyMidletStartError(int suiteId, String className, int errorCode,
                                String errorDetails);

    /**
     * Called when state of the midlet changes.
     *
     * @param si corresponding suite info
     * @param newSi new suite info
     */
    void notifyMIDletSuiteStateChanged(RunningMIDletSuiteInfo si,
                                             RunningMIDletSuiteInfo newSi);

    /**
     * Requests that the ui element, associated with the specified midlet
     * suite, be visible and active.
     * 
     * @param item corresponding suite info
     */
    void setCurrentItem(RunningMIDletSuiteInfo item);
   
    /**
     * Called to determine MidletSuiteInfo of the last selected Item.
     * Is used to restore selection in the app manager.
     *
     * @return last selected MidletSuiteInfo
     */
    RunningMIDletSuiteInfo getSelectedMIDletSuiteInfo();

    /**
     * Called when midlet switcher is needed.
     *
     * @param onlyFromLaunchedList true if midlet should
     *        be selected from the list of already launched midlets,
     *        if false then possibility to launch midlet is needed.
     */
    void showMidletSwitcher(boolean onlyFromLaunchedList);

    /**
     * Called when midlet selector is needed. Should show a list of
     * midlets present in the given suite and allow to select one.
     *
     * @param msiToRun a suite from which a midlet must be selected
     */
    void showMidletSelector(RunningMIDletSuiteInfo msiToRun);

    /**
     * Called by Manager when destroyApp happens to clean up data.
     */
    void cleanUp();

    /**
     * Returns the main displayable of the AppManagerUI.
     * @return main screen
     */
    Displayable getMainDisplayable();

}
