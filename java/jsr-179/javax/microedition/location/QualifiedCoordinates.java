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

/**
 * This class is defined by the JSR-179 specification
 * <em>Location API for J2ME for J2ME&trade;.</em>
 */
// JAVADOC COMMENT ELIDED
public class QualifiedCoordinates extends Coordinates {
    /** Horizontal accuracy. */
    private float horizontalAccuracy;
    /** Vertical accuracy. */
    private float verticalAccuracy;

    // JAVADOC COMMENT ELIDED
    public QualifiedCoordinates(double latitude, double longitude,
                                float altitude,	float horizontalAccuracy,
				float verticalAccuracy) {
        super(latitude, longitude, altitude);
        setHorizontalAccuracy(horizontalAccuracy);
        setVerticalAccuracy(verticalAccuracy);
    }

    // JAVADOC COMMENT ELIDED
    public float getHorizontalAccuracy() {
        return horizontalAccuracy;
    }

    // JAVADOC COMMENT ELIDED
    public float getVerticalAccuracy() {
        return verticalAccuracy;
    }

    // JAVADOC COMMENT ELIDED
    public void setHorizontalAccuracy(float horizontalAccuracy) {
        if (horizontalAccuracy < 0) {
            throw new 
		IllegalArgumentException("Horizontal accuracy has to be "
					 + "larger than 0, it was set to: "
					 + horizontalAccuracy);
        }
        this.horizontalAccuracy = horizontalAccuracy;
    }

    // JAVADOC COMMENT ELIDED
    public void setVerticalAccuracy(float verticalAccuracy) {
        if (verticalAccuracy < 0) {
            throw new IllegalArgumentException(
		"Vertical accuracy has to be larger than 0, it was set to: "
		+ verticalAccuracy);
        }
        this.verticalAccuracy = verticalAccuracy;
    }
}
