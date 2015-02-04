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

package com.sun.midp.midlet;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.j2me.security.AccessController;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * The MIDletStateHandler starts and controls MIDlets through the lifecycle
 * states.
 * MIDlets are created using its no-arg Constructor. Once created
 * a MIDlet is sequenced through the <code>ACTIVE</code>,
 * <code>PAUSED</code>, and <code>DESTROYED</code> states.
 * <p>
 * The MIDletStateHandler is a singleton for the suite being run and
 * is retrieved with getMIDletStateHandler(). This allow the
 * MIDletStateHandler to be the anchor of trust internally for the MIDP API,
 * restricted methods can obtain the MIDletStateHandler for a MIDlet suite
 * inorder to check the properties and actions of a suite.
 * Because of this, there MUST only be one a MIDlet suite per
 * MIDletStateHandler. In addition a method can assume that the application
 * manager is the caller if there is no suite started.</p>
 * <p>
 * The MIDlet methods are protected in the javax.microedition.midlet package
 * so the MIDletStateHandler can not call them directly.  The MIDletState
 * object and
 * MIDletTunnel subclass class allow the MIDletStateHandler to hold the state
 * of the
 * MIDlet and to invoke methods on it.  The MIDletState instance is created
 * by the MIDlet when it is constructed.
 * <p>
 * This implementation of the MIDletStateHandler introduces
 * extra internal MIDlet states to allow processing of MIDlet
 * requested state changes in the control thread and serialized with
 * other state changes.  The additional states are:
 * <UL>
 * <LI> <code>ACTIVE_PENDING</code> - The MIDlet is still PAUSED but
 * will be <code>ACTIVE</code> after startApp is called when the state is
 * next processed.
 * <LI> <code>PAUSE_PENDING</code> - The MIDlet is still ACTIVE but
 * will be <code>PAUSED</code> after pauseApp is called when the state is
 * next processed.
 * <LI> <code>DESTROY_PENDING</code> - Indicates that the MIDlet needs
 * to be <code>DESTROYED</code>. The MIDlet's destroyApp has not yet been
 * called.
 * </UL>
 * The MIDletStateHandler loops, looking for MIDlets that require state changes
 * and making the requested changes including calling methods in
 * the MIDlet to make the change.
 * <p>
 * When a MIDlet's state is changed to <code>ACTIVE</code>,
 * <code>PAUSED</code>, or <code>DESTROYED</code> the MIDlet state listener
 * is notified of the change, which in turn sends the notification onto
 * the central AMS.
 *
 * @see MIDlet
 * @see MIDletPeer
 * @see MIDletLoader
 * @see MIDletStateHandler
 */

public class MIDletStateHandler {
    /** the current MIDlet suite. */
    private MIDletSuite midletSuite;
    /** loads the MIDlets from a suite's JAR in a VM specific way. */
    private MIDletLoader midletLoader;
    /** array of MIDlets. */
    private MIDletPeer[] midlets;
    /** current number of MIDlets [0..n-1]. */
    private int nmidlets;
    /** next index to be scanned by selectByPriority. */
    private int scanIndex;

    /** The event handler of all MIDlets in an Isolate. */
    private static MIDletStateHandler stateHandler;
    /** The listener for the state of all MIDlets in an Isolate. */
    private static MIDletStateListener listener;

    /** Serializes the creation of MIDlets. */
    private static Object createMIDletLock = new Object();

    /** New MIDlet peer waiting for the next MIDlet created to claim it. */
    private static MIDletPeer newMidletPeer;

    /** MIDlet peer for MIDlet being constructed but not registered yet. */
    private MIDletPeer underConstructionPeer;

    /**
     * Construct a new MIDletStateHandler object.
     */
    private MIDletStateHandler() {
        nmidlets = 0;

        // start with 5 empty slots, we will add more if needed
        midlets = new MIDletPeer[5];
    }

