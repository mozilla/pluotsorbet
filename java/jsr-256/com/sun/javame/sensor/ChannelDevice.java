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

import java.util.*;

public abstract class ChannelDevice {

    /** Sensor number. */
    protected int numberSensor;

    /** Channel number. */
    protected int numberChannel;

    /** Hashtable of listeners. */
    private Vector listeners = new Vector();
    
    /**
     * Constructor of the device.
     *
     * @param numberSensor - number of sensor
     * @param numberChannel - number of channel
     * @param sensorType - Sensor type. This is an ID needed only at the native level.
     */
    public ChannelDevice(int numberSensor, int numberChannel) {
        this.numberSensor = numberSensor;
        this.numberChannel = numberChannel;
    }

    /**
     * Initialization of channel device.
     *
     * @return true when initialization of channel device
     * was OK else false
     */
    public abstract boolean initChannel();

    /**
     * Measures the next data from channel.
     *
     * @param numberSensor - number of sensor
     * @param numberChannel - number of channel
     * @param sensorType - Sensor type. This is an ID needed only at the native level.
     * @return error code of measuring
     */
    protected abstract int measureData(int numberSensor, int numberChannel);

   /**
     * Gets the last data from channel.
     *
     * @param numberSensor - number of sensor
     * @param numberChannel - number of channel
     * @return data of measuring
     */
    protected abstract Object[] getData(int numberSensor, int numberChannel);

    /**
      * Gets the last uncertainty from channel.
      *
      * @param numberSensor - number of sensor
      * @param numberChannel - number of channel
      * @return uncertainty of measuring
      */
    protected abstract float[] getUncertainty(int numberSensor, int numberChannel);

    /**
      * Gets the last validity from channel.
      *
      * @param numberSensor - number of sensor
      * @param numberChannel - number of channel
      * @return validity of measuring
      */
    protected abstract boolean[] getValidity(int numberSensor, int numberChannel);

    /**
     * Gets the value listener.
     *
     * @return value listener instance
     */
    synchronized ValueListener getListener() {
        ValueListener returnValue = null;
        if (listeners.size() > 0) {
            returnValue = (ValueListener)listeners.firstElement();
            listeners.removeElementAt(0);
        }
        return returnValue;
    }

   /**
     * Measures the next data from channel.
     *
     * @return error code of measuring
     */
    int measureData() {
        return measureData(numberSensor, numberChannel);
    }

   /**
     * Gets the last data from channel.
     *
     * @return data of measuring
     */
    Object[] getData() {
        return getData(numberSensor, numberChannel);
    }

    /**
      * Gets the last uncertainty from channel.
      *
      * @return uncertainty of measuring
      */
    float[] getUncertainty() {
        return getUncertainty(numberSensor, numberChannel);
    }

    /**
      * Gets the last validity from channel.
      *
      * @return validity of measuring
      */
    boolean[] getValidity() {
        return getValidity(numberSensor, numberChannel);
    }

    /**
      * Starts getting data from channel.
      *
      * @param listener - value listener instance
      */
    synchronized void startGetData(ValueListener listener) {
        listeners.addElement(listener);
        NativeSensorRegistry.postSensorEvent(NativeSensorRegistry.EVENT_DATA_COLLECT_CODE,
            numberSensor, numberChannel, 0);
    }

   /**
     * Checks is channel device available.
     *
     * @return true when channel device is available else false
     */
    public abstract boolean isAvailable();
    
}
