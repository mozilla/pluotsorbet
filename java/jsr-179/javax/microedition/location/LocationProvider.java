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

package javax.microedition.location;

import com.sun.j2me.location.LocationProviderImpl;
import com.sun.j2me.location.ProximityNotifier;
import com.sun.j2me.location.Util;
import com.sun.j2me.main.Configuration;
import com.sun.j2me.security.LocationPermission;

/**
 * This class is defined by the JSR-179 specification
 * <em>Location API for J2ME for J2ME&trade;.</em>
 */
// JAVADOC COMMENT ELIDED
public abstract class LocationProvider {

    /** Proximity support property */
    private static final String PROXIMITY_SUPPORTED = 
        "com.sun.j2me.location.ProximitySupported";

    // JAVADOC COMMENT ELIDED
    public static final int AVAILABLE = 1;

    // JAVADOC COMMENT ELIDED
    public static final int TEMPORARILY_UNAVAILABLE = 2;

    // JAVADOC COMMENT ELIDED
    public static final int OUT_OF_SERVICE = 3;

    // JAVADOC COMMENT ELIDED
    protected LocationProvider() {
    }

    // JAVADOC COMMENT ELIDED
    public abstract int getState();

    // JAVADOC COMMENT ELIDED
    public static LocationProvider getInstance(Criteria criteria)
	throws LocationException {
        return LocationProviderImpl.getInstanceImpl(criteria);
    }

    // JAVADOC COMMENT ELIDED
    public abstract Location getLocation(int timeout)
	throws LocationException, InterruptedException;
    
    // JAVADOC COMMENT ELIDED
    public abstract void setLocationListener(LocationListener listener,
					     int interval, int timeout,
					     int maxAge);

    // JAVADOC COMMENT ELIDED
    public abstract void reset();

    // JAVADOC COMMENT ELIDED
    public static Location getLastKnownLocation() {
        Util.checkForPermission(LocationPermission.LOCATION, false);
        return LocationProviderImpl.getLastKnownLocation();
    }

    // JAVADOC COMMENT ELIDED
    public static void addProximityListener(ProximityListener listener,
	Coordinates coordinates, float proximityRadius)
	throws LocationException {
        String proximitySupported = 
                Configuration.getProperty(PROXIMITY_SUPPORTED);
        if (proximitySupported.equals("true")) {
            Util.checkForPermission(LocationPermission.LOCATION_PROXIMITY, false);
            if (listener == null || coordinates == null) {
                throw new NullPointerException();
            }
            if (proximityRadius <= 0.0F || Float.isNaN(proximityRadius)) {
                throw new IllegalArgumentException(
                    "Illegal proximityRadius: " + proximityRadius);
            }
            ProximityNotifier.getInstance().addProximityListener(listener,
                coordinates, proximityRadius);
        } else {
            throw new LocationException(
                    "Proximity monitoring is not supported");
        }
    }

    // JAVADOC COMMENT ELIDED
    public static void removeProximityListener(ProximityListener listener) {
        if (listener == null) {
	    throw new NullPointerException("Proximity listener is null");
	}
        ProximityNotifier.getInstance().removeProximityListener(listener);
    }
    
}
