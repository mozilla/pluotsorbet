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

import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class NativeChannel extends ChannelDevice {
    
    private Object[] dataBuffers;
    private boolean[] validityBuffers;
    private float[] uncertaintyBuffers;
   
    /**
      * Constructor of the device.
      *
      * @param numberSensor - number of sensor
      * @param numberChannel - number of channel
      */
     public NativeChannel(int numberSensor, int numberChannel) {
         super(numberSensor,numberChannel);
     }

   /**
     * Initialization of channel device.
     *
     * @return true when initialization of channel device
     * was OK else false
     */
    public boolean initChannel() {
        // done in sensor initialization
        return true;
    }

   /**
     * Measures the next data from channel.
     *
     * @param sensorNumber - number of sensor
     * @param channelNumber - number of channel
     * @param sensorType
     * @return error code of measuring
     */
    protected synchronized int measureData(int sensorNumber, int channelNumber) {
    
      byte[] buffer = doMeasureData(sensorNumber,channelNumber);
      
      
      if (buffer.length < 0)
        return ValueListener.SENSOR_UNAVAILABLE;
        
      DataInputStream is =
                new DataInputStream(new ByteArrayInputStream(buffer));
      try {
        byte dataType = is.readByte();
        int dataLen = is.readInt();
  
        validityBuffers = new boolean[dataLen];
        uncertaintyBuffers = new float[dataLen];

        if (dataType == 1) {
          dataBuffers = new Double[dataLen];
        } else if (dataType == 2) {
          dataBuffers = new Integer[dataLen];
        } else if (dataType == 4) {
          dataBuffers = new Object[dataLen];
        } else {
          return ValueListener.MEASURING_FAIL;
        }
        for (int i = 0; i < dataLen; i++) {
          validityBuffers[i] = is.readBoolean();
          uncertaintyBuffers[i] = is.readFloat();
          if (dataType == 1) {
            dataBuffers[i] = new Double(is.readDouble());
          } else if (dataType == 2) {
            dataBuffers[i] = new Integer(is.readInt());
          } else if (dataType == 4) {
            // TODO use custom "object decoder" instance
            throw new RuntimeException("Unsupported data type object");
          }
        }
      } catch (IOException e) {
        return ValueListener.MEASURING_FAIL;
      }
      return ValueListener.DATA_READ_OK;
    }

   /**
     * Measures the next data from channel.
     *
     * @param sensorNumber - number of sensor
     * @param channelNumber - number of channel
     * @param sensorType
     * @return error code of measuring
     */
    private native byte[] doMeasureData(int sensorNumber, int channelNumber);

   /**
     * Gets the last data from channel.
     *
     * @param sensorNumber - number of sensor
     * @param channelNumber - number of channel
     * @return data of measuring
     */
    protected Object[] getData(int sensorNumber, int channelNumber) {;
        return dataBuffers;
    }

    /**
      * Gets the last uncertainty from channel.
      *
      * @param sensorNumber - number of sensor
      * @param channelNumber - number of channel
      * @return uncertainty of measuring
      */
     protected float[] getUncertainty(int sensorNumber, int channelNumber) {
        return uncertaintyBuffers;
    }

    /**
      * Gets the last validity from channel.
      *
      * @param sensorNumber - number of sensor
      * @param channelNumber - number of channel
      * @return validity of measuring
      */
     protected boolean[] getValidity(int sensorNumber, int channelNumber) {
         return validityBuffers;
    }

   /**
     * Checks is channel device available.
     *
     * @return true when channel device is available else false
     */
    public boolean isAvailable() {
        return true; // always available
    }


}