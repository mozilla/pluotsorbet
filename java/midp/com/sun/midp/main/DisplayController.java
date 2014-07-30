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

import java.util.*;

/**
 * This class controls which MIDlet's display is in the foreground.
 * Running only in the AMS Isolate (0) the controller consulted by the MIDlet
 * proxy list for any foreground when various state changes occur in a MIDlet.
 * The display controller automatically selects the next foreground if
 * needed.
 * <p>
 * From the user perspective when the last MIDlet the user launched sets its
 * current displayable for the first time, that MIDlet should automatically
 * get the foreground (see the midletCreated and foregroundRequest methods).
 * <p>
 * A MIDlet that is paused or destroyed is treated as if it has requested the
 * background as described above.
 */
class DisplayController {
    /**
     * The last MIDlet added to the MIDlet proxy list, but has not requested
     * to be in the foreground.
     */
    protected MIDletProxy lastMidletCreated;

    /** Cache of the MIDlet proxy list reference. */
    protected MIDletProxyList midletProxyList;

    /** What objects want to listen. */
    private Vector listeners = new Vector(1, 1);

    /**
     * Construct a DisplayController with a reference to the ProxyList.
     *
     * @param theMIDletProxyList reference to the MIDlet proxy list
     */
    protected DisplayController(MIDletProxyList theMIDletProxyList) {
        midletProxyList = theMIDletProxyList;
    }

    /**
     * Add a listener.
     *
     * @param listener DisplayController listener
     */
    public void addListener(DisplayControllerListener listener) {
        listeners.addElement(listener);
    }

    /**
     * Remove a listener for DisplayController.
     *
     * @param listener DisplayController listener
     */
    public void removeListener(DisplayControllerListener listener) {
        listeners.removeElement(listener);
    }

    /**
     * Update the last MIDlet created field so when the wantsForeground
     * field of the MIDletProxy is updated to be true, the MIDlet can
     * be automatically put into the foreground.
     * <p>
     * Called when a MIDlet is created to the proxy list.
     *
     * @param midlet The proxy of the MIDlet being created
     */
    void midletCreated(MIDletProxy midlet) {
        lastMidletCreated = midlet;
    }

    /**
     * Called when a MIDlet is move to active state.
     *
     * @param midlet The proxy of the MIDlet being activated
     *
     * @return Proxy of the next foreground MIDlet, may be the foreground
     *         MIDlet if the foreground should not change
     */
    MIDletProxy midletActive(MIDletProxy midlet) {
        return midletProxyList.getForegroundMIDlet();
    }

    /**
     * Handles any possible foreground changes due the state of a MIDlet
     * changing to paused.
     * <p>
     * Treat this state change as a background request.
     * <p>
     * Called when the state of a MIDlet in the MIDlet proxy list is paused.
     *
     * @param midlet The proxy of the MIDlet that was updated
     *
     * @return Proxy of the next foreground MIDlet, may be the foreground
     *         MIDlet if the foreground should not change
     */
    MIDletProxy midletPaused(MIDletProxy midlet) {
        return backgroundRequest(midlet);
    }

    /**
     * If the removed MIDlet is the foreground MIDlet find a new
     * foreground MIDlet. After clearing the last midlet created, treat this
     * state change as background request.
     * <p>
     * Called when a MIDlet is removed from the proxy list.
     *
     * @param midlet The proxy of the removed MIDlet
     *
     * @return Proxy of the next foreground MIDlet, may be the foreground
     *         MIDlet if the foreground should not change
     */
    MIDletProxy midletDestroyed(MIDletProxy midlet) {
        clearLastMidletCreated(midlet);

        return backgroundRequest(midlet);
    }

    /**
     * Handles MIDlet foreground requests.
     * <p>
     * If proxy being updated belongs last MIDlet created in the proxy list,
     * then put the MIDlet in the foreground.
     * <p>
     * If there is no foreground MIDlet or the foreground MIDlet does not
     * want the foreground or the foreground MIDlet is paused, then put the
     * MIDlet in the foreground.
     *
     * @param midlet The proxy of the MIDlet that was updated
     *
     * @return Proxy of the next foreground MIDlet, may be the foreground
     *         MIDlet if the foreground should not change
     */
    MIDletProxy foregroundRequest(MIDletProxy midlet) {
        MIDletProxy foreground;

        /*
         * When the last MIDlet started wants the foreground automatically
         * put in the foreground this time only.
         */
        if (midlet == lastMidletCreated) {
            return midlet;
        }

        foreground = midletProxyList.getForegroundMIDlet();
        if (foreground == null || !foreground.wantsForeground() ||
            foreground.getMidletState() == MIDletProxy.MIDLET_PAUSED) {
            return midlet;
        }

        // don't change the foreground
        return foreground;
    }

