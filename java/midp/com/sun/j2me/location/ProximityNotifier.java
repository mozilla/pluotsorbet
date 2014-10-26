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

package com.sun.j2me.location;

import com.sun.j2me.log.Logging;
import java.util.Vector;
import javax.microedition.location.Coordinates;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.ProximityListener;
import javax.microedition.location.QualifiedCoordinates;

/**
 * Asynchronous thread for periodic location
 * event handling.
 */
public class ProximityNotifier {
    /**
     * Instance of the proximity notifier. If the application does not
     * use proximity notification, this class will not be instantiated.
     */
    static ProximityNotifier Instance = null;
    
    /** Array of registered listeners. */
    Vector proximityListeners = new Vector();
    
    /** Thread for proximity notifications. */
    ProximityThread proximityThread = null;
    
    /** Current thread performing monitoring state updating. */
    StateMonitorThread stateThread = null;
    
    /** Dedicated provider for delivering proximity data. */
    LocationProviderImpl proximityProvider = null;

    /**
     * Gets a handle to proximity notifier.
     * @return current proximity notifier handle
     */
    public static ProximityNotifier getInstance() {
        if (Instance == null) {
            Instance = new ProximityNotifier();
        }
        return Instance;
    }
    
    /**
     * Constructor.
     */
    private ProximityNotifier() {
    }

    /**
     * This class wraps proximity listeners and adds additional information.
     */
    static class ProximityListenerDecorator {
	
	/** Location proximity listener. */
        private ProximityListener listener;
	
	/** Coordinates to monitor. */
        private Coordinates coordinates;
	
	/** Radius for proximity check. */
        private float proximityRadius;
	
         // JAVADOC COMMENT ELIDED
        ProximityListenerDecorator(ProximityListener listener,
				   Coordinates coordinates,
				   float proximityRadius) {
            this.listener = listener;
            this.coordinates = coordinates;
            this.proximityRadius = proximityRadius;
        }

        // JAVADOC COMMENT ELIDED
        public void proximityEvent(Location location) {
            // Nothing to notify if an invalid location
            if (!location.isValid()) {
                return;
            }
            QualifiedCoordinates qCoord = location.getQualifiedCoordinates();
            float distance = coordinates.distance(qCoord);
            float hAccuracy = qCoord.getHorizontalAccuracy();
            if (Float.isNaN(hAccuracy)) {
                hAccuracy = 0.0F;
            }
	    /* 
	     * Perform a stricter test for proximity. The looser test for
	     * proximity would be distance + hAccuracy but that would
	     * mean that we might be notifying for a location that's not
	     * within the requested radius.
	     */
	    if (distance - hAccuracy <= proximityRadius) {
		// listener should be removed *before* notifying,
		// because it can be re-registered in the user code
		Instance.removeProximityListener(listener);
                listener.proximityEvent(coordinates, location);
            }
        }

	/**
	 * Monitor state change dispatcher.
	 *
	 * @param isMonitoringActive is trur if monitoring is enabled
	 */
        public void monitoringStateChanged(boolean isMonitoringActive) {
            listener.monitoringStateChanged(isMonitoringActive);
        }
    }

    // JAVADOC COMMENT ELIDED
    public void addProximityListener(ProximityListener listener,
				     Coordinates coordinates,
				     float proximityRadius) {
        synchronized (proximityListeners) {
	    proximityListeners.addElement(
		new ProximityListenerDecorator(listener, coordinates,
					       proximityRadius));
	}
	synchronized (this) {
	    if (proximityProvider == null) {
		try {
		    proximityProvider =
			LocationProviderImpl.getInstanceImpl(null);
		} catch (LocationException e) {
		    // nothing to do
		}
	    }
	    if (proximityThread == null) {
		proximityThread = new ProximityThread();
		proximityThread.start();
	    } else {
            synchronized (proximityThread) {
               proximityThread.notify();
            } 
        }
	    if (stateThread == null) {
		stateThread = new StateMonitorThread();
		stateThread.start();
	    } else {
            synchronized (stateThread) {
               stateThread.notify();
            } 
	    }
	}
    }

    // JAVADOC COMMENT ELIDED
    public void removeProximityListener(ProximityListener listener) {
        ProximityListenerDecorator[] listeners;
        synchronized (proximityListeners) {
            listeners =
		new ProximityListenerDecorator[proximityListeners.size()];
            proximityListeners.copyInto(listeners);
        }
	for (int i = 0; i < listeners.length; i++) {
	    if (listeners[i].listener == listener) {
		synchronized (proximityListeners) {
		    proximityListeners.removeElement(listeners[i]);
		}
	    }
	}
	// check if it was the last listener
	if (proximityListeners.isEmpty()) {
	    synchronized (this) {
		if (proximityThread != null) {
		    proximityThread.terminate();
		    if (Thread.currentThread() != proximityThread) {
			try { // wait for thread to die
			    proximityThread.join();
			} catch (InterruptedException e) { // do nothing
			    if (Logging.TRACE_ENABLED) {
				Logging.trace(e, "Wrong thread exception.");
			    }
			}
		    }
		    proximityThread = null;
		}
		if (stateThread != null) {
		    stateThread.terminate();
		    try { // wait for thread to die
			stateThread.join();
		    } catch (InterruptedException e) { // do nothing
			if (Logging.TRACE_ENABLED) {
			    Logging.trace(e, "Wrong thread exception.");
			}
		    }
		    stateThread = null;
		}
		// dedicated provider is no longer needed
		proximityProvider = null;
	    }
	}
    }
    
