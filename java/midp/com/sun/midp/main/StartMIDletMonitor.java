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

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.cldc.isolate.Isolate;

import java.util.Vector;

/**
 * Implements the mechanism to monitor the startup of a MIDlet.
 * It keeps track of which MIDlets are being started
 * and are not yet in the MIDletProxyList.
 * The information is used to avoid starting the same
 * MIDlet twice.
 */
class StartMIDletMonitor implements MIDletProxyListListener {

    /**
     * Reference to the ProxyList.
     */
    private static MIDletProxyList midletProxyList;

    /**
     * Vector of pending start requests.
     */
    private static Vector startPending = new Vector();

    /**
     * The id of the MIDlet suite being started.
     */
    private int suiteId;

    /**
     * The midlet of the MIDlet being started.
     */
    private String midlet;

    /**
     * The IsolateID of the MIDlet being started.
     */
    private Isolate isolate;

    /**
     * Initializes StartMIDletMonitor class.
     * Shall only be called from AmsUtil.
     * No need in security checks since it is package private method.
     *
     * @param theMIDletProxyList MIDletController's container
     */
    static void initClass(MIDletProxyList theMIDletProxyList) {

        midletProxyList = theMIDletProxyList;
    }

    /**
     * Construct a new StartMIDletMonitor instance to track the
     * process of starting a MIDlet in a new Isolate.
     * The new instance is appended to the startPending vector
     * and is registered with the MIDletProxyList to receive
     * notifications if/when the MIDlet starts/fails to start.
     *
     * @param id ID of an installed suite
     * @param midletClassName class name of MIDlet to invoke
     */
    private StartMIDletMonitor(int id, String midletClassName) {
        suiteId = id;
        midlet = midletClassName;
        startPending.addElement(this);
        midletProxyList.addListener(this);
    }

    /**
     * Sets the Isolate associated with this starting MIDlet.
     * It is used to cleanup the Isolate if the start does not
     * start correctly.
     *
     * @param newIsolate the Isolate used to start the MIDlet
     */
    void setIsolate(Isolate newIsolate) {
        isolate = newIsolate;
    }

    /**
     * Check if the MIDlet is already in the ProxyList or is
     * already being started.  If so, return.
     * If not, start it. Register with the proxy list and
     * cleanup when the start of the MIDlet
     * succeeds (and is now in the ProxyList) or
     * fails (and is eligible to be started again).
     *
     * @param id ID of an installed suite
     * @param midletClassName class name of MIDlet to invoke; may be null
     * @return the new StartMIDletMonitor to allow the MIDlet to be started;
     *    null if the MIDlet is already active or being started
     */
    static StartMIDletMonitor okToStart(int id, String midletClassName) {
        synchronized (startPending) {
            // Verify that the requested MIDlet is not already running
            // (is not in the MIDletProxyList)
            if (midletProxyList.isMidletInList(id, midletClassName)) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                           "MIDlet already running; execute ignored");
                }
                return null;
            }

            /*
             * Find the StartMIDletMonitor instance
             * to track the startup, (if any)
             */
            StartMIDletMonitor start = findMonitor(id, midletClassName);
            if (start == null) {
                // Not already starting; register new start
                start = new StartMIDletMonitor(id, midletClassName);
            } else {
                // MIDlet is already started; return null
                start = null;
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_CORE,
                           "MIDlet already started; execute ignored");
                }
            }
            return start;
        }
    }

    /**
     * Scan the startPending list for a matching id and MIDlet.
     * The caller must synchronize using {@link #startPending}.
     * If <code>midlet</code> is null then it only checks to see
     * if the suite is started and returns any monitor for the suite.
     * To prevent using stale Isolate state; check that the Isolate (if any)
     * has not terminated.
     *
     * @param id ID of an installed suite
     * @param midlet class name of MIDlet to invoke
     * @return a StartMIDletMonitor entry with id and midlet;
     *    otherwise <code>null</code>
     */
    private static StartMIDletMonitor findMonitor(int id,
                                                  String midletClassName) {
	for (int i = 0; i < startPending.size(); i++) {
	    StartMIDletMonitor pending =
		(StartMIDletMonitor)startPending.elementAt(i);
	    // If there is a terminated Isolate in the list, clean it up
	    if (pending.isolate != null &&
		pending.isolate.isTerminated()) {
		// Isolate is not alive, clean the pending entry
		startPending.removeElementAt(i);
		midletProxyList.removeListener(pending);
		// Recheck the element at the same index
		i--;
		continue; // keep looking
	    }

	    if (id == pending.suiteId &&
                    (midletClassName == null ||
                     midletClassName.equals(pending.midlet))) {
		return pending;
	    }
	}
	return null;
    }

    /**
     * Cleanup the matching entry in the startPending list.
     * Once removed; the MIDlet will be eligible to be started
     * again.
     * @param id ID of an installed suite of the notifying MIDlet
     * @param midletClassName class name of MIDlet of the notifying MIDlet
     */
    private void cleanupPending(int id, String midletClassName) {
	synchronized (startPending) {
	    // If the notification is for this monitor
	    if (id == suiteId &&
		(midletClassName == null || midletClassName.equals(midlet))) {
		// Remove from the startPending list
		startPending.removeElement(this);

		// Remove the instance as a listener of the MIDletProxyList
		midletProxyList.removeListener(this);
	    }
	}
    }

    /**
     * Called when a MIDlet is added to the list.
     * If there's a match in the startPending list clean it up.
     *
     * @param midletProxy The proxy of the MIDlet being added
     */
    public void midletAdded(MIDletProxy midletProxy) {
        IsolateMonitor.addIsolate(midletProxy, isolate);
        cleanupPending(midletProxy.getSuiteId(), midletProxy.getClassName());
    }

    /**
     * Called when the state of a MIDlet in the list is updated.
     * If there's a match in the startPending list clean it up.
     *
     * @param midletProxy The proxy of the MIDlet that was updated
     * @param fieldId code for which field of the proxy was updated
     */
    public void midletUpdated(MIDletProxy midletProxy, int fieldId) {
    }

    /**
     * Called when a MIDlet is removed from the list.
     * If there's a match in the startPending list clean it up.
     *
     * @param midletProxy The proxy of the removed MIDlet
     */
    public void midletRemoved(MIDletProxy midletProxy) {
        cleanupPending(midletProxy.getSuiteId(), midletProxy.getClassName());
    }

    /**
     * Called when error occurred while starting a MIDlet object.
     * If there's a match in the startPending list clean it up.
     *
     * @param externalAppId ID assigned by the external application manager
     * @param suiteId Suite ID of the MIDlet
     * @param className Class name of the MIDlet
     * @param errorCode start error code
     * @param errorDetails start error details
     */
    public void midletStartError(int externalAppId, int suiteId,
                                 String className, int errorCode,
                                 String errorDetails) {
        cleanupPending(suiteId, className);
    }
}
