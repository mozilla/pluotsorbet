/*
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

package com.sun.javame.sensor;

/**
 * Derivatives of this class will contain code
 * responsible for initialization and shutdown
 * of a physical sensor.
*/
public abstract class SensorDevice {

    /** Sensor number. */
    int numberSensor;

    /**
     * Constructor of the device.
     *
     * @param numberSensor - number of sensor
    * @param numberOfChannels - number of channels
    * @param sensorType - Sensor type. This is an ID needed only at the native level.
    */
    public SensorDevice(int numberSensor) {
        this.numberSensor = numberSensor;
    }

   /**
     * Initialization of channel device.
     *
     * @return true when initialization of channel device
     * was OK else false
     */
    public abstract boolean initSensor();

   /**
     * Finalization of channel device.
     *
     * @return true when finalization of channel device
     * was OK else false
     */
    public abstract boolean finishSensor();

   /**
     * Checks if sensor device is available.
     *
     * @return true when sensor device is available else false
     */
    public abstract boolean isAvailable();

    /**
     * Inform the SensorDevice to start monitoring availability.
     *
     * @param listener AvailabilityListener listening for notifications
     */
    public abstract void startMonitoringAvailability(
            AvailabilityListener listener);

    /**
     * Inform the SensorDevice to stop monitoring availability.
     */
    public abstract void stopMonitoringAvailability();
}
