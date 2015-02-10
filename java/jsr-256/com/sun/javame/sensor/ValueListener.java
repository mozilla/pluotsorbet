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

public interface ValueListener {

    /** Error code: read data is OK. */
    public static final int DATA_READ_OK = 0;
    /** Error code: channel is busy. */
    public static final int	CHANNEL_BUSY = 1;
  	/** Error code: buffer is overflow. */
	  public static final int BUFFER_OVERFLOW = 2;
	  /** Error code: sensor becomes unavailable. */
	  public static final int SENSOR_UNAVAILABLE = 3;
	  /** Error code: other channel error. */
	  public static final int MEASURING_FAIL = 4;

    /**
     * Object value from channel has been received.
     *
     * @param number the channel number
     * @param value the object value
     * @param uncertainty the uncertainty of data
     * @param validity the validity of data
     */
    public void valueReceived(int number, Object[] value, float[] uncertainty,
                              boolean[] validity);

    /**
     * Wrong data reading.
     *
     * @param number the channel number
     * @param errorCode the code error of data reading
     */
    public void dataReadError(int number, int errorCode);
}