    /**
     * Handles MIDlet background requests.
     * <p>
     * If the MIDlet is requesting to be put in the background is the
     * foreground MIDlet, then find a MIDlet to bring to the foreground
     * (see the findNextForeground method).
     *
     * @param midlet The proxy of the MIDlet that was updated
     *
     * @return Proxy of the next foreground MIDlet, may be the foreground
     *         MIDlet if the foreground should not change
     */
    MIDletProxy backgroundRequest(MIDletProxy midlet) {
        MIDletProxy foreground = midletProxyList.getForegroundMIDlet();

        if (midlet != foreground) {
            // not in the foreground, so don't change the foreground
            return foreground;
        }

        return findNextForegroundMIDlet();
    }

    /**
     * Find a MIDlet that wants the foreground. If none wants the foreground
     * then find one that is not paused, if no find one that is paused
     * and wants the foreground, then find one that is paused.
     *
     * @return new foreground task or null
     */
    private MIDletProxy findNextForegroundMIDlet() {
        Enumeration midlets;

        // find the first task that is active and wants foreground
        midlets = midletProxyList.getMIDlets();
        while (midlets.hasMoreElements()) {
            MIDletProxy current = (MIDletProxy)midlets.nextElement();

            if (current.getMidletState() != MIDletProxy.MIDLET_ACTIVE) {
                continue;
            }

            if (current.wantsForeground()) {
                return current;
            }
        }

        // find the first task that is active
        midlets = midletProxyList.getMIDlets();
        while (midlets.hasMoreElements()) {
            MIDletProxy current = (MIDletProxy)midlets.nextElement();

            if (current.getMidletState() != MIDletProxy.MIDLET_ACTIVE) {
                return current;
            }
        }

        // find the first task that is paused and wants the foreground
        midlets = midletProxyList.getMIDlets();
        while (midlets.hasMoreElements()) {
            MIDletProxy current = (MIDletProxy)midlets.nextElement();

            if (current.getMidletState() != MIDletProxy.MIDLET_PAUSED) {
                continue;
            }

            if (current.wantsForeground()) {
                return current;
            }
        }

        // find the first task that is paused
        midlets = midletProxyList.getMIDlets();
        while (midlets.hasMoreElements()) {
            MIDletProxy current = (MIDletProxy)midlets.nextElement();

            if (current.getMidletState() != MIDletProxy.MIDLET_PAUSED) {
                return current;
            }
        }

        return null;
    }

    /**
     * Request a transfer of the foreground from one MIDlet to another.
     * The transfer only succeeds if the current foreground is the "from"
     * MIDlet.
     * @param origin the MIDletProxy from which the FG should transfer from
     * @param target the MIDletProxy to which the FG should transfer to
     * @return the choice about which to become the FG
     */
    MIDletProxy transferRequest(MIDletProxy origin,
                                       MIDletProxy target) {
        MIDletProxy foreground = midletProxyList.getForegroundMIDlet();
        if (origin == foreground) {
            // Foreground is the requesting MIDlet, change to target
            return target;
        }

        // The foreground is not the requesting origin; don't change
        return foreground;
    }
    
    /**
     * Called to process a preempt event. The default is to preempt the
     * foreground.
     *
     * @param preempting proxy of the preempting MIDlet to be put in the
     *  foreground when a preempted MIDlet gets the foreground or to be
     *  put in the foreground directly.
     *
     * @return Proxy of the next foreground MIDlet, may be the foreground
     *         MIDlet if the foreground should not change
     * 
     */
    MIDletProxy startPreempting(MIDletProxy preempting) {
        MIDletProxy foreground;
        
        foreground = midletProxyList.getForegroundMIDlet();
        if (foreground != null) {
            preempting.setPreemptedMidlet(foreground);
            foreground.setPreemptingDisplay(preempting);
        }

        return preempting;
    }

