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

package javax.microedition.sensor;

public interface SensorConnection extends javax.microedition.io.Connection {
    public static final int STATE_CLOSED    = 4;
    public static final int STATE_LISTENING = 2;
    public static final int STATE_OPENED    = 1;
    
    public Channel getChannel(ChannelInfo channelInfo);

    /**
     * Fetches data in the synchronous mode.
     *
     * @param bufferSize the size of the data buffer ( &gt; 0)
     * @return the collected data of all the channels
     * of this sensor
     * @throws IllegalArgumentException - when bufferSize &lt; 1
     * or if bufferSize &gt; the maximum size of the buffer
     * @throws java.io.IOException - if the state is STATE_CLOSED
     * or if any input/output problems are occured
     * @throws java.lang.IllegalStateException - in case of  the
     * state is STATE_LISTENING
     */
    public Data[] getData(int bufferSize) throws java.io.IOException;

    /**
     * Retrieves data in the synchronous mode.
     *
     * @param bufferSize - the size of the data buffer 
     * @param bufferingPeriod - the time to buffer values
     * @param isTimestampIncluded - if true timestamps should be 
     *  included in returned Data objects
     * @param isUncertaintyIncluded - if true uncertainties should be
     *  included in returned Data objects
     * @param isValidityIncluded - if true validities should be
     *  included in returned Data objects
     * @return collected data of all the channels of this sensor.
     * @throws java.lang.IllegalArgumentException - if the both, bufferSize
     *  and bufferingPeriod, have values less than 1, or if bufferSize
     *  exceeds the maximum size of the buffer
     * @throws java.lang.IllegalStateException - if the state is STATE_LISTENING 
     * @throws java.io.IOException - if the state is STATE_CLOSED
     *  or if any input/output problems are occured
     */
    public Data[] getData(int bufferSize,
                      long bufferingPeriod,
                      boolean isTimestampIncluded,
                      boolean isUncertaintyIncluded,
                      boolean isValidityIncluded)
               throws java.io.IOException;
    
    public SensorInfo getSensorInfo();
    public int getState();

    /**
     * Removes the DataListener registered to this SensorConnection.
     *
     * @throws java.lang.IllegalStateException - if this SensorConnection
     * is already closed
     */
    public void removeDataListener();

    /**
     * Registers a DataListener to receive collected data asynchronously.
     *
     * @param listener - DataListener to be registered
     * @param bufferSize - size of the buffer, value must be &gt; 0
     * @throws java.lang.NullPointerException - if the listener is null
     * @throws java.lang.IllegalArgumentException - if the bufferSize &lt; 1,
     *  or if bufferSize exceeds the maximum size of the buffer
     * @throws java.lang.IllegalStateException - if this SensorConnection
     * is already closed
     */
    public void setDataListener(DataListener listener, int bufferSize);

    /**
     * Registers a DataListener to receive collected data asynchronously.
     *
     * @param listener - the listener to be registered
     * @param bufferSize - the size of the buffer of the data values, bufferSize &lt; 1
     * means the size is left undefined
     * @param bufferingPeriod - the time in milliseconds to buffer values inside
     * one Data object. bufferingPeriod &lt; 1 means the period is left undefined.
     * @param isTimestampIncluded - if true timestamps should be included in
     * returned Data objects
     * @param isUncertaintyIncluded - if true uncertainties should be included
     * in returned Data objects
     * @param isValidityIncluded - if true validities should be included in
     * returned Data objects
     * @throws java.lang.NullPointerException - if the listener is null
     * @throws java.lang.IllegalArgumentException - if the bufferSize
     * and the bufferingPeriod both are &lt; 1 or if bufferSize exceeds
     * the maximum size of the buffer
     * @throws java.lang.IllegalStateException - if this SensorConnection is already closed
     */
    public void setDataListener(DataListener listener,
                            int bufferSize,
                            long bufferingPeriod,
                            boolean isTimestampIncluded,
                            boolean isUncertaintyIncluded,
                            boolean isValidityIncluded);

    /**
     * Gets the sensor error codes.
     *
     * @return array of error codes specified for the given sensor 
     */
    public int[] getErrorCodes();

    /**
     * Gets the error description.
     *
     * @param errorCode code of the error
     * @return description of error
     */
    public String getErrorText(int errorCode);
}
