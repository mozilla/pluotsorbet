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

package com.sun.midp.main;

import com.sun.midp.lcdui.ForegroundEventProducer;

import com.sun.midp.midlet.MIDletEventProducer;

import com.sun.midp.suspend.SuspendDependency;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Represents the state of a running MIDlet and its Display so that objects
 * do not have to be shared across Isolates. The states in this object are
 * updated by the MIDletProxyList upon receiving a notification event.
 * This class also provides methods for asynchronously changing a MIDlet's
 * state.
 */
public class MIDletProxy implements SuspendDependency {

    /** Constant for active state of a MIDlet. */
    public static final int MIDLET_ACTIVE = 0;

    /** Constant for paused state of a MIDlet. */
    public static final int MIDLET_PAUSED = 1;

    /** Constant for destroyed state of a MIDlet. */
    public static final int MIDLET_DESTROYED = 2;

    /** Cached reference to the ForegroundEventProducer. */
    private static ForegroundEventProducer foregroundEventProducer;

    /** Cached reference to the MIDletEventProducer. */
    private static MIDletEventProducer midletEventProducer;

    /** ID given to this midlet by an external application manager. */
    private int externalId;

    /** ID of the Isolate the MIDlet is running in. */
    private int isolateId;

    /** IDs of the MIDlet's Displays. */
    private int[] displayIds = new int[1];

    /** Number of MIDlet's Displays. */
    private int numOfDisplays = 0;

    /** ID of the suite the MIDlet belongs to. */
    private int suiteId;

    /** Class name of the MIDlet. */
    private String className;

    /** Display name of the MIDlet to show the user. */
    private String displayName;

    /**
     * MIDlet life cycle state. Will be either MIDLET_ACTIVE, MIDLET_PAUSED,
     * or MIDLET_DESTROYED.
     */
    private int midletState;

    /** Indicates that the midlet was just created. */
    boolean wasNotActive;

    /** True if the MIDlet want's its Display in the foreground. */
    private boolean wantsForegroundState;

    /** True if the MIDlet has the foreground at least once. */
    private boolean requestedForeground;

    /** The display that is preempting this MIDlet. */
    private MIDletProxy preempting;

    /** True if alert is waiting for the foreground. */
    private boolean alertWaiting;

    /**
     * The display to put in the foreground after this display stops
     * preempting. If no display in this isolate had the foreground
     * then this will be null.
     */
    private MIDletProxy preempted;

    /** Timer for the MIDlet proxy. Used when a midlet is hanging. */
    private Timer proxyTimer;

    /** Parent list. */
    private MIDletProxyList parent;


    /** Constant for pausing the MIDlet when it's in the background. */
    public static final byte MIDLET_BACKGROUND_PAUSE = 1;

    /** Constant for denying an user to terminate the MIDlet. */
    public static final byte MIDLET_NO_EXIT = 2;

    /** Constant for launching the MIDlet directly in the background. */
    public static final byte MIDLET_LAUNCH_BG = 4;

    /**
     * Cached extended attributes for the MIDlet. The valid values are:
     * MIDLET_BACKGROUND_PAUSE, MIDLET_NO_EXIT and MIDLET_LAUNCH_BG.  
     */
    private byte extendedAttributes;


    /**
     * Initialize the MIDletProxy class. Should only be called by the
     * MIDletProxyList.
     *
     * @param  theForegroundEventProducer reference to the event producer
     * @param  theMIDletEventProducer reference to the event producer
     */
    static void initClass(
        ForegroundEventProducer theForegroundEventProducer,
        MIDletEventProducer theMIDletEventProducer) {

        foregroundEventProducer = theForegroundEventProducer;
        midletEventProducer = theMIDletEventProducer;
    }
    
