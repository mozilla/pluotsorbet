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

/**
 * The <code>SensorListener</code> represents a listener that receives
 * notifications when the availability of the sensor changes. Classes
 * implementing the SensorListener interface receive following notifications:
 * <ul>
 * <li>{@link #sensorAvailable(SensorInfo)}, the unavailable sensor getting
 * available</li>
 * <li>{@link #sensorUnavailable(SensorInfo)}, the available sensor getting
 * unavailable</li>
 * </ul>
 * In addition to changes in availability, after registration of the
 * <code>SensorListener</code>, either of these notifications is sent to the
 * application based on the availability at that point.
 * </p>
 * <p>
 * Availability does not mean the same as opened, but that the access to the
 * sensor is possible. In the case of the sensor working via a serial cable, it
 * means that the sensor is now plugged in. In the case of the sensor working
 * over Bluetooth it means that the sensor has come within the proximity of the
 * device. The application must implement this interface and register itself to
 * the <code>SensorManager</code> to obtain the sensor availability updates.
 * To stop getting availability notifications the application must remove the
 * registration with the method
 * {@link SensorManager#removeSensorListener(SensorListener)}.
 * </p>
 * <p>
 * The application is responsible for any possible synchronization required in
 * the listener methods. The listener methods must return quickly and should not
 * perform any extensive processing. The method calls are intended as triggers
 * for the application. The application should do all necessary extensive
 * processing in a separate thread and only use these methods to initiate the
 * processing.
 * </p>
 */
public interface SensorListener {

    /**
     * The notification called when the sensor becomes available. The
     * notification is also called immediately if the sensor is already
     * available when adding the listener.
     *
     * @param info the SensorInfo object indicating which sensor is now
     *        available
     */
    void sensorAvailable(SensorInfo info);

    /**
     * The notification called when the sensor becomes unavailable. The
     * notification is also called immediately if the sensor is already
     * unavailable when adding the listener.
     *
     * @param info the SensorInfo object indicating which sensor is now
     *        unavailable
     */
    void sensorUnavailable(SensorInfo info);
}
