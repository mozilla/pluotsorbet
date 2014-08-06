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

import com.sun.cldc.isolate.Isolate;



/**
 * Implements the mechanism to monitor MIDlet suites isolate.
 * The StartMIDletMonitor provides the isolate references at it gets
 * a MIDlet create notification from the MIDletProxyList. When an isolate
 * terminates it notifies the native application manager. The notification is
 * needed for native auto testers because the notification that the test MIDlet
 * is destoryed does not indicate that the VM has closed it JAR.
 */
class IsolateMonitor implements MIDletProxyListListener {

    /** Reference to the ProxyList. */
    private static MIDletProxyList midletProxyList;

    /** Reference to the Isolate Monitor. */
    private static IsolateMonitor monitor;

    /** Table of isolates indexed by midlet proxies. */
    private Hashtable isolates;

    /** What objects should get list changes. */
    private Vector listeners;

    /**
     * Initializes StartMIDletMonitor class.
     * Shall only be called from AmsUtil.
     * No need in security checks since it is package private method.
     *
     * @param theMIDletProxyList MIDletController's container
     */
    static void initClass(MIDletProxyList theMIDletProxyList) {

        midletProxyList = theMIDletProxyList;

        monitor = new IsolateMonitor();
    }

    /**
     * Construct a new StartMIDletMonitor instance to track the
     * process of starting a MIDlet in a new Isolate.
     * The new instance is appended to the startPending vector
     * and is registered with the MIDletProxyList to receive
     * notifications if/when the MIDlet starts/fails to start.
     */
    private IsolateMonitor() {
        isolates = new Hashtable();
        listeners = new Vector();
        midletProxyList.addListener(this);
    }

    /**
     * Adds the Isolate associated with a started MIDlet.
     *
     * @param proxy proxy of a started MIDlet
     * @param isolate the Isolate of the started MIDlet, for systems that
     *                run multiple Isolates from a suite concurrently this
     *                may be a duplicate isolate
     */
    static void addIsolate(MIDletProxy proxy, Isolate isolate) {
        synchronized (monitor.isolates) {
            monitor.isolates.put(proxy, isolate);
        }
    }

    /**
     * Add a listener for MIDlet suite terminations.
     *
     * @param listener Isolate monitor listener
     */
    public static void addListener(IsolateMonitorListener listener) {
        monitor.listeners.addElement(listener);
    }

    /**
     * Called when a MIDlet is added to the list.
     *
     * @param midlet The proxy of the MIDlet being added
     */
    public void midletAdded(MIDletProxy midlet) {
    }

    /**
     * Called when the state of a MIDlet in the list is updated.
     *
     * @param midlet The proxy of the MIDlet that was updated
     * @param fieldId code for which field of the proxy was updated
     */
    public void midletUpdated(MIDletProxy midlet, int fieldId) {
    }

    /**
     * Called when a MIDlet is removed from the list.
     * If this is the last MIDlet in its isolate then start a termination
     * notification thread to wait on its isolate.
     *
     * @param proxy The proxy of the removed MIDlet
     */
    public void midletRemoved(MIDletProxy proxy) {
        synchronized (isolates) {
            if (lastInIsolate(proxy)) {
                startNotificationThread(proxy);
            }

            isolates.remove(proxy);
        }
    }

    /**
     * Called when error occurred while starting a MIDlet object.
     *
     * @param externalAppId ID assigned by the external application manager
     * @param suiteId Suite ID of the MIDlet
     * @param className Class name of the MIDlet
     * @param errorCode start error code
     * @param errorDetails start error details
     */
    public void midletStartError(int externalAppId, int suiteId,
                                 String className, int errorCode,
                                 String errorDetails) {}

    /**
     * Determine if a MIDlet is the last MIDlet in an isolate.
     *
     * @param proxy proxy of MIDlet
     *
     * @return true if the MIDlet is the last MIDlet in the isolate
     */
    private boolean lastInIsolate(MIDletProxy proxy) {
        Isolate isolate = (Isolate)isolates.get(proxy);
        int midletCount = 0;

        if (isolate != null) {
            Enumeration enum = isolates.elements();

            while (enum.hasMoreElements()) {
                Isolate current = (Isolate)enum.nextElement();

                if (current == isolate) {
                    midletCount++;
                }
            }
        }

        return midletCount == 1;
    }

    /**
     * Start a thread that will wait the MIDlet's isolate to terminate and
     * then notify the native application manager. This method will also
     * remove the MIDlet from the monitor's MIDlet list.
     *
     * @param proxy proxy of MIDlet
     */
    private void startNotificationThread(MIDletProxy proxy) {
        Isolate isolate = (Isolate)isolates.get(proxy);
        TerminationNotifier notifier = new TerminationNotifier();

        notifier.midlet = proxy;
        notifier.isolate = isolate;
        notifier.parent = this;

        new Thread(notifier).start();
    }

    /**
     * Notifies the listeners that a suite has terminated.
     *
     * @param suiteId ID of the MIDlet suite
     * @param className class name of the MIDlet suite
     */
    void notifyListeners(int suiteId, String className) {
        synchronized (listeners) {
            for (int i = 0; i < listeners.size(); i++) {
                IsolateMonitorListener listener =
                    (IsolateMonitorListener)listeners.elementAt(i);

                listener.suiteTerminated(suiteId, className);
            }
        }
    }
}

/**
 * Waits for an isolate to terminate and then notifies the native app
 * manager.
 */
class TerminationNotifier implements Runnable {
    /** MIDlet information. */
    MIDletProxy midlet;

    /** Isolate of the MIDlet. */
    Isolate isolate;

    /** Parent monitor. */
    IsolateMonitor parent;

    /** Performs this classes function. */
    public void run() {
        isolate.waitForExit();
        parent.notifyListeners(midlet.getSuiteId(), midlet.getClassName());
    }
}
