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

public interface Data {

    /**
     * Returns the ChannelInfo that tells the data properties
     * of the channel from where the data values were fetched.
     *
     * @return ChannelInfo of the Data
     */
    public ChannelInfo getChannelInfo();

    /**
     * Returns the data values as a double array if the data type
     * of the channel is ChannelInfo.TYPE_DOUBLE.
     *
     * @return the data values as a double array,
     * a zero-length double array if no values have been measured
     * @throws IllegalStateException - if the data type of the
     * channel is not ChannelInfo.TYPE_DOUBLE
     */
    public double[] getDoubleValues();

    /**
     * Returns the data values as an int array if the data type
     * of this channel is ChannelInfo.TYPE_INT.
     *
     * @return the data values as an int array, a zero-length
     * int array if no values have been measured
     * @throws IllegalStateException - if the data type of the
     * channel is not ChannelInfo.TYPE_INT
     */
    public int[] getIntValues();

    /**
     * Returns the data values as an array of Objects if the data
     * type of this channel is ChannelInfo.TYPE_OBJECT.
     *
     * @return the data values as an Object array, a zero-length
     * Object array if no values have been measured
     * @throws IllegalStateException - if the data type of the channel
     * is not ChannelInfo.TYPE_OBJECT
     */
    public Object[] getObjectValues();

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
    public long getTimestamp(int index);

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
    public float getUncertainty(int index);

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
    public boolean isValid(int index);
}