    /**
     * Gets the MIDletStateHandler that manages the lifecycle states of
     * MIDlets running in an Isolate.
     * <p>
     * If the instance of the MIDletStateHandler has already been created
     * it is returned.  If not it is created.
     * The instance becomes the MIDletStateHandler for this suite.
     * <p>
     * The fact that there is one handler per Isolate
     * is a security feature. Also a security feature, is that
     * getMIDletStateHandler is
     * static, so API can find out what suite is calling, if in the future
     * multiple suites can be run in the same VM, the MIDlet state handler
     * for each suite
     * should be loaded in a different classloader or have some other way
     * having multiple instances of static class data.
     *
     * @return the MIDlet state handler for this Isolate
     */
    public static synchronized MIDletStateHandler getMidletStateHandler() {
        /*
         * If the midlet state handler has not been created, create one now.
         */
        if (stateHandler == null) {
            /* This is the default scheduler class */
            stateHandler = new MIDletStateHandler();
        }

        return stateHandler;
    }

    /**
     * Initializes MIDlet State Handler.
     *
     * @param token security token for initilaization
     * @param theMIDletStateListener processes MIDlet states in a
     *                               VM specific way
     * @param theMidletLoader loads a MIDlet in a VM specific way
     * @param thePlatformRequestHandler the platform request handler
     */
    public void initMIDletStateHandler(
        SecurityToken token,
        MIDletStateListener theMIDletStateListener,
        MIDletLoader theMidletLoader,
        PlatformRequest thePlatformRequestHandler) {
        listener = theMIDletStateListener;
        midletLoader = theMidletLoader;

        MIDletPeer.initClass(this, listener, thePlatformRequestHandler);
    }

    /**
     * Starts a MIDlet from outside of the package.
     *
     * @param classname name of MIDlet class
     * @param displayName name to show the user
     *
     * @exception SecurityException if the suite does not have the
     *   AMS permission.
     * @exception ClassNotFoundException is thrown, if the MIDlet main class is
     * not found
     * @exception InstantiationException is thrown, if the MIDlet can not be
     * created
     * @exception IllegalAccessException is thrown, if the MIDlet is not
     * permitted to perform a specific operation
     */
    public void startMIDlet(String classname, String displayName) throws
           ClassNotFoundException, InstantiationException,
           IllegalAccessException {
        startMIDlet(0, classname, displayName);
    }

    /**
     * Starts a MIDlet from outside of the package.
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * @param externalAppId ID of given by an external application manager
     * @param classname name of MIDlet class
     * @param displayName name to show the user
     *
     * @exception SecurityException if the suite does not have the
     *   AMS permission.
     * @exception ClassNotFoundException is thrown, if the MIDlet main class is
     * not found
     * @exception InstantiationException is thrown, if the MIDlet can not be
     * created
     * @exception IllegalAccessException is thrown, if the MIDlet is not
     * permitted to perform a specific operation
     */
    public void startMIDlet(int externalAppId, String classname,
                            String displayName) throws
           ClassNotFoundException, InstantiationException,
           IllegalAccessException {
        createAndRegisterMIDlet(externalAppId, classname);
    }

    /**
     * Starts a MIDlet from outside of the package.
     *
     * @param token security token of the caller
     * @param classname name of MIDlet class
     * @param displayName name to show the user
     *
     * @exception SecurityException if the caller does not have the
     *   AMS permission.
     * @exception ClassNotFoundException is thrown, if the MIDlet main class is
     * not found
     * @exception InstantiationException is thrown, if the MIDlet can not be
     * created
     * @exception IllegalAccessException is thrown, if the MIDlet is not
     * permitted to perform a specific operation
     */
    public void startMIDlet(SecurityToken token, String classname,
                            String displayName) throws
           ClassNotFoundException, InstantiationException,
           IllegalAccessException {
        startMIDlet(token, 0, classname, displayName);
    }

