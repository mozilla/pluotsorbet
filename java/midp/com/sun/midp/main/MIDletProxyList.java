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

import com.sun.j2me.security.AccessController;

import com.sun.midp.events.EventQueue;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.security.SecurityInitializer;

import com.sun.midp.suspend.SuspendSystem;
import com.sun.midp.suspend.SuspendSystemListener;

import com.sun.midp.configurator.Constants;

/**
 * Manages a list of MIDlet proxies, each proxy representing a running MIDlet
 * and tracks which MIDlet has the foreground display, the list only available
 * to objects running in the AMS isolate.
 * <p>
 * The list is updated upon receiving events.  See the process method for event
 * processing.
 * <p>
 * Objects can listen (see MIDletProxyListListener) to the list for additions,
 * updates to proxies, and removals as will as set foreground events and
 * select foreground events.
 * <p>
 * This class also provides a shutdown method and processes a shutdown event
 * to enable the method be used by native code.
 *
 */
public class MIDletProxyList
        implements MIDletControllerEventConsumer, SuspendSystemListener {

    /** MIDletProxy added constant. */
    static final int PROXY_ADDED = 0;

    /** MIDletProxy removed constant. */
    static final int PROXY_REMOVED = 1;

    /** The one and only MIDlet proxy list. */
    private static MIDletProxyList midletProxyList;

    /** True when the system is shutting down. */
    private static boolean shutdownFlag;

    /** What objects should get list changes. */
    private Vector listeners = new Vector(2, 2);

    /** Vector to hold MIDlet proxies. */
    private Vector midletProxies = new Vector(5, 5);

    /** The foreground MIDlet. */
    private MIDletProxy foregroundMidlet;

    /** The one and only displayController. */
    private DisplayController displayController;

    /** True if all midlets are paused, false - otherwise */
    private boolean allPaused; // = false

    /**
     * MIDlet proxy whose peer application runs in the AMS isolate. Should
     * be accessed through findAmsProxy(), may be invalid otherwise.
     */
    private MIDletProxy amsProxy;

    /**
     * Class registered in SecurityInitializer that identifies this
     * implementation permissions for accessing restricted API's
     */
    private static class SecurityTrusted implements ImplicitlyTrustedClass {}

    /** Security token for provileged access to restricted API's. */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /**
     * Called by the MIDlet suite loader in AMS Isolate to intialize the
     * midletProxy list. Shall be called only by MIDletSuiteLoader's main().
     *
     * @param theMIDletProxyList proxy list instance to be used
     *                           as MIDlet controller container
     *
     * Should only be called in the AMS Isolate.
     */
    static void initClass(MIDletProxyList theMIDletProxyList) {

        /*
        TBD: the code below is commented until
        the issue with non-static Logging.assertTrue
        will be fixed !

        Logging.assertTrue(theMIDletProxyList != null,
                           "theMIDletProxyList must be non-null");
        Logging.assertTrue(midletProxyList == null,
                           "midletProxyList must be initialized only once");
        */
        midletProxyList = theMIDletProxyList;
    }

    /**
     * Get a reference to the MIDlet proxy list in a secure way.
     * The calling suite must have the com.sun.midp.ams permission "allowed".
     *
     * Should only be called in the AMS Isolate.
     *
     * @return MIDP MIDlet proxy list reference
     */
    public static MIDletProxyList getMIDletProxyList() {
        return getMIDletProxyList(null);
    }

    /**
     * Get a reference to the MIDlet proxy list in a secure way.
     * The calling suite must have the com.sun.midp.ams permission "allowed".
     * <p>
     * Should only be called in the AMS Isolate.
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * @param token SecurityToken with the AMS permission allowed or
     *              null to use the midletSuite permission
     *
     * @return MIDP MIDlet proxy list reference
     */
    public static MIDletProxyList getMIDletProxyList(SecurityToken token) {
        if (token != null) {
            token.checkIfPermissionAllowed(Permissions.AMS);
        } else {
            AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);
        }

        return midletProxyList;
    }

    /**
     * Returns shutdown status
     *
     * @return true if shutdown is in progress, else false
     */
    static boolean shutdownInProgress() {
        return shutdownFlag;
    }

    /**
     * Package private constructor.
     * Shall be called from MIDletSuiteLoader's main()
     *
     * @param  eventQueue reference to the event queue
     */
    MIDletProxyList(EventQueue eventQueue) {
        displayController = new DisplayController(this);

        /* register event listener for events processed by MIDletProxyList */
        new MIDletControllerEventListener(eventQueue, this);
    }

    /**
     * Enables the display controller to be replaced by an application
     * manager.
     *
     * @param newController new display controller
     */
    public void setDisplayController(DisplayController newController) {
        displayController = newController;
    }

    /**
     * Add a listener for MIDlet proxy list changes.
     *
     * @param listener MIDlet proxy list listener
     */
    public void addListener(MIDletProxyListListener listener) {
        listeners.addElement(listener);
    }

    /**
     * Remove a listener for MIDlet proxy list changes.
     *
     * @param listener MIDlet proxy list listener
     */
    public void removeListener(MIDletProxyListListener listener) {
        listeners.removeElement(listener);
    }

    /**
     * Get an enumeration of MIDlet proxies.
     *
     * @return enumeration of midletProxys
     */
    public Enumeration getMIDlets() {
        Vector v = new Vector();
        synchronized (midletProxies) {
            int size = midletProxies.size();
            for (int i = 0; i < size; i++) {
                v.addElement(midletProxies.elementAt(i));
            }
       }
        return v.elements();
    }

    /**
     * Shutdown the system by asynchronously destroying all MIDlets.
     *
     */
    public void shutdown() {
        synchronized (midletProxies) {
            if (shutdownFlag) {
                return;
            }

            shutdownFlag = true;

            if (midletProxies.size() == 0) {
                /*
                 * We are done if no proxyies are in the list.
                 * Notify any objects waiting for shutdown.
                 */
                midletProxies.notifyAll();
                return;
            }

            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                MIDletProxy current = (MIDletProxy)midletProxies.elementAt(i);

                current.destroyMidlet();
            }
        }
    }

    /** Wait for the system to asynchronously destroy all MIDlets. */
    public void waitForShutdownToComplete() {
        synchronized (midletProxies) {
            // Wait for shutdown to be called.
            while (!shutdownFlag) {
                try {
                    midletProxies.wait();
                } catch (InterruptedException ie) {
                    return;
                }
            }

            // Wait for all MIDlets to be destroyed.
            while (midletProxies.size() > 0) {
                try {
                    midletProxies.wait();
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    /**
     * Find the MIDletProxy that has matching Isolate ID and Display ID.
     *
     * @param isolateId Isolate ID
     * @param displayId Display ID
     *
     * @return a reference to the matching MIDletProxy or null if no match
     */
    private MIDletProxy findMIDletProxy(int isolateId, int displayId) {
        synchronized (midletProxies) {
            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                MIDletProxy current = (MIDletProxy)midletProxies.elementAt(i);

                if (current.getIsolateId() == isolateId &&
		    current.containsDisplay(displayId)) {
                    return current;
                }
            }
        }

        return null;
    }

    /**
     * Find the MIDletProxy that has matching suiteId and classname.
     *
     * @param suiteId the suiteId of the target application
     * @param classname classname of the MIDlet
     *
     * @return a reference to the matching MIDletProxy or null if no match
     */
    public MIDletProxy findMIDletProxy(int suiteId, String classname) {
        synchronized (midletProxies) {
            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                MIDletProxy current = (MIDletProxy)midletProxies.elementAt(i);

                if (current.getSuiteId() == suiteId &&
                        current.getClassName().equals(classname)) {
                    return current;
                }
            }
        }

        return null;
    }

    /**
     * Find the MIDletProxy that has matching external app ID.
     *
     * @param externalAppId ID assigned by the external application manager
     *
     * @return a reference to the matching MIDletProxy or null if no match
     */
    public MIDletProxy findMIDletProxy(int externalAppId) {
        synchronized (midletProxies) {
            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                MIDletProxy current = (MIDletProxy)midletProxies.elementAt(i);

                if (current.getExternalAppId() == externalAppId) {
                    return current;
                }
            }
        }

        return null;
    }

    /**
     * Retireves proxy whose peer application runs in the AMS isolate.
     * The proxy is identified by isolate ID since only one application
     * can run in the AMS solate.
     * @return the proxy or null if one cannot be found
     */
    public MIDletProxy findAmsProxy() {
        if (null == amsProxy) {
            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                MIDletProxy current = (MIDletProxy)midletProxies.elementAt(i);

                if (current.getIsolateId() == MIDletSuiteUtils.getAmsIsolateId()) {
                    amsProxy = current;
                    break;
                }
            }
        }

        return amsProxy;
    }

    /**
     * Process a MIDlet created notification.
     * MIDletControllerEventConsumer I/F method.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     * @param midletIsolateId isolate ID of the sending MIDlet
     * @param midletExternalAppId ID of given by an external application
     *                            manager
     * @param midletDisplayName name to show the user
     */
    public void handleMIDletCreateNotifyEvent(
        int midletSuiteId,
        String midletClassName,
        int midletIsolateId,
        int midletExternalAppId,
        String midletDisplayName) {

        MIDletProxy midletProxy = findMIDletProxy(midletSuiteId,
                                                  midletClassName);
        if (midletProxy != null) {
            /**
             * The isolate this MIDlet was last run in, died because the
             * event proccessing encountered a fatal error, which ends
             * the isolate without notifing the proxy list.
             * So just remove the MIDlet's proxy now.
             * So we can add the new midlet proxy.
             */
            removeMidletProxy(midletProxy);
        }

        /* MIDlet's are constructed in PAUSED state. */
        midletProxy = new MIDletProxy(this,
            midletExternalAppId,
            midletIsolateId,
            midletSuiteId,
            midletClassName,
            midletDisplayName,
            MIDletProxy.MIDLET_PAUSED);

        int suspendResumeState =
                SuspendSystem.getInstance(classSecurityToken).getState();

        if ((suspendResumeState == SuspendSystem.SUSPENDED) ||
                (suspendResumeState == SuspendSystem.SUSPENDING)) {
            MIDletProxyUtils.terminateMIDletIsolate(midletProxy, this);
            midletProxy = null;
            return;
        }

        // Load and cache run-time extended MIDlet attributes
        MIDletProxyUtils.setupExtendedAttributes(midletProxy);

        midletProxies.addElement(midletProxy);

        notifyListenersOfProxyListChange(midletProxy, PROXY_ADDED);

        displayController.midletCreated(midletProxy);
    }

    /**
     * Process a MIDlet active notification
     * MIDletControllerEventConsumer I/F method.
     *
     * TBD: param midletProxy proxy with information about MIDlet
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void handleMIDletActiveNotifyEvent(
        int midletSuiteId,
        String midletClassName) {

        MIDletProxy midletProxy = findMIDletProxy(midletSuiteId,
                                                  midletClassName);
        if (midletProxy == null) {
            /*
             * There is nothing we can do for the other events
             * if a proxy was not found.
             *
             * Sometimes an display can send an event after a
             * MIDlet's destroyApp method is called and the proxy removed
             * from this list. One of the cases is when
             * a thread the MIDlet started and is still running after
             * the destroyApp method has returned, calls display.setCurrent
             * with null while cleaning up.
             */
            return;
        }

        midletProxy.setMidletState(MIDletProxy.MIDLET_ACTIVE);
        notifyListenersOfProxyUpdate(midletProxy,
                                     MIDletProxyListListener.MIDLET_STATE);
        setForegroundMIDlet(displayController.midletActive(midletProxy));
        notifyIfMidletActive();
    }

    /**
     * Process a MIDlet paused notification.
     * MIDletControllerEventConsumer I/F method.
     *
     * TBD: param midletProxy proxy with information about MIDlet
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void handleMIDletPauseNotifyEvent(
        // MIDletProxy midletProxy) {
        int midletSuiteId,
        String midletClassName) {

        MIDletProxy midletProxy = findMIDletProxy(midletSuiteId,
                                                  midletClassName);
        if (midletProxy == null) {
            /*
             * There is nothing we can do for the other events
             * if a proxy was not found. See midletActiveNotification().
             */
            return;
        }

        midletProxy.setMidletState(MIDletProxy.MIDLET_PAUSED);
        notifyListenersOfProxyUpdate(midletProxy,
                                     MIDletProxyListListener.MIDLET_STATE);

        setForegroundMIDlet(displayController.midletPaused(midletProxy));
    }

    /**
     * Process a MIDlet destroyed event.
     * MIDletControllerEventConsumer I/F method.
     *
     * TBD: param midletProxy proxy with information about MIDlet
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void handleMIDletDestroyNotifyEvent(
        // MIDletProxy midletProxy) {
        int midletSuiteId,
        String midletClassName) {

        MIDletProxy midletProxy = findMIDletProxy(midletSuiteId,
                                                  midletClassName);
        if (midletProxy == null) {
            /*
             * There is nothing we can do for the event
             * if a proxy was not found. See midletActiveNotification().
             */
            return;
        }

        midletProxy.destroyedNotification();
        removeMidletProxy(midletProxy);
    }


    /**
     * Processes a MIDLET_RESUME_REQUEST event.
     *
     * MIDletControllerEventConsumer I/F method.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void handleMIDletResumeRequestEvent(int midletSuiteId,
                                               String midletClassName) {
        MIDletProxy midletProxy = findMIDletProxy(midletSuiteId,
                                                  midletClassName);
        if (midletProxy == null) {
            /*
             * There is nothing we can do for the event
             * if a proxy was not found. See midletActiveNotification().
             */
            return;
        }

        // Just grant the request.
        midletProxy.activateMidlet();
    }

    /**
     * Handles notification event of MIDlet resources pause.
     * MIDletControllerEventConsumer I/F method.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     */
    public void handleMIDletRsPauseNotifyEvent(
        int midletSuiteId,
        String midletClassName) {

        MIDletProxy midletProxy = findMIDletProxy(midletSuiteId,
                                                  midletClassName);
        if (midletProxy == null) {
            /*
             * There is nothing we can do for the other events
             * if a proxy was not found.
             */
            return;
        }

        notifyListenersOfProxyUpdate(midletProxy,
                MIDletProxyListListener.RESOURCES_SUSPENDED);
    }

    /**
     * Process a MIDlet destroy request event.
     * MIDletControllerEventConsumer I/F method.
     *
     * TBD: param midletProxy proxy with information about MIDlet
     *
     * @param midletIsolateId isolate ID of the sending Display
     * @param midletDisplayId ID of the sending Display
     */
    public void handleMIDletDestroyRequestEvent(
        // MIDletProxy midletProxy) {
        int midletIsolateId,
        int midletDisplayId) {

        MIDletProxy midletProxy = findMIDletProxy(midletIsolateId,
                                                  midletDisplayId);
        if (midletProxy == null) {
            return;
        }

        midletProxy.destroyMidlet();
    }

    /**
     * Process an ACTIVATE_ALL_EVENT.
     * MIDletControllerEventConsumer I/F method.
     *
     */
    public void handleActivateAllEvent() {
        SuspendSystem.getInstance(classSecurityToken).resume();

        synchronized (midletProxies) {
            MIDletProxy current;

            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                current = (MIDletProxy)midletProxies.elementAt(i);

                current.activateMidlet();
            }
        }
    }

    /**
     * Process a PAUSE_ALL_EVENT.
     * MIDletControllerEventConsumer I/F method.
     */
    public void handlePauseAllEvent() {
        synchronized (midletProxies) {
            MIDletProxy current;

            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                current = (MIDletProxy)midletProxies.elementAt(i);
                if (!current.wasNotActive) {
                    SuspendSystem.getInstance(classSecurityToken).
                            addSuspendDependency(current);
                    current.pauseMidlet();
                } else {
                    MIDletProxyUtils.terminateMIDletIsolate(current, this);
                }
            }
        }

        SuspendSystem.getInstance(classSecurityToken).suspend();

    }

    /**
     * Finalizes PAUSE_ALL_EVENT processing after timeout for
     * pausing MIDlets expires.
     */
    public void terminatePauseAll() {
        synchronized (midletProxies) {
            MIDletProxy current;

            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                current = (MIDletProxy)midletProxies.elementAt(i);

                current.terminateNotPausedMidlet();
            }

        }
    }

    /**
     * Process a SHUTDOWN_ALL_EVENT.
     * MIDletControllerEventConsumer I/F method.
     *
     * It simply calls "shutdown()". In future it shall be merged with
     * "shutdown()" and substitute it.
     */
    public void handleDestroyAllEvent() {
        shutdown();
    }

    /**
     * Processes FATAL_ERROR_NOTIFICATION.
     *
     * MIDletControllerEventConsumer I/F method.
     *
     * @param midletIsolateId isolate ID of the sending isolate
     * @param midletDisplayId ID of the sending Display
     */
    public void handleFatalErrorNotifyEvent(
        int midletIsolateId,
        int midletDisplayId) {

        removeIsolateProxies(midletIsolateId);
        AmsUtil.terminateIsolate(midletIsolateId);
    }

    /**
     * Notifies the device if an active MIDlet appeared in the system.
     * The notification is produced only once when the system runs
     * out of the state with all the MIDlets being either paused 
     * or destroyed.
     */
    private void notifyIfMidletActive() {
        MIDletProxy midletProxy;

        synchronized (midletProxies) {
            if (allPaused) {
                for (int i = midletProxies.size() - 1; i >= 0; i--) {
                    midletProxy = (MIDletProxy)midletProxies.elementAt(i);
                    if (midletProxy.getMidletState() ==
                            MIDletProxy.MIDLET_ACTIVE) {
                        allPaused = false;
                        notifyResumeAll0();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Notifies the device if all MIDlets considered to be paused, that is
     * at least one MIDlet is paused and others are either paused or
     * destroyed. The notification is produced only once when the system
     * runs to that state.
     */
    private void notifyIfAllPaused() {
        boolean  allMidletsPaused = false;
        int midletState;

        synchronized (midletProxies) {
            if (!allPaused) {
                for (int i = midletProxies.size() - 1; i >= 0; i--) {
                    midletState = ((MIDletProxy) midletProxies.elementAt(i)).
                            getMidletState();

                    if (MIDletProxy.MIDLET_PAUSED == midletState) {
                        allMidletsPaused = true;
                    } else if (MIDletProxy.MIDLET_DESTROYED != midletState) {
                        allMidletsPaused = false;
                        break;
                    }
                }

                if (allMidletsPaused) {
                    allPaused = true;
                    notifySuspendAll0();
                }
            }
        }
    }

    /**
     * Process a Display created notification.
     * MIDletControllerEventConsumer I/F method.
     *
     * @param midletIsolateId isolate ID of the sending Display
     * @param midletDisplayId ID of the sending Display
     * @param midletClassName Class name of the MIDlet that owns the display
     */
    public void handleDisplayCreateNotifyEvent(
        int midletIsolateId,
        int midletDisplayId,
        String midletClassName) {

        MIDletProxy midletProxy = null;

        synchronized (midletProxies) {
            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                MIDletProxy current = (MIDletProxy)midletProxies.elementAt(i);
                if (current.getIsolateId() == midletIsolateId &&
                        current.getClassName().equals(midletClassName)) {
                    midletProxy = current;
                    break;
                }
            }
        }

        if (midletProxy == null) {
            /**
             * The isolate is create a display without a MIDlet to display
             * a fatal error loading the suite in SVM mode.
             * So just do nothing, a preempt event will follow.
             */
            return;
        }

        /* Just set the display ID of the proxy. */
        midletProxy.setDisplayId(midletDisplayId);
    }

    /**
     * Process a foreground request event.
     * MIDletControllerEventConsumer I/F method.
     *
     * TBD: param midletProxy proxy with information about MIDlet
     *
     * @param midletIsolateId isolate ID of the sending Display
     * @param midletDisplayId ID of the sending Display
     * @param isAlert true if the current displayable is an Alert
     */
    public void handleDisplayForegroundRequestEvent(
        // MIDletProxy midletProxy) {
        int midletIsolateId,
        int midletDisplayId,
        boolean isAlert) {

        MIDletProxy midletProxy = findMIDletProxy(midletIsolateId,
                                                  midletDisplayId);
        if (midletProxy == null) {
            /*
             * There is nothing we can do for the event
             * if a proxy was not found. See midletActiveNotification().
             */
            return;
        }

        if (midletProxy == foregroundMidlet) {
            // force alert waiting to false, since it can't be waiting
            midletProxy.setWantsForeground(true, false);
        } else {
            midletProxy.setWantsForeground(true, isAlert);
            setForegroundMIDlet(
                displayController.foregroundRequest(midletProxy));
        }

        /**
         * The internal calls to  notifyListenersOfProxyChange() within
         * setForegroundMIDlet() should not override the behaviour of
         * listener which is an IndicatorManager in this case. So,
         * notifyListenersOfProxyChange() should be called after
         * setForegroundMIDlet().
         */
        notifyListenersOfProxyUpdate(midletProxy,
                                     MIDletProxyListListener.WANTS_FOREGROUND);
    }

    /**
     * Process a background request event.
     * MIDletControllerEventConsumer I/F method.
     *
     * TBD: param midletProxy proxy with information about MIDlet
     *
     * @param midletIsolateId isolate ID of the sending Display
     * @param midletDisplayId ID of the sending Display
     */
    public void handleDisplayBackgroundRequestEvent(
        // MIDletProxy midletProxy) {
        int midletIsolateId,
        int midletDisplayId) {

        MIDletProxy midletProxy = findMIDletProxy(midletIsolateId,
                                                  midletDisplayId);
        if (midletProxy == null) {
            /*
             * There is nothing we can do for the event
             * if a proxy was not found.
             *
             * Sometimes an display can send an event before
             * MIDlet. One of the cases is when
             * a MIDlet calls Display.setCurrent with a new displayable in
             * its constructor which happens before the MIDlet created
             * event.
             *
             * Sometimes an display can send an event after a
             * MIDlet's destroyApp method is called and the proxy removed
             * from this list. One of the cases is when
             * a thread the MIDlet started and is still running after
             * the destroyApp method has returned, calls display.setCurrent
             * with null while cleaning up.
             */
            return;
        }

        midletProxy.setWantsForeground(false, false);
        setForegroundMIDlet(displayController.backgroundRequest(midletProxy));
        notifyListenersOfProxyUpdate(midletProxy,
                                     MIDletProxyListListener.WANTS_FOREGROUND);
    }

    /**
     * Process a "display preempt start" event.
     * <p>
     * Set the foreground to a given display if a certain display
     * has the foreground. Used to start preempting.
     *
     * MIDletControllerEventConsumer I/F method.
     *
     * @param midletIsolateId isolate ID of the sending Display
     * @param midletDisplayId ID of the sending Display
     */
    public void handleDisplayPreemptStartEvent(
        int midletIsolateId,
        int midletDisplayId) {

        MIDletProxy preempting = new MIDletProxy(this, 0,
            midletIsolateId, MIDletSuite.UNUSED_SUITE_ID,
                null, null, MIDletProxy.MIDLET_ACTIVE);
        preempting.setDisplayId(midletDisplayId);

        MIDletProxy nextForeground =
            displayController.startPreempting(preempting);

        if (nextForeground != null) {
            setForegroundMIDlet(nextForeground);
        }
    }

    /**
     * Process a "display preempt stop" event.
     * <p>
     * Set the foreground to a given display if a certain display
     * has the foreground. Used to end preempting.
     *
     * MIDletControllerEventConsumer I/F method.
     *
     * @param midletIsolateId isolate ID of the sending Display
     * @param midletDisplayId ID of the sending Display
     */
    public void handleDisplayPreemptStopEvent(
        int midletIsolateId,
        int midletDisplayId) {

        MIDletProxy nextForeground =
            displayController.endPreempting(
                midletIsolateId,
                midletDisplayId);

        setForegroundMIDlet(nextForeground);
    }

    /**
     * Process a select foreground event by putting the foreground selector
     * MIDlet in the foreground.
     *
     * MIDletControllerEventConsumer I/F method.
     *
     */
    public void handleMIDletForegroundSelectEvent(int onlyFromLaunched) {
        MIDletProxy nextForeground =
            displayController.selectForeground(onlyFromLaunched == 1);

        if (nextForeground == foregroundMidlet) {
            return;
        }

        setForegroundMIDlet(nextForeground);
    }

    /**
     * Process an event to transition the foreground from a current display
     * to a target MIDlet by ID and classname. If the source display
     * does not currently own the foreground the request is ignored.
     * If the target MIDlet is found in the active list then it it set
     * as the foreground. If not found, then it should be added as
     * the next display to get the foreground (when it asks).
     *
     * MIDletControllerEventConsumer I/F method.
     *
     * @param originMIDletSuiteId ID of MIDlet from which
     *        to take forefround ownership away,
     * @param originMIDletClassName Name of MIDlet from which
     *        to take forefround ownership away
     * @param targetMIDletSuiteId ID of MIDlet
     *        to give forefround ownership to,
     * @param targetMIDletClassName Name of MIDlet
     *        to give forefround ownership to
     */
    public void handleMIDletForegroundTransferEvent(
        int originMIDletSuiteId,
        String originMIDletClassName,
        int targetMIDletSuiteId,
        String targetMIDletClassName) {

        MIDletProxy origin = findMIDletProxy(originMIDletSuiteId,
                                             originMIDletClassName);
        if (origin == null) {
            return;
        }
        // See if the foreground can be handed to the target MIDlet
        MIDletProxy target = findMIDletProxy(targetMIDletSuiteId,
                                             targetMIDletClassName);
        if (target != null) {
            target.setWantsForeground(true, false);
            // Let the DisplayController make the UE policy choice
            setForegroundMIDlet(displayController.transferRequest(origin,
                                                                  target));

            /**
            * The internal calls to  notifyListenersOfProxyChange() within
            * setForegroundMIDlet() should not override the behaviour of a
            * listener which is an IndicatorManager in this case. So,
            * notifyListenersOfProxyChange() should be called after
            * setForegroundMIDlet().
            */
            notifyListenersOfProxyUpdate(target,
                MIDletProxyListListener.WANTS_FOREGROUND);
        }
    }

    /**
     * Processes SET_FOREGROUND_BY_NAME_REQUEST event.
     * <p>
     * Set specified MIDlet to foreground.
     *
     * @param suiteId MIDlet's suite ID
     * @param className MIDlet's class name
     */
    public void handleSetForegroundByNameRequestEvent(
        int suiteId,
        String className) {

        MIDletProxy midletProxy = findMIDletProxy(suiteId, className);
        if (midletProxy != null) {
            setForegroundMIDlet(midletProxy);
        }
    }

    /**
     * Process a MIDlet start error event.
     * Notify from last to first added to allow the listener to
     * remove itself without causing a missed notification.
     *
     * MIDletControllerEventConsumer I/F method.
     *
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     * @param midletExternalAppId ID of given by an external application
     *                            manager
     * @param errorCode start error code
     * @param errorDetails start error details
     */
    public void handleMIDletStartErrorEvent(
        int midletSuiteId,
        String midletClassName,
        int midletExternalAppId,
        int errorCode,
        String errorDetails) {

        for (int i = listeners.size() - 1; i >= 0; i--) {
            MIDletProxyListListener listener =
                (MIDletProxyListListener)listeners.elementAt(i);

            listener.midletStartError(midletExternalAppId, midletSuiteId,
                                      midletClassName, errorCode, errorDetails);
        }
    }

    /**
     * Sets the foreground MIDlet. If the given midletProxy is paused,
     * then it will be activated before given the foreground.
     * <p>
     * The follow steps are performed when changed:<p>
     * 1. Send an event to notify the old foreground Display it has lost the
     *    foreground<p>
     * 2. Change the foreground field in this object and in the native
     *    code. <p>
     * 3. Send an event to notify the new foreground Display is has gained the
     *    foreground<p>
     *
     * @param newForeground Proxy of the MIDlet to be put in the foreground
     */
    public void setForegroundMIDlet(MIDletProxy newForeground) {
        if (newForeground != null &&
            (newForeground.getMidletState() == MIDletProxy.MIDLET_DESTROYED ||
            newForeground == foregroundMidlet ||
            newForeground.noDisplay())) {
            return;
        }

        /*
         * If the value is "yes" when the MIDlet is launched, it must be
         * launched directly to the background.
         *
         * This attribute inherits all the features and requirements from
         * "MIDlet-Background-Pause:no" even the attribute is not present.
         *
         * A MIDlet that was launched per this attribute must be able to bring
         * itself to the foreground by calling MIDlet.resumeRequest. An user is
         * not able to put the MIDlet to the foreground.
         */
        if (Constants.EXTENDED_MIDLET_ATTRIBUTES_ENABLED) {
            if (newForeground != null && newForeground.wasNotActive) {
                if (newForeground.getExtendedAttribute(
                        MIDletProxy.MIDLET_LAUNCH_BG)) {
                    return;
                }
            }
        }

        if (foregroundMidlet != null &&
            (foregroundMidlet.getMidletState() !=
            MIDletProxy.MIDLET_DESTROYED)) {
            /*
             * Background MIDlet will run with a lower priority
             */
            MIDletProxyUtils.minPriority(foregroundMidlet);
            foregroundMidlet.notifyMIDletHasForeground(false);

            /*
             * If extended MIDlet attributes support enabled and
             * MIDlet-Background-Pause attribute is set to true when pause the
             * MIDlet right after it's put it in the background.
             */
            if (Constants.EXTENDED_MIDLET_ATTRIBUTES_ENABLED) {
                if (foregroundMidlet.getExtendedAttribute(
                        MIDletProxy.MIDLET_BACKGROUND_PAUSE)) {
                    foregroundMidlet.pauseMidlet();
                }
            }
        }

        MIDletProxy oldForeground = foregroundMidlet;
        foregroundMidlet =
            displayController.foregroundMidletChanging(newForeground);

        /*
         * When there are no more midletProxys the foreground midletProxy
         * will be null again.
         */

        if (foregroundMidlet != null) {
	    int[] displayIds =  foregroundMidlet.getDisplayIds();
	    int isolateId = foregroundMidlet.getIsolateId();
	    resetForegroundInNativeState();
	    for (int i = 0; i < displayIds.length; i++) {
		setForegroundInNativeState(isolateId, displayIds[i]);
	    }
            // This call with a true parameter will set the alertWaiting field.
            foregroundMidlet.notifyMIDletHasForeground(true);

           /*
            * Foreground MIDlet will run with a normal priority
            */
            MIDletProxyUtils.normalPriority(foregroundMidlet);

            /*
             * IMPL_NOTE: there are no listeners for the certain
             * MIDletProxyListListener.ALERT_WAITING reason in MIDP.
             * To check whether call to notifyListenersOfProxyUpdate may be safely
             * removed here.
             */
            notifyListenersOfProxyUpdate(foregroundMidlet,
                MIDletProxyListListener.ALERT_WAITING);

            /*
             * If extended MIDlet attributes support enabled and
             * MIDlet-Background-Pause attribute is set to true when activate
             * the MIDlet back after it's returned back to the foreground.
             */

            /* IMPL_NOTE: in current implementation, MIDlet is always activated
             * when it's put to the foreground regardless it was paused by user
             * or due to the MIDlet-Pause-Backround attribute.
             */
/*
            if (Constants.EXTENDED_MIDLET_ATTRIBUTES_ENABLED) {
                if (foregroundMidlet.getExtendedAttribute(
                        MIDletProxy.MIDLET_BACKGROUND_PAUSE)) {
                    foregroundMidlet.activateMidlet();
                }
            }
*/
        } else {
            setForegroundInNativeState(MIDletSuiteUtils.getAmsIsolateId(),
                                       -1);
        }

        /*
         * Notify display controller that foreground change has completed
         */
        displayController.foregroundMidletChanged(oldForeground, 
                newForeground);
        
    }

    /**
     * Get the foreground MIDlet.
     *
     * @return proxy to the MIDlet that is in the foreground
     */
    public MIDletProxy getForegroundMIDlet() {
        return foregroundMidlet;
    }

    /**
     * Return true if home indicator needs to be turned on
     *
     * @return true if any MIDlet has set an Alert as the current displayable
     *   while in the background, otherwise false
     *
     */
    public boolean isAlertWaitingInBackground() {
        synchronized (midletProxies) {
            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                MIDletProxy current = (MIDletProxy)midletProxies.elementAt(i);

                if (current.isAlertWaiting()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check to see if the MIDlet is already started.
     *
     * @param id Suite ID of the MIDlet
     * @param className Class name of the MIDlet
     *
     * @return true if the MIDlet has been started
     */
    public boolean isMidletInList(int id, String className) {
        synchronized (midletProxies) {
            for (int i = midletProxies.size() - 1; i >= 0; i--) {
                MIDletProxy current = (MIDletProxy)midletProxies.elementAt(i);

                if (current.getSuiteId() == id &&
                    current.getClassName().equals(className)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Notify the listeners of change in the size of the proxy list.
     *
     * Notify from last to first added to allow the listener to
     * remove itself without causing a missed notification.
     *
     * @param midletProxy midletProxy that was added or removed in the list
     * @param notificationType type of change, added or removed
     */
    void notifyListenersOfProxyListChange(MIDletProxy midletProxy,
                                          int notificationType) {

        for (int i = listeners.size() - 1; i >= 0; i--) {
            MIDletProxyListListener listener =
                (MIDletProxyListListener)listeners.elementAt(i);

            switch (notificationType) {
            case PROXY_ADDED:
                listener.midletAdded(midletProxy);
                break;

            case PROXY_REMOVED:
                listener.midletRemoved(midletProxy);
                break;
            }
        }
    }

    /**
     * Notify the listeners of the midletProxy list that a proxy
     * has been updated.
     *
     * Notify from last to first added to allow the listener to
     * remove itself without causing a missed notification.
     *
     * @param midletProxy midletProxy that changed in the list
     * @param reason reason for the change
     */
    void notifyListenersOfProxyUpdate(MIDletProxy midletProxy,
                                      int reason) {

        for (int i = listeners.size() - 1; i >= 0; i--) {
            MIDletProxyListListener listener =
                (MIDletProxyListListener)listeners.elementAt(i);

            listener.midletUpdated(midletProxy, reason);
        }
    }

    /**
     * Removes a MidletProxy from the MidletProxyList
     *
     * @param midletProxy the MIDletProxy to be removed.
     */
    void removeMidletProxy(MIDletProxy midletProxy) {
        MIDletProxy preempting;

        /*
         * Remove the proxy before notifying the display controller,
         * so that new foreground search will not find MIDlet, but
         * don't notify the listener until after new foreground, since
         * the old foreground will generate a update notification.
         */
        midletProxies.removeElement(midletProxy);

        preempting = midletProxy.getPreemptingDisplay();
        if (preempting != null) {
            setForegroundMIDlet(displayController.midletDestroyed(preempting));
        } else {
            setForegroundMIDlet(
                displayController.midletDestroyed(midletProxy));
        }

        notifyListenersOfProxyListChange(midletProxy, PROXY_REMOVED);

        // An object may be waiting for the shutdown to complete
        if (shutdownFlag) {
            synchronized (midletProxies) {
                if (midletProxies.size() == 0) {
                    midletProxies.notifyAll();
                }
            }
        }
    }

    /**
     * Removes the MIDletproxies that belong to the isolate id
     *
     * @param id IsolateId
     */
    void removeIsolateProxies(int id) {
        // We are removing multiple items from the list we
        // have to remove from the enumeration.
        Enumeration e = getMIDlets();
        while (e.hasMoreElements()) {
            MIDletProxy mp = (MIDletProxy)e.nextElement();
            if (mp.getIsolateId() == id) {
                removeMidletProxy(mp);
            }
        }
    }

    /**
     * Called to notify that java side of MIDP system has been suspended.
     */
    public void midpSuspended() {
        notifyIfAllPaused();
    }

    /**
     * Called to notify that java side of MIDP system has been resumed.
     */
    public void midpResumed() {}

    /**
     * Set foreground midletProxy in the native midletProxy list state.
     *
     * @param isolateId Isolate ID
     * @param displayId Display ID
     */
    private native void setForegroundInNativeState(int isolateId,
                                                   int displayId);
    /**
     * Reset foreground midletProxy in the native midletProxy list state.
     *
     */
    private native void resetForegroundInNativeState();

    /**
     * Notify native code that all MIDlets have been paused.
     */
    private native void notifySuspendAll0();

    /**
     * Notify native code that an active MIDlet appeared in the system.
     */
    private native void notifyResumeAll0();
}
