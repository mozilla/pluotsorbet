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

public class DeviceFactory {
    /**
     * Sensor types. These constants are recognized at the native level,
     * if there is native level.
     */
    /** sensor:current_bearer */
    public static final int SENSOR_CURRENT_BEARER =0;
    /** sensor:network_code */
    public static final int SENSOR_NETWORK_CODE=1;
    /** sensor:network_quality */
    public static final int SENSOR_NETWORK_QUALITY=2;
    /** sensor:data_counter */
    public static final int SENSOR_DATA_COUNTER=3;
    /** sensor:cellid */
    public static final int SENSOR_CELL_ID=4;
    /** sensor:sound_level */
    public static final int SENSOR_SOUND_LEVEL=5;
    /** sensor:battery_level */
    public static final int SENSOR_BATTERY_LEVEL=6;
    /** sensor:battery_charge */
    public static final int SENSOR_BATTERY_CHARGE=7;
    /** sensor:flip_state */
    public static final int SENSOR_FLIP_STATE=8;
    /** sensor:dvbh_quality */
    public static final int SENSOR_DVBH_SIGNAL_QUALITY=9;
    /** sensor:dvbh_realtime*/
    public static final int SENSOR_DVBH_REALTIME=10;
    /** other type of sensor, that is, not recognized at the native level */
    public static final int SENSOR_OTHER=-1;
    /** Special testing sensor, sensor:sensor_tester. */
    public static final int SENSOR_TESTER = -2;

    /** 
     * Generates a channel implementation instance.
     *
     * @param numberSensor number of the sensor based 0
     * @param numberChannel number of the channel based 0
     * @return new channel implementation instance
     */
    public static ChannelDevice generateChannel(int numberSensor, int numberChannel) {
        return new NativeChannel(numberSensor, numberChannel);
    }

    /**
     * Generates a sensor implementation instance.
     *
     * @param numberSensor number of the sensor based 0
     * @param channelCount number of channels
     * @return new sensor implementation instance, or null
     */
    public static SensorDevice generateSensor(int numberSensor) {
        return new NativeSensor(numberSensor);
    }
}
