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
public class Criteria {
    // JAVADOC COMMENT ELIDED
    public static final int NO_REQUIREMENT = 0;
    // JAVADOC COMMENT ELIDED
    public static final int POWER_USAGE_LOW = 1;
    // JAVADOC COMMENT ELIDED
    public static final int POWER_USAGE_MEDIUM = 2;
    // JAVADOC COMMENT ELIDED
    public static final int POWER_USAGE_HIGH = 3;

    // JAVADOC COMMENT ELIDED
    private int preferredPowerConsumption = NO_REQUIREMENT;
    // JAVADOC COMMENT ELIDED
    private boolean allowedToCost = true;
    // JAVADOC COMMENT ELIDED
    private boolean speedAndCourseRequired = false;
    // JAVADOC COMMENT ELIDED
    private boolean altitudeRequired = false;
    // JAVADOC COMMENT ELIDED
    private boolean addressInfoRequired = false;
    // JAVADOC COMMENT ELIDED
    private int horizontalAccuracy = NO_REQUIREMENT;
    // JAVADOC COMMENT ELIDED
    private int verticalAccuracy = NO_REQUIREMENT;
    // JAVADOC COMMENT ELIDED
    private int preferredResponseTime = NO_REQUIREMENT;

    // JAVADOC COMMENT ELIDED
    public Criteria() {
    }

    // JAVADOC COMMENT ELIDED
    public int getPreferredPowerConsumption() {
        return preferredPowerConsumption;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isAllowedToCost() {
        return allowedToCost;
    }

    // JAVADOC COMMENT ELIDED
    public int getVerticalAccuracy() {
        return verticalAccuracy;
    }

    // JAVADOC COMMENT ELIDED
    public int getHorizontalAccuracy() {
        return horizontalAccuracy;
    }

    // JAVADOC COMMENT ELIDED
    public int getPreferredResponseTime() {
        return preferredResponseTime;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isSpeedAndCourseRequired() {
        return speedAndCourseRequired;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isAltitudeRequired() {
        return altitudeRequired;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isAddressInfoRequired() {
        return addressInfoRequired;
    }

    // JAVADOC COMMENT ELIDED
    public void setHorizontalAccuracy(int accuracy) {
        horizontalAccuracy = accuracy;
    }

    // JAVADOC COMMENT ELIDED
    public void setVerticalAccuracy(int accuracy) {
        verticalAccuracy = accuracy;
    }

    // JAVADOC COMMENT ELIDED
    public void setPreferredResponseTime(int time) {
        preferredResponseTime = time;
    }

    // JAVADOC COMMENT ELIDED
    public void setPreferredPowerConsumption(int level) {
        preferredPowerConsumption = level;
    }

    // JAVADOC COMMENT ELIDED
    public void setCostAllowed(boolean costAllowed) {
        allowedToCost = costAllowed;
    }

    // JAVADOC COMMENT ELIDED
    public void setSpeedAndCourseRequired(boolean speedAndCourseRequired) {
        this.speedAndCourseRequired = speedAndCourseRequired;
    }

    // JAVADOC COMMENT ELIDED
    public void setAltitudeRequired(boolean altitudeRequired) {
        this.altitudeRequired = altitudeRequired;
    }

    // JAVADOC COMMENT ELIDED
    public void setAddressInfoRequired(boolean addressInfoRequired) {
        this.addressInfoRequired = addressInfoRequired;
    }

}

