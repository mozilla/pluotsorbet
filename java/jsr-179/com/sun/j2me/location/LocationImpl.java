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

import javax.microedition.location.*;

/**
 * This class is an implementation of the <code>Location</code> class defined
 * by the JSR-179 specification.
 */
// JAVADOC COMMENT ELIDED
public class LocationImpl extends Location {
    // JAVADOC COMMENT ELIDED
    private static final String MIMETYPE_NMEA_STRING = 
                                "application/X-jsr179-location-nmea";
    // JAVADOC COMMENT ELIDED				
    private static final String MIMETYPE_LIF_STRING = 
                                "application/X-jsr179-location-lif";
    // JAVADOC COMMENT ELIDED				
    private static final String MIMETYPE_PLAIN_STRING = "text/plain";

    // JAVADOC COMMENT ELIDED
    private boolean isValid;
    // JAVADOC COMMENT ELIDED
    private long timestamp;
    // JAVADOC COMMENT ELIDED
    private QualifiedCoordinates coordinates;
    // JAVADOC COMMENT ELIDED
    private float speed;
    // JAVADOC COMMENT ELIDED
    private float course;
    // JAVADOC COMMENT ELIDED
    private int method;
    // JAVADOC COMMENT ELIDED
    private AddressInfo address;
    // JAVADOC COMMENT ELIDED
    String extraInfoNMEA;
    // JAVADOC COMMENT ELIDED
    String extraInfoLIF;
    // JAVADOC COMMENT ELIDED
    String extraInfoPlain;
    // JAVADOC COMMENT ELIDED
    String extraInfoOther;
    // JAVADOC COMMENT ELIDED
    String extraInfoOtherMIMEType;

    // JAVADOC COMMENT ELIDED
    LocationImpl(QualifiedCoordinates coordinates, float speed,
		 float course, int method, AddressInfo address, 
                 boolean isValid) {
	this.isValid = isValid;
	this.timestamp = System.currentTimeMillis();
	this.coordinates = coordinates;
	this.speed = speed;
	this.course = course;
	this.method = method;
	this.address = address;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isValid() {
        return isValid;
    }

    // JAVADOC COMMENT ELIDED
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    // JAVADOC COMMENT ELIDED
    public long getTimestamp() {
        return timestamp;
    }

    // JAVADOC COMMENT ELIDED
    public void setTimestamp(long timestamp) {
        if (timestamp == 0) {
            this.timestamp = System.currentTimeMillis();
        } else {
            this.timestamp = timestamp;
        }
    }

    // JAVADOC COMMENT ELIDED
    public void setTimestamp() {
        timestamp = System.currentTimeMillis();
    }

    // JAVADOC COMMENT ELIDED
    public QualifiedCoordinates getQualifiedCoordinates() {
        return coordinates;
    }

    // JAVADOC COMMENT ELIDED
    public float getSpeed() {
        return speed;
    }

    // JAVADOC COMMENT ELIDED
    public float getCourse() {
        return course;
    }

    // JAVADOC COMMENT ELIDED
    public int getLocationMethod() {
        return method;
    }

    // JAVADOC COMMENT ELIDED
    public AddressInfo getAddressInfo() {
        return address;
    }

    // JAVADOC COMMENT ELIDED
    public String getExtraInfo(String mimetype) {
        if (mimetype == null) {
            return null;
        }
        if (mimetype.equalsIgnoreCase(MIMETYPE_NMEA_STRING)) {
            return extraInfoNMEA;
        }
        if (mimetype.equalsIgnoreCase(MIMETYPE_LIF_STRING)) {
            return extraInfoLIF;
        }
        if (mimetype.equalsIgnoreCase(MIMETYPE_PLAIN_STRING)) {
            return extraInfoPlain;
        }
        if (mimetype.equalsIgnoreCase(extraInfoOtherMIMEType)) {
            return extraInfoOther;
        }
        return null;
    }
    
}