    /**
     * Construct a new MIDletProxy.
     *
     * @param  theParentList parent MIDlet proxy list
     * @param  theExternalAppId ID of given by an external application manager
     * @param  theIsolateId ID of the Isolate the MIDlet is running in.
     * @param  theSuiteId   ID of the suite MIDlet
     * @param  theClassName Class name of the MIDlet
     * @param  theDisplayName Display name of the MIDlet to show the user
     * @param  theMidletState MIDlet lifecycle state.
     */
    MIDletProxy(MIDletProxyList theParentList, int theExternalAppId,
		int theIsolateId, int theSuiteId,
		String theClassName, String theDisplayName, int theMidletState) {
	
        parent = theParentList;
        externalId = theExternalAppId;
        isolateId = theIsolateId;
        suiteId = theSuiteId;
        className = theClassName;
        displayName = theDisplayName;
        midletState = theMidletState;
        wasNotActive = true;
    }

    /**
     * Get the external application ID used for forwarding changes.
     *
     * @return ID assigned by the external application manager
     */
    public int getExternalAppId() {
        return externalId;
    }

    /**
     * Get the ID of the Isolate the MIDlet is running in. Public for testing
     * purposes.
     *
     * @return ID of the Isolate the MIDlet is running in
     */
    public int getIsolateId() {
        return isolateId;
    }

    /**
     * Sets the ID of the MIDlet's Display.
     *
     * @param id of the MIDlet's Display
     */
    void setDisplayId(int id) {
	if (numOfDisplays == displayIds.length) {
	    int[] newTable = new int[numOfDisplays + 2];
	    
	    /* Grow the display ids table. */
	    for (int i = 0; i < numOfDisplays; i++) {
		newTable[i] = displayIds[i];
	    }
	    
	    displayIds = newTable;
	}

        displayIds[numOfDisplays] = id;
	numOfDisplays++;
    }

    /**
     * Get the IDs of the MIDlet's Displays. Public for testing purposes.
     *
     * @return IDs of the MIDlet's Displays
     */
    public int[] getDisplayIds() {
	int[] ret;
	if (numOfDisplays == displayIds.length) {
	    ret = displayIds;
	} else {
	    ret = new int[numOfDisplays];
	    for (int i = 0; i < numOfDisplays; i++ ) {
		ret[i] = displayIds[i];
	    }
	}
        return ret;
    }
    
    /**
     * Check if the specified display id is added to the list of the displays
     * @param displayId display id 
     * @return true if the display is in the list , false - otherwise
     */
    public boolean containsDisplay(int displayId) {
	boolean ret = false;
	for (int i = numOfDisplays; --i >= 0;) {
	    if (displayIds[i] == displayId) {
		ret = true;
		break;
	    }
	}
	return ret;
    }

    /**
     * Get the ID of the MIDlet's suite.
     *
     * @return ID of the MIDlet's suite
     */
    public int getSuiteId() {
        return suiteId;
    }

    /**
     * Get the class name of the MIDlet.
     *
     * @return class name of the MIDlet
     */
    public String getClassName() {
        return className;
    }

    /**
     * Get the Display name of the MIDlet.
     *
     * @return Display name of the MIDlet
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the MIDlet cycle state. Called by the
     * MIDlet proxy list when it receives an event from the MIDlet
     * to update this value.
     *
     * @param newMidletState new MIDlet state
     */
    void setMidletState(int newMidletState) {
        midletState = newMidletState;
    }

    /**
     * Get the MIDlet lifecycle state.
     *
     * @return MIDlet state
     */
    public int getMidletState() {
        return midletState;
    }

    /**
     * Set the wants foreground state in the proxy. Called by the
     * MIDlet proxy list when it receives an event from the MIDlet's
     * display to update this value.
     *
     * @param newWantsForeground new wants foreground value.
     * @param isAlert true if the displayable requesting the foreground,
     *        is an Alert, this parameter is ignored if newWantsForeground
     *        is false
     */
    void setWantsForeground(boolean newWantsForeground, boolean isAlert) {
        wantsForegroundState = newWantsForeground;

        if (newWantsForeground) {
            requestedForeground = true;
            alertWaiting = isAlert;
        } else {
            alertWaiting = false;
        }
    }

    /**
     * Check if the MIDlet want's its Display in the foreground.
     *
     * @return true if the MIDlet want's its Display in the foreground
     */
    public boolean wantsForeground() {
        return wantsForegroundState;
    }