    /**
     * Starts a MIDlet from outside of the package.
     *
     * @param token security token of the caller
     * @param externalAppId ID of given by an external application manager
     * @param classname name of MIDlet class
     * @param displayName name to show the user
     *
     * @exception SecurityException if the caller does not have the
     *   AMS permission.
     * @exception ClassNotFoundException is thrown, if the MIDlet main class is
     * not found
     * @exception InstantiationException is thrown, if the MIDlet can not be
     * created
     * @exception IllegalAccessException is thrown, if the MIDlet is not
     * permitted to perform a specific operation
     */
    public void startMIDlet(SecurityToken token, int externalAppId,
                            String classname, String displayName) throws
           ClassNotFoundException, InstantiationException,
           IllegalAccessException {
        createAndRegisterMIDlet(externalAppId, classname);
    }

    /**
     * Gets the class name first midlet in the list of running MIDlets.
     *
     * @return the classname or null if no midlet are running
     */
    public String getFirstRunningMidlet() {
        synchronized (this) {
            if (nmidlets <= 0) {
                return null;
            }

            return midlets[0].midlet.getClass().getName();
        }
    }

    /**
     * Registers a MIDlet being constructed.
     *
     * @param midlet to be registered with this state handler
     */
    private void register(MIDlet midlet) {
        synchronized (this) {
            MIDletPeer state = MIDletPeer.getMIDletPeer(midlet);

            /*
             * If a MIDlet of the same class is already running
             * Make the existing MIDlet current so that startSuite()
             * will run it
             */
            int i = findMIDletByClass(state);
            if (i >= 0) {
                state.setState(MIDletPeer.DESTROY_PENDING);
                // Fall into adding it to the list so destroyApp
                // can be called at a reasonable time.
            }

            // Grow the list if necessary
            if (nmidlets >= midlets.length) {
                MIDletPeer[] n = new MIDletPeer[nmidlets+5];
                System.arraycopy(midlets, 0, n, 0, nmidlets);
                midlets = n;
            }

            // Add it to the end of the list
            midlets[nmidlets++] = state;

            // MIDlet peer is registered now
            underConstructionPeer = null;

            this.notify();
        }
    }

    /**
     * Creates and register MIDlet with VM notification
     * of the MIDlet's startup phase.
     *
     * @param externalAppId ID of given by an external application manager
     * @param classname name of MIDlet class
     *
     * @exception ClassNotFoundException if the MIDlet class is
     * not found
     * @exception InstantiationException if the MIDlet cannot be
     * created
     * @exception IllegalAccessException if the MIDlet is not
     * permitted to perform a specific operation
     */
    private void createAndRegisterMIDlet(int externalAppId, String classname)
           throws ClassNotFoundException, InstantiationException,
           IllegalAccessException {

        listener.midletPreStart(getMIDletSuite(), classname);
        register(createMIDlet(externalAppId, classname));
    }

    /**
     * Provides a object with a mechanism to retrieve
     * <code>MIDletSuite</code> being run.
     *
     * @return MIDletSuite being run
     */
    public MIDletSuite getMIDletSuite() {
        return midletSuite;
    }