    /**
     * Dispatches a proximity event.
     *
     * @param location location that triggered the proximity event
     */
    void fireProximityEvent(Location location) {
        // prevents concurent modification in which the event code modifies
        // the vector by invoking remove/add during event execution
        ProximityListenerDecorator[] listeners;
        synchronized (proximityListeners) {
            listeners =
		new ProximityListenerDecorator[proximityListeners.size()];
            proximityListeners.copyInto(listeners);
        }
        if (listeners.length > 0) {
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].proximityEvent(location);
            }
        }
    }

    /**
     * Dispatches a monitor stat changed event.
     *
     * @param isMonitoringActive is true if monitoring is enabled
     */
    void fireMonitoringStateChanged(boolean isMonitoringActive) {
        // prevents concurent modification in which the event code modifies
        // the vector by invoking remove/add during event execution
        ProximityListenerDecorator[] listeners;
        synchronized (proximityListeners) {
            listeners =
		new ProximityListenerDecorator[proximityListeners.size()];
            proximityListeners.copyInto(listeners);
        }
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].monitoringStateChanged(isMonitoringActive);
        }
    }

    /**
     * Returns a vector of proximity listeners.
     *
     * @return the vector of registered proximity listeners
     */
    Vector getListeners() {
        return proximityListeners;
    }

    /**
     * The recommended time interval for proximity update events.
     *
     * @return the default interval of the dedicated provider
     */
    int getProximityInterval() {
	if (proximityProvider != null) {
	    return proximityProvider.getDefaultInterval();
	}
	return 10;
    }

    /**
     * The recommended time interval for querying monitoring state.
     *
     * @return time interval in seconds
     */
    int getStateInterval() {
	if (proximityProvider != null) {
	    return proximityProvider.getStateInterval();
	}
	return 10;
    }

    /**
     * Retrieves location from the dedicated provider.
     *
     * @return location to be used for proximity detection.
     */
    Location getLocation() {
        Location location = null;
        try {
            if (proximityProvider == null) {
                proximityProvider = LocationProviderImpl.getInstanceImpl(null);
            }
            location = proximityProvider.getLocationImpl(-1);
        } catch (LocationException e) {
            //nothing to do
        } catch (InterruptedException e) {
            //nothing to do
        }
        return location;
    }

    /**
     * Checks if the monitoring is active.
     *
     * @return true if monitoring is active, false otherwise
     */
    boolean getMonitoringState() {
	return proximityProvider != null &&
	    proximityProvider.getState() == LocationProvider.AVAILABLE;
    }
}

/**
 *  Class ProximityThread sends proximity notifications 
 *  when the proximity is detected.
 */
class ProximityThread extends Thread {

    /** Flag indicating if the thread should terminate. */
    private boolean terminated = false;
  
    /**
    * Constructor.
    */
    ProximityThread() {
    }
    
    /**
    * Terminates the thread.
    */
    void terminate() {
	terminated = true;
	synchronized (this) {
	    notify();
	}
    }
    
    /**
     * Runs the proximity notifier logic.
     */
    public void run() {
	    ProximityNotifier notifier = ProximityNotifier.getInstance();
	    // time interval for updating location
	    int interval = notifier.getProximityInterval();
        Location l = notifier.getLocation();
	    try {
            while (!terminated) {
                if (l != null) {
                    notifier.fireProximityEvent(l);
                }
                if (terminated) { // the thread was stopped
                    break;
                }
                long startWait = System.currentTimeMillis();
	            synchronized (this) {
                    wait((long)interval * 1000);
                }
                l = LocationProviderImpl.getLastKnownLocation();
                if (l == null || (l.getTimestamp() < startWait)) {
                    l = notifier.getLocation();
                }
            }
        } catch (InterruptedException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Wrong thread exception.");
            }
        }
    }
}

/**
 *  Class StateMonitorThread sends notifications when
 *  the state of monitor is changed.
 */
class StateMonitorThread extends Thread {
    
    /** Flag indicating if the thread should terminate. */
    private boolean terminated = false;
   
    /**
     * Constructor.
     */
    StateMonitorThread() {
    }
    
    /**
     * Terminates the thread.
     */
    void terminate() {
	terminated = true;
	synchronized (this) {
	    notify();
	}
    }
    
    /**
     * Runs the proximity notifier logic.
     */
    public void run() {
        boolean state = true;
	ProximityNotifier notifier = ProximityNotifier.getInstance();
	// time interval for checking the state in seconds
	int interval = notifier.getStateInterval();
	try {
	    while (!terminated) {
		boolean newState = notifier.getMonitoringState();
		if (newState != state) {
		    state = newState;
		    notifier.fireMonitoringStateChanged(state);
		}
		if (terminated) { // the thread was stopped
		    break;
		}
	        synchronized (this) {
                    wait((long)interval * 1000);
		}
            }
        } catch (InterruptedException e) {
            if (Logging.TRACE_ENABLED) {
	        Logging.trace(e, "Wrong thread exception.");
            }
        }
    }
}    