    /**
     * Check if the MIDlet has not created its display.
     *
     * @return true if the MIDlet has no display.
     */
    public boolean noDisplay() {
        return numOfDisplays == 0;
    }

    /**
     * Check if the MIDlet has not set a displayable in its display.
     * Used by foreground selector to determine if the MIDlet it is
     * about to put in the foreground will draw the screen.
     *
     * @return true if the MIDlet has no displayable.
     */
    public boolean noDisplayable() {
        return !requestedForeground;
    }

    /**
     * Set the proxy of the display that is preempting this MIDlet.
     *
     * @param preemptingDisplay the preempting display
     */
    void setPreemptingDisplay(MIDletProxy preemptingDisplay) {
        // Turn on the user notification status for this proxy
        if (preemptingDisplay != null) {
            alertWaiting = true;
        } else {
            if (preempting != null) {
                /*
                 * There could be a proxy timer waiting to destroy the
                 * isolate if the user ended the alert with the end MIDlet
                 * button, so cancel the timer.
                 */
                preempting.setTimer(null);
            }

            alertWaiting = false;
        }

        preempting = preemptingDisplay;
    }

    /**
     * Get the proxy of the display that is preempting this MIDlet.
     *
     * @return the preempting display
     */
    MIDletProxy getPreemptingDisplay() {
        return preempting;
    }

    /**
     * Set the proxy of the MIDlet that should get the foreground
     * after preempting is done.
     *
     * @param preemptedDisplay the preempted display
     */
    void setPreemptedMidlet(MIDletProxy preemptedDisplay) {
        preempted = preemptedDisplay;
    }

    /**
     * Get the proxy of the MIDlet that should get the foreground
     * after preempting is done.
     *
     * @return the preempted display or null for none
     */
    MIDletProxy getPreemptedMidlet() {
        return preempted;
    }

    /**
     * Called to determine if alert is waiting for the foreground.
     *
     * @return true if an alert of the MIDlet is waiting in background.
     */
    public boolean isAlertWaiting() {
        return alertWaiting;
    }

    /**
     * Asynchronously change the MIDlet's state to active.
     *
     * This method does NOT change the state in the proxy, but
     * sends a activate MIDlet event to the MIDlet's Display.
     * The state in the proxy is only update when the MIDlet sends
     * a MIDlet activated event to the proxy list.
     */
    public void activateMidlet() {
        if (midletState != MIDLET_DESTROYED) {
            wasNotActive = false;
            midletEventProducer.sendMIDletActivateEvent(isolateId, className);
        }
    }

    /**
     * Asynchronously change the MIDlet's state to paused.
     *
     * This method does NOT change the state in the proxy, but
     * sends a pause MIDlet event to the MIDlet's Display.
     * The state in the proxy is only update when the MIDlet sends
     * a MIDlet paused event to the proxy list.
     */
    public void pauseMidlet() {
        if (midletState != MIDLET_DESTROYED) {
            midletEventProducer.sendMIDletPauseEvent(isolateId, className);
        }
    }

    /**
     * Terminates ther MIDlet if it is neither paused nor destroyed.
     */
    public void terminateNotPausedMidlet() {
        if (midletState != MIDLET_DESTROYED && midletState != MIDLET_PAUSED) {
            MIDletProxyUtils.terminateMIDletIsolate(this, parent);
        }
    }

    /**
     * Asynchronously change the MIDlet's state to destroyed.
     *
     * This method does NOT change the state in the proxy, but
     * sends request to destroy MIDlet event to the AMS.
     * The state in the proxy is only update when the MIDlet sends
     * a MIDlet destroyed event to the proxy list.
     */
    public void destroyMidlet() {
        if (midletState != MIDLET_DESTROYED) {
            if (getTimer() != null) {
                // A destroy MIDlet event has been sent.
                return;
            }

            MIDletDestroyTimer.start(this, parent);

            midletEventProducer.sendMIDletDestroyEvent(isolateId, className);
        }
    }

