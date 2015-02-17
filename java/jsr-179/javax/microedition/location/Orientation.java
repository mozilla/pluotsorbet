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

import com.sun.j2me.location.OrientationProvider;
import com.sun.j2me.location.Util;
import com.sun.j2me.security.LocationPermission;

/**
 * This class is defined by the JSR-179 specification
 * <em>Location API for J2ME for J2ME&trade;.</em>
 */
// JAVADOC COMMENT ELIDED
public class Orientation {
    /** Angle off the horizon. */
    private float azimuth;
    /** Sample uses magnetic north. */
    private boolean isMagnetic;
    /** Pitch direction. */
    private float pitch;
    /** Roll direction. */
    private float roll;

    // JAVADOC COMMENT ELIDED
    public Orientation(float azimuth, boolean isMagnetic,
		       float pitch, float roll) {
        this.azimuth = azimuth;
        this.isMagnetic = isMagnetic;
        this.pitch = pitch;
        this.roll = roll;
    }

    // JAVADOC COMMENT ELIDED
    public float getCompassAzimuth() {
        return azimuth;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isOrientationMagnetic() {
        return isMagnetic;
    }

    // JAVADOC COMMENT ELIDED
    public float getPitch() {
        return pitch;
    }

    // JAVADOC COMMENT ELIDED
    public float getRoll() {
        return roll;
    }

    // JAVADOC COMMENT ELIDED
    public static Orientation getOrientation() throws LocationException {
        Util.checkForPermission(LocationPermission.ORIENTATION);
        OrientationProvider provider = OrientationProvider.getInstance();
        if (provider != null) {
            return provider.getOrientation();
        }
        return null;
    }
}
