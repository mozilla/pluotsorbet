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


public class NativeSensor extends SensorDevice {

    /**
      * Constructor of the device.
      *
      * @param numberSensor - number of sensor
      * @param numberChannel - number of channels
      */
     public NativeSensor(int numberSensor) {
         super(numberSensor); // sensor type is irrelevant
     }

    /**
     * Initialization of channel device.
     *
     * @return true when initialization of channel device
     *         was OK else false
     */
     public boolean initSensor() {
        return doInitSensor(numberSensor);
    }

    /**
     * Finalization of channel device.
     *
     * @return true when finalization of channel device
     *         was OK else false
     */
     public boolean finishSensor() {
        return doFinishSensor(numberSensor);
    }

    /**
      * Checks is sensor device available.
      *
      * @return true when channel device is available else false
      */
     public boolean isAvailable() {
         return doIsAvailable(numberSensor); 
     }

     /**
      * Inform the {@link NativeSensorRegistry} to start listening.
      *
      * @param listener AvailabilityListener listening for notifications
      */
     public void startMonitoringAvailability(AvailabilityListener listener) {
         NativeSensorRegistry.startMonitoringAvailability(numberSensor, listener);
     }

     /**
      * Inform the {@link NativeSensorRegistry} to stop listening.
      */
     public void stopMonitoringAvailability() {
         NativeSensorRegistry.stopMonitoringAvailability(numberSensor);
     }

    /**
     * Initialization of sensor device -- native implementation.
     *
     * @param sensorType a copy of sensorType class member
     * @return true when initialization of channel device
     *         was OK else false
     */
    private native boolean doInitSensor(int numberSensor);
    // calls javacall_result javacall_sensor_open(javacall_sensor_type sensor, void** pContext);
    // uses javacall_result javanotify_sensor_connection_completed( javacall_sensor_type sensor, javacall_bool isOpen, int errCode );

    /**
     * Finalization of channel device -- native implementation.
     *
     * @param sensorType a copy of sensorType class member
     * @return true when finalization of channel device
     *         was OK else false
     */
    private native boolean doFinishSensor(int numberSensor);
    // calls javacall_result javacall_sensor_close(javacall_sensor_type sensor, void** pContext);
    // uses javacall_result javanotify_sensor_connection_completed( javacall_sensor_type sensor, javacall_bool isOpen, int errCode );

    /**
      * Checks is sensor device available -- native implementation.
      *
      * @param sensorType a copy of sensorType class member
      * @return true when channel device is available else false
      */
    private native boolean doIsAvailable(int numberSensor);
    // calls javacall_result javacall_sensor_is_available(javacall_sensor_type sensor);
}