    /** Process a MIDlet destroyed notification. */
    void destroyedNotification() {
        setTimer(null);
        setMidletState(MIDLET_DESTROYED);
    }

    /**
     * Notify the midlet's display of a foreground change. Called by
     * the MIDlet proxy list to notify the old and new foreground displays
     * of a foreground change.
     *
     * @param hasForeground true if the target is being put in the foreground
     */
    void notifyMIDletHasForeground(boolean hasForeground) {
        if (hasForeground) {
            alertWaiting = false;
	    for (int i = displayIds.length; --i >= 0;) { 
		foregroundEventProducer.sendDisplayForegroundNotifyEvent(
							  isolateId, displayIds[i]);
	    }
        } else {
	    for (int i = displayIds.length; --i >= 0;) { 
		foregroundEventProducer.sendDisplayBackgroundNotifyEvent(
							  isolateId, displayIds[i]);
	    }
        }
    }

    /**
     * Sets the timer object
     *
     * @param t Timer object
     */
    void setTimer(Timer t) {
        proxyTimer = t;
    }

    /**
     * Gets the timer object
     *
     * @return Timer
     */
    Timer getTimer() {
        return proxyTimer;
    }

    /**
     * Set the boolean attribute in the attributes' cache to true.
     *
     * The method is used to cache run time extended MIDlet attribute for better
     * performance. Default value for an atrribute is false.
     *
     * @param attribute extended MIDlet attribute, the valid values are:
     *                  MIDLET_BACKGROUND_PAUSE, MIDLET_NO_EXIT and
     *                  MIDLET_LAUNCH_BG
     * 
     * @see #getExtendedAttribute
     */
    void setExtendedAttribute(byte attribute) {
        extendedAttributes |= attribute;
    }

    /**
     * Retrives the boolean attribute from the attributes' cache.
     *
     * @param attribute extended MIDlet attribute, the valid values are:
     *                  MIDLET_BACKGROUND_PAUSE, MIDLET_NO_EXIT and
     *                  MIDLET_LAUNCH_BG
     *
     * @return If setExtendedAttribute was called for the attribute return true,
     *         false otherwise.
     *
     * @see #setExtendedAttribute
     */
    public boolean getExtendedAttribute(byte attribute) {
        return (extendedAttributes & attribute) != 0;
    }

    /**
     * Print the state of the proxy.
     *
     * @return printable representation of the state of this object
     */
    public String toString() {
	String displays = "";
	for (int i = 0; i < numOfDisplays; i++) {
	    displays += displayIds[i];
	    if (i < numOfDisplays - 1) {
		displays += ", ";
	    }
	}

        return "MIDletProxy: suite id = " + suiteId +
            "\n    class name = " + className +
            "\n    display name = " + displayName +
            "\n    isolate id = " + isolateId +
            ", number of displays  = " + numOfDisplays +
	    ", display ids = " + displays +
            ", midlet state = " + midletState +
            ", wantsForeground = " + wantsForegroundState +
            ", requestedForeground = " + requestedForeground +
            "\n    alertWaiting = " + alertWaiting;
    }
}

/**
 * If the MIDlet is hanging this class will start a
 * timer and terminate the MIDlet when the timer
 * expires.  The timer will not work in SVM mode.
 */
class MIDletDestroyTimer {
    /** Timeout to let MIDlet destroy itself. */
    private static final int TIMEOUT =
        1000 * Configuration.getIntProperty("destoryMIDletTimeout", 5);

    /**
     * Starts timer for the specified MIDlet (proxy) .
     *
     * @param mp MIDletProxy to terminate if not destroyed in time
     * @param mpl the MIDletProxyList
     */
    static void start(final MIDletProxy mp, final MIDletProxyList mpl) {
        Timer timer = new Timer();
        mp.setTimer(timer);

        TimerTask task = new TimerTask() {
            /** Terminates MIDlet isolate and updates the proxy list. */
            public void run() {
                if (mp.getTimer() != null) {
                    MIDletProxyUtils.terminateMIDletIsolate(mp, mpl);
                }
                cancel();
            }
        };

        timer.schedule(task, TIMEOUT);
    }
}