    /**
     * Runs MIDlets until there are none.
     * Handle any pending state transitions of any MIDlet.
     * If there are none, wait for transitions.
     *
     * @param exceptionHandler the handler for midlet execution exceptions.
     * @param aMidletSuite the current midlet suite
     * @param externalAppId ID of given by an external application manager
     * @param classname name of MIDlet class
     *
     * @exception ClassNotFoundException is thrown, if the MIDlet main class is
     * not found
     * @exception InstantiationException is thrown, if the MIDlet can not be
     * created
     * @exception IllegalAccessException is thrown, if the MIDlet is not
     * permitted to perform a specific operation
     */
    public void startSuite(MIDletSuiteExceptionListener exceptionHandler,
           MIDletSuite aMidletSuite, int externalAppId, String classname)
           throws ClassNotFoundException, InstantiationException,
           IllegalAccessException {

        if (midletSuite != null) {
            throw new RuntimeException(
                 "There is already a MIDlet Suite running.");

        }

        midletSuite = aMidletSuite;
        createAndRegisterMIDlet(externalAppId, classname);

        /*
         * Until there are no MIDlets
         * Scan all the MIDlets looking for state changes.
         */
        while (nmidlets > 0) {
            try {
                MIDletPeer curr;
                int state;

                /*
                 * A MIDlet can change the MIDlet concurrently.
                 * the MIDlet state handler this is used to
                 * synchronize these changes. However any calls to outside of
                 * this package should NOT be done holding
                 * "this".
                 * For this reason there are 2 phases each with a switch
                 * statement to process a state.
                 *
                 * The state is obtained and changed before the work is
                 * done so that when "this" is released to
                 * perform external calls for that state, any state change done
                 * by the MIDlet concurrently are not lost.
                 */


                synchronized (this) {
                    /*
                     * Find the highest priority state of any MIDlet and
                     * process, but do not hold the lock while processing
                     * to avoid deadlocks with LCDUI and event handling.
                     * Perform state changes with a lock so
                     * no state changes are lost.
                     */
                    curr = selectByPriority();
                    state = curr.getState();

                    switch (state) {
                    case MIDletPeer.ACTIVE:
                        // fall through
                    case MIDletPeer.PAUSED:
                        // Wait for some change in the state of a MIDlet
                        // that needs attention
                        try {
                            this.wait();
                        } catch (InterruptedException e) {

                            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                                Logging.report(Logging.WARNING,
                                               LogChannels.LC_AMS,
                                               "InterruptedException " +
                                               "during mutex wait");
                            }
                        }

                        continue;

                    case MIDletPeer.ACTIVE_PENDING:
                        // Start the MIDlet
                        curr.setStateWithoutNotify(MIDletPeer.ACTIVE);
                        break;

                    case MIDletPeer.PAUSE_PENDING:
                        // The system wants the MIDlet paused
                        curr.setStateWithoutNotify(MIDletPeer.PAUSED);
                        break;

                    case MIDletPeer.DESTROY_PENDING:
                        curr.setStateWithoutNotify(MIDletPeer.DESTROYED);
                        break;

                    case MIDletPeer.DESTROYED:
                        unregister(curr);
                        break;

                    default:
                        throw new Error("Illegal MIDletPeer state " +
                                        curr.getState());
                    }
                }

                /** perform work that may block outside of "this" */
                switch (state) {
                case MIDletPeer.ACTIVE_PENDING:
                    try {
                        listener.preActivated(getMIDletSuite(),
                            curr.getMIDlet().getClass().getName());
                       
                        curr.startApp();
                    } catch (Throwable ex) {
                        if (Logging.TRACE_ENABLED) {
                            Logging.trace(ex, "startApp threw an Exception");
                        }
                        curr.setState(MIDletPeer.DESTROY_PENDING);
                        exceptionHandler.handleException(ex);
                        break;
                    }

                    /*
                     * The actual state of the MIDlet is already active.
                     * But any notifications done after startApp call.
                     */
                    listener.midletActivated(getMIDletSuite(),
                        curr.getMIDlet());
                    break;

                case MIDletPeer.PAUSE_PENDING:
                    try {
                        curr.pauseApp();
                    } catch (Throwable ex) {
                        if (Logging.TRACE_ENABLED) {
                            Logging.trace(ex, "pauseApp threw an Exception");
                        }

                        curr.setState(MIDletPeer.DESTROY_PENDING);
                        exceptionHandler.handleException(ex);
                        break;
                    }

                    /*
                     * The actual state of the MIDlet is already paused.
                     * But any notifications done after pauseApp() call.
                     */
                    listener.midletPaused(getMIDletSuite(),
                        curr.getMIDlet().getClass().getName());

                    break;

                case MIDletPeer.DESTROY_PENDING:
                    // If the MIDlet is in the DESTROY_PENDING state
                    // call its destroyApp method to clean it up.
                    try {
                        // Tell the MIDlet to cleanup.
                        curr.destroyApp(true);
                    } catch (MIDletStateChangeException ex) {
                        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                            Logging.report(Logging.WARNING,
                                           LogChannels.LC_AMS,
                                           "destroyApp  threw a " +
                                           "MIDletStateChangeException");
                        }
                        exceptionHandler.handleException(ex);
                    } catch (Throwable ex) {
                        if (Logging.TRACE_ENABLED) {
                            Logging.trace(ex, "destroyApp threw an Exception");
                        }
                        exceptionHandler.handleException(ex);
                    }
                    break;

                case MIDletPeer.DESTROYED:
                    listener.midletDestroyed(getMIDletSuite(),
                        curr.getMIDlet().getClass().getName(),
                        curr.getMIDlet());
                    break;
                }
            } catch (Throwable ex) {
                if (Logging.TRACE_ENABLED) {
                    Logging.trace(ex, "Exception in startSuite");
                }
                exceptionHandler.handleException(ex);
            }
        }
    }

    /**
     * Destroys all running MIDlets in this suite only. This method is only
     * used by the push registry in single VM mode.
     */
    public void destroySuite() {
        synchronized (this) {
            for (int i = 0; i < nmidlets; i++) {
                if (midlets[i].getState() != MIDletPeer.DESTROYED) {
                    midlets[i].
                        setStateWithoutNotify(MIDletPeer.DESTROY_PENDING);
                }
            }

            this.notify();
        }
    }

    /**
     * Checks if the named <code>MIDlet</code> has already been instantiated.
     * @param name class name of <code>MIDlet</code> to test if
     *             currently run
     * @return <code>true</code> if an instance of the MIDlet is already
     *     running
     */
    public boolean isRunning(String name) {
        boolean found = false;
        synchronized (this) {
            if (underConstructionPeer != null &&
                    underConstructionPeer.getMIDlet().
                        getClass().getName().equals(name)) {
                found = true;
            } else {
                for (int i = 0; i < nmidlets; i++) {
                    if (midlets[i].getMIDlet().
                            getClass().getName().equals(name)) {
                        // found only if has not been destroyed
                        found = (midlets[i].getState() != MIDletPeer.DESTROYED);
                        break;
                    }
                }
            }
        }
        return found;
    }

    /**
     * Gets MIDlet event consumer of the named <code>MIDlet</code>.
     *
     * @param token security token for authorizing the caller
     * @param name class name of <code>MIDlet</code>
     *
     * @return reference of the MIDlet event consumer
     */
    public MIDletEventConsumer getMIDletEventConsumer(SecurityToken token,
                                                      String name) {
        token.checkIfPermissionAllowed(Permissions.AMS);

        synchronized (this) {
            for (int i = 0; i < nmidlets; i++) {
                if (midlets[i].getMIDlet().getClass().getName().equals(name)) {
                    return midlets[i];
                }
            }
        }

        return null;
    }

    /**
     * Looks through the current MIDlets and select one to
     * be processed.
     * <p>Note: that this method is called while synchronized on "this"
     * @return the MIDlet to process next
     */
    private MIDletPeer selectByPriority() {
        MIDletPeer found = null; // Chosen MIDletPeer
        int state = -1;         // the state of the chosen MIDlet

        /*
         * Find the most desirable MIDlet based on its state
         * The higher state values are preferred because they
         * are needed for cleanup.
         */
        for (int i = nmidlets-1; i >= 0; i--) {

            // make sure index is inside current array, favoring the end
            if (scanIndex < 0 || scanIndex >= nmidlets)
                scanIndex = nmidlets-1;

            // Pick this MIDlet if the state is higher priority
            int s = midlets[scanIndex].getState();
            if (s > state) {
                found = midlets[scanIndex];
                state = s;
            }
            scanIndex--;
        }
        return found;
    }

    /**
     * Removes a MIDlet from the list if it is there,
     * otherwise ignore the request.
     * Call only while synchronized on "this".
     * @param m the MIDlet to remove
     */
    private void unregister(MIDletPeer m) {
        // Find it in the list and switch the last one for it.
        for (int i = 0; i < nmidlets; i++) {
            if (m == midlets[i]) {
                // Switch the last MIDlet into that offset.
                midlets[i] = midlets[nmidlets-1];

                // null out from array and remove from map to allow for GC
                midlets[--nmidlets] = null;
                break;
            }
        }
    }

    /**
     * Finds a MIDlet in the list by it class.
     * Only a single MIDlet of a class can be active at
     * a time.
     * Must be called synchronized on "this".
     * @param m the MIDlet to find
     * @return the index in the array of MIDlets.
     *  return -1 if the MIDlet is not found.
     */
    private int findMIDletByClass(MIDletPeer m) {
        // Find it in the list
        for (int i = 0; i < nmidlets; i++) {
            if (m.getMIDlet().getClass() ==
                midlets[i].getMIDlet().getClass()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Creates a MIDlet.
     *
     * @param externalAppId ID of given by an external application manager
     * @param classname name of MIDlet class
     *
     * @return newly created MIDlet
     *
     * @exception ClassNotFoundException if the MIDlet class is
     * not found
     * @exception InstantiationException if the MIDlet cannot be
     * created
     * @exception IllegalAccessException if the MIDlet is not
     * permitted to perform a specific operation
     */
    private MIDlet createMIDlet(int externalAppId, String classname) throws
           ClassNotFoundException, InstantiationException,
           IllegalAccessException {
        MIDlet midlet = null;

        synchronized (createMIDletLock) {
            /*
             * Just in case there is a hole we have not found.
             * Make sure there is not a new MIDlet state already created.
             */
            if (newMidletPeer != null) {
                throw new SecurityException("Recursive MIDlet creation");
            }

            newMidletPeer = new MIDletPeer();
            underConstructionPeer = newMidletPeer;
            try {
                /*
                 * We can send a MIDlet create event because the peer that
                 * the AMS uses has been created.
                 */
                listener.midletCreated(getMIDletSuite(), classname,
                                       externalAppId);

                try {
                    midlet = midletLoader.newInstance(getMIDletSuite(),
                                                      classname);
                    return midlet;
                } finally {
                    if (midlet == null) {
                        /*
                         * The MIDlet was not constructed, send destroy
                         * notification to remove the peer from any lists.
                         */
                        listener.midletDestroyed(getMIDletSuite(), classname,
                                                 null);
                    }
                }
            } finally {
                /* Make sure the creation window is closed. */
                newMidletPeer = null;
            }
        }
    }

    /**
     * Called by the MIDlet constructor to a new MIDletPeer object.
     *
     * @param token security token for authorizing the caller
     * @param m the MIDlet for which this state is being created;
     *          must not be <code>null</code>.
     * @return the preallocated MIDletPeer for the MIDlet being constructed by
     *         {@link #createMIDlet}
     *
     * @exception SecurityException AMS permission is not granted and
     * if is constructor is not being called in the context of
     * <code>createMIDlet</code>.
     */
    public static MIDletPeer newMIDletPeer(MIDlet m) {
        synchronized (createMIDletLock) {
            MIDletPeer temp;

            if (newMidletPeer == null) {
                throw new SecurityException(
                    "MIDlet not constructed by createMIDlet.");
            }

            temp = newMidletPeer;
            newMidletPeer = null;
            temp.midlet = m;
            return temp;
        }
    }

    /**
     * Retrieves current state of the MIDlet given.
     * @param midlet the MIDlet of interest
     * @return the MIDlet state as defined in MIDletPeer class
     */
    public static int getMIDletState(MIDlet midlet) {
        return MIDletPeer.getMIDletPeer(midlet).getState();
    }
}
