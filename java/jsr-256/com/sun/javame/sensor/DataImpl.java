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

import javax.microedition.sensor.*;

public class DataImpl implements Data {

    /** ChannelInfo instance. */
    private ChannelInfo channelInfo;

    /** Time stamp included flag. */
    private boolean isTimestampIncluded;

    /** Timestamps array. */
    private long[] timeStamps;

    /** Uncertainty included flag. */
    private boolean isUncertaintyIncluded;

    /** Uncertainties array. */
    private float[] uncertainties;

    /** Validity included flag. */
    private boolean isValidityIncluded;

    /** Validities array. */
    private boolean[] validities;

    /** Data type. */
    private int dataType;

    /** Size of buffer. */
    private int bufferSize;

    /** Object values. */
    private Object[] objectValues;

    /** 
     * Creates a new instance of DataImpl.
     *
     * @param channelInfo - the ChannelInfo instance of the channel
     * from where the data values were fetched
     * @param bufferSize - the size of the buffer of the data values
     * @param dataType - the data type of the channel. The type alternatives are: 
     * * TYPE_INT
     * * TYPE_DOUBLE
     * * TYPE_OBJECT
     * @param isTimestampIncluded - if true timestamps should be 
     *  included in returned Data objects
     * @param isUncertaintyIncluded - if true uncertainties should be
     *  included in returned Data objects
     * @param isValidityIncluded - if true validities should be
     *  included in returned Data objects
     */
    DataImpl(ChannelInfo channelInfo, int bufferSize, int dataType,
        boolean isTimestampIncluded, boolean isUncertaintyIncluded,
        boolean isValidityIncluded) {
        this.channelInfo = channelInfo;
        this.isTimestampIncluded = isTimestampIncluded;
        if (isTimestampIncluded) {
            timeStamps = new long[bufferSize];
        }
        this.isUncertaintyIncluded = isUncertaintyIncluded;
        if (isUncertaintyIncluded) {
            uncertainties = new float[bufferSize];
        }
        this.isValidityIncluded = isValidityIncluded;
        if (isValidityIncluded) {
            validities = new boolean[bufferSize];
        }
        this.dataType = dataType;
        objectValues = new Object[bufferSize];
        this.bufferSize = bufferSize;
    }

    /** 
     * Returns the ChannelInfo that tells the data properties
     * of the channel from where the data values were fetched.
     *
     * @return ChannelInfo of the Data
     */
    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    /**
     * Returns the data values as a double array if the data type
     * of the channel is ChannelInfo.TYPE_DOUBLE.
     *
     * @return the data values as a double array,
     * a zero-length double array if no values have been measured
     * @throws IllegalStateException - if the data type of the
     * channel is not ChannelInfo.TYPE_DOUBLE
     */
    public double[] getDoubleValues() {
        if (dataType != ChannelInfo.TYPE_DOUBLE) {
            throw new IllegalStateException("Data type is not double");
        }
        double[] retValues = new double[0];
        if (objectValues != null) {
            if (bufferSize > 0) {
                retValues = new double[bufferSize];
                Object tmp;
                for (int i = 0; i < bufferSize; i++) {
                    tmp = objectValues[i];
                    if( tmp instanceof Double ){
                        retValues[i] = ((Double)tmp).doubleValue();
                    } else {
                        retValues[i] = 0.0;
                        if (isValidityIncluded && validities[i]) {
                            validities[i] = false;
                        }
                    }                   
                }
            }
        }
        return retValues;
    }

    /**
     * Returns the data values as an int array if the data type
     * of this channel is ChannelInfo.TYPE_INT.
     *
     * @return the data values as an int array, a zero-length
     * int array if no values have been measured
     * @throws IllegalStateException - if the data type of the
     * channel is not ChannelInfo.TYPE_INT
     */
    public int[] getIntValues() {
        if (dataType != ChannelInfo.TYPE_INT) {
            throw new IllegalStateException("Data type is not int");
        }
        int[] retValues = new int[0];
        if (objectValues != null) {
            if (bufferSize > 0) {
                retValues = new int[bufferSize];
                Object tmp;
                for (int i = 0; i < bufferSize; i++) {
                    tmp = objectValues[i];
                    if (tmp instanceof Integer) {
                        retValues[i] = ((Integer)tmp).intValue();
                    } else {
                        retValues[i] = 0;
                        if (isValidityIncluded && validities[i]) {
                            validities[i] = false;
                        }
                    }
                }
            }
        }
        return retValues;
    }