    /**
     * End the preempt an Isolate's displays.
     *
     * @param isolateId isolate ID of display that done preempting
     * @param displayId display ID of display that done preempting
     *
     * @return Proxy of the next foreground MIDlet, may be the foreground
     *         MIDlet if the foreground should not change
     * 
     */
    MIDletProxy endPreempting(int isolateId, int displayId) {
        Enumeration midlets;
        MIDletProxy foreground;
        MIDletProxy preempted;
        MIDletProxy preempting = null;

        /*
         * Stop preempting all of the MIDlets that were preempted.
         */
        midlets = midletProxyList.getMIDlets();
        while (midlets.hasMoreElements()) {
            MIDletProxy current = (MIDletProxy)midlets.nextElement();
            MIDletProxy temp;

            temp = current.getPreemptingDisplay();
            if (temp == null) {
                continue;
            }

            if (temp.getIsolateId() != isolateId) {
                continue;
            }

            if (!temp.containsDisplay(displayId)) {
                continue;
            }
            preempting = temp;

            current.setPreemptingDisplay(null);
            midletProxyList.notifyListenersOfProxyUpdate(current, 
                MIDletProxyListListener.PREEMPTING_DISPLAY);
        }

        foreground = midletProxyList.getForegroundMIDlet();
        if (foreground == null) {
            return null;
        }

        if (preempting == null) {
            return null;
        }

        // if the preempting display is not in the foreground then do nothing
        if (foreground != preempting) {
            return foreground;
        }

        preempted = preempting.getPreemptedMidlet(); 

        if (preempted != null) {
            return preempted;
        }

        return foreground;
    }

    /**
     * Call to notify that foreground MIDlet is changing and give the
     * display controller a chance to preempt the change.
     * Also the last MIDlet created state will be reset.
     * <p>
     * If the MIDlet to get the foreground is paused, then activate it.
     *
     * @param midlet proxy of the MIDlet to be put in the foreground
     *
     * @return Proxy of the next foreground MIDlet, may be the foreground
     *         MIDlet if the foreground should not change
     */
    MIDletProxy foregroundMidletChanging(MIDletProxy midlet) {
        MIDletProxy preempting;

        if (midlet == null) {
            return null;
        }

        preempting = midlet.getPreemptingDisplay();
        if (preempting != null) {
            return preempting;
        }

        clearLastMidletCreated(midlet);

        if (midlet.getMidletState() == MIDletProxy.MIDLET_PAUSED) {
            midlet.activateMidlet();
        }

        return midlet;
    }


    /**
     * Call to notify display controller that foreground MIDlet 
     * has changed. Currently used by Automation API which 
     * subclasses DisplayController.
     *
     * @param oldForeground proxy of the old foreground MIDlet 
     * @param newForeground proxy of the new foreground MIDlet 
     *
     */
    void foregroundMidletChanged(MIDletProxy oldForeground, 
            MIDletProxy newForeground) {
        // we aren't interested in it, but our subclasses are
    }
    

    /**
     * Clear the last MIDlet created, if it is one given.
     *
     * @param midlet a proxy of the MIDlet that may be the last MIDlet created
     */
    private void clearLastMidletCreated(MIDletProxy midlet) {
        if (lastMidletCreated == midlet) {
            lastMidletCreated = null;
        }
    }

    /**
     * Called to process a select foreground event. Processing this event
     * only needs to be done when application MIDlets are allowed to run
     * concurrently. In SVM mode the display controller returns
     * foreground MIDlet.
     *
     * @param onlyFromLaunchedList true if midlet should
     *        be selected from the list of already launched midlets,
     *        if false then possibility to launch midlet is needed.
     * @return Proxy of the next foreground MIDlet, may be the foreground
     *         MIDlet if the foreground should not change
     */
    MIDletProxy selectForeground(boolean onlyFromLaunchedList) {
        notifyListenersOfSelectForeground(onlyFromLaunchedList);
        return midletProxyList.getForegroundMIDlet();
    }

    /**
     * Notify the listeners of the display controller that foreground
     * selection ui should be launched.
     *
     * @param onlyFromLaunchedList true if midlet should
     *        be selected from the list of already launched midlets,
     *        if false then possibility to launch midlet is needed.
     */
    void notifyListenersOfSelectForeground(boolean onlyFromLaunchedList) {
        for (int i = listeners.size() - 1; i >= 0; i--) {
            DisplayControllerListener listener =
                (DisplayControllerListener)listeners.elementAt(i);

            listener.selectForeground(onlyFromLaunchedList);
        }
    }
}
