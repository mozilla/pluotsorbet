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

public interface ChannelInfo {

    /** Constant used to indicate the double data type. */
    public static final int     TYPE_DOUBLE = 1;

    /** Constant used to indicate the int data type. */
    public static final int     TYPE_INT    = 2;

    /** Constant used to indicate the Object data type. */
    public static final int     TYPE_OBJECT = 4;

    /**
     * Returns the accuracy of this channel.
     *
     * @return the accuracy of the channel of the sensor
     */
    public float getAccuracy();

    /**
     * Returns the data type of the channel.
     *
     * @return the data type of the channel
     */
    public int getDataType();

    /**
     * This method returns all the measurement ranges
     * of this channel of the sensor.
     *
     * @return all measurement ranges of the channel
     */
    public MeasurementRange[] getMeasurementRanges();

    /**
     * Returns the name of the channel.
     *
     * @return the name of the channel
     */
    public java.lang.String getName();

    /**
     * Returns the scale used for the measurement values of this channel.
     *
     * @return scale
     */
    public int getScale();

    /**
     * Returns the unit, in which data values are presented.
     *
     * @return the unit, in which data values of the channel are presented
     */
    public Unit getUnit();
}