    /**
     * Returns the data values as an array of Objects if the data
     * type of this channel is ChannelInfo.TYPE_OBJECT.
     *
     * @return the data values as an Object array, a zero-length
     * Object array if no values have been measured
     * @throws IllegalStateException - if the data type of the channel
     * is not ChannelInfo.TYPE_OBJECT
     */
    public Object[] getObjectValues() {
        if (dataType != ChannelInfo.TYPE_OBJECT) {
            throw new IllegalStateException("Data type is not Object");
        }
        Object[] retValues = new Object[0];
        if (objectValues != null) {
            if (bufferSize == objectValues.length) {
                retValues = objectValues;
            } else if (bufferSize > 0) {
                retValues = new Object[bufferSize];
                System.arraycopy(objectValues, 0, retValues, 0, bufferSize);
            }
        }
        return retValues;
    }

    /**
     * Returns the timestamp corresponding to the time when
     * the data value indicated by the index was measured.
     *
     * @param index the index of the data value
     * @return the timestamp indicating when the data value was retrieved
     * @throws IndexOutOfBoundsException - if the index is out of the
     * closed range [0, (size of the data buffer - 1)]
     * @throws IllegalStateException - if the timestamp was not requested
     * according to the parameters of the SensorConnection.getData()
     * or the SensorConnection.setDataListener() methods.
     */
    public long getTimestamp(int index) {
        if (!isTimestampIncluded) {
            throw new IllegalStateException("Timestamp wasn't requested");
        }
        return timeStamps[index];
    }

    /**
     * Returns the estimate of the error of the measured data value.
     *
     * @param index the index of the data value
     * @return the uncertainty of the data value
     * @throws IndexOutOfBoundsException - if the index is out of the
     * closed range [0,(size of the data buffer - 1)]
     * @throws IllegalStateException - if the uncertainty was not
     * requested according to the parameters of the
     * SensorConnection.getData() or the 
     * SensorConnection.setDataListener() methods.
     */
    public float getUncertainty(int index) {
        if (!isUncertaintyIncluded) {
            throw new IllegalStateException("Uncertainty wasn't requested");
        }
        return uncertainties[index];
    }

    /**
     * Returns the validity of the data value at the given index.
     *
     * @param index the index of the data value
     * @return the validity of the data value
     * @throws IndexOutOfBoundsException - if the index is out of the
     * closed range [0, (size of the data buffer - 1)]
     * @throws IllegalStateException - if the validity was not requested
     * according to the parameters of the method
     * SensorConnection.getData() or SensorConnection.setDataListener().
     */
    public boolean isValid(int index) {
        if (!isValidityIncluded) {
            throw new IllegalStateException("Validity wasn't requested");
        }
        return validities[index];
    }

    /** 
     * Sets the new size of buffer.
     *
     * @param bufferSize - new buffer size value
     */
    void setBufferSize(int bufferSize) {
        if ( this.bufferSize != bufferSize ){
        //If buffer is not large enough, increase the buffer size so that all read data
        //can be stored in the Data instance.
            int minSize = Math.min(this.bufferSize, bufferSize);
            long[] tmpTimeStamps = null;
            float[] tmpUncertainties = null;
            boolean[] tmpValidities = null;
            Object[] tmpObjectValues = null;
            if (timeStamps != null) {
                tmpTimeStamps = new long[minSize];
                System.arraycopy( timeStamps, 0, tmpTimeStamps, 0, minSize);
            }
            if ( uncertainties != null) {
                tmpUncertainties = new float[minSize];
                System.arraycopy( uncertainties, 0, tmpUncertainties, 0, minSize);
            }
            if ( validities != null) {
                tmpValidities = new boolean[minSize];
                System.arraycopy( validities, 0, tmpValidities, 0, minSize);
            }
            tmpObjectValues = new Object[minSize];
            System.arraycopy( objectValues, 0, tmpObjectValues, 0, minSize);
            
            timeStamps = tmpTimeStamps;
            uncertainties = tmpUncertainties;
            validities = tmpValidities;
            objectValues = tmpObjectValues;
        }
        this.bufferSize = bufferSize;
   }

    /** 
     * Sets the data to buffer.
     *
     * @param index - index of place in buffer
     * @param item - data item for saving
     */
    void setData(int index, Object item) {
        objectValues[index] = item;
    }

    /**
     * Sets the timestamp to buffer.
     *
     * @param index - index of place in buffer
     * @param timestamp - data item for saving
     */
    void setTimestamp(int index, long timestamp) {
        if (isTimestampIncluded) {
            timeStamps[index] = timestamp;
        }
    }

    /**
     * Sets the uncertainty to buffer.
     *
     * @param index - index of place in buffer
     * @param uncertainty - uncertainty for saving
     */
    void setUncertainty(int index, float uncertainty) {
        if (isUncertaintyIncluded) {
            uncertainties[index] = uncertainty;
        }
    }

    /**
     * Sets the validity to buffer.
     *
     * @param index - index of place in buffer
     * @param validity - data item for saving
     */
    void setValidity(int index, boolean validity) {
        if (isValidityIncluded) {
            validities[index] = validity;
        }
    }
}